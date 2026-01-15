# MyBicocca

**One App to Rule Them All.**

As a student at the University of Milano-Bicocca, you know the struggle. To manage your university life, you constantly juggle between multiple disconnected platforms:
*   **Esse3 (Segreterie Online):** Where you check your grades, book exams, manage your study plan, and pay fees.
*   **E-Learning (Moodle):** Where you download lecture slides, submit assignments, and take quizzes.
*   **The Official "Bicocca" App:** Where you might check your class schedule or find a building on the map.

**MyBicocca** solves this fragmentation. It is an unofficial, open-source "Super App" that unifies all these services into a single, modern, and beautiful interface. No more logging into three different websites to get through your day.

## 🚀 The Ecosystem: Unified

MyBicocca deeply integrates with the university's existing infrastructure to bring you a seamless experience:

### 1. Esse3 Integration (Career & Administration)
*Connects directly to `s3w.si.unimib.it`*
Instead of navigating the clunky web interface of Segreterie Online, MyBicocca provides a native mobile experience for:
*   **Digital Transcript (Libretto):** View your passed exams, grades, and credits at a glance.
*   **Exam Booking:** Search for exam sessions (Appelli) and register for them instantly.
*   **Study Plan:** Review your "Piano di Studi" to see what exams you still need to take.
*   **Stats & Analytics:** Visualize your academic progress with beautiful charts (Average Grade, ECTS progress, Grade predictions).
*   **Administrative Tasks:** Handle questionnaires and admissions directly in the app.

### 2. E-Learning / Moodle Integration (Didactics)
*Connects directly to `elearning.unimib.it`*
Accessing course materials on mobile has never been easier. MyBicocca supports the full range of Moodle features:
*   **Course Content:** Download files, folders, and view pages for all your enrolled courses.
*   **Assignments & Quizzes:** Check deadlines and even submit assignments or take quizzes from your phone.
*   **Interactive Modules:** Full support for Forums, BigBlueButton, H5P activities, and more.
*   **Grades:** View your partial grades for coursework directly.

### 3. Campus Services
*Connects to the official app's backend*
*   **Smart Calendar:** Your daily class schedule, automatically synced and organized.
*   **Campus Maps:** Find buildings, classrooms, and study spots easily.
*   **Messages:** Receive official communications from the university.
*   **Badges:** Access your virtual student badge for campus entry.

---

## ✨ Key Features
*   **Unified Login:** Log in once with your university credentials and access everything.
*   **Modern UI:** Built with Google's latest **Material 3** design system for a fluid and accessible experience.
*   **Dark Mode:** Fully supported system-wide dark theme.
*   **Privacy Focused:** Your data stays on your device. MyBicocca communicates directly with university servers; no intermediate servers collect your credentials.

## 🛠 For Developers

MyBicocca is an open-source project built with the latest Android technology stack. We welcome contributions!

**Tech Stack:**
*   **Kotlin** & **Jetpack Compose** (UI)
*   **Hilt** (Dependency Injection)
*   **Retrofit** & **OkHttp** (Networking)
*   **Room** (Local Database)
*   **Vico** (Charting)

### Setup & Build
1.  Clone the repo: `git clone https://github.com/yourusername/MyBicocca.git`
2.  Open in **Android Studio Ladybug** (or newer).
3.  Sync Gradle and run on an emulator/device (min SDK 25).

## 📄 License
MyBicocca is an unofficial project and is not endorsed by the University of Milano-Bicocca.
Licensed under the terms found in the [LICENSE](LICENSE) file.