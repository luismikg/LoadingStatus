package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.material.ButtonState
import com.udacity.material.LoadingButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), LoadingButton.CompleteDownload {

    private var downloadID: Long = 0
    private var selectedUrl = ""

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setCompleteDownloadListener(this)
        custom_button.setOnClickListener {
            onClickDownload()
        }

        initNotification()
    }

    private fun onClickDownload() {
        if (!custom_button.isBlocked && selectedUrl.isNotEmpty()) {
            enableRadioButton(false)
            custom_button.buttonState = ButtonState.Clicked
            download()
        }
    }

    private fun initNotification() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        channel(
            getString(R.string.channel_id),
            getString(R.string.channel_name)
        )
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    fun onClickRadioButton(view: View) {
        if (view is RadioButton) {

            selectedUrl = when (view.getId()) {
                R.id.radio_button_glide -> Urls.glide
                R.id.radio_button_load_app -> Urls.loadingApp
                else -> Urls.retrofit
            }

            if (selectedUrl.isNotEmpty()) {
                custom_button.buttonState = ButtonState.Download
            }
        }
    }

    private fun getFileDownloaded(): String {
        return when (selectedUrl) {
            Urls.glide -> resources.getString(R.string.glide_image_loading_library_by_bumptech)
            Urls.loadingApp -> resources.getString(R.string.loadapp_current_repository_by_udacity)
            else -> resources.getString(
                R.string.retrofit_type_safe_http_client_for_android_and_java_by_square_inc
            )
        }
    }

    override fun onCompleteDownload() {
        enableRadioButton(true)
        radio_group.clearCheck()
        custom_button.buttonState = ButtonState.Unknown
        selectedUrl = ""
    }

    private fun enableRadioButton(isEnable: Boolean) {
        radio_button_glide.isEnabled = isEnable
        radio_button_load_app.isEnabled = isEnable
        radio_button__retrofit.isEnabled = isEnable
    }

    //BroadcastReceiver
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (downloadID == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                val query = DownloadManager.Query()
                val manager =
                    context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0))
                val cursor: Cursor = manager.query(query)

                if (cursor.moveToFirst() && cursor.count > 0) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        successDownload()
                    } else {
                        errorDownload()
                    }
                }
            }
        }
    }

    private fun successDownload() {
        val fileDownloaded = getFileDownloaded()
        Toast.makeText(
            applicationContext,
            "${resources.getString(R.string.success)} $fileDownloaded",
            Toast.LENGTH_SHORT
        ).show()

        notificationManager.send(
            applicationContext,
            "${resources.getString(R.string.notification_description)} $fileDownloaded}",
            fileDownloaded
        )
    }

    private fun errorDownload() {
        Toast.makeText(
            applicationContext,
            resources.getString(R.string.ups_something_fail),
            Toast.LENGTH_SHORT
        ).show()
    }

    //Notification Chanel
    private fun channel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
