package com.example.audios

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

    public fun setEnviado(enviado: Boolean){
        this.enviado = enviado
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.audiolistrow, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioList[position])
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val audioNameTextView: TextView = itemView.findViewById(R.id.audio_name_textview)
        private val playButton: Button = itemView.findViewById(R.id.play_button)
        private val pauseButton: Button = itemView.findViewById(R.id.pause_button)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        private var mediaPlayer: MediaPlayer? = null

        fun bind(audioPath: String) {
            audioNameTextView.text = "audio_${System.currentTimeMillis()}"
            if (enviado) audioNameTextView.textAlignment = TEXT_ALIGNMENT_TEXT_END
            else audioNameTextView.textAlignment = TEXT_ALIGNMENT_TEXT_START

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

            deleteButton.setOnClickListener {
                val file = File(audioPath)
                if (file.exists()) {
                    file.delete()
                    audioList.remove(audioPath)
                    notifyDataSetChanged()
                }
            }
        }
    }
}