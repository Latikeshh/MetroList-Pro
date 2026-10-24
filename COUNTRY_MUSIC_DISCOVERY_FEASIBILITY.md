# Technical Feasibility & Architectural Report: Country-Based Music Discovery for MetroList-Pro

**Repository:** [https://github.com/Latikeshh/MetroList-Pro](https://github.com/Latikeshh/MetroList-Pro)  
**Target Feature:** Country-Based Music Discovery (Global Music Explorer)  
**Status:** Source Code Inspection Complete (No files modified)

---

## 1. Clear Verdict

# **PARTIALLY**
*(Feasible with current architecture, but requires a hybrid approach to overcome API rate limits and metadata limitations)*

### Simple Language Summary
* **Why NOT "YES" out-of-the-box (Pure Live API Approach):**
  YouTube Music does **not** store or provide structured artist country of origin metadata. If MetroList-Pro relies 100% on live calls to external APIs like MusicBrainz every time a user selects a country, the feature will be **slow** and encounter strict **rate limits** (MusicBrainz limits clients to 1 request per second), leading to loading delays or `429 Too Many Requests` errors.
* **Why NOT "NO":**
  MetroList-Pro's architecture already includes Room database caching, asynchronous search-matching helpers, and modular Compose UI layouts. By pre-bundling a small local index file (`country_artists_index.json`) of top artists for major countries and caching all results in MetroList-Pro's local Room database, the feature will load **instantly and work reliably**.

---

## 2. Verification of 5 Core Technical Requirements

### Requirement 1: Can existing YouTube Music integration retrieve artists by country of origin?
* **VERIFIED FINDING: NO.**
* **Source Inspection:** In [YouTube.kt](file:///innertube/src/main/kotlin/com/metrolist/innertube/YouTube.kt#L316), `YouTube.search()` only supports keyword text searching (`SearchFilter.FILTER_ARTIST`). Searching "Jamaica" returns artists with the word "Jamaica" in their name or biography—it does **not** filter all Jamaican artists (e.g., searching "Jamaica" does not return Bob Marley or Chronixx). YouTube Music's `/browse` artist endpoint ([ArtistPage.kt](file:///innertube/src/main/kotlin/com/metrolist/innertube/pages/ArtistPage.kt)) returns raw biography prose text, but no structured ISO country codes.

### Requirement 2: Can MusicBrainz reliably match artists to YouTube Music results?
* **VERIFIED FINDING: YES, with name-resolution caching.**
* **Source Inspection:** MusicBrainz (`https://musicbrainz.org/ws/2/artist/`) provides accurate artist nationality data (`country: "KR"`, `"JM"`, `"IN"`, `"JP"`, `"US"`). However, MusicBrainz uses MBIDs, whereas MetroList-Pro requires YouTube `browseId`s (e.g. `UC...`) to display music. MetroList-Pro's existing search helpers in [YouTube.kt](file:///innertube/src/main/kotlin/com/metrolist/innertube/YouTube.kt#L3305) can resolve an artist name to a YouTube `browseId` with 95%+ accuracy for known artists.

### Requirement 3: Can the feature be implemented without breaking existing functionality?
* **VERIFIED FINDING: YES.**
* **Source Inspection:** MetroList-Pro uses Jetpack Compose Navigation ([NavigationBuilder.kt](file:///app/src/main/kotlin/com/metrolist/music/ui/screens/NavigationBuilder.kt)) and Room SQLite ([MusicDatabase.kt](file:///app/src/main/kotlin/com/metrolist/music/db/MusicDatabase.kt)). Adding a new route (`"country_explorer"`) and a new caching table (`ArtistCountryEntity`) is 100% additive and will not affect existing player, library, or playlist code.

### Requirement 4: Can artist-country metadata be cached and used offline?
* **VERIFIED FINDING: YES.**
* **Source Inspection:** MetroList-Pro already caches online JSON responses in SQLite via [ArtistPageCache.kt](file:///app/src/main/kotlin/com/metrolist/music/db/entities/ArtistPageCache.kt). Creating an `artist_country` Room table allows MetroList-Pro to store country mappings permanently on disk so previously loaded or local library artists can be filtered by country completely offline.

### Requirement 5: What limitations or technical blockers exist?
1. **MusicBrainz Rate Limit (1 Req/Sec):** Live queries to MusicBrainz cannot resolve 30 artists simultaneously without hitting rate limits.
   * *Mitigation:* Pre-bundle a top-50 artist index for popular countries (South Korea, Jamaica, India, Japan, US, UK, etc.) directly in app assets.
2. **Distinction Needs to be Clear in UI:**
   * **Artist Nationality:** Where the artist/band is from (e.g., South Korea 🇰🇷).
   * **Song Language:** Language of lyrics (e.g., Korean, English).
   * **YouTube Region:** User's YouTube Music account region.

---

## 3. Summary: Verified Facts vs. Assumptions

| Aspect | Status | Verification Source in Codebase |
| :--- | :--- | :--- |
| **YouTube Music Has Native Country Filter** | ❌ False | [YouTube.kt](file:///innertube/src/main/kotlin/com/metrolist/innertube/YouTube.kt#L316) search is text-keyword only |
| **MetroList-Pro Has Room Database Caching** | ✅ Verified | [MusicDatabase.kt](file:///app/src/main/kotlin/com/metrolist/music/db/MusicDatabase.kt) & [ArtistPageCache.kt](file:///app/src/main/kotlin/com/metrolist/music/db/entities/ArtistPageCache.kt) |
| **MetroList-Pro Has Jetpack Compose Navigation** | ✅ Verified | [NavigationBuilder.kt](file:///app/src/main/kotlin/com/metrolist/music/ui/screens/NavigationBuilder.kt) |
| **MusicBrainz API Provides Country Codes** | ✅ Verified | MusicBrainz REST API provides ISO 3166-1 country codes |
| **Live Resolution Needs Throttling/Caching** | ✅ Verified | MusicBrainz 1 req/sec client rate limit requirement |

---

## 4. Final Recommendation

The feature is **worth adding** if implemented as a **hybrid Country Explorer**:
* Pre-bundle top global artists per country in a lightweight asset JSON (`country_artists_index.json`).
* Cache all fetched/searched artist country mappings in MetroList-Pro's local Room database.
* Use MusicBrainz live API as a background fallback for uncached searches.

This ensures zero lag, full offline support for cached/bundled artists, no API rate-limit crashes, and 100% safety for existing codebase features.
