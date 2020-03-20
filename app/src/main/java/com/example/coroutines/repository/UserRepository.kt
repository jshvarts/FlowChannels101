package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.UserDetails
import com.example.coroutines.threading.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.IOException

const val DELAY_ONE_SECOND = 1_000L

class UserRepository(
    private val apiService: ApiService,
    private val dispatchers: DispatcherProvider
) {

    @ExperimentalCoroutinesApi
    fun getUserDetails(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.getUserDetails(login)
            emit(Result.success(userDetails))
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io())
    }

    @ExperimentalCoroutinesApi
    fun getUserDetailsRetryIfFailed(login: String): Flow<Result<UserDetails>> {
        return flow {
            val userDetails = apiService.getUserDetails(login)
            emit(Result.success(userDetails))
        }.retry(retries = 2) { t ->
            (t is IOException).also {
                if (it) delay(DELAY_ONE_SECOND)
            }
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io())
    }

    @ExperimentalCoroutinesApi
    fun getUserRepos(login: String): Flow<Result<List<Repo>>> {
        return flow {
            val repos = apiService.getUserRepos(login)
            emit(Result.success(repos))
        }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatchers.io())
    }
}
