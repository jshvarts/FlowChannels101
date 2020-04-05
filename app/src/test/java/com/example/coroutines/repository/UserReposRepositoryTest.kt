package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner
import com.example.coroutines.repository.api.ApiService
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserReposRepositoryTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `should get user repos on success`() = testDispatcher.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 10)
        val repo2 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 55)

        val expectedRepoList = listOf(repo1, repo2)

        val apiService = mock<ApiService> {
            onBlocking { getUserRepos(TEST_USERNAME) } doReturn expectedRepoList
        }

        val repository = UserReposRepository(apiService, testDispatcher)

        // WHEN
        val actualRepoList = repository.getUserRepos(TEST_USERNAME)
            .toList()

        // THEN
        actualRepoList.shouldBeEqualTo(expectedRepoList)
    }

    @Test(expected = IOException::class)
    fun `should throw error for user repos if non-HttpException`() =
        testDispatcher.runBlockingTest {
            // GIVEN
            val apiService = mock<ApiService> {
                onBlocking { getUserRepos(TEST_USERNAME) } doAnswer {
                    throw IOException()
                }
            }

            val repository = UserReposRepository(apiService, testDispatcher)

            // WHEN/THEN
            val actualRepoList = repository.getUserRepos(TEST_USERNAME)
                .toList()

            actualRepoList.shouldBeEmpty()
        }
}
