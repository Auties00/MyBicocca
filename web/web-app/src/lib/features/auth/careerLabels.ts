import IconArrowCircleDown from '~icons/ph/arrow-circle-down-fill';
import IconPauseCircle from '~icons/ph/pause-circle-fill';
import type { CareerLevel, CareerStatus } from '#lib/data/account.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Icon, Status } from '#lib/ui/types.ts';

/** The level pip: "L" or "LM" on the card, the full name for screen readers. */
export function levelLabel(level: CareerLevel | null): { short: string; full: string } | undefined {
	switch (level) {
		case 'bachelor':
			return { short: m.shell_level_bachelor_short(), full: m.shell_level_bachelor() };
		case 'master':
			return { short: m.shell_level_master_short(), full: m.shell_level_master() };
		case null:
			return undefined;
	}
}

/** The chip of a career that is not plainly active, with the account menu's wording and tones. */
export function statusLabel(
	status: CareerStatus
): { label: string; tone: Status; icon?: Icon } | undefined {
	switch (status) {
		case 'suspended':
			return { label: m.shell_career_suspended(), tone: 'warning', icon: IconPauseCircle };
		case 'provisional':
			return { label: m.shell_career_provisional(), tone: 'info' };
		case 'graduated':
			return { label: m.shell_career_graduated(), tone: 'gold' };
		case 'interrupted':
			return { label: m.shell_career_interrupted(), tone: 'neutral', icon: IconArrowCircleDown };
		case 'active':
		case 'other':
			return undefined;
	}
}
