package com.rideincab.user.taxi.sendrequest

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage sendrequest
 * @category AcceptedTripId Model
 * @author SMR IT Solutions
 *
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.CircularMusicProgressBar
import com.rideincab.user.common.helper.WaveDrawable
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.pushnotification.Config
import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.common.utils.Enums.REQ_GET_DRIVER
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsBottomPadding
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivitySendingRequestBinding
import com.rideincab.user.taxi.RoundedLayout
import com.rideincab.user.taxi.database.AddFirebaseDatabase
import com.rideincab.user.taxi.database.IFirebaseReqListener
import com.rideincab.user.taxi.datamodels.trip.TripDetailsModel
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import io.github.krtkush.lineartimer.LinearTimer
import io.github.krtkush.lineartimer.LinearTimerView
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
    Sending request to nearest selected type drivers
    *************************************************************** */
class SendingRequestActivity : CommonActivity(), LinearTimer.TimerListener, ServiceListener, IFirebaseReqListener, OnMapReadyCallback {

    private lateinit var binding:AppActivitySendingRequestBinding

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

   /* @BindView(R.id.binding.carnameRequest)
    lateinit var binding.carnameRequest: TextView

    @BindView(R.id.album_art)
    lateinit var binding.albumArt: CircularMusicProgressBar

    @BindView(R.id.maplayout)
    lateinit var binding.maplayout: RoundedLayout*/

    lateinit var mMap: GoogleMap

    lateinit var mapFragment: SupportMapFragment

    var polylineOptions: PolylineOptions = PolylineOptions()
    var polylinepoints = ArrayList<LatLng>()

    lateinit var waveDrawable: WaveDrawable
    var locationHashMap: HashMap<String, String>? = HashMap()
    lateinit var request_receive_dialog_layout: RelativeLayout
    lateinit var linearTimerView: LinearTimerView
    lateinit var linearTimer: LinearTimer

    //lateinit var map_snap: ImageView
    lateinit var JSON_DATA: String
    private var overviewPolylines: String? = null
    var carname: String? = null
    protected var isInternetAvailable: Boolean = false
    private var duration = (120 * 1000).toLong()
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null

    private val addFirebaseDatabase = AddFirebaseDatabase()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    /*@BindView(R.id.iv_request_loader)
    lateinit var ivRequestLoader: ImageView*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivitySendingRequestBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)
        binding.root.applySystemBarsBottomPadding()
        AppController.appComponent.inject(this)


        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        circulatLayoutFile()
        dialog = commonMethods.getAlertDialog(this)
        carname = intent.getStringExtra("carname")
        DebuggableLogV("carname1", "carname=$carname")
        try {
            if (carname != null) {
                sessionManager.carName = carname as String
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        locationHashMap = intent.getSerializableExtra("hashMap") as HashMap<String, String>?
        // polylineOptions = intent.getSerializableExtra("polylineOptions") as PolylineOptions
        overviewPolylines = locationHashMap!!.get("polyline")
        if (!overviewPolylines.isNullOrEmpty()) {
            overviewPolylines = locationHashMap!!.get("polyline")
            polylinepoints = PolyUtil.decode(overviewPolylines) as ArrayList<LatLng>
            polylineOptions.addAll(polylinepoints)
            polylineOptions.width(8f)
            polylineOptions.color(ContextCompat.getColor(this, R.color.app_primary_color))
            polylineOptions.geodesic(true)
        }
        /**
         * Get total car count from main activity
         */
        if (intent.getIntExtra("totalcar", 0) != 0) {
            duration = (intent.getIntExtra("totalcar", 0) * 60000).toLong()

        }

        isInternetAvailable = commonMethods.isOnline(applicationContext)

        /**
         * Load reqeuest details
         */
        if (intent.getStringExtra("loadData") == "loadData") {
            loadJSONDATA()
        } else {
            circularProgressfunction()
        }

        /**
         * Create a req in Firebase DB
         */
        addFirebaseDatabase.createRequestTable(this, this)
        //Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.app_request_loader)).into(GlideDrawable(ivRequestLoader,200))
        //Glide.with(this).asGif().load(R.drawable.app_request_loader).into(ivRequestLoader)
        /*Glide.with(this).asGif()
                .load(R.drawable.app_request_loader)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(ivRequestLoader)*/


        /**
         * Push notification received
         */

        requestReceive()



        binding.carnameRequest.text = (resources.getString(R.string.request_car_msg)
                + " " + sessionManager.carName
                + "" + resources.getString(R.string.request_car_msg1))
        // set progress to 40%
        binding.albumArt.setValue(100f)

        /* binding.albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    linearTimer.pauseTimer();
                    //linearTimer.resetTimer();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                //waveDrawable.stopAnimation();


            }
        });*/
    }

    private fun circulatLayoutFile() {
        binding.maplayout.setShapeCircle(true)
        binding.maplayout.showBorder(true)
        binding.maplayout.setBorderWidth(2)
    }


    override fun onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Waiting for driver response to show the circular image
     */
    fun circularProgressfunction() {
        val display = windowManager.defaultDisplay
        val width = display.width
        val height = display.height
        var radius: Int
        if (width < height)
            radius = (width / 2.2).toInt()
        else
            radius = (height / 2.2).toInt()
        print("radius: $radius")
        //radius-= 4;
        radius = (radius / resources.displayMetrics.density).toInt()
        print("Height: $height Width: $width")

        waveDrawable = WaveDrawable(ContextCompat.getColor(this, R.color.sending_request_blue), width - 250)

        request_receive_dialog_layout = findViewById<View>(R.id.request_receive_dialog_layout) as RelativeLayout
        linearTimerView = findViewById<View>(R.id.linearTimer) as LinearTimerView
        //map_snap = findViewById<View>(R.id.map_snap) as ImageView


        val vto = linearTimerView.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                linearTimerView.viewTreeObserver.removeOnPreDrawListener(this)
                val finalHeight1 = linearTimerView.measuredHeight
                val finalWidth1 = linearTimerView.measuredWidth
                print("Height: $finalHeight1 Width: $finalWidth1")
                /*binding.maplayout.layoutParams.height = (finalHeight1 / 1.1).toInt()
                binding.maplayout.layoutParams.width = (finalWidth1 / 1.1).toInt()
                binding.maplayout.requestLayout()*/
                return true
            }
        })
        print("radius: $radius")
        linearTimerView.circleRadiusInDp = radius


        linearTimer = LinearTimer.Builder()
                .linearTimerView(linearTimerView)
                .duration(duration)
                .timerListener(this)
                .progressDirection(LinearTimer.COUNTER_CLOCK_WISE_PROGRESSION)
                .preFillAngle(0f)
                .endingAngle(360)
                .getCountUpdate(LinearTimer.COUNT_UP_TIMER, 1000)
                .build()
        request_receive_dialog_layout.background = waveDrawable
        val interpolator = LinearInterpolator()

        waveDrawable.setWaveInterpolator(interpolator)
        waveDrawable.startAnimation()


        try {
            linearTimerView.clearAnimation()
            linearTimerView.animate()
            linearTimerView.animation = null
            linearTimer.startTimer()
            linearTimerView.visibility = View.GONE

        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * After completed time for reqeust to stop animation
     */
    override fun animationComplete() {
        waveDrawable.stopAnimation()
        //        linearTimer.pauseTimer();
        //linearTimer.resumeTimer();
        linearTimer.resetTimer()
        val main = Intent(this, DriverNotAcceptActivity::class.java)
        main.putExtra("url", intent.getStringExtra("url"))
        main.putExtra("carname", intent.getStringExtra("carname"))
        //main.putExtra("mapurl", getIntent().getStringExtra("mapurl"));
        main.putExtra("totalcar", intent.getIntExtra("totalcar", 0))
        main.putExtra("hashMap", locationHashMap)
        startActivity(main)
        finish()
    }

    /**
     * Loader timer for request
     */
    override fun timerTick(tickUpdateInMillis: Long) {
        DebuggableLogI("Time left", tickUpdateInMillis.toString())

    }

    override fun onTimerReset() {
        DebuggableLogV("Do", "Nothing")
    }


    /**
     * Receive push notification
     */
    fun requestReceive() {
        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (intent.action == Config.REGISTRATION_COMPLETE) {
                    // FCM successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)


                } else if (intent.action == Config.PUSH_NOTIFICATION) {
                    // new push notification is received

                    //  String message = intent.getStringExtra("message");

                    loadJSONDATA()

                }
            }
        }
    }


    /**
     * Load Trip details
     */
    fun loadJSONDATA() {
        JSON_DATA = sessionManager.pushJson.toString()



        try {
            val jsonObject = JSONObject(JSON_DATA)

            if (jsonObject.getJSONObject("custom").has("accept_request")) {

                val trip_id = jsonObject.getJSONObject("custom").getJSONObject("accept_request").getString("trip_id")
                //sessionManager.setTripId(trip_id);
                sessionManager.tripStatus = "accept_request"
                //   req_id = jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("request_id");
                //   pickup_address= jsonObject.getJSONObject("custom").getJSONObject("ride_request").getString("pickup_location");
                commonMethods.showProgressDialog(this)

                if (!isInternetAvailable) {
                    commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
                } else {
                    if (!trip_id.equals(sessionManager.tripId, ignoreCase = true)) {
                        sessionManager.tripId = trip_id
                        getTripDetails(trip_id)
                    }

                }
            } else if (jsonObject.getJSONObject("custom").has("no_cars")) {
                addFirebaseDatabase.removeRequestTable()

                val main = Intent(this, DriverNotAcceptActivity::class.java)
                main.putExtra("url", intent.getStringExtra("url"))
                main.putExtra("carname", intent.getStringExtra("carname"))
                //                main.putExtra("fare_estimation", getIntent().getStringExtra("fare_estimation"));
                //main.putExtra("mapurl", getIntent().getStringExtra("mapurl"));
                main.putExtra("totalcar", intent.getIntExtra("totalcar", 0))
                main.putExtra("hashMap", locationHashMap)

                startActivity(main)
                finish()
            }


        } catch (e: JSONException) {

        }

    }

    public override fun onResume() {
        super.onResume()
        isSendingRequestisLive = true
        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
                IntentFilter(Config.REGISTRATION_COMPLETE))

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
                IntentFilter(Config.PUSH_NOTIFICATION))

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)
        if (CommonKeys.isRequestAccepted) {
            CommonKeys.isRequestAccepted = false
            val requstreceivepage = Intent(this, MainActivity::class.java)
            startActivity(requstreceivepage)
            finish()
        }

    }

    public override fun onPause() {
        super.onPause()
        isSendingRequestisLive = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver!!)
        MainActivity.isMainActivity = true
    }

    override fun onDestroy() {
        isSendingRequestisLive = false
        super.onDestroy()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            onSuccessDriver(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }

    }

    fun onSuccessDriver(jsonResp: JsonResponse) {
        addFirebaseDatabase.removeRequestTable()

        commonMethods.hideProgressDialog()
        tripDetailsModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        sessionManager.isrequest = false
        sessionManager.isTrip = true
        MainActivity.isMainActivity = true
        val requstreceivepage = Intent(this, MainActivity::class.java)
        requstreceivepage.putExtra("driverDetails", tripDetailsModel)
        startActivity(requstreceivepage)
        finish()
    }


    /**
     * After driver accept the request to get driver details from the API
     */
    fun getTripDetails(tripId: String) {
        MainActivity.isMainActivity = true
        sessionManager.tripId = tripId
        commonMethods.showProgressDialog(this)
        apiService.getTripDetails(sessionManager.accessToken!!, tripId).enqueue(RequestCallback(REQ_GET_DRIVER, this))
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val dropmarker: MarkerOptions
        val pickupMarker: MarkerOptions


//        locationHashMap["pickup_longitude"] = pickup?.longitude.toString()
        val pickupLatLng = LatLng(this.locationHashMap?.get("pickup_latitude")!!.toDouble(), locationHashMap?.get("pickup_longitude")!!.toDouble())
        // val dropLatLng = LatLng(this.locationHashMap["drop_latitude"]!!.toDouble(),locationHashMap["drop_longitude"]!!.toDouble())
        val dropLatLng = LatLng(this.locationHashMap?.get("drop_latitude")!!.toDouble(), locationHashMap?.get("drop_longitude")!!.toDouble())

        pickupMarker = MarkerOptions()
        pickupMarker.position(pickupLatLng)
        pickupMarker.icon(commonMethods.vectorToBitmap(R.drawable.app_ic_pickup_small, this))
        pickupMarker.alpha(.6f)
        //pickupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_dot))
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        mMap.addMarker(pickupMarker)

        dropmarker = MarkerOptions()
        dropmarker.position(dropLatLng)
        dropmarker.icon(commonMethods.vectorToBitmap(R.drawable.app_ic_drop_small, this))
        dropmarker.alpha(.6f)
        //dropmarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dest_dot))
        mMap.addMarker(dropmarker)
        mMap.addPolyline(polylineOptions)

        val builder = LatLngBounds.Builder()

        //the include method will calculate the min and max bound.
        builder.include(pickupMarker.position)
        builder.include(dropmarker.position)

        val bounds = builder.build()

        // val cameraPosition = CameraPosition.Builder().zoom(13f).tilt(0f).build()


        val width = resources.displayMetrics.widthPixels / 3
        val height = resources.displayMetrics.heightPixels / 3
        val padding = (width * 0.05).toInt() // offset from edges of the map 10% of screen

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

        mMap.moveCamera(cu)

        val zoom = CameraUpdateFactory.zoomTo(14f)
        mMap.animateCamera(zoom)

        // mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        //mMap.setMaxZoomPreference(15f)
        mMap.uiSettings.isScrollGesturesEnabled = false

        mMap.uiSettings.setAllGesturesEnabled(false)
    }


    override fun RequestListener(Tripid: String) {
        getTripDetails(Tripid)
    }

    companion object {

        var tripDetailsModel = TripDetailsModel()

        var isSendingRequestisLive = false
    }
}

