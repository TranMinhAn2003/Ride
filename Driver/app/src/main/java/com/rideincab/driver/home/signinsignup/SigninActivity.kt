package com.rideincab.driver.home.signinsignup

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage signinsignup model
 * @category SigninActivity
 * @author SMR IT Solutions
 *
 */

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
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
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.common.views.applySystemBarsTopPadding
import com.rideincab.driver.databinding.AppActivitySigninBinding
import com.rideincab.driver.home.MainActivity
import com.rideincab.driver.home.datamodel.LoginDetails
import com.rideincab.driver.home.facebookAccountKit.FacebookAccountKitActivity
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

/* ************************************************************
                SigninActivity
Its used to  get the signin detail function
*************************************************************** */
class SigninActivity : CommonActivity(), ServiceListener {

    lateinit var binding:AppActivitySigninBinding

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
    internal lateinit var context: Context
    
    /*@BindView(R.id.input_layout_username)
    lateinit var input_layout_username: TextInputLayout*/
   /* @BindView(R.id.input_layout_mobile)
    lateinit var input_layout_mobile: TextInputLayout*/
    /*@BindView(R.id.binding.inputLayoutPasssword)
     lateinit var binding.inputLayoutPasssword: TextInputLayout*/
    /*@BindView(R.id.user_edit)
     lateinit var user_edit: EditText*/
    /* @BindView(R.id.sigin_button)
  lateinit var sigin_button: Button*/
    /*  @BindView(R.id.dochome_back)
   lateinit var dochome_back: ImageView*/
    
/*    @BindView(R.id.binding.phone)
     lateinit var binding.phone: EditText
    @BindView(R.id.binding.ccp)
     lateinit var binding.ccp: CountryCodePicker
    @BindView(R.id.binding.passwordEdit)
    lateinit var binding.passwordEdit: EditText
    @BindView(R.id.forgot_password)
   lateinit  var forgot_password: RelativeLayout*/

    protected var isInternetAvailable: Boolean = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT) {
            /*if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER) {
                commonMethods.showMessage(this, dialog, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY));
            } else if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER) {
                openPasswordResetActivity(data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY));
            }*/

            if (resultCode == Activity.RESULT_OK) {
                openPasswordResetActivity(
                    data!!.getStringExtra(
                        FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
                    ).toString(), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString(),
                        data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY).toString()
                )
            }
        }


    }

    private fun openPasswordResetActivity(phoneNumber: String, countryCode: String,countryNamecode:String) {

        val signin = Intent(applicationContext, ResetPassword::class.java)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, phoneNumber)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, countryCode)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY, countryNamecode)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        finish()

    }


    fun signIn() {
        var input_password_str = binding.passwordEdit.text.toString()

        isInternetAvailable = commonMethods.isOnline(this)
        val phonenumber_str = binding.phone.text.toString()

        try {
            input_password_str = URLEncoder.encode(input_password_str, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        sessionManager.password = input_password_str
        sessionManager.phoneNumber = phonenumber_str
        sessionManager.countryCode =
                binding.ccp.selectedCountryNameCode.replace("\\+".toRegex(), "")

        isInternetAvailable = commonMethods.isOnline(this)
        if (isInternetAvailable) {


            if (!validateMobile("check")) {
               // input_layout_mobile.error = getString(R.string.error_msg_mobilenumber)
            }
            getUserProfile()
            // new SigninSignup().execute(url);
        } else {
            commonMethods.showMessage(
                applicationContext,
                dialog,
                resources.getString(R.string.no_connection)
            )

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsTopPadding()
        AppController.getAppComponent().inject(this)
        //commonMethods.imageChangeforLocality(this,dochome_back)

        /**Commmon Header Text View and */
        commonMethods.setheaderText(resources.getString(R.string.signin),binding.commonHeader.headertext)
        commonMethods.setButtonText(resources.getString(R.string.signin),binding.commonButton.button)

        dialog = commonMethods.getAlertDialog(this)
        binding.ccp.setAutoDetectedCountry(true)
        if (Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }

        context = this

        binding.commonButton.button.isEnabled = false
        //user_edit.addTextChangedListener(NameTextWatcher(user_edit))
        binding.passwordEdit.addTextChangedListener(NameTextWatcher(binding.passwordEdit))
        binding.phone.addTextChangedListener(NameTextWatcher(binding.phone))

        binding.commonButton.button.isEnabled = false
        binding.commonButton.button.background = resources.getDrawable(R.drawable.app_curve_button_yellow_disable)

        binding.commonHeader.arrow.setOnClickListener {
            onBackPressed()
        }
        binding.forgotPassword.setOnClickListener {
            FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 1)
        }

        binding.commonButton.button.setOnClickListener {
            signIn()
        }

    }


    fun getUserProfile() {
        commonMethods.showProgressDialog(this)

        val input_password_str = binding.passwordEdit.text.toString().trim { it <= ' ' }
        sessionManager.password = input_password_str


        apiService.login(
            sessionManager.phoneNumber!!,
            sessionManager.type!!,
            sessionManager.countryCode!!,
            input_password_str,
            sessionManager.deviceId!!,
            sessionManager.deviceType!!,
            sessionManager.languageCode!!
        ).enqueue(RequestCallback(this))

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val signin = Intent(applicationContext, SigninSignupHomeActivity::class.java)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            try {
                onSuccessLogin(jsonResp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    @Throws(JSONException::class)
    private fun onSuccessLogin(jsonResp: JsonResponse) {

        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, LoginDetails::class.java)
        if (signInUpResultModel != null) {

            val driverStatus = signInUpResultModel.userStatus
            sessionManager.userId = signInUpResultModel.userID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sessionManager.currencySymbol =
                    Html.fromHtml(signInUpResultModel.currencySymbol,Html.FROM_HTML_MODE_LEGACY).toString()
            }else{
                Html.fromHtml(signInUpResultModel.currencySymbol).toString()
            }
            sessionManager.currencyCode = signInUpResultModel.currencyCode
            sessionManager.countryCode = signInUpResultModel.country_code
            sessionManager.paypalEmail = signInUpResultModel.payoutId
            sessionManager.driverSignupStatus = signInUpResultModel.userStatus
            sessionManager.setAcesssToken(signInUpResultModel.token)
            sessionManager.isRegister = true
            sessionManager.countryCode = signInUpResultModel.country_code
            sessionManager.phoneNumber = signInUpResultModel.mobileNumber
            sessionManager.userType = signInUpResultModel.companyId



            disclaimerDialog(driverStatus,signInUpResultModel)

        }

    }

    private fun disclaimerDialog(driverStatus: String, signInUpResultModel: LoginDetails) {
        try {
            val dialog = Dialog(this, R.style.DialogCustomTheme)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.disclaimer_dialog)

            val tvDisclaimer = dialog.findViewById<TextView>(R.id.tvDisclaimer)
            val tvAccept = dialog.findViewById<TextView>(R.id.tvAccept)

            tvDisclaimer.setText(getString(R.string.location_disclaimer))

            tvAccept.setOnClickListener {
                sessionManager.isDialogShown = "yes"
                driverStatus(driverStatus,signInUpResultModel);
                dialog.dismiss()
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        } catch (e: Exception) {
            Log.i("TAG", "disclaimerDialog: Error=${e.localizedMessage}")
        }

    }

    private fun driverStatus(driverStatus: String, signInUpResultModel: LoginDetails) {
        if (driverStatus == "Car_details") {
            val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)
            sessionManager.carType = carDeailsModel
            startMainActivity()
            finishAffinity()
        } else if (driverStatus == "Document_details") {
            // If motorista status is document_details then redirect to document upload page

            startMainActivity()
            finishAffinity()
        } else if (driverStatus.equals("Pending", ignoreCase = true)) {

            sessionManager.vehicle_id = signInUpResultModel.vehicleId

            startMainActivity()
        } else if (driverStatus == "Active") {
            // If motorista status is active check paypal email is exists then redirect to home page otherwise redirect to paypal email address page
            sessionManager.vehicle_id = signInUpResultModel.vehicleId
            startMainActivity()
        } else {
            // Redirect to sign in signup home page
            val x = Intent(applicationContext, SigninSignupHomeActivity::class.java)
            val bndlanimation = ActivityOptions.makeCustomAnimation(
                applicationContext,
                R.anim.cb_fade_in,
                R.anim.cb_face_out
            ).toBundle()
            startActivity(x, bndlanimation)
            finishAffinity()
        }
    }

    private fun startMainActivity() {
        val x = Intent(applicationContext, MainActivity::class.java)
        x.putExtra("signinup", true)
        val bndlanimation = ActivityOptions.makeCustomAnimation(
            applicationContext,
            R.anim.cb_fade_in,
            R.anim.cb_face_out
        ).toBundle()
        startActivity(x, bndlanimation)
        finishAffinity()
    }

    /*
     *   Validate binding.phone number
     */
    private fun validateMobile(type: String): Boolean {
        if (binding.phone.text.toString().trim { it <= ' ' }.isEmpty() || binding.phone.text.length < 6) {
            if ("check" == type) {
                //input_layout_mobile.setError(getString(R.string.error_msg_mobilenumber));
                requestFocus(binding.phone)
            }
            return false
        } else {
           // input_layout_mobile.isErrorEnabled = false
        }


        return true
    }

    /*
     *   validate password
     */
    private fun validateLast(type: String): Boolean {
        if (binding.passwordEdit.text.toString().trim { it <= ' ' }.isEmpty() || binding.passwordEdit.text.length < 6) {
            if ("check" == type) {
                //binding.inputLayoutPasssword.setError(getString(R.string.error_msg_password));
                requestFocus(binding.passwordEdit)
            }
            return false
        } else {
            binding.inputLayoutPasssword.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate user name
     */
    /*private fun validateFirst(): Boolean {
        if (user_edit.text.toString().trim { it <= ' ' }.isEmpty()) {
            input_layout_username.error = getString(R.string.error_msg_firstname)
            requestFocus(user_edit)
            return false
        } else {
            input_layout_username.isErrorEnabled = false
        }

        return true
    }*/

    /*
     *   focus edit text
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }


    /*
     *   Text watcher for validate signin fields
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (validateLast("validate") && validateMobile("validate")) {
                binding.commonButton.button.isEnabled = true
                binding.commonButton.button.background = resources.getDrawable(R.drawable.app_curve_button_yellow)
            } else {

                binding.commonButton.button.isEnabled = false
                binding.commonButton.button.background = resources.getDrawable(R.drawable.app_curve_button_yellow_disable)
            }
           /* if (binding.phone.text.toString().startsWith("0")) {
                binding.phone.setText("")
            }*/
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                //R.id.user_edit -> validateFirst()
                R.id.password_edit -> validateLast("check")
                R.id.phone -> validateMobile("check")
            }
        }
    }


}
