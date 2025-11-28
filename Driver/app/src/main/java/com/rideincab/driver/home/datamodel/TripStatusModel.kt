package com.rideincab.driver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripStatusModel {


    @SerializedName("status")
    @Expose
    var status: String? = null


    @SerializedName("data")
    @Expose
    var tripDetailsModel: TripDetailsModel? = null



}