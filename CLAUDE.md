# Taximeter App

Kotlin Multiplatform (Android + iOS) taximeter app using Compose Multiplatform.

## Architecture

- **MVI pattern**: State, Action, Event per screen; ViewModel handles logic; ScreenRoot connects Koin-injected ViewModel to stateless Screen composable
- **Data layer**: Room database with entities, DAOs, and repositories
- **DI**: Koin — shared module in commonMain, platform-specific modules in androidMain/iosMain
- **Navigation**: Compose Navigation with type-safe `@Serializable` route objects
- **Error handling**: `Result<T, E>` sealed interface with `DataError` types

## Project Structure

```
composeApp/src/commonMain/kotlin/com/lerchenflo/taximeter/
├── core/                    # Shared utilities (Result, UiText, LocationTracker interface)
├── datasource/              # Database, repositories, preferences
├── passenger/presentation/  # Passenger list and routes screens (MVI)
├── taximeter/presentation/  # Taximeter screen (MVI)
├── navigation/              # Routes and NavGraph
└── App.kt                   # Root composable with NavHost
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

## Conventions

- Package: `com.lerchenflo.taximeter` (shared), `com.lerchenflo.androidapp` (Android app)
- Presentation files follow: State, Action, Event, ViewModel, ScreenRoot, Screen
- Platform-specific code uses expect/actual pattern
- GPS tracking is Android-only (iOS has stub implementations)
