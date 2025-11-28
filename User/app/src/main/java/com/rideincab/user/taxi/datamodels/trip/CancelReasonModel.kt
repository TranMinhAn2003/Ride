package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CancelReasonModel {

    @SerializedName("id")
    @Expose
    var id: Int = 0
    @SerializedName("reason")
    @Expose
    var reason: String=""
    @SerializedName("status")
    @Expose
    var status: String=""


}