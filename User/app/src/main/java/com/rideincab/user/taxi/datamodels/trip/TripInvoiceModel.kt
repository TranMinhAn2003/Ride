package com.rideincab.user.taxi.datamodels.trip

import java.util.ArrayList
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripInvoiceModel {

    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("total_time")
    @Expose
    var totalTime: Int = 0
    @SerializedName("pickup_location")
    @Expose
    var pickupLocation: String=""
    @SerializedName("drop_location")
    @Expose
    var dropLocation: String=""
    @SerializedName("payment_mode")
    @Expose
    var paymentMode: String=""
    @SerializedName("payment_status")
    @Expose
    var paymentStatus: String=""
    @SerializedName("applied_owe_amount")
    @Expose
    var appliedOweAmount: String=""
    @SerializedName("remaining_owe_amount")
    @Expose
    var remainingOweAmount: String=""
    @SerializedName("is_calculation")
    @Expose
    var isCalculation: String=""
    @SerializedName("invoice")
    @Expose
    var invoice: ArrayList<InvoiceModel> = ArrayList()
    @SerializedName("total_fare")
    @Expose
    var totalFare: String=""
    @SerializedName("currency_code")
    @Expose
    var currency: String=""
    @SerializedName("driver_payout")
    @Expose
    var driverPayout: String=""
    @SerializedName("promo_amount")
    @Expose
    var promoAmount: String=""
    @SerializedName("promo_details")
    @Expose
    var promoDetails: List<Any> = ArrayList()
    @SerializedName("trip_status")
    @Expose
    var tripStatus: String=""
    @SerializedName("trip_id")
    @Expose
    var tripId: Int = 0
    @SerializedName("driver_image")
    @Expose
    var driverImage: String=""
    @SerializedName("driver_name")
    @Expose
    var driverName: String=""
    @SerializedName("rider_image")
    @Expose
    var riderImage: String=""
    @SerializedName("rider_name")
    @Expose
    var riderName: String=""
    @SerializedName("paypal_app_id")
    @Expose
    var paypalAppId: String=""
    @SerializedName("paypal_mode")
    @Expose
    var paypalMode: Int = 0
}