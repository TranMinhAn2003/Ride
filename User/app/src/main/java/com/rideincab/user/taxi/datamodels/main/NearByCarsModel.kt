package com.rideincab.user.taxi.datamodels.main

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NearByCarsModel {
    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("data")
    @Expose
    var data: List<Datum> = ArrayList()

    inner class Datum {

        @SerializedName("driver_id")
        @Expose
        var driverId: Int = 0
        @SerializedName("vehicle_id")
        @Expose
        var vehicleId: Int = 0
        @SerializedName("vehicle_type")
        @Expose
        var vehicleType: String=""
        @SerializedName("latitude")
        @Expose
        var latitude: String=""
        @SerializedName("longitude")
        @Expose
        var longitude: String=""

    }

}
