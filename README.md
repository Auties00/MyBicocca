<div align="center">

<img src="common/screenshots/MyBicoccaSplash.png" width="400" alt="MyBicocca logo" />

# MyBicocca

### Your entire university. One app.

**MyBicocca** folds every digital service of the University of Milano‑Bicocca into a single,
fast, offline‑first app: native on **Android** (Jetpack Compose & Material 3 Expressive) and on
the **web** (SvelteKit & Carta), with an **iOS** app on the way.

<br />

[![Android](https://img.shields.io/badge/Android-7.1%2B-9C0C35?style=for-the-badge&logo=android&logoColor=white)](#-installation)
[![Web](https://img.shields.io/badge/Web-9C0C35?style=for-the-badge&logo=svelte&logoColor=white)](#-installation)
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
- **Web**: Open [**web.mybicocca.it**](https://web.mybicocca.it) on desktop (installable as a PWA) or mobile, or run it locally from [`web/web-app`](web/web-app).
- **iOS**: Coming soon on the App Store. In the meantime, you can use the web app in the browser on iPhone and iPad.


---

## 📅 Calendar

Your whole week at a glance. Lessons from EasyStaff, exams you've booked on Esse3,
Moodle assignment deadlines, Esse3 appointments and library reservations all
land on the same timeline, colour‑coded by source and pinch‑zoomable.

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Calendar1.png" width="180" alt="Calendar1 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Calendar1.png" width="180" alt="Calendar1 (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Day

A pinch‑zoomable hourly timeline of everything on your day, each block coloured by where it comes from.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Calendar1.png" width="125" alt="Calendar1 (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Calendar1.png" width="125" alt="Calendar1 (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Calendar1.png" width="145" alt="Calendar1 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Calendar1.png" width="145" alt="Calendar1 (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Calendar1.png" width="125" alt="Calendar1 (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar1.png" width="125" alt="Calendar1 (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_CalendarEvent.png" width="180" alt="CalendarEvent (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_CalendarEvent.png" width="180" alt="CalendarEvent (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Event details

Tap any block for its time, room and teacher, then get directions on the campus map or jump straight to the course.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_CalendarEvent.png" width="125" alt="CalendarEvent (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_CalendarEvent.png" width="125" alt="CalendarEvent (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_CalendarEvent.png" width="145" alt="CalendarEvent (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_CalendarEvent.png" width="145" alt="CalendarEvent (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_CalendarEvent.png" width="125" alt="CalendarEvent (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_CalendarEvent.png" width="125" alt="CalendarEvent (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Calendar2.png" width="180" alt="Calendar2 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Calendar2.png" width="180" alt="Calendar2 (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Week

Seven days at a glance. The zoom level is shared with the Day view, so the density you choose follows you across both.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Calendar2.png" width="125" alt="Calendar2 (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Calendar2.png" width="125" alt="Calendar2 (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Calendar2.png" width="145" alt="Calendar2 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Calendar2.png" width="145" alt="Calendar2 (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Calendar2.png" width="125" alt="Calendar2 (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar2.png" width="125" alt="Calendar2 (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Calendar3.png" width="180" alt="Calendar3 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Calendar3.png" width="180" alt="Calendar3 (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Month

A heatmap tints each date by how busy it is, with a draggable agenda sheet for the day you tap. Pull down anywhere and every source re‑syncs without losing your place.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Calendar3.png" width="125" alt="Calendar3 (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Calendar3.png" width="125" alt="Calendar3 (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Calendar3.png" width="145" alt="Calendar3 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Calendar3.png" width="145" alt="Calendar3 (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Calendar3.png" width="125" alt="Calendar3 (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar3.png" width="125" alt="Calendar3 (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

---
## 🎓 E‑learning

A complete Moodle client, not a webview. Browse your courses, dive into
materials, hand in assignments, sit quizzes, follow forum threads and watch lecture
recordings, all inside the app.

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Elearning.png" width="180" alt="Elearning (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Elearning.png" width="180" alt="Elearning (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Your courses

Every enrolled course, grouped by academic period and filterable by year or favourites.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Elearning.png" width="125" alt="Elearning (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Elearning.png" width="125" alt="Elearning (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Elearning.png" width="145" alt="Elearning (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Elearning.png" width="145" alt="Elearning (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Elearning.png" width="125" alt="Elearning (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Elearning.png" width="125" alt="Elearning (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningAddCourse.png" width="180" alt="ElearningAddCourse (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningAddCourse.png" width="180" alt="ElearningAddCourse (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Course catalog

Browse every teaching area on the university's Moodle, or search by course name or code, and enrol on the spot. The catalog ships with the app, so browsing it needs no network.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningAddCourse.png" width="125" alt="ElearningAddCourse (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningAddCourse.png" width="125" alt="ElearningAddCourse (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningAddCourse.png" width="145" alt="ElearningAddCourse (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningAddCourse.png" width="145" alt="ElearningAddCourse (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningAddCourse.png" width="125" alt="ElearningAddCourse (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningAddCourse.png" width="125" alt="ElearningAddCourse (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourse.png" width="180" alt="ElearningCourse (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourse.png" width="180" alt="ElearningCourse (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Course detail

A collapsing header opens onto tabs for info, content, quizzes, assignments and forums, so the whole course lives behind one screen.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourse.png" width="125" alt="ElearningCourse (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourse.png" width="125" alt="ElearningCourse (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourse.png" width="145" alt="ElearningCourse (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourse.png" width="145" alt="ElearningCourse (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourse.png" width="125" alt="ElearningCourse (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourse.png" width="125" alt="ElearningCourse (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseContent.png" width="180" alt="ElearningCourseContent (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseContent.png" width="180" alt="ElearningCourseContent (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Materials

Sections and folders expand inline, with every file one tap away.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseContent.png" width="125" alt="ElearningCourseContent (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseContent.png" width="125" alt="ElearningCourseContent (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseContent.png" width="145" alt="ElearningCourseContent (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseContent.png" width="145" alt="ElearningCourseContent (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseContent.png" width="125" alt="ElearningCourseContent (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseContent.png" width="125" alt="ElearningCourseContent (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningFile.png" width="180" alt="ElearningFile (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningFile.png" width="180" alt="ElearningFile (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### File viewer

PDFs, images and text open in an in‑app viewer, page by page, ready to share or download. Office files hand off to the app of your choice.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningFile.png" width="125" alt="ElearningFile (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningFile.png" width="125" alt="ElearningFile (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningFile.png" width="145" alt="ElearningFile (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningFile.png" width="145" alt="ElearningFile (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningFile.png" width="125" alt="ElearningFile (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningFile.png" width="125" alt="ElearningFile (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseQuiz.png" width="180" alt="ElearningCourseQuiz (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseQuiz.png" width="180" alt="ElearningCourseQuiz (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Quizzes

Review past attempts, resume one in progress, and read your results and feedback question by question.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseQuiz.png" width="125" alt="ElearningCourseQuiz (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseQuiz.png" width="125" alt="ElearningCourseQuiz (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseQuiz.png" width="145" alt="ElearningCourseQuiz (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseQuiz.png" width="145" alt="ElearningCourseQuiz (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseQuiz.png" width="125" alt="ElearningCourseQuiz (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseQuiz.png" width="125" alt="ElearningCourseQuiz (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseForum.png" width="180" alt="ElearningCourseForum (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseForum.png" width="180" alt="ElearningCourseForum (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Forums

Read announcements and discussions, reply, attach files and manage your subscriptions, straight from the app.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseForum.png" width="125" alt="ElearningCourseForum (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseForum.png" width="125" alt="ElearningCourseForum (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseForum.png" width="145" alt="ElearningCourseForum (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseForum.png" width="145" alt="ElearningCourseForum (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseForum.png" width="125" alt="ElearningCourseForum (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseForum.png" width="125" alt="ElearningCourseForum (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ElearningCourseAssignment.png" width="180" alt="ElearningCourseAssignment (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ElearningCourseAssignment.png" width="180" alt="ElearningCourseAssignment (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Assignments

Read the brief, upload your files and submit, then track submission status, teacher feedback and your grade.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ElearningCourseAssignment.png" width="125" alt="ElearningCourseAssignment (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ElearningCourseAssignment.png" width="125" alt="ElearningCourseAssignment (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ElearningCourseAssignment.png" width="145" alt="ElearningCourseAssignment (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ElearningCourseAssignment.png" width="145" alt="ElearningCourseAssignment (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseAssignment.png" width="125" alt="ElearningCourseAssignment (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseAssignment.png" width="125" alt="ElearningCourseAssignment (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

Lecture recordings stream in‑app (Kaltura / HLS), alongside grades and module deadlines.

---
## 🗺️ Maps

The whole campus, offline. Buildings and rooms render from a bundled Protomaps vector
tileset on a MapLibre engine (**no Google Maps key, no network needed**) and the map
recolours itself to match your chosen theme.

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Map.png" width="180" alt="Map (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Map.png" width="180" alt="Map (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Offline campus map

Tappable pins for every building, rendered from a bundled vector tileset. No Google Maps key, no network needed.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Map.png" width="125" alt="Map (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Map.png" width="125" alt="Map (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Map.png" width="145" alt="Map (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Map.png" width="145" alt="Map (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Map.png" width="125" alt="Map (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Map.png" width="125" alt="Map (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_MapBuildings.png" width="180" alt="MapBuildings (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_MapBuildings.png" width="180" alt="MapBuildings (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Building directory

Search the whole campus and get one‑tap directions to anywhere.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_MapBuildings.png" width="125" alt="MapBuildings (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_MapBuildings.png" width="125" alt="MapBuildings (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_MapBuildings.png" width="145" alt="MapBuildings (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_MapBuildings.png" width="145" alt="MapBuildings (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_MapBuildings.png" width="125" alt="MapBuildings (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_MapBuildings.png" width="125" alt="MapBuildings (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_MapBuildingU1.png" width="180" alt="MapBuildingU1 (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_MapBuildingU1.png" width="180" alt="MapBuildingU1 (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Inside a building

Drill into rooms and their details, with live EasyBadge occupancy schedules.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_MapBuildingU1.png" width="125" alt="MapBuildingU1 (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_MapBuildingU1.png" width="125" alt="MapBuildingU1 (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_MapBuildingU1.png" width="145" alt="MapBuildingU1 (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_MapBuildingU1.png" width="145" alt="MapBuildingU1 (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_MapBuildingU1.png" width="125" alt="MapBuildingU1 (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_MapBuildingU1.png" width="125" alt="MapBuildingU1 (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

---
## 🗂️ Services

The registry tab fans out into the full Esse3 toolbox: every student service you'd
otherwise hunt for online, grouped into Teaching, Bookings, Documents and Payments.

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Services.png" width="180" alt="Services (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Services.png" width="180" alt="Services (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### The services hub

Home to everything administrative, in four groups, topped by a live deadlines banner.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Services.png" width="125" alt="Services (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Services.png" width="125" alt="Services (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Services.png" width="145" alt="Services (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Services.png" width="145" alt="Services (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Services.png" width="125" alt="Services (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Services.png" width="125" alt="Services (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ServicesScadenze.png" width="180" alt="ServicesScadenze (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ServicesScadenze.png" width="180" alt="ServicesScadenze (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Deadlines

The banner counts what's urgent and expands into a full chronological timeline of tuition, enrolment and exam dates.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ServicesScadenze.png" width="125" alt="ServicesScadenze (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ServicesScadenze.png" width="125" alt="ServicesScadenze (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ServicesScadenze.png" width="145" alt="ServicesScadenze (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ServicesScadenze.png" width="145" alt="ServicesScadenze (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ServicesScadenze.png" width="125" alt="ServicesScadenze (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ServicesScadenze.png" width="125" alt="ServicesScadenze (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

### Teaching

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Career.png" width="180" alt="Career (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Career.png" width="180" alt="Career (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Study plan

Your programme year by year, with the activities and credits of each, ready to print.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Career.png" width="125" alt="Career (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Career.png" width="125" alt="Career (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Career.png" width="145" alt="Career (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Career.png" width="145" alt="Career (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Career.png" width="125" alt="Career (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Career.png" width="125" alt="Career (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Attendance.png" width="180" alt="Attendance (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Attendance.png" width="180" alt="Attendance (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Attendance

How much of each course you have attended so far, and whether you are on track.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Attendance.png" width="125" alt="Attendance (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Attendance.png" width="125" alt="Attendance (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Attendance.png" width="145" alt="Attendance (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Attendance.png" width="145" alt="Attendance (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Attendance.png" width="125" alt="Attendance (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Attendance.png" width="125" alt="Attendance (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_AttendanceOpt.png" width="180" alt="AttendanceOpt (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_AttendanceOpt.png" width="180" alt="AttendanceOpt (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Check in

Record your presence at a lecture, or at another activity, in a couple of taps. No paper sheet to chase.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_AttendanceOpt.png" width="125" alt="AttendanceOpt (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_AttendanceOpt.png" width="125" alt="AttendanceOpt (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_AttendanceOpt.png" width="145" alt="AttendanceOpt (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_AttendanceOpt.png" width="145" alt="AttendanceOpt (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_AttendanceOpt.png" width="125" alt="AttendanceOpt (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_AttendanceOpt.png" width="125" alt="AttendanceOpt (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ExamResults.png" width="180" alt="ExamResults (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ExamResults.png" width="180" alt="ExamResults (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Results

Published grades appear the moment they land, and you can accept or reject each one right from the list.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ExamResults.png" width="125" alt="ExamResults (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ExamResults.png" width="125" alt="ExamResults (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ExamResults.png" width="145" alt="ExamResults (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ExamResults.png" width="145" alt="ExamResults (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ExamResults.png" width="125" alt="ExamResults (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_ExamResults.png" width="125" alt="ExamResults (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Questionnaires.png" width="180" alt="Questionnaires (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Questionnaires.png" width="180" alt="Questionnaires (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Questionnaires

The teaching evaluations still to fill in, and the ones you have already sent.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Questionnaires.png" width="125" alt="Questionnaires (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Questionnaires.png" width="125" alt="Questionnaires (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Questionnaires.png" width="145" alt="Questionnaires (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Questionnaires.png" width="145" alt="Questionnaires (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Questionnaires.png" width="125" alt="Questionnaires (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Questionnaires.png" width="125" alt="Questionnaires (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

### Bookings

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_BookableExams.png" width="180" alt="BookableExams (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_BookableExams.png" width="180" alt="BookableExams (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Book an exam

Every open exam call (appello) of your courses, grouped by course, bookable in a single tap.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_BookableExams.png" width="125" alt="BookableExams (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_BookableExams.png" width="125" alt="BookableExams (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_BookableExams.png" width="145" alt="BookableExams (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_BookableExams.png" width="145" alt="BookableExams (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_BookableExams.png" width="125" alt="BookableExams (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_BookableExams.png" width="125" alt="BookableExams (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_BookedExams.png" width="180" alt="BookedExams (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_BookedExams.png" width="180" alt="BookedExams (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Your exams

The exams you've booked, with their date, time and room, and a confirmation you can show on the day.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_BookedExams.png" width="125" alt="BookedExams (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_BookedExams.png" width="125" alt="BookedExams (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_BookedExams.png" width="145" alt="BookedExams (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_BookedExams.png" width="145" alt="BookedExams (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_BookedExams.png" width="125" alt="BookedExams (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_BookedExams.png" width="125" alt="BookedExams (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_BookAppointment.png" width="180" alt="BookAppointment (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_BookAppointment.png" width="180" alt="BookAppointment (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Offices

Book an appointment at the student offices (segreteria) and keep track of the ones coming up.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_BookAppointment.png" width="125" alt="BookAppointment (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_BookAppointment.png" width="125" alt="BookAppointment (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_BookAppointment.png" width="145" alt="BookAppointment (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_BookAppointment.png" width="145" alt="BookAppointment (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_BookAppointment.png" width="125" alt="BookAppointment (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_BookAppointment.png" width="125" alt="BookAppointment (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Library.png" width="180" alt="Library (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Library.png" width="180" alt="Library (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Library seats

Reserve a study seat in the campus libraries, powered by Affluences, and manage the booking from the calendar.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Library.png" width="125" alt="Library (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Library.png" width="125" alt="Library (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Library.png" width="145" alt="Library (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Library.png" width="145" alt="Library (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Library.png" width="125" alt="Library (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Library.png" width="125" alt="Library (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

### Documents

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Enrollments.png" width="180" alt="Enrollments (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_EnrollmentDetail.png" width="180" alt="EnrollmentDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Enrollments.png" width="180" alt="Enrollments (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_EnrollmentDetail.png" width="180" alt="EnrollmentDetail (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Enrolments

Each academic year you enrolled in, and for each its enrolment type, fee exemptions and bands, and degree programme.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Enrollments.png" width="125" alt="Enrollments (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_EnrollmentDetail.png" width="125" alt="EnrollmentDetail (Web phone, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Enrollments.png" width="125" alt="Enrollments (Web phone, dark theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_EnrollmentDetail.png" width="125" alt="EnrollmentDetail (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Enrollments.png" width="145" alt="Enrollments (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_EnrollmentDetail.png" width="145" alt="EnrollmentDetail (Web tablet, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Enrollments.png" width="145" alt="Enrollments (Web tablet, dark theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_EnrollmentDetail.png" width="145" alt="EnrollmentDetail (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Enrollments.png" width="125" alt="Enrollments (Android phone, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_EnrollmentDetail.png" width="125" alt="EnrollmentDetail (Android phone, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Enrollments.png" width="125" alt="Enrollments (Android phone, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_EnrollmentDetail.png" width="125" alt="EnrollmentDetail (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Titles.png" width="180" alt="Titles (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_TitleDetail.png" width="180" alt="TitleDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Titles.png" width="180" alt="Titles (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_TitleDetail.png" width="180" alt="TitleDetail (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Degrees

The qualifications on your record, such as your high school diploma, with grade, year and institution.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Titles.png" width="125" alt="Titles (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_TitleDetail.png" width="125" alt="TitleDetail (Web phone, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Titles.png" width="125" alt="Titles (Web phone, dark theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_TitleDetail.png" width="125" alt="TitleDetail (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Titles.png" width="145" alt="Titles (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_TitleDetail.png" width="145" alt="TitleDetail (Web tablet, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Titles.png" width="145" alt="Titles (Web tablet, dark theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_TitleDetail.png" width="145" alt="TitleDetail (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Titles.png" width="125" alt="Titles (Android phone, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_TitleDetail.png" width="125" alt="TitleDetail (Android phone, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Titles.png" width="125" alt="Titles (Android phone, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_TitleDetail.png" width="125" alt="TitleDetail (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Documents.png" width="180" alt="Documents (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Documents.png" width="180" alt="Documents (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Certificates

Digitally signed self‑certifications, such as enrolment with exams or the tuition you paid each year, downloaded as PDFs.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Documents.png" width="125" alt="Documents (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Documents.png" width="125" alt="Documents (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Documents.png" width="145" alt="Documents (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Documents.png" width="145" alt="Documents (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Documents.png" width="125" alt="Documents (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Documents.png" width="125" alt="Documents (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

### Payments

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Fees.png" width="180" alt="Fees (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Fees.png" width="180" alt="Fees (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Fees

The tuition instalments still to pay and the ones already paid, with amounts and deadlines.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Fees.png" width="125" alt="Fees (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Fees.png" width="125" alt="Fees (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Fees.png" width="145" alt="Fees (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Fees.png" width="145" alt="Fees (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Fees.png" width="125" alt="Fees (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Fees.png" width="125" alt="Fees (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Isee.png" width="180" alt="Isee (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_IseeDetail.png" width="180" alt="IseeDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Isee.png" width="180" alt="Isee (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_IseeDetail.png" width="180" alt="IseeDetail (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### ISEE

Your ISEE indicator for each academic year, against the threshold for benefits and the exemption it earns you.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Isee.png" width="125" alt="Isee (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_IseeDetail.png" width="125" alt="IseeDetail (Web phone, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Isee.png" width="125" alt="Isee (Web phone, dark theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_IseeDetail.png" width="125" alt="IseeDetail (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Isee.png" width="145" alt="Isee (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_IseeDetail.png" width="145" alt="IseeDetail (Web tablet, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Isee.png" width="145" alt="Isee (Web tablet, dark theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_IseeDetail.png" width="145" alt="IseeDetail (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Isee.png" width="125" alt="Isee (Android phone, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_IseeDetail.png" width="125" alt="IseeDetail (Android phone, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Isee.png" width="125" alt="Isee (Android phone, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_IseeDetail.png" width="125" alt="IseeDetail (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Refunds.png" width="180" alt="Refunds (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_RefundDetail.png" width="180" alt="RefundDetail (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Refunds.png" width="180" alt="Refunds (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_RefundDetail.png" width="180" alt="RefundDetail (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Refunds

What the university has paid back, and for each refund its reason, mandate and credit date.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Refunds.png" width="125" alt="Refunds (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_RefundDetail.png" width="125" alt="RefundDetail (Web phone, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Refunds.png" width="125" alt="Refunds (Web phone, dark theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_RefundDetail.png" width="125" alt="RefundDetail (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Refunds.png" width="145" alt="Refunds (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_RefundDetail.png" width="145" alt="RefundDetail (Web tablet, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Refunds.png" width="145" alt="Refunds (Web tablet, dark theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_RefundDetail.png" width="145" alt="RefundDetail (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Refunds.png" width="125" alt="Refunds (Android phone, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_RefundDetail.png" width="125" alt="RefundDetail (Android phone, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Refunds.png" width="125" alt="Refunds (Android phone, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_RefundDetail.png" width="125" alt="RefundDetail (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

And the rest of the registry too: MAV and PagoPA payments, study‑plan editing and more.

---
## 👤 Profile & career

A live dashboard of your academic standing: a flippable student card, your headline
stats, a grade‑trend chart, and a simulator that answers _"what grade do I need?"_.

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Profile.png" width="180" alt="Profile (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Profile.png" width="180" alt="Profile (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Student card & stats

A flippable student card up top, then your headline numbers: arithmetic and weighted average, exams passed and credits earned.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Profile.png" width="125" alt="Profile (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Profile.png" width="125" alt="Profile (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Profile.png" width="145" alt="Profile (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Profile.png" width="145" alt="Profile (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Profile.png" width="125" alt="Profile (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Profile.png" width="125" alt="Profile (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_ExamsTaken.png" width="180" alt="ExamsTaken (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Credits.png" width="180" alt="Credits (Web desktop, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_ExamsTaken.png" width="180" alt="ExamsTaken (Web desktop, dark theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Credits.png" width="180" alt="Credits (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Exams and credits

Every exam you passed, year by year, with its grade, and the credits each one earned you towards your degree.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_ExamsTaken.png" width="125" alt="ExamsTaken (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Credits.png" width="125" alt="Credits (Web phone, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_ExamsTaken.png" width="125" alt="ExamsTaken (Web phone, dark theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Credits.png" width="125" alt="Credits (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_ExamsTaken.png" width="145" alt="ExamsTaken (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Credits.png" width="145" alt="Credits (Web tablet, light theme)" /><br /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_ExamsTaken.png" width="145" alt="ExamsTaken (Web tablet, dark theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Credits.png" width="145" alt="Credits (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_ExamsTaken.png" width="125" alt="ExamsTaken (Android phone, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_Credits.png" width="125" alt="Credits (Android phone, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_ExamsTaken.png" width="125" alt="ExamsTaken (Android phone, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Credits.png" width="125" alt="Credits (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Average.png" width="180" alt="Average (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Average.png" width="180" alt="Average (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Grade simulator

A "what‑if" calculator that shows how the next exam moves your average, in both arithmetic and weighted modes.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Average.png" width="125" alt="Average (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Average.png" width="125" alt="Average (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Average.png" width="145" alt="Average (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Average.png" width="145" alt="Average (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Average.png" width="125" alt="Average (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Average.png" width="125" alt="Average (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_AccountSwitcher.png" width="180" alt="AccountSwitcher (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_AccountSwitcher.png" width="180" alt="AccountSwitcher (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Accounts

Sign in with more than one account, and switch between them and between your careers.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_AccountSwitcher.png" width="125" alt="AccountSwitcher (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_AccountSwitcher.png" width="125" alt="AccountSwitcher (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_AccountSwitcher.png" width="145" alt="AccountSwitcher (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_AccountSwitcher.png" width="145" alt="AccountSwitcher (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_AccountSwitcher.png" width="125" alt="AccountSwitcher (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_AccountSwitcher.png" width="125" alt="AccountSwitcher (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

---
## ⚙️ Settings

Make the app yours: its look, its language, how it opens files and how it protects your data.

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_Settings.png" width="180" alt="Settings (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_Settings.png" width="180" alt="Settings (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Preferences

Appearance, language, security, file opening and haptics, plus everything about the app.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_Settings.png" width="125" alt="Settings (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_Settings.png" width="125" alt="Settings (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_Settings.png" width="145" alt="Settings (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_Settings.png" width="145" alt="Settings (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_Settings.png" width="125" alt="Settings (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_Settings.png" width="125" alt="Settings (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsAppearance.png" width="180" alt="SettingsAppearance (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsAppearance.png" width="180" alt="SettingsAppearance (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Appearance

Custom palettes (MyBicocca, Material You on Android, Oceano and Bosco), each in light, dark or following the system.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsAppearance.png" width="125" alt="SettingsAppearance (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsAppearance.png" width="125" alt="SettingsAppearance (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsAppearance.png" width="145" alt="SettingsAppearance (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsAppearance.png" width="145" alt="SettingsAppearance (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsAppearance.png" width="125" alt="SettingsAppearance (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsAppearance.png" width="125" alt="SettingsAppearance (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsLanguage.png" width="180" alt="SettingsLanguage (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsLanguage.png" width="180" alt="SettingsLanguage (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Language and haptics

Use the app in Italian or English, or follow the system, and choose whether it vibrates as you tap (a dedicated screen on Android, and a toggle in Settings on touch web devices).

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsLanguage.png" width="125" alt="SettingsLanguage (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsLanguage.png" width="125" alt="SettingsLanguage (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsLanguage.png" width="145" alt="SettingsLanguage (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsLanguage.png" width="145" alt="SettingsLanguage (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsLanguage.png" width="125" alt="SettingsLanguage (Android phone, light theme)" /><img src="android/tools/screenshots/en/light/Screenshot_SettingsHaptic.png" width="125" alt="SettingsHaptic (Android phone, light theme)" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsLanguage.png" width="125" alt="SettingsLanguage (Android phone, dark theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsHaptic.png" width="125" alt="SettingsHaptic (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsSecurity.png" width="180" alt="SettingsSecurity (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsSecurity.png" width="180" alt="SettingsSecurity (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### Security

Lock the app behind your fingerprint or device password (or a PIN/passkey on the web), and choose whether anonymous crash reports are sent on Android.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsSecurity.png" width="125" alt="SettingsSecurity (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsSecurity.png" width="125" alt="SettingsSecurity (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsSecurity.png" width="145" alt="SettingsSecurity (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsSecurity.png" width="145" alt="SettingsSecurity (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsSecurity.png" width="125" alt="SettingsSecurity (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsSecurity.png" width="125" alt="SettingsSecurity (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsFileAssociation.png" width="180" alt="SettingsFileAssociation (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsFileAssociation.png" width="180" alt="SettingsFileAssociation (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### File opening

Pick how each kind of file opens, from PDFs and images to videos, audio and archives, or be asked every time.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsFileAssociation.png" width="125" alt="SettingsFileAssociation (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsFileAssociation.png" width="125" alt="SettingsFileAssociation (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsFileAssociation.png" width="145" alt="SettingsFileAssociation (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsFileAssociation.png" width="145" alt="SettingsFileAssociation (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsFileAssociation.png" width="125" alt="SettingsFileAssociation (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsFileAssociation.png" width="125" alt="SettingsFileAssociation (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

<table>
<tr>
<td><strong>Web (Desktop)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/desktop/light/Screenshot_SettingsAppInfo.png" width="180" alt="SettingsAppInfo (Web desktop, light theme)" /><img src="web/web-app/tools/screenshots/en/desktop/dark/Screenshot_SettingsAppInfo.png" width="180" alt="SettingsAppInfo (Web desktop, dark theme)" /></td>
<td rowspan="7">

#### About

The app's version, what's new in it, its updates, including beta releases, and the people behind it.

</td>
</tr>
<tr>
<td><strong>Web (Phone)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/mobile/light/Screenshot_SettingsAppInfo.png" width="125" alt="SettingsAppInfo (Web phone, light theme)" /><img src="web/web-app/tools/screenshots/en/mobile/dark/Screenshot_SettingsAppInfo.png" width="125" alt="SettingsAppInfo (Web phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Web (Tablet)</strong></td>
<td><img src="web/web-app/tools/screenshots/en/tablet/light/Screenshot_SettingsAppInfo.png" width="145" alt="SettingsAppInfo (Web tablet, light theme)" /><img src="web/web-app/tools/screenshots/en/tablet/dark/Screenshot_SettingsAppInfo.png" width="145" alt="SettingsAppInfo (Web tablet, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Phone)</strong></td>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsAppInfo.png" width="125" alt="SettingsAppInfo (Android phone, light theme)" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsAppInfo.png" width="125" alt="SettingsAppInfo (Android phone, dark theme)" /></td>
</tr>
<tr>
<td><strong>Android (Tablet)</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
<tr>
<td><strong>iPadOS</strong></td>
<td><em>Coming soon</em></td>
</tr>
</table>

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
