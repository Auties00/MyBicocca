const repository = 'https://github.com/Auties00/MyBicocca';

/** Site-wide metadata and outbound links, shared by the layout, header and footer. */
export const site = {
  name: 'MyBicocca',
  tagline: 'Your entire university. One app.',
  description:
    'MyBicocca folds every digital service of the University of Milano-Bicocca into a single, fast, offline-first Android app: calendar, e-learning, campus maps, exams and more.',
  themeColor: '#0c0709',
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

export interface NavLink {
  readonly label: string;
  readonly href: `#${string}`;
}

export const navLinks: readonly NavLink[] = [
  { label: 'Features', href: '#features' },
  { label: 'Tour', href: '#tour' },
  { label: 'Privacy', href: '#privacy' },
  { label: 'FAQ', href: '#faq' },
];
