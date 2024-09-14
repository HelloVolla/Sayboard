package com.volla.vollaboard.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.volla.vollaboard.R
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries

enum class KeepScreenAwakeMode {
    NEVER, WHEN_LISTENING, WHEN_OPEN;

    companion object {
        @Composable
        fun listEntries() = listPrefEntries {
            entry(
                key = NEVER,
                label = stringResource(id = R.string.p_keep_screen_awake_mode_never),
            )
            entry(
                key = WHEN_LISTENING,
                label = stringResource(id = R.string.p_keep_screen_awake_mode_when_listening),
            )
            entry(
                key = WHEN_OPEN,
                label = stringResource(id = R.string.p_keep_screen_awake_mode_when_open),
            )
        }
    }
}