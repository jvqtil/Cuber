<div align="center">

<img src="assets/cuber-logo.svg" width="100" alt="">
<h1>
  Cuber
</h1>

Cubing app for Android. Timer, scrambles, stats

[**Features**](#features) · [**Contributing**](#contributing)

<a href="https://github.com/jvqtil/Cuber/releases/latest">
  <img src="https://img.shields.io/badge/Download%20APK-8B5CF6?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
</a>

</div>

## Usage
Tap the timer to start/stop. **Long press** to reset.

Swipe **up** to view your statistics and solve history. Swipe **down** to return to the timer.

## Features

- Fast and simple speedcubing timer
- WCA-style 3×3 scrambles
- Visual scramble preview
- Solves history
- Statistics
- `+2` and `DNF` penalties
- Comments for individual solves
- Local-only storage
- Material UI
- Dynamic colors on Android 12+
- Predictive back gesture support (Android 16+)

## Contributing

### Building
Clone the repository and open it in Android Studio.

Build it with:
  ```bash
  ./gradlew assembleRelease
  ```

Found a bug, have an idea, or want to improve something?
Issues and pull requests are welcome.

## Thanks to

* [tnoodle-lib](https://github.com/thewca/tnoodle-lib) — Scramble generation library.
* [CubeTime](https://github.com/CubeLabsNZ/CubeTime) — Heavily inspired by.