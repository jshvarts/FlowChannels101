package com.jshvarts.coroutines.domain

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

data class RepoWrapper(
    val items: List<Repo>
)

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class Wrapped

class ReposJsonConverter {
    @Wrapped
    @FromJson
    fun fromJson(json: RepoWrapper): List<Repo> {
        return json.items
    }

    @ToJson
    fun toJson(@Wrapped value: List<Repo>): RepoWrapper {
        throw UnsupportedOperationException()
    }
}


