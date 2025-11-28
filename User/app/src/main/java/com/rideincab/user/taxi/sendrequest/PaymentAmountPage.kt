package com.rideincab.user.taxi.sendrequest

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage sendrequest
 * @category PaymentAmountPage
 * @author SMR IT Solutions
 *
 */

//import com.paypal.android.sdk.payments.*
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.model.StripeIntent
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.helper.Constants.Payment
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.pushnotification.Config
import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonKeys.REQUEST_CODE_PAYMENT
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideincab.user.common.utils.Enums.REQ_AFTER_PAY
import com.rideincab.user.common.utils.Enums.REQ_CURRENCY_CONVERT
import com.rideincab.user.common.utils.Enums.REQ_GET_INVOICE
import com.rideincab.user.common.utils.Enums.REQ_PAYPAL_CURRENCY
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.PaymentWebViewActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityPaymentAmountPageBinding
import com.rideincab.user.taxi.adapters.PriceRecycleAdapter
import com.rideincab.user.taxi.database.AddFirebaseDatabase
import com.rideincab.user.taxi.datamodels.trip.InvoiceModel
import com.rideincab.user.taxi.datamodels.trip.TripInvoiceModel
import com.rideincab.user.taxi.sidebar.payment.PaymentPage
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

/* ************************************************************
    After trip completed driver can send payment
    *************************************************************** */
class PaymentAmountPage : CommonActivity(), ServiceListener {

    private lateinit var binding:AppActivityPaymentAmountPageBinding
    
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

/*    @BindView(R.id.btnSubmit)
    lateinit var binding.btnSubmit: Button

    @BindView(R.id.binding.walletImg)
    lateinit var binding.walletImg: ImageView

    @BindView(R.id.binding.paymentmethodImg)
    lateinit var binding.paymentmethodImg: ImageView  // Payment method image

    @BindView(R.id.binding.paymentmethodType)
    lateinit var binding.paymentmethodType: TextView// Payment method type show in text view

    @BindView(R.id.rvPrice)
    lateinit var binding.rvPrice: RecyclerView

    @BindView(R.id.paymentmethod)
    lateinit var binding.paymentmethod: RelativeLayout*/

    var payable_amt = 0f
    lateinit var total_fare: String
    lateinit var paypal_app_id: String
    lateinit var driver_payout: String
    lateinit var walletType: String
    lateinit var payment_method: String
    protected var isInternetAvailable: Boolean = false
    private var isBrainTree: Boolean = false
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    internal var invoiceModels: ArrayList<InvoiceModel>? = ArrayList()

    // lateinit var config: PayPalConfiguration

    private var statusCode: String? = null

    private var tripInvoiceModel: TripInvoiceModel? = null

    private val REQUEST_CODE = 101
    private var nonce = ""


    fun rateSubmit() {
        if (binding.btnSubmit.text.toString() == resources.getString(R.string.pay)) {
            if (sessionManager.payementModeWebView!!) {
                val webview = Intent(this, PaymentWebViewActivity::class.java)
                webview.putExtra("payableWalletAmount", payable_amt.toString())
                webview.putExtra(Constants.INTENT_PAY_FOR, CommonKeys.PAY_FOR_TRIP)
                webview.putExtra(Constants.PAY_FOR_TYPE, CommonKeys.AFTER_PAYMENT)
                startActivityForResult(webview, Payment)
            } else {
                if (payment_method.contains(CommonKeys.PAYMENT_CARD, ignoreCase = true)) {
                    afterPayment("")
                } else {
                    currencyConversion(payable_amt.toString())
                }
            }
        } else if (binding.btnSubmit.text.toString() == resources.getString(R.string.proceed)) {
            paymentCompleted("")
        }


    }


    fun paymentchange() {
        gotoPaymentpage()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=AppActivityPaymentAmountPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.paymentdetails),binding.commonHeader.tvHeadertext)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        binding.rvPrice.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.rvPrice.layoutManager = layoutManager

        // Receive push notification
        receivepushnotification()

        binding.commonHeader.back.setOnClickListener { super.onBackPressed() }
        binding.btnSubmit.setOnClickListener { rateSubmit() }
        binding.paymentmethodChange.setOnClickListener { gotoPaymentpage() }
    }

    /**
     * Convert Currency For BrainTree
     *
     * @param payableWalletAmount
     */
    protected fun currencyConversion(payableWalletAmount: String) {
        //commonMethods.showProgressDialogPaypal(this,customDialog,resources.getString(R.string.few_seconds))
        commonMethods.showProgressDialog(this)
        apiService.currencyConversion(payableWalletAmount, sessionManager.accessToken.toString(), sessionManager.paymentMethodkey!!.lowercase()).enqueue(RequestCallback(REQ_CURRENCY_CONVERT, this))
    }


    fun callBrainTreeUI(payableAmount: String) {
        /*val dropInRequest = DropInRequest()
                .requestThreeDSecureVerification(true)
                .threeDSecureRequest(commonMethods.threeDSecureRequest(payableAmount))
                .clientToken(sessionManager.brainTreeClientToken)
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE)*/
    }


    /**
     * To get nounce from brain tree paypal
     */

    /* override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce) {

         //commonMethods.hideProgressDialog();
         val nonce: String = paymentMethodNonce.nonce
         afterPayment(nonce)
     }*/

    /**
     * Load fare details for current trip
     */
    fun loadData() {
        val adapter = PriceRecycleAdapter(this, invoiceModels!!)
        binding.rvPrice.adapter = adapter
        adapter.notifyDataSetChanged()

        payable_amt = java.lang.Float.valueOf(tripInvoiceModel!!.totalFare)
        total_fare = tripInvoiceModel!!.totalFare
        paypal_app_id = tripInvoiceModel!!.paypalAppId

        driver_payout = tripInvoiceModel!!.driverPayout
        payment_method = tripInvoiceModel!!.paymentMode
        if (payable_amt <= 0) {
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_yellow)// Check payable amount is available or not ( sometimes wallet and promo to pay full trip amount that time payable amount is 0)
            binding.btnSubmit.text = resources.getString(R.string.proceed) // if payable amount is 0 then directly we can proceed to next step
        } else {
            if (payment_method.contains(CommonKeys.PAYMENT_CASH, ignoreCase = true)) {                           // if payment method is cashLayout then driver confirm the driver other wise rider pay the amount to another payment option
                binding.btnSubmit.isEnabled = false
                binding.btnSubmit.text = resources.getString(R.string.waitfordriver)
                binding.btnSubmit.setTextSize(15F)
                binding.btnSubmit.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_yellow_disable)
                ///binding.btnSubmit.setBackgroundColor(ContextCompat.getColor(this, R.color.cabme_app_black))
            } else {
                binding.btnSubmit.visibility = View.VISIBLE
                binding.btnSubmit.text = resources.getString(R.string.pay) // Rider pay button
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_yellow)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        mBraintreeFragment?.removeListener(this)

    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Back button pressed
     */
    override fun onBackPressed() {
        if (intent.getIntExtra("isBack", 0) == 1) {
            super.onBackPressed()
        } else {
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            CommonKeys.IS_ALREADY_IN_TRIP = true
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Update Payment API called
     */
    fun changeCurrencyPayment(paypalamount: String) {
        commonMethods.showProgressDialog(this)
        apiService.paypalCurrency(sessionManager.accessToken.toString(),
            sessionManager.currencyCode.toString(),
            paypalamount).enqueue(RequestCallback(REQ_PAYPAL_CURRENCY, this))

    }

    /**
     * Update Payment API called
     */
    fun afterPayment(payKey: String) {
        commonMethods.showProgressDialog(this)
        apiService.proceedAfterPayment(getParamsForPaypalAndStripe(payKey)).enqueue(RequestCallback(REQ_AFTER_PAY, this))
    }

    /**
     * LinkedHashMap For Stripe and PayPal
     */
    private fun getParamsForPaypalAndStripe(paykey: String): LinkedHashMap<String, String> {
        println("paymentMethod ${sessionManager.paymentMethodkey}")
        val params = LinkedHashMap<String, String>()
        params["token"] = sessionManager.accessToken.toString()
        params["trip_id"] = sessionManager.tripId.toString()
        params["payment_type"] = sessionManager.paymentMethodkey.toString()
        params["amount"] = payable_amt.toString()
        params["pay_key"] = paykey
        return params

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        val statuscode = commonMethods.getJsonValue(jsonResp.strResponse, "status_code", String::class.java) as String
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_GET_INVOICE -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    getInvoice(jsonResp)
                } else if (jsonResp.statusCode.equals("2", ignoreCase = true)) {
                    this.onBackPressed()
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.hideProgressDialog()
                }
            }
            REQ_CURRENCY_CONVERT -> if (jsonResp.isSuccess) {
                val amount = commonMethods.getJsonValue(jsonResp.strResponse, "amount", String::class.java) as String
                if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_PAYPAL)) {

                    //commonMethods.showProgressDialogPaypal(this,customDialog,resources.getString(R.string.few_seconds))
                    commonMethods.showProgressDialog(this)
                    val mAuthorization: String = commonMethods.getJsonValue(jsonResp.strResponse, "braintree_clientToken", String::class.java) as String

                    try {
//                        mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization)

                    } catch (e: Exception) {

                    }
                    // mBraintreeFragment is ready to use!

//                    mBraintreeFragment?.addListener(this)


                    val currency_code = commonMethods.getJsonValue(jsonResp.strResponse, "currency_code", String::class.java) as String

                    setupBraintreeAndStartExpressCheckout(amount, currency_code)

                    //payment(amount, currency_code,sessionManager.paypal_mode,sessionManager.paypal_app_id);
                } else if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_BRAINTREE)) {
                    sessionManager.brainTreeClientToken = commonMethods.getJsonValue(jsonResp.strResponse, "braintree_clientToken", String::class.java) as String
                    callBrainTreeUI(amount)
                }
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_AFTER_PAY -> if (jsonResp.isSuccess) {
                if (statuscode.equals("2")) run {
                    commonMethods.showProgressDialog(this@PaymentAmountPage)
                    commonMethods.getClientSecret(jsonResp, this)
                } else {
                    CommonMethods.stopFirebaseChatListenerService(this)
                    commonMethods.removeLiveTrackingNodesAfterCompletedTrip(this)
                    commonMethods.removeTripNodesAfterCompletedTrip(this)
                    sessionManager.clearTripID()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    statusDialog(resources.getString(R.string.paymentcompleted))
                }

            } else if (jsonResp.statusCode.equals("2")) run {
                commonMethods.showProgressDialog(this@PaymentAmountPage)
                commonMethods.getClientSecret(jsonResp, this)
            } else {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                DebuggableLogV("jsonResp.getStatusMsg()", "" + jsonResp.statusMsg)
            }
            else -> {
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
        }
    }


    fun setupBraintreeAndStartExpressCheckout(amount: String, currencycode: String) {
        /*val request = PayPalRequest(amount)
                .currencyCode(currencycode)
                .intent(PayPalRequest.INTENT_SALE)
        PayPal.requestOneTimePayment(mBraintreeFragment, request)*/
    }


    /**
     * Get Invoice Response
     */
    private fun getInvoice(jsonResponse: JsonResponse) {
        if (jsonResponse.statusCode.equals("2", ignoreCase = true)) {
            CommonMethods.gotoMainActivityWithoutHistory(this@PaymentAmountPage)
            finish()
        } else if (jsonResponse.isSuccess) {
            tripInvoiceModel = gson.fromJson(jsonResponse.strResponse, TripInvoiceModel::class.java)
            if (invoiceModels != null)
                invoiceModels!!.clear()
            invoiceModels!!.addAll(tripInvoiceModel!!.invoice)
            loadData()
            binding.btnSubmit.visibility = View.VISIBLE
        }
    }

    /**
     * Paypal button clicked
     */
    fun payPalButtonClick() {
        var paypalamount = ""
        if (payable_amt > 0) {
            paypalamount = payable_amt.toString()
        } else {
            paypalamount = total_fare
        }


        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (isInternetAvailable) {
            //new Updatepayment().execute(url,"currency");
            changeCurrencyPayment(paypalamount)
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        }
    }

    /**
     * Paypal payable amount set
     */
    /*private fun payment(amount: String, currencycode: String, paypal_mode: Int, paypal_app_id: String) {
        *//*
            * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
            * Change PAYMENT_INTENT_SALE to
            *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
            *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
            *     later via calls from your server.
            *
            * Also, to include additional payment details and an item list, see getStuffToBuy() below.*//*


        paypalCredentialsUpdate(paypal_mode, paypal_app_id)


        val thingToBuy: PayPalPayment = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE, amount, currencycode)
        //PayPalPayment thingToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        *//*  * See getStuffToBuy(..) for examples of some available payment options.*//*


        //addAppProvidedShippingAddress(thingToBuy);   /// Add shipping address
        //enableShippingAddressRetrieval(thingToBuy,true);  //  Enable retrieval of shipping addresses from buyer's PayPal account

        intent = Intent(this, PaymentActivity::class.java)

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy)

        startActivityForResult(intent, REQUEST_CODE_PAYMENT)
    }

    *//* * Setup Paypal Credentials
     *
     * @param paypal_mode
     * @param paypal_app_id
     **//*
    private fun paypalCredentialsUpdate(paypal_mode: Int, paypal_app_id: String) {

        lateinit var CONFIG_ENVIRONMENT: String
        if (0 == paypal_mode) {
            CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX
        } else {
            CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION
        }
        // note that these credentials will differ between live & sandbox environments.
        config = PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(paypal_app_id)
                // The following are only used in PayPalFuturePaymentActivity.
                .merchantName(resources.getString(R.string.app_name))
                .merchantPrivacyPolicyUri(Uri.parse(CommonKeys.PrivacyURL))
                .merchantUserAgreementUri(Uri.parse(CommonKeys.termPolicyUrl))

        config.acceptCreditCards(false)
        // Call payal service
        intent = Intent(this, PayPalService::class.java)
        var servicerRunningCheck: Boolean = isMyServiceRunning(PayPalService::class.java)

        System.out.println("Service Running check : " + servicerRunningCheck)
        if (servicerRunningCheck) {
            stopService(intent)
        }
        servicerRunningCheck = isMyServiceRunning(PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        startService(intent)
    }

    private fun getThingToBuy(paymentIntent: String, amount: String, currencycode: String): PayPalPayment {

        return PayPalPayment(BigDecimal(amount), currencycode, resources.getString(R.string.payment_name),
                paymentIntent)
    }*/


    /**
     * show dialog for payment completed
     */
    fun statusDialog(message: String) {

        try {
            if (!this.isFinishing) {
                val inflater = layoutInflater
                val view = inflater.inflate(R.layout.addphoto_header, null)
                val tit = view.findViewById<View>(R.id.header) as TextView
                tit.text = resources.getString(R.string.paymentcompleted)
                val builder = android.app.AlertDialog.Builder(this)
                builder.setCustomTitle(view)
                builder.setTitle(message)
                    .setCancelable(false)
                    //.setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton(resources.getString(R.string.ok_c)) { dialog, _ ->
                        dialog.dismiss()
                        sessionManager.isrequest = false
                        sessionManager.isTrip = false
                        if (CommonKeys.IS_ALREADY_IN_TRIP) {
                            CommonKeys.IS_ALREADY_IN_TRIP = false
                        }
                        /*Intent main = new Intent(getApplicationContext(), DriverRatingActivity.class);
                        main.putExtra("imgprofile",invoicePaymentDetail.getDriverImage());
                        startActivity(main);*/
                        CommonMethods.gotoMainActivityWithoutHistory(this@PaymentAmountPage)
                        finish()
                    }

                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*   if(dialog.isShowing)
           {
               dialog.dismiss()
           }*/

    }


    /**
     * Check payment completed or not
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {/*
            if (resultCode == Activity.RESULT_OK) {
                val confirm: PaymentConfirmation? = data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)

                if (confirm != null) {
                    try {
                        DebuggableLogI(TAG, confirm.toJSONObject().toString(4))
                        DebuggableLogI(TAG, confirm.toJSONObject().getJSONObject("response").get("id").toString())
                        DebuggableLogI(TAG, confirm.payment.toJSONObject().toString(4))
                        */
            /**
             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
             * or consent completion.
             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
             * for more details.
             *
             * For sample mobile backend interactions, see
             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
             *//*
                        paymentCompleted(confirm.toJSONObject().getJSONObject("response").get("id").toString())
                        // displayResultText("PaymentConfirmation info received from PayPal");

                    } catch (e: JSONException) {
                        DebuggableLogE(TAG, "an extremely unlikely failure occurred: ", e)
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebuggableLogI(TAG, "The user canceled.")
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                DebuggableLogI(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.")
            }
        */
        } else if (requestCode == Payment) {
            if (data != null) {
                val statusCode = data.extras?.getString("status_code")
                val message = data.extras?.getString("status_message")
                if (statusCode.equals("1", true)) {
                    CommonMethods.stopFirebaseChatListenerService(this)
                    commonMethods.removeLiveTrackingNodesAfterCompletedTrip(this)
                    commonMethods.removeTripNodesAfterCompletedTrip(this)
                    sessionManager.clearTripID()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    statusDialog(message!!)
                } else {
                    commonMethods.showMessage(this, dialog, message!!)
                }
            }
        } else if (requestCode == REQUEST_CODE) {
            /* if (data != null) {
                 commonMethods.hideProgressDialog()
                 try {
                     val result = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                     nonce = result?.paymentMethodNonce!!.nonce
                     isBrainTree = true
                     afterPayment(nonce)
                 } catch (e: Exception) {
                     Toast.makeText(this, resources.getString(R.string.internal_server_error), Toast.LENGTH_LONG).show()
                     e.printStackTrace()
                 }

             }*/
        } else {
            if (data != null) {
                commonMethods.hideProgressDialog()
                commonMethods.stripeInstance()!!.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
                    override fun onError(e: Exception) {
                        e.printStackTrace()
                    }

                    override fun onSuccess(result: PaymentIntentResult) {
                        val paymentIntent = result.intent
                        if (paymentIntent.status == StripeIntent.Status.Succeeded) {
                            afterPayment(paymentIntent.id!!)
                        } else {
                            commonMethods.showMessage(this@PaymentAmountPage, dialog, "Failed")
                        }
                    }

                })
            }
        }
    }


    /**
     * Paypal payment completed to update transaction id to server
     */
    fun paymentCompleted(payKey: String) {
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        if (isInternetAvailable) {
            afterPayment(payKey)
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        }
    }

    /**
     * Receive push notification for payment completed
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

                    //String message = intent.getStringExtra("message");

                    val JSON_DATA = sessionManager.pushJson


                    var jsonObject: JSONObject?
                    try {
                        jsonObject = JSONObject(JSON_DATA)
                        if (jsonObject.getJSONObject("custom").has("trip_payment")) {
                            //statusDialog(resources.getString(R.string.paymentcompleted))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }


                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        //payment method image settings
        /* if (sessionManager.paymentMethod == "") {
             sessionManager.paymentMethod = CommonKeys.PAYMENT_PAYPAL
             sessionManager.isWallet = true
         }
 */

        commonMethods.dismissDialog()
        if (sessionManager.promoCount > 0) {

            if (sessionManager.paymentMethodkey.equals("") || sessionManager.paymentMethodkey!!.isEmpty()) {
                binding.paymentmethodType.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_green)
                binding.paymentmethodType.text = resources.getString(R.string.promo_applied)
                binding.paymentmethodType.gravity = Gravity.CENTER
                binding.paymentmethodType.setTextColor(ContextCompat.getColor(this,R.color.cabme_app_white))
                binding.paymentmethodType.isAllCaps = false
                binding.paymentmethodImg.visibility = View.GONE
            } else {

                binding.paymentmethodType.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_green)
                binding.paymentmethodType.text = resources.getString(R.string.promo_applied)
                binding.paymentmethodType.gravity = Gravity.CENTER
                binding.paymentmethodType.setTextColor(ContextCompat.getColor(this,R.color.cabme_app_white))
                binding.paymentmethodType.isAllCaps = false
                binding.paymentmethodImg.visibility = View.VISIBLE
                Picasso.get().load(sessionManager.paymentMethodImage).into(binding.paymentmethodImg)
            }
            /* if (sessionManager.paymentMethod == CommonKeys.PAYMENT_CASH) {
                 binding.paymentmethodImg.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.cash))
                 binding.paymentmethodType.background = ContextCompat.getDrawable(this,R.drawable.d_green_fillboarder)
                 binding.paymentmethodType.setTextColor(ContextCompat.getColor(this,R.color.white))
                 binding.paymentmethodType.text = resources.getString(R.string.promo_applied)
                 binding.paymentmethodType.isAllCaps = false
             } else if (sessionManager.paymentMethod == CommonKeys.PAYMENT_CARD) {
                 binding.paymentmethodImg.setImageDrawable(CommonMethods.getCardImage(sessionManager.cardBrand, resources))
                 binding.paymentmethodType.background = ContextCompat.getDrawable(this,R.drawable.d_green_fillboarder)
                 binding.paymentmethodType.setTextColor(ContextCompat.getColor(this,R.color.white))
                 binding.paymentmethodType.text = resources.getString(R.string.promo_applied)
                 binding.paymentmethodType.isAllCaps = false

             } else if (sessionManager.paymentMethod == CommonKeys.PAYMENT_BRAINTREE) {
                 binding.paymentmethodImg.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.online_payment))
                 binding.paymentmethodType.background = ContextCompat.getDrawable(this,R.drawable.d_green_fillboarder)
                 binding.paymentmethodType.setTextColor(ContextCompat.getColor(this,R.color.white))
                 binding.paymentmethodType.text = resources.getString(R.string.promo_applied)
                 binding.paymentmethodType.isAllCaps = false

             } else {
                 binding.paymentmethodImg.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.paypal))
                 binding.paymentmethodType.background = ContextCompat.getDrawable(this,R.drawable.d_green_fillboarder)
                 binding.paymentmethodType.setTextColor(ContextCompat.getColor(this,R.color.white))
                 binding.paymentmethodType.isAllCaps = false
                 binding.paymentmethodType.text = resources.getString(R.string.promo_applied)
             }*/

        } else {
            if (sessionManager.paymentMethodkey.equals("") || sessionManager.paymentMethodkey!!.isEmpty()) {
                binding.paymentmethodType.text = resources.getString(R.string.Add_payment_type)
                binding.paymentmethodImg.visibility = View.GONE
            } else {
                if (sessionManager.paymentMethodkey == CommonKeys.PAYMENT_CARD) {
                    if (sessionManager.cardValue != "") {
                        binding.paymentmethodType.text = "•••• " + sessionManager.cardValue
                    } else {
                        binding.paymentmethodType.text = resources.getString(R.string.card)
                    }
                    binding.paymentmethodImg.setImageResource(R.drawable.app_ic_card)
                } else {
                    binding.paymentmethodType.text = sessionManager.paymentMethod
                    Picasso.get().load(sessionManager.paymentMethodImage).into(binding.paymentmethodImg)
                }
                binding.paymentmethodImg.visibility = View.VISIBLE
            }
        }

        //card selected
        if (sessionManager.isWallet) {
            DebuggableLogV("isWallet", "iswallet" + sessionManager.walletAmount)
            //iswallet="Yes";
            if ("" != sessionManager.walletAmount) {
                if (java.lang.Float.valueOf(sessionManager.walletAmount) > 0)
                    binding.walletImg.visibility = View.VISIBLE
                else
                    binding.walletImg.visibility = View.GONE
            }

        } else {
            //iswallet="No";
            binding.walletImg.visibility = View.GONE
        }

        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
            IntentFilter(Config.REGISTRATION_COMPLETE))

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
            IntentFilter(Config.PUSH_NOTIFICATION))

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)

        if (isInternetAvailable && !isBrainTree) {
            commonMethods.showProgressDialog(this)
            println("Get Wallet type" + sessionManager.isWallet)
            if (sessionManager.isWallet) {
                walletType = "Yes"
            } else {
                walletType = "No"
            }
            AddFirebaseDatabase().updatePaymentChangedNode(walletType, sessionManager.paymentMethodkey!!, this)
            apiService.getInvoice(walletType, sessionManager.paymentMethodkey!!, sessionManager.accessToken!!, sessionManager.tripId!!, sessionManager.type!!).enqueue(RequestCallback(REQ_GET_INVOICE, this))
        }
        /*if (dialog.isShowing) {
            dialog.dismiss()
        }*/


        Handler().postDelayed({
            commonMethods.hideProgressDialog()
        }, 3000)

        if (CommonKeys.isPaymentOrCancel) {
            CommonKeys.isPaymentOrCancel = false
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            if (CommonKeys.IS_ALREADY_IN_TRIP) {
                CommonKeys.IS_ALREADY_IN_TRIP = false
            }
            /*Intent main = new Intent(getApplicationContext(), DriverRatingActivity.class);
            main.putExtra("imgprofile",invoicePaymentDetail.getDriverImage());
            startActivity(main);*/
            CommonMethods.gotoMainActivityWithoutHistory(this@PaymentAmountPage)
            finish()
        }


    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver!!)
    }

    //Change payment mode
    fun gotoPaymentpage() {
        if (commonMethods.isOnline(this)) {
            val intent = Intent(this, PaymentPage::class.java)
            intent.putExtra(CommonKeys.TYPE_INTENT_ARGUMENT_KEY, CommonKeys.StatusCode.startPaymentActivityForView)
            startActivityForResult(intent, CommonKeys.ChangePaymentOpetionBeforeRequestCarApi)
            overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        } else {
            CommonMethods.showUserMessage(resources.getString(R.string.turnoninternet))
        }
    }

    companion object {

        private val TAG = "paymentExample"
    }
}