package com.example.carrentalapp.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        UserEntity.class,
        CarEntity.class,
        RentalOrderEntity.class,
        CarCommentEntity.class,
        FavoriteEntity.class,
        AppLogEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    private static final ExecutorService DATABASE_EXECUTOR = Executors.newFixedThreadPool(4);

    public abstract UserDao userDao();

    public abstract CarDao carDao();

    public abstract RentalOrderDao rentalOrderDao();

    public abstract CarCommentDao carCommentDao();

    public abstract FavoriteDao favoriteDao();

    public abstract AppLogDao appLogDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "car_rental_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    DATABASE_EXECUTOR.execute(() -> seedInitialData());
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService getDatabaseExecutor() {
        return DATABASE_EXECUTOR;
    }

    private static void seedInitialData() {
        if (INSTANCE == null) {
            return;
        }
        long now = System.currentTimeMillis();

        // 创建系统默认用户，确保初始登录账号可用
        UserEntity superAdmin = new UserEntity();
        superAdmin.setUsername("admin");
        superAdmin.setPassword("123456");
        superAdmin.setRole(UserRole.SUPER_ADMIN);
        superAdmin.setDisplayName("超级管理员");
        superAdmin.setActive(true);
        superAdmin.setCreatedAt(now);

        UserEntity merchant = new UserEntity();
        merchant.setUsername("merchant");
        merchant.setPassword("123456");
        merchant.setRole(UserRole.MERCHANT);
        merchant.setDisplayName("旗舰商家");
        merchant.setPhone("13800000000");
        merchant.setActive(true);
        merchant.setCreatedAt(now);

        UserEntity customer = new UserEntity();
        customer.setUsername("customer");
        customer.setPassword("123456");
        customer.setRole(UserRole.CUSTOMER);
        customer.setDisplayName("体验客户");
        customer.setPhone("13900000000");
        customer.setActive(true);
        customer.setCreatedAt(now);

        long adminId = INSTANCE.userDao().insert(superAdmin);
        long merchantId = INSTANCE.userDao().insert(merchant);
        long customerId = INSTANCE.userDao().insert(customer);

        // 准备演示车辆数据，方便课程汇报展示
        CarEntity suv = new CarEntity();
        suv.setName("豪华SUV");
        suv.setBrand("丰田");
        suv.setCategory("SUV");
        suv.setDailyPrice(688);
        suv.setStatus("可出租");
        suv.setInventory(5);
        suv.setDescription("城市与越野皆宜的7座SUV");
        suv.setOwnerId(merchantId);
        suv.setCreatedAt(now);

        CarEntity sedan = new CarEntity();
        sedan.setName("商务轿车");
        sedan.setBrand("奥迪");
        sedan.setCategory("轿车");
        sedan.setDailyPrice(598);
        sedan.setStatus("可出租");
        sedan.setInventory(3);
        sedan.setDescription("舒适静音，适合商务接待");
        sedan.setOwnerId(merchantId);
        sedan.setCreatedAt(now);

        long suvId = INSTANCE.carDao().insert(suv);
        long sedanId = INSTANCE.carDao().insert(sedan);

        RentalOrderEntity demoOrder = new RentalOrderEntity();
        demoOrder.setOrderCode(generateOrderCode());
        demoOrder.setCarId(suvId);
        demoOrder.setUserId(customerId);
        demoOrder.setStartDate(now);
        demoOrder.setEndDate(now + 2 * 24 * 60 * 60 * 1000L);
        demoOrder.setTotalDays(2);
        demoOrder.setTotalAmount(2 * suv.getDailyPrice());
        demoOrder.setStatus("已预定");
        demoOrder.setCreatedAt(now);
        demoOrder.setNotes("课程演示订单");
        INSTANCE.rentalOrderDao().insert(demoOrder);

        CarCommentEntity comment = new CarCommentEntity();
        comment.setCarId(suvId);
        comment.setUserId(customerId);
        comment.setContent("车辆舒适，服务专业");
        comment.setCreatedAt(now);
        INSTANCE.carCommentDao().insert(comment);

        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setUserId(customerId);
        favorite.setCarId(sedanId);
        favorite.setCreatedAt(now);
        INSTANCE.favoriteDao().insert(favorite);

        AppLogEntity log = new AppLogEntity();
        log.setModule("系统初始化");
        log.setLevel("INFO");
        log.setMessage("已初始化默认账号与演示数据");
        log.setOperator("系统");
        log.setCreatedAt(now);
        INSTANCE.appLogDao().insert(log);
    }

    private static String generateOrderCode() {
        return String.format(Locale.CHINA, "ORD-%s", UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.CHINA));
    }
}
