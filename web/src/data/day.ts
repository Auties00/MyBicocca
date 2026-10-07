import type { ImageMetadata } from 'astro';
import type { ClockTime } from '~/lib/clock';

import calendarWeek from '@screenshots/Screenshot_Calendar2.png';
import calendarMonth from '@screenshots/Screenshot_Calendar3.png';
import course from '@screenshots/Screenshot_ElearningCourse.png';
import courseContent from '@screenshots/Screenshot_ElearningCourseContent.png';
import courseQuiz from '@screenshots/Screenshot_ElearningCourseQuiz.png';
import courseAssignment from '@screenshots/Screenshot_ElearningCourseAssignment.png';
import map from '@screenshots/Screenshot_Map.png';
import mapBuildings from '@screenshots/Screenshot_MapBuildings.png';
import mapRooms from '@screenshots/Screenshot_MapBuildingU1.png';
import attendance from '@screenshots/Screenshot_Attendance.png';
import bookableExams from '@screenshots/Screenshot_BookableExams.png';
import deadlines from '@screenshots/Screenshot_ServicesScadenze.png';
import services from '@screenshots/Screenshot_Services.png';
import results from '@screenshots/Screenshot_ExamResults.png';
import profile from '@screenshots/Screenshot_Profile.png';
import average from '@screenshots/Screenshot_Average.png';
import search from '@screenshots/Screenshot_Search.png';
import appearance from '@screenshots/Screenshot_SettingsAppearance.png';

/**
 * Event colours, named after the calendar's own palette
 * (android/.../calendar/theme/EventPalette.kt). Each has a light and a dark variant.
 */
export type EventTint = 'amber' | 'violet' | 'teal' | 'exam' | 'deadline';

/**
 * The piece of app UI that marks each moment of the day. They are rebuilt in HTML
 * (not screenshots) so they stay sharp, readable and themeable.
 */
export type Moment =
  | {
      readonly kind: 'event';
      readonly tint: EventTint;
      readonly range: string;
      readonly title: string;
      readonly type: string;
      readonly place: string;
    }
  | {
      readonly kind: 'exam-call';
      readonly course: string;
      readonly code: string;
      readonly dates: readonly { readonly day: number; readonly month: string; readonly mode: 'Scritto' | 'Orale' }[];
    }
  | {
      readonly kind: 'result';
      readonly grade: number;
      readonly course: string;
      readonly note: string;
    }
  | {
      readonly kind: 'building';
      readonly code: string;
      readonly name: string;
      readonly address: string;
      readonly rooms: number;
    }
  | {
      readonly kind: 'search';
      readonly query: string;
      readonly hits: readonly { readonly title: string; readonly section: string }[];
    };

/** Which university platform powers a feature, shown as a small source ledger. */
export interface Source {
  readonly platform: string;
  readonly detail: string;
}

export interface Screen {
  readonly image: ImageMetadata;
  readonly alt: string;
  readonly caption: string;
}

export interface Chapter {
  readonly id: string;
  readonly time: ClockTime;
  /** The app tab this moment happens in. */
  readonly tab: string;
  readonly heading: string;
  readonly body: readonly string[];
  readonly moment: Moment;
  readonly sources: readonly Source[];
  readonly screens: readonly [Screen, ...Screen[]];
}

/** One day at Bicocca, in order. Times must increase: the page clock follows them. */
export const day: readonly Chapter[] = [
  {
    id: 'calendar',
    time: '08:10',
    tab: 'Calendar',
    heading: 'Your day, assembled before you are.',
    body: [
      'Lessons, the exams you booked, assignment deadlines, appointments with the segreteria and library seats land on one timeline, coloured by where they came from.',
      'Pinch to zoom the hours. Swipe between days. Pull down and every source syncs again.',
    ],
    moment: {
      kind: 'event',
      tint: 'amber',
      range: '08:30–10:30',
      title: 'Algoritmi e strutture dati',
      type: 'Lecture',
      place: 'U6 · Aula 6',
    },
    sources: [
      { platform: 'EasyStaff', detail: 'Lessons, rooms, last-minute changes' },
      { platform: 'Esse3', detail: 'Booked exams and appointments' },
      { platform: 'Moodle', detail: 'Assignment and quiz deadlines' },
      { platform: 'Affluences', detail: 'Library seat bookings' },
    ],
    screens: [
      { image: calendarWeek, caption: 'Week', alt: 'Calendar week view with colour-coded lectures across seven days' },
      { image: calendarMonth, caption: 'Month, tinted by how busy each day is', alt: 'Calendar month view with a busyness heatmap and the agenda for the selected day' },
    ],
  },
  {
    id: 'elearning',
    time: '10:40',
    tab: 'E-learning',
    heading: 'Moodle, without the browser tabs.',
    body: [
      'A native client for the university’s e-learning platform. Each course opens onto tabs for its content, quizzes, assignments and forums.',
      'Slides open in the built-in viewer. Lecture recordings stream right inside the app.',
    ],
    moment: {
      kind: 'event',
      tint: 'violet',
      range: '10:30–12:30',
      title: 'Programmazione di dispositivi mobili',
      type: 'Lecture',
      place: 'U24 · U24-DISCO-C2',
    },
    sources: [
      { platform: 'Moodle', detail: 'Courses, materials, quizzes, forums' },
      { platform: 'Kaltura', detail: 'Lecture recordings, streamed in-app' },
    ],
    screens: [
      { image: course, caption: 'Course', alt: 'Mobile Programming course page with a lecture recording and tabs' },
      { image: courseContent, caption: 'Materials', alt: 'Course content with expandable sections of forums, PDFs and links' },
      { image: courseQuiz, caption: 'Quizzes', alt: 'Quiz list with a self-assessment test ready to resume' },
    ],
  },
  {
    id: 'maps',
    time: '12:40',
    tab: 'Maps',
    heading: 'Every aula on campus, even with no signal.',
    body: [
      'The campus map ships inside the app as vector tiles, so it works in a basement and needs no Google account.',
      'Search a building or a room, see its seats and whether it is in use, and get directions there.',
    ],
    moment: { kind: 'building', code: 'U1', name: 'Atlas', address: 'Piazza della Scienza, Milano', rooms: 12 },
    sources: [
      { platform: 'Protomaps', detail: 'Campus tiles bundled with the app' },
      { platform: 'MapLibre', detail: 'Rendering, recoloured to your theme' },
      { platform: 'EasyBadge', detail: 'Live room occupancy' },
    ],
    screens: [
      { image: map, caption: 'Campus', alt: 'Dark campus map of Bicocca with building pins' },
      { image: mapBuildings, caption: 'Buildings', alt: 'Directory of campus buildings with directions' },
      { image: mapRooms, caption: 'Rooms in U1', alt: 'Rooms of building U1 Atlas grouped by floor with seat counts' },
    ],
  },
  {
    id: 'attendance',
    time: '14:30',
    tab: 'Services',
    heading: 'Present. One scan.',
    body: [
      'When the attendance code goes up on the projector, scan it from the app.',
      'Every course keeps count of the sessions you attended, so you know where you stand long before the exam.',
    ],
    moment: {
      kind: 'event',
      tint: 'teal',
      range: '14:30–17:30',
      title: 'Sicurezza ed affidabilità',
      type: 'Lecture',
      place: 'U24 · U24-DISCO-C1',
    },
    sources: [{ platform: 'EasyStaff', detail: 'Attendance sessions for each lecture' }],
    screens: [{ image: attendance, caption: 'Attendance by course', alt: 'Attendance list per course with a button to scan the lecture QR code' }],
  },
  {
    id: 'exams',
    time: '16:45',
    tab: 'Services',
    heading: 'The appello opened. You are already in.',
    body: [
      'Every open exam call, written and oral, grouped by course. Book with one tap and keep the confirmation for the day.',
      'The rest of Esse3 lives here too: tuition and PagoPA payments, ISEE, certificates, your study plan, questionnaires and segreteria appointments. A banner keeps count of what is due.',
    ],
    moment: {
      kind: 'exam-call',
      course: 'Analisi matematica',
      code: 'E3102Q100 · 6 appelli',
      dates: [
        { day: 15, month: 'Giu', mode: 'Scritto' },
        { day: 23, month: 'Giu', mode: 'Orale' },
        { day: 1, month: 'Lug', mode: 'Scritto' },
        { day: 7, month: 'Lug', mode: 'Orale' },
      ],
    },
    sources: [
      { platform: 'Esse3', detail: 'Exam calls, bookings, payments, certificates' },
      { platform: 'Affluences', detail: 'Library seats' },
    ],
    screens: [
      { image: bookableExams, caption: 'Book an exam', alt: 'Exam booking screen listing written and oral sessions per course' },
      { image: deadlines, caption: 'Deadlines', alt: 'Timeline of upcoming exam and enrolment deadlines' },
      { image: services, caption: 'All services', alt: 'Services hub grouped into teaching, bookings, documents and payments' },
    ],
  },
  {
    id: 'career',
    time: '18:47',
    tab: 'Profile',
    heading: 'A 28 just landed.',
    body: [
      'Results appear as soon as they are published. Accept or reject them from the list.',
      'Your libretto updates the averages, the credits and the grade chart, and the simulator tells you how the next exam moves your weighted average.',
    ],
    moment: { kind: 'result', grade: 28, course: 'Algebra lineare e geometria', note: 'Published today · to accept' },
    sources: [{ platform: 'Esse3', detail: 'Results, libretto, career' }],
    screens: [
      { image: results, caption: 'Results', alt: 'Archived exam results with grades' },
      { image: profile, caption: 'Card and averages', alt: 'Profile with a digital student card, averages and a grade-trend chart' },
      { image: average, caption: 'What-if average', alt: 'Hypothetical average calculator' },
    ],
  },
  {
    id: 'search',
    time: '21:10',
    tab: 'Search',
    heading: 'Type three letters. Find anything.',
    body: [
      'One search bar covers the whole university: services, courses, exams, quizzes, your career, and actions like booking an exam or editing your study plan.',
      'After dark the app follows your system theme. Or pick a palette of your own, and the map follows along.',
    ],
    moment: {
      kind: 'search',
      query: 'esa',
      hits: [
        { title: 'Esami', section: 'Services' },
        { title: 'Prenota un esame', section: 'Action' },
        { title: 'Esempio d’esame, parte 2', section: 'Quiz · Distributed Systems' },
      ],
    },
    sources: [],
    screens: [
      { image: search, caption: 'Search', alt: 'Search results for exams across services, actions, pages and quizzes' },
      { image: appearance, caption: 'Themes', alt: 'Appearance settings with four theme previews' },
    ],
  },
  {
    id: 'deadline',
    time: '23:39',
    tab: 'E-learning',
    heading: 'Submitted, with twenty minutes to spare.',
    body: [
      'Read the brief, attach the files from your phone and hand the assignment in.',
      'Status, feedback and grade come back to the same screen. The deadline sat on your calendar all week.',
    ],
    moment: {
      kind: 'event',
      tint: 'deadline',
      range: 'Due 23:59',
      title: 'Consegna progetto',
      type: 'Assignment',
      place: 'Distributed Systems',
    },
    sources: [{ platform: 'Moodle', detail: 'Uploads, submission status, feedback' }],
    screens: [{ image: courseAssignment, caption: 'Submission', alt: 'Submitted project assignment with its deadline and status' }],
  },
];
