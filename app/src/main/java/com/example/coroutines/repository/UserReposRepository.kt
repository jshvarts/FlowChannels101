package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.repository.api.ApiService
import com.example.coroutines.threading.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

class UserReposRepository(
    private val apiService: ApiService,
    private val dispatchers: DispatcherProvider
) {

    suspend fun getUserRepos(login: String): Flow<Repo> {
        return apiService.getUserRepos(login).asFlow()
            .catch { if (it !is HttpException) throw it }
            .flowOn(dispatchers.io)
    }
}
