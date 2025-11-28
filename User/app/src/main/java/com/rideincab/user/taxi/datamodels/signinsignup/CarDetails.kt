package com.rideincab.user.taxi.datamodels.signinsignup

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by SMR IT Solutions on 6/9/18.
 */

class CarDetails {

    @SerializedName("id")
    @Expose
    var id: String=""
    @SerializedName("carName")
    @Expose
    var carName: String=""
    @SerializedName("description")
    @Expose
    var description: String=""
}
