package com.volla.vollaboard.ime.recognizers.providers

import android.content.Context;
import com.volla.vollaboard.Constants
import com.volla.vollaboard.data.InstalledModelReference
import com.volla.vollaboard.data.LocalModel
import com.volla.vollaboard.data.ModelType
import com.volla.vollaboard.ime.recognizers.RecognizerSource
import com.volla.vollaboard.ime.recognizers.WhisperLocal
import java.util.Locale

private const val GGML = "ggml-"

class WhisperLocalProvider(private val context: Context): RecognizerSourceProvider {
    override fun getInstalledModels(): Collection<InstalledModelReference> {
        val models: MutableList<InstalledModelReference> = ArrayList()
        val modelsDir = Constants.getModelsDirectory(context)
        if (!modelsDir.exists()) return models
        for (localeFolder in modelsDir.listFiles()!!) {
            if (!localeFolder.isDirectory) continue
            for (entry in localeFolder.listFiles()!!) {
                if (entry.isDirectory) continue
                if (!entry.name.startsWith(GGML)) continue
                val modelName = entry.name.replace(GGML, "Whisper ").replace(".bin", "")
                val model = InstalledModelReference(
                    entry.absolutePath,
                    modelName,
                    ModelType.WhisperLocal
                )
                models.add(model)
            }
        }
        return models
    }

    override fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource? {
        // FIXME: Whisper Models support multiple locales,
        // Do NOT hardcode this
        return WhisperLocal(LocalModel(localModel.path, Locale.ENGLISH, localModel.name, localModel.type));
    }
}