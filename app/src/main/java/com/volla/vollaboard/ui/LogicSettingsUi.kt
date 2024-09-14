package com.volla.vollaboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.volla.vollaboard.R
import com.volla.vollaboard.data.KeepScreenAwakeMode
import com.volla.vollaboard.vollaboardPreferenceModel
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference

@Composable
fun LogicSettingsUi() = ScrollablePreferenceLayout(vollaboardPreferenceModel()) {
    ListPreference(
        listPref = prefs.logicKeepScreenAwake,
        title = stringResource(id = R.string.logic_keep_screen_awake_title),
        entries = KeepScreenAwakeMode.listEntries()
    )
    SwitchPreference(
        pref = prefs.logicListenImmediately,
        title = stringResource(id = R.string.logic_listen_immediately_title),
        summary = stringResource(
            id = R.string.logic_listen_immediately_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicAutoSwitchBack,
        title = stringResource(id = R.string.logic_auto_switch_back_title),
        summary = stringResource(
            id = R.string.logic_auto_switch_back_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicKeepModelInRam,
        title = stringResource(id = R.string.logic_keep_model_in_ram_title),
        summary = stringResource(
            id = R.string.logic_keep_model_in_ram_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicAutoCapitalize,
        title = stringResource(id = R.string.logic_auto_capitalize_title),
        summary = stringResource(id = R.string.logic_auto_capitalize_summary)
    )
}