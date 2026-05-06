# NexRe

A privacy-first, on-device read-it-later app for Android. Save links and plain text snippets from the share sheet, get AI-powered bullet-point summaries via Gemini Flash, and track your reading habits — all without an account or server.

---

## Why

The default workflow for saving interesting content on Android is broken. You share a link to WhatsApp or Telegram, plan to read it later, and never do. The context is gone, the link is buried in a chat, and there is no way to know what you have already read.

NexRe fixes this by living in the Android share sheet as two distinct targets: one that saves silently in the background, and one that pops up a Gemini summary before you commit to saving. Everything is stored on-device in Room. No account. No sync. No subscription.

---

## Features

### Two share targets

**NexRe — Save**
Completely silent. Share a URL or plain text → activity receives it → WorkManager job runs in the background → quiet notification confirms the save. The app never opens.

**NexRe — Summarize**
Opens a translucent bottom sheet over whatever app you were using. Fetches OG metadata and article body, sends it to Gemini Flash, and returns a structured bullet-point summary with suggested tags. Tap Save and you are back where you started.

### Reading library
- Home screen with unread count, reading streak, weekly reads, and a "Next Read" hero card that surfaces the best unread item (prefers Gemini-summarised links)
- Library with filter tabs: All · Unread · Read · Archived · Favourites
- Topics screen listing all tags with unread counts; tap to filter
- Full-text search across title, description, URL, notes, and tags — entirely local

### Swipe gestures
- Right swipe → toggle Read / Unread (green for mark-read, blue for mark-unread)
- Left swipe → toggle Archive / Unarchive (orange for archive, blue for unarchive)

### AI summaries
- Gemini 2.0 Flash via REST
- Returns 4–6 bullet points covering what the topic is, key findings, why it matters, and notable details
- Fallback chain: no API key → prompt in sheet; no body text → OG description; Gemini failure → OG description with `summary_source = OG_META`
- API key stored in `EncryptedSharedPreferences` (AES256-GCM); never held in memory beyond the call

### Plain text support
Both share targets handle plain text (not just URLs). Quotes, notes, copied paragraphs, and tweet text all save correctly. Text notes display their full body in the detail view instead of a browser button.

### Source detection
Automatic platform detection from URL domain with branded badges: GitHub, LinkedIn, X/Twitter, Medium, dev.to, Stack Overflow, arXiv, and Web. Plain text saves show a "Note" badge.

### Detail view
- Full Gemini summary rendered as individual bullet lines
- Editable personal note
- Favourite toggle
- Stats: saved date, open count, total read time
- "Open in Browser" via Chrome Custom Tab (hidden for text notes)
- "Did you finish reading?" snackbar on return — only mechanism for tracking read time

### Export
JSON export to Downloads with full schema: all fields including `tags`, `summary_source`, `read_duration_sec`, `read_count`. Uses MediaStore API on Android 10+.

### Settings
- Gemini API key input with show/hide and Save & Test (validated via `GET /v1beta/models`)
- Export, Clear archived, Clear all — each gated behind a confirmation dialog
- Daily reminder and weekly digest notification toggles

---

## Tech stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture (data / domain / ui) |
| Database | Room |
| Background | WorkManager (custom factory, no `@HiltWorker`) |
| DI | Hilt |
| Networking | Retrofit + OkHttp + Moshi |
| Secure storage | EncryptedSharedPreferences |
| HTML parsing | Jsoup |
| Image loading | Coil |
| Preferences | DataStore |
| Browser | Chrome Custom Tabs |
| AI | Gemini 2.0 Flash Lite (REST) |

---

## Architecture

```
app/
├── data/
│   ├── local/           # Room database, DAOs, entities
│   ├── remote/          # GeminiApiService, OgFetcher, KeywordTagger
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Link, Tag, enums (pure Kotlin)
│   ├── repository/      # Repository interfaces
│   └── usecase/         # SaveLinkUseCase, SaveTextUseCase,
│                        #   SummarizeLinkUseCase, ValidateGeminiKeyUseCase,
│                        #   ExportJsonUseCase
├── ui/
│   ├── home/            # Home screen + ViewModel
│   ├── library/         # Library screen + ViewModel
│   ├── tags/            # Topics screen + ViewModel
│   ├── search/          # Search screen + ViewModel
│   ├── detail/          # Detail screen + ViewModel
│   ├── settings/        # Settings screen + ViewModel
│   ├── onboarding/      # First-launch carousel
│   ├── share/           # SummarizeBottomSheet + ShareViewModel
│   ├── navigation/      # NexReNavHost
│   ├── components/      # LinkCard, TagChip, SourceBadge,
│   │                    #   GradientThumbnail, ReadDot, EmptyState
│   └── theme/           # Color, Type, NexReTheme
├── share/
│   ├── StoreActivity.kt       # Silent save share target
│   └── SummarizeActivity.kt   # Bottom sheet share target
├── worker/
│   ├── StoreLinkWorker.kt     # OG fetch + save (URL or plain text)
│   ├── NexReWorkerFactory.kt  # Custom factory — WorkManager initialised manually
│   ├── DailyReminderWorker.kt
│   └── WeeklyDigestWorker.kt
└── di/
    ├── DatabaseModule.kt
    ├── NetworkModule.kt
    ├── RepositoryModule.kt
    └── WorkerModule.kt
```

**WorkManager initialisation**: The Hilt `WorkManagerInitializer` is removed from the manifest via `tools:node="remove"`. `NexReApplication` implements `Configuration.Provider` and supplies `NexReWorkerFactory` directly. Workers are instantiated by the factory with their injected dependencies rather than using `@HiltWorker`, which avoids the Hilt work artifact's additional complexity.

**Swipe gesture implementation**: `SwipeToDismissBox` from Material 3 1.3+. `confirmValueChange` always returns `false` so the card snaps back instead of sliding off. The raw drag offset is read via `requireOffset()` (wrapped in `runCatching` to handle the pre-layout frame) so the colour reveal starts from the first pixel of drag rather than only after the threshold. `rememberUpdatedState` is used for the action callbacks so the lambda inside `confirmValueChange` always reads the latest status-aware action even though the `remember`ed state is created once.

---

## Data model

```
links
  id                TEXT PRIMARY KEY
  url               TEXT
  title             TEXT
  description       TEXT
  thumbnail_url     TEXT
  source_platform   TEXT  (GITHUB | LINKEDIN | TWITTER | MEDIUM | DEV |
                           STACKOVERFLOW | RESEARCH | WEB | TEXT)
  status            TEXT  (UNREAD | READ | ARCHIVED)
  is_favourite      INTEGER
  personal_note     TEXT
  summary           TEXT
  summary_source    TEXT  (NONE | OG_META | GEMINI)
  saved_at          INTEGER  (epoch ms)
  opened_at         INTEGER  (epoch ms)
  read_duration_sec INTEGER
  read_count        INTEGER

tags
  id    INTEGER PRIMARY KEY AUTOINCREMENT
  name  TEXT UNIQUE

link_tags  (junction)
  link_id  TEXT
  tag_id   INTEGER
  source   TEXT  (KEYWORD | GEMINI)
```

---

## Getting started

### Requirements
- Android Studio Hedgehog or later
- Android device / emulator running API 26+
- A Gemini API key (free tier available at [aistudio.google.com](https://aistudio.google.com)) — required only for summaries

### Build

```bash
git clone https://github.com/your-username/nexre.git
cd nexre
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### First run
1. Launch the app — onboarding carousel plays once
2. Open Settings → paste your Gemini API key → tap **Save & Test API Key**
3. Go to any browser or app, share a URL or text
4. Choose **NexRe — Save** for a silent background save, or **NexRe — Summarize** to preview before saving

---

## Gemini integration

The app sends requests to:
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key={API_KEY}
```

The prompt instructs Gemini to return a JSON object with two fields:

```json
{
  "summary": "• What it is: ...\n• Key point: ...\n• Why it matters: ...",
  "tags": ["android", "kotlin", "compose"]
}
```

Key validation uses `GET /v1beta/models` — a simple, body-free request that returns 200 for valid keys and 403 for invalid ones, with no JSON parsing involved.

---

## Export format

```json
{
  "exported_at": "2026-05-07T10:00:00Z",
  "app_version": "1.0.0",
  "total_links": 42,
  "links": [
    {
      "id": "uuid",
      "url": "https://example.com/article",
      "title": "Article title",
      "description": "OG description",
      "thumbnail_url": "",
      "source_platform": "WEB",
      "status": "READ",
      "is_favourite": false,
      "personal_note": "",
      "summary": "• What it is: ...",
      "summary_source": "GEMINI",
      "tags": ["android", "kotlin"],
      "saved_at": 1746614400000,
      "opened_at": 1746700800000,
      "read_duration_sec": 142,
      "read_count": 1
    }
  ]
}
```

---

## Accent colour

`#3B4ACC` — deep indigo, used as the Material 3 primary throughout the app.

---

## License

MIT
