package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

class PromoDetail : Serializable {
    @SerializedName("id")
    @Expose
    var id: Int = 0
    @SerializedName("code")
    @Expose
    var code: String=""
    @SerializedName("amount")
    @Expose
    var amount: String=""
    @SerializedName("expire_date")
    @Expose
    var expireDate: String=""
}
