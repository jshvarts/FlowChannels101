package com.example.coroutines.repository

import com.example.coroutines.domain.UserDetails
import com.example.coroutines.repository.api.ApiService
import com.example.coroutines.threading.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import java.io.IOException

const val DELAY_ONE_SECOND = 1_000L

class UserRepository(
    private val apiService: ApiService,
    private val dispatchers: DispatcherProvider
) {

    fun getUserDetails(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.getUserDetails(login)
            emit(Result.success(userDetails))
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io)
    }

    fun getUserDetailsRetryIfFailed(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.getUserDetails(login)
            emit(Result.success(userDetails))
        }.retry(retries = 2) { e ->
            (e is IOException).also {
                if (it) delay(DELAY_ONE_SECOND)
            }
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io)
    }
}
