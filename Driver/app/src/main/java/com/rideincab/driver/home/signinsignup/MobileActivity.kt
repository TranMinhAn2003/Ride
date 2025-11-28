package com.rideincab.driver.home.signinsignup

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage signinsignup model
 * @category MobileActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.rideincab.driver.home.facebookAccountKit.FacebookAccountKitActivity
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.datamodel.ForgetPwdModel
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback

import java.util.Locale

import javax.inject.Inject

import com.google.firebase.auth.*
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.R
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityMobilenumberBinding

/* ************************************************************
                MobileActivity
Its used to get the mobile number detail function
*************************************************************** */

class MobileActivity : CommonActivity(), ServiceListener {

    lateinit var binding: ActivityMobilenumberBinding
    lateinit var dialog: AlertDialog

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    protected var isInternetAvailable: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobilenumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)
        binding.root.applySystemBarsPadding()
        binding.ccp.detectLocaleCountry(true)
        if (Locale.getDefault().language == "fa") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        } else if (Locale.getDefault().language == "es") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.SPANISH)
        } else if (Locale.getDefault().language == "ar") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        } else if (Locale.getDefault().language == "en") {
            binding.ccp.changeDefaultLanguage(CountryCodePicker.Language.ENGLISH)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.sharedElementEnterTransition.duration = 600
            window.sharedElementReturnTransition.setDuration(600).interpolator =
                DecelerateInterpolator()
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.entermobileno.transitionName = "mobilenumber"
            binding.mobilelayout.transitionName = "binding.mobilelayout"
        }

        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }

        binding.next.setOnClickListener {
            getUserProfile()
        }

        //Text listner
        binding.phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                CommonMethods.DebuggableLogI("Character sequence ", " Check")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (binding.phone.text.toString().startsWith("0")) {
                    binding.phone.setText("")
                }
            }

            override fun afterTextChanged(s: Editable) {

                CommonMethods.DebuggableLogI("Character sequence ", " Checkins")

            }
        })

        sessionManager.countryCode = binding.ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")


    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            onSuccessForgetPwd(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            onSuccessForgetPwd(jsonResp)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        binding.progressBar.visibility = View.GONE
        binding.backArrow.visibility = View.VISIBLE
    }


    private fun onSuccessForgetPwd(jsonResp: JsonResponse) {

        binding.progressBar.visibility = View.GONE
        binding.backArrow.visibility = View.VISIBLE

        val forgetPwdModel = gson.fromJson(jsonResp.strResponse, ForgetPwdModel::class.java)
        if (forgetPwdModel != null) {


            if (forgetPwdModel.statusCode.matches("1".toRegex())) {
                binding.progressBar.visibility = View.GONE
                binding.backArrow.visibility = View.VISIBLE
                sessionManager.temporaryPhonenumber = binding.phone.text.toString()
                sessionManager.temporaryCountryCode =
                    binding.ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")

                /*String otp = forgetPwdModel.getOtp();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Your OTP IS " + forgetPwdModel.getOtp(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();*/
                /*Intent intent = new Intent(getApplicationContext(), RegisterOTPActivity.class);
                intent.putExtra("otp", otp);
                intent.putExtra("resetpassword", true);
                if (sessionManager.getisEdit())
                    intent.putExtra("phone_number", binding.phone.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                if (sessionManager.getisEdit()) {
                    finish();
                }*/

                FacebookAccountKitActivity.openFacebookAccountKitActivity(this)
            } else {
                binding.progressBar.visibility = View.GONE
                binding.backArrow.visibility = View.VISIBLE
                if (forgetPwdModel.statusMessage == "Message sending Failed,please try again..") {

                    /*String otp = forgetPwdModel.getOtp();
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Your OTP IS " + forgetPwdModel.getOtp(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();*/
                    /*Intent intent = new Intent(getApplicationContext(), RegisterOTPActivity.class);
                    intent.putExtra("otp", otp);
                    intent.putExtra("resetpassword", true);
                    if (sessionManager.getisEdit())
                        intent.putExtra("phone_number", binding.phone.getText().toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                    if (sessionManager.getisEdit()) {
                        finish();
                    }*/
                    sessionManager.temporaryPhonenumber = binding.phone.text.toString()
                    sessionManager.temporaryCountryCode =
                        binding.ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")
                    FacebookAccountKitActivity.openFacebookAccountKitActivity(this)
                } else {
                    commonMethods.showMessage(this, dialog, forgetPwdModel.statusMessage)

                }
            }
        }
    }


    fun getUserProfile() {

        isInternetAvailable = commonMethods.isOnline(this)
        val phonestr = binding.phone.text.toString()


        sessionManager.countryCode = binding.ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), "")
        if (phonestr.length == 0) {

            commonMethods.showMessage(this, dialog, getString(R.string.pleaseentermobile))


        } else if (phonestr.length > 5) {
            if (isInternetAvailable) {

                binding.progressBar.visibility = View.VISIBLE
                binding.backArrow.visibility = View.GONE

                // isEdit is set from Driver profile page
                if (!sessionManager.getisEdit()) {
                    // this is from forgot password

                    apiService.numberValidation(
                        sessionManager.type!!,
                        binding.phone.text.toString(),
                        sessionManager.countryCode!!,
                        "1",
                        sessionManager.languageCode!!
                    ).enqueue(RequestCallback(this))
                    // here, binding.phone number is stored to retrive from facebook account kit
                    sessionManager.phoneNumber = binding.phone.text.toString()
                } else {

                    apiService.numberValidation(
                        sessionManager.type!!,
                        binding.phone.text.toString(),
                        sessionManager.countryCode!!,
                        "",
                        sessionManager.languageCode!!
                    ).enqueue(RequestCallback(this))


                }


            } else {
                commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
            }

        } else {
            commonMethods.showMessage(
                this,
                dialog,
                resources.getString(R.string.InvalidMobileNumber)
            )
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT) { // confirm that this response matches your request
            if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_VERIFACATION_SUCCESS) {
                if (sessionManager.getisEdit()) {
                    this.finish()
                } else {
                    callResetPasswordAPI()
                }
            }
        }
    }

    private fun callResetPasswordAPI() {
        val intent = Intent(applicationContext, ResetPassword::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

}
