package com.rideincab.driver.home.interfaces

import okhttp3.RequestBody

/**
 * Created by SMR IT Solutions on 9/7/18.
 */

interface ImageListener {
    fun onImageCompress(filePath: String, requestBody: RequestBody?)
}

