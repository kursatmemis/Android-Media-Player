package com.kursatmemis.androidmusicplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.room.Room
import com.kursatmemis.androidmusicplayer.configs.AppDataBase
import com.kursatmemis.androidmusicplayer.models.FavoriteMusic

class FavoriteMusicActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var favoriteMusic: List<FavoriteMusic>? = null
    private lateinit var db: AppDataBase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_music)

        listView = findViewById(R.id.listView)

        db = Room.databaseBuilder(this, AppDataBase::class.java, "AppDataBase")
            .build()

        val run = Runnable {
            val favoriteMusicList = db.studentDao().getAll()
            runOnUiThread {
                favoriteMusic = favoriteMusicList
                val adapter = ArrayAdapter<FavoriteMusic>(
                    this, android.R.layout.simple_list_item_1,
                    favoriteMusic!!
                )
                listView.adapter = adapter
            }
        }
        Thread(run).start()

        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@FavoriteMusicActivity,MediaPlayerActivity::class.java)
            val favori = favoriteMusic?.get(position)
            intent.putExtra("musicName", favori?.title)
            intent.putExtra("musicUrl", favori?.url)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val run = Runnable {
            val favoriteMusicList = db.studentDao().getAll()
            runOnUiThread {
                favoriteMusic = favoriteMusicList
                val adapter = ArrayAdapter<FavoriteMusic>(
                    this,
                    android.R.layout.simple_list_item_1,
                    favoriteMusic!!
                )
                listView.adapter = adapter
            }
        }
        Thread(run).start()
    }

}