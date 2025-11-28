package com.rideincab.driver.common.dependencies.module

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage dependencies.module
 * @category ApplicationModule
 * @author SMR IT Solutions
 *
 */

import android.app.Application

import com.rideincab.driver.common.util.CommonMethods

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

    @Provides
    @Singleton
    fun providesCommonMethods(): CommonMethods {
        return CommonMethods()
    }

    /* @Provides
    @Singleton
    public JsonResponse providesJsonResponse() {
        return new JsonResponse();
    }*/
}
