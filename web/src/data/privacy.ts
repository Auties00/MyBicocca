import type { Source } from './day';

/** What stays private, written in the same platform/detail ledger as the day's sources. */
export const privacy: readonly Source[] = [
  { platform: 'On device', detail: 'Credentials encrypted, data cached locally' },
  { platform: 'HTTPS', detail: 'Every request, every response' },
  { platform: 'Nobody', detail: 'No trackers, no analytics, no ads' },
  { platform: 'Biometrics', detail: 'Optional fingerprint or face lock' },
  { platform: 'MIT', detail: 'Open source. Read every line.' },
];
