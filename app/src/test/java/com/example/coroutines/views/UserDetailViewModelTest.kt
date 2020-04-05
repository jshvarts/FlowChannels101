package com.example.coroutines.views

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.coroutines.domain.UserDetails
import com.example.coroutines.repository.UserRepository
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

private const val TEST_USERNAME = "someUsername"

class UserDetailViewModelTest {
    @get:Rule
    val rule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val userRepository = mock<UserRepository>()

    private val userDetailsObserver = mock<Observer<UserDetails>>()

    private val isErrorObserver = mock<Observer<Boolean>>()

    private lateinit var userDetailViewModel: UserDetailViewModel

    @Before
    fun setUp() {
        userDetailViewModel = UserDetailViewModel(userRepository).apply {
            userDetails.observeForever(userDetailsObserver)
            isError.observeForever(isErrorObserver)
        }
    }

    @Test
    fun `should emit user details on success`() = rule.dispatcher.runBlockingTest {
        // GIVEN
        val userDetails = UserDetails(
            login = "someUsername",
            id = 1,
            avatarUrl = "someAvatarUrl",
            name = "someName",
            company = "someCompany"
        )

        val result = Result.success(userDetails)
        val channel = Channel<Result<UserDetails>>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(userRepository)
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
        val result = Result.failure<UserDetails>(RuntimeException())
        val channel = Channel<Result<UserDetails>>()
        val flow = channel.consumeAsFlow()

        doReturn(flow)
            .whenever(userRepository)
            .getUserDetails(TEST_USERNAME)

        // WHEN
        launch {
            channel.send(result)
        }

        userDetailViewModel.lookupUser(TEST_USERNAME)

        // THEN
        verify(isErrorObserver).onChanged(true)
    }
}
