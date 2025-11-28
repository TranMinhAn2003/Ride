package com.rideincab.user.common.interfaces

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage interfaces
 * @category ApiService
 * @author SMR IT Solutions
 * 
 */

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/*****************************************************************
 * ApiService
 */

interface ApiService {

    //Check user Mobile Number
    @GET("numbervalidation")
    fun numbervalidation(@Query("mobile_number") mobile_number: String, @Query("country_code") country_code: String, @Query("user_type") user_type: String, @Query("language") language: String, @Query("forgotpassword") forgotpassword: String): Call<ResponseBody>

    //Forgot password
    @GET("forgotpassword")
    fun forgotpassword(@Query("mobile_number") mobile_number: String, @Query("country_code") country_code: String, @Query("user_type") user_type: String, @Query("password") password: String, @Query("device_type") device_type: String, @Query("device_id") device_id: String): Call<ResponseBody>

    //Login
    @GET("login")
    fun login(@Query("mobile_number") mobile_number: String, @Query("country_code") country_code: String, @Query("user_type") user_type: String, @Query("password") password: String, @Query("device_type") device_type: String, @Query("device_id") device_id: String, @Query("language") language: String): Call<ResponseBody>

    // Old Social login
    @GET("socialsignup")
    fun socialoldsignup(@Query("auth_type") authType: String, @Query("auth_id") authId: String, @Query("device_type") device_type: String, @Query("device_id") device_id: String, @Query("language") language: String): Call<ResponseBody>

    // Register
    @GET("register")
    fun register(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    // Get Rider Profile
    @GET("get_rider_profile")
    fun getRiderProfile(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    // Get Driver details

    @GET("trip_rating")
    fun tripRating(@Query("token") token: String, @Query("trip_id") trip_id: String, @Query("rating") rating: String, @Query("rating_comments") rating_comments: String, @Query("user_type") user_type: String, @Query("tips") driverTips: String): Call<ResponseBody>

    // Update Device ID for Push notification
    @GET("search_cars")
    fun searchCars(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    @FormUrlEncoded
    @POST("request_cars")
    fun sendRequest(@FieldMap hashMap: HashMap<String, String>): Call<ResponseBody>

    @FormUrlEncoded
    @POST("save_schedule_ride")
    fun scheduleRide(@FieldMap hashMap: HashMap<String, String>): Call<ResponseBody>

    // Update location with lat,lng
    @GET("updateriderlocation")
    fun updateLocation(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>
    // Update location with lat,lng
    @GET("send_message")
    fun updateChat(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    // Update Work, Home location
    @GET("update_rider_location")
    fun uploadRiderLocation(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    // Get Trip Details
    @GET("get_rider_trips")
    fun getRiderTrips(@Query("token") token: String, @Query("user_type") type: String): Call<ResponseBody>

    // Change Paypal CurrencyModelList
    @GET("paypal_currency_conversion")
    fun paypalCurrency(@Query("token") token: String, @Query("currency_code") currency_code: String, @Query("amount") amount: String): Call<ResponseBody>

    // Add wallet amount using stripe card
    @FormUrlEncoded
    @POST("add_wallet")
    fun addWalletMoneyUsingStripe(@FieldMap walletParams: LinkedHashMap<String, String>): Call<ResponseBody>

    // Add wallet amount using paypal
    @GET("add_wallet")
    fun addWalletMoneyUsingPaypal(@Query("token") token: String, @Query("paykey") paykey: String, @Query("amount") amount: String): Call<ResponseBody>

    // Proceed To Card and Paypal Payment
    @FormUrlEncoded
    @POST("after_payment")
    fun proceedAfterPayment(@FieldMap linkedHashMap: LinkedHashMap<String, String>): Call<ResponseBody>

    // Update Rider Profile
    @GET("update_rider_profile")
    fun updateProfile(@Query("profile_image") profile_image: String, @Query("first_name") first_name: String, @Query("last_name") last_name: String, @Query("country_code") country_code: String, @Query("mobile_number") mobile_number: String, @Query("email_id") email_id: String, @Query("token") token: String): Call<ResponseBody>

    //Upload Profile Image
    @POST("upload_profile_image")
    fun uploadImage(@Body RequestBody: RequestBody, @Query("token") token: String): Call<ResponseBody>

    // Log out Rider
    @GET("logout")
    fun logOut(@Query("token") token: String, @Query("user_type") user_type: String): Call<ResponseBody>

    // Get Currency
    @GET("currency_list")
    fun currencyList(@Query("token") token: String): Call<ResponseBody>

    // Get Currency
    @GET("language")
    fun updateLanguage(@Query("token") token: String, @Query("language") language: String): Call<ResponseBody>

    // Update Currency
    @GET("update_user_currency")
    fun updateCurrency(@Query("token") token: String, @Query("currency_code") currency_code: String): Call<ResponseBody>

    // Cancel selected order
    @GET("cancel_trip")
    fun cancelTrip(@Query("cancel_reason_id") cancelReason: String, @Query("cancel_comments") cancelMessage: String, @Query("trip_id") trip_id: String, @Query("user_type") user_type: String, @Query("token") token: String): Call<ResponseBody>

    // Cancel Reason
    @GET("cancel_reasons")
    fun cancelReasons(@Query("token") token: String): Call<ResponseBody>

    // Cancel selected order
    @GET("schedule_ride_cancel")
    fun cancelScheduleTrip(@Query("cancel_reason_id") cancelReason: String, @Query("cancel_comments") cancelMessage: String, @Query("trip_id") trip_id: String, @Query("user_type") user_type: String, @Query("token") token: String): Call<ResponseBody>

    // Change Mobile Number
    @GET("update_device")
    fun updateDevice(@Query("token") token: String, @Query("user_type") userType: String, @Query("device_type") device_type: String, @Query("device_id") device_id: String): Call<ResponseBody>

    // Get Promo Details
    @GET("promo_details")
    fun promoDetails(@Query("token") token: String): Call<ResponseBody>

    // Add Promo Code
    @GET("add_promo_code")
    fun addPromoDetails(@Query("token") token: String, @Query("code") code: String): Call<ResponseBody>

    // add card details
    @GET("add_card_details")
    fun addCard(@Query("intent_id") stripeId: String, @Query("token") token: String): Call<ResponseBody>
    // SoS alert
    @GET("sosalert")
    fun sosalert(@Query("token") token: String, @Query("latitude") latitude: String, @Query("longitude") longitude: String): Call<ResponseBody>

    // Add Emergency Contact
    @GET("sos")
    fun sos(@Query("token") token: String, @Query("mobile_number") mobile_number: String, @Query("action") action: String, @Query("name") name: String, @Query("country_code") country_code: String, @Query("id") id: String): Call<ResponseBody>


    @GET("get_referral_details")
    fun getReferralDetails(@Query("token") token: String): Call<ResponseBody>

    // get Trip invoice Details  Rider
    @GET("get_invoice")
    fun getInvoice(@Query("is_wallet") isWallet: String, @Query("payment_mode") paymentMethod: String, @Query("token") token: String, @Query("trip_id") TripId: String, @Query("user_type") userType: String): Call<ResponseBody>

    //Force Update API
    @GET("check_version")
    fun checkVersion(@Query("version") code: String, @Query("user_type") type: String, @Query("device_type") deviceType: String): Call<ResponseBody>


    // get profile picture and name of opponent caller for sinch call page
    @GET("get_caller_detail")
    fun getCallerDetail(@Query("token") token: String, @Query("user_id") userID: String, @Query("send_push_notification") pushStatus: String): Call<ResponseBody>

    //Common Data
    @FormUrlEncoded
    @POST("common_data")
    fun getCommonData(@Field("token") token: String): Call<ResponseBody>

    //Common Data
    @GET("get_nearest_vehicles")
    fun getNearestVehicles(@Query("latitude") latitude: String, @Query("longitude") longitude: String, @Query("token") token: String): Call<ResponseBody>

    //Common Data
    @GET("get_past_trips")
    fun getPastTrips(@Query("token") token: String, @Query("page") page: String): Call<ResponseBody>

    //Common Data
    @GET("get_upcoming_trips")
    fun getUpcomingTrips(@Query("token") token: String, @Query("page") page: String): Call<ResponseBody>

    // Get given trip details
    @GET("get_trip_details")
    fun getTripDetails(@Query("token") token: String, @Query("trip_id") trip_id: String): Call<ResponseBody>

    //Currency Conversion for Braintree
    @FormUrlEncoded
    @POST("currency_conversion")
    fun currencyConversion(@Field("amount") amount: String, @Field("token") token: String, @Field("payment_type") paymentType:  String): Call<ResponseBody>

    // Get Card
    @GET("get_card_details")
    fun viewCard(@Query("token") token: String): Call<ResponseBody>

    // GET PAYMENTMETHODLIST
    @FormUrlEncoded
    @POST("get_payment_list")
    fun getPaymentMethodlist(@Field("token") token: String,@Field("is_wallet") isWallet: Int): Call<ResponseBody>


    @GET("otp_verification")
    fun otpVerification(@QueryMap hashMap: HashMap<String, String>): Call<ResponseBody>

    @FormUrlEncoded
    @POST("delete-user")
    fun deleteAccount(@Field("token") token: String): Call<ResponseBody>
}