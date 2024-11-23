package com.ww.simpletv

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.e("RetryInterceptor", "intercept：download github iptv file fail, retry")
        val request = chain.request()
        val url = API.URL_IPTV
        val newRequest = request.newBuilder().url(url).build()
        return chain.proceed(newRequest)
    }
}