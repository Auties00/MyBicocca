import { afterEach, describe, expect, it } from 'vitest';
import { overwriteGetLocale } from '#lib/paraglide/runtime.js';
import { archiveLine, decideByLine, resultFacts, resultStatus, takenOnLine } from './format.ts';
import type { ExamResult } from './model.ts';

function speak(locale: 'it' | 'en') {
	overwriteGetLocale(() => locale);
}

afterEach(() => speak('it'));

const on = (month: number, day: number) => new Date(2026, month - 1, day);

function result(fields: Partial<ExamResult> = {}): ExamResult {
	return {
		id: '901',
		booking: '10-20-1',
		applicationListId: 901,
		title: 'Basi di Dati',
		code: 'E3101Q106',
		callDescription: null,
		credits: null,
		takenOn: on(6, 16),
		bookedOn: null,
		grade: { kind: 'numeric', value: 27, cumLaude: false },
		choice: null,
		note: null,
		deadline: null,
		pending: false,
		daysLeft: null,
		lapsed: false,
		...fields
	};
}

describe('dates after an article', () => {
	it('writes the first of the month as an ordinal and elides before 8 and 11, in Italian', () => {
		expect(decideByLine(on(11, 1))).toBe('Decidi entro il 1° nov');
		expect(decideByLine(on(11, 1), 'long')).toBe('Decidi entro il 1° novembre 2026');
		expect(takenOnLine(on(6, 11))).toBe('Sostenuto l’11 giu 2026');
		expect(takenOnLine(on(10, 8))).toBe('Sostenuto l’8 ott 2026');
		expect(takenOnLine(on(10, 21))).toBe('Sostenuto il 21 ott 2026');
		speak('en');
		expect(decideByLine(on(11, 1))).toBe('Decide by 1 Nov');
	});
});

describe('archiveLine', () => {
	it('leads with the answer, else with an outcome that is a word, else says when it was taken', () => {
		expect(archiveLine(result({ choice: 'accepted' }))).toBe('Accettato · 16 giu 2026');
		expect(archiveLine(result({ grade: { kind: 'absent' }, choice: 'rejected' }))).toBe(
			'Assente · 16 giu 2026'
		);
		expect(archiveLine(result())).toBe('Sostenuto il 16 giu 2026');
		expect(archiveLine(result({ takenOn: null, grade: { kind: 'not_passed' } }))).toBe('Respinto');
	});
});

describe('resultStatus', () => {
	it('puts a pending decision before the answer, a grade left standing, then the outcome', () => {
		expect(resultStatus(result({ pending: true, choice: 'accepted' }))?.label).toBe('Da decidere');
		expect(resultStatus(result({ choice: 'rejected', lapsed: true }))?.status).toBe('danger');
		expect(resultStatus(result({ lapsed: true }))).toEqual({
			status: 'neutral',
			label: 'Non rifiutato'
		});
		expect(resultStatus(result({ grade: { kind: 'withdrew' } }))?.status).toBe('neutral');
		expect(resultStatus(result())).toBeUndefined();
	});
});

describe('resultFacts', () => {
	it('gives the exam’s day only while the subtitle gives the deadline instead', () => {
		const deadline = on(11, 1);
		const pending = resultFacts(result({ pending: true, deadline, credits: 6 }));
		expect(pending.map((fact) => fact.value)).toEqual(['Martedì 16 giugno 2026', '6 CFU']);
		const settled = resultFacts(result({ choice: 'accepted', deadline, bookedOn: on(6, 2) }));
		expect(settled.map((fact) => fact.value)).toEqual(['2 giugno 2026']);
		expect(resultFacts(result({ lapsed: true, deadline }))).toEqual([]);
	});
});
