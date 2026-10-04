package com.example.data

import kotlinx.coroutines.flow.Flow

class SafeWalkRepository(
    private val contactDao: ContactDao,
    private val walkSessionDao: WalkSessionDao,
    private val sosLogDao: SosLogDao
) {
    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()
    val allSessions: Flow<List<WalkSession>> = walkSessionDao.getAllSessions()
    val activeSession: Flow<WalkSession?> = walkSessionDao.getActiveSession()
    val allSosLogs: Flow<List<SosLog>> = sosLogDao.getAllSosLogs()

    suspend fun addContact(contact: EmergencyContact): Long = contactDao.insertContact(contact)

    suspend fun updateContact(contact: EmergencyContact) = contactDao.updateContact(contact)

    suspend fun deleteContact(contact: EmergencyContact) = contactDao.deleteContact(contact)

    suspend fun ensureDefaultContactsIfEmpty() {
        if (contactDao.getCount() == 0) {
            val defaults = listOf(
                EmergencyContact(
                    name = "Women Helpline",
                    phone = "1091",
                    relationship = "National 24/7 Helpline",
                    isPrimary = true,
                    isPolice = true
                ),
                EmergencyContact(
                    name = "Police Rapid Response",
                    phone = "112",
                    relationship = "Central Police",
                    isPrimary = true,
                    isPolice = true
                ),
                EmergencyContact(
                    name = "Mom (Primary Guard)",
                    phone = "+1-555-019-2834",
                    relationship = "Parent",
                    isPrimary = true,
                    isPolice = false
                )
            )
            contactDao.insertAll(defaults)
        }
    }

    suspend fun startWalkSession(session: WalkSession) = walkSessionDao.insertSession(session)

    suspend fun updateWalkSession(session: WalkSession) = walkSessionDao.updateSession(session)

    suspend fun logSosEvent(log: SosLog): Long = sosLogDao.insertSosLog(log)
}
