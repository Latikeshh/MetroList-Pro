# feat(export): add public audio exporter and playlist search

## Problem
1. Users were unable to export/download standalone audio files (`.mp3`/`.m4a`) to public device storage (`Music/Metrolist`) for playback in external media players or PC file transfers.
2. Duplicate songs appeared in library lists, artist song lists, album song lists, and queue continuations due to missing SQL query DISTINCT clauses and lack of metadata deduplication.
3. Users lacked the ability to search for saved playlists in the Library Playlists tab and filter songs inside open playlists.

## Cause
1. In-app downloads were stored in ExoPlayer's private sandboxed cache directory (`/data/data/com.metrolist...`), making files inaccessible to external apps.
2. SQL queries joining relational mapping tables (`song_artist_map` and `song_album_map`) without `DISTINCT` clauses returned multiple duplicate `Song` rows when a track was linked to multiple artists or albums.
3. `PlaylistsViewModel` and `LocalPlaylistViewModel` did not expose debounced search query flows or normalized text matching filters.

## Solution
1. **Public Audio Exporter (`LatikeshExporter.kt`):**
   * Implemented `LatikeshExporter.exportToPublicStorage(...)` which resolves playback audio stream, streams audio bytes with a 16KB buffer on `Dispatchers.IO`, and saves files to `Environment.DIRECTORY_MUSIC + "/Metrolist"` via `MediaStore.Audio.Media`.
   * Added an existing file check (`isAudioFileAlreadyExported`) to prevent duplicate file downloads.
   * Added live progress notifications in the notification shade using `NotificationCompat.Builder`.
   * Added **"Export audio to public storage"** option with storage icon to `SongMenu.kt`.

2. **Deduplication Fixes (`DatabaseDao.kt`, `ListExt.kt`, `LibraryViewModels.kt`, `YouTubeQueue.kt`):**
   * Added `DISTINCT` to SQL queries in `DatabaseDao.kt` (`albumSongs`, `artistSongsByCreateDateAsc`, `artistSongsByNameAsc`, `artistSongsByPlayTimeAsc`, `artistSongsPreview`).
   * Implemented smart metadata fingerprinting (`deduplicationKey()`) and `deduplicateSongs()` in `ListExt.kt`.
   * Applied `.deduplicateSongs()` across all song list flows in `LibraryViewModels.kt`.
   * Applied `.distinctBy { it.mediaId }` to initial queue and pagination items in `YouTubeQueue.kt`.

3. **Playlist Search Features (`PlaylistsViewModel.kt`, `LocalPlaylistViewModel.kt`, `LibraryPlaylistsScreen.kt`):**
   * Added `searchQuery` and `debouncedSearchQuery` filtering using `matchesNormalizedQuery` in `PlaylistsViewModel.kt` and `LocalPlaylistViewModel.kt`.

## Testing
- [x] **Gradle Build:** Verified compilation using `./gradlew :app:assembleFossDebug` (`BUILD SUCCESSFUL`).
- [x] **Device Deployment:** Installed and tested on connected device (`SM-E156B`).
- [x] **Export Verification:** Verified audio file exporting to `Music/Metrolist` with live notification progress bar.
- [x] **Existing File Detection:** Verified that attempting to export an already saved song skips downloading and displays `"File already exists in Music/Metrolist"`.
- [x] **UI Performance:** Verified zero UI freeze or stutter during audio export.

## Related Issues
None
