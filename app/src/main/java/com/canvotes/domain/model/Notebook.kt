package com.canvotes.domain.model

data class Notebook(
    val id: String,
    val title: String,
    val description: String = "",
    val pageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
