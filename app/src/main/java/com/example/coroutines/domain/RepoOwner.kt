package com.example.coroutines.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RepoOwner(
    val login: String
)