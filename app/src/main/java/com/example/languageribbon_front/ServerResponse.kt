package com.example.languageribbon_front

data class ServerResponse(
    val uploadSuccess: Boolean?,
    val confirm: Boolean?,
    val message: String?,
    val metric: Metric?
)


