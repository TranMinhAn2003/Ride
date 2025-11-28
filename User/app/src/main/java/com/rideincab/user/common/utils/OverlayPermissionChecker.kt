package com.rideincab.user.common.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings


object OverlayPermissionChecker {
    fun isOverlayPermissionAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            return isIntentAvailable(context, intent)
        }
        return true // Assume true for versions below Android M
    }

    private fun isIntentAvailable(context: Context, intent: Intent?): Boolean {
        val packageManager = context.packageManager
        if (intent == null || packageManager == null) {
            return false
        }
        return packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).size > 0
    }
}
