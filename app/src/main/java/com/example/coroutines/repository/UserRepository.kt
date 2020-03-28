package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.UserDetails
import com.example.coroutines.repository.api.ApiService
import com.example.coroutines.threading.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import retrofit2.HttpException
import java.io.IOException

const val DELAY_ONE_SECOND = 1_000L
const val MIN_STARS = 50

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
            .flowOn(dispatchers.io)
    }

    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
    suspend fun getUserRepos(login: String): Flow<Repo> {
        return apiService.getUserRepos(login).asFlow()
            .filter { it.stars >= MIN_STARS }
            .catch { if (it !is HttpException) throw it }
            .flowOn(dispatchers.io)
    }

//    A more verbose version of the above
//    @ExperimentalCoroutinesApi
//    fun getUserRepos(login: String): Flow<Repo> {
//        return flow {
//            apiService.getUserRepos(login).forEach {
//                emit(it)
//            }
//        }
//        .onStart { println("Started flow") }
//        .onCompletion { println("Completed flow") }
//        .onEach { println("Repo name before filter: ${it.name}") }
//        .filter { it.stars >= 50 }
//        .onEach { println("Repo name after filter: ${it.name}") }
//        .catch { if (it !is HttpException) throw it }
//        .flowOn(dispatchers.io)
//    }
}
