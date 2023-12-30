package com.volla.vollaboard.data

import java.util.*

// Locale list available at: https://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android
/**
 *
 */
enum class ModelLink(
    val link: String,
    val locale: Locale,
    private val modelType: ModelType
) {

    WHISPER_TINY_ENGLISH("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
        Locale.ENGLISH,
        ModelType.WhisperLocal),
    /*
    WHISPER_TINY("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin",
        Locale.ENGLISH,
        ModelType.WhisperLocal),
     */
    WHISPER_BASE_ENGLISH("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
        Locale.ENGLISH,
        ModelType.WhisperLocal),
    /*
    WHISPER_BASE("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
        Locale.ENGLISH,
        ModelType.WhisperLocal),
     */

    ENGLISH_US(
        "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
        Locale.US,
        ModelType.VoskLocal
    ),
    ENGLISH_IN(
        "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip",
        Locale("en", "IN"),
        ModelType.VoskLocal
    ),
    CHINESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
        Locale.CHINESE,
        ModelType.VoskLocal
    ),
    RUSSIAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip",
        Locale("ru"),
        ModelType.VoskLocal
    ),
    FRENCH(
        "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip",
        Locale.FRENCH,
        ModelType.VoskLocal
    ),
    GERMAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip",
        Locale.GERMAN,
        ModelType.VoskLocal
    ),
    SPANISH(
        "https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip",
        Locale("es"),
        ModelType.VoskLocal
    ),
    PORTUGUESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip",
        Locale("pt"),
        ModelType.VoskLocal
    ),
    TURKISH(
        "https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip",
        Locale("tr"),
        ModelType.VoskLocal
    ),
    VIETNAMESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-vn-0.3.zip",
        Locale("vi"),
        ModelType.VoskLocal
    ),
    ITALIAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-it-0.22.zip",
        Locale.ITALIAN,
        ModelType.VoskLocal
    ),
    DUTCH(
        "https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip",
        Locale("nl"),
        ModelType.VoskLocal
    ),
    CATALAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip",
        Locale("ca"),
        ModelType.VoskLocal
    ),
    PERSIAN(
        "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.4.zip",
        Locale("fa"),
        ModelType.VoskLocal
    ),
    KAZAKH(
        "https://alphacephei.com/vosk/models/vosk-model-small-kz-0.15.zip",
        Locale("kk"),
        ModelType.VoskLocal
    ),
    JAPANESE(
        "https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip",
        Locale.JAPANESE,
        ModelType.VoskLocal
    ),
    ESPERANTO(
        "https://alphacephei.com/vosk/models/vosk-model-small-eo-0.42.zip",
        Locale("eo"),
        ModelType.VoskLocal
    ),
    HINDI(
        "https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip",
        Locale("hi"),
        ModelType.VoskLocal
    ),
    CZECH(
        "https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip",
        Locale("cs"),
        ModelType.VoskLocal
    ),
    POLISH(
        "https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip",
        Locale("pl"),
        ModelType.VoskLocal
    );

    val filename: String
        get() =
            if (modelType == ModelType.VoskLocal) link.substring(link.lastIndexOf('/') + 1, link.lastIndexOf('.'))
            else link.substring(link.lastIndexOf('/') + 1)

    val displayname: String
        get() =
            if (modelType == ModelType.VoskLocal) locale.displayName
            else  "Whisper (" + (link.substring(link.lastIndexOf('-') + 1, link.lastIndexOf('.'))) + ")"
}