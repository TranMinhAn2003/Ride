package com.rideincab.user.taxi

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage -
 * @category MainActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityScheduleRideDetailBinding
import com.rideincab.user.taxi.sidebar.trips.YourTrips
import com.rideincab.user.taxi.views.customize.CustomDialog
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
   To call Api for Schedule ride
    *********************************************************** */


class ScheduleRideDetailActivity : CommonActivity(), ServiceListener {
    private lateinit var binding: AppActivityScheduleRideDetailBinding
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

    private lateinit var date: String
    private var overviewPolylines: String = ""
    private var locationId: String? = null
    private var peakId: String? = null
    private var clickedCar: String? = null
    private var pickupLocation: String? = null
    private var dropLocation: String? = null
    private var isWallet: String? = null
    private var fare: String? = null
    private var pickupLatitude: Double = 0.toDouble()
    private var pickupLongitude: Double = 0.toDouble()
    private var dropLatitude: Double = 0.toDouble()
    private var dropLongitude: Double = 0.toDouble()
    private var isInternetAvailable: Boolean = false
    private lateinit var scheduleHashMap: HashMap<String, String>

   /* @BindView(R.id.trip_date_time)
    lateinit var tripDateTime: TextView

    @BindView(R.id.amountscheduled)
    lateinit var amountScheduled: TextView*/


    fun done() {
        val finalDateTime = sessionManager.scheduleDate + " " + sessionManager.presentTime
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")

        try {
            val calendar = Calendar.getInstance(Locale.getDefault())

            val currentDateandTime = sdf.format(calendar.time)

            val date: Date = sdf.parse(currentDateandTime)
            val selectedDate: Date = sdf.parse(finalDateTime)

            calendar.add(Calendar.MINUTE, Constants.scheduledDuration)
            val updatedDate = sdf.format(calendar.time)

            val aHeadCurrentDate: Date = sdf.parse(updatedDate)

            if (selectedDate.after(date) && (selectedDate.equals(aHeadCurrentDate) || selectedDate.after(aHeadCurrentDate))) {
                saveScheduleRide()
            } else {
                Toast.makeText(this, resources.getString(R.string.valid_time), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityScheduleRideDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(applicationContext)  //Check internet access

        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.UpcomingRideSet), binding.commonHeader.tvHeadertext)

        println("thisSchedule" + intent.getStringExtra("is_wallet"))
        date = sessionManager.scheduleDateTime.toString()
        locationId = intent.getStringExtra("location_id")
        peakId = intent.getStringExtra("peak_id")
        clickedCar = intent.getStringExtra("clicked_car")
        pickupLocation = intent.getStringExtra("pickup_location")
        dropLocation = intent.getStringExtra("drop_location")
        isWallet = intent.getStringExtra("is_wallet")
        fare = intent.getStringExtra("fare_estimation")
        pickupLatitude = intent.getDoubleExtra("pickup_latitude", 0.0)
        pickupLongitude = intent.getDoubleExtra("pickup_longitude", 0.0)
        dropLatitude = intent.getDoubleExtra("drop_latitude", 0.0)
        dropLongitude = intent.getDoubleExtra("drop_longitude", 0.0)
        overviewPolylines = intent.getStringExtra("polyline").toString()
        binding.tripDateTime.text = date
        binding.amountscheduled.text = sessionManager.currencySymbol + fare

        binding.commonHeader.back.setOnClickListener { onBackPressed() }
        binding.doneLayout.setOnClickListener { done() }
    }

    /**
     * To save schedule by calling api
     */

    private fun saveScheduleRide() {
        if (isInternetAvailable) {
            sessionManager.isrequest = false
            val tz = TimeZone.getDefault()
            scheduleHashMap = HashMap()

            scheduleHashMap["schedule_date"] = sessionManager.scheduleDate.toString()
            scheduleHashMap["schedule_time"] = sessionManager.presentTime.toString()
            scheduleHashMap["car_id"] = clickedCar.toString()
            scheduleHashMap["pickup_latitude"] = pickupLatitude.toString()
            scheduleHashMap["pickup_longitude"] = pickupLongitude.toString()
            scheduleHashMap["drop_latitude"] = dropLatitude.toString()
            scheduleHashMap["drop_longitude"] = dropLongitude.toString()
            scheduleHashMap["pickup_location"] = pickupLocation.toString()
            scheduleHashMap["drop_location"] = dropLocation.toString()
            scheduleHashMap["payment_method"] = sessionManager.paymentMethod.toString()
            scheduleHashMap["is_wallet"] = isWallet.toString()
            scheduleHashMap["user_type"] = sessionManager.type.toString()
            scheduleHashMap["device_type"] = sessionManager.deviceType.toString()
            scheduleHashMap["device_id"] = sessionManager.deviceId.toString()
            scheduleHashMap["token"] = sessionManager.accessToken.toString()
            scheduleHashMap["timezone"] = tz.id
            scheduleHashMap["polyline"] = overviewPolylines
            scheduleHashMap["location_id"] = locationId.toString()
            scheduleHashMap["peak_id"] = peakId.toString()
            scheduleRide()
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        }
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            val intent = Intent(this@ScheduleRideDetailActivity, YourTrips::class.java)
            intent.putExtra("upcome", "upcome")
            startActivity(intent)
            finish()
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            commonMethods.hideProgressDialog()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
        commonMethods.hideProgressDialog()
    }

    /**
     * Call ScheduleRide API
     */
    private fun scheduleRide() {
        commonMethods.showProgressDialog(this)
        apiService.scheduleRide(scheduleHashMap).enqueue(RequestCallback(this))
    }
}