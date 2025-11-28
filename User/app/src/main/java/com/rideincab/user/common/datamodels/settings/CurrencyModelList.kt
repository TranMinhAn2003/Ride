package com.rideincab.user.common.datamodels.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

/**
 * Created by SMR IT Solutions on 11/9/20.
 */

class CurrencyModelList {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("currency_list")
    @Expose
    var currencyList: ArrayList<CurrencyListModel> = ArrayList()
}
