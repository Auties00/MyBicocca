import { describe, expect, it } from 'vitest';
import { dueTone } from './due.ts';

describe('dueTone', () => {
	it('is red through the due day, amber for the next two days, then quiet', () => {
		expect([-3, 0, 1, 2, 3, 30].map(dueTone)).toEqual([
			'danger',
			'danger',
			'warning',
			'warning',
			'muted',
			'muted'
		]);
	});
});
