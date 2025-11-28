package com.rideincab.driver.trips.viewmodel

import androidx.lifecycle.LiveData
import com.rideincab.driver.common.model.JsonResponse

interface RequestAcceptActivityInterface {

    fun onSuccessResponse(jsonResponse: LiveData<JsonResponse>)
    fun onFailureResponse(jsonResponse: LiveData<JsonResponse>)

}