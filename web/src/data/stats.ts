export interface Stat {
  readonly value: string;
  readonly label: string;
}

export const stats: readonly Stat[] = [
  { value: '6+', label: 'university platforms in one place' },
  { value: '4', label: 'tabs between you and everything' },
  { value: '0', label: 'trackers, ads or analytics' },
  { value: '100%', label: 'open source, MIT licensed' },
];
