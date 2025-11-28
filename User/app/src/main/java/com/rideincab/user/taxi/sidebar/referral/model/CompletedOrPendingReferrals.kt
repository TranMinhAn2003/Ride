package com.rideincab.user.taxi.sidebar.referral.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CompletedOrPendingReferrals {
    @SerializedName("name")
    @Expose
    var name: String = ""
    @SerializedName("profile_image")
    @Expose
    var profileImage: String = ""
    @SerializedName("remaining_days")
    @Expose
    var remainingDays: Long =0
    @SerializedName("remaining_trips")
    @Expose
    var remainingTrips: Long = 0
    @SerializedName("earnable_amounts")
    @Expose
    var earnableAmount: String = ""
    @SerializedName("payment_status")
    @Expose
    var status: String = ""

}