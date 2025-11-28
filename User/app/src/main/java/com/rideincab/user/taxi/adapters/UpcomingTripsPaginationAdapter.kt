package com.rideincab.user.taxi.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.custompalette.FontButton
import com.rideincab.user.common.custompalette.FontTextView
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.taxi.datamodels.trip.ScheduleDetail
import com.rideincab.user.taxi.datamodels.trip.TripListModelArrayList
import com.rideincab.user.taxi.interfaces.PaginationAdapterCallback
import com.rideincab.user.taxi.sendrequest.CancelYourTripActivity
import com.rideincab.user.taxi.singledateandtimepicker.SingleDateAndTimePicker
import com.rideincab.user.taxi.views.customize.CustomDialog
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class UpcomingTripsPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback, private val appCompatActivity: CommonActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServiceListener {
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
    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null


    // Schedule Ride variables
    protected var isInternetAvailable: Boolean = false
    private lateinit var scheduleHashMap: HashMap<String, String>
    private val scheduleRideDetailsArrayList: ArrayList<ScheduleDetail>? = null
    private var scheduleridetext: TextView? = null
    private var singleDateAndTimePicker: SingleDateAndTimePicker? = null
    private lateinit var presenttime: String

    //private String date_time;
    private lateinit var ScheduleRidedate: String
    private var tripType: String? = null
    private var tripStatus: String? = null
    private var InCompleteTrips: String? = null
    private var ScheduleTrip: String? = null
    lateinit var onItemRatingClickListener: onItemRatingClickListner

    private var tripStatusModels = ArrayList<TripListModelArrayList>()
    val isEmpty: Boolean
        get() = itemCount == 0

    fun setOnItemRatingClickListner(onItemRatingClickListner: onItemRatingClickListner) {
        onItemRatingClickListener = onItemRatingClickListner
    }

    interface onItemRatingClickListner {
        fun setRatingClick(position: Int, TripDetailsModel: TripListModelArrayList)
    }

    init {
        //tripStatusModels = ArrayList()
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(this.context)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)


        when (viewType) {

            ITEM -> {
                val viewItem = inflater.inflate(R.layout.app_card_layout, parent, false)
                viewHolder = CurrentTripsViewHolder(viewItem)
            }
            MANUAL_ITEM -> {
                val viewItem = inflater.inflate(R.layout.app_schedule_layout, parent, false)
                viewHolder = UpcomingViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading = inflater.inflate(R.layout.app_item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }

        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tripStatusModel = tripStatusModels!![position] // Upcoming Detail
        when (getItemViewType(position)) {

            ITEM -> {
                val currentTripsViewHolder = holder as CurrentTripsViewHolder

                currentTripsViewHolder.tvCountry.text = context.resources.getString(R.string.trip_id) + tripStatusModel.tripId
                currentTripsViewHolder.carName.text = tripStatusModel.carType
                if (tripStatusModel.status == "Rating") {
                    currentTripsViewHolder.status.text = context.getString(R.string.Rating)
                } else if (tripStatusModel.status == "Cancelled") {
                    currentTripsViewHolder.status.text = context.getString(R.string.Cancelled)
                    currentTripsViewHolder.status.setTextColor(Color.RED)
                } else if (tripStatusModel.status == "Completed") {
                    currentTripsViewHolder.status.text = context.getString(R.string.completed)
                    currentTripsViewHolder.status.setTextColor(Color.GREEN)
                } else if (tripStatusModel.status == "Payment") {
                    currentTripsViewHolder.status.text = context.getString(R.string.payment)
                } else if (tripStatusModel.status == "Begin trip") {
                    currentTripsViewHolder.status.text = context.getString(R.string.begin_trip)
                } else if (tripStatusModel.status == "End trip") {
                    currentTripsViewHolder.status.text = context.getString(R.string.end_trip)
                } else if (tripStatusModel.status == "Scheduled") {
                    currentTripsViewHolder.status.text = context.getString(R.string.scheduled)
                    currentTripsViewHolder.status.setTextColor(Color.BLUE)
                } else {
                    currentTripsViewHolder.status.text = tripStatusModel.carType
                }

                currentTripsViewHolder.amountCard.text = sessionManager.currencySymbol + tripStatusModel.totalFare
                 if (!TextUtils.isEmpty(tripStatusModel.mapImage)) {
                     currentTripsViewHolder.imageLayout.visibility = View.VISIBLE
                     Picasso.get().load(tripStatusModel.mapImage).placeholder(R.drawable.trip_map_placeholder)
                             .into(currentTripsViewHolder.imageView)
                     currentTripsViewHolder.rltData.visibility = View.GONE
                 } else {
                     currentTripsViewHolder.imageLayout.visibility = View.GONE
                     currentTripsViewHolder.tv_pickLocation.text = tripStatusModel.pickup
                     currentTripsViewHolder.tv_dropLocation.text = tripStatusModel.drop
                     currentTripsViewHolder.rltData.visibility = View.VISIBLE
                 }

               /* if (!TextUtils.isEmpty(tripStatusModel.pickup)&&!TextUtils.isEmpty(tripStatusModel.drop)){
                    currentTripsViewHolder.imageLayout.visibility = View.GONE
                    currentTripsViewHolder.tv_pickLocation.text = tripStatusModel.pickup
                    currentTripsViewHolder.tv_dropLocation.text = tripStatusModel.drop
                    currentTripsViewHolder.rltData.visibility = View.VISIBLE
                }else{
                    currentTripsViewHolder.imageLayout.visibility = View.VISIBLE
                    Picasso.get().load(tripStatusModel.mapImage).placeholder(R.drawable.trip_map_placeholder)
                        .into(currentTripsViewHolder.imageView)
                    currentTripsViewHolder.rltData.visibility = View.GONE
                }*/

                if (tripStatusModel.isPool!! && tripStatusModel.seats != 0) {
                    currentTripsViewHolder.seatcount.visibility = View.VISIBLE
                    currentTripsViewHolder.seatcount.setText(context.getString(R.string.seat_count) + " " + tripStatusModel.seats.toString())
                } else {
                    currentTripsViewHolder.seatcount.visibility = View.GONE
                }
                currentTripsViewHolder.rltData.setOnClickListener { onItemRatingClickListener.setRatingClick(position, tripStatusModel) }
                currentTripsViewHolder.imageView.setOnClickListener { onItemRatingClickListener.setRatingClick(position, tripStatusModel) }
            }

            MANUAL_ITEM -> {
                val upcomingViewHolder = holder as UpcomingViewHolder

                println("position" + position + "tripId" + tripStatusModels!![position].tripId.toString())
                upcomingViewHolder.dateAndTime.text = tripStatusModel.scheduleDisplayDate
                upcomingViewHolder.tripType.text = context.resources.getString(R.string.schedule_trip)
                upcomingViewHolder.carType.text = tripStatusModel.carType

                if (tripStatusModel.totalFare != null && tripStatusModel.totalFare != "null" && !tripStatusModel.equals("")) {
                    upcomingViewHolder.amount.text = sessionManager.currencySymbol + tripStatusModel.totalFare
                }
                upcomingViewHolder.pickUpAddress.text = tripStatusModel.pickup
                upcomingViewHolder.destAdddress.text = tripStatusModel.drop
                upcomingViewHolder.cancel.setOnClickListener {
                    cancelTripDialog(tripStatusModels[position].tripId.toString())
                }

                upcomingViewHolder.edit.setOnClickListener {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return@setOnClickListener
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    scheduleRide(position)//schedule ride date picker
                    ScheduleMethod()
                }
            }

            LOADING -> {
                val loadingVH = holder as LoadingViewHolder

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE

                    loadingVH.mErrorTxt.text = if (errorMsg != null)
                        errorMsg
                    else
                        context.getString(R.string.error_msg_unknown)

                } else {
                    loadingVH.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun cancelTripDialog(tripId: String) {
        try {
            val dialog = Dialog(context, R.style.DialogCustomTheme)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.cancel_booking_dialog)

            val btnCancel = dialog.findViewById<FontTextView>(R.id.txt_cancel)
            val btnNo = dialog.findViewById<FontTextView>(R.id.txt_no)


            btnCancel.setOnClickListener {
                val intent = Intent(context, CancelYourTripActivity::class.java)
                intent.putExtra("upcome", "upcome")
                intent.putExtra("scheduleID", tripId)
                context.startActivity(intent)
                dialog.dismiss()
            }
            btnNo.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)

            if (!dialog.isShowing)
                dialog.show()

        }catch (e:Exception){
            Log.i("TAG", "cancelTripDialog: Error=${e.localizedMessage}")
        }
    }

    override fun getItemCount(): Int {
        return if (tripStatusModels == null) 0 else tripStatusModels!!.size
    }

    override fun getItemViewType(position: Int): Int {
        //return if (position == tripStatusModels!!.size - 1 && isLoadingAdded) LOADING else ITEM
        if (tripStatusModels.size > 0) {
            tripType = tripStatusModels[position].bookingType
            tripStatus = tripStatusModels[position].status

            if (tripStatus.equals("pending", true)) {
                return MANUAL_ITEM
            } else if (tripType != null && !tripStatus.equals("pending", true)) {
                return ITEM
            } else {
                return LOADING
            }
        } else {
            return LOADING
        }
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
                val dateFormat = SimpleDateFormat("yyyy-MM-dd - HH:mm")
                val currentDateandTime = dateFormat.format(Date())
                val Finaldate = ScheduleRidedate + " - " + presenttime
                val aHeadCurrentDateandTime = dateFormat.format(Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(Constants.scheduledDuration.toLong())))

                try {
                    val date: Date = dateFormat.parse(currentDateandTime)
                    val selectedDate: Date = dateFormat.parse(Finaldate)
                    val aHeadCurrentDate: Date = dateFormat.parse(aHeadCurrentDateandTime)

                    if (selectedDate.after(date) && (selectedDate.equals(aHeadCurrentDate) || selectedDate.after(aHeadCurrentDate))) {
                        scheduleHashMap = HashMap()
                        scheduleHashMap["schedule_id"] = tripStatusModels!![position].tripId.toString()
                        scheduleHashMap["schedule_date"] = ScheduleRidedate
                        scheduleHashMap["schedule_time"] = presenttime
                        scheduleHashMap["token"] = sessionManager.accessToken.toString()
                        scheduleRide()
                    } else {
                        Toast.makeText(context, context.resources.getString(R.string.valid_time), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }
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
        mindate.minutes = mindate.minutes + Constants.scheduledDuration
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
            calendarMax.add(Calendar.MINUTE, Constants.scheduledDuration)       //Setting Maximum Duration upto 15.
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
        commonMethods.showProgressDialog(appCompatActivity)
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
                    val scheduleId = jsonObj.getInt("id")
                    for (j in 0 until tripStatusModels.size) {
                        if (tripStatusModels[j].tripId == scheduleId) {
                            tripStatusModels[j].scheduleDisplayDate = schedule_display
                        }
                    }

                }
                notifyDataSetChanged()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            commonMethods.hideProgressDialog()
        }

    }

    /*
      Helpers - Pagination
    */

    fun removeAll() {
        tripStatusModels.clear()
        notifyDataSetChanged()
    }

    fun add(tripStatusModel: TripListModelArrayList) {
        tripStatusModels!!.add(tripStatusModel)
        notifyItemInserted(tripStatusModels!!.size - 1)
    }

    fun addAll(tripStatusModels: ArrayList<TripListModelArrayList>) {
        for (tripStatusModel in tripStatusModels) {
            add(tripStatusModel)
        }
    }

    fun remove(tripStatusModel: TripListModelArrayList?) {
        val position = tripStatusModels!!.indexOf(tripStatusModel)
        if (position > -1) {
            tripStatusModels!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun clearAll() {
        isLoadingAdded = false
        tripStatusModels!!.clear()
        notifyDataSetChanged()
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(TripListModelArrayList())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = tripStatusModels!!.size - 1
        val tripStatusModel = getItem(position)

        if (tripStatusModel != null) {
            tripStatusModels!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): TripListModelArrayList? {
        return tripStatusModels!![position]
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(tripStatusModels!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }


    inner class UpcomingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val dateAndTime: TextView
        val tripType: TextView
        val carType: TextView
        val amount: TextView
        val pickUpAddress: TextView
        val destAdddress: TextView
        val cancel: TextView
        val edit: TextView
        val cancelLay: LinearLayout

        init {

            dateAndTime = itemView.findViewById<View>(R.id.date_and_time) as TextView
            tripType = itemView.findViewById<View>(R.id.trip_tupe) as TextView
            carType = itemView.findViewById<View>(R.id.car_type) as TextView
            amount = itemView.findViewById<View>(R.id.amount) as TextView
            pickUpAddress = itemView.findViewById<View>(R.id.pickupaddress) as TextView
            destAdddress = itemView.findViewById<View>(R.id.destadddress) as TextView
            cancelLay = itemView.findViewById<View>(R.id.cancel_lay) as LinearLayout
            cancel = itemView.findViewById<View>(R.id.cancel) as TextView
            edit = itemView.findViewById<View>(R.id.edit) as TextView
        }
    }

    inner class CurrentTripsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvCountry: TextView
        val carName: TextView
        val status: TextView
        val amountCard: TextView
        val imageView: ImageView
        val btnRate: Button
        internal val tv_dropLocation: TextView
        internal val tv_pickLocation: TextView
        internal val mapview: RelativeLayout
        internal val imageLayout: RelativeLayout
        internal val rltData: RelativeLayout
        val seatcount: TextView

        init {
            tvCountry = itemView.findViewById<View>(R.id.datetime) as TextView
            carName = itemView.findViewById<View>(R.id.carname) as TextView
            status = itemView.findViewById<View>(R.id.status) as TextView
            amountCard = itemView.findViewById<View>(R.id.amountcard) as TextView
            imageView = itemView.findViewById<View>(R.id.imageView) as ImageView
            btnRate = itemView.findViewById<View>(R.id.btnrate) as Button
            rltData = itemView.findViewById<View>(R.id.rltdata) as RelativeLayout
            tv_pickLocation = itemView.findViewById(R.id.tv_pick_location) as TextView
            mapview = itemView.findViewById(R.id.static_map) as RelativeLayout
            tv_dropLocation = itemView.findViewById(R.id.tv_drop_location) as TextView
            imageLayout = itemView.findViewById(R.id.image_layout) as RelativeLayout
            seatcount = itemView.findViewById(R.id.seatcount) as TextView
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val mProgressBar: ProgressBar
        val mRetryBtn: ImageButton
        val mErrorTxt: TextView
        val mErrorLayout: LinearLayout


        init {

            mProgressBar = itemView.findViewById(R.id.loadmore_progress)
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry)
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt)
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout)

            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {
                    showRetry(false, null)
                    mCallback.retryPageLoad()
                }
            }
        }
    }

    companion object {

        // View Types and Pagination variables
        private val ITEM = 0
        private val MANUAL_ITEM = 1
        private val LOADING = 2
    }

}
