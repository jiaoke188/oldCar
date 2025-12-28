package com.example.carrentalapp.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.DataRepository;
import com.example.carrentalapp.data.RepositoryCallback;
import com.example.carrentalapp.data.entity.CarEntity;

public class ReturnDateReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "return_date_reminder";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String returnDate = intent.getStringExtra("returnDate");
        long carId = intent.getLongExtra("carId", -1);
        String carName = intent.getStringExtra("carName");

        // 如果有车辆ID，获取最新的车辆库存信息
        if (carId > 0) {
            DataRepository repository = DataRepository.getInstance(context);
            repository.loadCarById(carId, new RepositoryCallback<CarEntity>() {
                @Override
                public void onComplete(CarEntity result) {
                    if (result != null) {
                        sendNotificationWithCarInfo(context, returnDate, result);
                    } else {
                        sendNotification(context, returnDate, carName, 0);
                    }
                }
            });
        } else {
            // 发送通知（无车辆信息）
            sendNotification(context, returnDate, carName, 0);
        }
    }

    private void sendNotificationWithCarInfo(Context context, String returnDate, CarEntity car) {
        int inventory = car.getInventory();
        sendNotification(context, returnDate, car.getName(), inventory);
    }

    private void sendNotification(Context context, String returnDate, String carName, int inventory) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建通知频道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "还车日期提醒",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("提醒您即将到达还车日期");
            notificationManager.createNotificationChannel(channel);
        }

        // 构建通知内容
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("您有一笔订单需要在 ").append(returnDate).append(" 之前还车");
        if (carName != null && !carName.isEmpty()) {
            contentBuilder.append(" | 车辆: ").append(carName);
        }
        if (inventory >= 0) {
            contentBuilder.append(" | 库存: ").append(inventory);
        }

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("还车日期提醒")
                .setContentText(contentBuilder.toString())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[] { 0, 500, 250, 500 });

        // 发送通知
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
