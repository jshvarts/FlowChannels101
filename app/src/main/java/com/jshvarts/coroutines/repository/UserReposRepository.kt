package com.jshvarts.coroutines.repository

import com.jshvarts.coroutines.di.IoDispatcher
import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.repository.api.ApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserReposRepository @Inject constructor(
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getUserRepos(login: String): Flow<List<Repo>> {
        return flow {
            emit(apiService.getUserRepos(login))
        }
            // this is just to illustrate how dispatcher on which the work is done is controlled.
            // Retrofit suspend functions are already doing work on io dispatcher
            .flowOn(ioDispatcher)
    }
}
