/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.

  This Source Code Form is "Incompatible With Secondary Licenses", as
  defined by the Mozilla Public License, v. 2.0.
*/

package io.github.dkter.notes

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.support.annotation.WorkerThread
import kotlinx.coroutines.experimental.*
import java.util.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var uid: Int,
    var modified: Long,
    var title: String?,
    var text: String?
)

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note)

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Query("SELECT * FROM notes ORDER BY modified DESC")
    fun loadAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE modified = (:modified)")
    fun loadNoteByID(modified: Long): LiveData<List<Note>>
}

@Database(entities = [Note::class], version = 3)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE notes")
                database.execSQL("CREATE TABLE `notes` (`title` TEXT, `modified` INTEGER NOT NULL, `text` TEXT, PRIMARY KEY(`modified`))")
                //database.execSQL("ALTER TABLE notes DROP COLUMN uid ADD PRIMARY KEY (modified)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): NoteDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "NoteDatabase"
                )//.addCallback(NoteDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                val instance = builder.build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class NoteDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.noteDao())
                }
            }
        }

        fun populateDatabase(noteDao: NoteDao) {
            val note1 = Note(0,
                        0,
                             "Note 1",
                             "This is a note")
            val note2 = Note(0,
                        1,
                             "Note 2",
                             "This is another note")
            noteDao.insertNote(note1)
            noteDao.insertNote(note2)
        }
    }
}

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.loadAllNotes()
    
    @WorkerThread
    suspend fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    @WorkerThread
    suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }
}
