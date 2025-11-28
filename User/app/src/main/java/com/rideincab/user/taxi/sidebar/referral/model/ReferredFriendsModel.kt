package com.rideincab.user.taxi.sidebar.referral.model


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ReferredFriendsModel {

    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""
    @SerializedName("status_message")
    @Expose
    var statusMessage: String = ""
    @SerializedName("referral_code")
    @Expose
    var referralCode: String = ""
    @SerializedName("referral_link")
    @Expose
    var referralLink: String = ""
    @SerializedName("total_earning")
    @Expose
    var totalEarning: String = ""
    @SerializedName("pending_referrals")
    @Expose
    var pendingReferrals: List<CompletedOrPendingReferrals> = ArrayList()
    @SerializedName("completed_referrals")
    @Expose
    var completedReferrals: List<CompletedOrPendingReferrals> = ArrayList()
    @SerializedName("referral_amount")
    @Expose
    var referralAmount: String = ""
}