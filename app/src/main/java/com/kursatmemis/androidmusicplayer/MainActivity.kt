package com.kursatmemis.androidmusicplayer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ExpandableListView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kursatmemis.androidmusicplayer.adapters.CustomExpandableListAdapter
import com.kursatmemis.androidmusicplayer.configs.ApiClient
import com.kursatmemis.androidmusicplayer.models.MockiResponse
import com.kursatmemis.androidmusicplayer.models.MusicCategory
import com.kursatmemis.androidmusicplayer.services.MockiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var musicCategories: MutableList<MusicCategory>
    private lateinit var expendableListView: ExpandableListView
    private lateinit var customExpandableListAdapter: CustomExpandableListAdapter
    private lateinit var database: DatabaseReference
    private lateinit var goToFavoriteMusicActivity: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setCustomExpandableListAdapter()
        val isFirstTime = isFirstTime()
        if (isFirstTime) {
            getDataFromAPIandWriteToFirebase()
        } else {
            readDataFromFirebase()
        }

        expendableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val musicName = musicCategories[groupPosition].items?.get(childPosition)?.title.toString()
            val musicUrl = musicCategories[groupPosition].items?.get(childPosition)?.url.toString()
            val intent = Intent(this@MainActivity, MediaPlayerActivity::class.java)
            intent.putExtra("musicName", musicName)
            intent.putExtra("musicUrl", musicUrl)
            startActivity(intent)
            true
        }

        goToFavoriteMusicActivity.setOnClickListener {
            val intent = Intent(this, FavoriteMusicActivity::class.java)
            startActivity(intent)
        }

    }

    // Firebase - Database'deki verileri okur ve ListView için kullanılan veri kaynağına ekler.
    private fun readDataFromFirebase() {
        val mockiResponseRef = Firebase.database.getReference("MockiResponse")
        mockiResponseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val mockiResponse = snapshot.getValue(MockiResponse::class.java)
                val musicCategories = mockiResponse?.musicCategories

                if (musicCategories != null) {
                    for (musicCategory in musicCategories) {
                        this@MainActivity.musicCategories.add(musicCategory)
                        this@MainActivity.customExpandableListAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("mKm - database", "onCancelled: $error")
            }

        })
    }

    // RestApi'den gelen verileri Firebase-Database'e yazar.
    private fun writeDataToFirebase(mockiResponse: MockiResponse?) {
        database = Firebase.database.reference
        database.child("MockiResponse").setValue(mockiResponse)
    }

    // RestApi'den gelen verileri alır, ListView için kullanılan veri kaynağına ekler.
    private fun getDataFromAPIandWriteToFirebase() {
        val mockiService = ApiClient.getClient().create(MockiService::class.java)
        mockiService.getResponse().enqueue(object : Callback<MockiResponse> {
            override fun onResponse(call: Call<MockiResponse>, response: Response<MockiResponse>) {
                val mockiResponse = response.body()
                if (mockiResponse != null) {
                    for (musicCategory in mockiResponse.musicCategories!!) {
                        musicCategories.add(musicCategory)
                        this@MainActivity.customExpandableListAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.w("mKm - retrofit", "mockiResponse is null.")
                }
                writeDataToFirebase(mockiResponse)
            }

            override fun onFailure(call: Call<MockiResponse>, t: Throwable) {
                Log.w("mKm - retrofit", "onFailure: ${t.toString()}")
            }

        })
    }

    // Uygulama ilk kez açılıyorsa true, aksi halde false döner.
    private fun isFirstTime(): Boolean {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false)
            editor.apply()
            return true
        } else {
            return false
        }
    }

    // ListView için kullanılacak veri kaynağını ve adapter'ı set eder.
    private fun setCustomExpandableListAdapter() {
        musicCategories = mutableListOf()
        customExpandableListAdapter = CustomExpandableListAdapter(this, musicCategories)
        expendableListView.setAdapter(customExpandableListAdapter)
    }

    // Layout dosyasındaki view'ları koda bağlar.
    private fun bindViews() {
        expendableListView = findViewById(R.id.expandableListView)
        goToFavoriteMusicActivity = findViewById(R.id.goToFavoriteMusicActivity)
    }
}