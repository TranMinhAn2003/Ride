package com.rideincab.driver.trips.rating

/**
 * @package com.cloneappsolutions.cabmedriver.trips.rating
 * @subpackage rating
 * @category Comments
 * @author SMR IT Solutions
 *
 */


import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.database.Sqlite
import com.rideincab.driver.common.helper.Constants
import com.rideincab.driver.common.helper.Constants.PAGE_START
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.PaginationScrollListener
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityCommentsBinding
import com.rideincab.driver.home.datamodel.RiderFeedBackArrayModel
import com.rideincab.driver.home.datamodel.RiderFeedBackModel
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.PaginationAdapterCallback
import com.rideincab.driver.home.interfaces.ServiceListener
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
                Comment
Its used to view the comments with rider screen page function
*************************************************************** */
class Comments : CommonActivity(), ServiceListener, PaginationAdapterCallback {

    private lateinit var binding:ActivityCommentsBinding
    @Inject
    lateinit var dbHelper: Sqlite
    private var isViewUpdatedWithLocalDB: Boolean = false


    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    /*@BindView(R.id.my_recycler_view2)
    lateinit var binding.myRecyclerView2: RecyclerView

    @BindView(R.id.norating)
    lateinit var binding.norating: TextView*/

    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START

    var isInternetAvailable: Boolean = false

    lateinit var riderFeedBackModel: RiderFeedBackModel

    /**
     * Hash map for user comments api
     *
     * @return
     */

    private val userComments: HashMap<String, String>
        get() {
            val userRatingHashMap = HashMap<String, String>()
            userRatingHashMap["user_type"] = sessionManager.type!!
            userRatingHashMap["token"] = sessionManager.accessToken!!
            userRatingHashMap["page"] = currentPage.toString()

            return userRatingHashMap
        }

    lateinit var commentsPaginationAdapter: CommentsPaginationAdapter

   

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)

        /* common Header */
        commonMethods.setheaderText(resources.getString(R.string.riderfeedback), binding.commonHeader.headertext)

        isInternetAvailable = commonMethods.isOnline(this)

        binding.myRecyclerView2.setHasFixedSize(false)
        val layoutManager = LinearLayoutManager(this)
        binding.myRecyclerView2.layoutManager = layoutManager
        binding.myRecyclerView2.isNestedScrollingEnabled = false


        commentsPaginationAdapter = CommentsPaginationAdapter(this, this)
        binding.myRecyclerView2.adapter = commentsPaginationAdapter

        binding.myRecyclerView2.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(this@Comments)) {
                    isLoading = true
                    currentPage += 1
                    updateUserComments(false)
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

        getUserComments()
        binding.commonHeader.back.setOnClickListener { onBackPressed() }
    }


    private fun getUserComments() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_RIDER_COMMENTS.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            try {
                onSuccessComments(allHomeDataCursor.getString(0), true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            updateUserComments(true)
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

    private fun initView() {

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            val getCurrentPage = commonMethods.getJsonValue(jsonResp.strResponse, "current_page", Int::class.java) as Int
            currentPage = getCurrentPage
            if (currentPage == 1) {
                dbHelper.insertWithUpdate(Constants.DB_KEY_RIDER_COMMENTS.toString(), jsonResp.strResponse)
                onSuccessComments(jsonResp.strResponse, false)
            } else {
                onLoadMoreComments(jsonResp.strResponse)
            }
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    /**
     * success response for comments
     *
     * @param jsonResp
     */

    private fun onSuccessComments(jsonResp: String, isFromDatabase: Boolean) {
        riderFeedBackModel = gson.fromJson(jsonResp, RiderFeedBackModel::class.java)
        if (riderFeedBackModel != null) {
            if (riderFeedBackModel.riderFeedBack?.size!! > 0) {
                commentsPaginationAdapter.clearAll()
                binding.norating.text = resources.getString(R.string.ratingsncomment)
                binding.norating.visibility = View.GONE
                binding.myRecyclerView2.visibility = View.VISIBLE
                TOTAL_PAGES = riderFeedBackModel.totalPages?.toInt()!!
                commentsPaginationAdapter.addAll(riderFeedBackModel.riderFeedBack as ArrayList<RiderFeedBackArrayModel>)
                commentsPaginationAdapter.notifyDataSetChanged()
                if (isFromDatabase) {
                    isLastPage = true
                    if (isViewUpdatedWithLocalDB) {
                        isViewUpdatedWithLocalDB = false
                        currentPage = 1
                        updateUserComments(false)
                    }
                } else {
                    if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                        isLastPage = false
                        commentsPaginationAdapter.addLoadingFooter()
                    } else
                        isLastPage = true
                }
            } else {
                if (isFromDatabase) {
                    isLastPage = true
                    if (isViewUpdatedWithLocalDB) {
                        isViewUpdatedWithLocalDB = false
                        currentPage = 1
                        updateUserComments(false)
                    }
                }
                binding.norating.text = resources.getString(R.string.noratings)
                binding.norating.visibility = View.VISIBLE
                binding.myRecyclerView2.visibility = View.GONE
            }
        } else {
            if (isFromDatabase) {
                isLastPage = true
                if (isViewUpdatedWithLocalDB) {
                    isViewUpdatedWithLocalDB = false
                    currentPage = 1
                    updateUserComments(false)
                }
            }
            binding.norating.text = resources.getString(R.string.noratings)
            binding.norating.visibility = View.VISIBLE
            binding.myRecyclerView2.visibility = View.GONE
        }
    }

    private fun onLoadMoreComments(response: String) {
        riderFeedBackModel = gson.fromJson(response, RiderFeedBackModel::class.java)
        TOTAL_PAGES = riderFeedBackModel.totalPages?.toInt()!!
        commentsPaginationAdapter.removeLoadingFooter()
        isLoading = false

        commentsPaginationAdapter.addAll(riderFeedBackModel.riderFeedBack as ArrayList<RiderFeedBackArrayModel>)
        commentsPaginationAdapter.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            commentsPaginationAdapter.addLoadingFooter()
        else
            isLastPage = true
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

    }

    /**
     * User Comments Api Call
     */

    private fun updateUserComments(showLoader: Boolean) {
        if (commonMethods.isOnline(this)) {
            if (currentPage == 1) {
                if (showLoader) {
                    commonMethods.showProgressDialog(this as CommonActivity)
                }
            }
            apiService.updateRiderFeedBack(userComments).enqueue(RequestCallback(this))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    override fun retryPageLoad() {
        updateUserComments(false)
    }
}
