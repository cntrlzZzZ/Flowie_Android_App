# Flowie

Flowie is an Android app that helps people discover nearby public drinking water spots, save favourites, and contribute new locations.

The current app focuses on Vienna and combines map exploration, community contributions, weather context, and hydration-focused profile features.

> **Academic context:** This project was developed as a **university group project** for the **Mobile Computing** class at **Johannes Kepler University Linz** (FOCUS THEME: **WS 2025 — Mitigating Impacts of Climate Change**). We chose to explore the theme through **urban tech**, focusing on improving access to water refill infrastructure and encouraging sustainable behaviour (refill instead of buying bottled water).

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
