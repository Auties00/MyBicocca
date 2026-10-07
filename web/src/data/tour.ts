import type { ImageMetadata } from 'astro';
import type { AstroComponent } from '@lucide/astro';
import BriefcaseBusiness from '@lucide/astro/icons/briefcase-business';
import CalendarDays from '@lucide/astro/icons/calendar-days';
import GraduationCap from '@lucide/astro/icons/graduation-cap';
import MapIcon from '@lucide/astro/icons/map';
import UserRound from '@lucide/astro/icons/user-round';
import type { Accent } from './accents';

import calendarDay from '@screenshots/Screenshot_Calendar1.png';
import calendarWeek from '@screenshots/Screenshot_Calendar2.png';
import calendarMonth from '@screenshots/Screenshot_Calendar3.png';
import elearningCourses from '@screenshots/Screenshot_Elearning.png';
import elearningCourse from '@screenshots/Screenshot_ElearningCourse.png';
import elearningContent from '@screenshots/Screenshot_ElearningCourseContent.png';
import elearningQuiz from '@screenshots/Screenshot_ElearningCourseQuiz.png';
import elearningForum from '@screenshots/Screenshot_ElearningCourseForum.png';
import elearningAssignment from '@screenshots/Screenshot_ElearningCourseAssignment.png';
import mapCampus from '@screenshots/Screenshot_Map.png';
import mapBuildings from '@screenshots/Screenshot_MapBuildings.png';
import mapBuilding from '@screenshots/Screenshot_MapBuildingU1.png';
import services from '@screenshots/Screenshot_Services.png';
import bookableExams from '@screenshots/Screenshot_BookableExams.png';
import examResults from '@screenshots/Screenshot_ExamResults.png';
import library from '@screenshots/Screenshot_Library.png';
import attendance from '@screenshots/Screenshot_Attendance.png';
import deadlines from '@screenshots/Screenshot_ServicesScadenze.png';
import profile from '@screenshots/Screenshot_Profile.png';
import average from '@screenshots/Screenshot_Average.png';
import career from '@screenshots/Screenshot_Career.png';

export interface TourScreen {
  readonly title: string;
  readonly description: string;
  readonly image: ImageMetadata;
  readonly alt: string;
}

export interface TourChapter {
  /** Stable id, used for anchors and ARIA relationships. */
  readonly id: string;
  readonly label: string;
  readonly title: string;
  readonly summary: string;
  readonly icon: AstroComponent;
  readonly accent: Accent;
  readonly screens: readonly [TourScreen, ...TourScreen[]];
}

export const tour: readonly TourChapter[] = [
  {
    id: 'calendar',
    label: 'Calendar',
    title: 'Your whole week, on one timeline.',
    summary:
      'Lessons from EasyStaff, booked exams, Moodle deadlines, appointments and library reservations all land on the same timeline, colour-coded by source.',
    icon: CalendarDays,
    accent: 'crimson',
    screens: [
      {
        title: 'Day',
        description:
          'A pinch-zoomable hourly timeline. Tap any block for the full detail and a deep link to the related course, assignment or booking.',
        image: calendarDay,
        alt: 'Calendar day view with two colour-coded lectures on an hourly timeline',
      },
      {
        title: 'Week',
        description:
          'Seven days at a glance. The zoom level is shared with the Day view, so the density you choose follows you across both.',
        image: calendarWeek,
        alt: 'Calendar week view with colour-coded lectures across seven days',
      },
      {
        title: 'Month',
        description:
          'A heatmap tints each date by how busy it is, with a draggable agenda sheet for the day you tap. Pull down to re-sync every source.',
        image: calendarMonth,
        alt: 'Calendar month view with a busyness heatmap and an agenda sheet',
      },
    ],
  },
  {
    id: 'elearning',
    label: 'E-learning',
    title: 'A native Moodle client. Not a webview.',
    summary:
      'Browse your courses, dive into materials, hand in assignments, sit quizzes, follow forums and stream lecture recordings, all inside the app.',
    icon: GraduationCap,
    accent: 'violet',
    screens: [
      {
        title: 'Your courses',
        description:
          'Every enrolled course, grouped by academic period and filterable by year or favourites. Enrol in new ones from the in-app catalog.',
        image: elearningCourses,
        alt: 'List of enrolled e-learning courses grouped by academic year',
      },
      {
        title: 'Course detail',
        description:
          'A collapsing header opens onto tabs for info, content, quizzes, assignments and forums, so the whole course lives behind one screen.',
        image: elearningCourse,
        alt: 'Mobile Programming course page with a lecture recording and tabs',
      },
      {
        title: 'Materials',
        description:
          'Sections and folders expand inline, and an in-app viewer opens PDFs, images and text without leaving the app.',
        image: elearningContent,
        alt: 'Course content tab with expandable sections of forums, PDFs and links',
      },
      {
        title: 'Quizzes',
        description: 'Review past attempts, resume one in progress, and read your results and feedback question by question.',
        image: elearningQuiz,
        alt: 'Quiz list with a self-assessment test ready to resume',
      },
      {
        title: 'Forums',
        description: 'Read announcements and discussions, reply, attach files and manage your subscriptions.',
        image: elearningForum,
        alt: 'Course forums with the latest teacher announcement highlighted',
      },
      {
        title: 'Assignments',
        description: 'Read the brief, upload your files and submit, then track status, teacher feedback and your grade.',
        image: elearningAssignment,
        alt: 'Submitted project assignment with its deadline and status',
      },
    ],
  },
  {
    id: 'maps',
    label: 'Maps',
    title: 'The whole campus, even offline.',
    summary:
      'Buildings and rooms render from a bundled vector tileset on a MapLibre engine. No Google Maps key, no network needed, and it recolours itself to match your theme.',
    icon: MapIcon,
    accent: 'mint',
    screens: [
      {
        title: 'Campus map',
        description: 'Tappable pins for every building, rendered from a bundled vector tileset that works without a connection.',
        image: mapCampus,
        alt: 'Dark campus map of Bicocca with building pins U1 to U7',
      },
      {
        title: 'Building directory',
        description: 'Search the whole campus, filter by category, and get one-tap directions to anywhere.',
        image: mapBuildings,
        alt: 'Directory of campus buildings with directions and details',
      },
      {
        title: 'Inside a building',
        description: 'Drill into floors and rooms, with live EasyBadge occupancy schedules for every classroom.',
        image: mapBuilding,
        alt: 'Room list for building U1 Atlas grouped by floor with seat counts',
      },
    ],
  },
  {
    id: 'services',
    label: 'Services',
    title: 'Every Esse3 service, finally in one place.',
    summary:
      'Exams, bookings, documents and payments, grouped the way you think about them and topped by a deadlines banner that always shows what is next.',
    icon: BriefcaseBusiness,
    accent: 'amber',
    screens: [
      {
        title: 'Services hub',
        description: 'Everything administrative, grouped into Teaching, Bookings, Documents and Payments.',
        image: services,
        alt: 'Services hub with deadlines banner and grouped student services',
      },
      {
        title: 'Exam sessions',
        description: 'Browse every open exam call and book your seat in a single tap, with a confirmation to show on the day.',
        image: bookableExams,
        alt: 'Exam booking screen listing written and oral sessions per course',
      },
      {
        title: 'Results',
        description: 'Published grades appear the moment they land, and you can accept or reject each one right from the list.',
        image: examResults,
        alt: 'Archived exam results with grades',
      },
      {
        title: 'Library seats',
        description: 'Reserve a study seat in the campus libraries, powered by Affluences, and manage it from the calendar.',
        image: library,
        alt: 'Library booking screen asking to verify the university email',
      },
      {
        title: 'Attendance',
        description: "Track attendance for every course and mark yourself present by scanning the lecture's QR code.",
        image: attendance,
        alt: 'Attendance list per course with a button to scan the lecture QR code',
      },
      {
        title: 'Deadlines',
        description: 'A chronological timeline of tuition, enrolment and exam dates, with the urgent ones counted up top.',
        image: deadlines,
        alt: 'Timeline of upcoming exam and enrolment deadlines',
      },
    ],
  },
  {
    id: 'career',
    label: 'Career',
    title: 'Know exactly where you stand.',
    summary:
      'A live dashboard of your academic standing: a flippable student card, your headline stats, a grade-trend chart and a simulator for the grade you need.',
    icon: UserRound,
    accent: 'sky',
    screens: [
      {
        title: 'Card & stats',
        description: 'Your student card up top, then arithmetic and weighted average, exams passed and credits earned.',
        image: profile,
        alt: 'Profile with a digital student card, averages and a grade-trend chart',
      },
      {
        title: 'Grade simulator',
        description: 'A what-if calculator that shows how the next exam moves your average, in both arithmetic and weighted modes.',
        image: average,
        alt: 'Hypothetical average calculator',
      },
      {
        title: 'Career',
        description: 'Your full study plan with progress for every year of the degree.',
        image: career,
        alt: 'Career overview split by academic year with credits',
      },
    ],
  },
];
