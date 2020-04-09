package com.jshvarts.coroutines.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jshvarts.coroutines.ui.ReposForQueryViewModel
import com.jshvarts.coroutines.ui.UserDetailViewModel
import com.jshvarts.coroutines.ui.UserReposViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(UserDetailViewModel::class)
    abstract fun bindUserDetailViewModel(view: UserDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserReposViewModel::class)
    abstract fun bindUserReposViewModel(view: UserReposViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReposForQueryViewModel::class)
    abstract fun bindReposForQueryViewModel(view: ReposForQueryViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Singleton
class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: creators.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException("unknown model class $modelClass")
        return creator.get() as T
    }
}
