package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<EmergencyContact>)

    @Update
    suspend fun updateContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getCount(): Int
}

@Dao
interface WalkSessionDao {
    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Query("SELECT * FROM walk_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveSession(): Flow<WalkSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WalkSession)

    @Update
    suspend fun updateSession(session: WalkSession)
}

@Dao
interface SosLogDao {
    @Query("SELECT * FROM sos_logs ORDER BY timestamp DESC")
    fun getAllSosLogs(): Flow<List<SosLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosLog(log: SosLog): Long
}
