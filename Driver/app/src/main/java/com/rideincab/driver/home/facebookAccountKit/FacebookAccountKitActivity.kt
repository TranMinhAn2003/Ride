package com.rideincab.driver.home.facebookAccountKit


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.custompalette.FontButton
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.CommonMethods.Companion.DebuggableLogD
import com.rideincab.driver.common.util.CommonMethods.Companion.DebuggableLogI
import com.rideincab.driver.common.util.Enums
import com.rideincab.driver.common.util.Enums.REQ_OTP_VERIFIACTION
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.rideincab.driver.R
import com.rideincab.driver.common.model.CheckVersionModel
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AppActivityMobileNumberVerificationBinding

class FacebookAccountKitActivity : CommonActivity(), ServiceListener {

    private lateinit var binding: AppActivityMobileNumberVerificationBinding
    lateinit var checkVersionModel: CheckVersionModel
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    internal var isPhoneNumberLayoutIsVisible = true

    /*@BindView(R.id.tv_mobile_heading)
    lateinit internal var mobileNumberHeading: TextView

    @BindView(R.id.tv_otp_resend_label)
    lateinit internal var binding.tvOtpResendLabel: TextView

    @BindView(R.id.tv_otp_resend_countdown)
    lateinit internal var binding.tvOtpResendCountdown: TextView

    @BindView(R.id.tv_resend_button)
    lateinit internal var binding.tvResendButton: TextView

    @BindView(R.id.tv_otp_error_field)
    lateinit internal var tvOTPErrorMessage: TextView

    @BindView(R.id.cl_phone_number_input)
    lateinit internal var ctlPhoneNumber: ConstraintLayout

    @BindView(R.id.cl_otp_input)
    lateinit internal var ctlOTP: ConstraintLayout

    @BindView(R.id.pb_number_verification)
    lateinit internal var pbNumberVerification: ProgressBar

    @BindView(R.id.imgv_next)
    lateinit internal var imgvArrow: ImageView

    @BindView(R.id.rl_edittexts)
    lateinit internal var rlEdittexts: RelativeLayout

    @BindView(R.id.one)
    lateinit internal var binding.one: EditText

    @BindView(R.id.two)
    lateinit internal var binding.two: EditText

    @BindView(R.id.three)
    lateinit internal var binding.three: EditText

    @BindView(R.id.four)
    lateinit internal var binding.four: EditText

    lateinit @BindView(R.id.phone)
    internal var edtxPhoneNumber: EditText

    lateinit @BindView(R.id.binding.ccp)
    var binding.ccp: CountryCodePicker

    lateinit @BindView(R.id.fab_verify)
    internal var binding.fabVerify: CardView

    @BindView(R.id.otp_verify)
    lateinit var binding.otpVerify: CardView*/

    private var isForForgotPassword = 0
    private var otp = ""
    private var receivedOTPFromServer: String? = null
    private val resendOTPWaitingSecond: Long = 120000
    private var resentCountdownTimer: CountDownTimer? = null
    private var backPressCounter: CountDownTimer? = null
    private var isDeletable = true

    private var mAuth: FirebaseAuth? = null
    private var verificationId: String? = null

    /*lateinit @BindView(R.id.tv_back_phone_arrow)
    internal var tvPhoneBack: TextView

    lateinit @BindView(R.id.tv_back_otp_arrow)
    internal var tvOTPback: TextView*/


    var dialog: AlertDialog? = null

    val FACEBOOK_ACCOUNTKIT_REQUEST_CODE = 157

    lateinit internal var facebookVerifiedPhoneNumber: String
    lateinit internal var facebookVerifiedCountryCode: String

    private lateinit var tvTermConditions: TextView
    private lateinit var iAgreeCheckBox: CheckBox
    private var iAgree: Boolean = false

    fun startAnimationd() {
        //startAnimation();
        //callSendOTPAPI()
        if (isPhoneNumberLayoutIsVisible && binding.phone.text.toString().length > 5) {
            if (checkVersionModel.otpEnabled) {
                callSendOTPAPI()
            } else if (checkVersionModel.firebaseOtpEnabled) sendOtpViaFirebase(binding.phone.text.toString().trim())
            else {
                if (isForForgotPassword == 1) {
                    redirectToActivity()
                } else {
                    if (iAgree) {
                        redirectToActivity()
                    } else Toast.makeText(this, "Please agree to continue", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        /*showOTPfield();
        showOTPMismatchIssue();*/
    }

    fun otpverify() {
        if (!isPhoneNumberLayoutIsVisible) {
            if (checkVersionModel.otpEnabled) verifyOTP()
            else verifyFirebaseOtp(otp)
        }
    }


    fun resendOTP() {
        callSendOTPAPI()
        //runCountdownTimer();

    }

    private fun redirectToActivity() {
        val returnIntent = Intent()
        returnIntent.putExtra(
            FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY,
            binding.phone.text.toString().trim { it <= ' ' })
        returnIntent.putExtra(
            FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY,
            binding.ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")
        )
        returnIntent.putExtra(
            FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY,
            binding.ccp.selectedCountryNameCode
        )
        //returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY, numberValidationModel.getStatusMessage());
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun verifyOTP() {
        showProgressBarAndHideArrow(true)
        otpVerificationAPI()
    }

    private fun otpVerificationAPI() {
        val otpParams = HashMap<String, String>()
        otpParams["otp"] = otp
        otpParams["country_code"] = binding.ccp.selectedCountryCode
        otpParams["mobile_number"] = binding.phone.text.toString()
        apiService.otpVerification(otpParams)
            .enqueue(RequestCallback(Enums.REQ_OTP_VERIFIACTION, this))

    }


    private fun shakeEdittexts() {
        val shake = TranslateAnimation(0f, 20f, 0f, 0f)
        shake.duration = 500
        shake.interpolator = CycleInterpolator(3f)
        binding.rlEdittexts.startAnimation(shake)

    }

    fun showOTPMismatchIssue() {
        shakeEdittexts()
        binding.tvOtpErrorField.visibility = View.VISIBLE
    }

    fun runCountdownTimer() {
        binding.tvResendButton.visibility = View.GONE
        binding.tvOtpResendCountdown.visibility = View.VISIBLE
        binding.tvOtpResendLabel.text = resources.getString(R.string.send_OTP_again_in)
        if (resentCountdownTimer != null) {
            resentCountdownTimer!!.cancel()
        }
        resentCountdownTimer = object : CountDownTimer(resendOTPWaitingSecond, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.tvOtpResendCountdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                binding.tvOtpResendCountdown.visibility = View.GONE
                binding.tvOtpResendLabel.text = resources.getString(R.string.resend_otp)
                binding.tvResendButton.visibility = View.VISIBLE
            }
        }.start()
    }


    fun showPhoneNumberField() {
        if (binding.clOtpInput.visibility == View.VISIBLE) {
            binding.clPhoneNumberInput.visibility = View.VISIBLE
            binding.clOtpInput.visibility = View.GONE
            isPhoneNumberLayoutIsVisible = true
            binding.tvResendButton.visibility = View.GONE
            binding.tvOtpResendLabel.visibility = View.GONE
            binding.tvOtpResendCountdown.visibility = View.GONE
            resentCountdownTimer!!.cancel()

            if (binding.phone.text.toString().length > 5) {
                binding.fabVerify.setCardBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.cabme_app_yellow
                    )
                )
                //binding.fabVerify.setCardBackgroundColor(resources.getColor(R.color.light_blue_button_color))
            } else {
                //binding.fabVerify.setCardBackgroundColor(resources.getColor(R.color.quantum_grey400))
                binding.fabVerify.setCardBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.cabme_app_yellow_disable
                    )
                )
            }
        } else {
            super.onBackPressed()
        }
    }

    /*@OnClick(R.id.tv_back_phone_arrow)
    fun finishThisActivity() {
        super.onBackPressed()
    }*/

    fun showOTPfield() {
        binding.clPhoneNumberInput.visibility = View.GONE
        binding.clOtpInput.visibility = View.VISIBLE
        isPhoneNumberLayoutIsVisible = false
        runCountdownTimer()
        binding.tvOtpResendLabel.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityMobileNumberVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        checkVersionModel = Gson().fromJson(sessionManager.checkVersionModel, CheckVersionModel::class.java)
        dialog = commonMethods.getAlertDialog(this)
        //commonMethods.setheaderText(resources.getString(R.string.register),common_header)
        initViews()
        initOTPTextviewListener()
        //startCallingFacebookKit();
       
        binding.fabVerify.setOnClickListener {
            startAnimationd()
        }
        binding.otpVerify.setOnClickListener {
            otpverify()
        }
        binding.tvResendButton.setOnClickListener {
            resendOTP()
        }
        binding.commonHeader.back.setOnClickListener {
            showPhoneNumberField()
        }
    }

    private fun initViews() {
        getIntentValues()
        binding.ccp.setAutoDetectedCountry(true)
        if (Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }
        binding.phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (binding.phone.text.toString().length > 5) {
                    binding.fabVerify.setCardBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.cabme_app_yellow
                        )
                    )
                    //binding.fabVerify.setCardBackgroundColor(resources.getColor(R.color.light_blue_button_color))
                } else {
                    //binding.fabVerify.setCardBackgroundColor(resources.getColor(R.color.quantum_grey400))
                    binding.fabVerify.setCardBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.cabme_app_yellow_disable
                        )
                    )
                }
            }
        })

        initDirectionChanges()

        if (isForForgotPassword == 0) {
            findViewById<LinearLayout?>(R.id.ll_term_condition).visibility = View.VISIBLE
            termConditionsDialog()
        }

        iAgreeCheckBox = findViewById(R.id.i_agree_box)
        iAgreeCheckBox.isChecked = true

        tvTermConditions = findViewById(R.id.tvTermConditions)
        customTextView(tvTermConditions)


    }

    private fun customTextView(view: TextView) {
        val spanTxt = SpannableStringBuilder(
            resources.getString(R.string.sigin_terms1)
        )
        spanTxt.append(resources.getString(R.string.sigin_terms2))
        spanTxt.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val url = resources.getString(R.string.termPolicyUrl)
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
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.bluetext)),
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
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.bluetext)),
            spanTxt.length - resources.getString(R.string.sigin_terms4).length,
            spanTxt.length,
            0
        )
        spanTxt.append(".")
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(spanTxt, TextView.BufferType.SPANNABLE)
    }


    private fun termConditionsDialog() {
        try {
            val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_term_condition)

            val rootView = dialog.findViewById<View>(R.id.root_dialog_term_condition)
            rootView.applySystemBarsPadding()

            val webView = dialog.findViewById<WebView>(R.id.web_view_terms)
            val btnAccept = dialog.findViewById<FontButton>(R.id.btnAccept)

            webView.loadUrl(getString(R.string.privacy_policy))
            webView.settings.javaScriptEnabled = true


            btnAccept.setOnClickListener {
                iAgreeCheckBox.isChecked = true
                iAgreeCheckBox.isEnabled = false
                iAgree = true
                dialog.dismiss()
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        } catch (e: Exception) {
            Log.i("TAG", "termConditionsDialog: Error=${e.localizedMessage}")
        }
    }

    private fun initDirectionChanges() {
        val laydir = resources.getString(R.string.layout_direction)

        if ("1" == laydir) {
            binding.fabVerify.rotation = 180f
            binding.otpVerify.rotation = 180f
            //tvPhoneBack.rotation = 180f
            //tvOTPback.rotation = 180f

        }
    }

    private fun getIntentValues() {
        try {
            isForForgotPassword = intent.getIntExtra("usableType", 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun sendOtpViaFirebase(phoneNo: String) {
        showProgressBarAndHideArrow(true)
        val mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)

                    verificationId = verifyId

                    otpSent()

                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    /* val code = phoneAuthCredential.smsCode
                     if (code != null) {
                         edtOTP?.setText(code)
                         verifyCode(code)
                     }*/
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // displaying error message with firebase exception.
                    Toast.makeText(this@FacebookAccountKitActivity, e.message, Toast.LENGTH_LONG)
                        .show()
                    showProgressBarAndHideArrow(false)
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this@FacebookAccountKitActivity, dialog,  e.message.toString())
                }
            }


        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(
                binding.ccp.selectedCountryNameCode.replace(
                    "\\+".toRegex(),
                    ""
                ) + phoneNo
            ) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun otpSent() {
        clearEditText()
        binding.otpVerify.setCardBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.cabme_app_yellow_disable
            )
        )
        showOTPfield()
        binding.one.requestFocus()
    }

    // below method is use to verify code from Firebase.
    private fun verifyFirebaseOtp(otp: String) {
        showProgressBarAndHideArrow(true)
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(OnCompleteListener<AuthResult?> { task ->
                if (task.isSuccessful) {
                  redirectToActivity()
                } else {
                    showProgressBarAndHideArrow(false)
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this@FacebookAccountKitActivity, dialog,  task.exception?.message.toString())
                }
            })
    }


    private fun initOTPTextviewListener() {
        binding.one.addTextChangedListener(OtpTextWatcher())
        binding.two.addTextChangedListener(OtpTextWatcher())
        binding.three.addTextChangedListener(OtpTextWatcher())
        binding.four.addTextChangedListener(OtpTextWatcher())

        binding.one.setOnKeyListener(OtpTextBackWatcher())
        binding.two.setOnKeyListener(OtpTextBackWatcher())
        binding.three.setOnKeyListener(OtpTextBackWatcher())
        binding.four.setOnKeyListener(OtpTextBackWatcher())


    }

    private inner class OtpTextWatcher : TextWatcher {


        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            DebuggableLogI("Cabme", "Textchange")
            if (binding.one.isFocused) {
                if (binding.one.text.toString().length > 0)
                //size as per your requirement
                {
                    binding.two.requestFocus()
                    binding.two.setSelectAllOnFocus(true)
                    //one.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                }
            } else if (binding.two.isFocused) {
                if (binding.two.text.toString().length > 0)
                //size as per your requirement
                {
                    binding.three.requestFocus()
                    binding.three.setSelectAllOnFocus(true)
                    //two.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                } else {
                    binding.one.requestFocus()
                    binding.one.setSelectAllOnFocus(true)
                    // binding.one.setSelection(1);
                }
            } else if (binding.three.isFocused) {
                if (binding.three.text.toString().length > 0)
                //size as per your requirement
                {
                    binding.four.requestFocus()
                    binding.four.setSelectAllOnFocus(true)
                    //three.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                } else {
                    binding.two.requestFocus()
                    binding.two.setSelectAllOnFocus(true)
                    //binding.two.setSelection(1);
                }
            } else if (binding.four.isFocused) {
                if (binding.four.text.toString().length == 0) {
                    binding.three.requestFocus()
                }
            }

            if (binding.one.text.toString().trim { it <= ' ' }.length > 0 && binding.two.text.toString()
                    .trim { it <= ' ' }.length > 0 && binding.three.text.toString()
                    .trim { it <= ' ' }.length > 0 && binding.four.text.toString()
                    .trim { it <= ' ' }.length > 0
            ) {
                otp = binding.one.text.toString().trim { it <= ' ' } + binding.two.text.toString()
                    .trim { it <= ' ' } + binding.three.text.toString()
                    .trim { it <= ' ' } + binding.four.text.toString().trim { it <= ' ' }
                //binding.fabVerify.setCardBackgroundColor(ContextCompat.getColor(applicationContext,R.color.light_blue_button_color))
                binding.otpVerify.setCardBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.cabme_app_yellow
                    )
                )
            } else {
                otp = ""
                //binding.fabVerify.setCardBackgroundColor(ContextCompat.getColor(applicationContext,R.color.quantum_grey400))
                binding.otpVerify.setCardBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.cabme_app_yellow_disable
                    )
                )
            }
            binding.tvOtpErrorField.visibility = View.GONE
        }

        override fun afterTextChanged(editable: Editable) {
            DebuggableLogI("Cabme", "Textchange")

        }
    }

    private inner class OtpTextBackWatcher : View.OnKeyListener {

        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {

            DebuggableLogD("keycode", keyCode.toString() + "")
            DebuggableLogD("keyEvent", event.toString())
            if (keyCode == KeyEvent.KEYCODE_DEL && isDeletable) {
                when (v.id) {
                    R.id.one -> {
                        binding.one.text.clear()
                    }
                    R.id.two -> {
                        binding.two.text.clear()
                        binding.one.requestFocus()
                        binding.one.setSelectAllOnFocus(true)
                    }
                    R.id.three -> {
                        binding.three.text.clear()
                        binding.two.requestFocus()
                        binding.two.setSelectAllOnFocus(true)
                    }
                    R.id.four -> {
                        binding.four.text.clear()
                        binding.three.requestFocus()
                        binding.three.setSelectAllOnFocus(true)
                    }//binding.three.setSelection(1);
                }
                countdownTimerForOTPBackpress()
                return true
            } else {
                return false
            }

        }
    }

    fun countdownTimerForOTPBackpress() {
        isDeletable = false
        if (backPressCounter != null) backPressCounter!!.cancel()
        backPressCounter = object : CountDownTimer(100, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                //binding.tvOtpResendCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            override fun onFinish() {
                isDeletable = true
            }
        }.start()
    }


    fun startAnimation() {
        /* View redLayout = findViewById(R.id.tv_mobile_heading);
        ViewGroup parent = findViewById(R.id.cl_phone_number_input);

        Transition transition = new Slide(Gravity.START);
        transition.setDuration(600);
        transition.addTarget(R.id.tv_mobile_heading);

        TransitionManager.beginDelayedTransition(parent, transition);
        redLayout.setVisibility(false ? View.VISIBLE : View.GONE);*/

        val RightSwipe = AnimationUtils.loadAnimation(this, R.anim.ub__slide_out_left)
        binding.tvMobileHeading.startAnimation(RightSwipe)
        RightSwipe.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                binding.tvMobileHeading.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    fun callSendOTPAPI() {
        showProgressBarAndHideArrow(true)
        apiService.numbervalidation(
            binding.phone.text.toString(),
            binding.ccp.selectedCountryNameCode.replace("\\+".toRegex(), ""),
            sessionManager.type!!,
            sessionManager.languageCode!!,
            isForForgotPassword.toString()
        ).enqueue(RequestCallback(this))
    }

    fun showProgressBarAndHideArrow(status: Boolean) {
        if (status) {
            binding.pbNumberVerification.visibility = View.VISIBLE
            binding.imgvNext.visibility = View.GONE
            //For Otp view
            binding.phNumberVerification.visibility = View.VISIBLE
            binding.imgNext.visibility = View.GONE
        } else {
            binding.pbNumberVerification.visibility = View.GONE
            binding.imgvNext.visibility = View.VISIBLE
            //For Otp view
            binding.phNumberVerification.visibility = View.GONE
            binding.imgNext.visibility = View.VISIBLE
        }
    }

/*
    private fun startCallingFacebookKit() {
        val intent = Intent(this, AccountKitActivity::class.java)
        val configurationBuilder = AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN)
        // PhoneNumber phoneNumber = new PhoneNumber(sessionManager.getTemporaryCountryCode(), sessionManager.getTemporaryPhonenumber());
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build())
        startActivityForResult(intent, FACEBOOK_ACCOUNTKIT_REQUEST_CODE)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }*/
    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == FACEBOOK_ACCOUNTKIT_REQUEST_CODE) { // confirm that this response matches your request
             val loginResult = data!!.getParcelableExtra<AccountKitLoginResult>(AccountKitLoginResult.RESULT_KEY)
             if (loginResult.error != null || loginResult.wasCancelled()) {
                 //showErrorMessageAndCloseActivity();
                 finish()

             } else {
                 getPhoneNumber()
             }
         }
     }*/

    /*   fun getPhoneNumber() {
           AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
               override fun onSuccessResponse(account: Account) {
                   val phoneNumbers: String
                   val countryCode: String
                   val phoneNumberWihtoutPlusSign: String
                  // val temporaryPhoneNumber: String

                   // Get phone number
                   val phoneNumber = account.phoneNumber
                   phoneNumbers = phoneNumber.phoneNumber.toString()
                   phoneNumberWihtoutPlusSign = phoneNumbers.replace("+", "")
                   countryCode = phoneNumber.countryCode
                   callPhoneNumberValidationAPI(phoneNumberWihtoutPlusSign, countryCode)
               }

               override fun onError(error: AccountKitError) {
                   showErrorMessageAndCloseActivity()

                   // Handle Error
               }
           })
       }*/

    /*public void phoneNumberChangedErrorMessage() {
        //commonMethods.showMessage(this, dialog, getString(R.string.InvalidMobileNumber));
        Toast.makeText(this, getString(R.string.InvalidMobileNumber), Toast.LENGTH_SHORT).show();
    }
*/

    /*void facebookAccountKitNumberVerificationSuccess() {
        setResult(ApiSessionAppConstants.FACEBOOK_ACCOUNT_KIT_VERIFACATION_SUCCESS);
        finish();
    }

    void facebookAccountKitNumberVerificationFailure() {
        setResult(ApiSessionAppConstants.FACEBOOK_ACCOUNT_KIT_VERIFACATION_FAILURE);
        finish();
    }*/

    // api call
    internal fun callPhoneNumberValidationAPI(
        facebookVerifiedPhoneNumber: String,
        facebookVerifiedCountryCode: String
    ) {
        this.facebookVerifiedCountryCode = facebookVerifiedCountryCode
        this.facebookVerifiedPhoneNumber = facebookVerifiedPhoneNumber
        commonMethods.showProgressDialog(this)
        apiService.numbervalidation(
            facebookVerifiedPhoneNumber,
            facebookVerifiedCountryCode,
            sessionManager.type!!,
            sessionManager.languageCode!!,
            ""
        ).enqueue(RequestCallback(this))
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        showProgressBarAndHideArrow(false)
        /*Intent returnIntent = new Intent();
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, facebookVerifiedPhoneNumber);
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, facebookVerifiedCountryCode);

        NumberValidationModel numberValidationModel = gson.fromJson(jsonResp.getStrResponse(), NumberValidationModel.class);

        if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_OLD_USER)) {

            returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY, numberValidationModel.getStatusMessage());
            setResult(CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER, returnIntent);
            finish();

        } else if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_NEW_USER)) {

            returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY, numberValidationModel.getStatusMessage());
            setResult(CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER, returnIntent);
            finish();
        } else {
            CommonMethods.DebuggableLogI(numberValidationModel.getStatusCode(), numberValidationModel.getStatusMessage());
        }*/
        //val numberValidationModel = gson.fromJson(jsonResp.strResponse, NumberValidationModel::class.java)
        /*

        if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_OLD_USER)) {

        } else if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_NEW_USER)) {
        }
*/
        when (jsonResp.requestCode) {
            REQ_OTP_VERIFIACTION -> {
                if (jsonResp.isSuccess) {
                    redirectToActivity()
                } else {
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    //showSettingsAlert(jsonResp.statusMsg)
                }
            }
            else -> {
                if (jsonResp.isSuccess) {
                    clearEditText()
                    receivedOTPFromServer = commonMethods.getJsonValue(
                        jsonResp.strResponse,
                        "otp",
                        String::class.java
                    ) as String
                    binding.otpVerify.setCardBackgroundColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.cabme_app_yellow_disable
                        )
                    )
                    showOTPfield()
                    /**
                     * For Test Propose
                     */
                    if (!receivedOTPFromServer.isNullOrEmpty() && getString(R.string.otp_enable).equals(
                            "true",
                            true
                        )
                    ) {
                        setOtp(receivedOTPFromServer)
                    } else {
                        binding.one.requestFocus()
                    }

                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    //commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    showSettingsAlert(jsonResp.statusMsg)
                }
            }
        }


    }

    /**
     * Setting otp on the Fields
     * @param otp
     */
    private fun setOtp(otp: String?) {
        if (otp != null) {
            binding.one.setText(otp.substring(0, 1))
            binding.two.setText(otp.substring(1, 2))
            binding.three.setText(otp.substring(2, 3))
            binding.four.setText(otp.substring(3, 4))
        }
    }

    private fun clearEditText() {
        binding.one.setText("")
        binding.two.setText("")
        binding.three.setText("")
        binding.four.setText("")
    }

    fun showSettingsAlert(statusMsg: String) {
        val alertDialog = AlertDialog.Builder(
            this
        )
        //alertDialog.setTitle(statusMsg);
        alertDialog.setMessage(statusMsg)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(
            resources.getString(R.string.ok)
        ) { _, _ -> finish() }

        alertDialog.show()
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        showErrorMessageAndCloseActivity()
    }

    private fun showErrorMessageAndCloseActivity() {
        CommonMethods.showServerInternalErrorMessage(this)
        finish()
    }

    override fun onBackPressed() {
        if (isPhoneNumberLayoutIsVisible) {
            super.onBackPressed()
        } else {
            showPhoneNumberField()
        }
    }

    companion object {


        fun openFacebookAccountKitActivity(activity: Activity) {
            val facebookIntent = Intent(activity, FacebookAccountKitActivity::class.java)
            facebookIntent.putExtra("usableType", 0)
            activity.startActivityForResult(
                facebookIntent,
                CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT
            )
        }

        fun openFacebookAccountKitActivity(activity: Activity, type: Int) {
            val facebookIntent = Intent(activity, FacebookAccountKitActivity::class.java)
            facebookIntent.putExtra("usableType", type)
            activity.startActivityForResult(
                facebookIntent,
                CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT
            )
        }
    }
}

