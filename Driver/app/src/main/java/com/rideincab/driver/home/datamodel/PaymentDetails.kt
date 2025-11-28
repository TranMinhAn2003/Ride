package com.rideincab.driver.home.datamodel

/**
 * Created by SMR IT Solutions on 9/12/18.
 */


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable


class PaymentDetails : Serializable {

    @SerializedName("currency_code")
    @Expose
    var currencyCode: String? = null
    @SerializedName("pickup_location")
    @Expose
    var pickupLocation: String? = null
    @SerializedName("drop_location")
    @Expose
    var dropLocation: String? = null
    @SerializedName("status")
    @Expose
    var tripStatus: String? = null
    @SerializedName("payment_method")
    @Expose
    var paymentMethod: String? = null
}
