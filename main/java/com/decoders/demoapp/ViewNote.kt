package com.decoders.demoapp

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_view_note.*

import java.io.File

class ViewNote : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_note)

        val path = intent.getStringExtra("path")
        val note = intent.getStringExtra("note")

        viewImage.setImageBitmap(BitmapFactory.decodeFile(path))
        viewNote.text = note
    }
}
