import type { ImageMetadata } from 'astro';
import type { AstroComponent } from '@lucide/astro';
import Armchair from '@lucide/astro/icons/armchair';
import ChartLine from '@lucide/astro/icons/chart-line';
import CloudOff from '@lucide/astro/icons/cloud-off';
import FileText from '@lucide/astro/icons/file-text';
import Fingerprint from '@lucide/astro/icons/fingerprint-pattern';
import Palette from '@lucide/astro/icons/palette';
import Play from '@lucide/astro/icons/play';
import ScanQrCode from '@lucide/astro/icons/scan-qr-code';
import Search from '@lucide/astro/icons/search';
import UsersRound from '@lucide/astro/icons/users-round';
import type { Accent } from './accents';

import searchScreen from '@screenshots/Screenshot_Search.png';
import appearanceScreen from '@screenshots/Screenshot_SettingsAppearance.png';

export interface Highlight {
  readonly title: string;
  readonly description: string;
  readonly icon: AstroComponent;
  readonly accent: Accent;
  /** Featured cards span two columns and rows and show a cropped screenshot. */
  readonly media?: { readonly image: ImageMetadata; readonly alt: string };
}

export const highlights: readonly Highlight[] = [
  {
    title: 'Search everything',
    description:
      'One bar for courses, exams, pages, quizzes and quick actions. Type "esami" and book one without hunting through menus.',
    icon: Search,
    accent: 'crimson',
    media: { image: searchScreen, alt: 'Unified search results for exams, pages, quizzes and career entries' },
  },
  {
    title: 'Offline-first',
    description: 'Timetable, courses and career are cached on-device, so they are there even when the network is not.',
    icon: CloudOff,
    accent: 'mint',
  },
  {
    title: 'SPID & CIE',
    description: 'Sign in with your university credentials, SPID or your electronic ID card.',
    icon: Fingerprint,
    accent: 'sky',
  },
  {
    title: 'Lecture recordings',
    description: 'Stream Kaltura and HLS recordings in a native player, right next to the slides.',
    icon: Play,
    accent: 'violet',
  },
  {
    title: 'Built-in viewer',
    description: 'PDFs, images and text open in-app. Office files hand off with a single tap.',
    icon: FileText,
    accent: 'amber',
  },
  {
    title: 'QR attendance',
    description: "Scan the lecture's code to mark yourself present and track every course's attendance.",
    icon: ScanQrCode,
    accent: 'lime',
  },
  {
    title: 'Library seats',
    description: 'Reserve a study seat through Affluences and see the booking on your calendar.',
    icon: Armchair,
    accent: 'amber',
  },
  {
    title: 'Make it yours',
    description:
      'Four palettes (MyBicocca, Material You, Ocean and Forest) in light, dark or system mode. Even the map recolours to match.',
    icon: Palette,
    accent: 'violet',
    media: { image: appearanceScreen, alt: 'Appearance settings with four theme previews' },
  },
  {
    title: 'Grade simulator',
    description: 'See how the next exam moves your arithmetic and weighted average before you sit it.',
    icon: ChartLine,
    accent: 'sky',
  },
  {
    title: 'Multiple accounts',
    description: 'Switch between careers or accounts in a tap, each with its own cached data.',
    icon: UsersRound,
    accent: 'mint',
  },
];
