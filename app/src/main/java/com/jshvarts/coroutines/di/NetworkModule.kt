package com.jshvarts.coroutines.di

import com.jshvarts.coroutines.domain.ReposJsonAdapter
import com.jshvarts.coroutines.repository.api.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val GITHUB_BASE_URL = "https://api.github.com/"

@InstallIn(ApplicationComponent::class)
@Module
object NetworkModule {

    @Provides
    fun provideRetrofit(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)

        val moshi = Moshi.Builder()
            .add(ReposJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpBuilder.build())
            .build()
            .create(ApiService::class.java)
    }
}
