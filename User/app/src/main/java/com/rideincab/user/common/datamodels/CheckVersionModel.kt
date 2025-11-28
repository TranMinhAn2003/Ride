package com.rideincab.user.common.datamodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CheckVersionModel {
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null

    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null

    @SerializedName("force_update")
    @Expose
    var forceUpdate: String? = null

    @SerializedName("enable_referral")
    @Expose
    var enableReferral: Boolean = false

    @SerializedName("client_id")
    @Expose
    var clientId: String? = null

    @SerializedName("apple_login")
    @Expose
    var appleLogin: Boolean = false

    @SerializedName("facebook_login")
    @Expose
    var facebookLogin: Boolean = false

    @SerializedName("google_login")
    @Expose
    var googleLogin: Boolean = false

    @SerializedName("otp_enabled")
    @Expose
    var otpEnabled = false

    @SerializedName("firebase_otp_enabled")
    @Expose
    var firebaseOtpEnabled = false

    @SerializedName("support")
    @Expose
    var support = ArrayList<Support>()

}

class Support {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("image")
    @Expose
    var image: String? = null

    @SerializedName("link")
    @Expose
    var link: String = ""

}