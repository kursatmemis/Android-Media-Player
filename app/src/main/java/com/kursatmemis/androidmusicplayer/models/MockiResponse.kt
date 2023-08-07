package com.kursatmemis.androidmusicplayer.models

data class MockiResponse (
    val musicCategories: List<MusicCategory>? = null
)

data class MusicCategory (
    val baseTitle: String? = null,
    val items: List<Item>? = null
)

data class Item (
    val baseCat: Long? = null,
    val title: String? = null,
    val url: String? = null
)
