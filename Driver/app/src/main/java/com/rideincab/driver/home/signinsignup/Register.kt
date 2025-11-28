package com.rideincab.driver.home.signinsignup

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage signinsignup model
 * @category Register
 * @author SMR IT Solutions
 *
 */

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.legacy.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.dependencies.module.ImageCompressAsyncTask
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.Constants.Female
import com.rideincab.driver.common.helper.Constants.Male
import com.rideincab.driver.common.helper.Constants.PICK_IMAGE_REQUEST_CODE
import com.rideincab.driver.common.helper.Constants.SELECT_FILE
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums.REQ_REG
import com.rideincab.driver.common.util.Enums.REQ_UPLOAD_PROFILE_IMG
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment.Companion.ANDROID13_CAMERA_PERMISSION_ARRAY
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment.Companion.CAMERA_PERMISSION_ARRAY
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment.Companion.checkPermissionStatus
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AppActivityRegisterBinding
import com.rideincab.driver.home.MainActivity
import com.rideincab.driver.home.datamodel.LoginDetails
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ImageListener
import com.rideincab.driver.home.interfaces.ServiceListener
import com.squareup.picasso.Picasso
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

/* ************************************************************
                Register
Its used to get the motorista register detail function
*************************************************************** */
class Register : CommonActivity(), ServiceListener, ImageListener, RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {

    lateinit var binding:AppActivityRegisterBinding
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

    var docType = 6
    private var imagePath: String = ""
    private var imageFile: File?=null
    private var profileImageRequestBody: RequestBody?=null

//    @BindView(R.id.binding.inputFirst)
//    lateinit var binding.inputFirst: EditText
/*    @BindView(R.id.binding.inputReferral)
    lateinit var binding.inputReferral: EditText
    @BindView(R.id.binding.inputLayoutReferral)
    lateinit var binding.inputLayoutReferral: TextInputLayout
//    @BindView(R.id.binding.inputLast)
//    lateinit var binding.inputLast: EditText
    @BindView(R.id.binding.emaitext)
    lateinit var binding.emaitext: EditText
    @BindView(R.id.binding.passwordtext)
    lateinit var binding.passwordtext: EditText
    @BindView(R.id.binding.cityText)
    lateinit var binding.cityText: EditText
    @BindView(R.id.binding.mobileNumber)
    lateinit var binding.mobileNumber: EditText
    *//*@BindView(R.id.input_layout_first)
    lateinit var input_layout_first: TextInputLayout*//*
   *//* @BindView(R.id.input_layout_last)
    lateinit var input_layout_last: TextInputLayout*//*
   *//* @BindView(R.id.emailName)
    lateinit var emailName: TextInputLayout
    @BindView(R.id.binding.passwordName)
    lateinit var binding.passwordName: TextInputLayout*//*
    @BindView(R.id.cityName)
    lateinit var cityName: TextInputLayout
    @BindView(R.id.mobile_code)
    lateinit var binding.mobileCode: CountryCodePicker
    *//*@BindView(R.id.btn_continue)
    lateinit var btn_continue: Button*//*
    @BindView(R.id.loginlink)
    lateinit var loginlink: TextView
    *//*@BindView(R.id.dochome_back)
    lateinit var dochome_back: ImageView*//*
   *//* @BindView(R.id.scrollView)
    lateinit var scrollView: ScrollView*//*
    @BindView(R.id.location_placesearch)
    lateinit var mRecyclerView: RecyclerView

    @BindView(R.id.rg_gender)
    lateinit var genderRadioGroup: RadioGroup*/

    private var selectedGender =""


    protected lateinit var mGoogleApiClient: GoogleApiClient
    protected var isInternetAvailable: Boolean = false
    private var oldstring = ""
    private var isCity = false


    var facebookKitVerifiedMobileNumber = ""
    var facebookVerifiedMobileNumberCountryCode = ""
    var facebookVerifiedMobileNumberCountryNameCode = ""
    /*
     *   Text watcher for city search
     */



    fun loginLink() {
        val intent = Intent(applicationContext, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= AppActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)

        /**Commmon Header Text View  and Button View*/
        commonMethods.setheaderText(resources.getString(R.string.register),binding.commonHeader.headertext)
        commonMethods.setButtonText(resources.getString(R.string.continues),binding.commonButton.button)

        //commonMethods.imageChangeforLocality(this,dochome_back)
        getMobileNumerFromIntentAndSetToEditText()
        dialog = commonMethods.getAlertDialog(this)

        showOrHideReferralAccordingToSessionData()
        isInternetAvailable = commonMethods.isOnline(this)

        //error_mob.visibility = View.GONE

        binding.inputFirst.addTextChangedListener(NameTextWatcher(binding.inputFirst))
        binding.inputLast.addTextChangedListener(NameTextWatcher(binding.inputLast))
        binding.emaitext.addTextChangedListener(NameTextWatcher(binding.emaitext))
        binding.passwordtext.addTextChangedListener(NameTextWatcher(binding.passwordtext))
        binding.cityText.addTextChangedListener(NameTextWatcher(binding.cityText))
        binding.mobileNumber.addTextChangedListener(NameTextWatcher(binding.mobileNumber))
        binding.editImage.setOnClickListener { checkAllPermission() }

        binding.locationPlacesearch.visibility = View.GONE


        //binding.mobileNumber.setText(sessionManager.getPhoneNumber());
        //binding.mobileCode.setCountryForPhoneCode(Integer.parseInt(sessionManager.getCountryCode()));
        //binding.mobileNumber.setKeyListener(null);
        //binding.mobileNumber.setEnabled(false);

        binding.vGender.rgGender.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            val radioButton = radioGroup.findViewById(i) as RadioButton
            if (radioButton.id == R.id.rd_male){
                selectedGender  = Male
            }else if (radioButton.id == R.id.rd_female){
                selectedGender = Female
            }
            validAllViews()
        })

        /*
         *   Redirect to signin page
         */
        binding.loginlink.setOnClickListener {
            loginLink()
        }
        binding.commonHeader.arrow.setOnClickListener {
            onBackPressed()
        }

        /*
         *   Validate registration fields
         */
        binding.commonButton.button.setOnClickListener { numberRegister() }

        //dochome_back.setOnClickListener { onBackPressed() }

        binding.mobileCode.setOnCountryChangeListener {
            binding.mobileCode.selectedCountryName //  Toast.makeText(getApplicationContext(), "Updated " + binding.mobileCode.getSelectedCountryName(), Toast.LENGTH_SHORT).show();
        }
        if (Locale.getDefault().language == "fa") {
            binding.mobileCode.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }
        /** Setting mobile number depends upon country code */

        binding.commonButton.button.isClickable = false
        binding.commonButton.button.background = ContextCompat.getDrawable(this,R.drawable.app_curve_button_yellow_disable)
    }

    private fun checkAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkPermissionStatus(this, supportFragmentManager, this, ANDROID13_CAMERA_PERMISSION_ARRAY, 0, 0)
        else checkPermissionStatus(this, supportFragmentManager, this, CAMERA_PERMISSION_ARRAY, 0, 0)
    }

    override fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {
        pickProfileImg()
    }

    override fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {

    }
    /**
     * Bottom Sheet to choose camera or gallery
     */

    fun pickProfileImg() {
        val view = layoutInflater.inflate(R.layout.app_camera_dialog_layout, null)
        val lltCamera = view.findViewById<LinearLayout>(R.id.llt_camera)
        val lltLibrary = view.findViewById<LinearLayout>(R.id.llt_library)
        val lltcancel = view.findViewById<LinearLayout>(R.id.llt_cancel)


        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        /* bottomSheetDialog.setCancelable(true)*/
        /*if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)*/
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        lltCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            cameraIntent()
            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            bottomSheetDialog.dismiss()
            imageFile = commonMethods.getDefaultFileName(this@Register)

            galleryIntent()
            bottomSheetDialog.dismiss()
        }
        lltcancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }


    /**
     * Intent to camera
     */
    private fun cameraIntent() {
        imageFile = commonMethods!!.cameraFilePath(this)
        commonMethods.cameraIntent(imageFile!!, this)
    }

    /**
     * Intent to gallery page
     */
    private fun galleryIntent() {
        imageFile = commonMethods.getDefaultFileName(this)
        commonMethods.galleryIntent(this)
    }

    /*
     *  Get data from camera and gallery
     */


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                if (imageFile == null) return

                imagePath = imageFile!!.path

                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            }
            SELECT_FILE -> try {
                val inputStream = this.contentResolver.openInputStream(data!!.data!!)
                val fileOutputStream = FileOutputStream(imageFile)
                commonMethods.copyStream(inputStream, fileOutputStream)
                fileOutputStream.close()
                inputStream?.close()

                if (imageFile == null) return

                imagePath = imageFile!!.path

                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            else -> {
            }
        }

    }
    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            binding.inputLayoutReferral.visibility = View.VISIBLE
        } else {
            binding.inputLayoutReferral.visibility = View.GONE
        }
    }

    private fun getMobileNumerFromIntentAndSetToEditText() {
        if (intent != null) {
            facebookKitVerifiedMobileNumber =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY).toString()
            facebookVerifiedMobileNumberCountryCode =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString()
            facebookVerifiedMobileNumberCountryNameCode =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY).toString()
        }
        binding.mobileNumber.setText(facebookKitVerifiedMobileNumber)
        binding.mobileNumber.isEnabled = false

        binding.mobileCode.setCountryForNameCode(facebookVerifiedMobileNumberCountryNameCode)
        binding.mobileCode.setCcpClickable(false)


    }

    private fun numberRegister() {


        isInternetAvailable = commonMethods.isOnline(this)
        if (!validateFirst()) {
            return
        }
        if (!validateLast()) {
            return
        }
        if (!validateEmail()) {      //setting error message in submit binding.commonButton.button
            //.error = getString(R.string.error_msg_email)
            return
        } else {
           // emailName.error = null
        }
        if (!validatePhone()) {
            //error_mob.visibility = View.VISIBLE
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.error_red))
            ViewCompat.setBackgroundTintList(binding.mobileNumber, colorStateList)
            return
        } else {
            //error_mob.visibility = View.GONE
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.app_continue))

           // val colorStateList = ColorStateList.valueOf(resources.getColor(R.color.app_continue))
            ViewCompat.setBackgroundTintList(binding.mobileNumber, colorStateList)
        }
        if (selectedGender.isNullOrEmpty()){
            Toast.makeText(this, resources.getString(R.string.error_gender), Toast.LENGTH_SHORT).show();
            return
        }
        if (!validatePassword()) {
            binding.passwordName.error = getString(R.string.error_msg_password)
            return
        } else {
            binding.passwordName.error = null
        }

        if (!validateCity()) {
            return
        }

        /*if (profileImageRequestBody==null) {
            Toast.makeText(this, resources.getString(R.string.please_update_profile_picture), Toast.LENGTH_SHORT).show();
            return
        }*/


        /*sessionManager.setFirstName(binding.inputFirst.getText().toString());
        sessionManager.setLastName(binding.inputLast.getText().toString());
        sessionManager.setEmail(binding.emaitext.getText().toString());
        sessionManager.setTemporaryPhonenumber(binding.mobileNumber.getText().toString());
        sessionManager.setPassword(binding.passwordtext.getText().toString());
        sessionManager.setTemporaryCountryCode(binding.mobileCode.getSelectedCountryCodeWithPlus().replaceAll("\\+", ""));
        sessionManager.setCity(binding.cityText.getText().toString());*/

        if (isInternetAvailable) {
            commonMethods.showProgressDialog(this@Register)
            //apiService.numberValidation(sessionManager.getType(), sessionManager.getTemporaryPhonenumber(), sessionManager.getTemporaryCountryCode(), "", sessionManager.getLanguageCode()).enqueue(new RequestCallback(REQ_VALIDATE_NUMBER),this);
            apiService.registerOtp(
                sessionManager.type!!,
                facebookKitVerifiedMobileNumber,
                facebookVerifiedMobileNumberCountryNameCode,
                binding.emaitext.text.toString(),
                binding.inputFirst.text.toString(),
                binding.inputLast.text.toString(),
                binding.passwordtext.text.toString(),
                binding.cityText.text.toString(),
                sessionManager.deviceId!!,
                sessionManager.deviceType!!,
                sessionManager.languageCode!!,
                binding.inputReferral.text.toString().trim { it <= ' ' },"email","", selectedGender!!,
//                profileImageRequestBody!!
                )
                .enqueue(RequestCallback(REQ_REG, this))

        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
        }
    }

    /*
     *   Validate first name
     */
    private fun validateFirst(): Boolean {
        if (binding.inputFirst.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_first.error = getString(R.string.error_msg_firstname)
            //requestFocus(binding.inputFirst)
            return false
        } else {
            //input_layout_first.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate last name
     */
    private fun validateLast(): Boolean {
        if (binding.inputLast.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_last.error = getString(R.string.error_msg_lastname)
            //requestFocus(binding.inputLast)
            return false
        } else {
            //input_layout_last.isErrorEnabled = false
        }
        return true
    }

    /*
     *   Validate email address
     */
    private fun validateEmail(): Boolean {
        val email = binding.emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            //emailName.setError(getString(R.string.error_msg_email));
            //requestFocus(binding.emaitext)
            return false
        } else {
            //emailName.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate phone number
     */
    private fun validatePhone(): Boolean {
        if (binding.mobileNumber.text.toString().trim { it <= ' ' }.isEmpty() || binding.mobileNumber.text.toString().length < 6) {

            //requestFocus(binding.mobileNumber)
            return false
        } else {
           //error_mob.visibility = View.GONE
            val colorStateList = ColorStateList.valueOf(resources.getColor(R.color.cabme_app_yellow))
            ViewCompat.setBackgroundTintList(binding.mobileNumber, colorStateList)
        }
        return true
    }

    /*
     *   Validate city
     */
    private fun validateCity(): Boolean {

        isCity = binding.cityText.text.toString() != ""


        if (!isCity) {
            if (binding.cityText.text.toString() == "") {
                //cityName.error = getString(R.string.error_msg_city)
            }else{
                //cityName.isErrorEnabled = false
            }
            //requestFocus(binding.cityText)
            return false
        } else {
            //cityName.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate password
     */
    private fun validatePassword(): Boolean {
        if (binding.passwordtext.text.toString().trim { it <= ' ' }.isEmpty() || binding.passwordtext.text.toString().length < 6) {
            //requestFocus(binding.passwordtext)
            return false
        } else {
            binding.passwordName.isErrorEnabled = false
        }

        return true
    }

    private fun validReferral(): Boolean {
        if (binding.inputReferral.text.toString().trim { it <= ' ' }.isEmpty() || binding.inputReferral.text.toString().length < 6) {
            //requestFocus(binding.inputFirst)
            return false
        } else {
            binding.inputLayoutReferral.isErrorEnabled = false
        }

        return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_REG -> {
                onSuccessRegisterPwd(jsonResp)
            }
            REQ_UPLOAD_PROFILE_IMG -> if (jsonResp.isSuccess) {
                onSuccessUploadImage(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }


    }

    /**
     * Upload Image using Post Method
     */
    private fun onSuccessUploadImage(jsonResponse: JsonResponse) {
//        Toast.makeText(this, R.string.image_uploaded, Toast.LENGTH_SHORT).show()
        commonMethods.hideProgressDialog()
        Picasso.get().load(commonMethods.getJsonValue(jsonResponse.strResponse, Constants.PROFILEIMAGE, String::class.java) as String).into(binding.commonProfile.profileImage1)
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val signin = Intent(applicationContext, SigninSignupHomeActivity::class.java)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }




    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    /*
     *
     *   Edit text, Text watcher
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogI("i Check", Integer.toString(i))
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
           /* if (binding.mobileNumber.text.toString().startsWith("0")) {
                binding.mobileNumber.setText("")
            }*/
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.input_first -> validateFirst()
                R.id.input_last -> validateLast()
                R.id.emaitext -> validateEmail()
                R.id.passwordtext -> validatePassword()
                R.id.mobile_number -> validatePhone()
                R.id.cityText -> validateCity()
                else -> {
                }
            }
            validAllViews()
        }
    }

    fun validAllViews(){
        if(validateFirst() && validateLast() && validateEmail() && validatePhone() && !selectedGender.isNullOrEmpty() && validatePassword() && validateCity()){
            binding.commonButton.button.isClickable = true
            binding.commonButton.button.background = ContextCompat.getDrawable(this,R.drawable.app_curve_button_yellow)
        }else{
            binding.commonButton.button.isClickable = false
            binding.commonButton.button.background = ContextCompat.getDrawable(this,R.drawable.app_curve_button_yellow_disable)
        }
    }

    private fun onSuccessRegisterPwd(jsonResp: JsonResponse) {
        commonMethods.hideProgressDialog()

        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, LoginDetails::class.java)

        if (signInUpResultModel != null) {

            try {

                if (signInUpResultModel.statusCode.matches("1".toRegex())) {


                    val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)
                    sessionManager.carType = carDeailsModel

                    /*JSONArray cardetails = new JSONArray(carDeailsModel);

                    StringBuilder type = new StringBuilder();
                    type.append(getResources().getString(R.string.vehicle_type)).append(",");
                    for (int i = 0; i < cardetails.length(); i++) {
                        JSONObject cartype = cardetails.getJSONObject(i);
                        type.append(cartype.getString("car_name")).append(",");
                    }*/

                    sessionManager.userId = signInUpResultModel.userID

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sessionManager.currencySymbol =
                            Html.fromHtml(signInUpResultModel.currencySymbol,Html.FROM_HTML_MODE_LEGACY).toString()
                    }else{
                        Html.fromHtml(signInUpResultModel.currencySymbol).toString()
                    }
                    sessionManager.currencyCode = signInUpResultModel.currencyCode
                    sessionManager.paypalEmail = signInUpResultModel.payoutId
                    sessionManager.driverSignupStatus = signInUpResultModel.userStatus
                    /*sessionManager.setType(type.toString());*/
                    sessionManager.setAcesssToken(signInUpResultModel.token)
                    sessionManager.isRegister = true
                    commonMethods.hideProgressDialog()

                    disclaimerDialog()
                } else {
                    commonMethods.showMessage(this, dialog, signInUpResultModel.statusMessage)

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }

    private fun disclaimerDialog() {
        try {
            val dialog= Dialog(this,R.style.DialogCustomTheme)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.disclaimer_dialog)

            val tvDisclaimer = dialog.findViewById<TextView>(R.id.tvDisclaimer)
            val tvAccept = dialog.findViewById<TextView>(R.id.tvAccept)

            tvDisclaimer.setText(getString(R.string.location_disclaimer))

            tvAccept.setOnClickListener {
                sessionManager.isDialogShown = "yes"
                startMainActivity()
                dialog.dismiss()
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        }catch (e:Exception){
            Log.i("TAG", "disclaimerDialog: Error=${e.localizedMessage}")
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
        finish()
    }

    companion object {

        private val BOUNDS_INDIA = LatLngBounds(
            LatLng(-0.0, 0.0), LatLng(0.0, 0.0)
        )
        private val TAG = "Register"

        /*
     *   Check email is valid or not
     */
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        commonMethods.hideProgressDialog()
        if (requestBody != null) {
            profileImageRequestBody = requestBody
            val file = File(filePath) // Convert filePath to File
            Picasso.get().load(file).into(binding.commonProfile.profileImage1)

           /* val uri = getImageUriFromMediaStore(this@Register, filePath)
            uri?.let {
                Picasso.get().load(it).into(binding.commonProfile.profileImage1)
            }*/
        }
        Log.i(TAG, "onImageCompress: if=${requestBody != null}")
        Log.i(TAG, "onImageCompress: filePath=$filePath")
//        requestBody?.let { apiService.uploadProfileImage(it, sessionManager.accessToken!!).enqueue(RequestCallback(
//            REQ_UPLOAD_PROFILE_IMG, this)) }
    }

    fun getImageUriFromMediaStore(context: Context, filePath: String): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(filePath)

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            }
        }
        return null
    }


}
