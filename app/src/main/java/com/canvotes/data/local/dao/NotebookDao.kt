package com.canvotes.data.local.dao

import androidx.room.*
import com.canvotes.data.local.entity.NotebookEntity
import kotlinx.coroutines.flow.Flow

data class NotebookWithPageCount(
    @Embedded
    val notebook: NotebookEntity,
    val pageCount: Int
)

@Dao
interface NotebookDao {
    @Query("""
        SELECT notebooks.*, COUNT(pages.id) AS pageCount 
        FROM notebooks 
        LEFT JOIN pages ON notebooks.id = pages.notebookId 
        GROUP BY notebooks.id 
        ORDER BY notebooks.updatedAt DESC
    """)
    fun getNotebooksWithPageCount(): Flow<List<NotebookWithPageCount>>

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    suspend fun getNotebookById(id: String): NotebookEntity?

    @Query("""
        SELECT notebooks.*, COUNT(pages.id) AS pageCount 
        FROM notebooks 
        LEFT JOIN pages ON notebooks.id = pages.notebookId 
        WHERE notebooks.title LIKE '%' || :query || '%' 
           OR notebooks.description LIKE '%' || :query || '%'
        GROUP BY notebooks.id 
        ORDER BY notebooks.updatedAt DESC
    """)
    fun searchNotebooks(query: String): Flow<List<NotebookWithPageCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: NotebookEntity)

    @Update
    suspend fun updateNotebook(notebook: NotebookEntity)

    @Delete
    suspend fun deleteNotebook(notebook: NotebookEntity)

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun deleteNotebookById(id: String)
}
