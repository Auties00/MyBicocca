import { m } from '#lib/paraglide/messages.js';

/**
 * How loud a deadline reads, the same on every page: overdue and due today in red, the next two
 * days in amber, later ones quiet.
 */
export type DueTone = 'danger' | 'warning' | 'muted';

/** `days` from today to the deadline, in calendar days: negative once it has passed. */
export function dueTone(days: number): DueTone {
	if (days <= 0) return 'danger';
	return days <= 2 ? 'warning' : 'muted';
}

/** "Entro oggi", "Entro domani", "Entro 5 giorni", "In ritardo di 2 giorni". */
export function dueLabel(days: number): string {
	if (days < 0) return m.common_overdue_days({ count: -days });
	if (days === 0) return m.common_due_today();
	if (days === 1) return m.common_due_tomorrow();
	return m.common_due_days({ count: days });
}
