package com.jshvarts.coroutines.repository

import com.jshvarts.coroutines.di.IoDispatcher
import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.repository.api.ApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ReposForQueryRepository @Inject constructor(
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getReposForQuery(query: String): Flow<List<Repo>> {
        return flow {
            emit(apiService.getReposForQuery(query))
        }
            .flowOn(ioDispatcher)
    }
}
