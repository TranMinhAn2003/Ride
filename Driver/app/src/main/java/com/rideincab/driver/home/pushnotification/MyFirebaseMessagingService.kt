package com.rideincab.driver.home.pushnotification

/**
 * @package com.cloneappsolutions.cabmedriver.home.pushnotification
 * @subpackage pushnotification model
 * @category MyFirebaseMessagingService
 * @author SMR IT Solutions
 *
 */

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.PowerManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.helper.ManualBookingDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.CommonMethods.Companion.DebuggableLogE
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.home.datamodel.TripDetailsModel
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.trips.RequestAcceptActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


/* ************************************************************
                MyFirebaseMessagingService
Its used to get the pushnotification FirebaseMessagingService function
*************************************************************** */
class MyFirebaseMessagingService : FirebaseMessagingService(), ServiceListener {

    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var apiService: ApiService

    val deviceId: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["device_type"] = sessionManager.deviceType!!
            driverStatusHashMap["device_id"] = sessionManager.deviceId!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        AppController.getAppComponent().inject(this)
        //val refreshedToken = commonMethods.getFireBaseToken()
        val refreshedToken = p0

        println("On New Token : "+p0)

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken)

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken)
    }


    private fun sendRegistrationToServer(token: String?) {
        // sending FCM token to server
        println("sendRegistrationToServer: " + token)
        sessionManager.deviceId = token!!

        if (sessionManager.accessToken != null) {
            updateDeviceId()
        }
    }

    /*
    * Update motorista device id
    */
    private fun storeRegIdInPref(token: String?) {
        val pref = applicationContext.getSharedPreferences(Config.SHARED_PREF, 0)
        val editor = pref.edit()
        editor.putString("regId", token)
        editor.commit()
    }

    fun updateDeviceId() {
        if (!sessionManager.accessToken.isNullOrEmpty() && !sessionManager.deviceId.isNullOrEmpty()) {

            apiService.updateDevice(deviceId).enqueue(RequestCallback(this))
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }

    override fun onCreate() {
        super.onCreate()
        AppController.getAppComponent().inject(this)
        setLocale()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        DebuggableLogE(TAG, "From: " + remoteMessage.from)
        wakeUpScreen()

        Log.i(TAG, "onMessageReceived: data=${remoteMessage.data}")
        Log.i(TAG, "onMessageReceived: notification=${remoteMessage.notification}")
        Log.i(TAG, "onMessageReceived: notification?.body=${remoteMessage.notification?.body}")
        Log.i(TAG, "onMessageReceived: notification?.title=${remoteMessage.notification?.title}")

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            DebuggableLogE(TAG, "Data Payload: " + remoteMessage.data.toString())

            try {
                val json = JSONObject(remoteMessage.data.toString())
                commonMethods.handleDataMessage(json,this)
//                handleDataMessage(json)
                if (remoteMessage.notification != null) {
                    DebuggableLogE(TAG, "Notification Body: " + remoteMessage.notification?.body)
                }

            } catch (e: Exception) {
                DebuggableLogE(TAG, "Exception: " + e.message)
            }

        }


    }

    @SuppressLint("InvalidWakeLockTag")
    private fun wakeUpScreen() {
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        Log.e("screen on......", "" + isScreenOn)
        if (!isScreenOn) {
            val wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "MyLock")
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }
    }

    fun manualBookingTripBookedInfo(manualBookedPopupType: Int, jsonObject: JSONObject , context: Context) {
        var riderName = ""
        var riderContactNumber = ""
        var riderPickupLocation = ""
        var riderPickupDateAndTime = ""
        try {
            riderName = jsonObject.getString("rider_first_name") + " " + jsonObject.getString("rider_last_name")
            riderContactNumber = jsonObject.getString("rider_country_code") + " " + jsonObject.getString("rider_mobile_number")
            riderPickupLocation = jsonObject.getString("pickup_location")
            riderPickupDateAndTime = jsonObject.getString("date") + " - " + jsonObject.getString("time")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val dialogs = Intent(context, ManualBookingDialog::class.java)
        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_NAME, riderName)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_CONTACT_NUMBER, riderContactNumber)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_LOCATION, riderPickupLocation)
        dialogs.putExtra(CommonKeys.KEY_MANUAL_BOOKED_RIDER_PICKU_DATE_AND_TIME, riderPickupDateAndTime)
        dialogs.putExtra(CommonKeys.KEY_TYPE, manualBookedPopupType)
        startActivity(dialogs)

    }

    fun manualBookingTripStarts(jsonResp: JSONObject, context: Context) {


        val riderModel = gson.fromJson(jsonResp.toString(), TripDetailsModel::class.java)
        sessionManager.riderName = riderModel.riderDetails.get(0).name
        sessionManager.riderId = riderModel.riderDetails.get(0).riderId!!
        sessionManager.riderRating = riderModel.riderDetails.get(0).rating
        sessionManager.riderProfilePic = riderModel.riderDetails.get(0).profileImage
        sessionManager.bookingType = riderModel.riderDetails.get(0).bookingType
        sessionManager.tripId = riderModel.riderDetails.get(0).tripId.toString()
        sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
        //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
        sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
        //sessionManager.paymentMethod = riderModel.paymentMode

        sessionManager.isDriverAndRiderAbleToChat = true
        CommonMethods.startFirebaseChatListenerService(this)


        /* if (!WorkerUtils.isWorkRunning(CommonKeys.WorkTagForUpdateGPS)) {
             DebuggableLogE("locationupdate", "StartWork:")
             WorkerUtils.startWorkManager(CommonKeys.WorkKeyForUpdateGPS, CommonKeys.WorkTagForUpdateGPS, UpdateGPSWorker::class.java,this,sessionManager.driverStatus)
         }*/

        //  acceptedDriverDetails = new AcceptedDriverDetails(ridername, mobilenumber, profileimg, ratingvalue, cartype, pickuplocation, droplocation, pickuplatitude, droplatitude, droplongitude, pickuplongitude);
        //        mPlayer.stop();
        val requestaccept = Intent(context, RequestAcceptActivity::class.java)
        requestaccept.putExtra("riderDetails", riderModel)
        requestaccept.putExtra("tripstatus", resources.getString(R.string.confirm_arrived))
        requestaccept.putExtra(CommonKeys.KEY_IS_NEED_TO_PLAY_SOUND, CommonKeys.YES)
        requestaccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(requestaccept)
    }




    fun setLocale() {
        val lang = sessionManager.language

        if (lang != "") {
            val langC = sessionManager.languageCode
            val locale = Locale(langC)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            resources.updateConfiguration(
                config,
                resources.displayMetrics
            )
        } else {
            sessionManager.language = "English"
            sessionManager.languageCode = "en"
        }


    }

    companion object {

        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }
}
