package com.example.coroutines.repository

import com.example.coroutines.domain.UserDetails
import com.example.coroutines.repository.api.ApiService
import com.example.coroutines.threading.TestDispatcherProvider
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserRepositoryTest {

    private val userDetails = UserDetails(
        login = TEST_USERNAME,
        id = 1,
        avatarUrl = "someAvatarUrl",
        name = "someName",
        company = "someCompany"
    )

    private val testDispatcherProvider = TestDispatcherProvider()

    @Test
    fun `should get user details on success`() = runBlocking {
        // GIVEN
        val apiService = mock<ApiService> {
            onBlocking { getUserDetails(TEST_USERNAME) } doReturn userDetails
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.getUserDetails(TEST_USERNAME)

        // THEN
        flow.collect { result: Result<UserDetails> ->
            result.isSuccess.shouldBeTrue()
            result.onSuccess { it shouldBeEqualTo userDetails }
        }
    }

    @Test
    fun `should get error for user details`() = runBlocking {
        // GIVEN
        val apiService = mock<ApiService> {
            onBlocking { getUserDetails(TEST_USERNAME) } doAnswer {
                throw IOException()
            }
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.getUserDetails(TEST_USERNAME)

        // THEN
        flow.collect { result: Result<UserDetails> ->
            result.isFailure.shouldBeTrue()
        }
    }

    @Test
    fun `should retry and all retries failed`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        val apiService = mock<ApiService> {
            onBlocking { getUserDetails(TEST_USERNAME) } doAnswer {
                throw IOException()
            }
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.getUserDetailsRetryIfFailed(TEST_USERNAME)

        // THEN
        flow.collect { result: Result<UserDetails> ->
            result.isFailure.shouldBeTrue()
        }
    }

    @Test
    fun `should retry and second retry succeeded`() =
        testDispatcherProvider.runBlockingTest {
            // GIVEN
            var throwError = true

            val apiService = mock<ApiService> {
                onBlocking { getUserDetails(TEST_USERNAME) } doAnswer {
                    if (throwError) throw IOException() else userDetails
                }
            }

            val repository = UserRepository(apiService, testDispatcherProvider)

            // WHEN
            val flow = repository.getUserDetailsRetryIfFailed(TEST_USERNAME)

            // THEN
            launch {
                flow.collect { result ->
                    result.isSuccess.shouldBeTrue()
                }
            }

            // 1st retry
            advanceTimeBy(DELAY_ONE_SECOND)

            // 2nd retry
            throwError = false
            advanceTimeBy(DELAY_ONE_SECOND)
        }
}
