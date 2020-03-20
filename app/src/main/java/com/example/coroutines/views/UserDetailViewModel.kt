package com.example.coroutines.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.UserDetails
import com.example.coroutines.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userDetails = MutableLiveData<UserDetails>()
    val userDetails: LiveData<UserDetails> = _userDetails

    private val _userRepos = MutableLiveData<List<Repo>>()
    val userRepos: LiveData<List<Repo>> = _userRepos

    private val _isUserDetailsError = MutableLiveData<Boolean>()
    val isUserDetailsError: LiveData<Boolean> = _isUserDetailsError

    private val _isUserReposError = MutableLiveData<Boolean>()
    val isUserReposError: LiveData<Boolean> = _isUserReposError

    @ExperimentalCoroutinesApi
    fun lookupUser(login: String) {

        viewModelScope.launch {
            val flow = userRepository.getUserDetails(login)
            flow.collect { result: Result<UserDetails> ->
                when {
                    result.isSuccess -> _userDetails.value = result.getOrNull()
                    result.isFailure -> _isUserDetailsError.value = true
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun lookupUserRepos(login: String) {

        viewModelScope.launch {
            val flow = userRepository.getUserRepos(login)
            // toList() is an extension function on Flow to collect data into a destination
            try {
                _userRepos.value = flow.toList()
            } catch (e: Throwable) {
                _isUserReposError.value = true
            }
        }
    }
}
