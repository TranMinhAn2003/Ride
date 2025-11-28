package com.rideincab.driver.trips.rating

/**
 * @package com.cloneappsolutions.cabmedriver.trips.rating
 * @subpackage rating
 * @category PaymentAmountPage
 * @author SMR IT Solutions
 *
 */

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.database.AddFirebaseDatabase
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums.REQ_CASH_COLLECTED
import com.rideincab.driver.common.util.Enums.REQ_GET_INVOICE
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityPaymentAmountPageBinding
import com.rideincab.driver.home.MainActivity
import com.rideincab.driver.home.datamodel.InvoiceModel
import com.rideincab.driver.home.datamodel.TripInvoiceModel
import com.rideincab.driver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.home.pushnotification.Config
import com.rideincab.driver.home.pushnotification.NotificationUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
                PaymentAmountPage
Its used to get rider payment screen page function
*************************************************************** */
class PaymentAmountPage : CommonActivity(), ServiceListener {

    private lateinit var binding: ActivityPaymentAmountPageBinding

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog
    lateinit var dialog: AlertDialog

/*    @BindView(R.id.adminamountlayout)
    lateinit var adminamountlayout: RelativeLayout

    @BindView(R.id.oweamountlayout)
    lateinit var oweamountlayout: RelativeLayout

    @BindView(R.id.driverpayoutlayout)
    lateinit var driverpayoutlayout: RelativeLayout

    @BindView(R.id.cashcollectamountlayout)
    lateinit var cashcollectamountlayout: RelativeLayout

    @BindView(R.id.basefare_amount)
    lateinit var basefare_amount: TextView

    @BindView(R.id.distance_fare)
    lateinit var distance_fare: TextView

    @BindView(R.id.time_fare)
    lateinit var time_fare: TextView

    @BindView(R.id.fee)
    lateinit var fee: TextView

    @BindView(R.id.totalamount)
    lateinit var totalamount: TextView

    @BindView(R.id.total_payouts)
    lateinit var total_payouts: TextView

    @BindView(R.id.oweamount)
    lateinit var oweamount: TextView

    @BindView(R.id.driverpayout)
    lateinit var driverpayout: TextView

    @BindView(R.id.adminamount)
    lateinit var adminamount: TextView

    @BindView(R.id.rvPrice)
    lateinit var binding.rvPrice: RecyclerView*/
    
    private val priceList = ArrayList<HashMap<String, String>>()
    var payment_status: String? = null
    var payment_method: String? = null
    protected var isInternetAvailable: Boolean = false
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    internal var invoiceModels: ArrayList<InvoiceModel> = ArrayList()
    lateinit internal var addFirebaseDatabase: AddFirebaseDatabase

    private var isPaymentCompleted: Boolean = false

    private var tripInvoiceModel: TripInvoiceModel? = null
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPaymentAmountPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        AppController.getAppComponent().inject(this)
        /*common header and binding.commonButton.button text*/
        commonMethods.setButtonText(
            resources.getString(R.string.waiting_for_payment),
            binding.commonButton.button
        )
        commonMethods.setheaderText(resources.getString(R.string.paymentdetails), binding.commonHeader.headertext)
        binding.commonButton.button.setTextColor(ContextCompat.getColor(this, R.color.primary_button_text_color))
        paymentAmountPageInstance = this
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)
        addFirebaseDatabase = AddFirebaseDatabase()


        binding.rvPrice.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.rvPrice.layoutManager = layoutManager

        binding.commonButton.button.setOnClickListener {
            /*
                    *  If trip is cash payment the motorista confirm the payment
                    *
                    */
            if (binding.commonButton.button.text.toString() == resources.getString(R.string.cashcollected)) {
                /*
                    *  Update motorista cash collected in server
                    */
                isInternetAvailable = commonMethods.isOnline(applicationContext)
                if (isInternetAvailable) {
                    cashCollected()
                } else {
                    snackBar(
                        resources.getString(R.string.no_connection),
                        resources.getString(R.string.go_online),
                        false,
                        2
                    )
                }
            } else if (binding.commonButton.button.text.toString() == resources.getString(R.string.payment_done)) {
                CommonKeys.IS_ALREADY_IN_TRIP = false
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            } else {
                AddFirebaseDatabase().removeLiveTrackingNodesAfterCompletedTrip(this)
                AddFirebaseDatabase().removeNodesAfterCompletedTrip(this)
                sessionManager.clearTripID()
                sessionManager.clearTripStatus()
                val main = Intent(applicationContext, MainActivity::class.java)
                startActivity(main)
            }
        }

        println("Delete trip id : " + sessionManager.tripId)
        deleteTripDb(sessionManager.tripId!!, this)
        lookForPaymentNodeChanges()
        receivepushnotification()

    }

    private fun lookForPaymentNodeChanges() {
        addFirebaseDatabase.initPaymentChangeListener(this)
    }

    fun callGetInvoiceAPI() {
        if (isInternetAvailable) {
            /*  commonMethods.showProgressDialog(this)*/
            apiService.getInvoice(
                sessionManager.accessToken!!,
                sessionManager.tripId!!,
                sessionManager.type!!
            ).enqueue(RequestCallback(REQ_GET_INVOICE, this))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        CommonKeys.IS_ALREADY_IN_TRIP = true

        sessionManager.isDriverAndRiderAbleToChat = false
        CommonMethods.stopFirebaseChatListenerService(applicationContext)
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    /*
   *   Load price data
   */
    fun loaddata() {

        binding.rvPrice.removeAllViews()
        val adapter = PriceRecycleAdapter(this, invoiceModels)
        binding.rvPrice.adapter = adapter


        var total_fare: String? = ""



        payment_status = tripInvoiceModel!!.paymentStatus
        payment_method = tripInvoiceModel!!.paymentMode
        total_fare = tripInvoiceModel!!.totalFare

        if (CommonKeys.TripStatus.Completed == payment_status) {
            binding.commonButton.button.setTextColor(resources.getColor(R.color.primary_button_text_color))
            //   binding.commonButton.button.setTextColor(resources.getColor(R.color.ub__contact_resolved_green))
            binding.commonButton.button.setBackground(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.app_curve_button_yellow
                )
            )
            binding.commonButton.button.text = resources.getString(R.string.paid)
        }

        if (payment_method!!.contains("cash", ignoreCase = true)) {
            if (java.lang.Float.valueOf(total_fare!!) > 0) {
                //binding.commonButton.button.setBackgroundColor(resources.getColor(R.color.app_button))
                binding.commonButton.button.setTextColor(resources.getColor(R.color.primary_button_text_color))
                binding.commonButton.button.setBackground(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.app_curve_button_yellow
                    )
                )
                binding.commonButton.button.text = resources.getString(R.string.cashcollected)
                binding.commonButton.button.isEnabled = true
            } else {
                //binding.commonButton.button.setBackgroundColor(resources.getColor(R.color.black_alpha_20))
                binding.commonButton.button.setTextColor(resources.getColor(R.color.primary_button_text_color))
                binding.commonButton.button.setBackground(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.app_curve_button_yellow_disable
                    )
                )
                binding.commonButton.button.text = resources.getString(R.string.waitforrider)
                binding.commonButton.button.isEnabled = false
            }
        } else {
            //binding.commonButton.button.setBackgroundColor(resources.getColor(R.color.black_alpha_20))
            binding.commonButton.button.setTextColor(resources.getColor(R.color.primary_button_text_color))
            binding.commonButton.button.setBackground(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.app_curve_button_yellow_disable
                )
            )
            binding.commonButton.button.text = resources.getString(R.string.waitforrider)
            binding.commonButton.button.isEnabled = false
        }


    }

    /*
    *   Receive push notification
    */
    fun receivepushnotification() {

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


                    if (JSON_DATA != null) {
                        var jsonObject: JSONObject?
                        try {
                            jsonObject = JSONObject(JSON_DATA)
                            if (jsonObject.getJSONObject("custom").has("trip_payment")) {
                                isPaymentCompleted = true
                                addFirebaseDatabase.removeRequestTable()
                                sessionManager.isDriverAndRiderAbleToChat = false
                                CommonMethods.stopFirebaseChatListenerService(applicationContext)
                                //binding.commonButton.button.setBackgroundColor(getResources().getColor(R.color.button_material_dark));
                                //binding.commonButton.button.setTextColor(resources.getColor(R.color.ub__contact_resolved_green))
                                binding.commonButton.button.setTextColor(resources.getColor(R.color.primary_button_text_color))
                                /*binding.commonButton.button.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_yellow)
                                binding.commonButton.button.isEnabled = true*/
                                binding.commonButton.button.text = resources.getString(R.string.payment_done)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(
                                        context
                                    )
                                ) {
                                    showDialog(resources.getString(R.string.paymentcompleted))
                                }
                                // showDialog("Payment completed successfully");
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }


                }
            }
        }
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

    public override fun onPause() {
        super.onPause()
    }

    /*
  *    Show dialog like arrive now push notification
  */
    fun showDialog(message: String) {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.addphoto_header, null)
        val tit = view.findViewById<View>(R.id.header) as TextView
        tit.text = resources.getString(R.string.paymentcompleted)
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCustomTitle(view)
        builder.setTitle(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, which ->
                dialog.dismiss()
                /*Intent intent = new Intent(getApplicationContext(), Riderrating.class);
                    intent.putExtra("imgprofile",invoicePaymentDetail.getRiderImage());
                    startActivity(intent);*/
                CommonKeys.IS_ALREADY_IN_TRIP = false
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }

            .show()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        when (jsonResp.requestCode) {
            REQ_GET_INVOICE -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    getInvoice(jsonResp)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    //commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }

            REQ_CASH_COLLECTED -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                sessionManager.isDriverAndRiderAbleToChat = false
                CommonMethods.stopFirebaseChatListenerService(applicationContext)
                AddFirebaseDatabase().removeNodesAfterCompletedTrip(this)
                showDialog(resources.getString(R.string.paymentcompleted))
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    private fun getInvoice(jsonResponse: JsonResponse) {
        tripInvoiceModel = gson.fromJson(jsonResponse.strResponse, TripInvoiceModel::class.java)
        invoiceModels.clear()
        invoiceModels.addAll(tripInvoiceModel!!.invoice!!)
        loaddata()
    }


    /*
      *  Cash collected API called
      */
    fun cashCollected() {
        if ((!isPaymentCompleted)) {
            commonMethods.showProgressDialog(this)
            apiService.cashCollected(sessionManager.tripId!!, sessionManager.accessToken!!)
                .enqueue(RequestCallback(REQ_CASH_COLLECTED, this))
        }
    }

    /*
      *   show error or information
      */
    @SuppressLint("RestrictedApi")
    fun snackBar(message: String, buttonmessage: String, buttonvisible: Boolean, duration: Int) {
        // Create the Snackbar
        val snackbar: Snackbar
        val snackbar_background: RelativeLayout
        val snack_button: TextView
        val snack_message: TextView

        // Snack bar visible duration
        if (duration == 1)
            snackbar = Snackbar.make(binding.commonButton.button, "", Snackbar.LENGTH_INDEFINITE)
        else if (duration == 2)
            snackbar = Snackbar.make(binding.commonButton.button, "", Snackbar.LENGTH_LONG)
        else
            snackbar = Snackbar.make(binding.commonButton.button, "", Snackbar.LENGTH_SHORT)

        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        val textView =
            layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val snackView = layoutInflater.inflate(R.layout.snackbar, null)
        // Configure the view

        snackbar_background = snackView.findViewById<View>(R.id.snackbar) as RelativeLayout
        snack_button = snackView.findViewById<View>(R.id.snack_button) as TextView
        snack_message = snackView.findViewById<View>(R.id.snackbar_text) as TextView

        snackbar_background.setBackgroundColor(resources.getColor(R.color.textblack)) // Background Color

        if (buttonvisible)
        // set Right side binding.commonButton.button visible or gone
            snack_button.visibility = View.VISIBLE
        else
            snack_button.visibility = View.GONE

        snack_button.setTextColor(resources.getColor(R.color.ub__ui_core_warning)) // set right side binding.commonButton.button text color
        snack_button.text = buttonmessage // set right side binding.commonButton.button text


        snack_message.setTextColor(resources.getColor(R.color.white)) // set left side main message text color
        snack_message.text = message  // set left side main message text

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(resources.getColor(R.color.textblack))
        snackbar.show()


    }

    companion object {
        lateinit var paymentAmountPageInstance: PaymentAmountPage

        fun deleteTripDb(tripID: String, context: Context) {
            try {
                val root =
                    FirebaseDatabase.getInstance().reference.child(context.getString(R.string.real_time_db))
                        .child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(tripID)
                root.removeValue()
                println("Trip ID Removed  : $tripID")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


}
