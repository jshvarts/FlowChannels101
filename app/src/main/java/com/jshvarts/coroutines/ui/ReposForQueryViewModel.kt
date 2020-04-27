package com.jshvarts.coroutines.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.repository.ReposForQueryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_QUERY = "kotlin"

class ReposForQueryViewModel @Inject constructor(
    private val reposForQueryRepository: ReposForQueryRepository
) : ViewModel() {

    private val _repos = MutableLiveData<List<Repo>>()
    val repos: LiveData<List<Repo>> = _repos

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _showSpinner = MutableLiveData<Boolean>(false)
    val showSpinner: LiveData<Boolean> = _showSpinner

    private var currentQuery: String = DEFAULT_QUERY

    init {
        lookupRepos(DEFAULT_QUERY)
    }

    fun lookupRepos(query: String) {
        currentQuery = query

        viewModelScope.launch {
            reposForQueryRepository.getReposForQuery(query)
                .onStart {
                    _showSpinner.value = true
                }.onCompletion {
                    _showSpinner.value = false
                }.catch {
                    _isError.value = true
                }.collect { repoList ->
                    _repos.value = repoList.sortedByDescending { it.stars }
                }
        }
    }

    fun refresh() {
        lookupRepos(currentQuery)
    }
}

