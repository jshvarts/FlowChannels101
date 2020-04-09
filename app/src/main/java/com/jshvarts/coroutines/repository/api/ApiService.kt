package com.jshvarts.coroutines.repository.api

import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.domain.UserDetails
import com.jshvarts.coroutines.domain.Wrapped
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val MAX_RESULTS_PER_PAGE = 100

interface ApiService {

    @GET("users/{login}")
    suspend fun getUserDetails(@Path("login") login: String): UserDetails

    @GET("users/{username}/repos?per_page=$MAX_RESULTS_PER_PAGE")
    suspend fun getUserRepos(
        @Path("username") username: String
    ): List<Repo>

    @GET("search/repositories?per_page=$MAX_RESULTS_PER_PAGE")
    @Wrapped
    suspend fun getReposForQuery(@Query("q") query: String): List<Repo>
}
