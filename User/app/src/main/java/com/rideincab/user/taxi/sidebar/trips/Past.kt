package com.rideincab.user.taxi.sidebar.trips

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar.trips
 * @category Past
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
import com.rideincab.user.common.utils.Enums.REQ_PAST_TRIPS
import com.rideincab.user.common.utils.PaginationScrollListener
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.databinding.AppActivityPastBinding
import com.rideincab.user.databinding.AppActivityYourTripsBinding
import com.rideincab.user.taxi.adapters.PastTripsPaginationAdapter
import com.rideincab.user.taxi.datamodels.trip.TripDetailsModel
import com.rideincab.user.taxi.datamodels.trip.TripListModel
import com.rideincab.user.taxi.datamodels.trip.TripListModelArrayList
import com.rideincab.user.taxi.interfaces.PaginationAdapterCallback
import com.rideincab.user.taxi.interfaces.YourTripsListener
import com.rideincab.user.taxi.sendrequest.DriverRatingActivity
import com.rideincab.user.taxi.sendrequest.PaymentAmountPage
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.main.MainActivity
import org.json.JSONException
import javax.inject.Inject

/* ************************************************************
    Past Trip details
    *********************************************************** */
class Past : Fragment(), ServiceListener, PaginationAdapterCallback {

    private lateinit var binding: AppActivityPastBinding

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

    lateinit var tripStatus: String
    lateinit var pastTripListModel: TripListModel

    private var url: String? = null
    private var check = true
    private var isInternetAvailable: Boolean = false
    private var listener: YourTripsListener? = null
    private var mActivity: YourTrips? = null
    private lateinit var adapter: PastTripsPaginationAdapter
    private var linearLayoutManager: LinearLayoutManager? = null
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START

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
        binding = AppActivityPastBinding.inflate(inflater, container, false)
        AppController.appComponent.inject(this)
        dialog = commonMethods.getAlertDialog(activity!!.baseContext)
        init()
        /**
         * Common loader
         */
        adapter = PastTripsPaginationAdapter(this.context!!, this)

        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rcView.layoutManager = linearLayoutManager
        binding.rcView.itemAnimator = DefaultItemAnimator()

        binding.rcView.adapter = adapter
        val resId = R.anim.layout_animation_bottom_up
        val animation = AnimationUtils.loadLayoutAnimation(context, resId)
        binding.rcView.layoutAnimation = animation


        binding.rcView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun getTotalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun loadMoreItems() {
                if (commonMethods.isOnline(mActivity)) {
                    println("isloading")
                    isLoading = true
                    currentPage += 1
                    getPastTrips(false)
                }
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        binding.swipeToRefresh.setOnRefreshListener {
            currentPage = 1
            getPastTrips(false)
            isLastPage = false
            binding.swipeToRefresh.isRefreshing = false
        }

        adapter.setOnItemRatingClickListner(object :
            PastTripsPaginationAdapter.onItemRatingClickListner {
            override fun setRatingClick(position: Int, TripDetailsModel: TripListModelArrayList) {
                /**
                 * Handle Action Based on Trip Status
                 */
                tripStatus = TripDetailsModel.status!!
                if ("Completed" == tripStatus || "Cancelled" == tripStatus) {
                    val tripId = TripDetailsModel.tripId.toString()
                    val intent = Intent(activity, TripDetails::class.java)
                    intent.putExtra("tripId", tripId)
                    startActivity(intent)
                } /*else if ("Rating" == tripStatus) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    sessionManager.isDriverAndRiderAbleToChat = false
                    val tripId = TripDetailsModel.riders.get(0).tripId.toString()
                    sessionManager.tripId = tripId
                    sessionManager.tripStatus = "end_trip"
                    val rating = Intent(activity, DriverRatingActivity::class.java)
                    rating.putExtra("imgprofile", TripDetailsModel.driverThumbImage)
                    rating.putExtra("tripid", tripId)
                    rating.putExtra("back", 1)
                    startActivity(rating)
                } */
                else {
                    if (!isInternetAvailable) {
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

        pastTrips()
        return binding.root
    }

    private fun pastTrips() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_PAST_TRIPS.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            try {
                onSuccessPastTrips(allHomeDataCursor.getString(0), true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(requireContext())) {
            getPastTrips(true)
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
     * Api Calling Function For Past Trips
     */
    private fun getPastTrips(showLoader: Boolean) {
        if (commonMethods.isOnline(requireContext())) {
            if (currentPage == 1) {
                if (showLoader) {
                    commonMethods.showProgressDialog(requireContext())
                }
            }
            apiService.getPastTrips(sessionManager.accessToken!!, currentPage.toString())
                .enqueue(RequestCallback(REQ_PAST_TRIPS, this))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(requireContext())
        }
    }

    /**
     * Get Accepted driver details
     */
    fun getTripDetails() {
        commonMethods.showProgressDialog(mActivity?.applicationContext!!)
        apiService.getTripDetails(sessionManager.accessToken!!, sessionManager.tripId!!)
            .enqueue(RequestCallback(REQ_GET_DRIVER, this))
    }

    /**
     * Handle the Response from Api
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(mActivity, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            REQ_GET_DRIVER -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                if (isAdded)
                    onSuccessDriver(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
            }

            REQ_PAST_TRIPS -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    val getCurrentPage = commonMethods.getJsonValue(
                        jsonResp.strResponse,
                        "current_page",
                        Int::class.java
                    ) as Int
                    currentPage = getCurrentPage
                    if (getCurrentPage == 1) {
                        dbHelper.insertWithUpdate(
                            Constants.DB_KEY_PAST_TRIPS.toString(),
                            jsonResp.strResponse
                        )
                        onSuccessPastTrips(jsonResp.strResponse, false)
                    } else {
                        onLoadMorePastTrips(jsonResp)
                    }
                } else if (currentPage == 1 && !TextUtils.isEmpty(jsonResp.statusMsg)) {
                    binding.rcView.visibility = View.GONE
                    binding.listempty.visibility = View.VISIBLE
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
                }
            }

            else -> if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
            }
        }
    }

    /**
     * Get the Success Response and set on the Adapter
     */
    private fun onSuccessPastTrips(jsonResponse: String, isFromDatabase: Boolean) {
        commonMethods.hideProgressDialog()
        pastTripListModel = gson.fromJson(jsonResponse, TripListModel::class.java)
        if (pastTripListModel.data.size > 0) {
            TOTAL_PAGES = pastTripListModel.totalPages
            adapter.clearAll()
            adapter.addAll(pastTripListModel.data)
            adapter.notifyDataSetChanged()
            if (isFromDatabase) {
                isLastPage = true
                if (isViewUpdatedWithLocalDB) {
                    isViewUpdatedWithLocalDB = false
                    currentPage = 1
                    getPastTrips(false)
                }
            } else {
                if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                    if (commonMethods.isOnline(mActivity)) {
                        isLastPage = false
                        adapter.addLoadingFooter()
                    }
                } else
                    isLastPage = true
            }
        } else {
            binding.listempty.visibility = View.VISIBLE
        }
    }

    /**
     * Load More Some Past trips and Set in this Adapter
     */
    private fun onLoadMorePastTrips(jsonResponse: JsonResponse) {
        pastTripListModel = gson.fromJson(jsonResponse.strResponse, TripListModel::class.java)
        TOTAL_PAGES = pastTripListModel.totalPages
        adapter.removeLoadingFooter()
        isLoading = false

        adapter.addAll(pastTripListModel.data)
        adapter.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            adapter.addLoadingFooter()
        else
            isLastPage = true
    }


    /**
     * Handle the Failure Response from API
     */
    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(mActivity, dialog, jsonResp.statusMsg)
        }
    }


    /**
     * Get the tripDetails
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun onSuccessDriver(jsonResp: JsonResponse) {
        tripDetailsModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        val invoiceModels = tripDetailsModel.riders.get(0).invoice
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
            requstreceivepage.putExtra("driverDetails", tripDetailsModel)
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
            rating.putExtra("imgprofile", tripDetailsModel.driverThumbImage)
            startActivity(rating)

        } else if ("Payment" == tripStatus) {
            // To open the payment page
            sessionManager.isrequest = false
            sessionManager.isTrip = false
            val bundle = Bundle()
            bundle.putSerializable("invoiceModels", invoiceModels)
            val main = Intent(activity, PaymentAmountPage::class.java)
            main.putExtra("AmountDetails", jsonResp.strResponse)
            main.putExtra("driverDetails", tripDetailsModel)
            main.putExtra("isBack", 1)
            main.putExtras(bundle)
            startActivity(main)
        }
        activity!!.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }


    /**
     * Dialog to show turn on the internet
     */
    @SuppressLint("UseRequireInsteadOfGet")
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this.activity!!)
        builder.setMessage(getString(R.string.turnoninternet)).setCancelable(true)
        val alert = builder.create()
        alert.show()
    }

    /**
     * init the Listeners using Activity instance
     */
    private fun init() {
        if (listener == null) return
        mActivity = listener!!.instance
    }


    /**
     * Reload the Page When  Failure on LoadMore
     */
    override fun retryPageLoad() {
        getPastTrips(false)
    }


    companion object {
        var tripDetailsModel = TripDetailsModel()
        private val PAGE_START = 1
    }
}
