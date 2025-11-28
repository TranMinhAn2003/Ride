package com.rideincab.user.taxi.sidebar.trips

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar.trips
 * @category Upcoming
 * @author SMR IT Solutions
 *
 */

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.database.SqLiteDb
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.Enums.REQ_GET_DRIVER
import com.rideincab.user.common.utils.Enums.REQ_UPCOMING_TRIPS
import com.rideincab.user.common.utils.PaginationScrollListener
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.databinding.AppActivityUpcomingBinding
import com.rideincab.user.taxi.adapters.UpcomingTripsPaginationAdapter
import com.rideincab.user.taxi.datamodels.trip.*
import com.rideincab.user.taxi.interfaces.PaginationAdapterCallback
import com.rideincab.user.taxi.interfaces.YourTripsListener
import com.rideincab.user.taxi.sendrequest.DriverRatingActivity
import com.rideincab.user.taxi.sendrequest.PaymentAmountPage
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
    Upcoming Trip details
    *********************************************************** */
class Upcoming : Fragment(), ServiceListener, PaginationAdapterCallback {

    private lateinit var binding: AppActivityUpcomingBinding

    @Inject
    lateinit var dbHelper: SqLiteDb
    private var isViewUpdatedWithLocalDB: Boolean = false

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

/*    @BindView(R.id.binding.rcView)
    lateinit var binding.rcView: RecyclerView

    @BindView(R.id.binding.listempty)
    lateinit var binding.listempty: TextView

    @BindView(R.id.binding.swipeToRefresh)
    lateinit var binding.swipeToRefresh: SwipeRefreshLayout*/
    
    var scheduleDetails = ArrayList<ScheduleDetail>()
    protected var isInternetAvailable: Boolean = false
    private var listener: YourTripsListener? = null
    private var mActivity: YourTrips? = null

    private lateinit var adapter: UpcomingTripsPaginationAdapter
    lateinit private var linearLayoutManager: LinearLayoutManager
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START
    private lateinit var upcomingTripMode: TripListModel
    lateinit var tripStatus: String
    private var check = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = activity as YourTripsListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Past must implement ActivityListener")
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= AppActivityUpcomingBinding.inflate(inflater, container, false)

        AppController.appComponent.inject(this)
        init()
        dialog = commonMethods.getAlertDialog(mActivity!!.baseContext)
        binding.swipeToRefresh.setOnRefreshListener {
            currentPage = 1
            getScheduledTrip()
            isLastPage = false
            binding.swipeToRefresh.isRefreshing = false
        }
        adapter = UpcomingTripsPaginationAdapter(requireContext(), this, mActivity!!)

        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rcView.layoutManager = linearLayoutManager
        binding.rcView.itemAnimator = DefaultItemAnimator()

        binding.rcView.adapter = adapter
        adapter.setOnItemRatingClickListner(object :
            UpcomingTripsPaginationAdapter.onItemRatingClickListner {
            override fun setRatingClick(position: Int, TripDetailsModel: TripListModelArrayList) {
                tripStatus = TripDetailsModel.status!!
                if ("Rating" == tripStatus) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    sessionManager.isDriverAndRiderAbleToChat = false
                    val tripId = TripDetailsModel.tripId.toString()
                    sessionManager.tripId = tripId
                    sessionManager.tripStatus = "end_trip"
                    val rating = Intent(activity, DriverRatingActivity::class.java)
                    rating.putExtra("imgprofile", TripDetailsModel.driverImage)
                    rating.putExtra("tripid", tripId)
                    rating.putExtra("back", 1)
                    startActivity(rating)
                } else {
                    if (!commonMethods.isOnline(mActivity)) {
                        commonMethods.showMessage(
                            mActivity,
                            dialog,
                            getString(R.string.no_connection)
                        )
                    } else {
                        check = false
                        sessionManager.tripId = TripDetailsModel.tripId.toString()
                        getTripDetails()
                    }
                }
            }
        })

        binding.rcView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(mActivity)) {
                    this@Upcoming.isLoading = true
                    currentPage += 1
                    getScheduledTrip()
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
        val resId = R.anim.layout_animation_bottom_up
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        binding.rcView.layoutAnimation = animation

        scheduleTripsFollow()
        return binding.root

    }

    private fun scheduleTripsFollow() {
        val allHomeDataCursor: Cursor =
            dbHelper.getDocument(Constants.DB_KEY_UPCOMING_TRIPS.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            try {
                onSuccessUpcomingsTrips(allHomeDataCursor.getString(0), true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(requireContext())) {
            getScheduledTrip()
        } else {
            CommonMethods.showNoInternetAlert(
                requireContext(),
                object : CommonMethods.INoInternetCustomAlertCallback {
                    override fun onOkayClicked() {
                        requireActivity().finish()
                    }

                    override fun onRetryClicked() {
                        followProcedureForNoDataPresentInDB()
                    }

                })
        }
    }

    /**
     * API called for Scheduled trips details
     */

    private fun getScheduledTrip() {
        apiService.getUpcomingTrips(sessionManager.accessToken!!, currentPage.toString())
            .enqueue(RequestCallback(REQ_UPCOMING_TRIPS, this))
    }

    /**
     * Dialog to show turn on the internet
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun dialogfunction() {
        val builder = AlertDialog.Builder(this.activity!!)
        builder.setMessage("Turn on your Internet")
            .setCancelable(false)

        val alert = builder.create()
        alert.show()
    }

    private fun init() {
        if (listener == null) return

        mActivity = listener!!.instance
    }

    override fun retryPageLoad() {
        getScheduledTrip()
    }

    fun getTripDetails() {
        commonMethods.showProgressDialog(requireContext())
        apiService.getTripDetails(sessionManager.accessToken!!, sessionManager.tripId!!)
            .enqueue(RequestCallback(REQ_GET_DRIVER, this))
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        //commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(mActivity, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_GET_DRIVER -> if (jsonResp.isSuccess) {
                //commonMethods.hideProgressDialog()
                if (isAdded)
                    onSuccessDriver(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                //commonMethods.hideProgressDialog()
                commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
            }

            REQ_UPCOMING_TRIPS -> if (jsonResp.isSuccess) {
                //commonMethods.hideProgressDialog()
                val getCurrentPage = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "current_page",
                    Int::class.java
                ) as Int
                currentPage = getCurrentPage
                if (getCurrentPage == 1) {
                    //dbHelper.insertWithUpdate(Constants.DB_KEY_UPCOMING_TRIPS.toString(), jsonResp.strResponse)
                    onSuccessUpcomingsTrips(jsonResp.strResponse, false)
                } else {
                    onLoadMoreUpcomingsTrips(jsonResp)
                }
            } else if (currentPage == 1 && !TextUtils.isEmpty(jsonResp.statusMsg)) {
                binding.rcView.visibility = View.GONE
                binding.listempty.visibility = View.VISIBLE
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                //commonMethods.hideProgressDialog()
                commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
            }

            else -> if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                //commonMethods.hideProgressDialog()
                commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
            }
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
        }
    }

    private fun onSuccessUpcomingsTrips(jsonResponse: String, isFromDatabase: Boolean) {
        upcomingTripMode = gson.fromJson(jsonResponse, TripListModel::class.java)
        if (upcomingTripMode.data.size > 0) {
            TOTAL_PAGES = upcomingTripMode.totalPages
            adapter.removeAll()
            adapter.addAll(upcomingTripMode.data)
            adapter.notifyDataSetChanged()

            if (isFromDatabase) {
                isLastPage = true
                if (isViewUpdatedWithLocalDB) {
                    isViewUpdatedWithLocalDB = false
                    currentPage = 1
                    getScheduledTrip()
                }
            } else {
                if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                    isLastPage = false
                    adapter.addLoadingFooter()
                } else
                    isLastPage = true
            }
        } else {
            binding.listempty.visibility = View.VISIBLE
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun onSuccessDriver(jsonResp: JsonResponse) {
        Past.tripDetailsModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        sessionManager.isrequest = false
        sessionManager.isTrip = true

        // If trips status is schedule or begin trip or endtrip to open the trips page ( Map Page)
        if ("Scheduled" == tripStatus || "Begin trip" == tripStatus || "End trip" == tripStatus) {

            if ("Scheduled" == tripStatus) {
                sessionManager.tripStatus = "arrive_now"
            } else if ("Begin trip" == tripStatus) {
                sessionManager.tripStatus = "arrive_now"
            } else if ("End trip" == tripStatus) {
                sessionManager.tripStatus = "end_trip"
            }

            val requstreceivepage = Intent(activity, MainActivity::class.java)
            requstreceivepage.putExtra("driverDetails", Past.tripDetailsModel)
            if ("Scheduled" == tripStatus || "Begin trip" == tripStatus) {
                requstreceivepage.putExtra("isTripBegin", false)
            } else if ("End trip" == tripStatus) {
                requstreceivepage.putExtra("isTripBegin", true)
            }
            sessionManager.isDriverAndRiderAbleToChat = true
            startActivity(requstreceivepage)

        } else if ("Rating" == tripStatus) {
            // To open rating page
            sessionManager.isDriverAndRiderAbleToChat = false
            CommonMethods.stopFirebaseChatListenerService(this.mActivity!!)
            val rating = Intent(activity, DriverRatingActivity::class.java)
            rating.putExtra("imgprofile", Past.tripDetailsModel.driverThumbImage)
            startActivity(rating)

        } else if ("Payment" == tripStatus) {
            // To open the payment page
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            val main = Intent(activity, PaymentAmountPage::class.java)
            main.putExtra("AmountDetails", jsonResp.strResponse)
            main.putExtra("driverDetails", Past.tripDetailsModel)
            main.putExtra("isBack", 1)
            startActivity(main)
        }
        activity!!.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }


    private fun onLoadMoreUpcomingsTrips(jsonResponse: JsonResponse) {
        upcomingTripMode = gson.fromJson(jsonResponse.strResponse, TripListModel::class.java)
        TOTAL_PAGES = upcomingTripMode.totalPages
        adapter.removeLoadingFooter()
        isLoading = false

        adapter.addAll(upcomingTripMode.data)
        adapter.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            adapter.addLoadingFooter()
        else
            isLastPage = true
    }

    companion object {
        private val PAGE_START = 1
    }
}

