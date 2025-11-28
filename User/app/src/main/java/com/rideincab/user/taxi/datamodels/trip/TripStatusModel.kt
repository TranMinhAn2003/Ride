package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripStatusModel {

    @SerializedName("trip_id")
    @Expose
    var tripId: Int = 0
    @SerializedName("car_id")
    @Expose
    var carId: Int = 0
    @SerializedName("trip_path")
    @Expose
    var tripPath: String=""
    @SerializedName("total_fare")
    @Expose
    var totalFare: String=""
    @SerializedName("subtotal_fare")
    @Expose
    var subtotalFare: String=""
    @SerializedName("driver_payout")
    @Expose
    var driverPayout: String=""
    @SerializedName("car_name")
    @Expose
    var carName: String=""
    @SerializedName("map_image")
    @Expose
    var mapImage: String=""
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
    @SerializedName("booking_type")
    @Expose
    var bookingType: String=""
    @SerializedName("currency_symbol")
    @Expose
    var currencySymbol: String=""
    @SerializedName("status")
    @Expose
    var status: String=""

    @SerializedName("driver_name")
    @Expose
    var driverName: String=""


    @SerializedName("driver_thumb_image")
    @Expose
    var driverThumbImage: String=""

    /**
     * Schedule Date and Time only for Upcoming Trips
     */
    @SerializedName("schedule_time")
    @Expose
    var scheduleTime: String=""

    @SerializedName("schedule_date")
    @Expose
    var scheduleDate: String=""

    @SerializedName("pickup_location")
    @Expose
    var pickupLocation: String=""

    @SerializedName("drop_location")
    @Expose
    var dropLocation: String=""

    @SerializedName("schedule_display_date")
    @Expose
    var scheduleDisplayDate: String=""

}