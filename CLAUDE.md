# Taximeter App

Kotlin Multiplatform (Android + iOS) taximeter app using Compose Multiplatform.

## Architecture

- **MVI pattern**: State, Action, Event per screen; ViewModel handles logic; Root composable connects Koin-injected ViewModel to stateless Screen composable (both in same file)
- **Data layer**: Room database with entities, DAOs, and repositories in `datasource/`
- **DI**: Koin — `datasourceModule` + `presentationModule` in commonMain, platform-specific modules in androidMain/iosMain
- **Navigation**: Compose Navigation with type-safe `@Serializable` route objects
- **Error handling**: `Result<T, E>` sealed interface with `DataError` types in `utilities/`

## Project Structure

```
composeApp/src/commonMain/kotlin/com/lerchenflo/taximeter/
├── app/                        # App-level: root composable, navigation
│   ├── App.kt
│   └── navigation/             # Routes and NavGraph
├── passenger/presentation/     # Passenger list and routes screens (MVI)
├── taximeter/
│   ├── domain/                 # LocationTracker, DistanceCalculator
│   └── presentation/           # Taximeter screen (MVI)
├── datasource/
│   ├── database/               # Room DB, DAOs, entities
│   ├── preferences/            # DataStore Preferencemanager
│   └── repository/             # PassengerRepository, RouteRepository
├── di/                         # Koin module definitions
└── utilities/                  # Result, DataError, UiText, FormatUtils, ObserveEvents, TimeUtils
```

## Modules

- `:composeApp` — Shared KMP code (commonMain, androidMain, iosMain)
- `:androidapp` — Android application entry point

## Key Dependencies

- Kotlin 2.3.20, Compose Multiplatform 1.10.3
- Room 2.8.4 (database), Koin 4.2.0 (DI), DataStore 1.2.1 (preferences)
- kotlinx-datetime 0.6.2, kotlinx-serialization 1.11.0
- Navigation Compose 2.9.0

## Database

Room database with destructive migration fallback. Entities: `Passenger`, `Route`, `RoutePoint`.

## Build & Run

```bash
./gradlew :androidapp:assembleDebug
```

Multiple Claude instances may edit this codebase concurrently. Ignore pre-existing build errors unrelated to your changes.

## Conventions

- Package: `com.lerchenflo.taximeter` (shared), `com.lerchenflo.androidapp` (Android app)
- Presentation files follow: State, Action, Event, ViewModel, Root + Screen (in same file)
- Root composable suffix: `Root` (e.g. `PassengerListRoot`), Screen suffix: `Screen`
- Platform-specific code uses expect/actual pattern
- GPS tracking is Android-only (iOS has stub implementations)
- DI: `datasourceModule` for all data singletons, `presentationModule` for all ViewModels
