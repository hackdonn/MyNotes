package com.app.notes.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.app.notes.BaseActivity
import com.app.notes.databinding.ActivityNewNoteBinding

class NewNoteActivity : BaseActivity() {
    private lateinit var binding: ActivityNewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        signInMessage = "Sign-in to create a new note!"
        binding.btnSave.setOnClickListener {
            if (TextUtils.isEmpty(binding.etTitle.text) || TextUtils.isEmpty(binding.etBody.text)) {
                resultIntent.putExtra(MainActivity.USER_ID, userId)
                setResult(Activity.RESULT_CANCELED, resultIntent)
            } else {
                val title = binding.etTitle.text.toString()
                val body = binding.etBody.text.toString()

                resultIntent.putExtra(NEW_TITLE, title)
                resultIntent.putExtra(NEW_BODY, body)
                resultIntent.putExtra(MainActivity.USER_ID, userId)
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    private val resultIntent = Intent()

    override fun onBackPressed() {
        resultIntent.apply {
            putExtra(MainActivity.USER_ID, userId)
        }
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
    }

    companion object {
        const val NEW_TITLE = "new_title"
        const val NEW_BODY = "new_body"
    }
}
