package com.rideincab.user.common.configs

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage configs
 * @category RunTimePermission
 * @author SMR IT Solutions
 *
 */

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

import com.rideincab.user.common.network.AppController

import java.util.ArrayList

import javax.inject.Inject

/*****************************************************************
 * Get Global runtime permission
 */
class RunTimePermission {

    @Inject
    lateinit var context: Context
    @Inject
    lateinit var permissionList: ArrayList<String>

    private val preferences: SharedPreferences

    var isFirstTimePermission: Boolean
        get() = preferences.getBoolean("isFirstTimePermission", false)
        set(isFirstTime) = preferences.edit().putBoolean("isFirstTimePermission", isFirstTime).apply()

    var fcmToken: String?
        get() = preferences.getString("fcmToken", "")
        set(fcmToken) = preferences.edit().putString("fcmToken", fcmToken).apply()

    private val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    init {
        AppController.appComponent.inject(this)
        preferences = context.getSharedPreferences("mcl_permission", Context.MODE_PRIVATE)
    }

    fun checkHasPermission(context: Activity?, permissions: Array<String>?): ArrayList<String> {
        permissionList.clear()
        if (isMarshmallow && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission)
                }
            }
        }
        return permissionList
    }

    fun isPermissionBlocked(context: Activity?, permissions: Array<String>?): Boolean {
        if (isMarshmallow && context != null && permissions != null && isFirstTimePermission) {
            for (permission in permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true
                }
            }
        }
        return false
    }

    fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray?): ArrayList<String> {
        permissionList.clear()
        if (grantResults != null && grantResults.size > 0) {
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permissions[i])
                }
            }
        }
        return permissionList
    }
}
