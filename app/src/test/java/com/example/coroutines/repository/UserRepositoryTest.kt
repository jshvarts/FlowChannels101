package com.example.coroutines.repository

import com.example.coroutines.domain.UserDetails
import com.example.coroutines.threading.TestDispatcherProvider
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserRepositoryTest {

    private val testDispatcherProvider = TestDispatcherProvider()

    @Test
    fun `should get user details on success`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        val userDetails = UserDetails(TEST_USERNAME, 1, "someAvatarUrl")

        val apiService = mock<ApiService> {
            onBlocking { userDetails(TEST_USERNAME) } doReturn userDetails
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.userDetails("someUsername")

        // THEN
        flow.collect { result: Result<UserDetails> ->
            result.isSuccess.shouldBeTrue()
            result.onSuccess { it shouldBeEqualTo userDetails }
        }
    }

    @Test
    fun `should get error for user details`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        val apiService = mock<ApiService> {
            onBlocking { userDetails(TEST_USERNAME) } doAnswer {
                throw IOException()
            }
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.userDetails("someUsername")

        // THEN
        flow.collect { result: Result<UserDetails> ->
            result.isFailure.shouldBeTrue()
        }
    }

    @Test
    fun `should retry and all retries failed`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        val apiService = mock<ApiService> {
            onBlocking { userDetails(TEST_USERNAME) } doAnswer {
                throw IOException()
            }
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.userDetailsRetryIfFailed("someUsername")

        // THEN
        flow.collect { result: Result<UserDetails> ->
            result.isFailure.shouldBeTrue()
        }
    }

    @Test
    fun `should retry and second retry succeeded`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        var throwError = true

        val userDetails = UserDetails(TEST_USERNAME, 1, "someAvatarUrl")

        val apiService = mock<ApiService> {
            onBlocking { userDetails(TEST_USERNAME) } doAnswer {
                if (throwError) throw IOException() else userDetails
            }
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.userDetailsRetryIfFailed("someUsername")

        // THEN
        launch {
            flow.collect { result ->
                result.isSuccess.shouldBeTrue()
            }
        }

        // 1st retry
        advanceTimeBy(delayTimeMillis = 1_000L)

        // 2nd retry
        throwError = false
        advanceTimeBy(delayTimeMillis = 1_000L)
    }
}