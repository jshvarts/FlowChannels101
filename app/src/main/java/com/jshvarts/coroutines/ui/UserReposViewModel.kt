package com.jshvarts.coroutines.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jshvarts.coroutines.domain.MinStarCount
import com.jshvarts.coroutines.domain.NoMinStarCount
import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.repository.UserReposRepository
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserReposViewModel @Inject constructor(
    private val userReposRepository: UserReposRepository
) : ViewModel() {

    private val _userRepos = MutableLiveData<List<Repo>>()
    val userRepos: LiveData<List<Repo>> = _userRepos

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _showSpinner = MutableLiveData<Boolean>(false)
    val showSpinner: LiveData<Boolean> = _showSpinner

    private val filterByStarCountChannel = ConflatedBroadcastChannel(NoMinStarCount)

    fun lookupUserRepos(username: String) {
        viewModelScope.launch {
            filterByStarCountChannel.asFlow()
                .flatMapLatest { minStarCount ->
                    userReposRepository.getUserRepos(username)
                        .map { repoList ->
                            repoList.filter { repo ->
                                repo.stars >= minStarCount.stars
                            }
                        }
                }.onStart {
                    _showSpinner.value = true
                }.onEach {
                    _showSpinner.value = false
                }.catch {
                    Timber.e(it, "error getting user repos")
                    _isError.value = true
                }.collect { repoList ->
                    _userRepos.value = repoList.sortedBy { it.stars }
                        .also {
                            Timber.i("success getting user repos: $it")
                        }
                }
        }
    }

    fun filterRepos(minStarCount: MinStarCount) {
        filterByStarCountChannel.offer(minStarCount)
    }
}

