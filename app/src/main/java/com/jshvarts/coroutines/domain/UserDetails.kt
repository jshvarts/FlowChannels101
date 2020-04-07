package com.jshvarts.coroutines.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetails(
    val login: String,
    val id: Int,
    @Json(name = "avatar_url") val avatarUrl: String,
    val name: String,
    val company: String
)
