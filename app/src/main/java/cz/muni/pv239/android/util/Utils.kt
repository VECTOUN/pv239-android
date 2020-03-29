package cz.muni.pv239.android.util

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient

fun getHttpClient(context: Context?): OkHttpClient {
    val clientBuilder = OkHttpClient.Builder()
    if (context != null) {
        clientBuilder.addInterceptor(AuthInterceptor(context))
    } else {
        Log.w("", "Created Http client without interceptor")
    }
    return clientBuilder.build();
}