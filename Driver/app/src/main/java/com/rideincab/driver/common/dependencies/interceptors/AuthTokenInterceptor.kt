package com.rideincab.driver.common.dependencies.interceptors

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage dependencies.interceptors
 * @category AuthTokenInterceptor
 * @author SMR IT Solutions
 *
 */

import com.rideincab.driver.common.configs.SessionManager

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/*****************************************************************
 * Auth Token Interceptor
 */
class AuthTokenInterceptor(private val sessionManager: SessionManager) : Interceptor {
    private var requestBuilder: Request.Builder? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val original = chain.request()
            if (sessionManager.accessToken != null) {
                // Request customization: add request headers
                requestBuilder = original.newBuilder().header("Authorization", sessionManager.accessToken!!).method(original.method, original.body)
            } else {
                // Request customization: add request headers
                requestBuilder = original.newBuilder().method(original.method, original.body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val request = requestBuilder!!.build()
        return chain.proceed(request)
    }
}
