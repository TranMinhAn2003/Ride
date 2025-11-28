package com.rideincab.user.taxi.datamodels.trip

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

/**
 * Created by SMR IT Solutions on 8/9/18.
 */

class TripDetailModel {


    var staticMapURL: String=""
        private set

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
    @SerializedName("request_id")
    @Expose
    var requestId: String=""
    @SerializedName("driver_id")
    @Expose
    var driverId: String=""
    @SerializedName("total_time")
    @Expose
    var totalTime: String=""
    @SerializedName("total_km")
    @Expose
    var totalKm: String=""
    @SerializedName("time_fare")
    @Expose
    var timeFare: String=""
    @SerializedName("distance_fare")
    @Expose
    var distanceFare: String=""
    @SerializedName("base_fare")
    @Expose
    var baseFare: String=""
    @SerializedName("total_fare")
    @Expose
    var totalFare: String=""
    @SerializedName("access_fee")
    @Expose
    var accessFee: String=""
    @SerializedName("status")
    @Expose
    var status: String=""
    @SerializedName("created_at")
    @Expose
    var createdAt: String=""
    @SerializedName("updated_at")
    @Expose
    var updatedAt: String=""
    @SerializedName("vehicle_name")
    @Expose
    var vehicleName: String=""
    @SerializedName("deleted_at")
    @Expose
    var deletedAt: String=""
    @SerializedName("driver_name")
    @Expose
    var driverName: String=""
    @SerializedName("driver_thumb_image")
    @Expose
    var driverThumbImage: String=""
    @SerializedName("trip_path")
    @Expose
    var tripPath: String=""
    @SerializedName("owe_amount")
    @Expose
    var oweAmount: String=""
    @SerializedName("applied_owe_amount")
    @Expose
    var appliedOweAmount: String=""
    @SerializedName("wallet_amount")
    @Expose
    var walletAmount: String=""
    @SerializedName("promo_amount")
    @Expose
    var promoAmount: String=""
    @SerializedName("driver_payout")
    @Expose
    var driverPayout: String=""
    @SerializedName("payment_method")
    @Expose
    var paymentMethod: String=""
    @SerializedName("remaining_owe_amount")
    @Expose
    var remainingOweAmount: String=""
    @SerializedName("currency_code")
    @Expose
    var currencyCode: String=""
    @SerializedName("map_image")
    @Expose
    var mapImage: String=""
    @SerializedName("invoice")
    @Expose
    var invoice: ArrayList<InvoiceModel> = ArrayList()

    fun setStaticMapUR(staticMapURL: String) {
        this.staticMapURL = staticMapURL
    }
}
