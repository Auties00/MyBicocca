import { describe, expect, it } from 'vitest';
import type { ServiceStatus } from './deadlines.ts';
import { serviceBadge, type BadgeInput } from './hub.ts';

const services: ServiceStatus = {
	pendingResults: 1,
	resultsDue: new Date(2026, 10, 1),
	owedFees: 1,
	overdueFees: 0,
	owedAmount: 612,
	feesDue: new Date(2026, 10, 23),
	bookedExams: 2,
	nextExam: new Date(2026, 10, 4),
	closingBookings: 1
};

const loaded: BadgeInput = { status: 'success', services, missing: [] };

describe('serviceBadge', () => {
	it('flags what waits, once every source has answered', () => {
		expect(serviceBadge('results', loaded)).toEqual({ label: '1 da decidere', tone: 'accent' });
		expect(serviceBadge('fees', loaded)).toEqual({ label: '1 da pagare', tone: 'warning' });
		expect(serviceBadge('exams', loaded)).toEqual({ label: 'In chiusura', tone: 'warning' });
		expect(serviceBadge('library', loaded)).toBeUndefined();
		expect(serviceBadge('fees', { status: 'pending', services: undefined, missing: [] })).toBe(
			undefined
		);
	});

	it('says a service is unchecked when its source is missing, never all clear', () => {
		const partial: BadgeInput = { ...loaded, missing: ['fees', 'bookings'] };
		const unchecked = { label: 'Non verificato', tone: 'neutral' };
		expect(serviceBadge('fees', partial)).toEqual(unchecked);
		expect(serviceBadge('exams', partial)).toEqual(unchecked);
		expect(serviceBadge('results', partial)).toEqual({ label: '1 da decidere', tone: 'accent' });
	});

	it('says so for every flagged service when nothing loaded', () => {
		const failed: BadgeInput = {
			status: 'error',
			services: undefined,
			missing: ['results', 'fees', 'exams', 'bookings']
		};
		expect(serviceBadge('results', failed)?.label).toBe('Non verificato');
		expect(serviceBadge('study-plan', failed)).toBeUndefined();
	});
});
