package com.rideincab.user.common.utils

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage utils
 * @category RequestCallback
 * @author SMR IT Solutions
 *
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.text.TextUtils
import android.util.Log
import com.rideincab.user.BuildConfig
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.taxi.views.signinsignup.SigninSignupActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/*****************************************************************
 * RequestCallback
 */
class RequestCallback : Callback<ResponseBody> {

    @Inject
    lateinit var jsonResp: JsonResponse
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var commonMethods: CommonMethods
    lateinit private var listener: ServiceListener
    private var requestCode = 0
    private var requestData = ""

    private val isOnline: Boolean
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

    constructor() {
        AppController.appComponent.inject(this)
    }

    constructor(listener: ServiceListener) {
        AppController.appComponent.inject(this)
        this.listener = listener
    }

    constructor(requestCode: Int, listener: ServiceListener) {
        AppController.appComponent.inject(this)
        this.listener = listener
        this.requestCode = requestCode
    }

    constructor(requestCode: Int, listener: ServiceListener, requestData: String) {
        AppController.appComponent.inject(this)
        this.listener = listener
        this.requestCode = requestCode
        this.requestData = requestData
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        this.listener.onSuccess(getSuccessResponse(call, response)!!, requestData)
    }

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        this.listener.onFailure(getFailureResponse(call, t)!!, requestData)
    }

    private fun getFailureResponse(call: Call<ResponseBody>?, t: Throwable): JsonResponse? {
        try {
            jsonResp.clearAll()
            if (call != null && call.request() != null) {
                jsonResp.method = call.request().method
                jsonResp.requestCode = requestCode
                jsonResp.url = call.request().url.toString()
                LogManager.i(call.request().toString())
            }
            jsonResp.isOnline = isOnline
            if (isOnline) {
                jsonResp.statusMsg = context.resources.getString(R.string.internal_server_error)
            } else {
                jsonResp.statusMsg = context.resources.getString(R.string.network_failure)
            }
            jsonResp.errorMsg = t.message!!
            jsonResp.isSuccess = false
            requestData = (if (!isOnline) context.resources.getString(R.string.network_failure) else t.message).toString()
            LogManager.e(t.message!!)
            LogManager.e(requestCode.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return jsonResp
    }

    private fun getSuccessResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?): JsonResponse? {
        try {
            jsonResp.clearAll()
            if (call != null && call.request() != null) {
                jsonResp.method = call.request().method
                jsonResp.requestCode = requestCode
                jsonResp.url = call.request().url.toString()
                LogManager.i(call.request().toString())
            }
            if (response != null) {
                jsonResp.responseCode = response.code()
                jsonResp.isSuccess = false
                jsonResp.statusMsg = context.resources.getString(R.string.internal_server_error)
                if (response.isSuccessful && response.body() != null) {
                    val strJson = response.body()!!.string()

                    if (BuildConfig.DEBUG)
                        Log.i("API_CALLS", "getSuccessResponse: strJson=${strJson}")

                    jsonResp.strResponse = strJson
                    jsonResp.statusMsg = getStatusMsg(strJson)
                    jsonResp.statusCode = getStatusCode(strJson)
                    if (jsonResp.statusMsg.equals("Token Expired", ignoreCase = true)) {
                        jsonResp.statusMsg = ""
                    }
                    jsonResp.isSuccess = isSuccess(strJson)
                    if (BuildConfig.DEBUG) {
                        LogManager.e(strJson)
                    }

                } else {

                    if (response.errorBody() != null) {
                        val strJson = response.errorBody()!!.string()
                        if (BuildConfig.DEBUG)
                         Log.i("API_CALLS", "getSuccessResponse: errorBody=${strJson}")

                        jsonResp.errorMsg = strJson
                        jsonResp.strResponse = strJson
                        jsonResp.statusMsg = getStatusMsg(strJson)
                        jsonResp.statusCode = getStatusCode(strJson)
                        if (jsonResp.statusMsg.equals("Token Expired", ignoreCase = true) && strJson.contains("refresh_token")) {
                            jsonResp.statusMsg = ""
                        }else if (response.code() == 401 || response.code() == 404) {
                            sessionManager.clearToken()
                            sessionManager.clearAll()

                            val intent = Intent(context, SigninSignupActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            (context as Activity).finish()
                            (context as Activity).finishAffinity()
                        }
                    }
                }
                jsonResp.requestData = requestData
                jsonResp.isOnline = isOnline
                requestData = if (!isOnline) context.resources.getString(R.string.network_failure) else ""
            }
        } catch (e: Exception) {
            Log.i("API_CALLS", "getSuccessResponse: Error=${e.localizedMessage}")
            e.printStackTrace()
        }

        return jsonResp
    }

    private fun isSuccess(jsonString: String): Boolean {
        var isSuccess = false
        try {
            if (!TextUtils.isEmpty(jsonString)) {
                val statusCode = commonMethods.getJsonValue(jsonString, Constants.STATUS_CODE, String::class.java) as String
                isSuccess = !TextUtils.isEmpty(statusCode) && ("1" == statusCode || "2" == statusCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isSuccess
    }

    private fun getStatusMsg(jsonString: String): String {
        var statusMsg = ""
        try {
            if (!TextUtils.isEmpty(jsonString)) {
                statusMsg = commonMethods.getJsonValue(jsonString, Constants.STATUS_MSG, String::class.java) as String
                if ("Token Expired".equals(statusMsg, ignoreCase = true)) {
                    val token = commonMethods.getJsonValue(jsonString, Constants.REFRESH_ACCESS_TOKEN, String::class.java) as String
                    sessionManager.accessToken = token
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return statusMsg
    }

    private fun getStatusCode(jsonString: String): String {
        var statuscode = ""
        var statusMsg =""
        try {
            if (!TextUtils.isEmpty(jsonString)) {
                statuscode = commonMethods.getJsonValue(jsonString, Constants.STATUS_CODE, String::class.java) as String
                statusMsg = commonMethods.getJsonValue(jsonString, Constants.STATUS_MSG, String::class.java) as String
                if ("Token Expired".equals(statusMsg, ignoreCase = true)) {
                    val token = commonMethods.getJsonValue(jsonString, Constants.REFRESH_ACCESS_TOKEN, String::class.java) as String
                    sessionManager.accessToken = token
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return statuscode
    }
}