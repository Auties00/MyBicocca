import { site } from './site';

export interface FaqEntry {
  readonly question: string;
  readonly answer: string;
}

export const faq: readonly FaqEntry[] = [
  {
    question: 'Is MyBicocca an official university app?',
    answer:
      'No. MyBicocca is an unofficial, student-built app and is not affiliated with the University of Milano-Bicocca. It talks to the same public services the university already exposes and brings them together behind one native experience.',
  },
  {
    question: 'Is it free?',
    answer:
      'Yes, completely. MyBicocca is open source under the MIT license, with no ads, no subscriptions and no in-app purchases.',
  },
  {
    question: 'Which devices are supported?',
    answer: `Any Android phone or tablet running Android ${site.minAndroidVersion} or newer. Grab the signed APK from the GitHub Releases page and install it directly.`,
  },
  {
    question: 'What happens to my credentials?',
    answer:
      'They are encrypted on your device and only ever sent to the platforms they belong to. Every request goes over HTTPS, and nothing is shared with third parties: no trackers, no analytics, no data selling.',
  },
  {
    question: 'Does it work without a connection?',
    answer:
      'Yes. MyBicocca is offline-first: your timetable, courses and career are cached on-device, and the campus map renders from a bundled vector tileset, so it all stays available when the network does not.',
  },
  {
    question: 'How do I sign in?',
    answer:
      'With your university credentials, exactly as you would on the web portals. SPID and CIE sign-in are supported too.',
  },
  {
    question: 'I found a bug or have an idea. Where do I report it?',
    answer:
      'Open an issue on GitHub. Bug reports and feature requests both have a template, and pull requests are welcome.',
  },
];
