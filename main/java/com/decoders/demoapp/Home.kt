package com.decoders.demoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_home.*

import java.util.ArrayList

class Home : AppCompatActivity() {
    internal var models = ArrayList<DataModel>()
    internal var homeAdapter: HomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        } else {
            loadData()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        } else {
            loadData()
        }
    }

    private fun loadData() {
        models = RealmFunctions.allData
        homeAdapter = HomeAdapter(this, models)
        listView!!.adapter = homeAdapter
        if (models.size == 0)
            noresult!!.visibility = View.VISIBLE
        else
            noresult!!.visibility = View.GONE
        addNote!!.setOnClickListener {
            val intent = Intent(this@Home, AddNote::class.java)
            startActivity(intent)
        }
        listView.setOnItemClickListener { parent, view, position, id ->
            val model = models[position]
            val intent = Intent(this@Home, ViewNote::class.java)
            intent.putExtra("path", model.imagePath)
            intent.putExtra("note", model.note)
            startActivity(intent)
        }
    }
}
