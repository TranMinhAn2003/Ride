package com.rideincab.user.taxi.firebase_keys

import androidx.annotation.StringDef

object FirebaseDbKeys {


    var chatFirebaseDatabaseName = "driver_rider_trip_chats"

    var Rider = "rider"
    var Notification = "Notification"
    var TripId = "trip_id"
    var Status = "status"
    var RELEASE_TYPE = "live"
    var GEOFIRE = "GeoFire"
    var TRIP_PAYMENT_NODE = "trip"
    var LIVE_TRACKING_NODE = "live_tracking"
    var TRIP_PAYMENT_NODE_REFRESH_PAYMENT_TYPE_KEY = "refresh_payment_screen"
    var TRIPLIVEPOLYLINE = "path"
    var TRIPETA = "eta_min"


    @StringDef(PaymentChangeMode.TYPE_CASH, PaymentChangeMode.TYPE_STRIPE, PaymentChangeMode.TYPE_PAYPAL, PaymentChangeMode.WALLET_ADDED, PaymentChangeMode.PROMO_ADDED, PaymentChangeMode.TIPS_ADDED)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class PaymentChangeMode {
        companion object {
            const val TYPE_CASH = "C"
            const val TYPE_STRIPE = "S"
            const val TYPE_PAYPAL = "P"
            const val TYPE_BRAINTREE = "B"
            const val TIPS_ADDED = "T"
            const val WALLET_ADDED = "W"
            const val PROMO_ADDED = "P"
        }
    }
}