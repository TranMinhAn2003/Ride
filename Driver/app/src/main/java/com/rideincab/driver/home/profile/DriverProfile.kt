package com.rideincab.driver.home.profile

/**
 * @package com.cloneappsolutions.cabmedriver.home.profile
 * @subpackage profile model
 * @category DriverProfile
 * @author SMR IT Solutions
 *
 */


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.squareup.picasso.Picasso
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.dependencies.module.ImageCompressAsyncTask
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.Constants.Female
import com.rideincab.driver.common.helper.Constants.Male
import com.rideincab.driver.common.helper.Constants.PICK_IMAGE_REQUEST_CODE
import com.rideincab.driver.common.helper.Constants.SELECT_FILE
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.helper.RunTimePermission
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.rideincab.driver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums.REQ_DRIVER_PROFILE
import com.rideincab.driver.common.util.Enums.REQ_UPDATE_PROFILE
import com.rideincab.driver.common.util.Enums.REQ_UPLOAD_PROFILE_IMG
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment.Companion.ANDROID13_CAMERA_PERMISSION_ARRAY
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment.Companion.CAMERA_PERMISSION_ARRAY
import com.rideincab.driver.common.util.RuntimePermissionDialogFragment.Companion.checkPermissionStatus
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityDriverProfileBinding
import com.rideincab.driver.databinding.AppActivityDriverProfileBinding
import com.rideincab.driver.home.facebookAccountKit.FacebookAccountKitActivity
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ImageListener
import com.rideincab.driver.home.interfaces.ServiceListener
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject


/* ************************************************************
                DriverProfile
Its used to get motorista profile details to show view on screen
*************************************************************** */

class DriverProfile : CommonActivity(), ServiceListener, ImageListener, RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {

    private lateinit var binding:AppActivityDriverProfileBinding

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var runTimePermission: RunTimePermission

    @Inject
    lateinit var sessionManager: SessionManager

    /*@BindView(R.id.editicon)
    lateinit var editicon: ImageView*/
    /*   @BindView(R.id.cameraicon)
       lateinit var cameraicon: ImageView*/

/*    @BindView(R.id.arrow)
    lateinit var arrow: ImageView

    @BindView(R.id.binding.inputFirst)
    lateinit var binding.inputFirst: EditText

    @BindView(R.id.binding.inputLast)
    lateinit var binding.inputLast: EditText

    @BindView(R.id.binding.emaitext)
    lateinit var binding.emaitext: EditText

    @BindView(R.id.mobile_number)
    lateinit var mobile_number: TextView

    @BindView(R.id.binding.addresstext)
    lateinit var binding.addresstext: EditText

    @BindView(R.id.binding.addresstext2)
    lateinit var binding.addresstext2: EditText

    @BindView(R.id.binding.citytext)
    lateinit var binding.citytext: EditText

    @BindView(R.id.binding.posttext)
    lateinit var binding.posttext: EditText

    @BindView(R.id.binding.statetext)
    lateinit var binding.statetext: EditText*/

/*    @BindView(R.id.binding.savebutton)
    lateinit var binding.savebutton: Button

    @BindView(R.id.mobile_code)
    lateinit var binding.mobileCode: CountryCodePicker

    @BindView(R.id.binding.commonProfile.profileImage1)
    lateinit var binding.commonProfile.profileImage1: ImageView

    @BindView(R.id.flaglayout)
    lateinit var flaglayout: RelativeLayout

    @BindView(R.id.rg_gender)
    lateinit var binding.vGender.rgGender: RadioGroup*/

    /*  @BindView(R.id.input_layout_first)
      lateinit var input_layout_first: TextInputLayout
      @BindView(R.id.input_layout_last)
      lateinit var input_layout_last: TextInputLayout
      @BindView(R.id.emailName)
      lateinit var emailName: TextInputLayout
      @BindView(R.id.addressName)
      lateinit var addressName: TextInputLayout
      @BindView(R.id.addressName2)
      lateinit var addressName2: TextInputLayout
      @BindView(R.id.cityName)
      lateinit var cityName: TextInputLayout
      @BindView(R.id.stateName)
      lateinit var stateName: TextInputLayout
      @BindView(R.id.postName)
      lateinit var postName: TextInputLayout*/
    /*@BindView(R.id.close)
    lateinit var close: TextView*/




    lateinit var first_name: String
    lateinit var last_name: String
    lateinit var mobile_numbers: String
    lateinit var country_code: String
    lateinit var email_id: String
    lateinit var user_thumb_image: String
    lateinit var address_line1: String
    lateinit var address_line2: String
    lateinit var city: String
    lateinit var state: String
    lateinit var postal_code: String
    lateinit var gender: String
    var bm: Bitmap? = null
    var docType = 6          //By Default Set 6 for Profile Image
    private var isUpdate = false
    private lateinit var dialog: AlertDialog
    private var imagePath: String = ""
    lateinit private var imageFile: File
    private var imageUri: Uri? = null

    /**
     * Getting Driver Deatils to Update
     *
     * @return hashmap Datas
     */
    private val details: LinkedHashMap<String, String>
        get() {
            val hashMap = LinkedHashMap<String, String>()
            hashMap["first_name"] = first_name
            hashMap["last_name"] = last_name
            hashMap["email_id"] = email_id
            hashMap["mobile_number"] = mobile_numbers
            hashMap["country_code"] = binding.mobileCode.selectedCountryNameCode.replace("\\+".toRegex(), "")
            hashMap["address_line1"] = address_line1
            hashMap["address_line2"] = address_line2
            hashMap["city"] = city
            hashMap["state"] = state
            hashMap["postal_code"] = postal_code
            hashMap["token"] = sessionManager.accessToken!!
            hashMap["profile_image"] = user_thumb_image
            return hashMap
        }


    /*@OnClick(R.id.mobile_code)
    public void mobilecode() {
        sessionManager.setisEdit(true);
        Intent mobile = new Intent(getApplicationContext(), MobileActivity.class);
        startActivity(mobile);
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=AppActivityDriverProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.profile), binding.commonHeader.headertext)

        dialog = commonMethods.getAlertDialog(this)

        binding.inputFirst.addTextChangedListener(NameTextWatcher(binding.inputFirst))
        binding.inputLast.addTextChangedListener(NameTextWatcher(binding.inputLast))
        binding.emaitext.addTextChangedListener(NameTextWatcher(binding.emaitext))
        binding.addresstext.addTextChangedListener(NameTextWatcher(binding.addresstext))
        binding.addresstext2.addTextChangedListener(NameTextWatcher(binding.addresstext2))
        binding.citytext.addTextChangedListener(NameTextWatcher(binding.citytext))
        binding.statetext.addTextChangedListener(NameTextWatcher(binding.statetext))
        binding.posttext.addTextChangedListener(NameTextWatcher(binding.posttext))

        for (i in 0 until binding.vGender.rgGender.childCount) {
            binding.vGender.rgGender.getChildAt(i).isClickable = false
        }
        binding.mobileCode.setCcpClickable(false)

        /*
         * Get motorista profile API call
         */
        val profiledetails = sessionManager.profileDetail
        if (profiledetails == null) {
            getDriver()
        } else {
            /*
             *  Load motorista profile API
             *
             */
            loadData(profiledetails)
        }

        binding.inputFirst.isFocusableInTouchMode = true
        binding.inputLast.isFocusableInTouchMode = true
        binding.emaitext.isFocusableInTouchMode = true
        binding.addresstext.isFocusableInTouchMode = true
        binding.addresstext2.isFocusableInTouchMode = true
        binding.citytext.isFocusableInTouchMode = true
        binding.statetext.isFocusableInTouchMode = true
        binding.posttext.isFocusableInTouchMode = true
        /*  cameraicon.visibility = View.VISIBLE*/

        binding.commonHeader.back.setOnClickListener { onBackPressed() }
        binding.savebutton.setOnClickListener { getValidation() }
        binding.editImage.setOnClickListener { checkAllPermission() }
        binding.flaglayout.setOnClickListener { FacebookAccountKitActivity.openFacebookAccountKitActivity(this) }
        binding.mobileNumber.setOnClickListener { FacebookAccountKitActivity.openFacebookAccountKitActivity(this) }

    }

    private fun getValidation() {
        if (!validateText(binding.inputFirst)) {
            return
        }
        if (!validateText(binding.inputLast)) {
            return
        }

        if (!validateEmail()) {
            return
        }
        if (!validatePhone()) {
            return
        }
        if (!validateText(binding.addresstext)) {
            return
        }
        /*if (!validateText(binding.addresstext2)) {
            return
        }*/
        if (!validateText(binding.citytext)) {
            return
        }
        if (!validateText(binding.statetext)) {
            return
        }
        if (!validateText(binding.posttext)) {
            return
        }

        /*
         *  get Driver profile data
         */
        first_name = binding.inputFirst.text.toString()
        last_name = binding.inputLast.text.toString()
        email_id = binding.emaitext.text.toString()
        mobile_numbers = binding.mobileNumber.text.toString()
        address_line1 = binding.addresstext.text.toString()
        address_line2 = binding.addresstext2.text.toString()
        city = binding.citytext.text.toString()
        state = binding.statetext.text.toString()
        postal_code = binding.posttext.text.toString()
        isUpdate = true

        updateDriver()
    }

    private fun updateDriver() {
        commonMethods.showProgressDialog(this)
        apiService.updateDriverProfile(details).enqueue(RequestCallback(REQ_UPDATE_PROFILE, this))
    }

    /**
     * Api call to fetch profile details
     */

    private fun getDriver() {
        commonMethods.showProgressDialog(this)
        apiService.getDriverProfile(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_DRIVER_PROFILE, this))
    }

    /**
     * Upload Image using Post Method
     */
    private fun onSuccessUploadImage(jsonResponse: JsonResponse) {
//        Toast.makeText(this, R.string.image_uploaded, Toast.LENGTH_SHORT).show()
        user_thumb_image = commonMethods.getJsonValue(jsonResponse.strResponse, Constants.PROFILEIMAGE, String::class.java) as String
        loadImage(user_thumb_image)
        getValidation()
    }

    /**
     * Load Image
     */
    private fun loadImage(imageurl: String) {
        commonMethods.hideProgressDialog()
        Picasso.get().load(imageurl).into(binding.commonProfile.profileImage1)
    }

    private fun checkAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkPermissionStatus(this, supportFragmentManager, this, ANDROID13_CAMERA_PERMISSION_ARRAY, 0, 0)
        else checkPermissionStatus(this, supportFragmentManager, this, CAMERA_PERMISSION_ARRAY, 0, 0)
    }

    /**
     * If Deny open and Enable the permission
     */
    /* private fun showEnablePermissionDailog(type: Int, message: String) {
         if (!customDialog.isVisible) {
             customDialog = CustomDialog("Alert", message, getString(R.string.ok), CustomDialog.btnAllowClick {
                 override fun clicked() {
                     if (type == 0)
                         callPermissionSettings()
                     else
                         startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 101)
                 }
             })
             customDialog.show(supportFragmentManager, "")
         }
     }*/

    /**
     * Opens the APP Permission Settings Page
     */
    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
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
            imageFile = commonMethods.getDefaultFileName(this@DriverProfile)

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
        commonMethods.cameraIntent(imageFile, this)
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
                imagePath = imageFile.path

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
                imagePath = imageFile.path
                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT -> {
                /*  if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER) {
                    updateDriverPhoneNumber(data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY));
                } else if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER) {
                    commonMethods.showMessage(this, dialog, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY));

                }*/

                if (resultCode == Activity.RESULT_OK) {
                    updateDriverPhoneNumber(data!!.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY)!!, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY)!!)
                }

            }
            else -> {
            }
        }

    }

    fun updateDriverPhoneNumber(phoneNumber: String, countryCode: String) {
        if (phoneNumber != null) {
            binding.mobileNumber.setText(phoneNumber)
            binding.mobileCode.setCountryForNameCode(countryCode)

        }

    }

    /*
     * Get image path from gallery
     */
    private fun onSelectFromGalleryResult(data: Intent?) {

        bm = null
        if (data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            /*  cursor.close()*/
            bm = BitmapFactory.decodeFile(picturePath)

            imagePath = picturePath

            if (!TextUtils.isEmpty(imagePath)) {
                commonMethods.showProgressDialog(this)
                ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
            }
        }
    }


    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        requestBody?.let { apiService.uploadProfileImage(it, sessionManager.accessToken!!).enqueue(RequestCallback(REQ_UPLOAD_PROFILE_IMG, this)) }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_UPDATE_PROFILE -> {
                val statusmessage = jsonResp.statusMsg  //(String) commonMethods.getJsonValue(jsonResp.getStrResponse(), "status_message", String.class);
                if (jsonResp.isSuccess) {
                    sessionManager.profileDetail = jsonResp.strResponse

                    if (isUpdate) {
                        isUpdate = false
                        commonMethods.showMessage(this, dialog, resources.getString(R.string.update_successfully))

                    } else {
                        loadData(jsonResp.strResponse)
                        //jsonResp.strResponse?.let { loadData(it) }
                    }
                } else {
                    commonMethods.showMessage(this, dialog, statusmessage)
                }
            }
            REQ_UPLOAD_PROFILE_IMG -> if (jsonResp.isSuccess) {
                onSuccessUploadImage(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_DRIVER_PROFILE -> {
                val currency_code = commonMethods.getJsonValue(jsonResp.strResponse, "currency_code", String::class.java) as String
                val dialcode = commonMethods.getJsonValue(jsonResp.strResponse, "country_code", String::class.java) as String
                var currency_symbol = commonMethods.getJsonValue(jsonResp.strResponse, "currency_symbol", String::class.java) as String
                //val carid = commonMethods.getJsonValue(jsonResp.strResponse, "car_id", String::class.java) as String
                val oweAmount = commonMethods.getJsonValue(jsonResp.strResponse, "owe_amount", String::class.java) as String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency_symbol = Html.fromHtml(currency_symbol, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    currency_symbol = Html.fromHtml(currency_symbol).toString()
                }
                sessionManager.currencyCode = currency_code
                sessionManager.countryCode = dialcode
                sessionManager.currencySymbol = currency_symbol
                //sessionManager.vehicle_id = carid
                sessionManager.oweAmount = oweAmount

                if (jsonResp.isSuccess)
                   Toast.makeText(this, jsonResp.statusMsg, Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    fun loadData(profiledetails: String) {
        try {
            val jsonObj = JSONObject(profiledetails)

            first_name = jsonObj.getString("first_name")
            last_name = jsonObj.getString("last_name")
            mobile_numbers = jsonObj.getString("mobile_number")
            country_code = jsonObj.getString("country_code")
            CommonMethods.DebuggableLogI("Country code from api", country_code)
            email_id = jsonObj.getString("email_id")
            user_thumb_image = jsonObj.getString("profile_image")
            address_line1 = jsonObj.getString("address_line1")
            address_line2 = jsonObj.getString("address_line2")
            city = jsonObj.getString("city")
            state = jsonObj.getString("state")
            postal_code = jsonObj.getString("postal_code")
            gender = jsonObj.getString("gender")



            if ("" != first_name) binding.inputFirst.setText(first_name)

            if ("" != last_name) binding.inputLast.setText(last_name)

            if ("" != email_id) binding.emaitext.setText(email_id)

            if ("" != mobile_numbers) binding.mobileNumber.setText(mobile_numbers)
            sessionManager.phoneNumber = mobile_numbers

            if ("" != address_line1) binding.addresstext.setText(address_line1)

            if ("" != address_line2) binding.addresstext2.setText(address_line2)

            if ("" != city) binding.citytext.setText(city)

            if ("" != state) binding.statetext.setText(state)

            if ("" != postal_code) binding.posttext.setText(postal_code)
            if ("" != sessionManager.countryCode) {
                binding.mobileCode.setCountryForNameCode(sessionManager.countryCode)
            }
            Picasso.get().load(user_thumb_image).into(binding.commonProfile.profileImage1)

            if (gender.isNotEmpty()) {
                if (gender.equals(Male, true)) {
                    binding.vGender.rgGender.check(R.id.rd_male)
                } else if (gender.equals(Female, true)) {
                    binding.vGender.rgGender.check(R.id.rd_female)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun validateText(input: EditText): Boolean {
        if (input.text.toString().trim { it <= ' ' }.isEmpty()) {
            if (input.id == R.id.input_first)
                return false
            else if (input.id == R.id.input_last)
                return false
            else if (input.id == R.id.addresstext)
                return false
            /* else if (input.id == R.id.addresstext2)
                 return false*/
            else if (input.id == R.id.citytext)
                return false
            else if (input.id == R.id.statetext)
                return false
            else if (input.id == R.id.posttext)
                return false
            requestFocus(input)
            return false
        } else {
        }

        return true
    }

    /*
     *  Validate Email address
     */
    private fun validateEmail(): Boolean {
        val email = binding.emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            /*emailName.error = getString(R.string.error_msg_email)
            requestFocus(binding.emaitext)*/
            return false
        } else {
            /* emailName.isErrorEnabled = false*/
        }

        return true
    }

    /*
     *  Validate phone number
     */
    private fun validatePhone(): Boolean {
        if (binding.mobileNumber.text.toString().trim { it <= ' ' }.isEmpty() || binding.mobileNumber.text.toString().length < 6) {
            commonMethods.showMessage(this, dialog, getString(R.string.error_msg_phone))
            requestFocus(binding.mobileNumber)
            return false
        }

        return true
    }

    /*
     * Edit address request focus
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.getisEdit()) {
            binding.mobileNumber.setText(sessionManager.phoneNumber)
            CommonMethods.DebuggableLogI("Country code from session", sessionManager.countryCode)
            binding.mobileCode.setCountryForNameCode(sessionManager.countryCode)
            sessionManager.setisEdit(false)
        }
    }

    override fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {
        pickProfileImg()
    }

    override fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {

    }

    /*
     *  edit taxtwatcher
     */
    private inner class NameTextWatcher internal constructor(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogV("Do", "Nothing")
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogV("Do", "Nothing")
            if (validateEmail() && validateText(binding.inputFirst) && validateText(binding.inputLast) && validateText(binding.addresstext) && validateText(binding.citytext) && validateText(binding.statetext) && validateText(binding.posttext)) {
                binding.savebutton.isEnabled = true
                binding.savebutton.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_yellow)
            } else {
                binding.savebutton.isEnabled = false
                binding.savebutton.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_yellow_disable)
            }/*&& validateText(binding.addresstext) && validateText(binding.citytext) && validateText(binding.statetext) && validateText(binding.posttext)*/
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {

                R.id.input_first -> validateText(binding.inputFirst)
                R.id.input_last -> validateText(binding.inputLast)
                R.id.emaitext -> validateEmail()
                R.id.mobile_number -> validatePhone()
                R.id.addresstext -> validateText(binding.addresstext)
                //  R.id.addresstext2 -> validateText(binding.addresstext2)
                R.id.citytext -> validateText(binding.citytext)
                R.id.statetext -> validateText(binding.statetext)
                R.id.posttext -> validateText(binding.posttext)
                else -> {
                }
            }
        }
    }

    companion object {

        /*
     * check is valid email or not
     */
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

}
