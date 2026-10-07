const repository = 'https://github.com/Auties00/MyBicocca';

/** Site-wide metadata and outbound links. */
export const site = {
  name: 'MyBicocca',
  tagline: 'Your entire university. One app.',
  description:
    'MyBicocca folds every digital service of the University of Milano-Bicocca into one fast, offline-first Android app: timetable, Moodle, campus map, exams and career.',
  locale: 'en',
  links: {
    repository,
    download: `${repository}/releases/latest`,
    issues: `${repository}/issues/new/choose`,
    license: `${repository}/blob/main/LICENSE`,
  },
  minAndroidVersion: '7.1',
  authors: ['Alessandro Autiero', 'Federico Giarrusso', 'Lorenzo Angelo Lupi', 'Alessandro Ferrari'],
} as const;
