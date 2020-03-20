package com.example.coroutines.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoOwner(
    val login: String
)