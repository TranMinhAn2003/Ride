package com.rideincab.user.common.views

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
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogD
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogE
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideincab.user.databinding.ActivityPaymentWebViewBinding
import com.rideincab.user.taxi.views.customize.CustomDialog
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject

class PaymentWebViewActivity : CommonActivity() {
    private lateinit var binding: ActivityPaymentWebViewBinding

    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var customDialog: CustomDialog

    private lateinit var progressDialog: ProgressDialog
    private var payFor: String? = null
    private var type: String? = null
    private var mAlertDialog: Dialog? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)

        initData()
        initHeader()
        initProgress()
        initWebView()
        loadPaymentPage()
    }

    private fun initData() {
        payFor = intent.getStringExtra(Constants.INTENT_PAY_FOR)
        type = intent.getStringExtra(Constants.PAY_FOR_TYPE)

        DebuggableLogI("PAYMENT_INIT", "payFor = $payFor, type = $type")
    }

    private fun initHeader() {
        commonMethods.setHeaderText(
            getString(R.string.payment),
            binding.commonHeader.tvHeadertext
        )
        binding.commonHeader.back.setOnClickListener { onBackPressed() }
    }

    private fun initProgress() {
        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading))
            setCancelable(false)
        }
        commonMethods.showProgressDialog(this)
    }

    private fun initWebView() {
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

        WebView.setWebContentsDebuggingEnabled(true)
        binding.paymentWv.addJavascriptInterface(MyJavaScriptInterface(this), "android")
        binding.paymentWv.webChromeClient = WebChromeClient()
        binding.paymentWv.webViewClient = createWebViewClient()
    }

    private fun loadPaymentPage() {
        val payableWalletAmount = intent.getStringExtra("payableWalletAmount")
        val url = getString(R.string.base_url) + CommonKeys.PAYMENT_WEBVIEW_KEY

        val postData = buildString {
            append("amount=").append(URLEncoder.encode(payableWalletAmount, "UTF-8"))
            append("&pay_for=").append(URLEncoder.encode(payFor, "UTF-8"))
            append("&payment_type=").append(
                URLEncoder.encode(
                    sessionManager.paymentMethod?.lowercase(),
                    "UTF-8"
                )
            )
            append("&trip_id=").append(URLEncoder.encode(sessionManager.tripId, "UTF-8"))
            append("&mode=").append(URLEncoder.encode("light", "UTF-8"))
            append("&token=").append(URLEncoder.encode(sessionManager.accessToken, "UTF-8"))
        }

        DebuggableLogI("PAYMENT_TAG", "url=$url")
        DebuggableLogI("PAYMENT_TAG", "paymentMethod=${sessionManager.paymentMethod?.lowercase()}")
        DebuggableLogI("PAYMENT_TAG", "postData=$postData")

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
            when {
                url.contains("status=success", ignoreCase = true) -> {
                    finishWithResult(true, "Payment Successful")
                    return true
                }
                url.contains("status=failed", ignoreCase = true) -> {
                    finishWithResult(false, "Payment Failed")
                    return true
                }
            }
            return false
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
            commonMethods.hideProgressDialog()
            finishWithResult(false, "Page error occurred: ${error?.description}")
        }
    }

    inner class MyJavaScriptInterface(private var ctx: Context) {
        @JavascriptInterface
        fun showHTML(html: String) {
            if (html.isNullOrBlank()) {
                DebuggableLogE("PAYMENT_TAG", "Empty HTML response")
                return
            }

            DebuggableLogI("PAYMENT_TAG", "Raw HTML/Response: $html")

            // Try parsing only if it looks like JSON
            if (html.trim().startsWith("{") && html.trim().endsWith("}")) {
                var response: JSONObject? = null

                try {
                    response = JSONObject(html)
                    DebuggableLogI("PAYMENT_TAG", "response=" + response)
                    if (response != null) {
                        val statusCode = response.getString("status_code")
                        val statusMessage = response.getString("status_message")

                        DebuggableLogI("PAYMENT_TAG", statusCode)
                        DebuggableLogI("PAYMENT_TAG", statusMessage)
                        redirect(response.toString())
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (t: Throwable) {
                    DebuggableLogE("PAYMENT_TAG", "${t.message} \"$response\"")
                }
            } else {
                DebuggableLogD("PAYMENT_TAG", "Non-JSON response, ignoring...")
            }

            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    private fun redirect(htmlResponse: String) {
        val intent = Intent()
        try {
            var walletAmount = ""
            val response = JSONObject(htmlResponse)
            if (response != null) {
                val statusCode = response.getString("status_code")
                val statusMessage = response.getString("status_message")
                intent.putExtra("status_message", statusMessage)
                if (statusCode.equals("1", true)) {
                    if (!payFor.isNullOrEmpty() && payFor.equals(CommonKeys.PAY_FOR_WALLET)) {
                        walletAmount = response.getString("wallet_amount")
                    }
                    if (walletAmount.isNotEmpty())
                        intent.putExtra("walletAmount", walletAmount)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun finishWithResult(
        isSuccess: Boolean,
        message: String,
        walletAmount: String? = null
    ) {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra("status_message", message)
                if (isSuccess && !walletAmount.isNullOrEmpty()) {
                    putExtra("walletAmount", walletAmount)
                }
            }
        )
        finish()
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
}
