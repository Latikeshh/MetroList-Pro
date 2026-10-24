# Automated In-App Update Architecture & Audit Report

**Target Repository:** `https://github.com/Latikeshh/MetroList-Pro`  
**Goal:** When a developer pushes a new release/tag (e.g. `v2026.10.25`), the Android app automatically detects the update, shows an in-app notification & dialog with 3 options (Cancel, Visit Repo, Download), and allows direct APK downloading.

---

## 1. Key Finding in Existing Codebase

In [Updater.kt](file:///app/src/main/kotlin/com/metrolist/music/utils/Updater.kt#L46), the GitHub API URL is currently pointing to the legacy upstream repository:

```kotlin
// Current:
private const val GITHUB_API_BASE = "https://api.github.com/repos/MetrolistGroup/Metrolist"

// Needed for MetroList-Pro:
private const val GITHUB_API_BASE = "https://api.github.com/repos/Latikeshh/MetroList-Pro"
```

---

## 2. Complete Workflow Flow (Tag Push to In-App Notification)

```
1. Developer Pushes Tag / Releases Version on GitHub
   ├── git tag v2026.10.25
   └── git push origin v2026.10.25 (or version bump in build.gradle.kts)
         │
         ▼
2. GitHub Actions Workflow (.github/workflows/release.yml)
   ├── Compiles signed APK: Metrolist-Pro-with-Google-Cast.apk
   └── Creates GitHub Release at: https://github.com/Latikeshh/MetroList-Pro/releases/tag/v2026.10.25
         │
         ▼
3. App Checks GitHub Releases API (Updater.kt)
   ├── Requests: https://api.github.com/repos/Latikeshh/MetroList-Pro/releases/latest
   ├── Compares: BuildConfig.BASE_VERSION_NAME ("2026.10.24") vs Latest Tag ("2026.10.25")
   └── Returns: isUpdateAvailable = true
         │
         ▼
4. In-App Notification & Dialog Triggered (MainActivity.kt)
   ├── System Notification: "New Update Available: Metrolist Pro v2026.10.25"
   └── Displays LatikeshUpdateDialog with 3 Options:
         ├── [Cancel / Not Now]: Dismisses dialog
         ├── [Visit Repo]: Opens https://github.com/Latikeshh/MetroList-Pro/releases
         └── [Download]: Opens direct APK download URL
```

---

## 3. Available Architectural Options

### Option A: System Notification + Non-Forced Material 3 Update Dialog (Recommended)
* **How it works:**
  1. On app startup (or manual check in Settings ➔ Updater), `Updater.checkForUpdate()` queries GitHub Releases API.
  2. If a newer tag/version is found:
     - Shows a system tray notification.
     - Displays `LatikeshUpdateDialog` with the release notes and 3 options (**Cancel**, **Visit Repo**, **Download**).
* **Pros:** Non-intrusive, clean Material 3 design, 0 permission issues, complies with Google Play & F-Droid policies.

### Option B: In-App Direct APK Downloader & Package Installer
* **How it works:**
  1. Clicking **Download** in `LatikeshUpdateDialog` uses Android's `DownloadManager` to download `Metrolist-Pro-with-Google-Cast.apk` in background into the Downloads directory with a progress bar.
  2. Upon completion, triggers `Intent(Intent.ACTION_VIEW)` with `application/vnd.android.package-archive` (or `PackageInstaller` API) to prompt the user to install the update.
* **Pros:** User stays inside the app during the download.

---

## 4. Required Modifications Summary

1. **[Updater.kt](file:///app/src/main/kotlin/com/metrolist/music/utils/Updater.kt):** Update `GITHUB_API_BASE` to `"https://api.github.com/repos/Latikeshh/MetroList-Pro"`.
2. **[LatikeshUpdateDialog.kt](file:///app/src/main/kotlin/com/metrolist/music/ui/component/LatikeshUpdateDialog.kt):** Wire the 3 options (**Cancel**, **Visit Repo**, **Download**) directly to `Updater`'s release URL and direct APK asset download URL.
3. **[release.yml](file:///.github/workflows/release.yml):** Ensure GitHub Releases are created with valid release notes whenever a tag or version bump occurs.
