# መዝሙረ ተዋሕዶ — Mezmure Tewahedo (Android app)

A native Android app built from your hymnal document (`MEZMURE_TEWHADO_2_.doc`).

## What's in the box

- **485 hymns**, correctly decoded from the original document's legacy Amharic/Ge'ez
  fonts (Ge'ez‑1, Addis98, Geezigna) into proper Unicode, and organized into 13
  categories (New Year & Meskel, Christmas, Timket, Marian hymns, Repentance,
  Hosanna, Resurrection, Praise, Wedding, Saints & Angels, Debre Tabor, etc.).
  This data ships as `app/src/main/assets/hymns.json` and is loaded into the
  app's local database the first time it runs.
- A **native Kotlin Android app** (Room database, RecyclerView, Material 3) with:
  - An **expandable list**: tap a category to expand/collapse its hymns.
  - **Search** across titles and lyrics.
  - A **Favorites** tab.
  - A **hymn detail screen** with share, favorite, edit, and delete.
  - **Two ways to add new lyrics**, as you asked:
    1. **In‑app editor** — the ➕ button opens a form to type in a new hymn
       (title, category, lyrics) directly.
    2. **Folder import** — drop a `.txt` file into the app's own folder on the
       device and tap the sync icon (or pull to refresh) to import it. See
       "Adding lyrics via folder" below.

## Getting an installable APK without installing anything

There's no reliable "upload a zip, get an APK" website for a real Gradle
project — but this project includes a **GitHub Actions** workflow
(`.github/workflows/build-apk.yml`) that builds the APK for you, for free,
entirely in the browser:

1. Create a free [github.com](https://github.com) account if you don't have one.
2. Create a new repository (any name, can be private).
3. Upload this whole project into it — easiest way: on the repo page, use
   "Add file → Upload files" and drag in everything from the unzipped
   `MezmureTewahedo` folder (or use `git push` if you're comfortable with git).
4. Go to the **Actions** tab of your repo. The "Build debug APK" workflow
   runs automatically after the upload (or click "Run workflow" to trigger
   it manually).
5. Wait a few minutes for it to finish (green checkmark), then open that
   run and download the **MezmureTewahedo-debug-apk** artifact at the
   bottom of the page — that's a `.zip` containing your installable
   `app-debug.apk`.
6. Transfer the `.apk` to your Android phone (email, Google Drive, USB) and
   tap it to install. You'll need to allow "install from unknown sources"
   the first time, since it isn't signed by the Play Store.

This produces an unsigned **debug** build, which is fine for installing on
your own device and testing. If you later want to publish it to the Play
Store, that needs a signed release build — Android Studio can do that for
you with a few clicks (Build → Generate Signed Bundle/APK).

## Opening the project

1. Install **Android Studio** (Giraffe/Koala or newer).
2. `File → Open` and select this project folder.
3. Let Gradle sync. If it asks to create a Gradle wrapper, accept — the
   wrapper `.jar` binary isn't included in this delivery, only
   `gradle/wrapper/gradle-wrapper.properties`, since I can't fetch binaries
   from Gradle's servers in the environment I built this in. Android Studio
   will regenerate `gradlew`/`gradlew.bat`/`gradle-wrapper.jar` for you
   automatically the first time you open the project (or run
   `gradle wrapper` yourself if you have Gradle installed).
4. Run on a device or emulator (▶ button). Min SDK 24 (Android 7+).

## Adding lyrics via folder

The app creates this folder on first "Sync":

```
Android/data/com.mezmuretewahedo.app/files/ImportLyrics/
```

(visible over USB/file manager on the device — no special permission needed,
since it's the app's own external folder). Drop a plain `.txt` file in there,
formatted like this:

```
Title: የመዝሙሩ ርዕስ
Category: ምድብ (optional)
---
የመዝሙሩ ግጥም
በዚህ ቦታ ይቀጥላል...
```

The `Category:` line and the `---` separator are optional — if you skip them,
the first line becomes the title and everything else becomes the lyrics.

Then in the app, tap the sync icon in the toolbar (or pull down to refresh)
and it scans the folder, adds any new hymns it finds, and archives the
processed files into an `Imported/` subfolder so they aren't re-added.

## Project structure

```
app/src/main/java/com/mezmuretewahedo/app/
  MezmurApp.kt                 - Application class, wires up the database/repo, seeds data on first run
  data/Hymn.kt                 - Room entity
  data/HymnDao.kt               - Room DAO (queries)
  data/HymnDatabase.kt          - Room database
  data/HymnRepository.kt        - single access point used by the UI
  data/SeedLoader.kt             - loads assets/hymns.json on first launch
  data/LyricsFileImporter.kt     - the folder-drop import feature
  ui/MainActivity.kt             - expandable category list, search, favorites tab
  ui/HymnListAdapter.kt          - RecyclerView adapter (headers + rows)
  ui/ListItem.kt                 - grouping logic for the expandable list
  ui/HymnDetailActivity.kt       - full lyrics view, share/favorite/edit/delete
  ui/AddEditHymnActivity.kt      - in-app add/edit form
```

## Notes on the source-document conversion

The original `.doc` file used several legacy, pre-Unicode Ethiopic fonts
(Ge'ez‑1/Power Ge'ez, Addis98/Samawerfa, Geezigna) mixed with some runs that
were already proper Unicode. I converted the legacy-encoded runs using the
public domain character-mapping tables from the `geezorg/geez-lib` project
(ICU transliteration rules originally sourced from the LibEth library), applied
per-run based on the font actually assigned to that run in the document —
that's why headings, footnotes, etc. all came out correctly instead of as a
single garbled blob.

Hymns were then automatically segmented using the document's own numbered,
bold headings ("**12. Title**") cross-referenced against its table of
contents for category names. This was a best-effort automated parse over
~500 hymns — if you spot a title or category that looks off for a specific
hymn, it's easiest to just fix it right in the app (tap the hymn → Edit).
