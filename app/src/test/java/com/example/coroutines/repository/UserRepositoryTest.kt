package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner
import com.example.coroutines.domain.UserDetails
import com.example.coroutines.threading.TestDispatcherProvider
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserRepositoryTest {

    private val testDispatcherProvider = TestDispatcherProvider()

    @Test
    fun `should get user details on success`() = runBlocking {
        // GIVEN
        val userDetails = UserDetails(TEST_USERNAME, 1, "someAvatarUrl")

        val apiService = mock<ApiService> {
            onBlocking { getUserDetails(TEST_USERNAME) } doReturn userDetails
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.getUserDetails("someUsername")

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
        val flow = repository.getUserDetails("someUsername")

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
        val flow = repository.getUserDetailsRetryIfFailed("someUsername")

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
            onBlocking { getUserDetails(TEST_USERNAME) } doAnswer {
                if (throwError) throw IOException() else userDetails
            }
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val flow = repository.getUserDetailsRetryIfFailed("someUsername")

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

    @Test
    fun `should get user repos on success`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner("someUsername"), stars = 10)
        val repo2 = Repo(name = "someRepo2", owner = RepoOwner("someUsername"), stars = 55)

        val rawRepoList = listOf(repo1, repo2)

        val apiService = mock<ApiService> {
            onBlocking { getUserRepos(TEST_USERNAME) } doReturn rawRepoList
        }

        val repository = UserRepository(apiService, testDispatcherProvider)

        // WHEN
        val actualRepoList = repository.getUserRepos("someUsername")
            .toList()

        // THEN
        actualRepoList
            .shouldHaveSize(1)
            .shouldContain(repo2)
    }

    @Test(expected = IOException::class)
    fun `should throw error for user repos if non-HttpException`() =
        testDispatcherProvider.runBlockingTest {
            // GIVEN
            val apiService = mock<ApiService> {
                onBlocking { getUserRepos(TEST_USERNAME) } doAnswer {
                    throw IOException()
                }
            }

            val repository = UserRepository(apiService, testDispatcherProvider)

            // WHEN/THEN
            val actualRepoList = repository.getUserRepos("someUsername")
                .toList()

            actualRepoList.shouldBeEmpty()
        }
}