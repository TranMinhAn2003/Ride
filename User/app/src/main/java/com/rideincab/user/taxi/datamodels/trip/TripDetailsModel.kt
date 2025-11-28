package com.rideincab.user.taxi.datamodels.trip

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage sendrequest
 * @category TripDetailsModel Model
 * @author SMR IT Solutions
 *
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/* ****************************************************************
    Get Accepted driver details
   **************************************************************** */



class TripDetailsModel : Serializable {

    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""

    @SerializedName("status_message")
    @Expose
    var statusMessage: String = ""

    @SerializedName("driver_latitude")
    @Expose
    var driverLatitude: String = ""

    @SerializedName("driver_longitude")
    @Expose
    var driverLongitude: String = ""

    @SerializedName("vehicle_number")
    @Expose
    var vehicleNumber: String = ""

    @SerializedName("vehicle_name")
    @Expose
    var vehicleName: String = ""

    @SerializedName("arrivar_time")
    @Expose
    var arrivarTime: Int = 0

    @SerializedName("is_pool")
    @Expose
    var isPool: Boolean? = null

    @SerializedName("seats")
    @Expose
    var seats: Int = 0

    @SerializedName("riders")
    @Expose
    var riders = ArrayList<Riders>()

    @SerializedName("driver_id")
    @Expose
    var driverId: String? = null

    @SerializedName("driver_name")
    @Expose
    var driverName: String = ""

    @SerializedName("mobile_number")
    @Expose
    var mobileNumber: String = ""

    @SerializedName("driver_thumb_image")
    @Expose
    var driverThumbImage: String = ""

    @SerializedName("rating")
    @Expose
    var rating: String = ""

}

class Riders : Serializable {
    @SerializedName("otp")
    @Expose
    var otp: String = ""

    @SerializedName("status")
    @Expose
    var status: String = ""

    @SerializedName("trip_id")
    @Expose
    var tripId: Int? = null

    @SerializedName("pickup")
    @Expose
    var pickup: String = ""

    @SerializedName("drop")
    @Expose
    var drop: String = ""

    @SerializedName("pickup_lat")
    @Expose
    var pickupLat: String = ""

    @SerializedName("pickup_lng")
    @Expose
    var pickupLng: String = ""

    @SerializedName("drop_lat")
    @Expose
    var dropLat: String = ""

    @SerializedName("drop_lng")
    @Expose
    var dropLng: String = ""

    @SerializedName("map_image")
    @Expose
    var mapImage: String = ""

    @SerializedName("car_type")
    @Expose
    var carType: String = ""

    @SerializedName("waiting_time")
    @Expose
    var waitingTime: String = ""

    @SerializedName("waiting_charge")
    @Expose
    var waitingCharge: String = ""

    @SerializedName("total_fare")
    @Expose
    var totalFare: String = ""

    @SerializedName("booking_type")
    @Expose
    var bookingType: String = ""

    @SerializedName("schedule_display_date")
    @Expose
    var scheduleDisplayDate: String = ""


    @SerializedName("created_at")
    @Expose
    var creatdate: String = ""

    @SerializedName("trip_path")
    @Expose
    var tripPath: String = ""


    @SerializedName("invoice")
    @Expose
    var invoice = ArrayList<InvoiceModel>()

}
