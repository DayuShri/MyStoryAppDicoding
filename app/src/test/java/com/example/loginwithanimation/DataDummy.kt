package com.example.loginwithanimation

import com.example.loginwithanimation.data.response.StoryItem

object DataDummy {

    fun generateDummyStories(): List<StoryItem> {
        val items: MutableList<StoryItem> = arrayListOf()
        for (i in 1..100) {
            val story = StoryItem(
                id = i.toString(),
                name = "Author $i",
                description = "This is a description for story $i",
                photoUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTumTqtlP9UO9Gg-JKvKnsxxL1D1_YFsgqoHA&s$i.jpg",
                createdAt = "2023-12-20T00:00:00Z",
                lat = -6.2 + i * 0.01,
                lon = 106.8 + i * 0.01
            )
            items.add(story)
        }
        return items
    }
}
