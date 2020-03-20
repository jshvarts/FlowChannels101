package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.UserDetails
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("users/{login}")
    suspend fun getUserDetails(@Path("login") login: String): UserDetails

    @GET("users/{username}/repos?per_page=200")
    suspend fun getUserRepos(
        @Path("username") username: String
    ): List<Repo>
}