package com.chatty.app.util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.chatty.app.R;
import com.chatty.app.activity.ChatActivity;

public class GenerateNotification {
    @SuppressLint("WrongConstant")
    public static void send(String title, String body, Context context) {
        /*for android version below oreo*/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context,
                    "captionplus-01"
            );
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setContentTitle(title);
            builder.setContentText(body);
            builder.setAutoCancel(true);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            Intent intent = new Intent(context, ChatActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, ChatActivity.class), Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));

            int notificationId = 1;
            notificationManager.notify(notificationId, builder.build());
        }

        /*for android version oreo and above*/
        else {
            int notificationId = 1;
            String channelId = "chatty-01";
            String channelName = "chatty";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance
            );
            notificationManager.createNotificationChannel(mChannel);


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context,
                    channelId
            )
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, ChatActivity.class), Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
            notificationManager.notify(notificationId, mBuilder.build());
        }
    }
}
