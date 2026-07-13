package com.example.data.model

import java.util.Date

data class User(
    val id: String,
    val username: String,
    val passwordHash: String, // Plain for local simulation or hashed
    val email: String,
    val role: String, // "admin" or "user"
    val isActive: Boolean = true,
    val expiresAt: String, // Persian expiration text or date string e.g., "۱۴۰۵/۱۲/۲۹"
    val createdAt: String = "۱۴۰۵/۰۴/۱۰"
)
