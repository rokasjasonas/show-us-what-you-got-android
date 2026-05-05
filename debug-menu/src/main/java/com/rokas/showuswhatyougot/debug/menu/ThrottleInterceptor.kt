package com.rokas.showuswhatyougot.debug.menu

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThrottleInterceptor @Inject constructor(
    private val config: NetworkThrottleConfig,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (config.enabled.value) {
            Thread.sleep(config.delayMillis)
        }
        return chain.proceed(chain.request())
    }
}

