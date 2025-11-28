@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.rideincab.user.taxi.sidebar

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar
 * @category Profile
 * @author SMR IT Solutions
 *
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.backgroundtask.ImageCompressAsyncTask
import com.rideincab.user.common.configs.RunTimePermission
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants.Female
import com.rideincab.user.common.helper.Constants.Male
import com.rideincab.user.common.helper.Constants.PICK_IMAGE_REQUEST_CODE
import com.rideincab.user.common.helper.Constants.SELECT_FILE
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ImageListener
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.common.utils.Enums.REQ_GET_RIDER_PROFILE
import com.rideincab.user.common.utils.Enums.REQ_UPDATE_PROFILE
import com.rideincab.user.common.utils.Enums.REQ_UPLOAD_PROFILE_IMG
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.utils.RuntimePermissionDialogFragment
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityProfileBinding
import com.rideincab.user.taxi.datamodels.RiderProfile
import com.rideincab.user.taxi.views.customize.CustomDialog
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import javax.inject.Inject


/* ************************************************************
   Rider profile details page
    *********************************************************** */

class Profile : CommonActivity(), ServiceListener, ImageListener,
    RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {

    private lateinit var binding: AppActivityProfileBinding

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
    lateinit var runTimePermission: RunTimePermission

    @Inject
    lateinit var gson: Gson

    /*  @BindView(R.id.binding.emaitext)
  lateinit var binding.emaitext: EditText // Email Edit text*/
    /*  @BindView(R.id.input_layout_first)
      lateinit var input_layout_first: TextInputLayout
      @BindView(R.id.input_layout_last)
      lateinit var input_layout_last: TextInputLayout*/
    /* @BindView(R.id.input_layout_email)
     lateinit var emailName: TextInputLayout*/
    lateinit var firstnamegettext: String
    lateinit var lastnamegettext: String
    lateinit var emailgettext: String  // First, Last, Email text string

/*    @BindView(R.id.binding.savebutton)
    lateinit var binding.savebutton: Button // Save button

    @BindView(R.id.binding.mobilenumber)
    lateinit var binding.mobilenumber: TextView  // Mobile number

    @BindView(R.id.profile_image1)
    lateinit var binding.commonProfile.profileImage1: ImageView  // Profile image view

    @BindView(R.id.mobile_code)
    lateinit var binding.mobileCode: CountryCodePicker  // Country code picker

    @BindView(R.id.binding.emaitext)
    lateinit var binding.emaitext: EditText

    @BindView(R.id.binding.inputFirst)
    lateinit var binding.inputFirst: EditText

    @BindView(R.id.binding.inputLast)
    lateinit var binding.inputLast: EditText

    @BindView(R.id.common_profile)
    lateinit var commonProfile: View

    @BindView(R.id.rg_gender)
    lateinit var binding.vGender.rgGender: RadioGroup*/

    private var gender: String? = null

    var bm: Bitmap? = null// Image bitmap
    lateinit var imagepath: String  // Image file path
    lateinit var imageInSD: String  // Store image in SD Card
    lateinit var image: File   // image file
    lateinit var imageUr: String  // Profile image url
    protected var isInternetAvailable: Boolean = false  // Check Network available or not
    private var imageFile: File? = null
    private val imageUri: Uri? = null

    /*@OnClick(R.id.phonelayout)
    fun mobileclick() {
        FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 0)
    }
    @OnClick(R.id.binding.mobilenumber)
    fun mobileclick1() {
        FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 0)
    }*/


    /*@OnClick(R.id.transprantview)
    fun mobileCodeClick() {
        FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 0)
    }
*/
    

    fun savebutton() {
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this@Profile, dialog, getString(R.string.no_connection))
        } else {

            firstnamegettext = binding.inputFirst.text.toString()
            lastnamegettext = binding.inputLast.text.toString()
            emailgettext = binding.emaitext.text.toString()


            if (!validateFirst()) {
                return
            }
            if (!validateLast()) {
                return
            }
            if (!validateEmail()) {
                //  emailName.error = getString(R.string.error_msg_email)
                return
            }

            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            /*try {
                firstnamegettext = URLEncoder.encode(firstnamegettext, "UTF-8")
                lastnamegettext = URLEncoder.encode(lastnamegettext, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }*/

            /**
             * Update Rider profile
             */

            hideSoftKeyboard()

            binding.emaitext.isFocusableInTouchMode = true
            binding.emaitext.isFocusable = true
            binding.emaitext.requestFocus()

            // Update profile API Call
            updateProfile()
        }
    }

    fun profileImage() {// Profile click listener
        //marshMallowPermission.getPhotoFromCamera();
        //checkAllPermission(Constants.PERMISSIONS_PHOTO);
        pickProfileImg()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(this)


        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.editaccount), binding.commonHeader.tvHeadertext)
        // Check network available or not
        isInternetAvailable = commonMethods.isOnline(applicationContext)

        binding.inputFirst.addTextChangedListener(NameTextWatcher(binding.inputFirst))
        binding.inputLast.addTextChangedListener(NameTextWatcher(binding.inputLast))
        binding.emaitext.addTextChangedListener(NameTextWatcher(binding.emaitext))

        val profiledetails =
            sessionManager.profileDetail.toString() // Get Profile details from JSON

        for (i in 0 until binding.vGender.rgGender.childCount) {
            binding.vGender.rgGender.getChildAt(i).isClickable = false
        }

        if (profiledetails.isEmpty() || profiledetails == "null") {
            if (!isInternetAvailable) {
                commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
            } else {
                getRiderDetails() // Get JSON data From profile API
            }
        } else {
            loaddata(profiledetails) // Load Profile details
        }

        binding.commonHeader.back.setOnClickListener { super.onBackPressed() }
        binding.savebutton.setOnClickListener { savebutton() }
        binding.editImage.setOnClickListener { profileImage() } 

    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    /**
     * Back button pressed
     */
    override fun onBackPressed() {
        super.onBackPressed()
        sessionManager.profileDetail = ""
        sessionManager.phoneNumber = ""
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            // Get Rider Profile
            REQ_GET_RIDER_PROFILE -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessGetProf(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            // Update Rider Location
            REQ_UPDATE_PROFILE -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessUpdateProf(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            REQ_UPLOAD_PROFILE_IMG -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                onSuccessProf(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            else -> commonMethods.hideProgressDialog()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

    /**
     * Update rider profile details API called
     */
    fun updateProfile() {
        commonMethods.showProgressDialog(this)
        apiService.updateProfile(
            imageUr,
            firstnamegettext,
            lastnamegettext,
            binding.mobileCode.selectedCountryNameCode.replace("\\+".toRegex(), ""),
            binding.mobilenumber.text.toString(),
            emailgettext,
            sessionManager.accessToken.toString()
        ).enqueue(RequestCallback(REQ_UPDATE_PROFILE, this))
    }

    private fun onSuccessGetProf(jsonResp: JsonResponse) {
        if (jsonResp.statusCode.matches("1".toRegex())) {
            sessionManager.profileDetail = jsonResp.strResponse
            val riderProfile = gson.fromJson(jsonResp.strResponse, RiderProfile::class.java)
            sessionManager.walletAmount = riderProfile.walletAmount
            loaddata(jsonResp.strResponse)
        } else if (jsonResp.statusCode.matches("2".toRegex())) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    fun onSuccessProf(jsonResp: JsonResponse) {
        sessionManager.profileDetail = jsonResp.strResponse
        val profile_imageget = commonMethods.getJsonValue(
            jsonResp.strResponse,
            "image_url",
            String::class.java
        ) as String

        imageUr = profile_imageget
        Picasso.get().load(imageUr)
            .into(binding.commonProfile.profileImage1)
    }

    fun onSuccessUpdateProf(jsonResp: JsonResponse) {
        sessionManager.profileDetail = jsonResp.strResponse
        val profile_imageget = commonMethods.getJsonValue(
            jsonResp.strResponse,
            "profile_image",
            String::class.java
        ) as String

        imageUr = profile_imageget
        Picasso.get().load(imageUr)
            .into(binding.commonProfile.profileImage1)
        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
    }

    /**
     * Check Camera and gallery image data
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                if (imageFile == null) return
                imagepath = imageFile?.path!!
                if (!TextUtils.isEmpty(imagepath)) {
                    ImageCompressAsyncTask(this, imagepath, this).execute()
                }
            }

            SELECT_FILE -> try {
                val inputStream = this.contentResolver.openInputStream(data!!.data!!)
                val fileOutputStream = FileOutputStream(imageFile)
                commonMethods.copyStream(inputStream, fileOutputStream)
                fileOutputStream.close()
                inputStream?.close()
                if (imageFile == null) return
                imagepath = imageFile?.path!!
                val bm = BitmapFactory.decodeFile(imagepath)
                binding.commonProfile.profileImage1.setImageBitmap(bm)
                if (isInternetAvailable) {
                    ImageCompressAsyncTask(this, imagepath, this).execute()
                } else {
                    commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateRiderPhoneNumber(
                        data!!.getStringExtra(
                            FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
                        ),
                        data.getStringExtra(CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY)!!
                    )
                }
            }/*if(resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER){
                            updateRiderPhoneNumber(data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY),data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY));
                        }else if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER){
                            commonMethods.showMessage(this, dialog, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY));

                        }*/
            else -> {
            }
        }

    }

    private fun updateRiderPhoneNumber(phoneNumber: String?, countryCode: String) {
        if (phoneNumber != null) {
            binding.mobilenumber.text = phoneNumber
            binding.mobileCode.setCountryForNameCode(countryCode)
            binding.savebutton.isEnabled = true
        }
    }


    /**
     * convert Image bitmap to image path
     */
    fun imageWrite(bitmap: Bitmap): String {

        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        lateinit var outStream: OutputStream
        val file = File(extStorageDirectory, "slectimage.png")
        try {
            outStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        imageInSD = getString(R.string.img_src)

        return imageInSD

    }

    /**
     * Load rider profile data
     */
    fun loaddata(profiledetails: String) {
        try {
            val jsonObj = JSONObject(profiledetails)
            val first_name = jsonObj.getString("first_name")
            val last_name = jsonObj.getString("last_name")
            val mobile_number = jsonObj.getString("mobile_number")
            val email_id = jsonObj.getString("email_id")
            val user_thumb_image = jsonObj.getString("profile_image")
            gender = jsonObj.getString("gender")

            sessionManager.countryCode = jsonObj.getString("country_code")
            sessionManager.dialCode = jsonObj.getString("country_code")
            if (sessionManager.countryCode != "")
                binding.mobileCode.setCountryForNameCode(sessionManager.countryCode)


            sessionManager.phoneNumber = jsonObj.getString("mobile_number")
            binding.inputFirst.setText(first_name)
            binding.mobilenumber.text = mobile_number
            binding.inputLast.setText(last_name)
            if ("" != email_id)
                binding.emaitext.setText(email_id)
            imageUr = user_thumb_image

            Picasso.get().load(imageUr)
                .into(binding.commonProfile.profileImage1)

            /* val rb = findViewById<View>(R.id.rd_female) as RadioButton
             val rb2 = findViewById<View>(R.id.rd_female) as RadioButton
             val font: Typeface = Typeface.createFromAsset(assets, resources.getString(R.string.fonts_UBERMedium))
             val font1: Typeface = Typeface.createFromAsset(assets, resources.getString(R.string.fonts_UBERMedium))
             rb.setTypeface(font)
             rb2.setTypeface(font1)*/

            if (!gender.isNullOrEmpty()) {
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

    /**
     * Get Rider profile API called
     */
    fun getRiderDetails() {
        commonMethods.showProgressDialog(this)
        apiService.getRiderProfile(commonMethods.getRiderProfile(false, null))
            .enqueue(RequestCallback(REQ_GET_RIDER_PROFILE, this))

    }

    /**
     * Validate Email
     */
    private fun validateEmail(): Boolean {
        val email = binding.emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            // emailName.setError(getString(R.string.error_msg_email));
            //requestFocus(binding.emaitext);
            return false
        } else {
            // emailName.isErrorEnabled = false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    /**
     * Validate first name
     */
    private fun validateFirst(): Boolean {
        if (binding.inputFirst.text.toString().trim { it <= ' ' }.isEmpty()) {
            //  input_layout_first.error = getString(R.string.error_msg_firstname)
            //requestFocus(binding.inputFirst);
            return false
        } else {
            //input_layout_first.isErrorEnabled = false
        }

        return true
    }

    /**
     * Validate last name
     */
    private fun validateLast(): Boolean {
        if (binding.inputLast.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_last.error = getString(R.string.error_msg_lastname)
            //requestFocus(binding.inputLast);
            return false
        } else {
            // input_layout_last.isErrorEnabled = false
        }
        return true
    }


    /*public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermissionStatus(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }*/

    /**
     * Camera permission
     */
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permission = runTimePermission.onRequestPermissionsResult(permissions, grantResults);
        DebuggableLogV("permission", "permission" + permission);
        DebuggableLogV("permission", "grantResults" + grantResults);
        if (permission != null && !permission.isEmpty()) {
            runTimePermission.setFirstTimePermission(true);
            String[] dsf = new String[permission.size()];
            permission.toArray(dsf);
            checkAllPermission(dsf);
        } else {
            pickProfileImg();
        }
    }
*/
    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        commonMethods.hideProgressDialog()
        if (!TextUtils.isEmpty(filePath)) {


            commonMethods.showProgressDialog(this)
            apiService.uploadImage(requestBody!!, sessionManager.accessToken.toString())
                .enqueue(RequestCallback(REQ_UPLOAD_PROFILE_IMG, this))
        }
    }

    /*private void checkAllPermission(String[] permission) {
        ArrayList<String> blockedPermission = runTimePermission.checkHasPermission(this, permission);
        DebuggableLogV("blockedPermission", "blockedPermission" + blockedPermission);
        if (blockedPermission != null && !blockedPermission.isEmpty()) {
            boolean isBlocked = runTimePermission.isPermissionBlocked(this, blockedPermission.toArray(new String[blockedPermission.size()]));
            if (isBlocked) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        showEnablePermissionDailog(0, getString(R.string.please_enable_permissions));
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, permission, 300);
            }
        } else {
            // pickProfileImg();
            //checkGpsEnable();

        }
    }*/

    /*private void showEnablePermissionDailog(final int type, String message) {
        if (!customDialog.isVisible()) {
            customDialog = new CustomDialog(message, getString(R.string.ok), new CustomDialog.BtnAllowClick() {
                @Override
                public void clicked() {
                    if (type == 0) callPermissionSettings();
                    else
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 101);
                }
            });
            customDialog.show(getSupportFragmentManager(), "");
        }
    }*/

    /*private void callPermissionSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 300);
    }*/

    fun pickProfileImg() {
        val view = layoutInflater.inflate(R.layout.app_camera_dialog_layout, null)
        val lltCamera = view.findViewById<LinearLayout>(R.id.llt_camera)
        val lltLibrary = view.findViewById<LinearLayout>(R.id.llt_library)
        val lltcancel = view.findViewById<LinearLayout>(R.id.llt_cancel)

        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        lltCamera.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                verifyAccessPermission(
                    arrayOf(
                        RuntimePermissionDialogFragment.CAMERA_PERMISSION,
                        RuntimePermissionDialogFragment.READ_MEDIA_IMAGES_STORAGE_PERMISSION
                    ), RuntimePermissionDialogFragment.cameraAndGallaryCallBackCode, 0
                )
            else verifyAccessPermission(
                arrayOf(
                    RuntimePermissionDialogFragment.CAMERA_PERMISSION,
                    RuntimePermissionDialogFragment.WRITE_EXTERNAL_STORAGE_PERMISSION
                ), RuntimePermissionDialogFragment.cameraAndGallaryCallBackCode, 0
            )

            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                verifyAccessPermission(
                    arrayOf(RuntimePermissionDialogFragment.READ_MEDIA_IMAGES_STORAGE_PERMISSION),
                    RuntimePermissionDialogFragment.externalStoreageCallbackCode,
                    0
                )
            else verifyAccessPermission(
                arrayOf(RuntimePermissionDialogFragment.WRITE_EXTERNAL_STORAGE_PERMISSION),
                RuntimePermissionDialogFragment.externalStoreageCallbackCode,
                0
            )

            bottomSheetDialog.dismiss()
        }

        lltcancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    fun verifyAccessPermission(
        requestPermissionFor: Array<String>,
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        RuntimePermissionDialogFragment.checkPermissionStatus(
            this,
            supportFragmentManager,
            this,
            requestPermissionFor,
            requestCodeForCallbackIdentificationCode,
            requestCodeForCallbackIdentificationCodeSubDivision
        )
    }

    fun pickImageFromCamera() {
        /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = commonMethods.cameraFilePath();
        Uri imageUri = FileProvider.getUriForFile(Profile.this, BuildConfig.APPLICATION_ID + ".provider", imageFile);

        try {
            List<ResolveInfo> resolvedIntentActivities = Profile.this.getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            cameraIntent.putExtra("return-data", true);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, 1);*/
        /* val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
         startActivityForResult(cameraIntent, 1)*/
        //commonMethods.refreshGallery(Profile.this, imageFile);

        imageFile = commonMethods!!.cameraFilePath(this)
        commonMethods.cameraIntent(imageFile!!, this)
    }

    private fun pickImageFromGallary() {
        imageFile = commonMethods.getDefaultFileName(this@Profile)
        commonMethods.galleryIntent(this)
    }

    override fun permissionGranted(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        when (requestCodeForCallbackIdentificationCode) {
            RuntimePermissionDialogFragment.cameraAndGallaryCallBackCode -> pickImageFromCamera()

            RuntimePermissionDialogFragment.externalStoreageCallbackCode -> pickImageFromGallary()

            else -> {
            }
        }
    }

    override fun permissionDenied(
        requestCodeForCallbackIdentificationCode: Int,
        requestCodeForCallbackIdentificationCodeSubDivision: Int
    ) {
        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun onImageCapturedFromCamera(data: Intent) {
        //startCropImage();
        // bm = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        //binding.commonProfile.profileImage1.setImageBitmap(bm);
        /*String myBase64Image;
                    myBase64Image = encodeToBase64(bm, Bitmap.CompressFormat.JPEG, 100);*/
        //imagepath = imageWrite(bm);
        /*imageUri = Uri.fromFile(imageFile);
                    imagepath = imageUri.getPath();*/

        val photo = data.extras?.get("data") as Bitmap
        // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
        val tempUri = getImageUri(applicationContext, photo)
        val realPath = getRealPathFromURI(tempUri)
        imagepath = getRealPathFromURI(tempUri)
        DebuggableLogI("camera image real path", realPath)
        DebuggableLogI("camera image path", imagepath)
        // CALL THIS METHOD TO GET THE ACTUAL PATH
        //File finalFile = new File(getRealPathFromURI(tempUri));


        if (!TextUtils.isEmpty(imagepath)) {
            // commonMethods.showProgressDialog(this);
            ImageCompressAsyncTask(this, imagepath, this).execute()
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val OutImage = Bitmap.createScaledBitmap(inImage, 1000, 1000, true)
        //  val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, OutImage, "Title", null)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            OutImage,
            "IMG_" + Calendar.getInstance().getTime(),
            null
        )
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        if (contentResolver != null) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }


    /**
     * Get gallery data
     */
    private fun onSelectFromGalleryResult(data: Intent?) {

        bm = null
        if (data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(
                selectedImage!!,
                filePathColumn, null, null, null
            )
            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
            val picturePath = cursor?.getString(columnIndex!!)
            cursor?.close()
            bm = BitmapFactory.decodeFile(picturePath)
            binding.commonProfile.profileImage1.setImageBitmap(bm)
            //imagepath = imageWrite(bm);

            imagepath = picturePath.toString()

            if (isInternetAvailable) {
                ImageCompressAsyncTask(this, imagepath, this).execute()
            } else {
                commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
            }

        }

    }

    /**
     * Name edit text listener
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            DebuggableLogV("beforeTextChanged", "beforeTextChanged")
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            //setting save button active when all dates are given
            if (validateFirst() && validateLast() && validateEmail()) {
                binding.savebutton.isEnabled = true
                binding.savebutton.setBackground(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.app_curve_button_yellow
                    )
                )
            } else {
                binding.savebutton.isEnabled = false
                binding.savebutton.setBackground(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.app_curve_button_yellow_disable
                    )
                )
            }
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.input_first -> validateFirst()
                R.id.input_last -> validateLast()
                R.id.emaitext -> validateEmail()

                R.id.mobilenumber -> {
                }

                else -> {

                }
            }// validatePhone();

        }
    }
}
