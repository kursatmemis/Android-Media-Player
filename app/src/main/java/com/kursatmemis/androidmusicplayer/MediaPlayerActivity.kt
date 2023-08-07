package com.kursatmemis.androidmusicplayer

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.room.Room
import com.kursatmemis.androidmusicplayer.configs.AppDataBase
import com.kursatmemis.androidmusicplayer.models.FavoriteMusic

class MediaPlayerActivity : AppCompatActivity() {
    private var player: MediaPlayer? = null

    private lateinit var musicNameTextView: TextView
    private lateinit var playPauseButton: Button
    private lateinit var addToFavoriteMusicButton: Button
    private lateinit var durationSeekBar: SeekBar
    private lateinit var durationTextView: TextView
    private lateinit var db: AppDataBase
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        bindViews()
        progressBar.visibility = View.VISIBLE

        val musicName = intent.getStringExtra("musicName")
        val musicUrl = intent.getStringExtra("musicUrl")
        musicNameTextView.text = musicName

        db = Room.databaseBuilder(this, AppDataBase::class.java, "AppDataBase")
            .build()

        player = MediaPlayer()
        player?.setDataSource(musicUrl)
        player?.prepareAsync()
        player?.setOnPreparedListener {
            progressBar.visibility = View.INVISIBLE
            player?.start()
            setDurationSeekBar()
            setSeekBarUpdateHandler()
        }

        var isPlaying = true

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                player?.pause()
                playPauseButton.setBackgroundResource(R.drawable.ic_play)
            } else {
                player?.start()
                playPauseButton.setBackgroundResource(R.drawable.ic_pause)
            }

            isPlaying = !isPlaying
        }

        setButtonColorAndText(musicUrl)
        setupVolumeControl()

        addToFavoriteMusicButton.setOnClickListener {
            val buttonText = addToFavoriteMusicButton.text

            if (buttonText == "Favorilere Ekle") {
                addToFavorites()
            } else {
                removeFromFavorites()
            }
        }

        player?.setOnCompletionListener {
            player?.seekTo(0)
            player?.start()
            durationSeekBar.progress = 0
            durationTextView.text = "00:00"
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    /*
    * O anda çalan müziği, url bilgisine göre, favori müzik database'in olup olmadığı bilgisini kontrol eder.
    * Eğer müzik database'de mevcutsa buton rengini kırmızı yapar ve buton text'ini 'Favorilerden Çıkar' yapar.
    * Aksi halde buton rengini mavi yapar ve buton text'ini 'Favorilere ekle' yapar.
    */
    private fun setButtonColorAndText(musicUrl: String?) {
        val run = Runnable {
            val exists = db.studentDao().existsByUrl(musicUrl!!)
            runOnUiThread {
                if (exists) {
                    addToFavoriteMusicButton.text = "Favorilerden Çıkar"
                    addToFavoriteMusicButton.setBackgroundColor(Color.RED)
                } else {
                    addToFavoriteMusicButton.text = "Favorilere Ekle"
                    addToFavoriteMusicButton.setBackgroundColor(Color.BLUE)
                }
            }
        }

        Thread(run).start()
    }

    // Çalan müziğin süresini gösteren seekBar componentini set eder.
    private fun setDurationSeekBar() {
        val totalDuration = player?.duration ?: 0
        durationSeekBar.max = totalDuration

        durationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                player?.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player?.start()
            }
        })
    }

    // MediaPlayer'ın çalma süresini ve ilgili componentleri günceller.
    private fun setSeekBarUpdateHandler() {
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                player?.let {
                    if (it.isPlaying) {
                        durationSeekBar.progress = it.currentPosition

                        val currentDuration = it.currentPosition / 1000
                        val minutes = currentDuration / 60
                        val seconds = currentDuration % 60
                        durationTextView.text = String.format("%02d:%02d", minutes, seconds)
                    }

                    if (it.currentPosition >= it.duration) {
                        it.seekTo(0)
                        durationSeekBar.progress = 0
                        durationTextView.text = "00:00"
                    }
                }

                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(runnable, 1000)
    }

    // İlgili müziğin favorilere eklenebilmesini ve buna bağlı olarak butonun renk değişimini sağlar.
    private fun addToFavorites() {
        val run = Runnable {
            val favoriteMusic = FavoriteMusic(
                null,
                musicNameTextView.text.toString(),
                intent.getStringExtra("musicUrl")
            )
            db.studentDao().insert(favoriteMusic)
            val list = db.studentDao().getAll()
            Log.w("mKm - room", list.toString())
            runOnUiThread {
                addToFavoriteMusicButton.setBackgroundColor(Color.RED)
                addToFavoriteMusicButton.text = "Favorilerden Çıkar"
            }
        }
        Thread(run).start()
    }

    // İlgili müziğin favorilerden çıkarılmasını ve buna bağlı olarak butonun renk değişimini sağlar.
    private fun removeFromFavorites() {
        val run = Runnable {
            db.studentDao().deleteByURL(intent.getStringExtra("musicUrl")!!)
            val list = db.studentDao().getAll()
            Log.w("mKm - room", list.toString())
            runOnUiThread {
                addToFavoriteMusicButton.setBackgroundColor(Color.BLUE)
                addToFavoriteMusicButton.text = "Favorilere Ekle"
            }
        }
        Thread(run).start()
    }

    // Ses seviyesinin ayarlanabilmesini ve kaydedilebilmesini sağlar.
    private fun setupVolumeControl() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val volumeKey = "volumeKey"
        val defaultVolume = 50

        val savedVolume = sharedPreferences.getInt(volumeKey, defaultVolume)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val initialVolume = (maxVolume * (savedVolume.toDouble() / 100)).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, 0)

        val volumeSeekBar = findViewById<SeekBar>(R.id.volumeSeekBar)
        val volumeTextView = findViewById<TextView>(R.id.volumeTextView)
        volumeSeekBar.progress = savedVolume
        volumeTextView.text = "$savedVolume%"

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val roundedProgress = (progress / 5) * 5
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    (maxVolume * (roundedProgress.toDouble() / 100)).toInt(),
                    0
                )
                volumeTextView.text = "$roundedProgress%"

                val editor = sharedPreferences.edit()
                editor.putInt(volumeKey, roundedProgress)
                editor.apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopPlayer()
        finish()
    }

    private fun stopPlayer() {
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        stopPlayer()
    }

    private fun bindViews() {
        musicNameTextView = findViewById(R.id.musicNameTextView)
        playPauseButton = findViewById(R.id.playButton)
        addToFavoriteMusicButton = findViewById(R.id.addToFavoriteMusicButton)
        durationSeekBar = findViewById(R.id.durationSeekBar)
        durationTextView = findViewById(R.id.durationTextView)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)
    }
}