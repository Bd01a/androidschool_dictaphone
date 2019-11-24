package com.fed.androidschool_dictaphone;

import android.media.MediaRecorder;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

class AudioRecordHelper {
    private MediaRecorder mMediaRecoder;

    AudioRecordHelper() {
        mMediaRecoder = new MediaRecorder();
        mMediaRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecoder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecoder.setAudioSamplingRate(16000);
        mMediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

    }

    void startRecoding(@NonNull File dir) {
        int number;
        if (dir.list() != null && dir.list().length > 0) {
            number = Integer.valueOf(dir.list()[dir.list().length - 1].substring(6, dir.list()[dir.list().length - 1].length() - 4)) + 1;
        } else {
            number = 1;
        }
        mMediaRecoder.setOutputFile(dir + "/record" + number + ".mp3");
        try {
            mMediaRecoder.prepare();
            mMediaRecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopRecoding() {
        try {
            mMediaRecoder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaRecoder.reset();
        mMediaRecoder.release();
    }


}
