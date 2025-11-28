package com.rideincab.driver.home.payouts.payout_model_classed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

/**
 * Created by SMR IT Solutions on 3/8/18.
 */

class PayoutDetailResult {

    @SerializedName("success_message")
    @Expose
    var statusMessage: String= ""

    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""

    @SerializedName("payout_details")
    @Expose
    var payout_details: ArrayList<PayoutDetail> = ArrayList()
}
