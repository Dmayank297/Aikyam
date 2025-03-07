package com.example.saahas.Models


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    var name: String,
    @PrimaryKey val phoneNumber: String
)
