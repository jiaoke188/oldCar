package com.example.carrentalapp.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.carrentalapp.R;

public class ReturnDateReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "return_date_reminder";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String returnDate = intent.getStringExtra("returnDate");

        // 发送通知
        sendNotification(context, returnDate);
    }

    private void sendNotification(Context context, String returnDate) {
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

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("还车日期提醒")
                .setContentText("您有一笔订单需要在 " + returnDate + " 之前还车")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[] { 0, 500, 250, 500 });

        // 发送通知
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
