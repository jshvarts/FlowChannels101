package com.example.coroutines.views

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner
import com.example.coroutines.domain.UserDetails
import com.example.coroutines.repository.UserRepository
import com.example.coroutines.threading.CoroutineTestRule
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserDetailViewModelTest {
    @get:Rule
    val rule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock<UserRepository>()

    private val userDetailsObserver = mock<Observer<UserDetails>>()

    private val isUserDetailsErrorObserver = mock<Observer<Boolean>>()

    private val userRepoObserver = mock<Observer<Repo>>()

    private val isUserRepoErrorObserver = mock<Observer<Boolean>>()

    private lateinit var userDetailViewModel: UserDetailViewModel

    @Before
    fun setUp() {
        userDetailViewModel = UserDetailViewModel(repository).apply {
            userDetails.observeForever(userDetailsObserver)
            isUserDetailsError.observeForever(isUserDetailsErrorObserver)
            userRepo.observeForever(userRepoObserver)
            isUserRepoError.observeForever(isUserRepoErrorObserver)
        }
    }

    @Test
    fun `should emit user details on success`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val userDetails = UserDetails(TEST_USERNAME, 1, "someAvatarUrl")

        val result = Result.success(userDetails)
        val channel = Channel<Result<UserDetails>>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(repository)
            .getUserDetails(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(result)
        }

        userDetailViewModel.lookupUser(TEST_USERNAME)

        // THEN
        verify(userDetailsObserver).onChanged(userDetails)
    }

    @Test
    fun `should emit error on user details lookup failure`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val result = Result.failure<UserDetails>(Exception())
        val channel = Channel<Result<UserDetails>>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(repository)
            .getUserDetails(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(result)
        }

        userDetailViewModel.lookupUser(TEST_USERNAME)

        // THEN
        verify(isUserDetailsErrorObserver).onChanged(true)
    }

    @Test
    fun `should emit user repos on success`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 55)
        val repo2 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 10)

        val channel = Channel<Repo>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(repository)
            .getUserRepos(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(repo1)
            channel.send(repo2)
        }

        userDetailViewModel.lookupUserRepos(TEST_USERNAME)

        // THEN
        inOrder(userRepoObserver) {
            verify(userRepoObserver).onChanged(repo1)
            verify(userRepoObserver).onChanged(repo2)
        }
        channel.isClosedForSend.shouldBeTrue()
    }

    @Test
    fun `should emit error on repos lookup failure`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 55)
        val channel = Channel<Repo>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(repository)
            .getUserRepos(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(repo1)
            channel.close(IOException())
        }

        userDetailViewModel.lookupUserRepos(TEST_USERNAME)

        // THEN
        inOrder(userRepoObserver, isUserRepoErrorObserver) {
            verify(userRepoObserver).onChanged(repo1)
            verify(isUserRepoErrorObserver).onChanged(true)
        }
        channel.isClosedForSend.shouldBeTrue()
    }
}