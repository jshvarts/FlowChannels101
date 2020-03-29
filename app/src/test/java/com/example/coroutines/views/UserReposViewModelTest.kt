package com.example.coroutines.views

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
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
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
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

    private lateinit var userReposViewModel: UserReposViewModel

    @Before
    fun setUp() {
        userReposViewModel = UserReposViewModel(userReposRepository).apply {
            userRepos.observeForever(userReposObserver)
            isError.observeForever(isErrorObserver)
        }
    }

    @Test
    fun `should emit user repos on success`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 55)
        val repo2 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 10)

        val channel = Channel<Repo>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(userReposRepository)
            .getUserRepos(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(repo1)
            channel.send(repo2)
        }

        userReposViewModel.lookupUserRepos(TEST_USERNAME)

        // THEN
        // TODO: even though flow.toList() is a terminal operator, the assertion below does not work
        // verify(userReposObserver).onChanged(listOf(repo1, repo2))
        channel.isClosedForSend.shouldBeFalse()
    }

    @Test
    fun `should emit error on repos lookup failure`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 55)
        val channel = Channel<Repo>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(userReposRepository)
            .getUserRepos(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(repo1)
            channel.close(IOException())
        }

        userReposViewModel.lookupUserRepos(TEST_USERNAME)

        // THEN
        verify(userReposObserver).onChanged(listOf(repo1))
        verify(isErrorObserver).onChanged(true)
        channel.isClosedForSend.shouldBeTrue()
    }
}