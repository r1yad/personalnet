# PersonaNet v1.1: Performance, Lag Optimization & System Insets Plan (Revised)

Comprehensive architecture and implementation plan addressing:
1. **Status Bar Visibility, Overlapping Content & System Insets** (Light status-bar icons, solid DarkSurface status background, prevention of content drawing under system icons).
2. **App Performance & Lag Elimination** (Room N+1 query batching, Compose `@Immutable` skipping, LazyColumn `contentType`, Coil memory caching & downsampling).
3. **APK Size Reduction** (Unused Firebase dependency pruning, release build R8 shrinking).

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture Decisions for System Bars & Inset Handling**:
> - **Light Status Bar Icons on Dark Theme**: Explicitly configure `enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(...), navigationBarStyle = SystemBarStyle.dark(...))` and set `WindowInsetsController.isAppearanceLightStatusBars = false` and `isAppearanceLightNavigationBars = false`. This guarantees status bar icons (clock, battery, Wi-Fi) and navigation buttons are crisp white/light and NEVER disappear or turn black on dark backgrounds.
> - **Solid Dark Material You Status Bar Separation**: Provide a solid `DarkSurface` (`0xFF12151F`) background across all top bars and status bar insets. App feed items, profile photos, and text will never bleed or merge into status bar icons while scrolling.
> - **Unified Inset Architecture**: Root and screen Scaffolds are aligned so every screen (`Feed`, `DMs`, `Personas`, `Profile`, `Detail`, `Settings`, `Chat`) uses consistent `statusBars` handling with zero unexpected blank gaps and zero overlapping.
> - **Performance Architecture**: Batch comment counts in Room, mark Compose models `@Immutable` for zero-cost recomposition skipping, downsample thumbnails in Coil, comment out unused Firebase libraries, and enable R8 release shrinking.

---

### 1. Overview & Core Concept

PersonaNet v1.1 will achieve rock-solid visual consistency and fluid 60/120 FPS performance:

1. **System Bar Integrity**: The Android status bar remains consistently visible, legible, and separated by a dark Material You surface. No screen or modal will cause status bar icons to disappear, turn dark, or merge into scrollable content.
2. **Smooth & Responsive Compose UI**: Eliminates recomposition stutters across the feed and direct message lists by providing `@Immutable` annotations, stable keys, and LazyColumn item content types.
3. **Optimized Database Operations**: Replaces the synchronous N+1 `commentDao.getCommentCountForPostDirect()` loop in `PostRepository` with an efficient single reactive `GROUP BY` batch query.
4. **Fast, Lightweight Media Decoding**: Downsamples image loading to fit feed/DM display dimensions with Coil hardware bitmaps and memory caching.
5. **Clean Release APK**: Comments out unused template Firebase dependencies and enables R8 code and resource shrinking for release APK packaging.

---

### 2. User Experience & Visual Design

- **Status Bar Appearance**: The physical status bar displays crisp, bright white system indicators (time, notifications, network, battery) resting on a clean `DarkSurface` (`#12151F`) tone that matches the app's top app bar and dark theme.
- **Header & Feed Visual Separation**: When scrolling feeds or conversation lists, cards and text slide cleanly underneath the opaque top app bar without bleeding into the status icons.
- **Full-Screen Media & Dialogs**: Bottom sheets (`CreatePostDialog`, `PersonaSwitcherBottomSheet`) and full-screen image viewers properly respect status bar and navigation bar insets with scrim backdrops.
- **Scroll & Transition Fluidity**: Rapid flings through feeds and direct messages remain smooth without stutter or frame drops.

---

### 3. Key Technical Decisions & System Architecture

#### A. System Bar Configuration (`MainActivity.kt` & `Theme.kt`)
- Call `enableEdgeToEdge` with explicit dark styles:
  ```kotlin
  enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
  )
  ```
- In `MainActivity.onCreate`, configure `WindowCompat.getInsetsController`:
  ```kotlin
  WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = false
      isAppearanceLightNavigationBars = false
  }
  ```
- Ensure window background color is `#12151F` to prevent light flashes or white bars during transitions.

#### B. Scaffold & Top Bar Inset Harmonization
- In `PersonaNetApp.kt`, configure root Scaffold with `contentWindowInsets = WindowInsets.statusBars` and add a solid top status-bar surface, ensuring screen headers align flush with system insets without double-padding or under-drawing.
- Ensure all screens (`FeedScreen`, `ChatScreen`, `ConversationsScreen`, `PersonaProfileScreen`, `EditPersonaScreen`, `PostDetailScreen`, `BackupSettingsScreen`) have opaque `DarkSurface` top app bars with standard `TopAppBarDefaults.windowInsets`.

#### C. Database Query Optimization (Eliminating N+1 Reads)
- Add to `CommentDao`:
  ```kotlin
  @Query("SELECT postId, COUNT(*) as count FROM comments GROUP BY postId")
  fun getAllCommentCountsFlow(): Flow<List<PostCommentCount>>
  ```
- In `PostRepository.getFeedPosts` and `getPostsByPersona`, replace the per-post synchronous `commentDao.getCommentCountForPostDirect(post.id)` call with an in-memory map lookup:
  ```kotlin
  val commentMap = commentCounts.associate { it.postId to it.count }
  // ...
  commentCount = commentMap[post.id] ?: 0
  ```

#### D. Compose Recomposition & Skipping Optimization
- Annotate `PostWithDetails`, `MessageWithDetails`, `ConversationWithDetails`, and `PersonaEntity` with `@androidx.compose.runtime.Immutable`.
- Add `contentType = { "post_card" }` and `contentType = { "chat_message" }` to `LazyColumn` item blocks.

#### E. Coil Image Downsampling & Caching
- Update `AsyncImage` in `PostMediaGallery` and `PersonaAvatar` with `precision(Precision.INEXACT)`, `allowHardware(true)`, and `memoryCachePolicy(CachePolicy.ENABLED)`.

#### F. Release APK Optimization
- Comment out unused Firebase libraries (`libs.firebase.ai`, `libs.firebase.appcheck.recaptcha`, `libs.firebase.appcheck.debug`) in `app/build.gradle.kts`.
- Remove unreferenced media assets in `res/raw/`.
- Enable R8 shrinking in `buildTypes.release`:
  ```kotlin
  release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
  }
  ```
- Configure keep rules in `app/proguard-rules.pro` for Room, Moshi, and Compose models.

---

### 4. Technical Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│ Android System Status Bar (White Icons on #12151F DarkSurface)   │
└────────────────────────────────┬─────────────────────────────────┘
                                 │ WindowInsets.statusBars
┌────────────────────────────────▼─────────────────────────────────┐
│ TopAppBar (Opaque DarkSurface, Title, Actions, Back Navigation)  │
├──────────────────────────────────────────────────────────────────┤
│ Compose LazyColumn (contentType, stable keys, @Immutable models) │
│   - PostCard / MessageBubble (recomposition skipping enabled)    │
│   - Coil AsyncImage (hardware bitmaps, memory cached)            │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────┐
│ PostRepository (Batch Reactive Combining with 0 N+1 Queries)    │
│   - PostsFlow + PersonasFlow + MediaFlow + CommentCountsFlow     │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────┐
│ Room Database (SQLite with Indexed Foreign Keys & Aggregations)  │
└──────────────────────────────────────────────────────────────────┘
```

#### Verification Plan
1. **Compilation**: Run `compile_applet`.
2. **Unit Tests**: Run `gradle :app:testDebugUnitTest`.
3. **APK Size**: Measure and report APK sizes before and after optimizations.
4. **Visual Inspection**: Verify status bar visibility and contrast across screens.
