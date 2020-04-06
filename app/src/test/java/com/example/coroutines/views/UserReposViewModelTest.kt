package com.example.coroutines.views

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.coroutines.domain.MinStarCount
import com.example.coroutines.domain.NoMinStarCount
import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner
import com.example.coroutines.repository.UserReposRepository
import com.example.coroutines.threading.CoroutineTestRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserReposViewModelTest {
    @get:Rule
    val rule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val userReposRepository = mock<UserReposRepository>()

    private val userReposObserver = mock<Observer<List<Repo>>>()

    private val isErrorObserver = mock<Observer<Boolean>>()

    private val showSpinnerObserver = mock<Observer<Boolean>>()

    private lateinit var userReposViewModel: UserReposViewModel

    @Before
    fun setUp() {
        userReposViewModel = UserReposViewModel(userReposRepository).apply {
            userRepos.observeForever(userReposObserver)
            isError.observeForever(isErrorObserver)
            showSpinner.observeForever(showSpinnerObserver)
        }
    }

    @Test
    fun `should emit user repos on success`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 10)
        val repo2 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 55)
        val expectedRepoList = listOf(repo1, repo2)

        val channel = Channel<List<Repo>>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(userReposRepository)
            .getUserRepos(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(expectedRepoList)
        }

        userReposViewModel.lookupUserRepos(TEST_USERNAME)

        // THEN
        verify(userReposObserver).onChanged(expectedRepoList)
    }

    @Test
    fun `should emit error on repos lookup failure`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 55)
        val expectedRepoList = listOf(repo1)

        val channel = Channel<List<Repo>>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(userReposRepository)
            .getUserRepos(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(expectedRepoList)
            channel.close(IOException())
        }

        userReposViewModel.lookupUserRepos(TEST_USERNAME)

        // THEN
        verify(userReposObserver).onChanged(expectedRepoList)
        verify(isErrorObserver).onChanged(true)
    }

    @Test
    fun `should emit user repos on success and filter by stars`() =
        rule.dispatcher.runBlockingTest {
            // GIVEN
            val repo1 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 10)
            val repo2 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 150)
            val expectedRepoList = listOf(repo2)

            val channel = Channel<List<Repo>>()
            val flow = channel.consumeAsFlow()

            doReturn(flow)
                .whenever(userReposRepository)
                .getUserRepos(TEST_USERNAME)

            // WHEN
            launch {
                channel.send(listOf(repo1, repo2))
            }

            userReposViewModel.filterRepos(MinStarCount(100))
            userReposViewModel.lookupUserRepos(TEST_USERNAME)

            // THEN
            verify(userReposObserver).onChanged(expectedRepoList)
        }

    @Test
    fun `should emit user repos in correct order and without filter applied`() =
        rule.dispatcher.runBlockingTest {
            // GIVEN
            val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 150)
            val repo2 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 10)
            val expectedRepoList = listOf(repo2, repo1)

            val channel = Channel<List<Repo>>()
            val flow = channel.consumeAsFlow()

            doReturn(flow)
                .whenever(userReposRepository)
                .getUserRepos(TEST_USERNAME)

            // WHEN
            launch {
                channel.send(listOf(repo1, repo2))
            }

            userReposViewModel.filterRepos(NoMinStarCount)
            userReposViewModel.lookupUserRepos(TEST_USERNAME)

            // THEN
            verify(userReposObserver).onChanged(expectedRepoList)
        }
}
