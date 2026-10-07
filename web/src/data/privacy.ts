import type { AstroComponent } from '@lucide/astro';
import EyeOff from '@lucide/astro/icons/eye-off';
import KeyRound from '@lucide/astro/icons/key-round';
import LockKeyhole from '@lucide/astro/icons/lock-keyhole';
import ScanFace from '@lucide/astro/icons/scan-face';

export interface PrivacyPoint {
  readonly title: string;
  readonly description: string;
  readonly icon: AstroComponent;
}

export const privacyPoints: readonly PrivacyPoint[] = [
  {
    title: 'Encrypted on your device',
    description: 'Credentials are encrypted locally and only ever sent to the platform they belong to.',
    icon: KeyRound,
  },
  {
    title: 'HTTPS everywhere',
    description: 'Every request and every response travels over an encrypted connection.',
    icon: LockKeyhole,
  },
  {
    title: 'Zero third parties',
    description: 'No trackers, no analytics, no ads, and nobody selling your data. Ever.',
    icon: EyeOff,
  },
  {
    title: 'Biometric app lock',
    description: 'Optionally lock MyBicocca behind your fingerprint or face, so your career stays yours.',
    icon: ScanFace,
  },
];
