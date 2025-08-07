package com.example.eventuretest.data.models

import com.example.eventuretest.R

enum class EventCategory(val displayName: String, val iconRes: Int) {
    MUSICAL("Musical", R.drawable.baseline_music_video_24),
    SPORTS("Sports", R.drawable.baseline_sports_basketball_24),
    FOOD("Food", R.drawable.baseline_fastfood_24),
    ART("Art", R.drawable.baseline_color_lens_24);

    companion object {
        fun fromString(category: String): EventCategory? {
            return values().find { it.name.equals(category, ignoreCase = true) }
        }

        fun getAllCategories(): List<String> {
            return values().map { it.displayName }
        }
    }
}