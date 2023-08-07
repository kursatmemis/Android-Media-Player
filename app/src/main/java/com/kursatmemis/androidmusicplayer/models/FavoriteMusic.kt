package com.kursatmemis.androidmusicplayer.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("favorite_music")
data class FavoriteMusic(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val title: String? = null,
    val url: String? = null
) {
    override fun equals(other: Any?): Boolean {
        return this.url == (other as FavoriteMusic).url
    }

    override fun toString(): String {
        return this.title!!
    }
}