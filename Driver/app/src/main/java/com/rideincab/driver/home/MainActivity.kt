package com.rideincab.driver.home


/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage -
 * @category MainActivity
 * @author SMR IT Solutions
 *
 */


import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.TileOverlay
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.custompalette.CustomTypefaceSpan
import com.rideincab.driver.common.database.AddFirebaseDatabase
import com.rideincab.driver.common.database.Sqlite
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.Constants.RequestEndTime
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.CommonMethods.Companion.startFirebaseChatListenerService
import com.rideincab.driver.common.util.CommonMethods.Companion.stopFirebaseChatListenerService
import com.rideincab.driver.common.util.Enums
import com.rideincab.driver.common.util.Enums.REQ_DEVICE_STATUS
import com.rideincab.driver.common.util.Enums.REQ_DRIVER_PROFILE
import com.rideincab.driver.common.util.Enums.REQ_DRIVER_STATUS
import com.rideincab.driver.common.util.Enums.REQ_UPDATE_ONLINE
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.SupportActivityCommon
import com.rideincab.driver.google.locationmanager.TrackingService
import com.rideincab.driver.google.locationmanager.TrackingServiceListener
import com.rideincab.driver.google.locationmanager.UpdateLocations
import com.rideincab.driver.home.datamodel.*
import com.rideincab.driver.home.firebaseChat.ActivityChat
import com.rideincab.driver.home.fragments.EarningActivity
import com.rideincab.driver.home.fragments.HomeFragment
import com.rideincab.driver.home.fragments.RatingActivity
import com.rideincab.driver.home.fragments.Referral.ShowReferralOptionsActivity
import com.rideincab.driver.home.fragments.payment.PayToAdminActivity
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.home.managevehicles.DocumentDetails
import com.rideincab.driver.home.managevehicles.ManageVehicles
import com.rideincab.driver.home.managevehicles.SettingActivity
import com.rideincab.driver.home.map.AppUtils
import com.rideincab.driver.home.profile.DriverProfile
import com.rideincab.driver.home.pushnotification.Config
import com.rideincab.driver.home.pushnotification.NotificationUtils
import com.rideincab.driver.trips.RequestReceiveActivity
import com.rideincab.driver.trips.tripsdetails.YourTrips
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.rideincab.driver.common.model.CheckVersionModel
import com.rideincab.driver.common.util.OverlayPermissionChecker
import com.rideincab.driver.common.views.applySystemBarsBottomPadding
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.common.views.applySystemBarsTopPadding
import com.rideincab.driver.databinding.ActivityMainBinding
import com.rideincab.driver.datamodel.DriverProfileStrResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                MainActivity page
Its main page to connected to all the screen pages
*************************************************************** */


class MainActivity : CommonActivity(), ServiceListener {

    lateinit var mainBinding: ActivityMainBinding

    lateinit var selectedFragment: HomeFragment
    lateinit var dialog: AlertDialog
    private var bubblePermissionDialog: Dialog? = null
    private var popUpWindowDialog: Dialog? = null
    lateinit var checkVersionModel: CheckVersionModel
    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dbHelper: Sqlite

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var headerview: View
    lateinit var profilelayout: LinearLayout
    lateinit var ivDriverName: TextView
    lateinit var ivDriverImage: ImageView
    internal var mOverlay2Hr: TileOverlay? = null

    lateinit var driverProfileModel: DriverProfileModel

    internal var bankDetailsModel: BankDetailsModel? = null


    private var companyName: String? = null
    private var company_id: Int = 0

    var menuItem: Menu? = null

    var statusmessage: String = ""
    var width: Int = 0
    lateinit var animation: TranslateAnimation
    var count = 1

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    lateinit var fragment: HomeFragment

    protected var isInternetAvailable: Boolean = false
    private var backPressed = 0
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    private lateinit var mContext: Context


    protected lateinit var locationRequest: LocationRequest
    internal var REQUEST_CHECK_SETTINGS = 100
    private lateinit var GPSCallbackStatus: Status
    private var isTriggeredFromDriverAPIErrorMessage = false

    internal var extraFeeReasons = ArrayList<ExtraFeeReason>()

    private var builder1: AlertDialog.Builder? = null

    lateinit internal var addFirebaseDatabase: AddFirebaseDatabase
    lateinit var trackingServiceListener: TrackingServiceListener

    lateinit var updateLocations: UpdateLocations
    private lateinit var locationAllowView: View
    private lateinit var locationAllowDialog: BottomSheetDialog
    private lateinit var btnAllowLPermission: Button
    private lateinit var btnAllowBGLPermission: Button
    private var PERMISSION_REQUEST_LOCATION = 102

    private val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

    /**
     * Hash map for update motorista status
     */

    val status: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }

    /* Hash Map for motorista location Upadte
    * */

    val location: HashMap<String, String>
        get() {
            val locationHashMap = HashMap<String, String>()
            locationHashMap["latitude"] = sessionManager.latitude!!
            locationHashMap["longitude"] = sessionManager.longitude!!
            locationHashMap["user_type"] = sessionManager.type!!
            locationHashMap["car_id"] = sessionManager.vehicle_id!!
            locationHashMap["status"] = sessionManager.driverStatus!!
            locationHashMap["token"] = sessionManager.accessToken!!

            return locationHashMap
        }

    /**
     * Hash map for update motorista status
     */

    val deviceId: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["device_type"] = sessionManager.deviceType!!
            driverStatusHashMap["device_id"] = sessionManager.deviceId!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }

    fun txtCheckDriverStatus() {
        isInternetAvailable = commonMethods.isOnline(this)
        if (isInternetAvailable) {
            updateDriverStatus()
        } else {
            commonMethods.showMessage(
                applicationContext,
                dialog,
                resources.getString(R.string.no_connection)
            )
        }
    }

    fun GotoRider() {
        val managerclock = packageManager
        var i = managerclock.getLaunchIntentForPackage(resources.getString(R.string.package_rider))

        if (i == null) {
            i = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + resources.getString(R.string.package_rider))
            )
        } else {
            i.addCategory(Intent.CATEGORY_LAUNCHER)

        }
        startActivity(i)
    }

    private var isViewUpdatedWithLocalDB: Boolean = false
    private lateinit var labeledSwitch: LabeledSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mainBinding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(mainBinding.getRoot());
        mainBinding.appBarMain.root.applySystemBarsTopPadding()
//        mainBinding.activityMain.applySystemBarsBottomPadding()
        mainBinding.drawerLayout.applySystemBarsBottomPadding()
        mainBinding.tvAppVersion.applySystemBarsBottomPadding()
        AppController.getAppComponent().inject(this)
        if (!sessionManager.checkVersionModel.isEmpty()) {
            checkVersionModel = Gson().fromJson(sessionManager.checkVersionModel, CheckVersionModel::class.java)
        }
        updateLocations = UpdateLocations(this)
        mContext = this
        builder1 = AlertDialog.Builder(mContext)
        addFirebaseDatabase = AddFirebaseDatabase()
        trackingServiceListener = TrackingServiceListener(this)
        addFirebaseDatabase.firebasePushLisener(this)
        dialog = commonMethods.getAlertDialog(this)
        /*
         *  Common loader initialize and internet connection check
         */
        isInternetAvailable = commonMethods.isOnline(this)

        labeledSwitch = mainBinding.appBarMain.appToolbarHome.labledSwitch

        labeledSwitch.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
                isInternetAvailable = commonMethods.isOnline(mContext)
                if (isOn) {
                    if (isInternetAvailable) {
                        sessionManager.driverStatus = "Online"
                        mainBinding.appBarMain.appToolbarHome.tvStatus.text = getString(R.string.online)
                        mainBinding.appBarMain.appToolbarHome.tvStatus.setTextColor(resources.getColor(R.color.cabme_app_yellow))
                        updateOnlineStatus()
                        updateDriverStatus()
                    } else {
                        commonMethods.showMessage(
                            mContext,
                            dialog,
                            resources.getString(R.string.no_connection)
                        )
                    }
                } else {
                    if (isInternetAvailable) {
                        sessionManager.driverStatus = "Offline"
                        mainBinding.appBarMain.appToolbarHome.tvStatus.text = getString(R.string.offline)
                        mainBinding.appBarMain.appToolbarHome.tvStatus.setTextColor(resources.getColor(R.color.cabme_app_white))
                        updateOnlineStatus()
                    } else {
                        commonMethods.showMessage(
                            mContext,
                            dialog,
                            resources.getString(R.string.no_connection)
                        )
                    }
                }

            }
        })

        if (sessionManager.isLocationUpdatedForOneTime!!) {
            sessionManager.isLocationUpdatedForOneTime = false
            updateOnlineStatus()
        }

        if (!isInternetAvailable) {

            dialogfunction() // Show dialog for internet connection not available
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission()
        }*/

        width = 1

        translateAnimation()



        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = (5 * 1000).toLong()


        //setUpDrawer()
        initNavitgaionview()
        initView()


        if (sessionManager.isDialogShown.equals("")) {
            disclaimerDialog()
        }

        locationAllowView = layoutInflater.inflate(R.layout.location_permission_alert_layout, null)
        locationAllowDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        btnAllowLPermission = locationAllowView.findViewById(R.id.btnAllowLPermission)
        btnAllowBGLPermission = locationAllowView.findViewById(R.id.btnAllowBGLPermission)

        try {
            if (intent!=null){
                if (intent.getBooleanExtra("manualBooking",false)){
                    startActivity(Intent(this, YourTrips::class.java))
                }
            }
        }catch (e:Exception){
            Log.i(TAG, "onCreate: Error=${e.localizedMessage}")
        }

        mainBinding.appBarMain.appToolbarHome.txtCheckdriverstatus.setOnClickListener {
            txtCheckDriverStatus()
        }
        mainBinding.gotorider.setOnClickListener {
            GotoRider()
        }
    }

    private fun requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            showLocationPermissionDialog()
        } else {
            showLocationPermissionDialog()
        }
    }

    private fun showLocationPermissionDialog() {

        if (locationAllowDialog != null && !locationAllowDialog.isShowing) {

            btnAllowLPermission.setOnClickListener {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_REQUEST_LOCATION
                )

            }

            btnAllowBGLPermission.setOnClickListener { requestBackgroundLocationPermission() }

            locationAllowDialog.setContentView(locationAllowView)
            locationAllowDialog.setCancelable(false)
            locationAllowDialog.setCanceledOnTouchOutside(false)
            if (!locationAllowDialog.isShowing)
                locationAllowDialog.show()
        }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            locationAllowDialog.getWindow()!!.getDecorView(),
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.ok) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }


    private fun requestBackgroundLocationPermission() {
        val permissionApproved = hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)


        if (permissionApproved) {
            if (locationAllowDialog != null)
                locationAllowDialog.dismiss()
        } else {
            requestPermissionWithRationale(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar
            )
        }
    }

    /*
        private fun backgroundPermissionDialog() {
            try {

                val view: View = layoutInflater.inflate(R.layout.background_location_dialog, null)
                bgPermissionDialog = Dialog(mContext, R.style.CustomBottomSheetDialogTheme)

                */
    /* Glide.with(this).asGif().load(R.drawable.bg_permission_img)
                     .into(view.findViewById<ImageView>(R.id.imgGIF))*//*


            view.findViewById<Button>(R.id.btnOpenSetting).clickWithThrottle {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )

            }

            bgPermissionDialog?.setContentView(view)
            bgPermissionDialog?.setCancelable(false)
            bgPermissionDialog?.setCanceledOnTouchOutside(false)
            if (!bgPermissionDialog!!.isShowing)
                bgPermissionDialog?.show()

        } catch (e: Exception) {
            Log.i(TAG, "showbgPermissionDialog: Error=${e.localizedMessage}")
        }
    }
*/

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            notificationPermissionDialog()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                when {
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> notificationPermissionDialog()
                    else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

            }
    }

    private fun notificationPermissionDialog() {
        try {
            val dialog = Dialog(this, R.style.DialogCustomTheme)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.disclaimer_dialog)

            val tvDisclaimer = dialog.findViewById<TextView>(R.id.tvDisclaimer)
            val tvAccept = dialog.findViewById<TextView>(R.id.tvAccept)

            tvDisclaimer.setText(getString(R.string.notification_permission_description))
            tvAccept.setText(getString(R.string.settings))

            tvAccept.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
                dialog.dismiss()
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        } catch (e: Exception) {
            Log.i("TAG", "notificationPermissionDialog: Error=${e.localizedMessage}")
        }

    }


    private fun checkPermission() {
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is missing and must be requested.
            requestLocationPermission()
        } else {
            updateBackgroundButtonState()
        }
    }

    private fun showBackgroundButton(): Boolean {
        return !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            btnAllowLPermission.visibility = View.GONE
            btnAllowBGLPermission.visibility = View.VISIBLE
            showLocationPermissionDialog()
        } else {
            if (locationAllowDialog != null)
                locationAllowDialog.dismiss()

            checkNotificationPermission()
        }
    }

    /**
     * Helper functions to simplify permission checks/requests.
     */
    fun hasPermission(permission: String): Boolean {

        // Background permissions didn't exit prior to Q, so it's approved by default.
        if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
        ) {
            return true
        }

        return ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissionWithRationale(
        activity: MainActivity,
        permission: String,
        requestCode: Int,
        snackbar: Snackbar
    ) {
        val provideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)

        if (provideRationale) {
            snackbar.show()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
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
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        } catch (e: Exception) {
            Log.i("TAG", "disclaimerDialog: Error=${e.localizedMessage}")
        }

    }


    private fun initView() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mainBinding.activityMain.visibility = View.VISIBLE
            mainBinding.activityMain.post { this.doCircularReveal() }
        }

        mainBinding.tvAppVersion.text = "V" + CommonMethods.getAppVersionNameFromGradle(this)

        /*
         *  FCM push notification receive function
         */
        receivepushnotification()

        updateDeviceId() // Update FCM device id
        //switch_driverstatus.switchPadding = 40
        //switch_driverstatus.setOnCheckedChangeListener(this)

        if (sessionManager.driverSignupStatus == "pending") {
            //labeledSwitch.visibility = View.GONE
            //switch_driverstatus.visibility = View.GONE

        }
        /*
         * Set motorista status
         */
        if (sessionManager.driverStatus == "Online") {
            labeledSwitch.isOn = true
            //switch_driverstatus.isChecked = true
            sessionManager.driverStatus = "Online"
        } else {
            labeledSwitch.isOn = false
            //switch_driverstatus.isChecked = false
            sessionManager.driverStatus = "Offline"
        }
    }


    fun setOnlineVisbility(visibile: Int) {
        labeledSwitch.visibility = visibile
    }


    fun initNavitgaionview() {

        setUpHomeFragment()

        setSupportActionBar(mainBinding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0F


        // mDrawerToggle?.syncState()


        mDrawerToggle = ActionBarDrawerToggle(
            this,
            mainBinding.drawerLayout,
            mainBinding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        mainBinding.drawerLayout.addDrawerListener(mDrawerToggle!!)
        // mDrawerToggle?.getDrawerArrowDrawable()?.setColor(Color.BLACK)
        mDrawerToggle?.syncState()
        drawerListener()

        setupDrawerContent()

        //binding.navView.menu.getItem(0).isChecked = true

        mainBinding.navView.menu.getItem(0).setChecked(false)

        fragment = HomeFragment.newInstance()


        val m = mainBinding.navView.menu
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)
            //for aapplying a font to subMenu ...
            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi)
        }

    }


    /**
     * Apply font
     */
    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, resources.getString(R.string.fonts_UBERMedium))
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(
            CustomTypefaceSpan("", font),
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        mi.title = mNewTitle
    }


    fun setUpHomeFragment() {
        selectedFragment = HomeFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, selectedFragment)
        transaction.commit()
    }


    private fun setupDrawerContent() {
        mainBinding.navView.setNavigationItemSelectedListener {
            mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            when (it.itemId) {

                /*R.id.choose_vehicle -> {
                    // recreate()
                    // setUpHomeFragment()
                    true
                }*/
                R.id.trips -> {

                    val signin = Intent(this, YourTrips::class.java)
                    startActivity(signin)
                    false
                }

                R.id.earnings -> {

                    val earnings = Intent(this, EarningActivity::class.java)
                    startActivity(earnings)
                    false
                }

                R.id.ratings -> {

                    val rating = Intent(this, RatingActivity::class.java)
                    startActivity(rating)
                    false
                }

                R.id.pay_to_admin -> {
                    val payto = Intent(this, PayToAdminActivity::class.java)
                    startActivity(payto)
                    false
                }

                R.id.nav_referral -> {

                    val intent = Intent(this, ShowReferralOptionsActivity::class.java)
                    startActivity(intent)
                    false
                }

                R.id.manage_vehicles -> {

                    val intent = Intent(this, ManageVehicles::class.java)
                    intent.putExtra(
                        CommonKeys.Intents.DocumentDetailsIntent,
                        driverProfileModel.driverDocuments
                    )
                    intent.putExtra(
                        CommonKeys.Intents.VehicleDetailsIntent,
                        driverProfileModel.vehicle
                    )
                    intent.putExtra("New", false)
                    startActivity(intent)
                    false
                }

                R.id.manage_documents -> {

                    val intent = Intent(this, DocumentDetails::class.java)
                    intent.putExtra(
                        CommonKeys.Intents.DocumentDetailsIntent,
                        driverProfileModel.driverDocuments
                    )
                    startActivity(intent)
                    false
                }

                R.id.nav_setting -> {

                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                    false
                }

                R.id.nav_support -> {
                    val intent = Intent(applicationContext, SupportActivityCommon::class.java)
                    startActivity(intent)
                    false
                }
                R.id.nav_powered_by -> {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.setData(Uri.parse("https://api.whatsapp.com/send?l=en&text=Hi!%20I%27m%20interested%20in%20your%20%20RideIn%20Taxi%20App%20Installation&phone=918607248802\\"))
                    startActivity(i)
                    false
                }
                R.id.nav_privacy_policy -> {
                    val url = resources.getString(R.string.privacy_policy)
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                    false
                }

                else -> false
            }

        }
    }


    private fun askPermission() {
        try {
            if (popUpWindowDialog == null) {
                val view: View = layoutInflater.inflate(R.layout.pop_up_permission_layout, null)
                popUpWindowDialog = Dialog(this, R.style.CustomBottomSheetDialogTheme)

                Glide.with(this).asGif().load(R.drawable.allow_pop_up_window)
                    .into(view.findViewById<ImageView>(R.id.imgGIF))

                view.findViewById<Button>(R.id.btnOpenSetting).setOnClickListener {

                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
                }

                popUpWindowDialog?.setContentView(view)
                popUpWindowDialog?.setCancelable(false)
                popUpWindowDialog?.setCanceledOnTouchOutside(false)

                if (!popUpWindowDialog!!.isShowing)
                    popUpWindowDialog!!.show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "pop_up_window_dialog: Error=${e.localizedMessage}")
        }


    }



    private fun drawerListener() {
        mainBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                mainBinding.appBarMain.toolbar.visibility = View.VISIBLE
            }

            override fun onDrawerOpened(drawerView: View) {
                mainBinding.appBarMain.toolbar.visibility = View.VISIBLE

            }

            override fun onDrawerClosed(drawerView: View) {
                mainBinding.appBarMain.toolbar.visibility = View.VISIBLE
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
    }


    /**
     * To do translate animation
     */


    fun translateAnimation() {
        animation = TranslateAnimation(
            5.0f, (width - 50).toFloat(),
            0.0f, 0.0f
        )          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.duration = 1000  // animation duration
        animation.repeatCount = 1000  // animation repeat count
        animation.repeatMode = 2   // repeat animation (left to right, right to left )
        animation.fillAfter = true
        animation.isFillEnabled = true
        animation.repeatMode = ValueAnimator.REVERSE
        animation.interpolator = AccelerateInterpolator(2f)

        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(arg0: Animation) {

                animation = TranslateAnimation(
                    5.0f, (width - 150).toFloat(),
                    0.0f, 0.0f
                )
                animation.duration = 1000  // animation duration
                animation.repeatCount = 1000  // animation repeat count
                animation.repeatMode = 2   // repeat animation (left to right, right to left )
                animation.fillAfter = true
                animation.isFillEnabled = true

                val lparams = RelativeLayout.LayoutParams(50, 10)
                mainBinding.appBarMain.appToolbarHome.ivLine.layoutParams = lparams
            }

            override fun onAnimationRepeat(arg0: Animation) {

            }

            override fun onAnimationEnd(arg0: Animation) {
                animation.duration = 1000  // animation duration
                animation.repeatCount = 1000  // animation repeat count
                animation.repeatMode = 2   // repeat animation (left to right, right to left )
                animation.fillAfter = true
                animation.isFillEnabled = true
                val lparams = RelativeLayout.LayoutParams(10, 10)
                mainBinding.appBarMain.appToolbarHome.ivLine.layoutParams = lparams

            }
        })
        //animation.setFillAfter(true);

        mainBinding.appBarMain.appToolbarHome.ivLine.startAnimation(animation)
    }

    /**
     * Driver Datas
     */
    fun getDriverProfile() {
        val allHomeDataCursor: Cursor =
            dbHelper.getDocument(Constants.DB_KEY_DRIVER_PROFILE.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
            try {
                onSuccessDriverProfile(allHomeDataCursor.getString(0))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    private fun driverProfile() {
        lifecycleScope.launch {
            delay(2000)
            withContext(Dispatchers.IO) {
                // Perform heavy operation here
                if (commonMethods.isOnline(this@MainActivity)) {
                    apiService.getDriverProfile(sessionManager.accessToken!!)
                        .enqueue(RequestCallback(REQ_DRIVER_PROFILE, this@MainActivity))
                } else {
                    CommonMethods.showInternetNotAvailableForStoredDataViewer(this@MainActivity)
                }
            }
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            commonMethods.showProgressDialog(this)
            driverProfile()
        } else {
            CommonMethods.showNoInternetAlert(
                this,
                object : CommonMethods.INoInternetCustomAlertCallback {
                    override fun onOkayClicked() {
                        finish()
                    }

                    override fun onRetryClicked() {
                        followProcedureForNoDataPresentInDB()
                    }

                })
        }
    }


    fun updateDriverStatus() {
        lifecycleScope.launch {
            delay(3000)
            withContext(Dispatchers.IO) {
                commonMethods.showProgressDialog(this@MainActivity)
                apiService.updateCheckStatus(status).enqueue(RequestCallback(REQ_DRIVER_STATUS, this@MainActivity))
            }
        }
    }

    /*
     *  FCM push nofication received funcation called
     */
    fun receivepushnotification() {

        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (intent.action == Config.REGISTRATION_COMPLETE) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)


                } else if (intent.action == Config.PUSH_NOTIFICATION) {
                    // new push notification is received

                    val JSON_DATA = sessionManager.pushJson
                    val json = JSONObject(sessionManager.pushJson)

                    if (json.getJSONObject("custom").has("chat_notification")) {


                        println("Chat json : ")
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
                        val notificationIntent =
                            Intent(applicationContext, ActivityChat::class.java)
                        notificationIntent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        notificationIntent.putExtra(
                            CommonKeys.FIREBASE_CHAT_FROM_PUSH,
                            json.toString()
                        )

                        sessionManager.chatJson = json.toString()
                        val notificationUtils = NotificationUtils(applicationContext)
                        val message =
                            json.getJSONObject("custom").getJSONObject("chat_notification")
                                .getString("message_data")
                        val title = json.getJSONObject("custom").getJSONObject("chat_notification")
                            .getString("user_name")
                        println("ChatNotification : Driver" + message)
                        notificationUtils.showNotificationMessage(
                            title,
                            message,
                            timeStamp,
                            notificationIntent,
                            null,
                            0L
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            notificationUtils.playNotificationSound()
                        }


                    } else if (count == 1) {
                        val jsonObject: JSONObject
                        try {
                            jsonObject = JSONObject(JSON_DATA)
                            if (jsonObject.getJSONObject("custom").has("cancel_trip")) {
                                statusDialog(
                                    resources.getString(R.string.yourtripcancelledbydriver),
                                    1
                                )
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }


                } else if (intent.action == Config.UPDATE_UI) {
                    if (intent.getBooleanExtra("isInActive", false)) {
                        HomeFragment.newInstance().updateActInActStatus(true)
                    } else {
                        HomeFragment.newInstance().updateActInActStatus(false)
                    }

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    Log.i(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    if (locationAllowDialog != null)
                        locationAllowDialog.dismiss()


                else -> {
                    Snackbar.make(
                        locationAllowDialog.getWindow()!!.getDecorView(),
                        R.string.background_permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                packageName,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }

            PERMISSION_REQUEST_LOCATION -> {

                // Request for camera permission.
                Log.i(TAG, "onRequestPermissionsResult: grantResults.size=" + grantResults.size)
                if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted. Start camera preview Activity.
                    /* locationAllowDialog.getWindow()!!.getDecorView().showSnackbar(
                        R.string.location_permission_granted,
                        Snackbar.LENGTH_SHORT
                    )*/



                    updateBackgroundButtonState()

                } else {
                    // Permission request was denied.

                    Snackbar.make(
                        locationAllowDialog.getWindow()!!.getDecorView(),
                        R.string.fine_permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                packageName,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                //GPS Enabled
            } else {
                //GPS not enabled
                //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //            startActivity(intent);
                showGPSNotEnabledWarning()
            }

        }else if (requestCode==SYSTEM_ALERT_WINDOW_PERMISSION){
            try {

                if (popUpWindowDialog != null)
                    popUpWindowDialog!!.dismiss()

                if ("xiaomi" == Build.MANUFACTURER.lowercase(Locale.ROOT))
                    showBubblePermissionDialog()

            } catch (e: Exception) {
                Log.e(TAG, "onActivityResult: Error=${e.localizedMessage}")
            }

        }else if(requestCode==SYSTEM_OPEN_APP_PERMISSION){

            if (resultCode == Activity.RESULT_OK) {
                if (bubblePermissionDialog != null) {
                    bubblePermissionDialog?.dismiss()
                }
            }

        }
    }

    private fun showBubblePermissionDialog() {
        try {

            val view: View = layoutInflater.inflate(R.layout.bubble_permission_layout, null)
            bubblePermissionDialog = Dialog(this, R.style.CustomBottomSheetDialogTheme)

            Glide.with(this).asGif().load(R.drawable.bubble_permission)
                .into(view.findViewById<ImageView>(R.id.imgGIF))

            view.findViewById<Button>(R.id.btnOpenSetting).setOnClickListener {

                if ("xiaomi" == Build.MANUFACTURER.lowercase(Locale.ROOT)) {
                    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                    intent.setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    intent.putExtra("extra_pkgname", getPackageName())
                    startActivityForResult(intent, SYSTEM_OPEN_APP_PERMISSION)
                } else {
                    startActivityForResult(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        ), SYSTEM_OPEN_APP_PERMISSION
                    )
                }


                lifecycleScope.launch {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        if (bubblePermissionDialog != null) {
                            bubblePermissionDialog?.dismiss()
                        }
                    }
                }

            }

            bubblePermissionDialog?.setContentView(view)
            bubblePermissionDialog?.setCancelable(false)
            bubblePermissionDialog?.setCanceledOnTouchOutside(false)

            if (!bubblePermissionDialog!!.isShowing)
                bubblePermissionDialog!!.show()

            /* if (!isAudioPlayingPreference(applicationContext, AppContect.IS_PLAYING_AUDIO)!!)
                 AudioPlayer.playAudio(applicationContext, R.raw.setting_accept_alert)*/

        } catch (e: Exception) {
            Log.e(TAG, "showBubblePermissionDialog: Error=${e.localizedMessage}")
        }
    }


    private fun showGPSNotEnabledWarning() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.location_not_enabled_please_enable_location))
        builder.setCancelable(true)
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialogInterface, _ ->
            try {
                GPSCallbackStatus.startResolutionForResult(
                    mContext as Activity,
                    REQUEST_CHECK_SETTINGS
                )
            } catch (e: IntentSender.SendIntentException) {
                AppUtils.openLocationEnableScreen(mContext)
                e.printStackTrace()
            }
        }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        dialog.show()

    }


    /*
     *  Animate home page
     */
    private fun doCircularReveal() {

        // get the center for the clipping circle
        val centerX = (mainBinding.activityMain.left + mainBinding.activityMain.right) / 2
        val centerY = (mainBinding.activityMain.top + mainBinding.activityMain.bottom) / 2

        val startRadius = 0
        // get the final radius for the clipping circle
        val endRadius = Math.max(mainBinding.activityMain.width, mainBinding.activityMain.height)

        // create the animator for this view (the start radius is zero)
        var anim: Animator? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(
                mainBinding.activityMain,
                centerX, centerY, startRadius.toFloat(), endRadius.toFloat()
            )
        }
        anim!!.duration = 1500
        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                // splash_logo.setBackgroundColor(getResources().getColor(R.color.colorblack));
            }

        })


        anim.start()
    }


    override fun onStop() {
        super.onStop()
        mRegistrationBroadcastReceiver?.debugUnregister
    }

    override fun onDestroy() {
        CommonKeys.IS_ALREADY_IN_TRIP = false
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (backPressed >= 1) {
            // startActivity(new Intent(this, MainActivity.class));
            CommonKeys.IS_ALREADY_IN_TRIP = false
            finishAffinity()
            super.onBackPressed()


        } else {
            // clean up
            backPressed = backPressed + 1
            Toast.makeText(
                this, resources.getString(R.string.pressbackagain),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    public override fun onResume() {
        super.onResume()
        sessionManager!!.bubbleClosedViaUser = false
        if (CommonKeys.isRideRequest) {
            CommonKeys.isRideRequest = false
            if (RequestEndTime.isNotEmpty() && commonMethods.checkTimings(
                    commonMethods.getCurrentTime(),
                    commonMethods.getTimeFromLong(RequestEndTime.toLong())
                )
            ) {
                RequestEndTime = ""
                val requstreceivepage =
                    Intent(applicationContext, RequestReceiveActivity::class.java)
                requstreceivepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                requstreceivepage.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(requstreceivepage)
            }
        }

        count = 1
        // register FCM registration complete receiver
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it,
                IntentFilter(Config.REGISTRATION_COMPLETE)
            )
        }
        commonMethods.hideProgressDialog()

        getDriverProfile()

        checkPermission()

        if (OverlayPermissionChecker.isOverlayPermissionAvailable(this)) {
            if (!Settings.canDrawOverlays(this)) {
                askPermission()
            }
        }

        mRegistrationBroadcastReceiver?.let {

            println("RequestReceiveActivity Triggered : four ")
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it,
                IntentFilter(Config.PUSH_NOTIFICATION)
            )
        }

        //update ui
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it,
                IntentFilter(Config.UPDATE_UI)
            )
        }


        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)

        // code to initiate firebase chat service

        if (!TextUtils.isEmpty(sessionManager.tripId) && sessionManager.isDriverAndRiderAbleToChat) {
            startFirebaseChatListenerService(this)
        } else {
            stopFirebaseChatListenerService(this)
        }


    }

    public override fun onPause() {
        super.onPause()
    }


    fun onSuccessUpdateDriverStatus(jsonResp: JsonResponse) {
        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, DriverStatus::class.java)
        if (signInUpResultModel != null) {
            val driver_status = signInUpResultModel.driverStatus
            sessionManager.driverSignupStatus = driver_status
            if (driver_status=="0") {
                commonMethods.showMessage(this, dialog, resources.getString(R.string.waiting))
            }
        }
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        Log.i(TAG, "onSuccess: MainActivity requestCode=${jsonResp.requestCode}")
        Log.i(TAG, "onSuccess: MainActivity Data=${Gson().toJson(jsonResp)}")
        Log.i(TAG, "onSuccess: MainActivity 8607248802")
        Log.i(TAG, "====================================================================")

        when (jsonResp.requestCode) {


            REQ_DRIVER_PROFILE -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    Log.i(TAG, "onSuccess:Driver Profile Data=${Gson().toJson(jsonResp)}")
                    Log.i(TAG, "onSuccess:Driver Profile Data=${jsonResp.strResponse}")
                    dbHelper.insertWithUpdate(
                        Constants.DB_KEY_DRIVER_PROFILE.toString(),
                        jsonResp.strResponse
                    )
                    onSuccessDriverProfile(jsonResp.strResponse)
                    getVehicleType(jsonResp.strResponse)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }

            REQ_DRIVER_STATUS -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    onSuccessUpdateDriverStatus(jsonResp)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }

            REQ_DEVICE_STATUS -> {
                if (!jsonResp.isSuccess) {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }

            REQ_UPDATE_ONLINE -> {
                val statuscode = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "status_code",
                    String::class.java
                ) as String
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    onSuccessUpdateOnline()
                } else if (statuscode.equals("2", true)) {
                    dialogfunction2(jsonResp.statusMsg)
                    isTriggeredFromDriverAPIErrorMessage = true
                    mainBinding.appBarMain.appToolbarHome.tvStatus.text = resources.getString(R.string.online)
                    sessionManager.driverStatus = "Online"
                    labeledSwitch.isOn = true
                    if (!commonMethods.isMyServiceRunning(TrackingService::class.java, this)) {
                        trackingServiceListener.startTrackingService(true, true)
                    }
                }
            }
        }
    }

    private fun getVehicleType(strResponse: String) {
        try {
            val data = Gson().fromJson(strResponse, DriverProfileStrResponse::class.java)
            sessionManager.vehicleTypeId = "${data.vehicleDetails!!.get(0).vehicleTypeId}"
//            Log.i(TAG, "getVehicleType: Vehicle Type 1=${data.vehicleDetails!!.get(0).vehicleTypeId}")
//            Log.i(TAG, "getVehicleType: Vehicle Type 2=${data.vehicleDetails!!.get(0).vehicleTypes!!.get(0).id}")
//            Log.i(TAG, "getVehicleType: Saved Vehicle Type=${sessionManager.vehicleTypeId}"
        } catch (e: Exception) {
            Log.e(TAG, "getVehicleType: Error=${e.localizedMessage}")
            sessionManager.vehicleTypeId = "1"
        }
    }

    private fun onSuccessUpdateOnline() {
        if (sessionManager.driverStatus == "Offline") {
            sessionManager.isGeoFireUpdatedWhenOnline = false
            addFirebaseDatabase.removeDriverFromGeofire(this)
            trackingServiceListener.stopTrackingService()
        } else {
            if (!sessionManager.isGeoFireUpdatedWhenOnline!!) {
                val targetLocation = Location("") //provider name is unnecessary
                if (!(sessionManager.latitude.isNullOrEmpty() || sessionManager.longitude.isNullOrEmpty())) {
                    targetLocation.latitude = sessionManager.latitude!!.toDouble()
                    targetLocation.longitude = sessionManager.longitude!!.toDouble()
                    sessionManager.isGeoFireUpdatedWhenOnline = true
                    println("GEO FIRE UPDATED ")
                    updateLocations.updateDriverLocationInGeoFire(targetLocation, 16.0, this)
                }
            }
            if (!commonMethods.isMyServiceRunning(TrackingService::class.java, this)) {
                trackingServiceListener.startTrackingService(true, true)
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }


    private fun onSuccessDriverProfile(jsonResponse: String) {
        driverProfileModel = gson.fromJson(jsonResponse, DriverProfileModel::class.java)
        sessionManager.profileDetail = jsonResponse
        loadData(driverProfileModel)
        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            driverProfile()
        }

    }

    /*
    *  Load Driver profile details
    **/
    fun loadData(driverProfileModel: DriverProfileModel) {

        val first_name = driverProfileModel.firstName
        val last_name = driverProfileModel.lastName
        val user_thumb_image = driverProfileModel.profileImage
        sessionManager.firstName = first_name
        sessionManager.phoneNumber = driverProfileModel.mobileNumber
        company_id = driverProfileModel.companyId
        companyName = driverProfileModel.companyName
        bankDetailsModel = driverProfileModel.bank_detail

        sessionManager.oweAmount = driverProfileModel.oweAmount
        sessionManager.driverReferral = driverProfileModel.driverReferralEarning

        sessionManager.countryCode = driverProfileModel.countryCode


        driverProfileModel.status?.let { selectedFragment.updateDocumentstatus(it) }
        selectedFragment.vehicleTypeModelList.clear()
        selectedFragment.vehicleTypeModelList.addAll(driverProfileModel.vehicle)
        val defaultVehiclePosition = getDefaultPosition()
        if (defaultVehiclePosition != null) {
            selectedFragment.updateUI(defaultVehiclePosition)
        }

        getDocumentsDetails()

        getVehicleDetails()

        headerview = mainBinding.navView.getHeaderView(0)
        ivDriverName = headerview.findViewById<View>(R.id.ivDriverName) as TextView
        ivDriverImage = headerview.findViewById<View>(R.id.ivDriverImage) as ImageView

        ivDriverName.text = "$first_name $last_name"
        Picasso.get().load(user_thumb_image).into(ivDriverImage)

        menuItem = mainBinding.navView.menu


        try {
            if (resources.getString(R.string.show_copyright).equals("true", true)) {
                mainBinding.navView.menu.findItem(R.id.nav_powered_by).setTitle(Html.fromHtml(getString(R.string.menu_redirect_link)))
            }else mainBinding.navView.menu.findItem(R.id.nav_powered_by).setVisible(false)

            mainBinding.navView.menu.findItem(R.id.nav_support).isVisible =
                checkVersionModel.support.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }

        /*if (company_id > 1) {
            menuItem?.findItem(R.id.pay_to_admin)?.isVisible = false
            menuItem?.findItem(R.id.nav_referral)?.isVisible = false
        } else {

            menuItem?.findItem(R.id.pay_to_admin)?.isVisible = true
            menuItem?.findItem(R.id.nav_referral)?.isVisible = true
        }*/

        headerViewset()
        driverPayoutPayed()
    }

    private fun driverPayoutPayed(){
        try {
            lifecycleScope.launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    Log.i(TAG, "loadData: makeDriverOffline called")
                    val oweAmount=(sessionManager.oweAmount)!!.toDouble()
                    val payoutLimit=(sessionManager.payoutLimit)!!.toDouble()

                    if (oweAmount>0&&payoutLimit>0&&(oweAmount>=payoutLimit)) {
                        labeledSwitch.isOn = false
                        sessionManager.driverStatus = "Offline"
                        labeledSwitch.isClickable=false
                        labeledSwitch.isActivated=false
                        labeledSwitch.visibility=View.GONE
                        sessionManager.isGeoFireUpdatedWhenOnline = false
                        addFirebaseDatabase.removeDriverFromGeofire(this@MainActivity)
                        trackingServiceListener.stopTrackingService()
                        commonMethods.showMessage(this@MainActivity, dialog, getString(R.string.payout_alert))
                    }
                }
            }

        }catch (e:Exception){
            Log.i(TAG, "loadData: makeDriverOffline Error=${e.localizedMessage}")
        }
    }

    private fun getDocumentsDetails() {
        if (driverProfileModel.driverDocuments.size > 0) {
            val driverDocumentSize = driverProfileModel.driverDocuments.size
            var driverdocumentlist = ArrayList<DocumentsModel>()
            driverdocumentlist.addAll(driverProfileModel.driverDocuments)
            for (i in driverdocumentlist.indices) {
                println("Document URL ${driverdocumentlist.get(i).documentUrl}")
                if (driverdocumentlist.get(i).documentUrl != null && !driverdocumentlist.get(i).documentUrl.equals(
                        ""
                    )
                ) {
                    selectedFragment.binding.tvAddDriverProoof.text =
                        resources.getString(R.string.manage_driver_document)

                } else {
                    selectedFragment.binding.tvAddDriverProoof.text =
                        resources.getString(R.string.add_driver_proof)
                    return
                }
            }
        } else {
            selectedFragment.binding.tvAddDriverProoof.text = resources.getString(R.string.add_driver_proof)
        }

    }

    private fun getVehicleDetails() {

        if (driverProfileModel.vehicle.size > 0) {
            val vehicleDocumentSize = driverProfileModel.vehicle.size
            for (i in selectedFragment.vehicleTypeModelList.indices) {
                var vehicledocumentlist = ArrayList<DocumentsModel>()
                var isDocumentuploaded: Boolean = false
                vehicledocumentlist.addAll(selectedFragment.vehicleTypeModelList.get(i).document)
                for (j in vehicledocumentlist.indices) {
                    isDocumentuploaded = false
                    if (vehicledocumentlist.get(j).documentUrl != null && !vehicledocumentlist.get(j).documentUrl.equals(
                            ""
                        )
                    ) {
                        selectedFragment.binding.tvAddVehicle.text = resources.getQuantityText(
                            R.plurals.manage_vehicles,
                            vehicleDocumentSize
                        )
                        selectedFragment.newVehicle = false
                        isDocumentuploaded = true
                        return
                    }

                }

                if (!isDocumentuploaded) {
                    selectedFragment.binding.tvAddVehicle.text = resources.getString(R.string.add_vehicle)
                    selectedFragment.newVehicle = false
                }
            }

        } else {
            selectedFragment.binding.tvAddVehicle.text = resources.getString(R.string.add_vehicle)
            selectedFragment.newVehicle = true
        }

    }

    private fun getDefaultPosition(): Int? {


        var defaultPosition: Int? = null
        for (j in selectedFragment.vehicleTypeModelList.indices) {

            if (selectedFragment.vehicleTypeModelList.get(j).isDefault.equals("1"))
                defaultPosition = j

        }

        return defaultPosition

    }


    fun headerViewset() {

        /**
         * After trip motorista profile details page
         */
        profilelayout = headerview.findViewById<View>(R.id.profilelayout) as LinearLayout
        profilelayout.setOnClickListener {
            val intent = Intent(this, DriverProfile::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
            mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }

    }


    /*
         *   Internet not available dialog
         */
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.turnoninternet))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                builder.setCancelable(
                    true
                )
            }

        val alert = builder.create()
        alert.show()
    }


    /*
     *  Dialog for arrive now , begin trip, end trip, payment completed
     */
    fun statusDialog(message: String, show: Int) {


        if (!this.isFinishing) {
            val alert11 = builder1!!.create()
            builder1!!.setMessage(message)
            builder1!!.setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }

            if (!alert11.isShowing) {
                try {
                    alert11.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }


    }


    /*
     *  Dialog for motorista status (Active or pending)
     */
    fun dialogfunction2(statusmessage: String) {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(statusmessage)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                //switch_driverstatus.setChecked(false);
            }

        val alert = builder.create()
        if (!this.isFinishing) {
            alert.show()
        }

    }

    fun updateOnlineStatus() {
        lifecycleScope.launch {
            delay(2000)
            withContext(Dispatchers.IO) {
                println("UPDATE FROM HOME")
                apiService.updateLocation(location).enqueue(RequestCallback(Enums.REQ_UPDATE_ONLINE, this@MainActivity))
            }
        }

    }

    /*
     *  Update motorista device id API call
     */

    fun updateDeviceId() {
        lifecycleScope.launch {
            delay(2000)
            withContext(Dispatchers.IO) {
                apiService.updateDevice(deviceId).enqueue(RequestCallback(REQ_DEVICE_STATUS, this@MainActivity))
            }
        }
    }


    companion object {
        private val SYSTEM_ALERT_WINDOW_PERMISSION = 2084
        private val SYSTEM_OPEN_APP_PERMISSION = 2086


        private val TAG = "MAP LOCATION"
        var selectedFrag = 0
    }

    /*
*  Check motorista status is online or offline
*/
    /*override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.switch_driverstatus -> {
                CommonMethods.DebuggableLogI("switch_compat", isChecked.toString() + "")

            }
        }
    }*/


}
