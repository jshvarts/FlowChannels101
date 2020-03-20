package com.example.coroutines.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Repo(
    val name: String,
    val owner: RepoOwner,
    @Json(name = "stargazers_count") val stars: Int
)