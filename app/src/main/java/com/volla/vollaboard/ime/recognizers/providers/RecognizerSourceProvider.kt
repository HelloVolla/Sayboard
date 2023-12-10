package com.volla.vollaboard.ime.recognizers.providers

import com.volla.vollaboard.data.InstalledModelReference
import com.volla.vollaboard.ime.recognizers.RecognizerSource

interface RecognizerSourceProvider {
    fun getInstalledModels(): Collection<InstalledModelReference>

    fun recognizerSourceForModel(localModel: InstalledModelReference): RecognizerSource?
}