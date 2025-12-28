package com.example.carrentalapp.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.carrentalapp.data.dao.AppLogDao;
import com.example.carrentalapp.data.dao.CarCommentDao;
import com.example.carrentalapp.data.dao.CarDao;
import com.example.carrentalapp.data.dao.FavoriteDao;
import com.example.carrentalapp.data.dao.RentalOrderDao;
import com.example.carrentalapp.data.dao.UserDao;
import com.example.carrentalapp.data.entity.AppLogEntity;
import com.example.carrentalapp.data.entity.CarCommentEntity;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.data.entity.FavoriteEntity;
import com.example.carrentalapp.data.entity.RentalOrderEntity;
import com.example.carrentalapp.data.entity.UserEntity;
import com.example.carrentalapp.data.model.CarCommentWithUser;
import com.example.carrentalapp.data.model.FavoriteWithCar;
import com.example.carrentalapp.data.model.OrderWithDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DataRepository {

    private static volatile DataRepository INSTANCE;

    private final AppDatabase database;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private DataRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    public static DataRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void login(String username, String password, RepositoryCallback<UserEntity> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            UserDao userDao = database.userDao();
            UserEntity user = userDao.login(username, password);
            dispatch(callback, user);
            if (user != null) {
                logAsync("用户登录", String.format(Locale.CHINA, "%s 登录系统", username), username);
            }
        });
    }

    public void registerUser(UserEntity entity, RepositoryCallback<Long> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            UserDao userDao = database.userDao();
            UserEntity existed = userDao.findByUsername(entity.getUsername());
            if (existed != null) {
                dispatch(callback, -1L);
                return;
            }
            entity.setCreatedAt(System.currentTimeMillis());
            entity.setActive(true);
            long id = userDao.insert(entity);
            logAsync("用户注册", String.format(Locale.CHINA, "%s 注册成功", entity.getUsername()), entity.getUsername());
            dispatch(callback, id);
        });
    }

    public void loadAllUsers(RepositoryCallback<List<UserEntity>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.userDao().loadAll()));
    }

    public void searchUsers(String keyword, RepositoryCallback<List<UserEntity>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            if (TextUtils.isEmpty(keyword)) {
                dispatch(callback, database.userDao().loadAll());
            } else {
                dispatch(callback, database.userDao().search(keyword));
            }
        });
    }

    public void loadUserById(long userId, RepositoryCallback<UserEntity> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.userDao().findById(userId)));
    }

    public void saveUser(UserEntity entity, String operator, RepositoryCallback<Long> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            UserDao userDao = database.userDao();
            long id = entity.getId();
            if (id == 0) {
                entity.setCreatedAt(System.currentTimeMillis());
                long newId = userDao.insert(entity);
                logAsync("系统用户管理", String.format(Locale.CHINA, "添加系统用户 %s", entity.getUsername()), operator);
                dispatch(callback, newId);
            } else {
                userDao.update(entity);
                logAsync("系统用户管理", String.format(Locale.CHINA, "更新系统用户 %s", entity.getUsername()), operator);
                dispatch(callback, id);
            }
        });
    }

    public void deleteUser(UserEntity entity, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            database.userDao().delete(entity);
            logAsync("系统用户管理", String.format(Locale.CHINA, "删除系统用户 %s", entity.getUsername()), operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    public void loadAllCars(RepositoryCallback<List<CarEntity>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.carDao().loadAll()));
    }

    public void searchCars(String keyword, RepositoryCallback<List<CarEntity>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            if (TextUtils.isEmpty(keyword)) {
                dispatch(callback, database.carDao().loadAll());
            } else {
                dispatch(callback, database.carDao().search(keyword));
            }
        });
    }

    public void saveCar(CarEntity entity, String operator, RepositoryCallback<Long> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            CarDao carDao = database.carDao();
            long id = entity.getId();
            if (id == 0) {
                entity.setCreatedAt(System.currentTimeMillis());
                long newId = carDao.insert(entity);
                logAsync("车辆管理", String.format(Locale.CHINA, "发布车辆 %s", entity.getName()), operator);
                dispatch(callback, newId);
            } else {
                carDao.update(entity);
                logAsync("车辆管理", String.format(Locale.CHINA, "更新车辆 %s", entity.getName()), operator);
                dispatch(callback, id);
            }
        });
    }

    public void deleteCar(CarEntity entity, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            database.carDao().delete(entity);
            logAsync("车辆管理", String.format(Locale.CHINA, "删除车辆 %s", entity.getName()), operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    public void decreaseCarInventory(long carId, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            CarDao carDao = database.carDao();
            CarEntity car = carDao.findById(carId);
            if (car == null) {
                dispatch(callback, Boolean.FALSE);
                return;
            }
            if (car.getInventory() <= 0) {
                dispatch(callback, Boolean.FALSE);
                return;
            }
            car.setInventory(car.getInventory() - 1);
            carDao.update(car);
            logAsync("车辆管理", String.format(Locale.CHINA, "%s 支付成功，车辆 %s 库存减少 1", operator, car.getName()), operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    public void increaseCarInventory(long carId, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            CarDao carDao = database.carDao();
            CarEntity car = carDao.findById(carId);
            if (car == null) {
                dispatch(callback, Boolean.FALSE);
                return;
            }
            car.setInventory(car.getInventory() + 1);
            carDao.update(car);
            logAsync("车辆管理", String.format(Locale.CHINA, "%s 提前还车，车辆 %s 库存增加 1", operator, car.getName()), operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    public void loadCarById(long carId, RepositoryCallback<CarEntity> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.carDao().findById(carId)));
    }

    public void loadOrderById(long orderId, RepositoryCallback<RentalOrderEntity> callback) {
        AppDatabase.getDatabaseExecutor()
                .execute(() -> dispatch(callback, database.rentalOrderDao().findById(orderId)));
    }

    public void loadCommentsByCar(long carId, RepositoryCallback<List<CarCommentWithUser>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.carCommentDao().loadByCar(carId)));
    }

    public void addComment(long carId, long userId, String content, String operator,
            RepositoryCallback<Long> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            CarCommentEntity entity = new CarCommentEntity();
            entity.setCarId(carId);
            entity.setUserId(userId);
            entity.setContent(content);
            entity.setCreatedAt(System.currentTimeMillis());
            long id = database.carCommentDao().insert(entity);
            logAsync("商品评论", String.format(Locale.CHINA, "用户 %s 评论车辆 %d", operator, carId), operator);
            dispatch(callback, id);
        });
    }

    public void toggleFavorite(long userId, long carId, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            FavoriteDao favoriteDao = database.favoriteDao();
            FavoriteEntity existed = favoriteDao.find(userId, carId);
            if (existed != null) {
                favoriteDao.delete(existed);
                logAsync("收藏夹", String.format(Locale.CHINA, "%s 取消收藏车辆 %d", operator, carId), operator);
                dispatch(callback, Boolean.FALSE);
            } else {
                FavoriteEntity entity = new FavoriteEntity();
                entity.setUserId(userId);
                entity.setCarId(carId);
                entity.setCreatedAt(System.currentTimeMillis());
                favoriteDao.insert(entity);
                logAsync("收藏夹", String.format(Locale.CHINA, "%s 收藏车辆 %d", operator, carId), operator);
                dispatch(callback, Boolean.TRUE);
            }
        });
    }

    public void isFavorite(long userId, long carId, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            FavoriteEntity existed = database.favoriteDao().find(userId, carId);
            dispatch(callback, existed != null);
        });
    }

    public void loadFavorites(long userId, RepositoryCallback<List<FavoriteWithCar>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.favoriteDao().loadByUser(userId)));
    }

    public void loadOrders(RepositoryCallback<List<OrderWithDetail>> callback) {
        AppDatabase.getDatabaseExecutor()
                .execute(() -> dispatch(callback, database.rentalOrderDao().loadAllWithDetail()));
    }

    public void searchOrders(String keyword, RepositoryCallback<List<OrderWithDetail>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            if (TextUtils.isEmpty(keyword)) {
                dispatch(callback, database.rentalOrderDao().loadAllWithDetail());
            } else {
                dispatch(callback, database.rentalOrderDao().searchWithDetail(keyword));
            }
        });
    }

    public void saveOrder(RentalOrderEntity entity, String operator, RepositoryCallback<Long> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            RentalOrderDao dao = database.rentalOrderDao();
            long id = entity.getId();
            if (id == 0) {
                entity.setCreatedAt(System.currentTimeMillis());
                if (TextUtils.isEmpty(entity.getOrderCode())) {
                    entity.setOrderCode(generateOrderCode());
                }
                long newId = dao.insert(entity);
                logAsync("租赁管理", String.format(Locale.CHINA, "%s 创建订单 %s", operator, entity.getOrderCode()), operator);
                dispatch(callback, newId);
            } else {
                dao.update(entity);
                logAsync("租赁管理", String.format(Locale.CHINA, "%s 更新订单 %s", operator, entity.getOrderCode()), operator);
                dispatch(callback, id);
            }
        });
    }

    public void deleteOrder(RentalOrderEntity entity, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            database.rentalOrderDao().delete(entity);
            logAsync("租赁管理", String.format(Locale.CHINA, "%s 删除订单 %s", operator, entity.getOrderCode()), operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    public void updateOrderStatus(long orderId, String status, String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            RentalOrderEntity order = database.rentalOrderDao().findById(orderId);
            if (order == null) {
                dispatch(callback, Boolean.FALSE);
                return;
            }
            order.setStatus(status);
            database.rentalOrderDao().update(order);
            logAsync("租赁管理", String.format(Locale.CHINA, "%s 更新订单状态 %s", operator, order.getOrderCode()), operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    public void exportOrders(Context context, RepositoryCallback<File> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            List<OrderWithDetail> orders = database.rentalOrderDao().loadAllWithDetail();
            JSONArray array = new JSONArray();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            for (OrderWithDetail order : orders) {
                JSONObject item = new JSONObject();
                try {
                    item.put("orderCode", order.getOrderCode());
                    item.put("carName", order.getCarName());
                    item.put("userName", order.getUserName());
                    item.put("startDate", format.format(new Date(order.getStartDate())));
                    item.put("endDate", format.format(new Date(order.getEndDate())));
                    item.put("status", order.getStatus());
                    item.put("totalDays", order.getTotalDays());
                    item.put("totalAmount", order.getTotalAmount());
                    item.put("notes", order.getNotes());
                } catch (Exception ignored) {
                }
                array.put(item);
            }
            File exportDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, "orders.json");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                String json = array.toString(2);
                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                logAsync("租赁管理", "导出订单数据", "系统");
                dispatch(callback, file);
            } catch (IOException | JSONException e) {
                dispatch(callback, null);
            }
        });
    }

    public void loadLogs(RepositoryCallback<List<AppLogEntity>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> dispatch(callback, database.appLogDao().loadAll()));
    }

    public void searchLogs(String keyword, RepositoryCallback<List<AppLogEntity>> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            AppLogDao dao = database.appLogDao();
            if (TextUtils.isEmpty(keyword)) {
                dispatch(callback, dao.loadAll());
            } else {
                dispatch(callback, dao.search(keyword));
            }
        });
    }

    public void clearLogs(String operator, RepositoryCallback<Boolean> callback) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            database.appLogDao().clear();
            logAsync("日志管理", "清空系统日志", operator);
            dispatch(callback, Boolean.TRUE);
        });
    }

    private void logAsync(String module, String message, String operator) {
        AppDatabase.getDatabaseExecutor().execute(() -> {
            AppLogEntity entity = new AppLogEntity();
            entity.setModule(module);
            entity.setLevel("INFO");
            entity.setMessage(message);
            entity.setOperator(operator);
            entity.setCreatedAt(System.currentTimeMillis());
            database.appLogDao().insert(entity);
        });
    }

    private <T> void dispatch(RepositoryCallback<T> callback, T result) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onComplete(result));
    }

    @NonNull
    private String generateOrderCode() {
        return String.format(Locale.CHINA, "ORD-%s",
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.CHINA));
    }
}
