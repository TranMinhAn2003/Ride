package com.rideincab.driver.home.paymentstatement

/**
 * @package com.cloneappsolutions.cabmedriver.home.paymentstatement
 * @subpackage paymentstatement model
 * @category PaymentStatementActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.custompalette.FontTextView
import com.rideincab.driver.common.database.Sqlite
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.Constants.PAGE_START
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.Enums.REQ_WEEKLY_TRIP_STATEMENT
import com.rideincab.driver.common.util.PaginationScrollListener
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityPaymentStatementBinding
import com.rideincab.driver.home.datamodel.WeeklyTripStatement
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.PaginationAdapterCallback
import com.rideincab.driver.home.interfaces.ServiceListener
import org.json.JSONException
import javax.inject.Inject

/* ************************************************************
                PaymentStatementActivity
Its used to view the payment statement all the details
*************************************************************** */
class PaymentStatementActivity : CommonActivity(), ServiceListener, PaginationAdapterCallback, PayStatementPaginationAdapter.DailyCallback {
    private lateinit var binding:ActivityPaymentStatementBinding
    private var isViewUpdatedWithLocalDB: Boolean = false

    @Inject
    lateinit var dbHelper: Sqlite

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var apiservice: ApiService

    @Inject
    lateinit var sessionManager: SessionManager
    internal lateinit var dialog: AlertDialog

 /*   @BindView(R.id.paystatementlist)
    lateinit var binding.paystatementlist: RecyclerView

    @BindView(R.id.listempty)
    lateinit var tvlistempty: FontTextView*/

    @Inject
    lateinit var gson: Gson
    private var startDate = ""
    private var endDate = ""
    private var weeklytriplistmodels: MutableList<WeeklyTripStatement.Statement> = ArrayList()
    private var payStatementPaginationAdapter: PayStatementPaginationAdapter? = null
    private lateinit var weeklyTripModel: WeeklyTripStatement


    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START


    /*@OnClick(R.id.pay_back_lay)
    fun onBack() {
        onBackPressed()
    }*/
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentStatementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        
        AppController.getAppComponent().inject(this)
        //commonMethods.imageChangeforLocality(this, pay_back)
        commonMethods.setheaderText(resources.getString(R.string.pay_statemet), binding.commonHeader.headertext)
        initViews()
        getIntentData()
        weeklyTripApiCall()
        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData() {
        val intent = intent
        startDate = intent.getStringExtra("start_date").toString()
        endDate = intent.getStringExtra("end_date").toString()
    }

    private fun weeklyTripApiCall() {
        try {
            val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_PAY_STATEMENTS_WEEKLY.toString())
            if (allHomeDataCursor.moveToFirst()) {
                isViewUpdatedWithLocalDB = true
                //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
                try {
                    onSuccessWeeklyTrip(allHomeDataCursor.getString(0),true)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                followProcedureForNoDataPresentInDB()
            }
        } catch (e: Exception) {
            commonMethods.showMessage(this, dialog, e.message.toString())
        }
    }

    private fun loadWeeklyTrips(showLoader:Boolean) {
        if (commonMethods.isOnline(this)){
            if (currentPage == 1) {
                if (showLoader) {
                    commonMethods.showProgressDialog(this)
                }
            }
            apiservice.weeklyTripStatement(sessionManager.accessToken!!, currentPage.toString()).enqueue(RequestCallback(REQ_WEEKLY_TRIP_STATEMENT, this))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            loadWeeklyTrips(true)
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    private fun onSuccessWeeklyTrip(jsonResponse: String,isFromDatabase:Boolean) {
        weeklyTripModel = gson.fromJson(jsonResponse, WeeklyTripStatement::class.java)
        weeklytriplistmodels.clear()
        weeklytriplistmodels = weeklyTripModel.tripWeekDetails as ArrayList<WeeklyTripStatement.Statement>
        if (weeklytriplistmodels.size > 0) {
            payStatementPaginationAdapter?.getSymbol(weeklyTripModel.symbol!!)
            binding.listempty.visibility = View.GONE
            TOTAL_PAGES = weeklyTripModel.totalPage!!
            payStatementPaginationAdapter?.clearAll()
            payStatementPaginationAdapter?.addAll(weeklytriplistmodels as ArrayList<WeeklyTripStatement.Statement>)
            payStatementPaginationAdapter!!.notifyDataSetChanged()
            if (isFromDatabase){
                isLastPage = true
                if (isViewUpdatedWithLocalDB) {
                    isViewUpdatedWithLocalDB = false
                    currentPage = 1
                    loadWeeklyTrips(false)
                }
            }else {
                if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                    if (commonMethods.isOnline(this)) {
                        isLastPage = false
                        payStatementPaginationAdapter?.addLoadingFooter()
                    }
                }
                else
                    isLastPage = true
            }
        } else {
            binding.listempty.visibility = View.VISIBLE
        }
    }

    private fun onLoadPayStatementsTrips(jsonResponse: String) {
        weeklyTripModel = gson.fromJson(jsonResponse, WeeklyTripStatement::class.java)
        TOTAL_PAGES = weeklyTripModel.totalPage!!
        payStatementPaginationAdapter!!.removeLoadingFooter()
        isLoading = false

        payStatementPaginationAdapter?.addAll(weeklyTripModel.tripWeekDetails as ArrayList<WeeklyTripStatement.Statement>)
        payStatementPaginationAdapter!!.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            payStatementPaginationAdapter!!.addLoadingFooter()
        else
            isLastPage = true
    }

    /**
     * API ON Success
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_WEEKLY_TRIP_STATEMENT -> if (jsonResp.isSuccess) {
                val getCurrentPage = commonMethods.getJsonValue(jsonResp.strResponse, "current_page", Int::class.java) as Int
                currentPage = getCurrentPage
                dbHelper.insertWithUpdate(Constants.DB_KEY_PAY_STATEMENTS_WEEKLY.toString(), jsonResp.strResponse)
                if (currentPage == 1) {
                    onSuccessWeeklyTrip(jsonResp.strResponse,false)
                } else {
                    onLoadPayStatementsTrips(jsonResp.strResponse)
                }
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
    }

    private fun initViews() {
        dialog = commonMethods.getAlertDialog(this)
        binding.paystatementlist.setHasFixedSize(false)
        payStatementPaginationAdapter = PayStatementPaginationAdapter(this, this, this)

        val linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        binding.paystatementlist.layoutManager = linearLayoutManager

        binding.paystatementlist.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(this@PaymentStatementActivity)) {
                    isLoading = true
                    currentPage += 1
                    loadWeeklyTrips(false)
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })
        binding.paystatementlist.adapter = payStatementPaginationAdapter
    }

    override fun retryPageLoad() {
        weeklyTripApiCall()
    }

    override fun onItemClick(date: String) {
        val intent = Intent(this@PaymentStatementActivity, PayStatementDetails::class.java)
        intent.putExtra("weekly_date", date)
        startActivity(intent)
    }
}