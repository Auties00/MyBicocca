import { formatNumber } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { LockTimeout } from './timing.ts';

/** "5 min", "1 hour", "4 hours"; the immediate lock has its own wording where it is shown. */
export function timeoutLabel(timeout: LockTimeout): string {
	if (timeout === 0) return m.settings_lock_timeout_immediately();
	return timeout < 60
		? m.settings_lock_timeout_minutes({ count: formatNumber(timeout) })
		: m.settings_lock_timeout_hours({ count: timeout / 60 });
}
