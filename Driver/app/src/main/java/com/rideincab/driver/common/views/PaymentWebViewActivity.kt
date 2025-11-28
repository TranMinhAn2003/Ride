package com.rideincab.driver.common.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.TextView
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonKeys.PAY_TO_ADMIN
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.CommonMethods.Companion.DebuggableLogD
import com.rideincab.driver.common.util.CommonMethods.Companion.DebuggableLogI
import com.rideincab.driver.databinding.ActivityPaymentWebViewBinding
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject

class PaymentWebViewActivity : CommonActivity() {

    private lateinit var binding:ActivityPaymentWebViewBinding
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var progressDialog: ProgressDialog

    private var mAlertDialog: Dialog? = null

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.payment), binding.commonHeader.headertext)
        val payableAdminAmount = intent.getStringExtra("payableAmount")
        val isReferralApplied = intent.getStringExtra("isReferralAmount")
        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }
        setProgress()

        if (!progressDialog.isShowing) {
            //progressDialog.show()
        }
        commonMethods.showProgressDialog(this)

        // ✅ Configure WebView
        with(binding.paymentWv.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        binding.paymentWv.addJavascriptInterface(MyJavaScriptInterface(this), "android")
        binding.paymentWv.webViewClient = createWebViewClient()

        val url = getString(R.string.apiBaseUrl) + CommonKeys.WEB_PAY_TO_ADMIN
        val postData =
            "amount=" + URLEncoder.encode(payableAdminAmount, "UTF-8") +
                    "&pay_for=" + URLEncoder.encode(PAY_TO_ADMIN, "UTF-8") +
                    "&applied_referral_amount=" + URLEncoder.encode(isReferralApplied, "UTF-8") +
                    "&token=" + URLEncoder.encode(sessionManager.accessToken, "UTF-8")

        binding.paymentWv.postUrl(url, postData.toByteArray())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebViewClient() = object : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            commonMethods.showProgressDialog(this@PaymentWebViewActivity)
            DebuggableLogI("PAYMENT_URL", "onPageStarted: $url")
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            DebuggableLogI("PAYMENT_TAG", "Loading URL: $url")

            // ✅ Paystack often redirects with status in query param
            return when {
                url.contains("status=success", ignoreCase = true) -> {
                    redirect("{\"status_code\":\"1\",\"status_message\":\"Payment Successful\"}")
                    true
                }
                url.contains("status=failed", ignoreCase = true) -> {
                    redirect("{\"status_code\":\"0\",\"status_message\":\"Payment Failed\"}")
                    true
                }
                else -> false
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            commonMethods.hideProgressDialog()
            DebuggableLogI("PAYMENT_URL", "onPageFinished: $url")

            // 1️⃣ First try: capture the whole page body text
            binding.paymentWv.evaluateJavascript(
                """
            (function() {
                try {
                    if (window.android) {
                        window.android.showHTML(document.body.innerText);
                    }
                } catch(e) {
                    console.log("evaluateJavascript error: " + e.message);
                }
            })();
            """.trimIndent()
            ) { result ->
                DebuggableLogI("PAYMENT_TAG", "evaluateJavascript Result: $result")
            }

            // 2️⃣ Fallback: look for specific element (#data)
            binding.paymentWv.loadUrl(
                "javascript:window.android && window.android.showHTML(document.getElementById('data') ? document.getElementById('data').innerHTML : '');"
            )
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            commonMethods.hideProgressDialog()
            redirect("{\"status_code\":\"0\",\"status_message\":\"${error?.description}\"}")
        }
    }

    private fun setProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.loading))
        progressDialog.setCancelable(false)
    }

    override fun onBackPressed() {
        alertDialog(this, getString(R.string.cancel_payment))
    }

    fun alertDialog(mContext: Context, alert: Any) {
        try {
            if (mAlertDialog == null)
                mAlertDialog = Dialog(mContext)

            mAlertDialog!!.setContentView(R.layout.alert_layout_dialog)
            mAlertDialog!!.setCancelable(true)
            mAlertDialog!!.window?.setBackgroundDrawable(ColorDrawable(0))

            mAlertDialog!!.findViewById<TextView>(R.id.tvMsg).text = Gson().toJson(alert)
            val btnOkay = mAlertDialog!!.findViewById<TextView>(R.id.btnOkay)
            val btnNo = mAlertDialog!!.findViewById<TextView>(R.id.btnNo)

            btnNo.visibility = View.VISIBLE
            btnOkay.setOnClickListener {
                if (mAlertDialog!!.isShowing) mAlertDialog!!.dismiss()
                finish()
            }
            btnNo.setOnClickListener {
                if (mAlertDialog!!.isShowing) mAlertDialog!!.dismiss()
            }
            mAlertDialog!!.setCancelable(false)
            mAlertDialog!!.setCanceledOnTouchOutside(false)

            if (!mAlertDialog!!.isShowing)
                mAlertDialog!!.show()

        } catch (e: java.lang.Exception) {
            DebuggableLogD("PAYMENT_TAG", "alertDialog: Error=${e.localizedMessage}")
        }
    }

    inner class MyJavaScriptInterface(private var ctx: Context) {

        @JavascriptInterface
        fun showHTML(html: String) {
            println("HTML$html")
            if (html.trim().startsWith("{") && html.trim().endsWith("}")) {
                var response: JSONObject? = null
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                commonMethods.hideProgressDialog()
                try {
                    response = JSONObject(html)
                    redirect(response.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (t: Throwable) {
                    Log.e("My App", "Could not parse malformed JSON: \"$response\"")
                }
            }else {
                DebuggableLogD("PAYMENT_TAG", "Non-JSON response, ignoring...")
            }
        }
    }

    private fun redirect(htmlResponse: String) {
        val intent = Intent()
        intent.putExtra("response", htmlResponse)
        setResult(RESULT_OK, intent)
        finish()
    }
}