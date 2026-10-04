package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [EmergencyContact::class, WalkSession::class, SosLog::class],
    version = 1,
    exportSchema = false
)
abstract class SafeWalkDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun walkSessionDao(): WalkSessionDao
    abstract fun sosLogDao(): SosLogDao

    companion object {
        @Volatile
        private var INSTANCE: SafeWalkDatabase? = null

        fun getInstance(context: Context): SafeWalkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeWalkDatabase::class.java,
                    "safewalk_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialContacts(database.contactDao())
                    }
                }
            }

            suspend fun populateInitialContacts(dao: ContactDao) {
                val initialContacts = listOf(
                    EmergencyContact(
                        name = "National Women Helpline",
                        phone = "1091",
                        relationship = "Helpline (24/7)",
                        isPrimary = true,
                        isPolice = true
                    ),
                    EmergencyContact(
                        name = "Police Rapid Response",
                        phone = "112",
                        relationship = "Emergency Dispatch",
                        isPrimary = true,
                        isPolice = true
                    ),
                    EmergencyContact(
                        name = "Mom (Priya Sharma)",
                        phone = "+1-555-019-2834",
                        relationship = "Family",
                        isPrimary = false,
                        isPolice = false
                    ),
                    EmergencyContact(
                        name = "Sister (Ananya)",
                        phone = "+1-555-019-4821",
                        relationship = "Sister",
                        isPrimary = false,
                        isPolice = false
                    )
                )
                dao.insertAll(initialContacts)
            }
        }
    }
}
