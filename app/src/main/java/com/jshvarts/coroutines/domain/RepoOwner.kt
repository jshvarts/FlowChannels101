package com.jshvarts.coroutines.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoOwner(
    val login: String,
    @Json(name = "avatar_url") val avatarUrl: String
)
