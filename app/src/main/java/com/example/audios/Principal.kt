package com.example.audios

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.net.Socket
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Principal : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var socket: Socket? = null
    private var dataOutputStream: DataOutputStream? = null
    private var isRecording = false
    private var textoEnviar: EditText? = null
    private var rutaAudio: File? = null
    private val context: Context = this
    private var permissionToRecordAccepted = true
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

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
        textoEnviar = findViewById(R.id.editTextTextPersonName)

        rutaAudio = File(this.filesDir, "audio.3pg")
        rutaAudio?.createNewFile()

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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {

        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setAudioEncodingBitRate(16)
        mediaRecorder?.setAudioSamplingRate(44100)
        mediaRecorder?.setOutputFile(rutaAudio?.absolutePath)
        mediaRecorder?.setMaxDuration(20000)
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
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
                dataOutputStream = DataOutputStream(socket?.getOutputStream())

                dataOutputStream!!.write(sendAudio())
                dataOutputStream!!.flush()
                dataOutputStream!!.close()
                socket!!.close()
                //deleteAudio()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun deleteAudio(){
        rutaAudio?.delete()
    }

    private fun sendAudio(): ByteArray? {

        var audioBytes: ByteArray? = null
        try {
            audioBytes = FileInputStream(rutaAudio).use { it.readBytes() }
            return audioBytes
        } catch (e: IOException){
            Log.e(TAG, "Error al enviar archivo de audio", e)
        }
        return audioBytes
    }

    private fun stopRecording() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        connection()
    }
}

