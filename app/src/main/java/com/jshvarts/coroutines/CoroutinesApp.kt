package com.jshvarts.coroutines

import android.app.Application
import com.jshvarts.coroutines.di.AppComponent
import com.jshvarts.coroutines.di.DaggerAppComponent
import timber.log.Timber

class CoroutinesApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        appComponent = DaggerAppComponent.factory().create(this)
    }
}
