package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class RiderTripsModel {

    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("total_pages")
    @Expose
    var totalPages: Int = 0
    @SerializedName("data")
    @Expose
    var tripDetailsModel: ArrayList<TripDetailsModel> = ArrayList()
}