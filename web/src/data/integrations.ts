export interface Integration {
  readonly name: string;
  readonly role: string;
}

/** Platforms MyBicocca talks to on the student's behalf. */
export const integrations: readonly Integration[] = [
  { name: 'Esse3', role: 'Exams & career' },
  { name: 'Moodle', role: 'E-learning' },
  { name: 'EasyStaff', role: 'Timetables' },
  { name: 'Affluences', role: 'Library seats' },
  { name: 'EasyBadge', role: 'Room occupancy' },
  { name: 'Kaltura', role: 'Lecture recordings' },
  { name: 'SPID', role: 'Digital identity' },
  { name: 'CIE', role: 'Electronic ID card' },
];
