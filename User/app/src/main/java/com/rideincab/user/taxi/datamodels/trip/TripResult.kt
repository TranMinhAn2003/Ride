package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

/**
 * Created by SMR IT Solutions on 8/9/18.
 */

class TripResult {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String = ""
    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""
    @SerializedName("trip_details")
    @Expose
    var tripDetail: ArrayList<TripDetailModel> = ArrayList()
    @SerializedName("schedule_ride")
    @Expose
    var scheduleTrip: ArrayList<ScheduleDetail> = ArrayList()
}
