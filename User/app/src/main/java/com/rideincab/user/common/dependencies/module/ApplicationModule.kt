package com.rideincab.user.common.dependencies.module

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage dependencies.module
 * @category ApplicationModule
 * @author SMR IT Solutions
 *
 */

import android.app.Application

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/*****************************************************************
 * Application Module
 */
@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun application(): Application {
        return application
    }
}
