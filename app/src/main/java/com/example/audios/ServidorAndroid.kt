package com.example.audios

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket

class ServidorAndroid(
    principal: Context,
    adapter: AudioAdapter,
    audioList: MutableList<String>
) {

    private val server = ServerSocket(7878)
    private var socket = Socket()
    private var context = principal
    private var adapter = adapter
    private var audioList = audioList
    private val MAX_AUDIOS = 10

    public fun Start() {
        Thread(Runnable {
            while (true) {
                try {
                    socket = server.accept()

                    val inputStream = socket.getInputStream()
                    val file = File(context.filesDir, "audio_${System.currentTimeMillis()}.mp3")
                    val fileOutputStream = FileOutputStream(file)
                    val bytes = ByteArray(1024)
                    var count: Int = 0

                    do {
                        count = inputStream.read(bytes)
                        if (count > 0) fileOutputStream.write(bytes, 0, count)
                    } while (count != -1)

                    fileOutputStream.flush()
                    fileOutputStream.close()
                    inputStream?.close()
                    socket.close()

                    AgregarAudio(file)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al recibir audio: ${e.message}")
                }
            }
        }).start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun AgregarAudio(file: File){
        adapter.setEnviado(false)
        audioList.add(file.absolutePath)

        if (audioList.size > MAX_AUDIOS) {
            val oldestAudio = File(audioList[0])
            oldestAudio.delete()
            audioList.removeAt(0)
        }
        adapter.notifyDataSetChanged()
    }
}