```text
███████╗██╗      ██████╗ ██╗    ██╗██╗███████╗ 
██╔════╝██║     ██╔═══██╗██║    ██║██║██╔════╝
█████╗  ██║     ██║   ██║██║ █╗ ██║██║█████╗  
██╔══╝  ██║     ██║   ██║██║███╗██║██║██╔══╝  
██║     ███████╗╚██████╔╝╚███╔███╔╝██║███████╗
╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝ ╚═╝╚══════╝
```

Flowie is an Android app that helps people discover nearby public drinking water spots, save favorites, and contribute new locations.

The current app focuses on Vienna and combines map exploration, community contributions, weather context, and hydration-focused profile features.

> **University project:** Flowie was developed as a group project for the course **Mobile Computing** at **Johannes Kepler University Linz**.  
> **FOCUS THEME (WS 2025): _Mitigating Impacts of Climate Change_**. We chose to interpret this theme through **urban tech**, aiming to make it easier for people to refill water (and reduce bottled water usage).

<img width="212" height="212" alt="Group 1" src="https://github.com/user-attachments/assets/9e365d61-3cbc-44e9-997e-49c0aa53f1f3" />

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
<img width="920" height="468" alt="App_Map_Product_Flow" src="https://github.com/user-attachments/assets/ff26ad25-b08c-4154-b146-2f00030c1720" />


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

1. Clone the repository.  
   The database snapshot CSV is already included in Git under `flowie_data/data/` and is cloned together with the code.
2. Open the `Flowie/` folder in Android Studio.
3. Create/update `Flowie/local.properties` with your own keys:
   - `SUPABASE_URL`
   - `SUPABASE_ANON_KEY`
   - `MAPTILER_API_KEY`
4. In your own Supabase project, create the required schema (table + storage bucket) and import the CSV snapshot (see section below).
5. Sync Gradle and run the `app` configuration on an emulator or device.

## Database (Supabase)

Flowie uses Supabase for:
- **Auth:** Email OTP login.
- **Database:** Postgres (queried via PostgREST).
- **Storage:** Contribution images.

### Data origin

Flowie’s dataset combines:
- **Verified seed data:** initial water points imported from Vienna open government data.  
  Source: dataset obtained on **Wed Dec 10 18:05:07 2025** from  
  https://www.data.gv.at/katalog/dataset/stadt-wien_trinkbrunnenstandortewien
- **Community data:** user-submitted spots created in-app after authentication.

Seed data provides immediate baseline coverage; community data expands and updates coverage over time.

### CSV snapshot in repo and import workflow

- A CSV database snapshot is stored in the repository under `flowie_data/data/` for reproducible demos.
- After cloning:
  1. Open your Supabase project.
  2. Create table `public.sources` (matching columns below).
  3. Import the CSV from `flowie_data/data/` into `public.sources`.
  4. Create storage bucket `spots-images` (public read).
  5. Add your Supabase + MapTiler keys in `Flowie/local.properties`.

### Table: `public.sources` (core schema)

Key columns and usage:
- `id` (uuid/text): unique spot identifier used across map, details, and saved state.
- `origin` (text): `verified` (seed/imported) or `community` (user-created).
- `type_label` (text): normalised category, e.g. `outdoor_water_fountain`, `indoor_water_fountain`.
- `status` (text): `active` or `inactive`.
- `lat`, `lng` (double): WGS84 coordinates used for marker placement and bbox queries.
- `address` (text, nullable): human-readable location.
- `wheelchair_access` (bool, nullable): accessibility flag (`null` = unknown).
- `dog_bowl` (bool, nullable): amenity flag (`null` = unknown).
- `image_path` (text, nullable): storage path such as `community/<uuid>.jpg`.
- `created_by` (uuid/text, nullable): Supabase user id of contributor.
- `created_at` (timestamp, nullable): creation time (e.g., for newest-first ordering).
- Optional import-tracking fields (e.g. `external_id`) may be present for upstream mapping.

### Storage: `spots-images`

- Public bucket used for contribution photos.
- Database stores `image_path`, not full URLs.
- App resolves image URLs using:  
  `SUPABASE_URL/storage/v1/object/public/spots-images/<image_path>`

### Runtime data flow

- **Explore:** map viewport is converted to bbox (`minLat/minLng/maxLat/maxLng`), then queried from `public.sources`; UI filters are applied client-side.
- **Saved:** favourites are local-only (SharedPreferences/JSON), no server write for save/unsave.
- **Contribute:** optional image upload to `spots-images`, then insert into `public.sources` with `origin='community'`, contributor id, coordinates, address, metadata, and optional `image_path`.

### Security model

Flowie relies on Supabase Auth + Row Level Security (RLS):
- **Auth:** Email OTP sessions identify users (`auth.uid()`).
- **RLS on `public.sources`:**
  - allow `SELECT` per your policy (public or authenticated-only),
  - allow `INSERT` only for authenticated users creating community rows,
  - enforce `created_by = auth.uid()` (policy and/or DB default/trigger),
  - block client edits/deletes of verified seed data.
- **Storage policies (`spots-images`):**
  - public read for display,
  - uploads restricted to authenticated users,
  - optional path constraints (e.g. uploads only under `community/`).
- **Data minimisation:** contributor identity stored as UUID (`created_by`), optional attributes nullable, and only storage paths persisted.

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
