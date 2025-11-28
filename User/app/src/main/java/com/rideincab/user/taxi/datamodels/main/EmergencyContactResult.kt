package com.rideincab.user.taxi.datamodels.main

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rideincab.user.taxi.datamodels.EmergencyContactModel

import java.util.ArrayList

/**
 * Created by SMR IT Solutions on 12/9/18.
 */

class EmergencyContactResult {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""
    @SerializedName("contact_count")
    @Expose
    var contactCount: Int = 0
    @SerializedName("contact_details")
    @Expose
    var contactDetails: ArrayList<EmergencyContactModel> = ArrayList()
}
