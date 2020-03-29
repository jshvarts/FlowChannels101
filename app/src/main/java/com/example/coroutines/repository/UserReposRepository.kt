package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.repository.api.ApiService
import com.example.coroutines.threading.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

const val MIN_STARS = 50

class UserReposRepository(
    private val apiService: ApiService,
    private val dispatchers: DispatcherProvider
) {

    suspend fun getUserRepos(login: String): Flow<Repo> {
        return apiService.getUserRepos(login).asFlow()
            .filter { it.stars >= MIN_STARS }
            .catch { if (it !is HttpException) throw it }
            .flowOn(dispatchers.io)
    }

//    A more verbose version of the above

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
