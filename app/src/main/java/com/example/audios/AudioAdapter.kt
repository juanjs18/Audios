package com.example.audios

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_TEXT_END
import android.view.View.TEXT_ALIGNMENT_TEXT_START
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException

class AudioAdapter(
    private val context: Context, private val audioList: MutableList<String>
    ) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var enviado: Boolean = false

    fun setEnviado(enviado: Boolean){
        this.enviado = enviado
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {

        val audioHolder = if (enviado)
            AudioViewHolder(LayoutInflater.from(context).inflate(R.layout.audiolistenviados, parent, false), enviado)
        else
            AudioViewHolder(LayoutInflater.from(context).inflate(R.layout.audiolistrecibidos, parent, false), enviado)

        audioHolder.set()

        return audioHolder
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioList[position])
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    inner class AudioViewHolder(itemView: View, private var enviado: Boolean) : RecyclerView.ViewHolder(itemView) {

        private lateinit var audioNameTextView: TextView
        private lateinit var playButton: Button
        private lateinit var pauseButton: Button
        private var mediaPlayer: MediaPlayer? = null

        fun set(){
            if (!enviado) {
                audioNameTextView = itemView.findViewById(R.id.audio_name_textview_recibido)
                playButton = itemView.findViewById(R.id.play_button_recibido)
                pauseButton = itemView.findViewById(R.id.pause_button_recibido)
            }
            else {
                audioNameTextView = itemView.findViewById(R.id.audio_name_textview)
                playButton = itemView.findViewById(R.id.play_button)
                pauseButton = itemView.findViewById(R.id.pause_button)
            }
        }

        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun bind(audioPath: String) {
            audioNameTextView.text = "audio_${System.currentTimeMillis()}"

            playButton.setOnClickListener {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioPath)
                        prepare()
                        start()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            pauseButton.setOnClickListener {
                mediaPlayer?.pause()
            }
        }
    }
}