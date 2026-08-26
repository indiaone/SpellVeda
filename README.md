# SpellVeda 🐝

**SpellVeda** is a modern, feature-rich Spelling Bee application for Android designed to help users master their spelling skills through an interactive and personalized learning experience. Combining the power of "Spell" and "Veda" (Knowledge), this app offers a comprehensive toolset for both competitive practice and relaxed learning.

## ✨ Features

- 🎧 **Interactive Quizzes**: Test your spelling with high-quality Text-to-Speech (TTS) technology.
- 📖 **Dual Modes**:
  - **Quiz Mode**: A focused testing environment to sharpen your accuracy.
  - **Learning Mode**: Provides immediate access to word definitions and usage examples as you practice.
- 🗂️ **Categorized Learning**: Words are organized into distinct categories (Difficulty 1-4) to cater to all skill levels.
- 🔊 **Advanced TTS Customization**: Choose from different voices and adjust the speech rate to suit your listening preference.
- 🌗 **Modern UI/UX**: Built with Jetpack Compose and Material 3, featuring a responsive design with Light and Dark mode support.
- 💾 **Offline-First**: Powered by a local Room database, ensuring all features are accessible without an internet connection.
- 🛠️ **Quiz Wizard**: Quickly set up custom quiz sessions with specific categories and question counts.

## 🚀 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for local persistence.
- **Preferences**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for user settings.
- **Navigation**: Navigation3 (Experimental) for screen transitions.
- **Networking**: Retrofit, OkHttp, and Moshi (for potential future API integrations).
- **Concurrency**: Kotlin Coroutines and Flow.
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/compose/) for efficient image handling.

## 🛠️ Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or higher.
- **Kotlin** 2.0.0 or higher.
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 37 (Android 15)
- **Current Version**: 0.1

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/indiaone/SpellVeda.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

## 📱 Screenshots

| Quiz Screen | Category Selection | Settings |
| :---: | :---: | :---: |
| ![Quiz](https://via.placeholder.com/200x400?text=Quiz+Screen) | ![Categories](https://via.placeholder.com/200x400?text=Categories) | ![Settings](https://via.placeholder.com/200x400?text=Settings) |

## 🤝 Contributing

Contributions are welcome! If you have suggestions for new features, categories, or find any bugs, please open an issue or submit a pull request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
