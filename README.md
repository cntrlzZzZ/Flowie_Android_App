# Flowie

Flowie is an Android app that helps people discover nearby public drinking water spots, save favorites, and contribute new locations.

The current app focuses on Vienna and combines map exploration, community contributions, weather context, and hydration-focused profile features.

> **University project:** Flowie was developed as a group project for the course **Mobile Computing** at **Johannes Kepler University Linz**.  
> **FOCUS THEME (WS 2025): _Mitigating Impacts of Climate Change_**. We chose to interpret this theme through **urban tech**, aiming to make it easier for people to refill water (and reduce bottled water usage).

## Highlights

- Explore nearby water spots on an interactive map (MapTiler + MapLibre SDK).
- Filter by status, origin, wheelchair access, and dog bowl availability.
- Save spots locally for quick revisit in the `Saved` tab.
- Contribute new community spots with metadata and optional photo upload.
- Email OTP authentication with Supabase Auth.
- Basic profile analytics: steps, weather, hydration tracker UI, and reminder notifications.

## Tech Stack

- Kotlin, Jetpack Compose, Material 3
- Android Navigation Compose
- Supabase (`gotrue`, `postgrest`, `storage`)
- Ktor + OkHttp (weather API client)
- MapLibre Android SDK (Mapbox namespace) + MapTiler styles
- Google Play Services Location
- SharedPreferences JSON storage for saved spots

## Repository Structure

```text
.
├── Flowie/                     # Android application (Gradle project)
│   ├── app/src/main/java/fr/eurecom/flowie/
│   │   ├── data/remote/        # Supabase auth + repository
│   │   ├── data/model/         # Spot DTO model
│   │   ├── data/local/         # Local saved spots store
│   │   ├── ui/screens/         # Login, explore, saved, contribute, profile, settings
│   │   ├── ui/components/      # Map wrapper, filters, sheets, bottom bar, notifications
│   │   ├── sensors/            # Step counter + Open-Meteo API client
│   │   └── navigation/         # App routes and NavHost
│   └── gradle/                 # Wrapper + versions catalog
└── flowie_data/                # Data assets + notebooks used for source preparation
```

## Implemented Product Flow

1. User enters email and receives OTP code.
2. User can explore spots on map (guest mode or authenticated).
3. User can save spots locally from details sheet.
4. Authenticated user can add community spots (with optional image).
5. Profile screen shows weather, steps, and hydration widgets.

## Prerequisites

- Android Studio (latest stable)
- JDK 17
- Android SDK (min SDK 24, compile SDK 36)
- A Supabase project with:
  - `public.sources` table
  - `spots-images` storage bucket
- A MapTiler API key

## Local Setup

1. Clone the repository.
2. Open `Flowie/` in Android Studio.
3. Add credentials in `Flowie/local.properties`:

```properties
MAPTILER_API_KEY=your_maptiler_key
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
```

4. Sync Gradle.
5. Run the `app` configuration on emulator/device.

## Launching the App (Android Studio)

1. Open Android Studio and select the `Flowie/` directory.
2. Ensure `Flowie/local.properties` contains `MAPTILER_API_KEY`, `SUPABASE_URL`, and `SUPABASE_ANON_KEY`.
3. Let Gradle sync complete.
4. Select an emulator or connected device.
5. Run the `app` configuration.

## Database (Supabase)

Supabase is used for:
- **Auth:** Email OTP login.
- **Database:** Postgres accessed via PostgREST.
- **Storage:** Image uploads for community contributions.

Core table:
- `public.sources` with key fields:
  - `lat`, `lng`, `address`, `type_label`, `status`, `origin`, `created_by`, `created_at`, `image_path`

Storage bucket:
- `spots-images` (public) for contribution photos.

Access control:
- RLS policies must allow read access and authenticated inserts/uploads for contributions.

### Exporting `sources` to CSV and committing it

1. Supabase Dashboard → **Table Editor** → `sources` → **Export** → **CSV**.
2. Save the file as `flowie_data/sources.csv`.
3. Commit and push:
   - `git add flowie_data/sources.csv`
   - `git commit -m "Add sources CSV snapshot"`
   - `git push`

The team may include a CSV snapshot in the repository for reproducible demos.

## Build and Test

- Build debug APK:
  - `cd Flowie && ./gradlew assembleDebug`
- Run unit tests:
  - `cd Flowie && ./gradlew testDebugUnitTest`
- Run instrumented tests (device/emulator required):
  - `cd Flowie && ./gradlew connectedDebugAndroidTest`

## Permissions Used

- `INTERNET`
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- `ACTIVITY_RECOGNITION`
- `POST_NOTIFICATIONS`
- `CAMERA`
- `READ_EXTERNAL_STORAGE` (legacy/optional)

## Current Scope and Limitations

- City coverage is primarily Vienna.
- Saved spots are local-only (not synced across devices).
- Profile metrics include placeholder/dummy values in some areas.
- Some settings/help text references planned features.

## Portfolio Positioning

Flowie demonstrates end-to-end mobile product engineering:
- Location-based UX and live map interactions
- Backend integration with auth, database, and storage
- Community contribution workflow with media upload
- Modular Compose UI with navigation and reusable components
- Sensor + notification integration for behaviour nudges

## Suggested Next Milestones

- Add CSV db from Supabase.
- Add deterministic tests for repositories, filters, and key UI flows.
- Introduce CI (lint, unit tests, static checks).
- Add architecture docs and screenshots/GIF walkthrough.
- Improve offline behaviour (cache + sync strategy).
- Harden contribution moderation/reporting workflows.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
