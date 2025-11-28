package com.rideincab.user.common.interfaces

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage interfaces
 * @category ImageListener
 * @author SMR IT Solutions
 * 
 */

import okhttp3.RequestBody

/*****************************************************************
 * ImageListener
 */

interface ImageListener {
    fun onImageCompress(filePath: String, requestBody: RequestBody?)
}
