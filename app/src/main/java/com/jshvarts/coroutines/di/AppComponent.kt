package com.jshvarts.coroutines.di

import android.app.Application
import com.jshvarts.coroutines.ui.UserDetailFragment
import com.jshvarts.coroutines.ui.UserReposFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        DispatcherModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    fun inject(fragment: UserDetailFragment)
    fun inject(fragment: UserReposFragment)
}
