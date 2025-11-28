package com.rideincab.user.common.utils

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

object CommonKeys {
    var keyCaller = ""
    var isSetPaymentMethod = false
    var isRequestAccepted = false
    var isPaymentOrCancel = false
    var isBeginTrip = false
    var isArrowNow = false
    var isEndTrip = false
    val ChangePaymentOpetionBeforeRequestCarApi = 124
    val IncompleteReferralArray = 358// just a random number
    val CompletedReferralArray = 247// just a random number
    val DeviceTypeAndroid = "2"
    var isLoggable: Boolean? = true
    val KEY_CALLER_ID = "caller_id"
    val PickupAddress = "PickupAddress"
    val DropAddress = "DropAddre//ioss"

    var Apple_Login_Scope = "name email"

    var getUrlCount = 0

    // payment types used in payment select activity for shared preference purpose
    val PAYMENT_PAYPAL = "paypal"
    val PAYMENT_CASH = "cash"
    val PAYMENT_CARD = "stripe"
    val PAYMENT_BRAINTREE = "braintree"
    val TYPE_INTENT_ARGUMENT_KEY = "type"

    const val PAY_FOR_WALLET = "wallet"
    const val PAY_FOR_TRIP = "trip"

    val DEFAULT_CARD = "stripe_card"
    val AFTER_PAYMENT = "after_payment"

    //val PAYMENT_WEBVIEW_KEY = "web_payment?"
    val PAYMENT_WEBVIEW_KEY = "web_payment"


    var FIREBASE_CHAT_MESSAGE_KEY = "message"
    var FIREBASE_CHAT_TYPE_KEY = "type"
    var FIREBASE_CHAT_TYPE_RIDER = "rider"
    var FIREBASE_CHAT_TYPE_DRIVER = "driver"

    var car_radius = "5"
    var IS_ALREADY_IN_TRIP = false

    var isFirstSetpaymentMethod = false
    var isWallet = 0

    /*Activity request codes*/
    val REQUEST_CODE_PAYMENT = 1


    val ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT = 102
    val ACTIVITY_REQUEST_CODE_SHOW_PEAK_HOURS_DETAILS = 103

    val FIREBASE_CHAT_ACTIVITY_SOURCE_ACTIVITY_TYPE_CODE = "sourceActivityCode"
    val FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_RIDER_OR_DRIVER_PROFILE = 110
    val FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_NOTIFICATION = 111
    val FACEBOOK_ACCOUNT_KIT_VERIFACATION_SUCCESS = 1


    val PEAK_PRICE_ACCEPTED = 1
    val PEAK_PRICE_DENIED = 0

    val YES = "Yes"
    val NO = "No"
    val KEY_PEAK_PRICE = "peakPrice"
    val KEY_MIN_FARE = "minimumFare"
    val KEY_PER_KM = "perKM"
    val KEY_PER_MINUTES = "perMin"
    val ISSCHEDULE = "yes"

    val FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY = "phoneNumber"
    val FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY = "countryCode"
    val FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_NAME_CODE_KEY = "countryNameCode"

    val API_KEY_HOME = "home"
    val API_KEY_WORK = "work"
    val API_KEY_LATITUDE = "latitude"
    val API_KEY_LONGITUDE = "longitude"
    val API_KEY_TOKEN = "token"


    @IntDef(StatusCode.startPaymentActivityForView, StatusCode.startPaymentActivityForChangePaymentOption, StatusCode.startPaymentActivityForAddMoneyToWallet)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class StatusCode {
        companion object {
            const val startPaymentActivityForView = 0
            const val startPaymentActivityForChangePaymentOption = 1
            const val startPaymentActivityForAddMoneyToWallet = 2
        }


    }

    @StringDef(TripStatus.BeginTrip, TripStatus.ArriveNow, TripStatus.AcceptRequest, TripStatus.EndTrip)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class TripStatus {
        companion object {
            const val BeginTrip = "begin_trip"
            const val ArriveNow = "arrive_now"
            const val EndTrip = "end_trip"
            const val AcceptRequest = "accept_request"
        }

    }

    @IntDef(FirebaseChatserviceTriggeredFrom.backgroundService, FirebaseChatserviceTriggeredFrom.chatActivity)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class FirebaseChatserviceTriggeredFrom {
        companion object {
            const val backgroundService = 0
            const val chatActivity = 1
        }
    }


    @StringDef(DownloadTask.DrawRoute, DownloadTask.MoveMarker)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DownloadTask {
        companion object {
            const val DrawRoute = "DRAW ROUTE"
            const val MoveMarker = "MOVE MARKER"

        }
    }

}