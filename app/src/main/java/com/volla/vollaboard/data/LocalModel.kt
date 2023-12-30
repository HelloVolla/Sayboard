package com.volla.vollaboard.data

import java.io.Serializable
import java.util.*

data class LocalModel(val path: String, val locale: Locale, val filename: String, val modelType: ModelType) : Serializable {

    companion object {
        fun serialize(model: LocalModel): String {
            return "[path:\"" + encode(model.path) +
                    "\", locale:\"" + model.locale +
                    "\", modeltype:\"" + (if (model.modelType == ModelType.VoskLocal) "VoskLocal" else "WhisperLocal") +
                    "\", name:\"" + encode(model.filename) + "\"]"
        }

        fun deserialize(serialized: String?): LocalModel {

            throw RuntimeException() // TODO: implement
        }

        private fun encode(s: String?): String {
            val sb = StringBuilder()
            var c: Char
            for (i in 0 until s!!.length) {
                c = s[i]
                when (c) {
                    ',', '"', '\\', ':' -> {
                        sb.append("\\")
                        sb.append(String.format("%02x", c.code))
                    }

                    else -> sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun decode(s: String): String {
            val sb = StringBuilder()
            var c: Char
            var i = 0
            while (i < s.length) {
                c = s[i]
                if (c == '\\') {
                    i++
                    sb.append(s.substring(i, i + 2).toInt().toChar())
                    i += 2
                } else {
                    sb.append(c)
                }
                i++
            }
            return sb.toString()
        }
    }
}