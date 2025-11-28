/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.rideincab.driver.home.fragments

/**
 * @package com.cloneappsolutions.cabmedriver.home.fragments
 * @subpackage fragments
 * @category RatingActivity
 * @author SMR IT Solutions
 *
 */

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.datamodel.RatingModel
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.ActivityRatingBinding
import com.rideincab.driver.trips.rating.Comments
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/* ************************************************************
                      RatingActivity
Its used get home screen rating fragment details
*************************************************************** */
class RatingActivity : CommonActivity(), ServiceListener {

    lateinit var binding: ActivityRatingBinding
    lateinit var dialog: AlertDialog

    lateinit @Inject
    var commonMethods: CommonMethods

    lateinit @Inject
    var apiService: ApiService

    lateinit @Inject
    var sessionManager: SessionManager

    lateinit @Inject
    var gson: Gson

    lateinit @Inject
    var customDialog: CustomDialog

    /*   lateinit @BindView(R.id.binding.feedbackhistorylayout)
       var binding.feedbackhistorylayout: RelativeLayout
       lateinit @BindView(R.id.rating_lay)
       var rating_lay: RelativeLayout
       lateinit @BindView(R.id.binding.lifetime)
       var binding.lifetime: TextView
       lateinit @BindView(R.id.binding.ratingtrips)
       var binding.ratingtrips: TextView
       lateinit @BindView(R.id.binding.fivestar)
       var binding.fivestar: TextView
       lateinit @BindView(R.id.binding.textView2)
       var binding.textView2: TextView
       lateinit @BindView(R.id.tv_rating_content)
       var binding.tvRatingContent: TextView
       lateinit @BindView(R.id.arrarowone)
       var arrarowone: TextView
       
       @OnClick(R.id.ivBack)
       fun onBack() {
           onBackPressed()
       }
       @BindView(R.id.tvTitle)
       lateinit var tvTitle: TextView*/


    protected var isInternetAvailable: Boolean = false

    val userRating: HashMap<String, String>
        get() {
            val userRatingHashMap = HashMap<String, String>()
            userRatingHashMap["user_type"] = sessionManager.type!!
            userRatingHashMap["token"] = sessionManager.accessToken!!

            return userRatingHashMap
        }


    fun feedbackHistoryLayout() {
        binding.feedbackhistorylayout.isEnabled = false
        val intent = Intent(this, Comments::class.java)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        binding.root.applySystemBarsPadding()
        dialog = commonMethods.getAlertDialog(this)
        isInternetAvailable = commonMethods.isOnline(this)
        binding.textView2.visibility = View.GONE
        binding.tvRatingContent.visibility = View.GONE
        binding.rltHeader.tvTitle.text = getString(R.string.rating)

        if (isInternetAvailable) {
            /*
         *  Get motorista rating and feed back details API
         **/
            updateEarningChart()

        } else {
            dialogfunction()
        }
        initView()
        binding.rltHeader.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.feedbackhistorylayout.setOnClickListener {
            feedbackHistoryLayout()
        }
    }

    private fun initView() {
    }


    fun updateEarningChart() {

        commonMethods.showProgressDialog(this)
        apiService.updateDriverRating(userRating).enqueue(RequestCallback(this))

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {

            onSuccessRating(jsonResp)

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

        }
    }

    override fun onResume() {
        super.onResume()
        binding.feedbackhistorylayout.isEnabled = true
    }

    private fun onSuccessRating(jsonResp: JsonResponse) {

        val ratingModel = gson.fromJson(jsonResp.strResponse, RatingModel::class.java)
        if (ratingModel != null) {
            val total_rating = ratingModel.totalRating
            val total_rating_count = ratingModel.totalRatingCount
            val five_rating_count = ratingModel.fiveRatingCount
            val driver_rating = ratingModel.driverRating

            binding.lifetime.text = total_rating_count
            binding.ratingtrips.text = total_rating
            binding.fivestar.text = five_rating_count

            if (driver_rating!!.equals("0.00", ignoreCase = true) || driver_rating.equals(
                    "0",
                    ignoreCase = true
                )
            ) {
                binding.tvRatingContent.visibility = View.GONE
                binding.textView2.visibility = View.VISIBLE
                binding.textView2.text = resources.getString(R.string.no_ratings_display)
                binding.textView2.textSize = 20f
                binding.textView2.setCompoundDrawablesRelative(null, null, null, null)
            } else {
                binding.textView2.visibility = View.VISIBLE
                binding.textView2.text = driver_rating
                binding.tvRatingContent.visibility = View.VISIBLE
            }


        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

    }


    /*
     *  show dialog for no internet available
     */
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.turnoninternet))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> builder.setCancelable(true) }

        val alert = builder.create()
        alert.show()
    }

    companion object {


    }

}
