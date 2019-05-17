package com.decoders.demoapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_note.*

class AddNote : AppCompatActivity() {
    internal var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        browse.setOnClickListener {
            ImageUtil.openGallery(this)
        }
        save.setOnClickListener {
            if (noteTxt!!.text.toString().isEmpty()) {
                noteTxt!!.error = "Required"
            }
            else if (filePath == null) {
                Toast.makeText(this@AddNote, "Please select image!", Toast.LENGTH_SHORT).show()
            }
            else {
                val model = DataModel()
                model.note = noteTxt!!.text.toString()
                model.imagePath = filePath
                RealmFunctions.storeData(model)
                Toast.makeText(this@AddNote, "Note Added!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bmp: Bitmap?
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ImageUtil.GALL_CODE -> {
                    bmp = ImageUtil.getGalleryImage(this@AddNote, data)
                    if (bmp != null) {
                        imageSel!!.setImageBitmap(bmp)
                        filePath = ImageUtil.filePath
                    }
                }
            }
        }
    }
}
