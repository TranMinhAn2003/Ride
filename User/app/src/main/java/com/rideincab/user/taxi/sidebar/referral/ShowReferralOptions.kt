package com.rideincab.user.taxi.sidebar.referral

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.View
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.interfaces.ApiService
import com.rideincab.user.common.interfaces.ServiceListener
import com.rideincab.user.common.network.AppController
import com.rideincab.user.taxi.sidebar.referral.model.ReferredFriendsModel
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.common.utils.RequestCallback
import com.rideincab.user.taxi.views.customize.CustomDialog

import javax.inject.Inject

import com.rideincab.user.common.utils.CommonKeys.CompletedReferralArray
import com.rideincab.user.common.utils.CommonKeys.IncompleteReferralArray
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppActivityShowReferralOptionBinding

@Suppress("DEPRECATION")
class ShowReferralOptions : CommonActivity(), ServiceListener {

    private lateinit var binding:AppActivityShowReferralOptionBinding
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

/*    @BindView(R.id.rv_in_completed_referrals)
    lateinit var binding.rvInCompletedReferrals: RecyclerView

    @BindView(R.id.rv_completed_referrals)
    lateinit var binding.rvCompletedReferrals: RecyclerView
    
    @BindView(R.id.constraintLayout_in_completed_friends)
    lateinit var binding.constraintLayoutInCompletedFriends: ConstraintLayout

    @BindView(R.id.scv_referal)
    lateinit var binding.scvReferal: ScrollView

    @BindView(R.id.constraintLayout_completed_friends)
    lateinit var binding.constraintLayoutCompletedFriends: ConstraintLayout

    @BindView(R.id.constraintLayout_referral_code)
    lateinit var binding.constraintLayoutReferralCode: ConstraintLayout
  
    
    @BindView(R.id.tv_referral_code)
    lateinit var binding.tvReferralCode: TextView

    @BindView(R.id.binding.binding.tvTotalEarned)
    lateinit var binding.tvTotalEarned: TextView

    @BindView(R.id.tv_amount)
    lateinit var binding.binding.tvAmount: TextView

    @BindView(R.id.tv_referral_benifit_text)
    lateinit var binding.tvReferralBenifitText: TextView

    @BindView(R.id.rlt_share)
    lateinit var rltShare: RelativeLayout

 *//*   @BindView(R.id.imag_share)
    lateinit var imgShare: ImageView*//*

    @BindView(R.id.tv_no_referrals_yet)
    lateinit var binding.tvNoReferralsYet: TextView*/

    /* @OnClick(R.id.rlt_share)
     fun share() {
         shareMyReferralCode()
     }*/
    
    private var referralCode = ""

    private var referralLink = ""

    lateinit private var referredFriendsModel: ReferredFriendsModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=AppActivityShowReferralOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.referral),binding.commonHeader.tvHeadertext)
        dialog = commonMethods.getAlertDialog(this)
        binding.scvReferal.visibility = View.GONE
        initView()
        
        binding.imagShare.setOnClickListener {
            shareMyReferralCode()
        }
        binding.commonHeader.arrow.setOnClickListener {
            onBackPressed()
        }
        binding.imagCopy.setOnClickListener {
            CommonMethods.copyContentToClipboard(this, referralCode)
        }
   
    }

    fun shareMyReferralCode() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name) + " " + resources.getString(R.string.referral))
        share.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invite_msg) + " " + spannableString(referralCode) + " " + referralLink)

        startActivity(Intent.createChooser(share, resources.getString(R.string.share_code)))
    }

    private fun spannableString(referralCode: String): String {
        val ss = SpannableString(referralCode)
        ss.setSpan(StyleSpan(Typeface.BOLD), 0, referralCode.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss.toString()
    }

    private fun initView() {
        showOrHideReferralAccordingToSessionData()

        getReferralInformationFromAPI()
    }

    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            binding.constraintLayoutReferralCode.visibility = View.VISIBLE
        } else {
            binding.constraintLayoutReferralCode.visibility = View.GONE
        }
    }

    private fun getReferralInformationFromAPI() {
        commonMethods.showProgressDialog(this)
        apiService.getReferralDetails(sessionManager.accessToken!!).enqueue(RequestCallback(this))
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (jsonResp.isSuccess) {
            binding.scvReferal.visibility = View.VISIBLE
            onSuccessResult(jsonResp.strResponse)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

        }
    }

    private fun onSuccessResult(strResponse: String) {
        referredFriendsModel = gson.fromJson(strResponse, ReferredFriendsModel::class.java)
        updateReferralCodeInUI()
        if (referredFriendsModel.pendingReferrals.size != 0 || referredFriendsModel.completedReferrals.size != 0) {

            showReferralsNotAvailable(true)

            proceedCompleteReferralDetails()
            proceedIncompleteReferralDetails()
        } else {
            showReferralsNotAvailable(false)
        }
    }

    private fun updateReferralCodeInUI() {
        referralCode = referredFriendsModel.referralCode
        referralLink = referredFriendsModel.referralLink
        binding.tvReferralCode.text = referralCode
        if ("1".equals(resources.getString(R.string.layout_direction), ignoreCase = true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, setCurrencyFrontForRTL(Html.fromHtml(referredFriendsModel.referralAmount,Html.FROM_HTML_MODE_LEGACY).toString()))
            }else {
                binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, setCurrencyFrontForRTL(Html.fromHtml(referredFriendsModel.referralAmount).toString()))
            }
        } else {
            binding.tvReferralBenifitText.text = resources.getString(R.string.max_referral_earning_statement, Html.fromHtml(referredFriendsModel.referralAmount).toString())
        }
        //binding.tvTotalEarned.append(referredFriendsModel.getTotalEarning().toString());
        binding.tvAmount.text = referredFriendsModel.totalEarning+")"
    }

    private fun setCurrencyFrontForRTL(amount: String): String {
        println("amount $amount")
        val currency = amount[0].toString()
        println("currency $currency")
        return amount.replace(currency, " ") + currency
    }

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

    private fun proceedIncompleteReferralDetails() {
        if (referredFriendsModel.pendingReferrals.size != 0) {
            binding.rvInCompletedReferrals.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            binding.rvInCompletedReferrals.layoutManager = layoutManager
            binding.rvInCompletedReferrals.adapter = ReferralFriendsListRecyclerViewAdapter(this, referredFriendsModel.pendingReferrals, IncompleteReferralArray)
        } else {
            binding.constraintLayoutInCompletedFriends.visibility = View.GONE
        }
    }

    private fun proceedCompleteReferralDetails() {
        if (referredFriendsModel.completedReferrals.size != 0) {
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
