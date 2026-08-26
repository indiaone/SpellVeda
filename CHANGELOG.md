# Changelog

All notable changes to this project will be documented in this file.

## [0.2.1] - 2026-08-27

### Fixed and improved
- Moved fastlane metadata to the repository root for F-Droid compatibility.
- Added the launcher icon and phone screenshots under the standard en-US/images paths.
- Replaced README placeholder screenshots with the project’s release media.

## [0.2.0] - 2026-08-26

### Fixed and improved
- Fixed editing words when their spelling or category changes.
- Fixed quiz setup for categories with fewer than 10 words.
- Added safe TTS fallback when an en-IN voice is unavailable.
- Restored saved TTS voices and clamped speech-rate preferences.
- Reduced DataStore writes while dragging the speech-rate slider.
- Added delete confirmation and a scrollable word editor.
- Added quiz question-count unit tests.

## [0.1] - 2026-08-25

### Added
- Initial alpha release of SpellVeda.
- Core spelling quiz engine with TTS support.
- Learning mode with word definitions and examples.
- Difficulty categories (1-4).
- User preferences (TTS voice, speech rate, app mode).
- Offline support via Room database.
- Modern Jetpack Compose UI with Material 3.
- Dark mode support.
