import { describe, expect, it } from 'vitest';
import { icsFile } from './ics.ts';

const now = Date.UTC(2026, 9, 27, 8, 0);

describe('icsFile', () => {
	it('writes instants in UTC, with the end and escaped text', () => {
		const file = icsFile(
			{
				uid: 'appointment-BK-1@mybicocca',
				title: 'Segreteria studenti; carriere',
				start: Date.UTC(2026, 10, 3, 13, 0),
				end: Date.UTC(2026, 10, 3, 13, 20),
				location: 'Edificio U17, Milano',
				description: null,
				url: null
			},
			now
		);
		expect(file.split('\r\n')).toEqual([
			'BEGIN:VCALENDAR',
			'VERSION:2.0',
			'PRODID:-//MyBicocca//MyBicocca//IT',
			'CALSCALE:GREGORIAN',
			'BEGIN:VEVENT',
			'UID:appointment-BK-1@mybicocca',
			'DTSTAMP:20261027T080000Z',
			'DTSTART:20261103T130000Z',
			'DTEND:20261103T132000Z',
			'SUMMARY:Segreteria studenti\\; carriere',
			'LOCATION:Edificio U17\\, Milano',
			'END:VEVENT',
			'END:VCALENDAR',
			''
		]);
	});

	it('gives a timed event without an end no length', () => {
		const lines = icsFile(
			{ uid: 'exam@mybicocca', title: 'Basi di Dati', start: Date.UTC(2026, 5, 29, 8) },
			now
		).split('\r\n');
		expect(lines).toContain('DTSTART:20260629T080000Z');
		expect(lines.filter((line) => /^(DTEND|DURATION)/.test(line))).toEqual([]);
	});

	it('writes an all-day event when the time is unknown', () => {
		const file = icsFile({ uid: 'x', title: 'Fisica', start: '2026-12-31' }, now);
		expect(file).toContain('DTSTART;VALUE=DATE:20261231\r\nDTEND;VALUE=DATE:20270101\r\n');
	});

	it('folds long lines at 75 octets', () => {
		const file = icsFile(
			{ uid: 'x', title: 'Analisi', start: '2026-01-01', description: `Rigo\n${'à'.repeat(60)}` },
			now
		);
		const description = file.slice(file.indexOf('DESCRIPTION:'), file.indexOf('END:VEVENT'));
		const lines = description.split('\r\n').filter(Boolean);
		expect(lines.length).toBeGreaterThan(1);
		for (const line of lines) expect(new TextEncoder().encode(line).length).toBeLessThanOrEqual(75);
		expect(lines.slice(1).every((line) => line.startsWith(' '))).toBe(true);
		expect(lines.map((line, i) => (i === 0 ? line : line.slice(1))).join('')).toBe(
			`DESCRIPTION:Rigo\\n${'à'.repeat(60)}`
		);
	});
});
