package com.rideincab.user.taxi.datamodels.main

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * Created by SMR IT Solutions on 7/9/18.
 */

class NearestCar : Serializable {

    var isSelected: Boolean = false
    /* @SerializedName("km")
    @Expose
    private ArrayList<LocationModel> km;*/

    @SerializedName("min_time")
    @Expose
    var minTime: String=""
    @SerializedName("car_description")
    @Expose
    var carDescription: String=""
    @SerializedName("driver_id")
    @Expose
    var driverId: String=""
    @SerializedName("car_id")
    @Expose
    var carId: String=""
    @SerializedName("car_name")
    @Expose
    var carName: String=""
    @SerializedName("base_fare")
    @Expose
    var baseFare: String=""

    @SerializedName("waiting_time")
    @Expose
    var waitingTime: String=""

    @SerializedName("is_pool")
    @Expose
    var isPool: Boolean=false

    @SerializedName("waiting_charge")
    @Expose
    var waitingCharge: String=""

    @SerializedName("min_fare")
    @Expose
    var minFare: String=""
    @SerializedName("per_min")
    @Expose
    var perMin: String=""
    @SerializedName("per_km")
    @Expose
    var perKm: String=""
    @SerializedName("schedule_fare")
    @Expose
    var scheduleFare: String=""
    @SerializedName("schedule_cancel_fare")
    @Expose
    var scheduleCancelFare: String=""
    @SerializedName("capacity")
    @Expose
    var capacity: String=""
    @SerializedName("fare_estimation")
    @Expose
    var fareEstimation: String=""
    @SerializedName("apply_fare")
    @Expose
    var applyFare: String=""
    @SerializedName("location")//location
    @Expose
    var location: ArrayList<LocationModel> = ArrayList()

    @SerializedName("apply_peak")
    @Expose
    var applyPeak: String=""

    @SerializedName("peak_price")
    @Expose
    var peakPrice: String=""

    @SerializedName("additional_rider_percentage")
    @Expose
    var peakPricePercentage: String=""

    @SerializedName("peak_id")
    @Expose
    var peakId: String=""


    @SerializedName("car_image")
    @Expose
    var carImage: String=""

    @SerializedName("car_active_image")
    @Expose
    var carActiveImage: String=""

    @SerializedName("location_id")
    @Expose
    var locationID: String=""

    @SerializedName("peak_id")
    @Expose
    var peak_id: String=""

    @SerializedName("drivers")
    @Expose
    var id: ArrayList<drivers> = ArrayList()

    fun getCarIsSelected(): Boolean {
        return isSelected
    }

    fun setCarIsSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }
}

class drivers {
    @SerializedName("drivers")
    @Expose
    var id: String=""
}
