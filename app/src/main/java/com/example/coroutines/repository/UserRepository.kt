package com.example.coroutines.repository

import com.example.coroutines.domain.UserDetails
import com.example.coroutines.threading.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.IOException

class UserRepository(
    private val apiService: ApiService,
    private val dispatchers: DispatcherProvider
) {

    @ExperimentalCoroutinesApi
    fun userDetails(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.userDetails(login)
            emit(Result.success(userDetails))
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io())
    }

    @ExperimentalCoroutinesApi
    fun userDetailsRetryIfFailed(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.userDetails(login)
            emit(Result.success(userDetails))
        }.retry(retries = 2) { t ->
            (t is IOException).also {
                if (it) delay(timeMillis = 1_000L)
            }
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io())
    }
}
