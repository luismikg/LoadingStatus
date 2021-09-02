package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        initView()
    }

    private fun initView() {

        val filename = intent.getStringExtra("filename").toString()
        file_name_text.text = if (filename.isEmpty()) {
            "N/A"
        } else {
            filename
        }
        status_text.text = if (filename.isEmpty()) {
            resources.getString(R.string.ups_something_fail)
        } else {
            resources.getString(R.string.success)
        }

        ok_button.setOnClickListener {
            onBackPressed()
        }
    }
}
