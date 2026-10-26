<div align="center">

<img src="common/screenshots/MyBicoccaSplash.png" width="400" alt="MyBicocca logo" />

# MyBicocca

### Your entire university. One app.

**MyBicocca** folds every digital service of the University of Milano‑Bicocca into a single,
fast, offline‑first Android app built with Jetpack Compose and Material 3 Expressive.

<br />

[![Platform](https://img.shields.io/badge/Android-7.1%2B-9C0C35?style=for-the-badge&logo=android&logoColor=white)](#-installation)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-9C0C35?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203%20Expressive-9C0C35?style=for-the-badge&logo=jetpackcompose&logoColor=white)
[![License](https://img.shields.io/badge/License-MIT-9C0C35?style=for-the-badge)](LICENSE)

<br />

<table>
  <tr>
    <td align="center"><img src="android/tools/screenshots/en/light/Screenshot_Calendar1.png" width="158" alt="Calendar1, light theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/light/Screenshot_Elearning.png" width="158" alt="Elearning, light theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/light/Screenshot_Map.png" width="158" alt="Map, light theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/light/Screenshot_Services.png" width="158" alt="Services, light theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/light/Screenshot_Profile.png" width="158" alt="Profile, light theme" /></td>
  </tr>
  <tr>
    <td align="center"><img src="android/tools/screenshots/en/dark/Screenshot_Calendar1.png" width="158" alt="Calendar1, dark theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/dark/Screenshot_Elearning.png" width="158" alt="Elearning, dark theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/dark/Screenshot_Map.png" width="158" alt="Map, dark theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/dark/Screenshot_Services.png" width="158" alt="Services, dark theme" /></td>
    <td align="center"><img src="android/tools/screenshots/en/dark/Screenshot_Profile.png" width="158" alt="Profile, dark theme" /></td>
  </tr>
</table>

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
university already exposes, and consolidates them behind one native experience.

---

## 🔐 Privacy & security

**Your data stays yours.** MyBicocca talks to the university on your behalf and keeps the
results on your phone, nowhere else.

- 🔑 &nbsp;**Credentials encrypted on‑device**, never sent anywhere but the platforms they belong to
- 🔒 &nbsp;**HTTPS everywhere**, every request and every response
- 🙅 &nbsp;**Your university data is never shared**: no ads, nothing sold
- 🩺 &nbsp;**Anonymous crash reports** go to Firebase Crashlytics to help fix bugs; turn them off in Settings › Security
- 📈 &nbsp;**Anonymous usage and speed statistics** go to Firebase Analytics and Performance Monitoring; request timings carry only the address's host and path, never your tokens or what you asked for

---

## 📦 Installation

Download the latest signed APK from the [**GitHub Releases**](https://github.com/Auties00/MyBicocca/releases/latest)
page and install it on your Android device.

---

## 📅 Calendar

Your whole week at a glance. Lessons from EasyStaff, exams you've booked on Esse3,
Moodle assignment deadlines, Esse3 appointments and library reservations all
land on the same timeline, colour‑coded by source and pinch‑zoomable.

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Calendar1.png" width="170" alt="Calendar1, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar1.png" width="170" alt="Calendar1, dark theme" /></td>
<td>

#### Day

A pinch‑zoomable hourly timeline of everything on your day, each block coloured by where it comes from.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_CalendarEvent.png" width="170" alt="CalendarEvent, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_CalendarEvent.png" width="170" alt="CalendarEvent, dark theme" /></td>
<td>

#### Event details

Tap any block for its time, room and teacher, then get directions on the campus map or jump straight to the course.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Calendar2.png" width="170" alt="Calendar2, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar2.png" width="170" alt="Calendar2, dark theme" /></td>
<td>

#### Week

Seven days at a glance. The zoom level is shared with the Day view, so the density you choose follows you across both.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Calendar3.png" width="170" alt="Calendar3, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Calendar3.png" width="170" alt="Calendar3, dark theme" /></td>
<td>

#### Month

A heatmap tints each date by how busy it is, with a draggable agenda sheet for the day you tap. Pull down anywhere and every source re‑syncs without losing your place.

</td>
</tr>
</table>

---
## 🎓 E‑learning

A complete, native Moodle client, not a webview. Browse your courses, dive into
materials, hand in assignments, sit quizzes, follow forum threads and watch lecture
recordings, all inside the app.

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Elearning.png" width="170" alt="Elearning, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Elearning.png" width="170" alt="Elearning, dark theme" /></td>
<td>

#### Your courses

Every enrolled course, grouped by academic period and filterable by year or favourites.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningAddCourse.png" width="170" alt="ElearningAddCourse, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningAddCourse.png" width="170" alt="ElearningAddCourse, dark theme" /></td>
<td>

#### Course catalog

Browse every teaching area on the university's Moodle, or search by course name or code, and enrol on the spot. The catalog ships with the app, so browsing it needs no network.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourse.png" width="170" alt="ElearningCourse, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourse.png" width="170" alt="ElearningCourse, dark theme" /></td>
<td>

#### Course detail

A collapsing header opens onto tabs for info, content, quizzes, assignments and forums, so the whole course lives behind one screen.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseContent.png" width="170" alt="ElearningCourseContent, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseContent.png" width="170" alt="ElearningCourseContent, dark theme" /></td>
<td>

#### Materials

Sections and folders expand inline, with every file one tap away.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningFile.png" width="170" alt="ElearningFile, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningFile.png" width="170" alt="ElearningFile, dark theme" /></td>
<td>

#### File viewer

PDFs, images and text open in an in‑app viewer, page by page, ready to share or download. Office files hand off to the app of your choice.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseQuiz.png" width="170" alt="ElearningCourseQuiz, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseQuiz.png" width="170" alt="ElearningCourseQuiz, dark theme" /></td>
<td>

#### Quizzes

Review past attempts, resume one in progress, and read your results and feedback question by question.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseForum.png" width="170" alt="ElearningCourseForum, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseForum.png" width="170" alt="ElearningCourseForum, dark theme" /></td>
<td>

#### Forums

Read announcements and discussions, reply, attach files and manage your subscriptions. Just like the web, but native.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ElearningCourseAssignment.png" width="170" alt="ElearningCourseAssignment, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ElearningCourseAssignment.png" width="170" alt="ElearningCourseAssignment, dark theme" /></td>
<td>

#### Assignments

Read the brief, upload your files and submit, then track submission status, teacher feedback and your grade.

</td>
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
<td><img src="android/tools/screenshots/en/light/Screenshot_Map.png" width="170" alt="Map, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Map.png" width="170" alt="Map, dark theme" /></td>
<td>

#### Offline campus map

Tappable pins for every building, rendered from a bundled vector tileset. No Google Maps key, no network needed.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_MapBuildings.png" width="170" alt="MapBuildings, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_MapBuildings.png" width="170" alt="MapBuildings, dark theme" /></td>
<td>

#### Building directory

Search the whole campus and get one‑tap directions to anywhere.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_MapBuildingU1.png" width="170" alt="MapBuildingU1, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_MapBuildingU1.png" width="170" alt="MapBuildingU1, dark theme" /></td>
<td>

#### Inside a building

Drill into rooms and their details, with live EasyBadge occupancy schedules.

</td>
</tr>
</table>

---
## 🗂️ Services

The registry tab fans out into the full Esse3 toolbox: every student service you'd
otherwise hunt for online, grouped into Teaching, Bookings, Documents and Payments.

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Services.png" width="170" alt="Services, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Services.png" width="170" alt="Services, dark theme" /></td>
<td>

#### The services hub

Home to everything administrative, in four groups, topped by a live deadlines banner.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ServicesScadenze.png" width="170" alt="ServicesScadenze, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ServicesScadenze.png" width="170" alt="ServicesScadenze, dark theme" /></td>
<td>

#### Deadlines

The banner counts what's urgent and expands into a full chronological timeline of tuition, enrolment and exam dates.

</td>
</tr>
</table>

### Teaching

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Career.png" width="170" alt="Career, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Career.png" width="170" alt="Career, dark theme" /></td>
<td>

#### Study plan

Your programme year by year, with the activities and credits of each, ready to print.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Attendance.png" width="170" alt="Attendance, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Attendance.png" width="170" alt="Attendance, dark theme" /></td>
<td>

#### Attendance

How much of each course you have attended so far, and whether you are on track.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_AttendanceOpt.png" width="170" alt="AttendanceOpt, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_AttendanceOpt.png" width="170" alt="AttendanceOpt, dark theme" /></td>
<td>

#### Check in

Record your presence at a lecture, or at another activity, in a couple of taps. No paper sheet to chase.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ExamResults.png" width="170" alt="ExamResults, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_ExamResults.png" width="170" alt="ExamResults, dark theme" /></td>
<td>

#### Results

Published grades appear the moment they land, and you can accept or reject each one right from the list.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Questionnaires.png" width="170" alt="Questionnaires, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Questionnaires.png" width="170" alt="Questionnaires, dark theme" /></td>
<td>

#### Questionnaires

The teaching evaluations still to fill in, and the ones you have already sent.

</td>
</tr>
</table>

### Bookings

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_BookableExams.png" width="170" alt="BookableExams, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_BookableExams.png" width="170" alt="BookableExams, dark theme" /></td>
<td>

#### Book an exam

Every open exam call (appello) of your courses, grouped by course, bookable in a single tap.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_BookedExams.png" width="170" alt="BookedExams, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_BookedExams.png" width="170" alt="BookedExams, dark theme" /></td>
<td>

#### Your exams

The exams you've booked, with their date, time and room, and a confirmation you can show on the day.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_BookAppointment.png" width="170" alt="BookAppointment, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_BookAppointment.png" width="170" alt="BookAppointment, dark theme" /></td>
<td>

#### Offices

Book an appointment at the student offices (segreteria) and keep track of the ones coming up.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Library.png" width="170" alt="Library, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Library.png" width="170" alt="Library, dark theme" /></td>
<td>

#### Library seats

Reserve a study seat in the campus libraries, powered by Affluences, and manage the booking from the calendar.

</td>
</tr>
</table>

### Documents

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Enrollments.png" width="170" alt="Enrollments, light theme" /><img src="android/tools/screenshots/en/light/Screenshot_EnrollmentDetail.png" width="170" alt="EnrollmentDetail, light theme" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Enrollments.png" width="170" alt="Enrollments, dark theme" /><img src="android/tools/screenshots/en/dark/Screenshot_EnrollmentDetail.png" width="170" alt="EnrollmentDetail, dark theme" /></td>
<td>

#### Enrolments

Each academic year you enrolled in, and for each its enrolment type, fee exemptions and bands, and degree programme.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Titles.png" width="170" alt="Titles, light theme" /><img src="android/tools/screenshots/en/light/Screenshot_TitleDetail.png" width="170" alt="TitleDetail, light theme" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Titles.png" width="170" alt="Titles, dark theme" /><img src="android/tools/screenshots/en/dark/Screenshot_TitleDetail.png" width="170" alt="TitleDetail, dark theme" /></td>
<td>

#### Degrees

The qualifications on your record, such as your high school diploma, with grade, year and institution.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Documents.png" width="170" alt="Documents, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Documents.png" width="170" alt="Documents, dark theme" /></td>
<td>

#### Certificates

Digitally signed self‑certifications, such as enrolment with exams or the tuition you paid each year, downloaded as PDFs.

</td>
</tr>
</table>

### Payments

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Fees.png" width="170" alt="Fees, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Fees.png" width="170" alt="Fees, dark theme" /></td>
<td>

#### Fees

The tuition instalments still to pay and the ones already paid, with amounts and deadlines.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Isee.png" width="170" alt="Isee, light theme" /><img src="android/tools/screenshots/en/light/Screenshot_IseeDetail.png" width="170" alt="IseeDetail, light theme" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Isee.png" width="170" alt="Isee, dark theme" /><img src="android/tools/screenshots/en/dark/Screenshot_IseeDetail.png" width="170" alt="IseeDetail, dark theme" /></td>
<td>

#### ISEE

Your ISEE indicator for each academic year, against the threshold for benefits and the exemption it earns you.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Refunds.png" width="170" alt="Refunds, light theme" /><img src="android/tools/screenshots/en/light/Screenshot_RefundDetail.png" width="170" alt="RefundDetail, light theme" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_Refunds.png" width="170" alt="Refunds, dark theme" /><img src="android/tools/screenshots/en/dark/Screenshot_RefundDetail.png" width="170" alt="RefundDetail, dark theme" /></td>
<td>

#### Refunds

What the university has paid back, and for each refund its reason, mandate and credit date.

</td>
</tr>
</table>

And the rest of the registry too: MAV and PagoPA payments, study‑plan editing and more.

---
## 👤 Profile & career

A live dashboard of your academic standing: a flippable student card, your headline
stats, a grade‑trend chart, and a simulator that answers _"what grade do I need?"_.

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Profile.png" width="170" alt="Profile, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Profile.png" width="170" alt="Profile, dark theme" /></td>
<td>

#### Student card & stats

A flippable student card up top, then your headline numbers: arithmetic and weighted average, exams passed and credits earned.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_ExamsTaken.png" width="170" alt="ExamsTaken, light theme" /><img src="android/tools/screenshots/en/light/Screenshot_Credits.png" width="170" alt="Credits, light theme" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_ExamsTaken.png" width="170" alt="ExamsTaken, dark theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Credits.png" width="170" alt="Credits, dark theme" /></td>
<td>

#### Exams and credits

Every exam you passed, year by year, with its grade, and the credits each one earned you towards your degree.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Average.png" width="170" alt="Average, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Average.png" width="170" alt="Average, dark theme" /></td>
<td>

#### Grade simulator

A "what‑if" calculator that shows how the next exam moves your average, in both arithmetic and weighted modes.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_AccountSwitcher.png" width="170" alt="AccountSwitcher, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_AccountSwitcher.png" width="170" alt="AccountSwitcher, dark theme" /></td>
<td>

#### Accounts

Sign in with more than one account, and switch between them and between your careers.

</td>
</tr>
</table>

---
## ⚙️ Settings

Make the app yours: its look, its language, how it opens files and how it protects your data.

<table>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_Settings.png" width="170" alt="Settings, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_Settings.png" width="170" alt="Settings, dark theme" /></td>
<td>

#### Preferences

Appearance, language, security, file opening and haptics, plus everything about the app.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsAppearance.png" width="170" alt="SettingsAppearance, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsAppearance.png" width="170" alt="SettingsAppearance, dark theme" /></td>
<td>

#### Appearance

Four palettes (MyBicocca, Material You, Oceano and Bosco), each in light, dark or following the system.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsLanguage.png" width="170" alt="SettingsLanguage, light theme" /><img src="android/tools/screenshots/en/light/Screenshot_SettingsHaptic.png" width="170" alt="SettingsHaptic, light theme" /><br /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsLanguage.png" width="170" alt="SettingsLanguage, dark theme" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsHaptic.png" width="170" alt="SettingsHaptic, dark theme" /></td>
<td>

#### Language and haptics

Use the app in Italian or English, or follow the system, and choose whether it vibrates as you tap.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsSecurity.png" width="170" alt="SettingsSecurity, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsSecurity.png" width="170" alt="SettingsSecurity, dark theme" /></td>
<td>

#### Security

Lock the app behind your fingerprint or your device password, and choose whether anonymous crash reports are sent.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsFileAssociation.png" width="170" alt="SettingsFileAssociation, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsFileAssociation.png" width="170" alt="SettingsFileAssociation, dark theme" /></td>
<td>

#### File opening

Pick the app that opens each kind of file, from PDFs and images to videos, audio and archives, or be asked every time.

</td>
</tr>
<tr>
<td><img src="android/tools/screenshots/en/light/Screenshot_SettingsAppInfo.png" width="170" alt="SettingsAppInfo, light theme" /><img src="android/tools/screenshots/en/dark/Screenshot_SettingsAppInfo.png" width="170" alt="SettingsAppInfo, dark theme" /></td>
<td>

#### About

The app's version, what's new in it, its updates, including beta releases, and the people behind it.

</td>
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

Released under the [MIT License](LICENSE) © 2025 Alessandro Autiero, Federico Giarrusso, Lorenzo Angelo Lupi, Alessandro Ferrari.

<div align="center">
<br />
<sub>Made with ❤️ by Bicocca students, for Bicocca students.</sub>
</div>
