package com.elishaazaria.sayboard.ime.recognizers;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.elishaazaria.sayboard.data.LocalModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;


class WhisperLib {
    public static native long initContextFromInputStream(InputStream inputStream);
    public static native long initContextFromAsset(AssetManager assetManager, String assetPath);
    public static native long initContext(String modelPath);
    public static native void freeContext(long contextPtr);
    public static native void fullTranscribe(long contextPtr, float[] audioData);
    public static native int getTextSegmentCount(long contextPtr);
    public static native String getTextSegment(long contextPtr, int index);

    private static final String LOG_TAG = "LibWhisper";

    private boolean isArmEabiV7a() {
        return Build.SUPPORTED_ABIS[0].equals("armeabi-v7a");
    }

    private String cpuInfo() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public WhisperLib() {
        String info = cpuInfo();
        if (isArmEabiV7a() && info.contains("vfpv4")) {
            Log.i(LOG_TAG, "Loading libwhisper_vfpv4.so");
            System.loadLibrary("whisper_vfpv4");
        } else {
            Log.i(LOG_TAG, "Loading libwhisper.so");
            System.loadLibrary("whisper");
        }
    }
}

public class WhisperLocal implements RecognizerSource {
    private final MutableLiveData<RecognizerState> stateLD = new MutableLiveData<>(RecognizerState.NONE);
    private final LocalModel localModel;

    private WhisperLib whisperLib;
    private long whisperContext;
    private WhisperRecognizer recognizer;

    public WhisperLocal(LocalModel localModel) {
        this.localModel = localModel;
        this.whisperLib = new WhisperLib();
        this.whisperContext = 0;
    }

    @Override
    public void initialize(Executor executor, Observer<RecognizerSource> onLoaded) {
        stateLD.postValue(RecognizerState.LOADING);

        if (whisperContext != 0) {
            modelLoaded(whisperContext);
        }

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Long context = WhisperLib.initContext(localModel.path);
            handler.post(() -> {
                modelLoaded(context);
                onLoaded.onChanged(this);
            });
        });
    }

    private void modelLoaded(long context) {
        this.whisperContext = context;
        stateLD.postValue(RecognizerState.READY);
        recognizer = new WhisperRecognizer(context, 16000.0f);
    }

    private static class WhisperRecognizer implements Recognizer {
        private final float sampleRate;
        private final long whisperContext;

        public WhisperRecognizer(long context, float sampleRate) {
            this.sampleRate = sampleRate;
            this.whisperContext = context;
        }

        @Override
        public float getSampleRate() {
            return sampleRate;
        }

        @Override
        public void reset() {
        }

        @Override
        public boolean acceptWaveForm(short[] buffer, int nread) {
            return false;
        }

        @Override
        public String getResult() {
            return "";
        }

        @Override
        public String getPartialResult() {
            return "";
        }

        @Override
        public String getFinalResult() {
            return "";
        }
    }

    @Override
    public Recognizer getRecognizer() {
        return recognizer;
    }

    @Override
    public void close(boolean freeRAM) {
        if (freeRAM) {
            if (whisperContext != 0) WhisperLib.freeContext(whisperContext);
            whisperContext = 0;
            stateLD.postValue(RecognizerState.CLOSED);
        } else {
            stateLD.postValue(RecognizerState.IN_RAM);
        }
    }

    @Override
    public LiveData<RecognizerState> getStateLD() {
        return stateLD;
    }

    @Override
    public int getErrorMessage() {
        return 0;
    }

    @Override
    public String getName() {
        return localModel.locale.getDisplayName();
    }
}
