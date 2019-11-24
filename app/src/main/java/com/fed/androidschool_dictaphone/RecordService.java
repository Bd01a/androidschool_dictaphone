package com.fed.androidschool_dictaphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RecordService extends Service {

    private static final String TAG = "RecordService";
    private static final String ACTION_CLOSE = "CLOSE_RECORD_SERVISE";

    private static final String CHANNEL_ID = "001";
    private static final int NOTOFICATION_ID = 1;
    AudioRecordHelper mAudioRecordHelper;

    @Override
    public void onCreate() {
        createNotificationChannel();
        mAudioRecordHelper = new AudioRecordHelper();
        mAudioRecordHelper.startRecoding(getFilesDir());
        super.onCreate();
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, "Record"
                    , NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }

        }

    }

    private Notification createNotification() {
        Notification customNotification = null;
        Intent stopIntent = new Intent(this, RecordService.class);
        stopIntent.setAction(ACTION_CLOSE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);


        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notidication_layout);
        remoteViews.setTextViewText(R.id.label_text_view,
                getResources().getString(R.string.label_record_notificaction));
        remoteViews.setTextViewText(R.id.content_text_view,
                getResources().getString(R.string.content_text_record_notificaction));
        remoteViews.setImageViewResource(R.id.content_image_view, R.drawable.ic_launcher_foreground);
        remoteViews.setOnClickPendingIntent(R.id.stop_image_btn, stopPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            customNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViews)
                    .build();

        }
        return customNotification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NOTOFICATION_ID, createNotification());
        if (!TextUtils.isEmpty(intent.getAction())) {
            if (ACTION_CLOSE.equals(intent.getAction())) {
                mAudioRecordHelper.stopRecoding();
                Intent updateIntent = new Intent(MainActivity.ACTION_UPDATE_ADAPTER);
                sendBroadcast(updateIntent);
                stopSelf();
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
