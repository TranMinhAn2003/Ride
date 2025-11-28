package com.rideincab.user.taxi.adapters

import android.content.Intent
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.taxi.datamodels.trip.ScheduleDetail
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.taxi.sendrequest.CancelYourTripActivity
import com.rideincab.user.taxi.singledateandtimepicker.SingleDateAndTimePicker
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.taxi.views.customize.CustomDialog
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by SMR IT Solutions on 7/11/18.
 */

class UpcomingAdapter(private val scheduleRideDetailsArrayList: ArrayList<ScheduleDetail>, private val context: CommonActivity) : RecyclerView.Adapter<UpcomingAdapter.ViewHolder>(), ServiceListener {
    private var mLastClickTime: Long = 0
    var dialog: AlertDialog
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
    protected var isInternetAvailable: Boolean = false
    private lateinit var scheduleHashMap: HashMap<String, String>
    private var scheduleridetext: TextView? = null
    private var singleDateAndTimePicker: SingleDateAndTimePicker? = null
    private lateinit var presenttime: String
    //private String date_time;
    private lateinit var ScheduleRidedate: String


    init {
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(this.context)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
                .from(viewGroup.context)
                .inflate(R.layout.app_schedule_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.dateAndTime.text = scheduleRideDetailsArrayList[position].scheduleDisplayDate
        holder.tripType.text = "Schedule Trip"
        holder.carType.text = scheduleRideDetailsArrayList[position].carName
        holder.amount.text = sessionManager.currencySymbol + scheduleRideDetailsArrayList[position].fareEstimation
        holder.pickUpAddress.text = scheduleRideDetailsArrayList[position].pickupLocation
        holder.destAdddress.text = scheduleRideDetailsArrayList[position].dropLocation
        holder.cancel.setOnClickListener {
            val intent = Intent(context, CancelYourTripActivity::class.java)
            intent.putExtra("upcome", "upcome")
            intent.putExtra("scheduleID", scheduleRideDetailsArrayList[position].id)
            context.startActivity(intent)
        }

        holder.edit.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            scheduleRide(position)//schedule ride date picker
            ScheduleMethod()
        }

    }

    override fun getItemCount(): Int {
        return scheduleRideDetailsArrayList.size
    }


    private fun scheduleRide(position: Int) {
        val dialog3 = BottomSheetDialog(context)
        dialog3.setContentView(R.layout.app_activity_schedule_ride)
        scheduleridetext = dialog3.findViewById<View>(R.id.time_date) as TextView?
        singleDateAndTimePicker = dialog3.findViewById<View>(R.id.single_day_picker) as SingleDateAndTimePicker?
        val set_pickup_window = (dialog3.findViewById<View>(R.id.set_pickup_window) as Button?)!!
        set_pickup_window.setOnClickListener {
            dialog3.dismiss()


            isInternetAvailable = commonMethods.isOnline(context)
            if (!isInternetAvailable)
                dialogfunction()
            else {
                scheduleHashMap = HashMap()
                scheduleHashMap["schedule_id"] = scheduleRideDetailsArrayList[position].id
                scheduleHashMap["schedule_date"] = ScheduleRidedate
                scheduleHashMap["schedule_time"] = presenttime
                scheduleHashMap["token"] = sessionManager.accessToken.toString()
                scheduleRide()
            }
        }
        if (!dialog3.isShowing) {
            dialog3.show()
        }
    }

    /**
     * To check wheather connected or not
     *
     * @return true connected false not connected
     */


    fun dialogfunction() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Turn on your Internet")
                .setCancelable(false)

        val alert = builder.create()
        alert.show()
    }


    /**
     * Schedule a ride wheel picker
     */
    private fun ScheduleMethod() {

        val mindate = Calendar.getInstance().time           //Getting Today's date
        mindate.minutes = mindate.minutes + 15
        val calendar = Calendar.getInstance()
        calendar.time = mindate


        singleDateAndTimePicker!!.setDefaultDate(mindate)
        singleDateAndTimePicker!!.selectDate(calendar)
        singleDateAndTimePicker!!.setDisplayDays(true)
        singleDateAndTimePicker!!.setMustBeOnFuture(true)
        singleDateAndTimePicker!!.mustBeOnFuture()                               //Setting date only to future
        singleDateAndTimePicker!!.minDate = mindate                           // MIn date ie-Today date
        singleDateAndTimePicker!!.maxDate = scheduleRideDate(mindate)        //Max date upto 30 dates
        singleDateAndTimePicker!!.setStepMinutes(1)                           //Setting minute intervals to 1min
        singleDateAndTimePicker!!.addOnDateChangedListener { _, date ->
            /**
             * When scrolling the picker
             */
            /**
             * When scrolling the picker
             */
            scheduleRideDate(date)
        }

    }


    private fun scheduleRideDate(dateToSelect: Date): Date {
        val calendarMax = Calendar.getInstance()

        val df = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)  //Normal date Format
        var newTime = ""
        try {
            val d = df.parse(dateToSelect.toString())

            calendarMax.time = d
            calendarMax.add(Calendar.MINUTE, 15)       //Setting Maximum Duration upto 15.
            calendarMax.add(Calendar.DATE, 30)          //Setting Maximum Date upto 30.
            newTime = df.format(calendarMax.time)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val maxDate = calendarMax.time


        /**
         * new time to compare
         */
        //val now = Calendar.getInstance().time


        val input_date = dateToSelect.toString()

        val new_date = newTime
        var dt1: Date? = null
        var dt2: Date? = null
        try {
            dt1 = df.parse(input_date)
            dt2 = df.parse(new_date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        /**
         * Getting the date format from normal present time
         */

        val format2 = SimpleDateFormat("EEE, MMM dd 'at' hh:mm a")
        val finalDay = format2.format(dt1)

        /**
         * Getting normal time with seconds
         */
        val format4 = SimpleDateFormat("HH:mm")
        presenttime = format4.format(dt1)
        /**
         * Getting the time format from 15 mins time
         */
        val format3 = SimpleDateFormat("hh:mm a")
        val time_15 = format3.format(dt2)


        val format5 = SimpleDateFormat("yyyy-MM-dd")
        ScheduleRidedate = format5.format(dt1)

        val ans = "$finalDay - $time_15"

        scheduleridetext!!.text = ans

        return maxDate
    }

    /**
     * To call schedule ride edit api
     */
    fun scheduleRide() {
        commonMethods.showProgressDialog(context)
        apiService.scheduleRide(scheduleHashMap).enqueue(RequestCallback(this))
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            onSuccessSchedule(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
        }
    }

    private fun onSuccessSchedule(jsonResp: JsonResponse) {
        try {
            val json = JSONObject(jsonResp.strResponse.toString())
            val scheduletrips_array = json.getJSONArray("schedule_rides")

            if (scheduletrips_array.length() > 0) {
                for (i in 0 until scheduletrips_array.length()) {
                    val jsonObj = scheduletrips_array.getJSONObject(i)
                    val schedule_display = jsonObj.getString("schedule_display_date")
                    scheduleRideDetailsArrayList[i].scheduleDisplayDate = schedule_display
                }
                notifyDataSetChanged()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            commonMethods.hideProgressDialog()
        }

    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
         val dateAndTime: TextView
         val tripType: TextView
         val carType: TextView
         val amount: TextView
         val pickUpAddress: TextView
         val destAdddress: TextView
         val cancel: TextView
         val edit: TextView

        private val cancel_lay: LinearLayout

        init {

            dateAndTime = view.findViewById<View>(R.id.date_and_time) as TextView
            tripType = view.findViewById<View>(R.id.trip_tupe) as TextView
            carType = view.findViewById<View>(R.id.car_type) as TextView
            amount = view.findViewById<View>(R.id.amount) as TextView
            pickUpAddress = view.findViewById<View>(R.id.pickupaddress) as TextView
            destAdddress = view.findViewById<View>(R.id.destadddress) as TextView
            cancel_lay = view.findViewById<View>(R.id.cancel_lay) as LinearLayout
            cancel = view.findViewById<View>(R.id.cancel) as TextView
            edit = view.findViewById<View>(R.id.edit) as TextView


        }
    }
}