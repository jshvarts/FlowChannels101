package com.example.coroutines.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.domain.Repo
import com.example.coroutines.repository.UserReposRepository
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserReposViewModel @Inject constructor(
    private val userReposRepository: UserReposRepository
) : ViewModel() {

    private val _userRepos = MutableLiveData<List<Repo>>()
    val userRepos: LiveData<List<Repo>> = _userRepos

    private val _isError = MutableLiveData<Boolean>(false)
    val isError: LiveData<Boolean> = _isError

    // TODO handle this Channel
    private val sortOrderChannel = ConflatedBroadcastChannel(SortOrder.ASCENDING)

    fun lookupUserRepos(login: String) {

        viewModelScope.launch {
            _userRepos.value = userReposRepository.getUserRepos(login)
                .catch {
                    _isError.value = true
                }
                .toList()
                .sortedByDescending { it.stars }
        }
    }

    private suspend fun getUserRepos(login: String): List<Repo> {
        return userReposRepository.getUserRepos(login)
            .catch {
                _isError.value = true
            }
            .toList()
    }

    fun changeSortOrder() {
        val newSortOrder = if (sortOrderChannel.value == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
        sortOrderChannel.offer(newSortOrder)
    }
}

private enum class SortOrder {
    ASCENDING, DESCENDING;
}

