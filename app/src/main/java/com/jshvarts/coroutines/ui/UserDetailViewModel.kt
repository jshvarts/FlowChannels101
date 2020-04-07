package com.jshvarts.coroutines.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jshvarts.coroutines.domain.UserDetails
import com.jshvarts.coroutines.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userDetails = MutableLiveData<UserDetails>()
    val userDetails: LiveData<UserDetails> = _userDetails

    private val _isError = MutableLiveData<Boolean>(false)
    val isError: LiveData<Boolean> = _isError

    fun lookupUser(login: String) {

        viewModelScope.launch {
            userRepository.getUserDetails(login)
                .collect { result ->
                    when {
                        result.isSuccess -> {
                            _userDetails.value = result.getOrNull().also { data ->
                                Timber.i("success getting user details: $data")
                            }
                        }
                        result.isFailure -> {
                            Timber.e(result.exceptionOrNull(), "error getting user details")
                            _isError.value = true
                        }
                    }
                }
        }
    }
}
