package com.volla.vollaboard.downloader.messages

import java.util.*

data class Status(
    val current: ModelInfo?,
    val queued: Queue<ModelInfo>,
    val downloadProgress: Float,
    val unzipProgress: Float,
    val state: State
)