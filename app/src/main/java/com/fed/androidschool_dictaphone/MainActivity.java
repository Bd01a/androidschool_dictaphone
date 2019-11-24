package com.fed.androidschool_dictaphone;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_UPDATE_ADAPTER = "action_update_adapter";
    public static final int MSG_NEXT = 111;
    public static final int MSG_PREV = 112;
    public static final int MSG_PAUSE = 113;
    public static final int MSG_START = 114;
    private static final int REQUEST_CODE = 1;
    MediaPlayer mMediaPlayer;
    private RecyclerView mRecyclerView;
    private Messenger mPlayerMessenger;
    private Messenger mMainActivityMessenger = new Messenger(new MainActivityHandler());
    private boolean mIsBound;
    private boolean mIsPlaying = false;
    private View mStartButton;
    private View mPauseButton;
    private OnGetChosenRecord mOnGetChosen;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerMessenger = new Messenger(service);
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerMessenger = null;
            mIsBound = false;
        }
    };
    private BroadcastReceiver mBrodcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE_ADAPTER.equals(intent.getAction())) {
                updateAdapter();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }

        Button startRecordButton = findViewById(R.id.btn_start_record);
        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordService.class);
                startService(intent);
            }
        });

        mStartButton = findViewById(R.id.btn_start_play);
        mPauseButton = findViewById(R.id.btn_pause_play);
        final View nextButton = findViewById(R.id.btn_next_play);
        View prevButton = findViewById(R.id.btn_prev_play);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFilesDir().listFiles().length > 0) {
                    buttonPlay();
                    Message message = new Message();
                    message.what = MainActivity.MSG_START;
                    message.replyTo = mMainActivityMessenger;
                    try {
                        mPlayerMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFilesDir().listFiles().length > 0) {
                    buttonPause();
                    Message message = new Message();
                    message.what = MainActivity.MSG_PAUSE;
                    try {
                        mPlayerMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFilesDir().listFiles().length > 0) {
                    buttonChangeTrack(1);
                }
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFilesDir().listFiles().length > 0) {
                    buttonChangeTrack(-1);
                }
            }
        });

        registerReceiver(mBrodcastReceiver, new IntentFilter(ACTION_UPDATE_ADAPTER));
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecordsAdapter recordsAdapter = new RecordsAdapter(getFilesDir(),
                new OnChosenChange() {
                    @Override
                    public void onChange() {
                        if (mMediaPlayer != null) {
                            if (mMediaPlayer.isPlaying()) {
                                stopPlay();
                                buttonPlay();
                            } else {
                                stopPlay();
                            }
                        }
                    }
                });
        mOnGetChosen = recordsAdapter.getChosenRecord();
        mRecyclerView.setAdapter(recordsAdapter);

    }

    private void buttonPlay() {

        mPauseButton.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.GONE);
        if (!mIsPlaying) {
            Intent intent = new Intent(MainActivity.this, PlayerService.class);
            startService(intent);
            mIsPlaying = true;

            startPlay(getFilesDir().listFiles()[mOnGetChosen.getChosen()].getAbsolutePath());

        } else {
            resumePlay();
        }

    }

    private void buttonPause() {
        mStartButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.GONE);

        pausePlay();
    }

    private void buttonChangeTrack(int i) {
        stopPlay();
        mOnGetChosen.changeChosen(i);
        buttonPlay();
    }

    private void updateAdapter() {
        RecordsAdapter adapter = ((RecordsAdapter) mRecyclerView.getAdapter());
        if (adapter != null) {
            adapter.updateData(getFilesDir());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }

    }

    void startPlay(String path) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mStartButton.setVisibility(View.VISIBLE);
                    mPauseButton.setVisibility(View.GONE);
                    mIsPlaying = false;

                    Message message = new Message();
                    message.what = MainActivity.MSG_PAUSE;
                    message.replyTo = mMainActivityMessenger;
                    try {
                        mPlayerMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            });
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setVolume(100, 100);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void pausePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    void resumePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    void stopPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mIsPlaying = false;
    }

    interface OnGetChosenRecord {
        int getChosen();

        void changeChosen(int delta);
    }

    interface OnChosenChange {
        void onChange();
    }

    private class MainActivityHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START:
                    buttonPlay();
                    break;
                case MSG_PAUSE:
                    buttonPause();
                    break;
                case MSG_PREV:
                    buttonChangeTrack(-1);
                    break;
                case MSG_NEXT:
                    buttonChangeTrack(1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
