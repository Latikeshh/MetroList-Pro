/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.extensions

import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Playlist
import com.metrolist.music.db.entities.Song

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) asReversed() else this

fun <T> MutableList<T>.move(
    fromIndex: Int,
    toIndex: Int,
): MutableList<T> {
    add(toIndex, removeAt(fromIndex))
    return this
}

// Extension function to filter explicit content for local Song entities
fun List<Song>.filterExplicit(enabled: Boolean = true) =
    if (enabled) {
        filter { !it.song.explicit }
    } else {
        this
    }

// Extension function to filter video songs for local Song entities
fun List<Song>.filterVideoSongs(enabled: Boolean = true) =
    if (enabled) {
        filter { !it.song.isVideo }
    } else {
        this
    }

// Extension function to filter explicit content for local Album entities
fun List<Album>.filterExplicitAlbums(enabled: Boolean = true) =
    if (enabled) {
        filter { !it.album.explicit }
    } else {
        this
    }

// Extension function to filter YouTube Shorts playlist
fun List<Playlist>.filterYoutubeShorts(enabled: Boolean = false) =
    if (enabled) {
        filterNot { it.playlist.browseId?.startsWith("SS") == true }
    } else {
        this
    }

private val songNoiseRegex = Regex("(?i)\\(official.*?\\)|\\[official.*?]|\\(audio\\)|\\(lyric.*?\\)|official video|official audio|4k|hd")

/**
 * Computes a normalized smart metadata fingerprint key for a song to detect duplicate uploads.
 *
 * @return Normalized fingerprint string based on title, primary artist, and duration bucket.
 */
fun Song.deduplicationKey(): String {
    val cleanTitle = song.title
        .replace(songNoiseRegex, "")
        .normalizeForSearch()
    val primaryArtist = orderedArtists.firstOrNull()?.name?.normalizeForSearch() ?: ""
    val durationBucket = if (song.duration > 0) song.duration / 3 else 0
    return if (cleanTitle.isBlank()) song.id else "$cleanTitle|$primaryArtist|$durationBucket"
}

/**
 * Extension function to deduplicate a list of songs using smart metadata fingerprinting.
 *
 * @return List of unique songs with duplicate uploads removed.
 */
fun List<Song>.deduplicateSongs(): List<Song> = distinctBy { it.deduplicationKey() }
