package com.volla.vollaboard.recognition.recognizers.providers

import com.volla.vollaboard.data.InstalledModelReference
import com.volla.vollaboard.recognition.recognizers.RecognizerSource

interface RecognizerSourceProvider {
    fun getInstalledModels(): Collection<InstalledModelReference>

    fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource?
}