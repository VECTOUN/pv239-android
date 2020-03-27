package cz.muni.pv239.android.util

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    private val prefManager: PrefManager by lazy { PrefManager(context) }

    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("AuthInterceptor", "Adding auth headers.")

        var request = chain.request()
        request = request.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer ${prefManager.accessToken}")
            .addHeader("WP-ExtSource", prefManager.usedExtSource ?: "")
            .build()
        return chain.proceed(request)
    }

}