package com.example.melpapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.nanoTime()
        val response = chain.proceed(request)
        val end = System.nanoTime()

        val durationMs = (end - start) / 1_000_000
        Timber.d("API ${request.url} -> ${response.code} in ${durationMs}ms")

        if (response.code == 401) {
            Timber.e("Unauthorized (401)")
        } else if (response.code >= 500) {
            Timber.e("Server error: ${response.code}")
        }

        return response
    }
}
