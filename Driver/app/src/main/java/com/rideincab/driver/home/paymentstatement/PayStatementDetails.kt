package com.rideincab.driver.home.paymentstatement

/**
 * @package com.cloneappsolutions.cabmedriver.home.paymentstatement
 * @subpackage paymentstatement model
 * @category PayStatementDetails
 * @author SMR IT Solutions
 *
 */

import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.custompalette.FontTextView
import com.rideincab.driver.common.database.Sqlite
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums.REQ_WEEKLY_STATEMENT
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityPaystatementDetailsBinding
import com.rideincab.driver.home.datamodel.InvoiceContent
import com.rideincab.driver.home.datamodel.WeeklyStatementModel
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
                PayStatementDetails
Its used to view the paymentstatement details function
*************************************************************** */
class PayStatementDetails : CommonActivity(), ServiceListener {
    private lateinit var binding: ActivityPaystatementDetailsBinding
    private var isViewUpdatedWithLocalDB: Boolean = false

    @Inject
    lateinit var dbHelper: Sqlite


   /* @BindView(R.id.dailyearning_list)
    lateinit var binding.dailyearningList: RecyclerView

    @BindView(R.id.nsv_whole)
    lateinit var binding.nsvWhole: NestedScrollView*/

    @Inject
    internal lateinit var commonMethods: CommonMethods

    @Inject
    internal lateinit var customDialog: CustomDialog

    @Inject
    lateinit var apiService: ApiService

    @Inject
    internal lateinit var sessionManager: SessionManager
    internal lateinit var dialog: AlertDialog

    @Inject
    internal lateinit var gson: Gson

   /* @BindView(R.id.tv_week_date)
    internal lateinit var tvWeekDate: FontTextView

    @BindView(R.id.tv_week_amt)
    internal lateinit var tvWeekAmt: FontTextView

    @BindView(R.id.tv_base_fare)
    internal lateinit var tvBaseFare: FontTextView

    @BindView(R.id.tv_access_fare)
    internal lateinit var tvAccessFare: FontTextView

    @BindView(R.id.tv_time_fare)
    internal lateinit var tvTimeFare: FontTextView

    @BindView(R.id.rl_time_fare)
    internal lateinit var rlTimeFare: RelativeLayout

    @BindView(R.id.tv_cabme_earn)
    internal lateinit var tvCabmeEarn: FontTextView

    @BindView(R.id.tv_cash_colltd)
    internal lateinit var tvCashColltd: FontTextView

    @BindView(R.id.tv_time_online)
    internal lateinit var tvTimeOnline: FontTextView

    @BindView(R.id.tv_cmptd_trip)
    internal lateinit var tvCmptdTrip: FontTextView

    @BindView(R.id.tv_cmptd_trip_title)
    internal lateinit var tvCmptdTripTitle: FontTextView

    @BindView(R.id.tv_bank_dep)
    internal lateinit var tvBankDep: FontTextView

    @BindView(R.id.header)
    internal lateinit var tvTitle: FontTextView

    @BindView(R.id.rv_weekly_stamt)
    internal lateinit var rvWeeklyStamt: RecyclerView*/

    private var weeklylistmodels: MutableList<WeeklyStatementModel.Statement> = ArrayList()
    internal lateinit var weeklyModel: WeeklyStatementModel
    internal lateinit var weeklyDriverStatement: WeeklyStatementModel.DriverStatement
    internal lateinit var weeklyDriverStatementContent: ArrayList<InvoiceContent>


    var searchlist: MutableList<PayModel> = ArrayList()
    var adapter: DailyEarnListAdapter? = null
    var Paydate = arrayOf("Sunday 03/06", "Monday 04/06 ", "Tuesday 04/06", "Wednesday 05/06", "Thursday 06/06", "Friday 07/06", "Saturday 08/06")
    var Payamount = arrayOf("$92.88", "$82.88", "$72.88", "$9002.88", "$902.88", "$92.88", "$892.88")

    var weeklyDate:String? = null


    private fun weeklyTripApiCall() {
        if (intent.hasExtra("weekly_date")) {
            val intent = intent
            weeklyDate = intent.getStringExtra("weekly_date").toString()

            val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_PAY_STATEMENTS_WEEKLY_DETAILS + weeklyDate)
            if (allHomeDataCursor.moveToFirst()) {
                isViewUpdatedWithLocalDB = true
                try {
                    onSuccessWeekly(allHomeDataCursor.getString(0))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                followProcedureForNoDataPresentInDB()
            }
        }
    }

    private fun loadWeeklyTripsDetail(){
        commonMethods.showProgressDialog(this)
        apiService.weeklyStatement(sessionManager.accessToken!!, sessionManager.userType!!, weeklyDate!!)
                .enqueue(RequestCallback(REQ_WEEKLY_STATEMENT, this@PayStatementDetails))
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            loadWeeklyTripsDetail()
        } else {
            CommonMethods.showNoInternetAlert(this, object : CommonMethods.INoInternetCustomAlertCallback {
                override fun onOkayClicked() {
                    finish()
                }

                override fun onRetryClicked() {
                    followProcedureForNoDataPresentInDB()
                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPaystatementDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.weeklystatement),binding.commonHeader.headertext)
        //commonMethods.imageChangeforLocality(this, insurance_back)
        dialog = commonMethods.getAlertDialog(this)
        binding.nsvWhole.visibility = View.GONE
        weeklyTripApiCall()

        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }

        /*
         *  list on click listener
         */
        //        binding.dailyearningList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
        //            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
        //
        //                @Override
        //                public boolean onSingleTapUp(MotionEvent e) {
        //                    return true;
        //                }
        //
        //            });
        //
        //            @Override
        //            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //                View child = rv.findChildViewUnder(e.getX(), e.getY());
        //                if (child != null && gestureDetector.onTouchEvent(e)) {
        //                    Intent paystatement_details = new Intent(getApplicationContext(), DailyEarningDetails.class);
        //                    startActivity(paystatement_details);
        //                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
        //                }
        //
        //                return false;
        //            }
        //
        //            @Override
        //            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        //
        //            }
        //
        //            @Override
        //            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //
        //            }
        //        });

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_WEEKLY_STATEMENT -> if (jsonResp.isSuccess) {
                dbHelper.insertWithUpdate(Constants.DB_KEY_PAY_STATEMENTS_WEEKLY_DETAILS + weeklyDate, jsonResp.strResponse)
                onSuccessWeekly(jsonResp.strResponse)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {}


    private fun onSuccessWeekly(jsonResponse: String) {
        binding.nsvWhole.visibility = View.VISIBLE
        weeklyModel = gson.fromJson(jsonResponse, WeeklyStatementModel::class.java)
        weeklylistmodels.clear()
        weeklylistmodels = weeklyModel.statement as MutableList<WeeklyStatementModel.Statement>
        weeklyDriverStatement = weeklyModel.driver_statement!!
        weeklyDriverStatementContent = weeklyModel.driver_statement!!.content!!

        //title
        binding.header.text = weeklyDriverStatement.title

        //header
        binding.tvWeekDate.text = weeklyDriverStatement.header?.date
        binding.tvWeekAmt.text = weeklyDriverStatement.header?.price

        //dailyDriverStatementContent
        binding.rvWeeklyStamt.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.rvWeeklyStamt.layoutManager = layoutManager
        var adapter: RecyclerView.Adapter<*> = PriceStatementAdapter(this, weeklyDriverStatementContent)
        binding.rvWeeklyStamt.adapter = adapter


        //footer
        binding.tvCmptdTripTitle.text = weeklyDriverStatement.footer!![0].key
        binding.tvCmptdTrip.text = weeklyDriverStatement.footer!![0].value


        //        tvBaseFare.setText(weeklyModel.getSymbol() + weeklyModel.getTotal_fare());
        //        tvAccessFare.setText("-"+weeklyModel.getSymbol() + weeklyModel.getAccess_fee());
        //        tvBankDep.setText(weeklyModel.getSymbol() + weeklyModel.getBank_deposits());
        //        rlTimeFare.setVisibility(View.GONE);
        //        tvCabmeEarn.setText(weeklyModel.getSymbol() + weeklyModel.getDriver_earnings());
        //        tvCashColltd.setText(weeklyModel.getSymbol() + weeklyModel.getCash_collected());


        if (!weeklylistmodels.isEmpty()) {
            binding.dailyearningList.setHasFixedSize(true)
            binding.dailyearningList.isNestedScrollingEnabled = false
            binding.dailyearningList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            adapter = DailyEarnListAdapter(this, weeklylistmodels)
            binding.dailyearningList.adapter = adapter
        }
        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            getWeeklyTripDetails()
        }
    }
    private fun getWeeklyTripDetails() {
        if (commonMethods.isOnline(this)) {
            apiService.weeklyStatement(sessionManager.accessToken!!, sessionManager.userType!!, weeklyDate!!).enqueue(RequestCallback(REQ_WEEKLY_STATEMENT, this@PayStatementDetails))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }
}
