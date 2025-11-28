@file:Suppress("DEPRECATION")

package com.rideincab.user.taxi.views.signinsignup

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage signin_signup
 * @category SigninSignupActivity
 * @author SMR IT Solutions
 *
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.*
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.custompalette.FontTextView
import com.rideincab.user.common.datamodels.CheckVersionModel
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY
import com.rideincab.user.common.utils.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogE
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.common.utils.Enums
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.utils.userchoice.UserChoice
import com.rideincab.user.common.utils.userchoice.UserChoiceSuccessResponse
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivitySigninSignupBinding
import com.rideincab.user.taxi.datamodels.signinsignup.SigninResult
import com.rideincab.user.taxi.sidebar.currency.CurrencyModel
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.facebookAccountKit.FacebookAccountKitActivity
import com.rideincab.user.taxi.views.main.MainActivity
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleCallback
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleConfiguration
import com.willowtreeapps.signinwithapplebutton.view.BaseUrl
import com.willowtreeapps.signinwithapplebutton.view.SignInWithAppleButton
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
   Sign in and sign up home page
   ************************************************************ */

class SigninSignupActivity : CommonActivity(), ServiceListener, UserChoiceSuccessResponse {
    
    private lateinit var binding: AppActivitySigninSignupBinding
    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var userChoice: UserChoice

    /*  @BindView(R.id.connectsocial) lateinit var connectsocial: TextView*/

   /* @BindView(R.id.rlt_language)
    lateinit var languageLayout: RelativeLayout

    @BindView(R.id.tv_language)
    lateinit var language: TextView

    @BindView(R.id.tv_powered_by)
    lateinit var binding.tvPoweredBy: TextView

    @BindView(R.id.rlt_login)
    lateinit var login: RelativeLayout

    @BindView(R.id.iv_app_logo)
    lateinit var binding.ivAppLogo: ImageView

    @BindView(R.id.v_google)
    lateinit var binding.vGoogle.googleBtn: View
    
    @BindView(R.id.sign_in_with_apple_button_white)
    lateinit var binding.signInWithAppleButtonWhite: SignInWithAppleButton*/

    lateinit var googleCustomView: CustomView

    private lateinit var languageView: RecyclerView
    private lateinit var languagelist: MutableList<CurrencyModel>
    private var isInternetAvailable: Boolean = false
    lateinit var dialog: AlertDialog


    var count = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var fbEmail: String
    lateinit var fbFullName: String
    lateinit var fbID: String
    lateinit var fbImageURL: String
    lateinit var alert: AlertDialog
    lateinit var parameters: Bundle
    private var mIntentInProgress: Boolean = false
    lateinit var loginResult: SigninResult
    lateinit var googleEmail: String
    lateinit var googleFullName: String
    lateinit var googleUserProfile: String
    lateinit var googleID: String

    @Inject
    lateinit var sessionManager: SessionManager
    private var mLastClickTime: Long = 0
    lateinit var checkVersionModel: CheckVersionModel
    
    fun lang() {
        languagelist = ArrayList()
        loadlang()
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        userChoice.getUsersLanguages(this,languagelist, Enums.USER_CHOICE_LANGUAGE,this)
    }

    
    fun login() {

        if (sessionManager.deviceId != null && sessionManager.deviceId != "" && !sessionManager.deviceId.isNullOrEmpty() && sessionManager.deviceId != "null") {
            openLoginActivity()
        } else {
            if (sessionManager.deviceId != null && sessionManager.deviceId != "" && !sessionManager.deviceId.isNullOrEmpty() && sessionManager.deviceId != "null") {

                openLoginActivity()
            }
        }

    }

   
    fun signUp() {
        if (sessionManager.deviceId != null && sessionManager.deviceId != "" && !sessionManager.deviceId.isNullOrEmpty() && sessionManager.deviceId != "null") {
            openFacebookAccountKitActivity()
        } else {
            if (sessionManager.deviceId != null && sessionManager.deviceId != "" && !sessionManager.deviceId.isNullOrEmpty() && sessionManager.deviceId != "null") {

                openFacebookAccountKitActivity()
            }
        }

    }

    
    fun gplogin() {
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
        } else {
            count = 1
            signIn()

        }
    }

    private fun openFacebookAccountKitActivity() {
        FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 0)
    }

    private fun openRegisterActivity(phoneNumber: String, countryCode: String, countryNameCode: String) {
        val signin = Intent(this, SSRegisterActivity::class.java)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, phoneNumber)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, countryCode)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY, countryNameCode)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        finish()
    }


    private fun openLoginActivity() {
        val intent = Intent(this, SSLoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }

    /* @OnClick(R.id.connectsocial)
    fun connect() {

        if (sessionManager.deviceId != null&& sessionManager.deviceId != ""&&!sessionManager.deviceId.isNullOrEmpty()&& sessionManager.deviceId != "null") {
            openConnectSocial()
        } else {
            commonMethods.getFireBaseToken()
            if (sessionManager.deviceId != null&& sessionManager.deviceId != ""&&!sessionManager.deviceId.isNullOrEmpty()&& sessionManager.deviceId != "null") {

                openConnectSocial()
            } else {
                dialogfunction("Unable to get Device Id. Please try again later...")
            }
        }
    }*/

    override fun onStart() {
        super.onStart()
        commonMethods.getFireBaseToken()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivitySigninSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Apply insets to root view (adds top and bottom padding)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        checkVersionModel = Gson().fromJson(sessionManager.checkVersionModel, CheckVersionModel::class.java);
        commonMethods.getFireBaseToken()

        isInternetAvailable = commonMethods.isOnline(applicationContext)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        dialog = commonMethods.getAlertDialog(this)

        initSocialLoginViews()
        socialIsViewOrGone()
        setLocale()

        sessionManager.type = "rider"
        sessionManager.deviceType = "2"

        DebuggableLogE(TAG, "Firebase reg id: " + sessionManager.deviceId)


        val isAttachedToWindow = ViewCompat.isAttachedToWindow(binding.ivAppLogo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.ivAppLogo.visibility = View.VISIBLE
            binding.ivAppLogo.post {
                try {
                    if (isAttachedToWindow) {
                        doCircularReveal()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (resources.getString(R.string.show_copyright).equals("true", true)) {
            binding.tvPoweredBy.setText(Html.fromHtml(getString(R.string.redirect_link)))
            binding.tvPoweredBy.setMovementMethod(LinkMovementMethod.getInstance())
        }else binding.tvPoweredBy.visibility=View.GONE
        
        binding.rltLanguage.setOnClickListener {
            lang()
        }
        binding.login.setOnClickListener {
            login()
        }
        binding.signup.setOnClickListener {
            signUp()
        }
        binding.vGoogle.googleBtn.setOnClickListener {
            gplogin()
        }

    }

    private fun isSocialEnable(): Boolean {
        return !(!checkVersionModel.appleLogin && !checkVersionModel.facebookLogin && !checkVersionModel.googleLogin)
    }

    private fun initSocialLoginViews() {
        googleCustomView = CustomView(binding.vGoogle.googleBtn)
        googleCustomView.ivIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_ic_google))
        googleCustomView.tvTitle.text = resources.getString(R.string.google)
    }

    private fun socialIsViewOrGone() {
        if (checkVersionModel.appleLogin) {
            binding.signInWithAppleButtonWhite.visibility = View.VISIBLE
            //Apple login Initialize
            appleLoginInitialize()
        } else {
            binding.signInWithAppleButtonWhite.visibility = View.GONE
        }


        if (checkVersionModel.googleLogin) {
            binding.vGoogle.googleBtn.visibility = View.VISIBLE
            //GooglePlus Initialize
            googlePlusInitialize()
        } else {
            binding.vGoogle.googleBtn.visibility = View.GONE
        }


    }


    /**
     * Exit revel animation
     */
    private fun doExitReveal() {


        // get the center for the clipping circle
        val centerX = (binding.ivAppLogo.left + binding.ivAppLogo.right) / 2
        val centerY = (binding.ivAppLogo.top + binding.ivAppLogo.bottom) / 2

        // get the initial radius for the clipping circle
        val initialRadius = binding.ivAppLogo.width

        // create the animation (the final radius is zero)
        val anim: Animator?
        anim = ViewAnimationUtils.createCircularReveal(binding.ivAppLogo,
                centerX, centerY, initialRadius.toFloat(), 0f)
        anim.duration = 1000
        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                binding.ivAppLogo.visibility = View.GONE
            }
        })

        // start the animation
        anim.start()

    }

    /**
     * Circular revel animation
     */
    private fun doCircularReveal() {

        // get the center for the clipping circle
        val centerX = (binding.ivAppLogo.left + binding.ivAppLogo.right) / 2
        val centerY = (binding.ivAppLogo.top + binding.ivAppLogo.bottom) / 2

        val startRadius = 0
        // get the final radius for the clipping circle
        val endRadius = Math.max(binding.ivAppLogo.width, binding.ivAppLogo.height)

        // create the animator for this view (the start radius is zero)
        var anim: Animator? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(binding.ivAppLogo,
                    centerX, centerY, startRadius.toFloat(), endRadius.toFloat())
        }
        anim?.duration = 1500

        anim?.start()
    }


    public override fun onResume() {
        super.onResume()

        val lan = sessionManager.language

        binding.tvLanguage.text = lan
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)
    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()       // bye

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.ivAppLogo.visibility = View.VISIBLE
            binding.ivAppLogo.post { doExitReveal() }
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    /**
     * To message in dialog ( internet not available)
     */
    private fun dialogfunction(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.ok_c)) { dialog, _ ->
                    dialog.dismiss()
                    //SigninSignupActivity.this.finish();
                }

        val alert = builder.create()
        alert.show()
    }

    private fun loadlang() {
        val languages = resources.getStringArray(R.array.language)
        val langCode = resources.getStringArray(R.array.languageCode)
        for (i in languages.indices) {
            val listdata = CurrencyModel()
            listdata.currencyName = languages[i]
            listdata.currencySymbol = langCode[i]
            languagelist.add(listdata)
        }
    }

    private fun setLocale() {
        val lang = sessionManager.language

        if (lang != "") {

            val langC = sessionManager.languageCode
            val locale = Locale(langC)
            val res: Resources = resources
            val configuration: Configuration = res.getConfiguration()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)

            } else
                configuration.setLocale(locale)

            createConfigurationContext(configuration)
            this@SigninSignupActivity.resources.updateConfiguration(configuration, this@SigninSignupActivity.resources.displayMetrics)
            DebuggableLogV("locale", "localesetted $locale")
        } else {
            sessionManager.language = "English"
            sessionManager.languageCode = "en"
            setLocale()
            recreate()
            val intent = baseContext.packageManager
                    .getLaunchIntentForPackage(baseContext.packageName)
            intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    fun clearSocialCredintials() {
        sessionManager.facebookId = ""
        sessionManager.appleId = ""
        sessionManager.googleId = ""
        sessionManager.firstName = ""
        sessionManager.lastName = ""
        sessionManager.email = ""
    }

    private fun appleLoginInitialize() {
        //This BaseUrl.baseUrl For Check the URL is AppleCallback in siginwithApplicationmodule
        BaseUrl.appleCallbackUrl = resources.getString(R.string.appleCallback)
        val configuration = SignInWithAppleConfiguration.Builder()
                .clientId(sessionManager.appleLoginClientId!!)
                .redirectUri(resources.getString(R.string.appleCallback))
                .scope(CommonKeys.Apple_Login_Scope)
                .build()

        val callback = object : SignInWithAppleCallback {
            override fun onSuccessOnSignIn(response: String) {

                var json: JSONObject? = null
                try {
                    json = JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                var statuscode: String? = ""
                var statusmessage: String? = ""

                try {
                    statuscode = json?.getString("status_code")
                    statusmessage = json?.getString("status_message")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (!statuscode.equals("0")) {
                    commonMethods.showProgressDialog(this@SigninSignupActivity)
                }
                if (statuscode == "1") {
                    //New User
                    commonMethods.hideProgressDialog()
                    createNewUser(response)
                } else if (statuscode == "2") {
                    // Alread user
                    commonMethods.hideProgressDialog()
                    onSuccessLogin(response)
                } else {
                    //Error or Other Response
                    statusmessage?.let { CommonMethods.Companion.DebuggableLogE("SSSocialActivity:Error Response in Apple Login", it) }
                    //Toast.makeText(this@SSSocialActivity, statusmessage, LENGTH_SHORT).show()
                }

            }

            override fun onSignInWithAppleSuccess(authorizationCode: String) {}

            override fun onSignInWithAppleFailure(error: Throwable) {}

            override fun onSignInWithAppleCancel() {}
        }

        binding.signInWithAppleButtonWhite.setUpSignInWithAppleOnClick(supportFragmentManager, configuration, resources.getString(R.string.sign_apple), callback)
    }


    /**
     * Call  StartActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        commonMethods.hideProgressDialog()

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            mIntentInProgress = false

        }

    }

    fun onSuccessLogin(jsonResp: String) {
        loginResult = gson.fromJson(jsonResp, SigninResult::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sessionManager.currencySymbol = Html.fromHtml(loginResult.currencySymbol, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            sessionManager.currencySymbol = Html.fromHtml(loginResult.currencySymbol).toString()
        }
        sessionManager.currencyCode = loginResult.currencyCode
        sessionManager.accessToken = loginResult.token
        sessionManager.walletAmount = loginResult.walletAmount
        sessionManager.userId = loginResult.userId
        sessionManager.isrequest = false
        commonMethods.hideProgressDialog()
        try {
            val response = JSONObject(jsonResp)
            if (response.has("promo_details")) {
                val promocount = response.getJSONArray("promo_details").length()

                sessionManager.promoDetail = response.getString("promo_details")
                sessionManager.promoCount = promocount
            }
        } catch (j: JSONException) {
            j.printStackTrace()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)

    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.

            getProfileInformation(account!!)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google login", "signInResult:failed code=" + e.statusCode)
        }

    }

    /********************************************************************
     * Google Signin Start
     */

    fun googlePlusInitialize() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .requestIdToken(getString(R.string.google_client_id))
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, SigninSignupActivity.RC_SIGN_IN)
    }

    /**
     * Fetching user's information name, email, profile pic
     */
    private fun getProfileInformation(account: GoogleSignInAccount) {


        commonMethods.hideProgressDialog()
        googleID = account.id!!

        googleFullName = account.displayName!!
        if (account.photoUrl != null)
            googleUserProfile = account.photoUrl.toString()
        else
            googleUserProfile = ""

        googleEmail = account.email!!


        googleUserProfile = googleUserProfile.replace("s96-c", "s400-c")
        val splitStr = googleFullName.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val firstName = splitStr[0]
        var lastName = ""
        for (i in 1 until splitStr.size) {
            lastName = lastName + " " + splitStr[i]
        }
        if (lastName == "") lastName = " "

        sessionManager.email = googleEmail
        sessionManager.facebookId = ""
        sessionManager.appleId = ""
        sessionManager.googleId = googleID
        sessionManager.firstName = firstName
        sessionManager.lastName = lastName
        sessionManager.profilepicture = googleUserProfile + ""

        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
        } else {

            if (count == 1) {
                signOut()
                commonMethods.showProgressDialog(this@SigninSignupActivity)
                apiService.socialoldsignup(commonMethods.getAuthType(), commonMethods.getAuthId()!!, sessionManager.deviceType!!, sessionManager.deviceId!!, sessionManager.languageCode!!).enqueue(RequestCallback(Enums.REQ_SIGNUP, this@SigninSignupActivity))
            }
            count++
        }


    }

    private fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this) { }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //  getMenuInflater().inflate(R.menu.main, menu);
        return true
    }

    fun createNewUser(response: String) {
        var json: JSONObject? = null
        try {
            json = JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var email: String? = ""
        var apple_id: String? = ""
        try {
            email = json?.getString("email_id")
            apple_id = json?.getString("apple_id")
            sessionManager.email = email!!
            sessionManager.appleId = apple_id!!
            sessionManager.facebookId = ""
            sessionManager.googleId = ""
            openFacebookAccountKitActivity()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        var alertDialogStores: android.app.AlertDialog? = null
        private const val TAG = "SigninSignupActivity"
        private val RC_SIGN_IN = 0
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        val statusCode = commonMethods.getJsonValue(jsonResp.strResponse, Constants.STATUS_CODE, String::class.java) as String
        if (jsonResp.isSuccess) {
            /**
             * Status Code 1 -- Existing User
             * Status Code 2 -- For New User
             */
            if (statusCode == "1") {
                onSuccessLogin(jsonResp.strResponse)
            } else if (statusCode == "2") {
                openFacebookAccountKitActivity()
            } else {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            DebuggableLogV("jsonResp.getStatusMsg()", "" + jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg))
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        commonMethods.hideProgressDialog()
    }


    inner class CustomView internal constructor(view: View?) {
         var tvTitle: FontTextView
         var ivIcon: ImageView

        init {
            tvTitle = view!!.findViewById(R.id.tv_title)
            ivIcon = view.findViewById(R.id.iv_icon)
        }
    }

    override fun onSuccessUserSelected(type: String?, userChoiceData: String?, userChoiceCode: String?) {
        if (type.equals(Enums.USER_CHOICE_LANGUAGE)){
            val langocde = sessionManager.languageCode
            val lang = sessionManager.language
            binding.tvLanguage.text = lang
            val lan = sessionManager.language
            binding.tvLanguage.text = lan
            setLocale()
            recreate()
            val intent = baseContext.packageManager
                    .getLaunchIntentForPackage(baseContext.packageName)
            intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            binding.rltLanguage.isClickable = true
        }
    }
}