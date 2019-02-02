/*
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.

  This Source Code Form is "Incompatible With Secondary Licenses", as
  defined by the Mozilla Public License, v. 2.0.
*/

package io.github.dkter.notes

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText

class NoteActivity : AppCompatActivity() {

    private lateinit var edit_title: EditText
    private lateinit var edit_text: EditText
    private var uid: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        edit_title = findViewById(R.id.edit_title)
        edit_text = findViewById(R.id.edit_text)

        val intent = getIntent()
        intent?.let {
            edit_title.setText(it.getStringExtra(TITLE))
            edit_text.setText(it.getStringExtra(TEXT))
            uid = it.getIntExtra(UID, 0)
        }

        val button = findViewById<Button>(R.id.save_button)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(edit_text.text) && TextUtils.isEmpty(edit_title.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val title = edit_title.text.toString()
                val text = edit_text.text.toString()
                replyIntent.putExtra(TITLE, title)
                replyIntent.putExtra(TEXT, text)
                replyIntent.putExtra(UID, uid)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    companion object {
        const val TITLE = "io.github.dkter.notes.TITLE"
        const val TEXT = "io.github.dkter.notes.TEXT"
        const val UID = "io.github.dkter.notes.UID"
    }
}
