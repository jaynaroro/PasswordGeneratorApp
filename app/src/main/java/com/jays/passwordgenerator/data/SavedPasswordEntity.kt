package com.jays.passwordgenerator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_passwords")
data class SavedPasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,

    val title:String,
    val password: String
)