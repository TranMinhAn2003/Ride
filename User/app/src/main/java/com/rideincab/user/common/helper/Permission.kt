package com.rideincab.user.common.helper

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage helper
 * @category Permission
 * @author SMR IT Solutions
 * 
 */

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/* ************************************************************
 Permission for marshMallow version mobiles
*************************************************************** */
class Permission(var activity: Activity) {

    companion object {
        /**
         * Check location permission
         */
        fun checkPermission(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }


}
