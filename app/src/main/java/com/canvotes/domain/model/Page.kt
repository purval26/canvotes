package com.canvotes.domain.model

data class Page(
    val id: String,
    val notebookId: String,
    val title: String,
    val pageNumber: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
