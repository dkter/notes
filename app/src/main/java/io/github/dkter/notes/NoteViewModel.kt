/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.

  This Source Code Form is "Incompatible With Secondary Licenses", as
  defined by the Mozilla Public License, v. 2.0.
*/

package io.github.dkter.notes

import android.app.Application
import android.arch.lifecycle.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlin.coroutines.experimental.CoroutineContext

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        val notesDao = NoteDatabase.getDatabase(application, scope).noteDao()
        repository = NoteRepository(notesDao)
        allNotes = repository.allNotes
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    fun insert(note: Note) = scope.launch(Dispatchers.IO) {
        repository.insert(note)
    }

    fun update(note: Note) = scope.launch(Dispatchers.IO) {
        repository.update(note)
    }
}