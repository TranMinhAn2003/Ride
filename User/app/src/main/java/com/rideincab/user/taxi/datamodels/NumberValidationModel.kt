package com.rideincab.user.taxi.datamodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NumberValidationModel {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String = ""
    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""

}