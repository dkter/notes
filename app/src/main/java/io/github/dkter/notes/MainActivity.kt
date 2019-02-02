/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.

  This Source Code Form is "Incompatible With Secondary Licenses", as
  defined by the Mozilla Public License, v. 2.0.
*/

package io.github.dkter.notes


import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.*
import android.widget.TextView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var noteList: RecyclerView
    private lateinit var viewAdapter: NoteListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewManager = LinearLayoutManager(this)
        viewAdapter = NoteListAdapter(this)

        noteList = findViewById<RecyclerView>(R.id.note_list)
        noteList.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        noteViewModel.allNotes.observe(this, Observer { notes ->
            // Update the cached copy of the notes in the adapter
            notes?.let { viewAdapter.setNotes(it) }
        })

        fab.setOnClickListener { view ->
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //    .setAction("Action", null).show()
            val intent = Intent(this@MainActivity, NoteActivity::class.java)
            startActivityForResult(intent, noteActivityRequestCode)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == noteActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.let {
                val note = Note(it.getIntExtra(NoteActivity.UID, 0),
                                System.currentTimeMillis(),
                                it.getStringExtra(NoteActivity.TITLE),
                                it.getStringExtra(NoteActivity.TEXT))
                noteViewModel.insert(note)
            }
        } else if (requestCode == noteActivityEditRequestCode && resultCode == Activity.RESULT_OK) {
            data?.let {
                val note = Note(it.getIntExtra(NoteActivity.UID, 0),
                    System.currentTimeMillis(),
                    it.getStringExtra(NoteActivity.TITLE),
                    it.getStringExtra(NoteActivity.TEXT))
                noteViewModel.update(note)
            }
        } else {
            Toast.makeText(
                applicationContext,
                R.string.not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun editNote(title: String, text: String, note: Note?) {
        val intent = Intent(this@MainActivity, NoteActivity::class.java)
        intent.putExtra(NoteActivity.TITLE, title)
        intent.putExtra(NoteActivity.TEXT, text)
        intent.putExtra(NoteActivity.UID, note?.uid)
        startActivityForResult(intent, noteActivityEditRequestCode)
    }

    companion object {
        const val noteActivityRequestCode = 1
        const val noteActivityEditRequestCode = 2
    }
}

class NoteListAdapter internal constructor(private val context: Context) : RecyclerView.Adapter<NoteListAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>()

    inner class ViewHolder(val cardView: CardView, context: Context) : RecyclerView.ViewHolder(cardView) {
        val note_title_view: TextView = itemView.findViewById(R.id.note_title)
        val note_text_view: TextView = itemView.findViewById(R.id.note_text)
        val note_date_view: TextView = itemView.findViewById(R.id.note_date)

        var currentNote: Note? = null

        init {
            cardView.setOnClickListener { view ->
                val title = note_title_view.text.toString()
                val text = note_text_view.text.toString()
                (context as MainActivity).editNote(title, text, currentNote)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardView = inflater.inflate(R.layout.note_list_item, parent, false) as CardView
        return ViewHolder(cardView, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.currentNote = currentNote

        // show title and text
        holder.note_title_view.text = currentNote.title
        holder.note_text_view.text = currentNote.text

        // show date modified
        val date = Date(currentNote.modified)
        val format = SimpleDateFormat("yyyy-MM-dd h:mm a")
        holder.note_date_view.text = format.format(date)
    }

    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    override fun getItemCount() = notes.size
}
