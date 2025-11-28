package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by SMR IT Solutions on 8/9/18.
 */

class ScheduleDetail {


    @SerializedName("id")
    @Expose
    var id: String=""
    @SerializedName("user_id")
    @Expose
    var userId: String=""
    @SerializedName("pickup_latitude")
    @Expose
    var pickupLatitude: String=""
    @SerializedName("pickup_longitude")
    @Expose
    var pickupLongitude: String=""
    @SerializedName("drop_latitude")
    @Expose
    var dropLatitude: String=""
    @SerializedName("drop_longitude")
    @Expose
    var dropLongitude: String=""
    @SerializedName("pickup_location")
    @Expose
    var pickupLocation: String=""
    @SerializedName("drop_location")
    @Expose
    var dropLocation: String=""
    @SerializedName("car_id")
    @Expose
    var carId: String=""
    @SerializedName("schedule_date")
    @Expose
    var scheduleDate: String=""
    @SerializedName("schedule_time")
    @Expose
    var scheduleTime: String=""
    @SerializedName("schedule_display_date")
    @Expose
    var scheduleDisplayDate: String=""
    @SerializedName("status")
    @Expose
    var status: String=""
    @SerializedName("currency_symbol")
    @Expose
    var currencySymbol: String=""
    @SerializedName("car_name")
    @Expose
    var carName: String=""
    @SerializedName("fare_estimation")
    @Expose
    var fareEstimation: String=""

}
