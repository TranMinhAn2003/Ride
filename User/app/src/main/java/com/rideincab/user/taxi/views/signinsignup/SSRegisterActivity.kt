package com.rideincab.user.taxi.views.signinsignup

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage signin_signup
 * @category SSRegisterActivity
 * @author SMR IT Solutions
 *
 */

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants.Female
import com.rideincab.user.common.helper.Constants.Male
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.common.utils.Enums.REQ_SIGNUP
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivitySssocialDetailsBinding
import com.rideincab.user.taxi.datamodels.signinsignup.SigninResult
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import java.io.UnsupportedEncodingException
import java.util.*
import javax.inject.Inject

//import static com.cloneappsolutions.cabmeuser.common.utils.Enums.REQ_VALIDATE_NUMBER;

/* ************************************************************
   Sign in and sign up using social media (FaceBook and Google plus)
   ************************************************************ */
@Suppress("DEPRECATION")
class SSRegisterActivity : CommonActivity(), ServiceListener {

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

/*    @BindView(R.id.binding.inputFirst)
    lateinit var binding.inputFirst: EditText

    @BindView(R.id.binding.inputLast)
    lateinit var binding.inputLast: EditText

    @BindView(R.id.binding.inputEmail)
    lateinit var binding.inputEmail: EditText

    @BindView(R.id.binding.inputPassword)
    lateinit var binding.inputPassword: EditText

    @BindView(R.id.binding.inputPhone)
    lateinit var binding.inputPhone: EditText*/

    /* @BindView(R.id.input_layout_first)
     lateinit var input_layout_first: TextInputLayout*/

    /*  @BindView(R.id.input_layout_last)
      lateinit var input_layout_last: TextInputLayout*/

    /*  @BindView(R.id.input_layout_email)
      lateinit var input_layout_email: TextInputLayout*/

    /*    @BindView(R.id.backArrow)
    lateinit var backArrow: ImageView*/

    /* @BindView(R.id.signinterms)
     lateinit var signinterms: TextView*/

    /*@BindView(R.id.binding.inputLayoutPhone)
    lateinit var binding.inputLayoutPhone: TextInputLayout

    @BindView(R.id.binding.inputLayoutPassword)
    lateinit var binding.inputLayoutPassword: TextInputLayout

    @BindView(R.id.binding.ccp)
    lateinit var binding.ccp: CountryCodePicker

    @BindView(R.id.input_referral)
    lateinit var edt_referral: EditText

    @BindView(R.id.binding.inputLayoutReferral)
    lateinit var binding.inputLayoutReferral: TextInputLayout

    @BindView(R.id.socialdetailnext)
    lateinit var binding.binding.socialdetailnext: Button

    @BindView(R.id.rg_gender)
    lateinit var binding.vGender.rgGender: RadioGroup*/


    private var selectedGender= ""

    lateinit var firstname: String
    lateinit var lastname: String
    lateinit var email: String
    lateinit var fbid: String
    lateinit var gpid: String
    lateinit var profile: String
    lateinit var Termpolicy: String

    protected var isInternetAvailable: Boolean = false
    lateinit var signinResult: SigninResult
    private var facebookKitVerifiedMobileNumber = ""
    private var facebookVerifiedMobileNumberCountryCode = ""
    private var facebookVerifiedMobileNumberCountryNameCode = ""

    /**
     * Check is email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }
    

    private var isSocialLogin: Boolean = false

    private lateinit var binding: AppActivitySssocialDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivitySssocialDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply insets to root view (adds top and bottom padding)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.register), binding.commonHeader.tvHeadertext)
        binding.inputLayoutPassword.visibility = View.VISIBLE
        getMobileNumerAndCountryCodeFromIntent()
        dialog = commonMethods.getAlertDialog(this)
        setLocale()
        showOrHideReferralAccordingToSessionData()
        //commonMethods.imageChangeforLocality(this, backArrow)
        firstname = sessionManager.firstName.toString()
        lastname = sessionManager.lastName.toString()
        email = sessionManager.email.toString()
        profile = sessionManager.profilepicture.toString()
        fbid = sessionManager.facebookId.toString()
        gpid = sessionManager.googleId.toString()

        binding.inputLayoutPhone.hint = resources.getString(R.string.mobile_s)
        binding.inputLayoutPassword.hint = resources.getString(R.string.password)
        binding.inputEmail.hint = resources.getString(R.string.email_s)
        binding.inputLast.hint = resources.getString(R.string.last)
        binding.inputFirst.hint = resources.getString(R.string.first)

        binding.inputFirst.setText(firstname)
        binding.inputLast.setText(lastname)
        binding.inputEmail.setText(email)
        if (sessionManager.appleId!!.isNotEmpty()) {

            if (binding.inputEmail.text?.isNotEmpty() == true) {
                binding.inputEmail.isEnabled = false
                binding.inputEmail.isFocusableInTouchMode =
                    false; // user touches widget on phone with touch screen
                binding.inputEmail.isClickable = false;
            }

        }
        /*val rb = findViewById<View>(R.id.rd_male) as RadioButton
        val rb2 = findViewById<View>(R.id.rd_female) as RadioButton
        val font: Typeface = Typeface.createFromAsset(assets, resources.getString(R.string.fonts_UBERMedium))
        val font1: Typeface = Typeface.createFromAsset(assets, resources.getString(R.string.fonts_UBERMedium))
        rb.setTypeface(font)
        rb2.setTypeface(font1)*/
        binding.vGender.rgGender.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            val radioButton = radioGroup.findViewById(i) as RadioButton
            if (radioButton.id == R.id.rd_male) {
                selectedGender = Male
            } else if (radioButton.id == R.id.rd_female) {
                selectedGender = Female
            }
            listeners()
        })
        Termpolicy = resources.getString(R.string.terms)
        /**
         * Custom text view for sign up link
         */
        //customTextView(signinterms)

        val laydir = getString(R.string.layout_direction)
        if ("1" == laydir) {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
            binding.ccp.setCurrentTextGravity(CountryCodePicker.TextGravity.RIGHT)
            binding.ccp.gravity = Gravity.START
        }

        binding.inputFirst.addTextChangedListener(NameTextWatcher(binding.inputFirst))
        binding.inputLast.addTextChangedListener(NameTextWatcher(binding.inputLast))
        binding.inputEmail.addTextChangedListener(NameTextWatcher(binding.inputEmail))
        //binding.inputPhone.addTextChangedListener(new NameTextWatcher(binding.inputPhone));
        binding.inputPassword.addTextChangedListener(NameTextWatcher(binding.inputPassword))

        if (commonMethods.getAuthType().equals("google")
            || commonMethods.getAuthType().equals("facebook")
            || commonMethods.getAuthType().equals("apple")
        ) {
            binding.inputLayoutPassword.visibility = View.GONE
            isSocialLogin = true
        }
        /*if(isSocialLogin){
            binding.binding.socialdetailnext.isClickable = true
            binding.binding.socialdetailnext.setBackground(resources.getDrawable(R.drawable.app_curve_button_yellow))
        }else{
            binding.binding.socialdetailnext.isClickable = false
            binding.binding.socialdetailnext.setBackground(resources.getDrawable(R.drawable.app_curve_button_yellow_disable))
        }*/
        binding.socialdetailnext.isClickable = false
        binding.socialdetailnext.background =
            resources.getDrawable(R.drawable.app_curve_button_yellow_disable)
        
        binding.commonHeader.back.setOnClickListener { super.onBackPressed() }
        binding.nextLogin.setOnClickListener { gotoLogin() }
        binding.socialdetailnext.setOnClickListener { submitForm() }
    }

    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            binding.inputLayoutReferral.visibility = View.VISIBLE
        } else {
            binding.inputLayoutReferral.visibility = View.GONE
        }
    }

    /**
     * Validating form
     */

    fun setLocale() {
        val lang = sessionManager.language

        if (lang != "") {

            val langC = sessionManager.languageCode
            val locale = Locale(langC)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            this.resources.updateConfiguration(config, this.resources.displayMetrics)
            //recreate();
            DebuggableLogV("locale", "localesetted $locale")
        } else {
            sessionManager.language = "English"
            sessionManager.languageCode = "en"
            setLocale()
            recreate()
            //Intent intent = new Intent(SigninSignupActivity.this, SigninSignupActivity.class);
            val intent = baseContext.packageManager
                .getLaunchIntentForPackage(baseContext.packageName)
            intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }


    }

    private fun submitForm() {

        if (!validateFirst()) {
            return
        }
        if (!validateLast()) {
            return
        }
        if (!validateEmail()) {
            binding.inputEmail.error = getString(R.string.error_msg_email)
            return
        }
        if (selectedGender.isNullOrEmpty()) {
            Toast.makeText(this, resources.getString(R.string.error_gender), Toast.LENGTH_SHORT)
                .show();
            return
        }
        if (!validatePassword() && !isSocialLogin) {
            binding.inputLayoutPassword.error = getString(R.string.error_msg_password)
            return
        }

        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
        } else {
            registerWithType()
        }
    }

    /**
     * validate first name
     */
    private fun validateFirst(): Boolean {
        if (binding.inputFirst.text.toString().trim { it <= ' ' }.isEmpty()) {
            //   binding.inputFirst.error = getString(R.string.error_msg_firstname)
            //    requestFocus(binding.inputFirst)
            return false
        } else {
            // binding.inputFirst.isErrorEnabled = false
        }
        return true
    }

    private fun getMobileNumerAndCountryCodeFromIntent() {
        if (intent != null) {
            facebookKitVerifiedMobileNumber =
                intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY)!!
            facebookVerifiedMobileNumberCountryCode =
                intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY)!!
            facebookVerifiedMobileNumberCountryNameCode =
                intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY)!!

            binding.inputPhone.setText(facebookKitVerifiedMobileNumber)
            binding.inputPhone.isEnabled = false

            binding.ccp.setCountryForNameCode(facebookVerifiedMobileNumberCountryNameCode)
            binding.ccp.setCcpClickable(false)

            sessionManager.countryNameCode=binding.ccp.selectedCountryNameCode
        }
    }

    /**
     * validate last name
     */

    private fun validateLast(): Boolean {
        if (binding.inputLast.text.toString().trim { it <= ' ' }.isEmpty()) {
            //  binding.inputLast.error = getString(R.string.error_msg_lastname)
            //  requestFocus(binding.inputLast)
            return false
        } else {
            //  input_layout_last.isErrorEnabled = false
        }
        return true
    }

    /**
     * validate email
     */
    private fun validateEmail(): Boolean {
        val email = binding.inputEmail.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            //  input_layout_email.setError(getString(R.string.error_msg_email));
            //  requestFocus(binding.inputEmail)
            return false
        } else {
            //  input_layout_email.isErrorEnabled = false
        }
        return true
    }

    /**
     * validate phone number
     */
    private fun validatePhone(): Boolean {
        if (binding.inputPhone.text.toString().trim { it <= ' ' }
                .isEmpty() || binding.inputPhone.text.toString().length < 6) {
            // binding.inputLayoutPhone.setError(getString(R.string.error_msg_phone));
            //requestFocus(binding.inputPhone);
            return false
        } else {
            binding.inputLayoutPhone.isErrorEnabled = false
        }
        return true
    }

    /**
     * validate password
     */
    private fun validatePassword(): Boolean {
        if (binding.inputPassword.text.toString().trim { it <= ' ' }
                .isEmpty() || binding.inputPassword.text.toString().length < 6) {
            //binding.inputLayoutPassword.setError(getString(R.string.error_msg_password));
            //    requestFocus(binding.inputPassword)
            return false
        } else {
            binding.inputLayoutPassword.isErrorEnabled = false
        }
        return true
    }

    /**
     * Change request focus
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        when (jsonResp.requestCode) {
            REQ_SIGNUP -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    clearSocialCredintials()
                    onSuccessRegister(jsonResp)
                } else {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    //backArrow.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg))
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
    }

    /**
     * Custom text view to link
     */
    private fun customTextView(view: TextView) {
        val spanTxt = SpannableStringBuilder(
            resources.getString(R.string.sigin_terms1)
        )
        spanTxt.append(resources.getString(R.string.sigin_terms2))
        spanTxt.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val url = Termpolicy
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)

                }
            },
            spanTxt.length - resources.getString(R.string.sigin_terms2).length,
            spanTxt.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanTxt.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_text_color)),
            spanTxt.length - resources.getString(R.string.sigin_terms2).length,
            spanTxt.length,
            0
        )
        spanTxt.append(resources.getString(R.string.sigin_terms3))
        spanTxt.append(resources.getString(R.string.sigin_terms4))
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = resources.getString(R.string.privacy_policy)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        }, spanTxt.length - resources.getString(R.string.sigin_terms4).length, spanTxt.length, 0)
        spanTxt.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_text_color)),
            spanTxt.length - resources.getString(R.string.sigin_terms4).length,
            spanTxt.length,
            0
        )
        spanTxt.append(".")
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(spanTxt, TextView.BufferType.SPANNABLE)
    }

    /**
     * Back button pressed
     */
    override fun onBackPressed() {
        super.onBackPressed()
        //  super.onBackPressed();
        val dialog = BottomSheetDialog(this@SSRegisterActivity, R.style.BottomSheetDialogTheme)
        dialog.setContentView(R.layout.app_dialogsignup_cancel)
        val confirm = dialog.findViewById<View>(R.id.signup_cancel_confirm) as Button
        val cancel = dialog.findViewById<View>(R.id.signup_cancel) as Button

        cancel.setOnClickListener { dialog.dismiss() }

        confirm.setOnClickListener {
            dialog.dismiss()
            clearSocialCredintials()
            val intent = Intent(this@SSRegisterActivity, SigninSignupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
            finish()
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun gotoLogin() {
        val intent = Intent(this@SSRegisterActivity, SSLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        finish()
    }

    /**
     * Text watcher for validate
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            DebuggableLogI("Cabme", "Textchange")
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            DebuggableLogI("Cabme", "Textchange")
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.input_first -> validateFirst()
                R.id.input_last -> validateLast()
                R.id.input_email -> validateEmail()
                R.id.input_password -> validatePassword()
                else -> {
                }
            }
            listeners()
        }
    }

    private fun listeners() {
        if (isSocialLogin) {
            if (validateFirst() && validateLast() && validateEmail() && !selectedGender.isNullOrEmpty()) {
                binding.socialdetailnext.isClickable = true
                binding.socialdetailnext.background =
                    resources.getDrawable(R.drawable.app_curve_button_yellow)
            } else {
                binding.socialdetailnext.isClickable = false
                binding.socialdetailnext.setBackground(resources.getDrawable(R.drawable.app_curve_button_yellow_disable))
            }
        } else {
            if (validateFirst() && validateLast() && validateEmail() && validatePassword() && !selectedGender.isNullOrEmpty()) {
                binding.socialdetailnext.isClickable = true
                binding.socialdetailnext.setBackground(resources.getDrawable(R.drawable.app_curve_button_yellow))
            } else {
                binding.socialdetailnext.isClickable = false
                binding.socialdetailnext.setBackground(resources.getDrawable(R.drawable.app_curve_button_yellow_disable))
            }
        }
    }

    private fun registerWithType() {
        DebuggableLogI("google id", sessionManager.googleId + "test")
        DebuggableLogI("fb id", sessionManager.facebookId + "test")
        if (sessionManager.googleId != "" || sessionManager.facebookId != "" || sessionManager.appleId != "") {
            DebuggableLogI("register type", "Social")
            socialSignup()
        } else {
            DebuggableLogI("register type", "normal")
            normalSignup()
        }
    }

    private fun normalSignup() {
        commonMethods.showProgressDialog(this)
        val locationHashMap: HashMap<String, String>
        locationHashMap = HashMap()
        locationHashMap["new_user"] = "0"
        locationHashMap["user_type"] = sessionManager.type.toString()
        locationHashMap["mobile_number"] = facebookKitVerifiedMobileNumber
        locationHashMap["country_code"] = facebookVerifiedMobileNumberCountryNameCode
        locationHashMap["first_name"] = binding.inputFirst.text.toString()
        locationHashMap["last_name"] = binding.inputLast.text.toString()
        locationHashMap["password"] = binding.inputPassword.text.toString()
        locationHashMap["email_id"] = binding.inputEmail.text.toString()
        locationHashMap["device_type"] = sessionManager.deviceType.toString()
        locationHashMap["device_id"] = sessionManager.deviceId.toString()
        locationHashMap["language"] = sessionManager.languageCode.toString()
        locationHashMap["referral_code"] = binding.inputReferral.text.toString().trim { it <= ' ' }
        locationHashMap["auth_type"] = commonMethods.getAuthType()
        locationHashMap["auth_id"] = commonMethods.getAuthId().toString()
        locationHashMap["gender"] = selectedGender!!
        /**
         * Signup API called
         */
        //apiService.socialsignup(locationHashMap).enqueue(new RequestCallback(REQ_SIGNUP, this));
        apiService.register(locationHashMap).enqueue(RequestCallback(REQ_SIGNUP, this))
    }


    fun socialSignup() {
        var profileimage = ""
        /* var firstnamestr: String? = null
         var lastnamestr: String? = null*/

        try {
            //profileimage = URLEncoder.encode(sessionManager.getProfilepicture(), "UTF-8");
            profileimage = sessionManager.profilepicture.toString()

            println("Fb Profile image Two : " + profileimage)
/*
            firstnamestr = URLEncoder.encode(sessionManager.firstName, "UTF-8")
            lastnamestr = URLEncoder.encode(sessionManager.lastName, "UTF-8")*/
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        commonMethods.showProgressDialog(this)
        val locationHashMap: HashMap<String, String>
        locationHashMap = HashMap()
        locationHashMap["new_user"] = "1"
        locationHashMap["user_type"] = sessionManager.type.toString()
        locationHashMap["mobile_number"] = facebookKitVerifiedMobileNumber
        locationHashMap["country_code"] = facebookVerifiedMobileNumberCountryNameCode
        locationHashMap["first_name"] = binding.inputFirst.text.toString()
        locationHashMap["last_name"] = binding.inputLast.text.toString()
        if (!isSocialLogin)
            locationHashMap["password"] = binding.inputPassword.text.toString()
        locationHashMap["email_id"] = binding.inputEmail.text.toString()
        locationHashMap["user_image"] = profileimage
        locationHashMap["device_type"] = sessionManager.deviceType.toString()
        locationHashMap["device_id"] = sessionManager.deviceId.toString()
        locationHashMap["language"] = sessionManager.languageCode.toString()
        locationHashMap["referral_code"] = binding.inputReferral.text.toString().trim { it <= ' ' }
        locationHashMap["auth_type"] = commonMethods.getAuthType()
        locationHashMap["auth_id"] = commonMethods.getAuthId().toString()
        locationHashMap["gender"] = selectedGender!!
        /**
         * Signup API called
         */
        //apiService.socialsignup(locationHashMap).enqueue(new RequestCallback(REQ_SIGNUP, this));
        apiService.register(locationHashMap).enqueue(RequestCallback(REQ_SIGNUP, this))
    }

    fun clearSocialCredintials() {
        sessionManager.facebookId = ""
        sessionManager.appleId = ""
        sessionManager.googleId = ""
        sessionManager.firstName = ""
        sessionManager.lastName = ""
        sessionManager.email = ""
    }

    fun onSuccessRegister(jsonResp: JsonResponse) {
        signinResult = gson.fromJson(jsonResp.strResponse, SigninResult::class.java)
        sessionManager.isrequest = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sessionManager.currencySymbol =
                Html.fromHtml(signinResult.currencySymbol, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            sessionManager.currencySymbol = Html.fromHtml(signinResult.currencySymbol).toString()
        }
        sessionManager.currencyCode = signinResult.currencyCode
        sessionManager.accessToken = signinResult.token
        sessionManager.walletAmount = signinResult.walletAmount
        sessionManager.userId = signinResult.userId
        sessionManager.countryCode = sessionManager.temporaryCountryCode
        sessionManager.phoneNumber = sessionManager.temporaryPhonenumber
        sessionManager.countryNameCode=binding.ccp.selectedCountryNameCode
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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
                finishAffinity()
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
}