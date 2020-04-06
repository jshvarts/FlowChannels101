package com.example.coroutines.di

import android.app.Application
import com.example.coroutines.ui.UserDetailFragment
import com.example.coroutines.ui.UserReposFragment
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
