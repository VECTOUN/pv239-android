package cz.muni.pv239.android.util

import android.content.Context
import android.content.SharedPreferences
import cz.muni.pv239.android.model.User

class PrefManager(context: Context?) {

    companion object {
        private const val PREF_NAME = "prefs_wp"
        private const val USED_EXT_SOURCE = "used_ext_source"
        private const val ACCESS_TOKEN = "access_token"
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
    }

    private val shared: SharedPreferences? =
        context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var usedExtSource: String?
        get() = shared?.getString(USED_EXT_SOURCE, null)
        set(value) = shared?.edit()?.putString(USED_EXT_SOURCE, value)!!.apply()

    var accessToken: String?
        get() = shared?.getString(ACCESS_TOKEN, null)
        set(value) = shared?.edit()?.putString(ACCESS_TOKEN, value)!!.apply()

    var userId: Long
        get() = shared?.getLong(USER_ID, -1) ?: -1
        set(value) = shared?.edit()?.putLong(USER_ID, value)!!.apply()

    var userName: String
        get() = shared?.getString(USER_NAME, "") ?: ""
        set(value) = shared?.edit()?.putString(USER_NAME, value)!!.apply()
}