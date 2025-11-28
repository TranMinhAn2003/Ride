package com.rideincab.user.taxi.views.signinsignup

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.taxi.datamodels.signinsignup.SigninResult
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivitySsloginBinding
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.facebookAccountKit.FacebookAccountKitActivity
import com.rideincab.user.taxi.views.main.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

@Suppress("DEPRECATION")
class SSLoginActivity : CommonActivity(), ServiceListener {

    private lateinit var binding:AppActivitySsloginBinding
    lateinit var dialog: AlertDialog
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var customDialog: CustomDialog

    /*@BindView(R.id.binding.inputLayoutMobile)
    lateinit var binding.inputLayoutMobile: TextInputLayout
    @BindView(R.id.input_layout_passsword)
    lateinit var input_layout_passsword: TextInputLayout
    @BindView(R.id.binding.phone)
    lateinit var binding.phone: EditText
    @BindView(R.id.binding.ccp)
    lateinit var binding.ccp: CountryCodePicker
    @BindView(R.id.binding.passwordEdit)
    lateinit var binding.passwordEdit: EditText
    @BindView(R.id.binding.siginButton)
    lateinit var binding.siginButton: Button
    @BindView(R.id.forgot_password)
    lateinit var forgot_password: RelativeLayout*/

    protected var isInternetAvailable: Boolean = false
    lateinit var signinResult: SigninResult
    
    fun signIn() {

        isInternetAvailable = commonMethods.isOnline(this)


        isInternetAvailable = commonMethods.isOnline(this)
        if (isInternetAvailable) {


            if (!validateMobile("check")) {
                binding.inputLayoutMobile.error = getString(R.string.InvalidMobileNumber)
            }
            initLoginApi()
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivitySsloginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply insets to root view (adds top and bottom padding)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.signin),binding.commonHeader.tvHeadertext)

        dialog = commonMethods.getAlertDialog(this)
        binding.ccp.setAutoDetectedCountry(true)
        if (Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }
        binding.siginButton.isEnabled = false
        binding.siginButton.setBackgroundDrawable(ContextCompat.getDrawable(baseContext,R.drawable.app_curve_button_yellow_disable))
        binding.passwordEdit.addTextChangedListener(NameTextWatcher(binding.passwordEdit))
        binding.phone.addTextChangedListener(NameTextWatcher(binding.phone))

        sessionManager.countryNameCode=binding.ccp.selectedCountryNameCode
        
        binding.commonHeader.back.setOnClickListener { super.onBackPressed() }
        binding.siginButton.setOnClickListener { signIn() }
        binding.forgotPassword.setOnClickListener { FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 1) }
        
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT) {
            if (resultCode == Activity.RESULT_OK) {
                openPasswordResetActivity(data!!.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY)!!, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY)!!, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY)!!)
            }
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {
            onSuccessLogin(jsonResp)
        } else {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (validatePassword("validate") && validateMobile("validate")) {
                binding.siginButton.isEnabled = true
                binding.siginButton.setBackgroundDrawable(ContextCompat.getDrawable(baseContext,R.drawable.app_curve_button_yellow))
                //binding.siginButton.setBackgroundColor(ContextCompat.getColor(baseContext,R.color.cabme_app_yellow))
            } else {
                binding.siginButton.isEnabled = false
                binding.siginButton.setBackgroundDrawable(ContextCompat.getDrawable(baseContext,R.drawable.app_curve_button_yellow_disable))
                //binding.siginButton.setBackgroundColor(ContextCompat.getColor(baseContext,R.color.button_disable_color))
            }
          /*  if (binding.phone.text.toString().startsWith("0")) {
                binding.phone.setText("")
            }*/
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.password_edit -> validatePassword("check")
                R.id.phone -> validateMobile("check")
            }
        }
    }

    private fun validateMobile(type: String): Boolean {
        if (binding.phone.text.toString().trim { it <= ' ' }.isEmpty() || binding.phone.text?.length ?: 0 < 6) {
            if ("check" == type) {
                requestFocus(binding.phone)
            }
            return false
        } else {
            binding.inputLayoutMobile.isErrorEnabled = false
        }
        return true
    }

    /*
     *   validate password
     */
    private fun validatePassword(type: String): Boolean {
        if (binding.passwordEdit.text.toString().trim { it <= ' ' }.isEmpty() || binding.passwordEdit.text?.length ?: 0 < 6) {
            if ("check" == type) {
                requestFocus(binding.passwordEdit)
            }
            return false
        } else {
            binding.inputLayoutPasssword.isErrorEnabled = false
        }
        return true
    }

    /*
     *   focus edit text
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    private fun openPasswordResetActivity(phoneNumber: String, countryCode: String,countryNameCode:String) {
        val signin = Intent(this, SSResetPassword::class.java)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, phoneNumber)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, countryCode)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY, countryNameCode)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        finish()
    }


    fun initLoginApi() {
        commonMethods.showProgressDialog(this)
        apiService.login(binding.phone.text.toString().trim { it <= ' ' },
                binding.ccp.selectedCountryNameCode.replace("\\+".toRegex(), ""),
                sessionManager.type!!, binding.passwordEdit.text.toString().trim { it <= ' ' }, sessionManager.deviceType!!, sessionManager.deviceId!!, sessionManager.languageCode!!).enqueue(RequestCallback(this))
    }

    fun onSuccessLogin(jsonResp: JsonResponse) {
        signinResult = gson.fromJson(jsonResp.strResponse, SigninResult::class.java)
        sessionManager.isrequest = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sessionManager.currencySymbol = Html.fromHtml(signinResult.currencySymbol,Html.FROM_HTML_MODE_LEGACY).toString()
        }
        else {
            sessionManager.currencySymbol = Html.fromHtml(signinResult.currencySymbol).toString()
        }
        sessionManager.currencyCode = signinResult.currencyCode
        sessionManager.accessToken=signinResult.token
        sessionManager.walletAmount = signinResult.walletAmount
        sessionManager.userId = signinResult.userId
        sessionManager.countryNameCode=binding.ccp.selectedCountryNameCode
        commonMethods.hideProgressDialog()

        try {
            val response = JSONObject(jsonResp.strResponse)
            if (response.has("promo_details")) {
                val promocount = response.getJSONArray("promo_details").length()
                sessionManager.promoDetail = response.getString("promo_details")
                sessionManager.promoCount = promocount
            }
        } catch (j: JSONException) {
            j.printStackTrace()
        }

        disclaimerDialog()
    }

    private fun disclaimerDialog() {
        try {
            val dialog = Dialog(this, R.style.DialogCustomTheme)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.disclaimer_dialog)

            val tvDisclaimer = dialog.findViewById<TextView>(R.id.tvDisclaimer)
            val tvAccept = dialog.findViewById<TextView>(R.id.tvAccept)

            tvDisclaimer.setText(getString(R.string.location_disclaimer))

            tvAccept.setOnClickListener {
                sessionManager.isDialogShown = "yes"
                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
                finish()
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        } catch (e: Exception) {
            Log.i("TAG", "disclaimerDialog: Error=${e.localizedMessage}")
        }

    }
}