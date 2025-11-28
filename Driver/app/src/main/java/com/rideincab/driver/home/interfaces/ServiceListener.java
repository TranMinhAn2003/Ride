package com.rideincab.driver.home.interfaces;

import com.rideincab.driver.common.model.JsonResponse;

/**
 * Created by SMR IT Solutions on 9/7/18.
 */

public interface ServiceListener {

    void onSuccess(JsonResponse jsonResp, String data);

    void onFailure(JsonResponse jsonResp, String data);


    /*void onSuccessResponse(JsonResponse jsonResp, String data);*/
}

