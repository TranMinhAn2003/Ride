package com.rideincab.user.common.configs

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage configs
 * @category SessionManager
 * @author SMR IT Solutions
 * 
 */

import android.content.SharedPreferences
import com.rideincab.user.common.network.AppController
import javax.inject.Inject

/*****************************************************************
 * Session manager to set and get glopal values
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SessionManager {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    init {
        AppController.appComponent.inject(this)
    }

    var token: String?
        get() = sharedPreferences.getString("token", "")
        set(token) = sharedPreferences.edit().putString("token", token).apply()

    var accessToken: String?
        get() = sharedPreferences.getString("access_token", "")
        set(accessToken) = sharedPreferences.edit().putString("access_token", accessToken).apply()

    var firebaseCustomToken: String?
        get() = sharedPreferences.getString("firebaseCustomToken", "")
        set(firebaseCustomToken) = sharedPreferences.edit().putString("firebaseCustomToken", firebaseCustomToken).apply()

    var defaultPayment: String?
        get() = sharedPreferences.getString("defaultPayment", "")
        set(defaultPayment) = sharedPreferences.edit().putString("defaultPayment", defaultPayment).apply()

    var isFirebaseTokenUpdated: Boolean
        get() = sharedPreferences.getBoolean("isFirebaseTokenUpdated", false)
        set(isFirebaseTokenUpdated) = sharedPreferences.edit().putBoolean("isFirebaseTokenUpdated", isFirebaseTokenUpdated).apply()

    var dialCode: String?
        get() = sharedPreferences.getString("dialCode", "")
        set(dialCode) = sharedPreferences.edit().putString("dialCode", dialCode).apply()

    var googleMapKey: String?
        get() = sharedPreferences.getString("google_map_key", "")
        set(google_map_key) = sharedPreferences.edit().putString("google_map_key", google_map_key).apply()

    var scheduleDateTime: String?
        get() = sharedPreferences.getString("date_time_for_schedule", "")
        set(date_time_to_save) = sharedPreferences.edit().putString("date_time_for_schedule", date_time_to_save).apply()

    var scheduleDate: String?
        get() = sharedPreferences.getString("date_for_schedule", "")
        set(date_for_schedule) = sharedPreferences.edit().putString("date_for_schedule", date_for_schedule).apply()

    var presentTime: String?
        get() = sharedPreferences.getString("present_time_for_schedule", "")
        set(present_time_for_schedule) = sharedPreferences.edit().putString("present_time_for_schedule", present_time_for_schedule).apply()

    var carName: String?
        get() = sharedPreferences.getString("carname", "")
        set(carname) = sharedPreferences.edit().putString("carname", carname).apply()

    var pushJson: String?
        get() = sharedPreferences.getString("json", "")
        set(PushJson) = sharedPreferences.edit().putString("json", PushJson).apply()

    var type: String?
        get() = sharedPreferences.getString("type", "")
        set(type) = sharedPreferences.edit().putString("type", type).apply()


    var requestId: String?
        get() = sharedPreferences.getString("requestId", "")
        set(PushJson) = sharedPreferences.edit().putString("requestId", PushJson).apply()

    var deviceType: String?
        get() = sharedPreferences.getString("devicetype", "")
        set(devicetype) = sharedPreferences.edit().putString("devicetype", devicetype).apply()

    var isDriverAndRiderAbleToChat: Boolean
        get() = sharedPreferences.getBoolean("setDriverAndRiderAbleToChat", false)
        set(status) = sharedPreferences.edit().putBoolean("setDriverAndRiderAbleToChat", status).apply()

    var language: String?
        get() = sharedPreferences.getString("language", "")
        set(language) = sharedPreferences.edit().putString("language", language).apply()

    var languageCode: String?
        get() = sharedPreferences.getString("languagecode", "")
        set(languagecode) = sharedPreferences.edit().putString("languagecode", languagecode).apply()

    var bookingType: String?
        get() = sharedPreferences.getString("bookingType", "")
        set(bookingType) = sharedPreferences.edit().putString("bookingType", bookingType).apply()

    var facebookId: String?
        get() = sharedPreferences.getString("facebookid", "")
        set(facebookid) = sharedPreferences.edit().putString("facebookid", facebookid).apply()

    var appleId: String?
        get() = sharedPreferences.getString("appleid", "")
        set(appleid) = sharedPreferences.edit().putString("appleid", appleid).apply()

    var googleId: String?
        get() = sharedPreferences.getString("googleid", "")
        set(languagecode) = sharedPreferences.edit().putString("googleid", languagecode).apply()

    var profilepicture: String?
        get() = sharedPreferences.getString("profilepicture", "")
        set(gender) = sharedPreferences.edit().putString("profilepicture", gender).apply()

    var currency: String?
        get() = sharedPreferences.getString("currency", "")
        set(currency) = sharedPreferences.edit().putString("currency", currency).apply()

    var firstName: String?
        get() = sharedPreferences.getString("firstname", "")
        set(firstName) = sharedPreferences.edit().putString("firstname", firstName).apply()

    var lastName: String?
        get() = sharedPreferences.getString("lastname", "")
        set(lastName) = sharedPreferences.edit().putString("lastname", lastName).apply()



    var password: String?
        get() = sharedPreferences.getString("password", "")
        set(password) = sharedPreferences.edit().putString("password", password).apply()

    var phoneNumber: String?
        get() = sharedPreferences.getString("phoneNumber", "")
        set(phoneNumber) = sharedPreferences.edit().putString("phoneNumber", phoneNumber).apply()

    // this is temporary phone number and country code, this data will passed to facebook account kit
    var temporaryPhonenumber: String?
        get() = sharedPreferences.getString("TemporaryPhonenumber", "")
        set(phoneNumber) = sharedPreferences.edit().putString("TemporaryPhonenumber", phoneNumber).apply()

    var temporaryCountryCode: String?
        get() = sharedPreferences.getString("TemporaryCountryCode", "")
        set(countryCode) = sharedPreferences.edit().putString("TemporaryCountryCode", countryCode).apply()

    var countryCode: String?
        get() = sharedPreferences.getString("countryCode", "")
        set(countryCode) = sharedPreferences.edit().putString("countryCode", countryCode).apply()

  var countryNameCode: String?
        get() = sharedPreferences.getString("countryNameCode", "")
        set(countryNameCode) = sharedPreferences.edit().putString("countryNameCode", countryNameCode).apply()

    var deviceId: String?
        get() = sharedPreferences.getString("deviceId", "")
        set(deviceId) = sharedPreferences.edit().putString("deviceId", deviceId).apply()

    var tripId: String?
        get() = sharedPreferences.getString("tripId", "")
        set(tripId) = sharedPreferences.edit().putString("tripId", tripId).apply()

    var isUpdateLocation: Int
        get() = sharedPreferences.getInt("isupldatelocation", 0)
        set(isupldatelocation) = sharedPreferences.edit().putInt("isupldatelocation", isupldatelocation).apply()

    var issignin: Int
        get() = sharedPreferences.getInt("issignin", 0)
        set(issignin) = sharedPreferences.edit().putInt("issignin", issignin).apply()

    var tripStatus: String?
        get() = sharedPreferences.getString("tripStatus", "")
        set(tripStatus) = sharedPreferences.edit().putString("tripStatus", tripStatus).apply()

    var promoDetail: String?
        get() = sharedPreferences.getString("PromoDetail", "")
        set(PromoDetail) = sharedPreferences.edit().putString("PromoDetail", PromoDetail).apply()

    var promoCount: Int
        get() = sharedPreferences.getInt("vehicleId", 0)
        set(vehicleId) = sharedPreferences.edit().putInt("vehicleId", vehicleId).apply()

    var currencyCode: String?
        get() = sharedPreferences.getString("currencyCode", "")
        set(currencyCode) = sharedPreferences.edit().putString("currencyCode", currencyCode).apply()

    var currencySymbol: String?
        get() = sharedPreferences.getString("currencysymbol", "")
        set(currencySymbol) = sharedPreferences.edit().putString("currencysymbol", currencySymbol).apply()

    var estimatedFare: String?
        get() = sharedPreferences.getString("estimatedFare", "0")
        set(estimatedFare) = sharedPreferences.edit().putString("estimatedFare", estimatedFare).apply()

    var homeAddress: String?
        get() = sharedPreferences.getString("homeadresstext", "")
        set(homeadresstext) = sharedPreferences.edit().putString("homeadresstext", homeadresstext).apply()

    var workAddress: String?
        get() = sharedPreferences.getString("workadresstext", "")
        set(workadresstext) = sharedPreferences.edit().putString("workadresstext", workadresstext).apply()

    var profileDetail: String?
        get() = sharedPreferences.getString("profilearratdetail", "")
        set(profilearratdetail) = sharedPreferences.edit().putString("profilearratdetail", profilearratdetail).apply()

    var paymentMethodDetail: String?
        get() = sharedPreferences.getString("paymentMethodDetail", "")
        set(paymentMethodDetail) = sharedPreferences.edit().putString("paymentMethodDetail", paymentMethodDetail).apply()

    var paymentMethod: String?
        get() = sharedPreferences.getString("paymentMethod", "")
        set(paymentMethod) = sharedPreferences.edit().putString("paymentMethod", paymentMethod).apply()

    var paymentMethodkey: String?
        get() = sharedPreferences.getString("paymentMethodkey", "")
        set(paymentMethodkey) = sharedPreferences.edit().putString("paymentMethodkey", paymentMethodkey).apply()

    var paymentMethodImage: String?
        get() = sharedPreferences.getString("paymentMethodImage", "")
        set(paymentMethodImage) = sharedPreferences.edit().putString("paymentMethodImage", paymentMethodImage).apply()


    var walletPaymentMethod: String?
        get() = sharedPreferences.getString("walletpaymentmethod", "")
        set(walletpaymentmethod) = sharedPreferences.edit().putString("walletpaymentmethod", walletpaymentmethod).apply()

    var walletPaymentMethodkey: String?
        get() = sharedPreferences.getString("walletPaymentMethodkey", "")
        set(walletPaymentMethodkey) = sharedPreferences.edit().putString("walletPaymentMethodkey", walletPaymentMethodkey).apply()
    var cardValue: String?
        get() = sharedPreferences.getString("cardValue", "")
        set(cardValue) = sharedPreferences.edit().putString("cardValue", cardValue).apply()

    var cardBrand: String?
        get() = sharedPreferences.getString("cardBrand", "")
        set(cardBrand) = sharedPreferences.edit().putString("cardBrand", cardBrand).apply()

    var isrequest: Boolean
        get() = sharedPreferences.getBoolean("isrequest", false)
        set(isrequest)= sharedPreferences.edit().putBoolean("isrequest",isrequest).apply()

    var isTrip: Boolean
        get() = sharedPreferences.getBoolean("istrip", false)
        set(istrip)= sharedPreferences.edit().putBoolean("istrip",istrip).apply()

    var isWallet: Boolean
        get() = sharedPreferences.getBoolean("isWallet", false)
        set(isWallet)= sharedPreferences.edit().putBoolean("isWallet",isWallet).apply()

    var walletAmount: String?
        get() = sharedPreferences.getString("wallet_amount", "")
        set(walletCard) = sharedPreferences.edit().putString("wallet_amount", walletCard).apply()

    var driverProfilePic: String?
        get() = sharedPreferences.getString("driverProfilePic", "")
        set(url) = sharedPreferences.edit().putString("driverProfilePic", url).apply()

    var driverId: String?
        get() = sharedPreferences.getString("driverID", "")
        set(url) = sharedPreferences.edit().putString("driverID", url).apply()

    var driverRating: String?
        get() = sharedPreferences.getString("driverRatingValue", "")
        set(ratingvalue) = sharedPreferences.edit().putString("driverRatingValue", ratingvalue).apply()

    var driverName: String?
        get() = sharedPreferences.getString("driverName", "")
        set(drivername) = sharedPreferences.edit().putString("driverName", drivername).apply()

    var adminContact: String?
        get() = sharedPreferences.getString("AdminContact", "")
        set(AdminContact) = sharedPreferences.edit().putString("AdminContact", AdminContact).apply()

    var userId: String?
        get() = sharedPreferences.getString("UserId", "")
        set(UserId) = sharedPreferences.edit().putString("UserId", UserId).apply()
    var scheduledDateAndTime: String?
        get() = sharedPreferences.getString("ScheduledDateAndTime", "")
        set(ScheduledDateAndTime) = sharedPreferences.edit().putString("ScheduledDateAndTime", ScheduledDateAndTime).apply()

    var isReferralOptionEnabled: Boolean
        get() = sharedPreferences.getBoolean("safkey64", true)
        set(isReferralOptionEnabled)=sharedPreferences.edit().putBoolean("safkey64", isReferralOptionEnabled).apply()

    var appleLoginClientId: String?
        get() = sharedPreferences.getString("appleLoginClientId", "")
        set(appleLoginClientId)=sharedPreferences.edit().putString("appleLoginClientId", appleLoginClientId).apply()

    var sinchKey: String?
        get() = sharedPreferences.getString("weasqr", "")
        set(sinchKey) = sharedPreferences.edit().putString("weasqr", sinchKey).apply()

    var sinchSecret: String?
        get() = sharedPreferences.getString("udueuw", "")
        set(sinchSecret) = sharedPreferences.edit().putString("udueuw", sinchSecret).apply()

    var brainTreeClientToken: String?
        get() = sharedPreferences.getString("BrainTreeClientToken", "")
        set(BrainTreeClientToken) = sharedPreferences.edit().putString("BrainTreeClientToken", BrainTreeClientToken).apply()

    var email: String?
        get() = sharedPreferences.getString("email", "")
        set(email) = sharedPreferences.edit().putString("email", email).apply()

    fun clearToken() {
        sharedPreferences.edit().putString("token", "").apply()
    }

    fun clearPaymentType() {
        sharedPreferences.edit().putString("paymentMethod", "").apply()
        sharedPreferences.edit().putString("paymentMethodImage", "").apply()
        sharedPreferences.edit().putString("walletPaymentMethod", "").apply()
        sharedPreferences.edit().putString("paymentMethodkey", "").apply()
        sharedPreferences.edit().putString("walletPaymentMethodkey", "").apply()
    }
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        type = "2"
    }

    fun clearTripID() {
        sharedPreferences.edit().remove("tripId").apply()
    }


    var stripePublishKey: String?
        get() = sharedPreferences.getString("StripePublishKey", "")
        set(stripePublishKey) =sharedPreferences.edit().putString("StripePublishKey",stripePublishKey).apply()


    var paypal_mode: Int
        get() = sharedPreferences.getInt("paypal_mode", 0)
        set(paypal_mode) =sharedPreferences.edit().putInt("paypal_mode",paypal_mode).apply()

    var paypal_app_id: String?
        get() = sharedPreferences.getString("paypal_app_id", "")
        set(paypal_app_id) =sharedPreferences.edit().putString("paypal_app_id",paypal_app_id).apply()

    var braintree_public_key: String?
        get() = sharedPreferences.getString("braintree_public_key", "")
        set(braintree_public_key) =sharedPreferences.edit().putString("braintree_public_key",braintree_public_key).apply()

    var braintree_env: String?
        get() = sharedPreferences.getString("braintree_env", "")
        set(braintree_env) =sharedPreferences.edit().putString("braintree_env",braintree_env).apply()

    var payementModeWebView: Boolean?
        get() = sharedPreferences.getBoolean("payementModeWebView", false)
        set(payementModeWebView) =sharedPreferences.edit().putBoolean("payementModeWebView",payementModeWebView!!).apply()

    var currentAddress: String?
        get() = sharedPreferences.getString("currentAddress", "")
        set(currentAddress) = sharedPreferences.edit().putString("currentAddress", currentAddress).apply()

    var isCovidFeature: Boolean?
        get() = sharedPreferences.getBoolean("isCovidFeature", false)
        set(isCovidFeature) =sharedPreferences.edit().putBoolean("isCovidFeature",isCovidFeature!!).apply()


    var notificationID: String
        get() = sharedPreferences.getString("notificationID", "").toString()
        set(notificationID) = sharedPreferences.edit().putString("notificationID", notificationID).apply()

    var isDialogShown: String
        get() = sharedPreferences.getString("isDialogShown", "").toString()
        set(isDialogShown) = sharedPreferences.edit().putString("isDialogShown", isDialogShown).apply()

    var carType: String
        get() = sharedPreferences.getString("carType", "").toString()
        set(carType) = sharedPreferences.edit().putString("carType", carType).apply()

    var checkVersionModel: String
        get() = sharedPreferences.getString("checkVersionModel", "").toString()
        set(checkVersionModel) = sharedPreferences.edit().putString("checkVersionModel", checkVersionModel).apply()


    var appInBackGround: Boolean
        get() = sharedPreferences.getBoolean("appInBackground", false)
        set(appInBackGround) = sharedPreferences.edit().putBoolean("appInBackGround", appInBackGround).apply()

    fun clearDriverNameRatingAndProfilePicture() {
        val editor = sharedPreferences.edit()
        editor.remove("driverRatingValue")
        editor.remove("driverName")
        editor.remove("driverProfilePic")
        editor.apply()
    }
}