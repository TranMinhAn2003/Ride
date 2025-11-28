package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CancelResultModel {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("cancel_reasons")
    @Expose
    var cancelReasons: List<CancelReasonModel> = ArrayList()

}
