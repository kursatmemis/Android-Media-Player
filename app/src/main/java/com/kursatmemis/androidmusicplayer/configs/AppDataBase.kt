package com.kursatmemis.androidmusicplayer.configs

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kursatmemis.androidmusicplayer.dao.FavoriteMusicDao
import com.kursatmemis.androidmusicplayer.models.FavoriteMusic


@Database(entities = [FavoriteMusic::class], version = 1)
abstract class AppDataBase : RoomDatabase() {

    abstract fun studentDao(): FavoriteMusicDao

}