package com.volla.vollaboard.recognition.recognizers.providers

import com.volla.vollaboard.data.InstalledModelReference
import com.volla.vollaboard.recognition.recognizers.RecognizerSource

class VoskServerProvider : RecognizerSourceProvider {
    override fun getInstalledModels(): List<InstalledModelReference> {
        TODO("Not yet implemented")
    }

    override fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource? {
        TODO("Not yet implemented")
    }
}