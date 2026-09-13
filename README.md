# SSKN — Saleem Sir Ki NCERT (NEET Biology)

> **NCERT First. AI Second. Practice Third.**  
> Read each page like an exam question. Every single line can become a 4-mark question in NEET.

SSKN is a modern, high-performance Android application built with Jetpack Compose and Material Design 3, tailored specifically for NEET Biology aspirants. It combines a line-by-line NCERT digital reader with an exam-grounded AI engine powered by Gemini, verified past-year questions (PYQ), and intelligent mistake diagnosis.

---

## 🌟 Key Features

1. **NCERT Digital Reader & Page Navigator**
   - Clean, high-contrast reading canvas for Class 11 and 12 NCERT Biology.
   - Interactive line highlights with contextual NEET importance tags (Critical, High-Yield, Important).
   - Instant page-to-page navigation and gesture support.

2. **Persistent Bottom Navigation**
   - Seamless top-level navigation across **Home**, **NCERT**, **PYQs**, **Search**, and **More**.
   - Persistent `Scaffold` architecture for instant, flicker-free switching.
   - Immersive auto-hiding during reader and practice quiz sessions.

3. **NCERT-Aware Gemini AI Engine**
   - **Dual AI Modes**: Toggle between **NCERT STRICT** (zero hallucination, purely syllabus-grounded) and **NEET EXAM FOCUS** (pattern trends, trick traps).
   - **Diagram Analysis**: Grounded anatomical and structural breakdowns, process sequences, and potential NEET questions.
   - **Concept Comparison**: Deep-dive comparisons for easily confused terms (e.g., C3 vs C4, Mitosis vs Meiosis).
   - **30-Second Summary**: Quick, high-impact bulleted recaps of any NCERT page.

4. **PYQ Vault & Targeted Practice**
   - Verified NEET Biology past-year questions mapped directly to specific NCERT pages.
   - Timed MCQ drills with instant answer evaluation and NCERT page citations.

5. **AI Mistake Notebook**
   - Intelligent root-cause analysis for incorrect answers (Knowledge Gap, Misreading, Confusion).
   - Targeted remediation drills to prevent repeat errors in NEET.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Concurrency**: Kotlin Coroutines & StateFlow
- **AI Integration**: Google Gemini API via Secure Service Layer
- **Local Persistence**: Room Database
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Android Studio Ladybug / Hedgehog or newer
- **JDK**: Version 17 (recommended: Eclipse Temurin or Azul Zulu)
- **Android SDK**: Min SDK 26 (Android 8.0), Target SDK 35 (Android 15)

### Local Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/<your-username>/<your-repo-name>.git
   cd <your-repo-name>
   ```

2. **Configure API Keys**:
   Copy `.env.example` to `.env` in the project root:
   ```bash
   cp .env.example .env
   ```
   Add your Gemini API Key in `.env`:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Build the Application**:
   On Linux/macOS:
   ```bash
   ./gradlew assembleDebug
   ```
   On Windows:
   ```bat
   gradlew.bat assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📦 Deployment & CI/CD

- **GitHub Actions**: Automated CI is configured in `.github/workflows/android.yml`. Every push or pull request to `main` builds the application and produces a downloadable debug APK artifact.
- **Generate Release APK / AAB in Android Studio**:
  - In Android Studio, go to **Build** > **Generate Signed Bundle / APK**.
  - Choose **Android App Bundle** (for Google Play) or **APK** (for direct testing/distribution).

---

## 🔒 Security & Privacy

- Sensitive API credentials and keys are never hardcoded; they are managed through build configuration secrets and injected via `BuildConfig`.
- Comply with Google Play Developer Program policies and least-privilege permission practices.
