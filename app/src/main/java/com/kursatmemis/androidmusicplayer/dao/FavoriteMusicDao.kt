package com.kursatmemis.androidmusicplayer.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kursatmemis.androidmusicplayer.models.FavoriteMusic

@Dao
interface FavoriteMusicDao {

    @Insert
    fun insert(favoriteMusic: FavoriteMusic)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_music WHERE url = :url LIMIT 1)")
    fun existsByUrl(url: String): Boolean

    @Query("DELETE FROM favorite_music WHERE url = :url")
    fun deleteByURL(url: String)

    @Query("Delete From favorite_music")
    fun deleteAll()

    @Query("Select * from favorite_music")
    fun getAll(): List<FavoriteMusic>
}