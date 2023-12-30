package com.volla.vollaboard.ime.recognizers

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.volla.vollaboard.data.LocalModel
import com.volla.vollaboard.ime.recognizers.WhisperLib
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executor

internal class WhisperLib {
    private val isArmEabiV7a: Boolean
        get() = Build.SUPPORTED_ABIS[0] == "armeabi-v7a"
    private val isArmEabiV8a: Boolean
        get() = Build.SUPPORTED_ABIS[0] == "arm64-v8a"

    private fun cpuInfo(): String {
        try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            val sb = StringBuilder()
            var line = br.readLine()
            while (line != null) {
                sb.append(line)
                sb.append(System.lineSeparator())
                line = br.readLine()
            }
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    init {
        val info = cpuInfo()
        if (isArmEabiV7a && info.contains("vfpv4")) {
            Log.i(LOG_TAG, "Loading libwhisper_vfpv4.so")
            System.loadLibrary("whisper_vfpv4")
        } else if (isArmEabiV8a && info.contains("fphp")) {
            Log.i(LOG_TAG, "Loading libwhisper_v8fp16_va.so")
            System.loadLibrary("whisper_v8fp16_va")
        } else {
            Log.i(LOG_TAG, "Loading libwhisper.so")
            System.loadLibrary("whisper")
        }
    }

    companion object {
        external fun initContext(modelPath: String?): Long
        external fun freeContext(contextPtr: Long)
        external fun acceptAudio(contextPtr: Long, audioData: FloatArray?): Boolean
        external fun transcribe(contextPtr: Long)
        external fun getTextSegmentCount(contextPtr: Long): Int
        external fun getTextSegment(contextPtr: Long, index: Int): String
        external fun getSampleRate(): Int
        private const val LOG_TAG = "LibWhisper"
    }
}

class WhisperLocal(private val localModel: LocalModel) : RecognizerSource {
    private val stateMLD = MutableLiveData(RecognizerState.NONE)
    override val stateLD: LiveData<RecognizerState>
        get() = stateMLD
    private val whisperLib: WhisperLib = WhisperLib()
    private var whisperContext: Long
    private var myRecognizer: WhisperRecognizer? = null
    override val recognizer: Recognizer
        get() = myRecognizer!!

    init {
        whisperContext = 0
    }

    override fun initialize(executor: Executor, onLoaded: Observer<RecognizerSource?>) {
        stateMLD.postValue(RecognizerState.LOADING)
        if (whisperContext != 0L) {
            modelLoaded(whisperContext)
            return
        }
        val handler = Handler(Looper.getMainLooper())
        executor.execute {

            // TODO: Initialize Whisper with the target languages and
            // Additional features
            val context = WhisperLib.initContext(localModel.path)
            handler.post {
                modelLoaded(context)
                onLoaded.onChanged(this)
            }
        }
    }

    private fun modelLoaded(context: Long) {
        whisperContext = context
        stateMLD.postValue(RecognizerState.READY)
        myRecognizer = WhisperRecognizer(context, WhisperLib.getSampleRate().toFloat())
    }

    override val closed: Boolean
        get() = myRecognizer == null

    override val addSpaces: Boolean
        get() = !listOf("ja", "zh").contains(localModel.locale.language)

    private class WhisperRecognizer(
        private val whisperContext: Long,
        override val sampleRate: Float
    ) : Recognizer {
        private var result = ""
        override fun reset() {
            result = ""
        }

        override fun acceptWaveForm(buffer: ShortArray?, nread: Int): Boolean {
            val floatBuffer = FloatArray(nread)
            for (i in buffer!!.indices) {
                floatBuffer[i] = buffer[i].toFloat() / 32767.0f
            }
            return WhisperLib.acceptAudio(whisperContext, floatBuffer)
        }

        override fun getResult(): String {
            WhisperLib.transcribe(whisperContext)
            val textSegmentCount = WhisperLib.getTextSegmentCount(whisperContext)
            // TODO: Use StringBuilder here
            for (i in 0 until textSegmentCount) {
                result += WhisperLib.getTextSegment(whisperContext, i)
                result += ' '
            }
            return result
        }

        override fun getPartialResult(): String {
            return ""
            /*
            WhisperLib.transcribe(whisperContext);
            int textSegmentCount = WhisperLib.getTextSegmentCount(whisperContext);
            // TODO: Use StringBuilder here
            for (int i = 0; i < textSegmentCount; i++) {
                result += WhisperLib.getTextSegment(whisperContext, i);
                result += ' ';
            }
            return result;
             */
        }

        override fun getFinalResult(): String {
            return result
        }

        override val locale: Locale?
            get() = null
    }


    override fun close(freeRAM: Boolean) {
        if (freeRAM) {
            if (whisperContext != 0L) WhisperLib.freeContext(whisperContext)
            whisperContext = 0
            stateMLD.postValue(RecognizerState.CLOSED)
        } else {
            stateMLD.postValue(RecognizerState.IN_RAM)
        }
    }

    override val errorMessage: Int
        get() = 0
    override val name: String
        get() = localModel.locale.displayName
}