package com.example.coroutines.repository

import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner
import com.example.coroutines.repository.api.ApiService
import com.example.coroutines.threading.TestDispatcherProvider
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.flow.toList
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldHaveSize
import org.junit.Test
import java.io.IOException

private const val TEST_USERNAME = "someUsername"

class UserReposRepositoryTest {

    private val testDispatcherProvider = TestDispatcherProvider()

    @Test
    private fun `should get user repos on success`() = testDispatcherProvider.runBlockingTest {
        // GIVEN
        val repo1 = Repo(name = "someRepo1", owner = RepoOwner(TEST_USERNAME), stars = 10)
        val repo2 = Repo(name = "someRepo2", owner = RepoOwner(TEST_USERNAME), stars = 55)

        val rawRepoList = listOf(repo1, repo2)

        val apiService = mock<ApiService> {
            onBlocking { getUserRepos(TEST_USERNAME) } doReturn rawRepoList
        }

        val repository = UserReposRepository(apiService, testDispatcherProvider)

        // WHEN
        val actualRepoList = repository.getUserRepos(TEST_USERNAME)
            .toList()

        // THEN
        actualRepoList
            .shouldHaveSize(1)
            .shouldContain(repo2)
    }

    @Test(expected = IOException::class)
    private fun `should throw error for user repos if non-HttpException`() =
        testDispatcherProvider.runBlockingTest {
            // GIVEN
            val apiService = mock<ApiService> {
                onBlocking { getUserRepos(TEST_USERNAME) } doAnswer {
                    throw IOException()
                }
            }

            val repository = UserReposRepository(apiService, testDispatcherProvider)

            // WHEN/THEN
            val actualRepoList = repository.getUserRepos(TEST_USERNAME)
                .toList()

            actualRepoList.shouldBeEmpty()
        }
}
