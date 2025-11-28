package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

class InvoiceModel : Serializable {

    @SerializedName("key")
    @Expose
    var key: String=""
    @SerializedName("value")
    @Expose
    var value: String=""

    @SerializedName("bar")
    @Expose
    var bar: String=""
    @SerializedName("colour")
    @Expose
    var colour: String=""

    @SerializedName("comment")
    @Expose
    var fareComments: String=""
}
