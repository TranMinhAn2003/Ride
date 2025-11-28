package com.rideincab.user.taxi.sidebar

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar
 * @category EnRoute
 * @author SMR IT Solutions
 *
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.taxi.datamodels.trip.Riders
import com.rideincab.user.taxi.datamodels.trip.TripDetailsModel
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.pushnotification.Config
import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.taxi.sendrequest.CancelYourTripActivity
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsBottomPadding
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityEnRouteBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
  Driver start the trip map and route shown
    *********************************************************** */
class EnRoute : CommonActivity() {
    private lateinit var binding: AppActivityEnRouteBinding

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    /*@BindView(R.id.cancel)
    lateinit var cancel: RelativeLayout

    @BindView(R.id.binding.cancelTxt)
    lateinit var binding.cancelTxt: TextView

    @BindView(R.id.driver_name)
    lateinit var driver_name: TextView

    @BindView(R.id.arrivel_time)
    lateinit var arrivel_time: TextView

    @BindView(R.id.vehicle_name)
    lateinit var vehicle_name: TextView

    @BindView(R.id.binding.starrating)
    lateinit var binding.starrating: TextView

    @BindView(R.id.binding.driverCarNumber)
    lateinit var binding.driverCarNumber: TextView

    @BindView(R.id.binding.arrivalTxt)
    lateinit var binding.arrivalTxt: TextView

    @BindView(R.id.binding.pickupLocation)
    lateinit var binding.pickupLocation: TextView

    @BindView(R.id.profile_image1)
    lateinit var driver_profile_image: ImageView

    @BindView(R.id.common_profile)
    lateinit var commonProfile: View*/

    var totalDuration = ""

    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null

    fun cancel() {
        val intent = Intent(this, CancelYourTripActivity::class.java)
        startActivity(intent)
    }

    /*@OnClick(R.id.contactlayout)
    fun contactlayout() {
        val intent = Intent(this, DriverContactActivity::class.java)
        intent.putExtra("drivername", tripDetailsModel.driverName)
        intent.putExtra("drivernumber", tripDetailsModel.mobileNumber)
        intent.putExtra(KEY_CALLER_ID, tripDetailsModel.driverId.toString())
        startActivity(intent)
    }*/

    fun callThisNo() {
        try {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.CALL_PHONE
                    ),
                    1
                )
                return;
            } else {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data =
                    Uri.parse("tel:${tripDetailsModel.mobileNumber}")
                startActivity(callIntent)
            }
        } catch (e: Exception) {
            Log.i("TAGA", "callThisNo: Error=${e.localizedMessage}")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityEnRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        binding.root.applySystemBarsPadding()
        binding.commonHeader.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)

        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.enrote), binding.commonHeader.tvHeadertext)
        /**
         * Get accepted driver details
         */

        if (extras != null) {
            tripDetailsModel =
                intent.getSerializableExtra("driverDetails") as TripDetailsModel //Obtaining data
            riderdetails = tripDetailsModel.riders.get(0)
            totalDuration = intent.getStringExtra("duration").toString()
        }

        /**
         * Receive push notification
         */
        receivePushNotification()


        /**
         * Show driver details
         */
        insertDriverInfoToSession()
        binding.driverName.text = tripDetailsModel.driverName


        /*if (Integer.parseInt(tripDetailsModel.getArrivalTime()) > 1) {
            arrivel_time.setText(tripDetailsModel.getArrivaltime() + " " + getResources().getString(R.string.mins));
        } else {
            arrivel_time.setText(tripDetailsModel.getArrivaltime() + " " + getResources().getString(R.string.min));
        }*/
        binding.vehicleName.text = tripDetailsModel.vehicleName

        if (tripDetailsModel.rating == "" || tripDetailsModel.rating == "0.0") {
            binding.starrating.visibility = View.GONE
        } else {
            binding.starrating.text = tripDetailsModel.rating

        }
        binding.driverCarNumber.text = tripDetailsModel.vehicleNumber


        /*   if (tripDetailsModel.arrivalTime > 1) {
               binding.arrivalTxt.text = tripDetailsModel.arrivalTime.toString() + " " + resources.getString(R.string.mins)
           } else {
               binding.arrivalTxt.text = tripDetailsModel.arrivalTime.toString() + " " + resources.getString(R.string.min)
           }*/

        var etaText: String

        val c = Calendar.getInstance()
        c.add(Calendar.MINUTE, tripDetailsModel.arrivarTime)
        val sdf = SimpleDateFormat("hh:mm a")
        val arTime = sdf.format(c.time)

        println("sessionManager.tripStatus ${sessionManager.tripStatus}")
        if (!TextUtils.isEmpty(totalDuration)) {

            if (sessionManager.tripStatus.equals(
                    "begin_trip",
                    ignoreCase = true
                ) || sessionManager.tripStatus.equals("end_trip", ignoreCase = true)
            ) {
                binding.arrivalTxt.text = totalDuration + " " + resources.getString(R.string.to_reach)

            } else {
                binding.arrivalTxt.text = totalDuration + " " + resources.getString(R.string.to_arrive)
            }

        } else {
            if (sessionManager.tripStatus.equals(
                    "begin_trip",
                    ignoreCase = true
                ) || sessionManager.tripStatus.equals("end_trip", ignoreCase = true)
            ) {
                binding.arrivalTxt.text = resources.getQuantityString(
                    R.plurals.minutes,
                    1,
                    1
                ) + resources.getString(R.string.to_reach)

            } else {
                binding.arrivalTxt.text == resources.getQuantityString(
                    R.plurals.minutes,
                    1,
                    1
                ) + resources.getString(R.string.to_arrive)
            }
        }
        if (sessionManager.tripStatus.equals(
                "begin_trip",
                ignoreCase = true
            ) || sessionManager.tripStatus.equals("end_trip", ignoreCase = true)
        ) {
            binding.pickupLocation.text = riderdetails.drop
        } else {
            binding.pickupLocation.text = riderdetails.pickup
        }

        Picasso.get().load(tripDetailsModel.driverThumbImage)
            .into(binding.commonProfile.profileImage1)


        if ((sessionManager.tripStatus != ""
                    //|| !sessionManager.getTripStatus().equals("null")
                    && sessionManager.tripStatus == "begin_trip") || sessionManager.tripStatus == "end_trip"
        ) {
            binding.cancel.isClickable = false
            binding.cancel.isEnabled = false
            binding.cancelTxt.setTextColor(ContextCompat.getColor(this, R.color.cancel_text_color))
        } else {
            binding.cancel.isClickable = true
            binding.cancel.isEnabled = true
            binding.cancelTxt.setTextColor(ContextCompat.getColor(this, R.color.app_primary_text))
        }

      
        binding.commonHeader.arrow.setOnClickListener { onBackPressed() }
        binding.cancel.setOnClickListener { cancel() }
        binding.contactlayout.setOnClickListener { callThisNo() }
    }

    fun insertDriverInfoToSession() {
        sessionManager.driverProfilePic = tripDetailsModel.driverThumbImage
        sessionManager.driverRating = tripDetailsModel.rating
        sessionManager.driverName = tripDetailsModel.driverName
        sessionManager.driverId = tripDetailsModel.driverId.toString()
    }

    /**
     * Receive push notification
     */
    fun receivePushNotification() {
        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (intent.action == Config.REGISTRATION_COMPLETE) {
                    // FCM successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)


                } else if (intent.action == Config.PUSH_NOTIFICATION) {
                    // new push notification is received

                    val JSON_DATA = sessionManager.pushJson


                    try {
                        val jsonObject = JSONObject(JSON_DATA)

                        if (jsonObject.getJSONObject("custom").has("begin_trip")) {
                            //String trip_id = jsonObject.getJSONObject("custom").getJSONObject("begin_trip").getString("trip_id");
                            binding.cancel.isClickable = false
                            binding.cancel.isEnabled = false
                            binding.cancelTxt.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.cancel_text_color
                                )
                            )

                        }


                    } catch (e: JSONException) {

                    }

                }
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver!!)
    }

    public override fun onResume() {


        super.onResume()

        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mRegistrationBroadcastReceiver!!,
            IntentFilter(Config.REGISTRATION_COMPLETE)
        )

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mRegistrationBroadcastReceiver!!,
            IntentFilter(Config.PUSH_NOTIFICATION)
        )

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)
    }

    companion object {

        lateinit var tripDetailsModel: TripDetailsModel
        lateinit var riderdetails: Riders

    }


}
