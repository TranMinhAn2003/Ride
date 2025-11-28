package com.rideincab.driver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by SMR IT Solutions on 9/12/18.
 */

class RiderFeedBackModel : Serializable {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("total_pages")
    @Expose
    var totalPages: String? = null
    @SerializedName("rider_feedback")
    @Expose
    var riderFeedBack: ArrayList<RiderFeedBackArrayModel>? = null
}
