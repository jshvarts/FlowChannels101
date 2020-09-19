package com.jshvarts.coroutines.domain

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
data class RepoList(
    val items: List<Repo>
)

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class WrappedRepoList

class ReposJsonAdapter {
    @WrappedRepoList
    @FromJson
    fun fromJson(json: RepoList): List<Repo> {
        return json.items
    }

    @ToJson
    fun toJson(@WrappedRepoList value: List<Repo>): RepoList {
        throw UnsupportedOperationException()
    }
}


