package com.rideincab.user.taxi.sendrequest

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage sendrequest
 * @category DriverRatingActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.CommonMethods.Companion.DebuggableLogD
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityDriverratingBinding
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


/* ************************************************************
    Rider give the rate and comments for driver
    *************************************************************** */
class DriverRatingActivity : CommonActivity(), ServiceListener {

    private lateinit var binding: AppActivityDriverratingBinding
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

    protected var isInternetAvailable: Boolean = false
    private var driverTipsAmount: String = ""

/*    @BindView(R.id.rider_rating)
    lateinit var binding.riderRating: RatingBar

    @BindView(R.id.rider_comments)
    lateinit var binding.riderComments: TextView

    @BindView(R.id.binding.tvTipText)
    lateinit var binding.tvTipText: TextView

    @BindView(R.id.binding.tvSkip)
    lateinit var binding.tvSkip: TextView

    @BindView(R.id.binding.imgvTip)
    lateinit var binding.imgvTip: ImageView

    @BindView(R.id.binding.rlDriverTip)
    lateinit var binding.rlDriverTip: RelativeLayout

    @BindView(R.id.common_profile)
    lateinit var binding.commonProfile: View

    @BindView(R.id.profile_image1)
    lateinit var profileImage: ImageView

    @BindView(R.id.tv_covid_feature)
    lateinit var binding.tvCovidFeature: TextView*/


    internal var tv_tripAmountsymbol: TextView? = null
    internal var ed_trip_amount: EditText? = null
    internal var setTripAmount: Button? = null
    internal var trip_layout_close: ImageView? = null
    private var ratingRate: Float = 0.0f
    lateinit var profile_image1: ImageView
    lateinit var bottomSheetDialog: BottomSheetDialog

    internal lateinit var user_thumb_image: String

    fun img_close() {
        if (istripamount) {
            binding.tvTipText.text = resources.getString(R.string.add_tip)
            driverTipsAmount = null.toString()
            println("driverTipsAmount" + driverTipsAmount)
            //   binding.tvTipText.setTextColor(getResources().getColor(R.color.white));
            binding.imgvTip.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_ic_tip))
            binding.rlDriverTip.background =
                ContextCompat.getDrawable(this, R.drawable.app_curve_button_black)
            istripamount = false
        }
    }


    fun submit() {  // Rating submit binding.rateSubmit
        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        } else {
            sessionManager.clearDriverNameRatingAndProfilePicture()
            //val rating = binding.riderRating.rating
            val ratingstr = ratingRate.toString()
            DebuggableLogD("OUTPUT REBAI", ratingstr)
            if ("0" != ratingstr) {
                var ridercomment = binding.riderComments.text.toString()
                ridercomment = ridercomment.replace("[\\t\\n\\r]".toRegex(), " ")
                sessionManager.isrequest = false
                sessionManager.isTrip = false
                submitRating(ratingRate, ridercomment)
            } else {
                commonMethods.showMessage(
                    this,
                    dialog,
                    resources.getString(R.string.error_msg_rating)
                )
                DebuggableLogD("OUTPUT REBAI", "error_msg_rating")
            }
        }
    }

    public fun skipRating() {
        if (isInternetAvailable) {
            sessionManager.clearDriverNameRatingAndProfilePicture()
            var ridercomment = binding.riderComments.text.toString()
            ridercomment = ridercomment.replace("[\\t\\n\\r]".toRegex(), " ")
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            submitRating(ratingRate, ridercomment)
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.no_connection))
        }
    }

    /*@OnClick(R.id.tvskip)
    public void skip() {
        onBackPressed();
    }*/


    private var mLastClickTime: Long = 0


    fun skip() {  //botton layout for add a trip amount
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        openBottomSheetToEnterTipsAmount()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityDriverratingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        AppController.appComponent.inject(this)
        binding.root.applySystemBarsPadding()
        isInternetAvailable = commonMethods.isOnline(applicationContext)
        dialog = commonMethods.getAlertDialog(this)

        //val laydir = resources.getString(R.string.layout_direction)
        /*if ("1" == laydir) {
            binding.riderRating.gravity = SimpleRatingBar.Gravity.Right
        }*/
        // Get Driver profile image
        val intent = intent
        if (intent.getStringExtra("imgprofile") != null && !TextUtils.isEmpty(
                intent.getStringExtra(
                    "imgprofile"
                )
            )
        ) {
            user_thumb_image = intent.getStringExtra("imgprofile")!!
        } else {
            user_thumb_image = sessionManager.driverProfilePic.toString()
        }
        Picasso.get().load(user_thumb_image)
            .into(binding.commonProfile.profileImage1)

        binding.riderComments.clearFocus()
        binding.tvTipText.text = resources.getString(R.string.add_tip)

        if (sessionManager.isCovidFeature!!) {
            binding.tvCovidFeature.visibility = View.VISIBLE
        } else {
            binding.tvCovidFeature.visibility = View.GONE
        }

        binding.rateSubmit.isEnabled = false
        binding.rateSubmit.background =
            ContextCompat.getDrawable(this, R.drawable.app_curve_button_yellow_disable)

        binding.riderRating.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                ratingRate = rating
                if (rating >= 1) {
                    binding.rateSubmit.isEnabled = true
                    binding.rateSubmit.background = ContextCompat.getDrawable(
                        this@DriverRatingActivity,
                        R.drawable.app_curve_button_yellow
                    )
                } else {
                    binding.rateSubmit.isEnabled = false
                    binding.rateSubmit.background = ContextCompat.getDrawable(
                        this@DriverRatingActivity,
                        R.drawable.app_curve_button_yellow_disable
                    )
                }
            }
        }

        binding.rlDriverTip.setOnClickListener { img_close() }
        binding.rlDriverTip.setOnClickListener { skip() }
        binding.rateSubmit.setOnClickListener { submit() }
        binding.tvSkip.setOnClickListener { skipRating() }


    }

    /**
     * Submit rating API called
     */
    fun submitRating(rating: Float, ridercomment: String) {
        commonMethods.showProgressDialog(this)
        val tripId: String
        if (intent.getIntExtra("back", 0) == 1) {
            tripId = intent.getStringExtra("tripid").toString()
        } else {
            tripId = sessionManager.tripId.toString()
        }
        apiService.tripRating(
            sessionManager.accessToken.toString(),
            tripId,
            rating.toString(),
            ridercomment,
            sessionManager.type.toString(),
            driverTipsAmount!!
        ).enqueue(RequestCallback(this))

    }

    override fun onResume() {
        super.onResume()
        CommonKeys.isEndTrip = false
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            onSuccessRating(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
//            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
//            commonMethods.hideProgressDialog()
            byPassRating()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
//            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
//            commonMethods.hideProgressDialog()
            byPassRating()
        }
    }

    private fun byPassRating() {
        commonMethods.hideProgressDialog()
        sessionManager.isrequest = false
        sessionManager.isTrip = false
//        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)
        finish()
    }


    fun onSuccessRating(jsonResp: JsonResponse) {
        //binding.riderRating.rating = 0f
        binding.riderComments.setText("") 
        commonMethods.hideProgressDialog()
        if (jsonResp.statusCode == "1") {

            sessionManager.isrequest = false
            sessionManager.isTrip = false

            try {
                val response = JSONObject(jsonResp.strResponse)
                if (response.has("promo_details")) {
                    val promocount = response.getJSONArray("promo_details").length()
                    sessionManager.promoDetail = response.getString("promo_details")
                    sessionManager.promoCount = promocount
                }
            } catch (j: JSONException) {
                j.printStackTrace()
            }

            //Bundle bundle = new Bundle();
            //bundle.putSerializable("invoiceModels", invoiceModels);
            val main = Intent(this, PaymentAmountPage::class.java)
            //main.putExtra("AmountDetails", jsonResp.getStrResponse().toString());
            //main.putExtras(bundle);
            startActivity(main)
            /*sessionManager.setIsrequest(false);
            sessionManager.setIsTrip(false);
            sessionManager.setDriverAndRiderAbleToChat(false);
            CommonMethods.stopFirebaseChatListenerService(this);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
            finish()

        } else if (jsonResp.statusCode == "2") {
            commonMethods.hideProgressDialog()
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            val main = Intent(this, MainActivity::class.java)
            startActivity(main)
            finish()
        }
    }

    override fun onBackPressed() {
        if (intent.getIntExtra("back", 0) == 1) {
            super.onBackPressed()
        } else {
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            sessionManager.isDriverAndRiderAbleToChat = false
            CommonMethods.stopFirebaseChatListenerService(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    fun openBottomSheetToEnterTipsAmount() {

        val tv_Currency_symbol: TextView
        val ed_trip_amount: EditText
        val btn_setTrip_amount: Button
        val imgv_layout_close: ImageView
        //val view = layoutInflater.inflate(R.layout.app_bottomsheet_tip_amount, null)

        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.app_bottomsheet_tip_amount)
        tv_Currency_symbol = bottomSheetDialog.findViewById(R.id.tv_currency_symbol)!!
        ed_trip_amount = bottomSheetDialog.findViewById(R.id.ed_tripAmount)!!
        btn_setTrip_amount = bottomSheetDialog.findViewById(R.id.btn_setTripAmount)!!
        imgv_layout_close = bottomSheetDialog.findViewById(R.id.imgv_close_tips_popup)!!
        println("getCurrencySymbol" + sessionManager.currencySymbol + "")
        tv_Currency_symbol.text = sessionManager.currencySymbol
        ed_trip_amount.isFocusable = true
        ed_trip_amount.requestFocus()

        ed_trip_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().startsWith(".")) {
                    ed_trip_amount.setText("0.")
                    ed_trip_amount.setSelection(ed_trip_amount.text.toString().length)
                }
            }
        })

        imgv_layout_close.setOnClickListener {
            bottomSheetDialog.cancel()
            overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
        }
        btn_setTrip_amount.setOnClickListener {
            val regexStr = "\\d+(?:\\.\\d+)?"
            if (!ed_trip_amount.text.toString().trim { it <= ' ' }
                    .isEmpty() && ed_trip_amount.text.toString().trim { it <= ' ' }
                    .matches(regexStr.toRegex())) {
                bottomSheetDialog.cancel()
                driverTipsAmount = ed_trip_amount.text.toString().trim { it <= ' ' }
                istripamount = true
                binding.imgvTip.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_close_round
                    )
                )

                binding.tvTipText.text = resources.getString(
                    R.string.add_tip_amt,
                    sessionManager.currencySymbol + driverTipsAmount
                )
                binding.rlDriverTip.background =
                    ContextCompat.getDrawable(this, R.drawable.filled_tip_background)
                println("driverTipsAmount" + driverTipsAmount)
            } else {
                Toast.makeText(this@DriverRatingActivity, "Please enter amount", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        /*Button confirm = (Button) dialog.findViewById(R.id.signup_cancel_confirm);
        Button cancel = (Button) dialog.findViewById(R.id.signup_cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right);
            }
        });*/
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }
    }

    companion object {
        private var istripamount = false
    }

}
