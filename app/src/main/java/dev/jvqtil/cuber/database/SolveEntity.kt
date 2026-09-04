package dev.jvqtil.cuber.database

import androidx.room.Entity
import androidx.room.PrimaryKey

const val PENALTY_OK = "OK"
const val PENALTY_PLUS_TWO = "+2"
const val PENALTY_DNF = "DNF"

@Entity(tableName = "solves")
data class SolveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    val scramble: String,
    val createdAt: Long,
    val penalty: String = PENALTY_OK,
    val comment: String? = null
)