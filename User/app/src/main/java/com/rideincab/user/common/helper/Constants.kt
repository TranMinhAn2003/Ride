package com.rideincab.user.common.helper

import com.google.android.gms.maps.model.LatLng
import com.rideincab.user.R

/**
 * @author SMR IT Solutions
 * 
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage helper
 * @category Constants
 */

/* ************************************************************
 Constants values for shared preferences
*************************************************************** */

object Constants {
    const val PICK_IMAGE_REQUEST_CODE = 1888
    const val SELECT_FILE = 1

    val notificationIcon = R.drawable.ic_notification
    const val scheduledDuration = 15
    const val Payment = 250

    var chatPageVisible = false
    val STATUS_MSG = "status_message"
    val STATUS_CODE = "status_code"
    val REFRESH_ACCESS_TOKEN = "refresh_token"
    const val REQUEST_CODE_GALLERY = 5
    val carname = "carname" // save currency code
    var changedPickupLatlng: LatLng? =null

    val Male = "male"
    val Female = "female"

    const val SKIP_UPDATE = "skip_update"
    const val FORCE_UPDATE = "force_update"

    const val INTENT_PAY_FOR = "pay_for"
    const val PAY_FOR_TYPE = "type"

    const val Req_TYPE_POOL = "Pool"
    const val Req_TYPE_NORMAL = "normal"


    /**************** DB Keys*************/
    val DB_KEY_TRIP_DETAILS: String? = "TripDetails"
    val DB_KEY_USERDETAILS: String? = "UserDetails"
    val DB_KEY_USER_EMERGENCY: String? = "UserEmergency"
    val DB_KEY_PAST_TRIPS: String? = "UserPastTrips"
    val DB_KEY_UPCOMING_TRIPS: String? = "UserUpcomingTrips"

}
