package com.rideincab.driver.home.fragments.Referral

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView

import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.gson.Gson
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.home.datamodel.ReferredFriendsModel
import com.rideincab.driver.common.helper.CustomDialog
import com.rideincab.driver.home.interfaces.ApiService
import com.rideincab.driver.home.interfaces.ServiceListener
import com.rideincab.driver.common.model.JsonResponse
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.util.RequestCallback

import javax.inject.Inject


import com.rideincab.driver.R
import com.rideincab.driver.common.network.AppController

import com.rideincab.driver.common.util.CommonKeys.CompletedReferralArray
import com.rideincab.driver.common.util.CommonKeys.IncompleteReferralArray
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.databinding.AppActivityShowReferralOptionsBinding

class ShowReferralOptionsActivity : CommonActivity(), ServiceListener {

    lateinit var binding: AppActivityShowReferralOptionsBinding
    
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
    
   /* @BindView(R.id.rv_in_completed_referrals)
    lateinit var rvIncompletedReferrals: RecyclerView

    @BindView(R.id.rv_completed_referrals)
    lateinit var rvCompletedReferrals: RecyclerView


    @BindView(R.id.constraintLayout_in_completed_friends)
    lateinit var cvIncompleteFriends: ConstraintLayout

    @BindView(R.id.constraintLayout_completed_friends)
    lateinit var cvCompleteFriends: ConstraintLayout

    @BindView(R.id.constraintLayout_referral_code)
    lateinit var cvReferralHeader: ConstraintLayout

    @BindView(R.id.tv_referral_code)
    lateinit var tvReferralCode: TextView

    @BindView(R.id.imag_share)
    lateinit var tv_share_option: ImageView


    @BindView(R.id.tv_total_earned)
    lateinit var tvTotalEarned: TextView

    @BindView(R.id.tv_amount)
    lateinit var tvEarnedAmount: TextView

    @BindView(R.id.tv_referral_benifit_text)
    lateinit var tvReferralBenifitStatement: TextView

    @BindView(R.id.rlt_share)
    lateinit var rltShare: RelativeLayout

    @BindView(R.id.scv_referal)
    lateinit var scvReferal: ScrollView

    @BindView(R.id.remaing_referral_amount)
    lateinit var remainingReferral: TextView


    @BindView(R.id.tv_no_referrals_yet)
    lateinit var tvNoReferralsYet: TextView*/

    private var referralCode = ""
    private var referralLink = ""
    private lateinit var referredFriendsModel: ReferredFriendsModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=AppActivityShowReferralOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppController.getAppComponent().inject(this)
        binding.root.applySystemBarsPadding()
        commonMethods.setheaderText(resources.getString(R.string.referral),binding.commonHeader.headertext)
        //commonMethods.imageChangeforLocality(this,arrow)
        dialog = commonMethods.getAlertDialog(this)
        binding.scvReferal.visibility = View.GONE
        initView()

        binding.imagCopy.setOnClickListener {
            CommonMethods.copyContentToClipboard(this, referralCode)
        }
        binding.imagShare.setOnClickListener {
            shareMyReferralCode()
        }

        binding.commonHeader.back.setOnClickListener {
            onBackPressed()
        }

    }

    /**
     * init Views For Referral
     */
    private fun initView() {
        showOrHideReferralAccordingToSessionData()
        getReferralInformationFromAPI()
    }

    /**
     * Hide and Show the Referral  Based on Referral Enable
     */
    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            binding.constraintLayoutReferralCode.visibility = View.VISIBLE
        } else {
            binding.constraintLayoutReferralCode.visibility = View.GONE
        }
    }

    /**
     * get Referral info for user
     */
    private fun getReferralInformationFromAPI() {
        commonMethods.showProgressDialog(this)
        apiService.getReferralDetails(sessionManager.accessToken!!).enqueue(RequestCallback(this))
    }


    /**
     * Get My Referral Code
     */
    fun shareMyReferralCode() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name) + " " + resources.getString(R.string.referral))
        share.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invite_msg) + " " + spannableString(referralCode) + " " + referralLink)
        startActivity(Intent.createChooser(share, resources.getString(R.string.share_my_code)))
    }

    /**
     * Combine Your Referral Code
     */
    private fun spannableString(referralCode: String): String {
        val ss = SpannableString(referralCode)
        ss.setSpan(StyleSpan(Typeface.BOLD), 0, referralCode.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss.toString()
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {
            binding.scvReferal.visibility = View.VISIBLE
            onSuccessResult(jsonResp.strResponse)
            //jsonResp.strResponse.let { onSuccessResult(it) }
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * onSuccessResponse of the Referral
     */
    private fun onSuccessResult(strResponse: String) {
        referredFriendsModel = gson.fromJson(strResponse, ReferredFriendsModel::class.java)
        if (!TextUtils.isEmpty(referredFriendsModel.remainingReferral)) {
            binding.remaingReferral.text = referredFriendsModel.remainingReferral
        } else {
            binding.remaingReferral.text = sessionManager.currencySymbol + "0"
        }
        updateReferralCodeInUI()
        if (referredFriendsModel.pendingReferrals?.size != 0 || referredFriendsModel.completedReferrals?.size != 0) {
            showReferralsNotAvailable(true)
            proceedCompleteReferralDetails()
            proceedIncompleteReferralDetails()
        } else {
            showReferralsNotAvailable(false)
        }
    }

    /**
     * Update Your Referral UI
     */
    private fun updateReferralCodeInUI() {
        referralCode = referredFriendsModel.referralCode.toString()
        referralLink = referredFriendsModel.referralLink.toString()
        binding.tvReferralCode.text = referralCode
        if ("1".equals(resources.getString(R.string.layout_direction), ignoreCase = true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, setCurrencyFrontForRTL(Html.fromHtml(referredFriendsModel.referralAmount, Html.FROM_HTML_MODE_LEGACY).toString()))
            }else{
                binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, setCurrencyFrontForRTL(Html.fromHtml(referredFriendsModel.referralAmount).toString()))
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, Html.fromHtml(referredFriendsModel.referralAmount, Html.FROM_HTML_MODE_LEGACY).toString())
            }else{
                binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, Html.fromHtml(referredFriendsModel.referralAmount).toString())
            }
        }
        //tvTotalEarned.append(referredFriendsModel.getTotalEarning().toString());
        binding.tvAmount.text = referredFriendsModel.totalEarning
    }

    /**
     * Currency symbol  RTL for Amount
     */
    private fun setCurrencyFrontForRTL(amount: String): String {
        println("amount $amount")
        val currency = amount[0].toString()
        println("currency $currency")
        return amount.replace(currency, " ") + currency
    }

    /**
     * Referral Hide
     */
    private fun showReferralsNotAvailable(show: Boolean) {
        if (show) {
            binding.constraintLayoutInCompletedFriends.visibility = View.VISIBLE
            binding.constraintLayoutCompletedFriends.visibility = View.VISIBLE
            binding.tvNoReferralsYet.visibility = View.GONE
        } else {
            binding.constraintLayoutInCompletedFriends.visibility = View.GONE
            binding.constraintLayoutCompletedFriends.visibility = View.GONE
            binding.tvNoReferralsYet.visibility = View.VISIBLE
        }
    }

    /**
     * InComplete ReferralDetails
     */
    private fun proceedIncompleteReferralDetails() {
        if (referredFriendsModel.pendingReferrals?.size != 0) {
            binding.rvInCompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            binding.rvInCompletedReferrals.layoutManager = layoutManager
            binding.rvInCompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(this, referredFriendsModel.pendingReferrals, IncompleteReferralArray)
        } else {
            binding.constraintLayoutInCompletedFriends.visibility = View.GONE
        }
    }

    /**
     * Proceed Completed ReferralDetails
     */
    private fun proceedCompleteReferralDetails() {
        if (referredFriendsModel.completedReferrals?.size != 0) {
            binding.rvCompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            binding.rvCompletedReferrals.layoutManager = layoutManager
            binding.rvCompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(this, referredFriendsModel.completedReferrals, CompletedReferralArray)
        } else {
            binding.constraintLayoutCompletedFriends.visibility = View.GONE
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }
}