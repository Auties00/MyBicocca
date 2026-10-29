import IconCalendar from '~icons/ph/calendar-blank';
import IconCalendarFill from '~icons/ph/calendar-blank-fill';
import IconGraduationCap from '~icons/ph/graduation-cap';
import IconGraduationCapFill from '~icons/ph/graduation-cap-fill';
import IconMap from '~icons/ph/map-trifold';
import IconMapFill from '~icons/ph/map-trifold-fill';
import IconBriefcase from '~icons/ph/briefcase';
import IconBriefcaseFill from '~icons/ph/briefcase-fill';
import IconUserCircle from '~icons/ph/user-circle';
import IconUserCircleFill from '~icons/ph/user-circle-fill';
import IconGear from '~icons/ph/gear-six';
import IconGearFill from '~icons/ph/gear-six-fill';
import { m } from '#lib/paraglide/messages.js';
import type { Icon } from './types.ts';

/*
 * The app's six destinations: the shell's navigation lists them, and the sign-in page presents
 * the primary ones. Outside the shell, so a signed-out page doesn't load the shell for them.
 */
export type DestinationId = 'calendar' | 'elearning' | 'map' | 'services' | 'profile' | 'settings';

/** A destination's app path, without the leading slash, as `resolve` takes it. */
export type DestinationPath = DestinationId;

export interface Destination {
	id: DestinationId;
	path: DestinationPath;
	label: () => string;
	icon: Icon;
	/** Filled variant for the selected item. */
	iconActive: Icon;
	/** Second key of the `g` chord. */
	key: string;
}

/** The four primary destinations, in the same order at every size. */
export const primaryDestinations: readonly Destination[] = [
	{
		id: 'calendar',
		path: 'calendar',
		label: m.shell_calendar,
		icon: IconCalendar,
		iconActive: IconCalendarFill,
		key: 'c'
	},
	{
		id: 'elearning',
		path: 'elearning',
		label: m.shell_elearning,
		icon: IconGraduationCap,
		iconActive: IconGraduationCapFill,
		key: 'e'
	},
	{
		id: 'map',
		path: 'map',
		label: m.shell_map,
		icon: IconMap,
		iconActive: IconMapFill,
		key: 'm'
	},
	{
		id: 'services',
		path: 'services',
		label: m.shell_services,
		icon: IconBriefcase,
		iconActive: IconBriefcaseFill,
		key: 's'
	}
];

/** Under the primaries in the sidebar; in the account menu on smaller screens. */
export const secondaryDestinations: readonly Destination[] = [
	{
		id: 'profile',
		path: 'profile',
		label: m.shell_profile,
		icon: IconUserCircle,
		iconActive: IconUserCircleFill,
		key: 'p'
	},
	{
		id: 'settings',
		path: 'settings',
		label: m.shell_settings,
		icon: IconGear,
		iconActive: IconGearFill,
		key: ','
	}
];

export const allDestinations: readonly Destination[] = [
	...primaryDestinations,
	...secondaryDestinations
];
