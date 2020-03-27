package cz.muni.pv239.android.util

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {

    companion object {
        private const val PREF_NAME = "prefs_wp"
        private const val USED_EXT_SOURCE = "used_ext_source"
        private const val ACCESS_TOKEN = "access_token"
    }

    private val shared: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var usedExtSource: String?
        get() = shared.getString(USED_EXT_SOURCE, null)
        set(value) = shared.edit().putString(USED_EXT_SOURCE, value).apply()

    var accessToken: String?
        get() = shared.getString(ACCESS_TOKEN, null)
        set(value) = shared.edit().putString(ACCESS_TOKEN, value).apply()
}