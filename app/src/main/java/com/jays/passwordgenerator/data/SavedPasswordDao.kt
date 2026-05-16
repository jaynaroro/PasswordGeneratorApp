package com.jays.passwordgenerator.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedPasswordDao {

    @Query("SELECT * FROM saved_passwords ORDER BY id DESC")
    suspend fun getAllPasswords(): List<SavedPasswordEntity>

    @Insert
    suspend fun insertPassword(savedPassword: SavedPasswordEntity)

    @Delete
    suspend fun deletePassword(savedPassword:  SavedPasswordEntity)
}