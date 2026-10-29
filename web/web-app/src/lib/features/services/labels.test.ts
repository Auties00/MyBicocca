import { afterAll, beforeEach, describe, expect, it } from 'vitest';
import { overwriteGetLocale } from '#lib/paraglide/runtime.js';
import type { Deadline } from './deadlines.ts';
import { deadlineDetail, deadlineWhen, staleLine, summaryLine } from './labels.ts';

type Locale = 'it' | 'en';
let locale: Locale = 'it';
overwriteGetLocale(() => locale);
afterAll(() => overwriteGetLocale(() => 'it'));
beforeEach(() => {
	locale = 'it';
});

const on = (days: number) => new Date(2026, 9, 27 + days);

/** Dates never break across lines: "8 nov" holds a no-break space. */
const nb = (text: string) => text.replaceAll('_', '\u00a0');

const base = (days: number) => ({
	id: `${days}`,
	date: on(days),
	days,
	urgent: days <= 7,
	title: null
});

const result = (days: number): Deadline => ({
	...base(days),
	source: 'result',
	applicationListId: 900,
	grade: { kind: 'numeric', value: 27, cumLaude: false }
});

const fee = (days: number, expired = false): Deadline => ({
	...base(days),
	urgent: expired || days <= 7,
	source: 'fee',
	invoiceId: 1,
	amount: 612,
	expired,
	installment: null,
	academicYear: null
});

const key = { courseOfStudyId: 1, activityId: 2, callId: 3 };

const exam = (days: number, room: string | null = null): Deadline => ({
	...base(days),
	source: 'exam',
	key,
	time: new Date(2026, 9, 27 + days, 9, 30),
	room
});

const booking = (days: number, callDate: Date | null): Deadline => ({
	...base(days),
	source: 'booking',
	key,
	code: null,
	callDate,
	opensAt: null
});

describe('deadlineWhen', () => {
	it.each<[Deadline, string, string]>([
		[exam(0), 'Oggi', 'Today'],
		[exam(1), 'Domani', 'Tomorrow'],
		[exam(8), 'Tra 8 giorni', 'In 8 days'],
		[result(0), 'Entro oggi', 'Due today'],
		[result(1), 'Entro domani', 'Due tomorrow'],
		[booking(2, null), 'Entro 2 giorni', 'Due in 2 days'],
		[fee(-3, true), 'In ritardo di 3 giorni', '3 days overdue'],
		[fee(0, true), 'In ritardo', 'Overdue'],
		[fee(-1), 'In ritardo di 1 giorno', '1 day overdue']
	])('words a deadline by what it is (%#)', (item, italian, english) => {
		expect(deadlineWhen(item)).toBe(italian);
		locale = 'en';
		expect(deadlineWhen(item)).toBe(english);
	});
});

describe('deadlineDetail', () => {
	it('keeps a room code on one line', () => {
		expect(deadlineDetail(exam(3, 'U4-08'))).toBe('Ore 09:30 · U4‑08');
	});

	it('elides the Italian article before the 1st, 8th and 11th', () => {
		expect(deadlineDetail(booking(2, new Date(2026, 10, 8)))).toBe(
			nb('Chiusura iscrizioni · appello dell’8_nov')
		);
		expect(deadlineDetail(booking(2, new Date(2026, 10, 9)))).toBe(
			nb('Chiusura iscrizioni · appello del 9_nov')
		);
		locale = 'en';
		expect(deadlineDetail(booking(2, new Date(2026, 10, 8)))).toBe(
			nb('Booking closes · exam on 8_Nov')
		);
	});
});

describe('summaryLine', () => {
	const done = { status: 'success', complete: true } as const;
	const partial = { status: 'success', complete: false, missing: ['bookings'] } as const;

	it.each<[Parameters<typeof summaryLine>[0], string]>([
		[{ status: 'pending', complete: false, deadlines: undefined }, 'Caricamento…'],
		[{ status: 'error', complete: false, deadlines: undefined }, 'Sincronizzazione non riuscita'],
		[{ ...done, deadlines: [] }, 'Nessuna scadenza imminente'],
		[{ ...partial, deadlines: [] }, 'Sincronizzazione non riuscita'],
		[{ ...done, deadlines: [exam(12)] }, 'Prossima l’8_nov'],
		[{ ...done, deadlines: [fee(-2, true), result(5), exam(12)] }, '2 urgenti · Prossima l’1_nov'],
		[{ ...done, deadlines: [fee(-2, true)] }, '1 urgente · Scaduta il_25_ott'],
		[{ ...partial, deadlines: [exam(13)] }, 'Prossima il_9_nov · alcune non caricate'],
		[
			{ ...partial, deadlines: [result(5), exam(13)] },
			'1 urgente · Prossima l’1_nov · alcune non caricate'
		]
	])('sums the deadlines up (%#)', (input, line) => {
		expect(summaryLine(input)).toBe(nb(line));
	});

	it('is localized', () => {
		locale = 'en';
		expect(summaryLine({ ...done, deadlines: [fee(-2, true), result(5)] })).toBe(
			nb('2 urgent · Next on_1_Nov')
		);
		expect(summaryLine({ ...partial, deadlines: [exam(13)] })).toBe(
			nb('Next on_9_Nov · some couldn’t load')
		);
	});
});

describe('staleLine', () => {
	const now = new Date(2026, 9, 27, 15, 0);
	const ago = (minutes: number) => now.getTime() - minutes * 60_000;

	it('tells how old the data is', () => {
		expect(staleLine(ago(180), now)).toBe('Non aggiornate · dati di 3 ore fa');
		expect(staleLine(ago(24 * 60), now)).toBe('Non aggiornate · dati di ieri');
		locale = 'en';
		expect(staleLine(ago(12), now)).toBe('Out of date · data from 12 minutes ago');
	});

	it('only tells the failure when the data is from this minute', () => {
		expect(staleLine(ago(0.5), now)).toBe('Aggiornamento non riuscito');
		expect(staleLine(now.getTime() + 5_000, now)).toBe('Aggiornamento non riuscito');
	});
});
