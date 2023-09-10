package com.app.notes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.notes.R

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
    }
}