package com.volla.vollaboard.ime.recognizers.providers

import android.content.Context
import com.volla.vollaboard.Constants
import com.volla.vollaboard.Tools
import com.volla.vollaboard.data.InstalledModelReference
import com.volla.vollaboard.data.ModelType
import com.volla.vollaboard.ime.recognizers.RecognizerSource
import com.volla.vollaboard.ime.recognizers.VoskLocal
import java.util.Locale

class VoskLocalProvider(private val context: Context) : RecognizerSourceProvider {
    override fun getInstalledModels(): List<InstalledModelReference> {
        val models: MutableList<InstalledModelReference> = ArrayList()
        val modelsDir = Constants.getModelsDirectory(context)
        if (!modelsDir.exists()) return models
        for (localeFolder in modelsDir.listFiles()!!) {
            if (!localeFolder.isDirectory) continue
            val locale = Locale.forLanguageTag(localeFolder.name)
            for (modelFolder in localeFolder.listFiles()!!) {
                if (!modelFolder.isDirectory) continue
//                val name = modelFolder.name
                val model = InstalledModelReference(
                    modelFolder.absolutePath,
                    locale.displayName,
                    ModelType.VoskLocal
                )
                models.add(model)
            }
        }
        return models
    }

    override fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource? {
        return VoskLocal(Tools.getVoskModelFromReference(localModel) ?: return null)
    }
}