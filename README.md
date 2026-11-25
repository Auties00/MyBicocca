<div align="center">

<img src="common/screenshots/MyBicoccaSplash.png" width="400" alt="MyBicocca logo" />

# MyBicocca

### Your entire university. One app.

**MyBicocca** folds every digital service of the University of Milano‑Bicocca into a single,
fast, offline‑first app — native on **Android** (Jetpack Compose & Material 3 Expressive) and on
the **web** (SvelteKit & Carta), with an **iOS** app on the way.

<br />

[![Android](https://img.shields.io/badge/Android-7.1%2B-9C0C35?style=for-the-badge&logo=android&logoColor=white)](#-installation)
[![Web](https://img.shields.io/badge/Web-PWA-9C0C35?style=for-the-badge&logo=svelte&logoColor=white)](#-installation)
![iOS](https://img.shields.io/badge/iOS-Coming%20soon-9C0C35?style=for-the-badge&logo=apple&logoColor=white)
[![License](https://img.shields.io/badge/License-AGPL--3.0-9C0C35?style=for-the-badge)](LICENSE)

</div>

---

## ⚖️ Disclaimer

MyBicocca is an **unofficial** application and is **not affiliated with the University of
Milano‑Bicocca**. It consumes publicly available APIs and web services of the platforms
it integrates. Please use it responsibly and in accordance with university policies.

---

## ✨ What is MyBicocca?

Bicocca students juggle a handful of disconnected web portals to get through the day:
one site to book an exam, another to read a lecture's slides, a third to check the
timetable, a fourth to reserve a seat in the library. **MyBicocca makes all of that a
single tab swipe away**, with a coherent design, a unified search, and data that's
cached on‑device so it's there even when the network isn't.
It is an **unofficial, student‑built** app. It speaks directly to the same services the
university already exposes, and consolidates them behind one coherent experience across
Android and the web.

---

## 🔐 Privacy & security

**Your data stays yours.** MyBicocca talks to the university on your behalf and keeps the
results on your device, nowhere else.

- 🔑 &nbsp;**Credentials encrypted on‑device**, never sent anywhere but the platforms they belong to
- 🔒 &nbsp;**HTTPS everywhere**, every request and every response
- 🙅 &nbsp;**Your university data is never shared**: no ads, nothing sold
- 🌐 &nbsp;**Stateless web relay**: on the web app, cross‑origin university requests pass through a stateless relay that returns cookies to your browser and stores nothing; the web app sends no Firebase telemetry
- 🩺 &nbsp;**Anonymous crash reports (Android)** go to Firebase Crashlytics to help fix bugs; turn them off in Settings › Security
- 📈 &nbsp;**Anonymous usage and speed statistics (Android)** go to Firebase Analytics and Performance Monitoring; request timings carry only the address's host and path, never your tokens or what you asked for

---

## 📦 Installation

- **Android**: Install from Google Play or download the latest signed APK from [**GitHub Releases**](https://github.com/Auties00/MyBicocca/releases/latest).
- **Web**: Open [**web.mybicocca.it**](https://web.mybicocca.it) on desktop or mobile (installable as a PWA), or run it locally from [`web/web-app`](web/web-app).
- **iOS**: Coming soon on the App Store — in the meantime, the web app runs on iPhone and iPad and can be added to the Home Screen.

---

## 📅 Calendar

Your whole week at a glance. Lessons from EasyStaff, exams you've booked on Esse3,
Moodle assignment deadlines, Esse3 appointments and library reservations all
land on the same timeline, colour‑coded by source and pinch‑zoomable.

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Calendar1.png" width="150" alt="Calendar1 (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar1.png" width="150" alt="Calendar1 (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Calendar1.png" width="225" alt="Calendar1 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Calendar1.png" width="225" alt="Calendar1 (Web desktop, dark theme)" /></td>
<td>

#### Day

A pinch‑zoomable hourly timeline of everything on your day, each block coloured by where it comes from.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_CalendarEvent.png" width="150" alt="CalendarEvent (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_CalendarEvent.png" width="150" alt="CalendarEvent (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_CalendarEvent.png" width="225" alt="CalendarEvent (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_CalendarEvent.png" width="225" alt="CalendarEvent (Web desktop, dark theme)" /></td>
<td>

#### Event details

Tap any block for its time, room and teacher, then get directions on the campus map or jump straight to the course.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Calendar2.png" width="150" alt="Calendar2 (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar2.png" width="150" alt="Calendar2 (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Calendar2.png" width="225" alt="Calendar2 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Calendar2.png" width="225" alt="Calendar2 (Web desktop, dark theme)" /></td>
<td>

#### Week

Seven days at a glance. The zoom level is shared with the Day view, so the density you choose follows you across both.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Calendar3.png" width="150" alt="Calendar3 (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar3.png" width="150" alt="Calendar3 (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Calendar3.png" width="225" alt="Calendar3 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Calendar3.png" width="225" alt="Calendar3 (Web desktop, dark theme)" /></td>
<td>

#### Month

A heatmap tints each date by how busy it is, with a draggable agenda sheet for the day you tap. Pull down anywhere and every source re‑syncs without losing your place.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Day</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Calendar1.png" width="145" alt="Calendar1 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Calendar1.png" width="145" alt="Calendar1 (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Calendar1.png" width="125" alt="Calendar1 (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Calendar1.png" width="125" alt="Calendar1 (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Event details</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_CalendarEvent.png" width="145" alt="CalendarEvent (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_CalendarEvent.png" width="145" alt="CalendarEvent (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_CalendarEvent.png" width="125" alt="CalendarEvent (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_CalendarEvent.png" width="125" alt="CalendarEvent (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Week</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Calendar2.png" width="145" alt="Calendar2 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Calendar2.png" width="145" alt="Calendar2 (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Calendar2.png" width="125" alt="Calendar2 (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Calendar2.png" width="125" alt="Calendar2 (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Month</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Calendar3.png" width="145" alt="Calendar3 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Calendar3.png" width="145" alt="Calendar3 (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Calendar3.png" width="125" alt="Calendar3 (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Calendar3.png" width="125" alt="Calendar3 (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

---
## 🎓 E‑learning

A complete Moodle client, not a webview. Browse your courses, dive into
materials, hand in assignments, sit quizzes, follow forum threads and watch lecture
recordings, all inside the app.

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Elearning.png" width="150" alt="Elearning (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Elearning.png" width="150" alt="Elearning (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Elearning.png" width="225" alt="Elearning (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Elearning.png" width="225" alt="Elearning (Web desktop, dark theme)" /></td>
<td>

#### Your courses

Every enrolled course, grouped by academic period and filterable by year or favourites.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningAddCourse.png" width="150" alt="ElearningAddCourse (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningAddCourse.png" width="150" alt="ElearningAddCourse (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningAddCourse.png" width="225" alt="ElearningAddCourse (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningAddCourse.png" width="225" alt="ElearningAddCourse (Web desktop, dark theme)" /></td>
<td>

#### Course catalog

Browse every teaching area on the university's Moodle, or search by course name or code, and enrol on the spot. The catalog ships with the app, so browsing it needs no network.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourse.png" width="150" alt="ElearningCourse (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourse.png" width="150" alt="ElearningCourse (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourse.png" width="225" alt="ElearningCourse (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourse.png" width="225" alt="ElearningCourse (Web desktop, dark theme)" /></td>
<td>

#### Course detail

A collapsing header opens onto tabs for info, content, quizzes, assignments and forums, so the whole course lives behind one screen.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseContent.png" width="150" alt="ElearningCourseContent (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseContent.png" width="150" alt="ElearningCourseContent (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseContent.png" width="225" alt="ElearningCourseContent (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseContent.png" width="225" alt="ElearningCourseContent (Web desktop, dark theme)" /></td>
<td>

#### Materials

Sections and folders expand inline, with every file one tap away.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningFile.png" width="150" alt="ElearningFile (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningFile.png" width="150" alt="ElearningFile (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningFile.png" width="225" alt="ElearningFile (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningFile.png" width="225" alt="ElearningFile (Web desktop, dark theme)" /></td>
<td>

#### File viewer

PDFs, images and text open in an in‑app viewer, page by page, ready to share or download. Office files hand off to the app of your choice.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseQuiz.png" width="150" alt="ElearningCourseQuiz (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseQuiz.png" width="150" alt="ElearningCourseQuiz (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseQuiz.png" width="225" alt="ElearningCourseQuiz (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseQuiz.png" width="225" alt="ElearningCourseQuiz (Web desktop, dark theme)" /></td>
<td>

#### Quizzes

Review past attempts, resume one in progress, and read your results and feedback question by question.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseForum.png" width="150" alt="ElearningCourseForum (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseForum.png" width="150" alt="ElearningCourseForum (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseForum.png" width="225" alt="ElearningCourseForum (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseForum.png" width="225" alt="ElearningCourseForum (Web desktop, dark theme)" /></td>
<td>

#### Forums

Read announcements and discussions, reply, attach files and manage your subscriptions, straight from the app.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseAssignment.png" width="150" alt="ElearningCourseAssignment (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseAssignment.png" width="150" alt="ElearningCourseAssignment (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseAssignment.png" width="225" alt="ElearningCourseAssignment (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseAssignment.png" width="225" alt="ElearningCourseAssignment (Web desktop, dark theme)" /></td>
<td>

#### Assignments

Read the brief, upload your files and submit, then track submission status, teacher feedback and your grade.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Your courses</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Elearning.png" width="145" alt="Elearning (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Elearning.png" width="145" alt="Elearning (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Elearning.png" width="125" alt="Elearning (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Elearning.png" width="125" alt="Elearning (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Course catalog</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningAddCourse.png" width="145" alt="ElearningAddCourse (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningAddCourse.png" width="145" alt="ElearningAddCourse (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningAddCourse.png" width="125" alt="ElearningAddCourse (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningAddCourse.png" width="125" alt="ElearningAddCourse (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Course detail</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourse.png" width="145" alt="ElearningCourse (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourse.png" width="145" alt="ElearningCourse (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourse.png" width="125" alt="ElearningCourse (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourse.png" width="125" alt="ElearningCourse (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Materials</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseContent.png" width="145" alt="ElearningCourseContent (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseContent.png" width="145" alt="ElearningCourseContent (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseContent.png" width="125" alt="ElearningCourseContent (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseContent.png" width="125" alt="ElearningCourseContent (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>File viewer</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningFile.png" width="145" alt="ElearningFile (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningFile.png" width="145" alt="ElearningFile (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningFile.png" width="125" alt="ElearningFile (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningFile.png" width="125" alt="ElearningFile (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Quizzes</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseQuiz.png" width="145" alt="ElearningCourseQuiz (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseQuiz.png" width="145" alt="ElearningCourseQuiz (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseQuiz.png" width="125" alt="ElearningCourseQuiz (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseQuiz.png" width="125" alt="ElearningCourseQuiz (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Forums</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseForum.png" width="145" alt="ElearningCourseForum (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseForum.png" width="145" alt="ElearningCourseForum (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseForum.png" width="125" alt="ElearningCourseForum (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseForum.png" width="125" alt="ElearningCourseForum (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Assignments</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseAssignment.png" width="145" alt="ElearningCourseAssignment (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseAssignment.png" width="145" alt="ElearningCourseAssignment (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseAssignment.png" width="125" alt="ElearningCourseAssignment (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseAssignment.png" width="125" alt="ElearningCourseAssignment (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

Lecture recordings stream in‑app (Kaltura / HLS), alongside grades and module deadlines.

---
## 🗺️ Maps

The whole campus, offline. Buildings and rooms render from a bundled Protomaps vector
tileset on a MapLibre engine (**no Google Maps key, no network needed**) and the map
recolours itself to match your chosen theme.

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Map.png" width="150" alt="Map (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Map.png" width="150" alt="Map (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Map.png" width="225" alt="Map (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Map.png" width="225" alt="Map (Web desktop, dark theme)" /></td>
<td>

#### Offline campus map

Tappable pins for every building, rendered from a bundled vector tileset. No Google Maps key, no network needed.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_MapBuildings.png" width="150" alt="MapBuildings (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_MapBuildings.png" width="150" alt="MapBuildings (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_MapBuildings.png" width="225" alt="MapBuildings (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_MapBuildings.png" width="225" alt="MapBuildings (Web desktop, dark theme)" /></td>
<td>

#### Building directory

Search the whole campus and get one‑tap directions to anywhere.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_MapBuildingU1.png" width="150" alt="MapBuildingU1 (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_MapBuildingU1.png" width="150" alt="MapBuildingU1 (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_MapBuildingU1.png" width="225" alt="MapBuildingU1 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_MapBuildingU1.png" width="225" alt="MapBuildingU1 (Web desktop, dark theme)" /></td>
<td>

#### Inside a building

Drill into rooms and their details, with live EasyBadge occupancy schedules.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Offline campus map</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Map.png" width="145" alt="Map (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Map.png" width="145" alt="Map (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Map.png" width="125" alt="Map (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Map.png" width="125" alt="Map (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Building directory</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_MapBuildings.png" width="145" alt="MapBuildings (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_MapBuildings.png" width="145" alt="MapBuildings (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_MapBuildings.png" width="125" alt="MapBuildings (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_MapBuildings.png" width="125" alt="MapBuildings (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Inside a building</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_MapBuildingU1.png" width="145" alt="MapBuildingU1 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_MapBuildingU1.png" width="145" alt="MapBuildingU1 (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_MapBuildingU1.png" width="125" alt="MapBuildingU1 (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_MapBuildingU1.png" width="125" alt="MapBuildingU1 (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

---
## 🗂️ Services

The registry tab fans out into the full Esse3 toolbox: every student service you'd
otherwise hunt for online, grouped into Teaching, Bookings, Documents and Payments.

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Services.png" width="150" alt="Services (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Services.png" width="150" alt="Services (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Services.png" width="225" alt="Services (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Services.png" width="225" alt="Services (Web desktop, dark theme)" /></td>
<td>

#### The services hub

Home to everything administrative, in four groups, topped by a live deadlines banner.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ServicesScadenze.png" width="150" alt="ServicesScadenze (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ServicesScadenze.png" width="150" alt="ServicesScadenze (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ServicesScadenze.png" width="225" alt="ServicesScadenze (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ServicesScadenze.png" width="225" alt="ServicesScadenze (Web desktop, dark theme)" /></td>
<td>

#### Deadlines

The banner counts what's urgent and expands into a full chronological timeline of tuition, enrolment and exam dates.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>The services hub</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Services.png" width="145" alt="Services (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Services.png" width="145" alt="Services (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Services.png" width="125" alt="Services (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Services.png" width="125" alt="Services (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Deadlines</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ServicesScadenze.png" width="145" alt="ServicesScadenze (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ServicesScadenze.png" width="145" alt="ServicesScadenze (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ServicesScadenze.png" width="125" alt="ServicesScadenze (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ServicesScadenze.png" width="125" alt="ServicesScadenze (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

### Teaching

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Career.png" width="150" alt="Career (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Career.png" width="150" alt="Career (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Career.png" width="225" alt="Career (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Career.png" width="225" alt="Career (Web desktop, dark theme)" /></td>
<td>

#### Study plan

Your programme year by year, with the activities and credits of each, ready to print.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Attendance.png" width="150" alt="Attendance (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Attendance.png" width="150" alt="Attendance (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Attendance.png" width="225" alt="Attendance (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Attendance.png" width="225" alt="Attendance (Web desktop, dark theme)" /></td>
<td>

#### Attendance

How much of each course you have attended so far, and whether you are on track.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_AttendanceOpt.png" width="150" alt="AttendanceOpt (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_AttendanceOpt.png" width="150" alt="AttendanceOpt (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_AttendanceOpt.png" width="225" alt="AttendanceOpt (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_AttendanceOpt.png" width="225" alt="AttendanceOpt (Web desktop, dark theme)" /></td>
<td>

#### Check in

Record your presence at a lecture, or at another activity, in a couple of taps. No paper sheet to chase.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ExamResults.png" width="150" alt="ExamResults (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ExamResults.png" width="150" alt="ExamResults (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ExamResults.png" width="225" alt="ExamResults (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ExamResults.png" width="225" alt="ExamResults (Web desktop, dark theme)" /></td>
<td>

#### Results

Published grades appear the moment they land, and you can accept or reject each one right from the list.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Questionnaires.png" width="150" alt="Questionnaires (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Questionnaires.png" width="150" alt="Questionnaires (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Questionnaires.png" width="225" alt="Questionnaires (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Questionnaires.png" width="225" alt="Questionnaires (Web desktop, dark theme)" /></td>
<td>

#### Questionnaires

The teaching evaluations still to fill in, and the ones you have already sent.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Study plan</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Career.png" width="145" alt="Career (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Career.png" width="145" alt="Career (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Career.png" width="125" alt="Career (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Career.png" width="125" alt="Career (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Attendance</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Attendance.png" width="145" alt="Attendance (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Attendance.png" width="145" alt="Attendance (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Attendance.png" width="125" alt="Attendance (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Attendance.png" width="125" alt="Attendance (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Check in</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_AttendanceOpt.png" width="145" alt="AttendanceOpt (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_AttendanceOpt.png" width="145" alt="AttendanceOpt (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_AttendanceOpt.png" width="125" alt="AttendanceOpt (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_AttendanceOpt.png" width="125" alt="AttendanceOpt (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Results</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ExamResults.png" width="145" alt="ExamResults (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ExamResults.png" width="145" alt="ExamResults (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ExamResults.png" width="125" alt="ExamResults (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ExamResults.png" width="125" alt="ExamResults (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Questionnaires</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Questionnaires.png" width="145" alt="Questionnaires (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Questionnaires.png" width="145" alt="Questionnaires (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Questionnaires.png" width="125" alt="Questionnaires (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Questionnaires.png" width="125" alt="Questionnaires (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

### Bookings

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_BookableExams.png" width="150" alt="BookableExams (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_BookableExams.png" width="150" alt="BookableExams (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_BookableExams.png" width="225" alt="BookableExams (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_BookableExams.png" width="225" alt="BookableExams (Web desktop, dark theme)" /></td>
<td>

#### Book an exam

Every open exam call (appello) of your courses, grouped by course, bookable in a single tap.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_BookedExams.png" width="150" alt="BookedExams (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_BookedExams.png" width="150" alt="BookedExams (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_BookedExams.png" width="225" alt="BookedExams (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_BookedExams.png" width="225" alt="BookedExams (Web desktop, dark theme)" /></td>
<td>

#### Your exams

The exams you've booked, with their date, time and room, and a confirmation you can show on the day.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_BookAppointment.png" width="150" alt="BookAppointment (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_BookAppointment.png" width="150" alt="BookAppointment (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_BookAppointment.png" width="225" alt="BookAppointment (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_BookAppointment.png" width="225" alt="BookAppointment (Web desktop, dark theme)" /></td>
<td>

#### Offices

Book an appointment at the student offices (segreteria) and keep track of the ones coming up.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Library.png" width="150" alt="Library (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Library.png" width="150" alt="Library (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Library.png" width="225" alt="Library (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Library.png" width="225" alt="Library (Web desktop, dark theme)" /></td>
<td>

#### Library seats

Reserve a study seat in the campus libraries, powered by Affluences, and manage the booking from the calendar.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Book an exam</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_BookableExams.png" width="145" alt="BookableExams (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_BookableExams.png" width="145" alt="BookableExams (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_BookableExams.png" width="125" alt="BookableExams (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_BookableExams.png" width="125" alt="BookableExams (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Your exams</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_BookedExams.png" width="145" alt="BookedExams (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_BookedExams.png" width="145" alt="BookedExams (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_BookedExams.png" width="125" alt="BookedExams (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_BookedExams.png" width="125" alt="BookedExams (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Offices</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_BookAppointment.png" width="145" alt="BookAppointment (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_BookAppointment.png" width="145" alt="BookAppointment (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_BookAppointment.png" width="125" alt="BookAppointment (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_BookAppointment.png" width="125" alt="BookAppointment (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Library seats</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Library.png" width="145" alt="Library (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Library.png" width="145" alt="Library (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Library.png" width="125" alt="Library (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Library.png" width="125" alt="Library (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

### Documents

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Enrollments.png" width="150" alt="Enrollments (Android, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_EnrollmentDetail.png" width="150" alt="EnrollmentDetail (Android, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Enrollments.png" width="150" alt="Enrollments (Android, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_EnrollmentDetail.png" width="150" alt="EnrollmentDetail (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Enrollments.png" width="225" alt="Enrollments (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_EnrollmentDetail.png" width="225" alt="EnrollmentDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Enrollments.png" width="225" alt="Enrollments (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_EnrollmentDetail.png" width="225" alt="EnrollmentDetail (Web desktop, dark theme)" /></td>
<td>

#### Enrolments

Each academic year you enrolled in, and for each its enrolment type, fee exemptions and bands, and degree programme.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Titles.png" width="150" alt="Titles (Android, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_TitleDetail.png" width="150" alt="TitleDetail (Android, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Titles.png" width="150" alt="Titles (Android, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_TitleDetail.png" width="150" alt="TitleDetail (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Titles.png" width="225" alt="Titles (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_TitleDetail.png" width="225" alt="TitleDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Titles.png" width="225" alt="Titles (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_TitleDetail.png" width="225" alt="TitleDetail (Web desktop, dark theme)" /></td>
<td>

#### Degrees

The qualifications on your record, such as your high school diploma, with grade, year and institution.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Documents.png" width="150" alt="Documents (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Documents.png" width="150" alt="Documents (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Documents.png" width="225" alt="Documents (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Documents.png" width="225" alt="Documents (Web desktop, dark theme)" /></td>
<td>

#### Certificates

Digitally signed self‑certifications, such as enrolment with exams or the tuition you paid each year, downloaded as PDFs.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Enrolments</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Enrollments.png" width="145" alt="Enrollments (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Enrollments.png" width="145" alt="Enrollments (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Enrollments.png" width="125" alt="Enrollments (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Enrollments.png" width="125" alt="Enrollments (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Enrolment detail</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_EnrollmentDetail.png" width="145" alt="EnrollmentDetail (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_EnrollmentDetail.png" width="145" alt="EnrollmentDetail (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_EnrollmentDetail.png" width="125" alt="EnrollmentDetail (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_EnrollmentDetail.png" width="125" alt="EnrollmentDetail (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Degrees</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Titles.png" width="145" alt="Titles (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Titles.png" width="145" alt="Titles (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Titles.png" width="125" alt="Titles (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Titles.png" width="125" alt="Titles (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Degree detail</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_TitleDetail.png" width="145" alt="TitleDetail (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_TitleDetail.png" width="145" alt="TitleDetail (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_TitleDetail.png" width="125" alt="TitleDetail (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_TitleDetail.png" width="125" alt="TitleDetail (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Certificates</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Documents.png" width="145" alt="Documents (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Documents.png" width="145" alt="Documents (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Documents.png" width="125" alt="Documents (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Documents.png" width="125" alt="Documents (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

### Payments

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Fees.png" width="150" alt="Fees (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Fees.png" width="150" alt="Fees (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Fees.png" width="225" alt="Fees (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Fees.png" width="225" alt="Fees (Web desktop, dark theme)" /></td>
<td>

#### Fees

The tuition instalments still to pay and the ones already paid, with amounts and deadlines.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Isee.png" width="150" alt="Isee (Android, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_IseeDetail.png" width="150" alt="IseeDetail (Android, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Isee.png" width="150" alt="Isee (Android, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_IseeDetail.png" width="150" alt="IseeDetail (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Isee.png" width="225" alt="Isee (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_IseeDetail.png" width="225" alt="IseeDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Isee.png" width="225" alt="Isee (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_IseeDetail.png" width="225" alt="IseeDetail (Web desktop, dark theme)" /></td>
<td>

#### ISEE

Your ISEE indicator for each academic year, against the threshold for benefits and the exemption it earns you.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Refunds.png" width="150" alt="Refunds (Android, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_RefundDetail.png" width="150" alt="RefundDetail (Android, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Refunds.png" width="150" alt="Refunds (Android, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_RefundDetail.png" width="150" alt="RefundDetail (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Refunds.png" width="225" alt="Refunds (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_RefundDetail.png" width="225" alt="RefundDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Refunds.png" width="225" alt="Refunds (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_RefundDetail.png" width="225" alt="RefundDetail (Web desktop, dark theme)" /></td>
<td>

#### Refunds

What the university has paid back, and for each refund its reason, mandate and credit date.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Fees</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Fees.png" width="145" alt="Fees (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Fees.png" width="145" alt="Fees (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Fees.png" width="125" alt="Fees (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Fees.png" width="125" alt="Fees (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>ISEE</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Isee.png" width="145" alt="Isee (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Isee.png" width="145" alt="Isee (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Isee.png" width="125" alt="Isee (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Isee.png" width="125" alt="Isee (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>ISEE detail</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_IseeDetail.png" width="145" alt="IseeDetail (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_IseeDetail.png" width="145" alt="IseeDetail (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_IseeDetail.png" width="125" alt="IseeDetail (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_IseeDetail.png" width="125" alt="IseeDetail (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Refunds</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Refunds.png" width="145" alt="Refunds (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Refunds.png" width="145" alt="Refunds (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Refunds.png" width="125" alt="Refunds (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Refunds.png" width="125" alt="Refunds (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Refund detail</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_RefundDetail.png" width="145" alt="RefundDetail (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_RefundDetail.png" width="145" alt="RefundDetail (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_RefundDetail.png" width="125" alt="RefundDetail (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_RefundDetail.png" width="125" alt="RefundDetail (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

And the rest of the registry too: MAV and PagoPA payments, study‑plan editing and more.

---
## 👤 Profile & career

A live dashboard of your academic standing: a flippable student card, your headline
stats, a grade‑trend chart, and a simulator that answers _"what grade do I need?"_.

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Profile.png" width="150" alt="Profile (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Profile.png" width="150" alt="Profile (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Profile.png" width="225" alt="Profile (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Profile.png" width="225" alt="Profile (Web desktop, dark theme)" /></td>
<td>

#### Student card & stats

A flippable student card up top, then your headline numbers: arithmetic and weighted average, exams passed and credits earned.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_ExamsTaken.png" width="150" alt="ExamsTaken (Android, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_Credits.png" width="150" alt="Credits (Android, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_ExamsTaken.png" width="150" alt="ExamsTaken (Android, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Credits.png" width="150" alt="Credits (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ExamsTaken.png" width="225" alt="ExamsTaken (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Credits.png" width="225" alt="Credits (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ExamsTaken.png" width="225" alt="ExamsTaken (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Credits.png" width="225" alt="Credits (Web desktop, dark theme)" /></td>
<td>

#### Exams and credits

Every exam you passed, year by year, with its grade, and the credits each one earned you towards your degree.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Average.png" width="150" alt="Average (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Average.png" width="150" alt="Average (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Average.png" width="225" alt="Average (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Average.png" width="225" alt="Average (Web desktop, dark theme)" /></td>
<td>

#### Grade simulator

A "what‑if" calculator that shows how the next exam moves your average, in both arithmetic and weighted modes.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_AccountSwitcher.png" width="150" alt="AccountSwitcher (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_AccountSwitcher.png" width="150" alt="AccountSwitcher (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_AccountSwitcher.png" width="225" alt="AccountSwitcher (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_AccountSwitcher.png" width="225" alt="AccountSwitcher (Web desktop, dark theme)" /></td>
<td>

#### Accounts

Sign in with more than one account, and switch between them and between your careers.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Student card &amp; stats</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Profile.png" width="145" alt="Profile (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Profile.png" width="145" alt="Profile (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Profile.png" width="125" alt="Profile (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Profile.png" width="125" alt="Profile (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Exams taken</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ExamsTaken.png" width="145" alt="ExamsTaken (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ExamsTaken.png" width="145" alt="ExamsTaken (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ExamsTaken.png" width="125" alt="ExamsTaken (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ExamsTaken.png" width="125" alt="ExamsTaken (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Credits</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Credits.png" width="145" alt="Credits (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Credits.png" width="145" alt="Credits (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Credits.png" width="125" alt="Credits (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Credits.png" width="125" alt="Credits (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Grade simulator</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Average.png" width="145" alt="Average (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Average.png" width="145" alt="Average (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Average.png" width="125" alt="Average (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Average.png" width="125" alt="Average (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Accounts</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_AccountSwitcher.png" width="145" alt="AccountSwitcher (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_AccountSwitcher.png" width="145" alt="AccountSwitcher (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_AccountSwitcher.png" width="125" alt="AccountSwitcher (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_AccountSwitcher.png" width="125" alt="AccountSwitcher (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

---
## ⚙️ Settings

Make the app yours: its look, its language, how it opens files and how it protects your data.

<table>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_Settings.png" width="150" alt="Settings (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Settings.png" width="150" alt="Settings (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Settings.png" width="225" alt="Settings (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Settings.png" width="225" alt="Settings (Web desktop, dark theme)" /></td>
<td>

#### Preferences

Appearance, language, security, file opening and haptics, plus everything about the app.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_SettingsAppearance.png" width="150" alt="SettingsAppearance (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsAppearance.png" width="150" alt="SettingsAppearance (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsAppearance.png" width="225" alt="SettingsAppearance (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsAppearance.png" width="225" alt="SettingsAppearance (Web desktop, dark theme)" /></td>
<td>

#### Appearance

Custom palettes (MyBicocca, Material You on Android, Oceano and Bosco), each in light, dark or following the system.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_SettingsLanguage.png" width="150" alt="SettingsLanguage (Android, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_SettingsHaptic.png" width="150" alt="SettingsHaptic (Android, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsLanguage.png" width="150" alt="SettingsLanguage (Android, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsHaptic.png" width="150" alt="SettingsHaptic (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsLanguage.png" width="225" alt="SettingsLanguage (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsLanguage.png" width="225" alt="SettingsLanguage (Web desktop, dark theme)" /></td>
<td>

#### Language and haptics

Use the app in Italian or English, or follow the system, and choose whether it vibrates as you tap (a dedicated screen on Android, and a toggle in Settings on touch web devices).

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_SettingsSecurity.png" width="150" alt="SettingsSecurity (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsSecurity.png" width="150" alt="SettingsSecurity (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsSecurity.png" width="225" alt="SettingsSecurity (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsSecurity.png" width="225" alt="SettingsSecurity (Web desktop, dark theme)" /></td>
<td>

#### Security

Lock the app behind your fingerprint or device password (or a PIN/passkey on the web), and choose whether anonymous crash reports are sent on Android.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_SettingsFileAssociation.png" width="150" alt="SettingsFileAssociation (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsFileAssociation.png" width="150" alt="SettingsFileAssociation (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsFileAssociation.png" width="225" alt="SettingsFileAssociation (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsFileAssociation.png" width="225" alt="SettingsFileAssociation (Web desktop, dark theme)" /></td>
<td>

#### File opening

Pick how each kind of file opens, from PDFs and images to videos, audio and archives, or be asked every time.

</td>
</tr>
<tr>
<td><strong>Android</strong><br /><img src="android/tools/screenshots/en/light/Screenshot_SettingsAppInfo.png" width="150" alt="SettingsAppInfo (Android, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsAppInfo.png" width="150" alt="SettingsAppInfo (Android, dark theme)" /><br /><strong>Web</strong><br /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsAppInfo.png" width="225" alt="SettingsAppInfo (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsAppInfo.png" width="225" alt="SettingsAppInfo (Web desktop, dark theme)" /></td>
<td>

#### About

The app's version, what's new in it, its updates, including beta releases, and the people behind it.

</td>
</tr>
</table>

<details>
<summary>Web tablet &amp; mobile screenshots</summary>

<table>
<tr>
<th>Screen</th>
<th>Tablet</th>
<th>Mobile</th>
</tr>
<tr>
<td><strong>Preferences</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Settings.png" width="145" alt="Settings (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Settings.png" width="145" alt="Settings (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Settings.png" width="125" alt="Settings (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Settings.png" width="125" alt="Settings (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Appearance</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsAppearance.png" width="145" alt="SettingsAppearance (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsAppearance.png" width="145" alt="SettingsAppearance (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsAppearance.png" width="125" alt="SettingsAppearance (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsAppearance.png" width="125" alt="SettingsAppearance (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Language</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsLanguage.png" width="145" alt="SettingsLanguage (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsLanguage.png" width="145" alt="SettingsLanguage (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsLanguage.png" width="125" alt="SettingsLanguage (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsLanguage.png" width="125" alt="SettingsLanguage (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>Security</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsSecurity.png" width="145" alt="SettingsSecurity (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsSecurity.png" width="145" alt="SettingsSecurity (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsSecurity.png" width="125" alt="SettingsSecurity (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsSecurity.png" width="125" alt="SettingsSecurity (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>File opening</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsFileAssociation.png" width="145" alt="SettingsFileAssociation (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsFileAssociation.png" width="145" alt="SettingsFileAssociation (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsFileAssociation.png" width="125" alt="SettingsFileAssociation (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsFileAssociation.png" width="125" alt="SettingsFileAssociation (Web mobile, dark theme)" /></td>
</tr>
<tr>
<td><strong>About</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsAppInfo.png" width="145" alt="SettingsAppInfo (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsAppInfo.png" width="145" alt="SettingsAppInfo (Web tablet, dark theme)" /></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsAppInfo.png" width="125" alt="SettingsAppInfo (Web mobile, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsAppInfo.png" width="125" alt="SettingsAppInfo (Web mobile, dark theme)" /></td>
</tr>
</table>

</details>

---
## 🙏 Acknowledgments

- [University of Milano‑Bicocca](https://www.unimib.it/) for the digital services
- [CINECA](https://www.cineca.it/) for the Esse3 platform
- [Moodle](https://moodle.org/) for the e‑learning platform
- [EasyStaff](https://www.easystaff.it/) for the scheduling system
- [Affluences](https://www.affluences.com/) for library seat booking

---

## 📄 License

© 2025 Alessandro Autiero, Lorenzo Angelo Lupi, Federico Giarrusso, Alessandro Ferrari Pagini.

MyBicocca is free software under the [GNU Affero General Public License v3.0](LICENSE): anyone
who distributes it, changes it or offers it as a service must publish their complete source
code under the same license. For any other use, such as a closed‑source product, the copyright
holders grant commercial licenses. Contributions are accepted under the
[Contributor License Agreement](CLA.md). See [`LICENSING.md`](LICENSING.md) for the details.

<div align="center">
<br />
<sub>Made with ❤️ by Bicocca students, for Bicocca students.</sub>
</div>
