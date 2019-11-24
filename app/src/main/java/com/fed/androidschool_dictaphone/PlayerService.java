package com.fed.androidschool_dictaphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PlayerService extends Service {
    public static final String ACTION_NEXT = "Next";
    public static final String ACTION_PREV = "Prev";
    public static final String ACTION_PAUSE = "Pause";
    public static final String ACTION_START = "Start";
    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "002";
    RemoteViews mRemoteView;
    NotificationManager mNotificationManager;
    private Messenger mMessenger = new Messenger(new PlayerHandler());
    private Messenger mMainActivityMessenger;
    private Notification mNotification;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        createNotificationChannel();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());

        if (intent.getAction() != null) {

            Message message = new Message();
            switch (intent.getAction()) {
                case ACTION_START:
                    setButtonsVisibility(View.GONE, View.VISIBLE);


                    message.what = MainActivity.MSG_START;
                    try {
                        mMainActivityMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case ACTION_PAUSE:
                    setButtonsVisibility(View.VISIBLE, View.GONE);

                    message.what = MainActivity.MSG_PAUSE;
                    try {
                        mMainActivityMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                    break;
                case ACTION_NEXT:
                    message.what = MainActivity.MSG_NEXT;
                    try {
                        mMainActivityMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_PREV:
                    message.what = MainActivity.MSG_PREV;
                    try {
                        mMainActivityMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    private void setButtonsVisibility(int startVisibility, int pauseVisibility) {
        int api = Build.VERSION.SDK_INT;
        mRemoteView.setViewVisibility(R.id.start_image_btn, startVisibility);
        mRemoteView.setViewVisibility(R.id.pause_image_btn, pauseVisibility);
        if (api < Build.VERSION_CODES.HONEYCOMB) {
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        } else if (api >= Build.VERSION_CODES.HONEYCOMB) {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, "Player"
                    , NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            mNotificationManager = getSystemService(NotificationManager.class);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private Notification createNotification() {
        Notification customNotification = null;
        Intent startIntent = new Intent(this, PlayerService.class);
        startIntent.setAction(ACTION_START);
        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startIntent, 0);

        Intent pauseIntent = new Intent(this, PlayerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        Intent prevIntent = new Intent(this, PlayerService.class);
        prevIntent.setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, 0);


        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.player_notification);
        remoteViews.setTextViewText(R.id.label_text_view,
                getResources().getString(R.string.label_player_notificaction));
        remoteViews.setOnClickPendingIntent(R.id.start_image_btn, startPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.pause_image_btn, pausePendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.next_image_btn, nextPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.prev_image_btn, prevPendingIntent);

        mRemoteView = remoteViews;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViews);
            customNotification = mBuilder.build();

        }
        mNotification = customNotification;
        return customNotification;
    }

    class PlayerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MainActivity.MSG_START:
                    mMainActivityMessenger = msg.replyTo;
                    setButtonsVisibility(View.GONE, View.VISIBLE);
                    break;
                case MainActivity.MSG_PAUSE:
                    setButtonsVisibility(View.VISIBLE, View.GONE);
                    break;
                case MainActivity.MSG_NEXT:
                    setButtonsVisibility(View.GONE, View.VISIBLE);
                    break;
                case MainActivity.MSG_PREV:
                    setButtonsVisibility(View.GONE, View.VISIBLE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
