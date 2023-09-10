package com.app.notes.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.notes.models.Note

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note)

    @get:Query("SELECT * FROM note ORDER BY date DESC")
    val allNotes: LiveData<List<Note>>
}