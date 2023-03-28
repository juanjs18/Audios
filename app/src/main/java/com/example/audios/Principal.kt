package com.example.audios

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat

class Principal : AppCompatActivity() {

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var socket: Socket
    private lateinit var dataOutputStream: DataOutputStream
    private var isRecording = false
    private lateinit var rutaAudio: File
    private var permissionToRecordAccepted = true
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var audioList = mutableListOf<String>()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        val handler = Handler(Looper.getMainLooper())
        var button: Button = findViewById(R.id.btnGrabar)
        listView = findViewById(R.id.listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, audioList)
        listView.adapter = adapter

        rutaAudio = File(this.filesDir, "audio.mp3")
        rutaAudio.createNewFile()

        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                    isRecording = true
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording()
                    isRecording = false
                }
            }
            true
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            playAudio(audioList[position])
        }

        var servidorAndroid = ServidorAndroid(this, adapter, audioList)
        servidorAndroid.Start()
    }

    private fun playAudio(audioPath: String) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception){
            Log.e(TAG, "Error al reproducir audio: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setAudioEncodingBitRate(16)
        mediaRecorder.setAudioSamplingRate(44100)
        mediaRecorder.setOutputFile(rutaAudio.absolutePath)
        mediaRecorder.setMaxDuration(20000)
        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun connection(){
        Thread(Runnable {
            try {
                socket = Socket("10.0.1.45", 7878)
                //192.168.176.1
                //172.28.208.1
                dataOutputStream = DataOutputStream(socket.getOutputStream())

                dataOutputStream.write(sendAudio())
                dataOutputStream.flush()
                dataOutputStream.close()
                socket.close()
                //deleteAudio()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun sendAudio(): ByteArray {

        var audioBytes = ByteArray(1024)
        try {
            return FileInputStream(rutaAudio).use { it.readBytes() }
        } catch (e: IOException){
            Log.e(TAG, "Error al enviar archivo de audio", e)
        }
        return audioBytes
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
        connection()
    }
}

