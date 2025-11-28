package com.rideincab.user.common.datamodels.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by SMR IT Solutions on 11/9/20.
 */

class CurrencyListModel {

    @SerializedName("code")
    @Expose
    var code: String=""
    @SerializedName("symbol")
    @Expose
    var symbol: String=""
}
