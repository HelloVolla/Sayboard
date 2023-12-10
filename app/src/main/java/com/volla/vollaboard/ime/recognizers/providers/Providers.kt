package com.volla.vollaboard.ime.recognizers.providers

import android.content.Context
import com.volla.vollaboard.Tools
import com.volla.vollaboard.data.InstalledModelReference
import com.volla.vollaboard.data.ModelType
import com.volla.vollaboard.ime.recognizers.RecognizerSource

class Providers(context: Context) {
    private val voskLocalProvider: VoskLocalProvider
    private val providers: List<RecognizerSourceProvider>

    init {
        val providersM = mutableListOf<RecognizerSourceProvider>()
        voskLocalProvider = VoskLocalProvider(context)
        providersM.add(voskLocalProvider)
        if (Tools.VOSK_SERVER_ENABLED) {
            providersM.add(VoskServerProvider())
        }
        providers = providersM
    }

    fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource? {
        return when (localModel.type) {
            ModelType.VoskLocal -> voskLocalProvider.recognizerSourceForModel(localModel)
            else -> null
        }
    }

    fun installedModels(): Collection<InstalledModelReference> {
        return providers.map { it.getInstalledModels() }.flatten()
    }
}