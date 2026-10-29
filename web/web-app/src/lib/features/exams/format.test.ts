import { afterEach, describe, expect, it } from 'vitest';
import { overwriteGetLocale } from '#lib/paraglide/runtime.js';
import {
	callState,
	countdown,
	examCountdown,
	locationLine,
	modeLabel,
	ordinal,
	outcomeStatus,
	positionLabel
} from './format.ts';
import type { ExamCall } from './model.ts';

const now = new Date(2026, 5, 18, 10, 30);
const on = (month: number, day: number) => new Date(2026, month - 1, day);

function speak(locale: 'it' | 'en') {
	overwriteGetLocale(() => locale);
}

afterEach(() => speak('it'));

function call(opensAt: Date | null, closesAt: Date | null): ExamCall {
	return {
		slug: '1-2-3',
		key: { courseOfStudyId: 1, activityId: 2, callId: 3 },
		activityChoiceId: 99,
		code: 'E3101Q106',
		title: 'Basi di Dati',
		degree: null,
		description: null,
		date: on(7, 10),
		timed: false,
		opensAt,
		closesAt,
		enrolled: null,
		kind: 'written',
		callType: 'final',
		notes: null,
		president: null,
		bookingMode: null
	};
}

describe('callState', () => {
	it('is open until two days before the window closes', () => {
		speak('en');
		const state = callState(call(on(6, 1), on(6, 22)), now);
		expect(state).toEqual({
			status: 'success',
			label: 'Open',
			long: 'Open',
			window: 'Book by 22 Jun'
		});
	});

	it('is closing within two days, and says when on its own page', () => {
		speak('en');
		expect(callState(call(on(6, 1), on(6, 21)), now).status).toBe('success');
		const closing = callState(call(on(6, 1), on(6, 20)), now);
		expect(closing.status).toBe('warning');
		expect(closing.label).toBe('Closing');
		expect(closing.long).toBe('Closes in 2 days');
		expect(callState(call(on(6, 1), on(6, 19)), now)).toMatchObject({
			long: 'Closes tomorrow',
			window: 'Book by tomorrow'
		});
		expect(callState(call(on(6, 1), on(6, 18)), now)).toMatchObject({
			status: 'danger',
			long: 'Closes today',
			window: 'Last day to book'
		});
	});

	it('says when a window opens, and that a past one is closed', () => {
		speak('en');
		expect(callState(call(on(6, 25), on(7, 5)), now)).toMatchObject({
			status: 'neutral',
			window: 'Opens 25 Jun'
		});
		expect(callState(call(on(6, 1), on(6, 17)), now)).toMatchObject({
			status: 'neutral',
			label: 'Closed'
		});
	});

	it('stays open, with no deadline to give, when the window has no end', () => {
		speak('en');
		expect(callState(call(null, null), now)).toMatchObject({ status: 'success', window: 'Open' });
	});
});

describe('ordinal', () => {
	it('uses English suffixes, teens included', () => {
		speak('en');
		expect([1, 2, 3, 4, 11, 12, 13, 21, 22, 23, 101, 111].map(ordinal)).toEqual([
			'1st',
			'2nd',
			'3rd',
			'4th',
			'11th',
			'12th',
			'13th',
			'21st',
			'22nd',
			'23rd',
			'101st',
			'111th'
		]);
	});

	it('uses the ordinal indicator in Italian', () => {
		expect(ordinal(37)).toBe('37º');
	});
});

describe('positionLabel', () => {
	it('gives the queue’s length only when it covers the position', () => {
		speak('en');
		expect(positionLabel(37, 84)).toBe('37th of 84 booked');
		expect(positionLabel(37, 30)).toBe('37th to book');
		expect(positionLabel(37, null)).toBe('37th to book');
	});
});

describe('countdown', () => {
	it('counts days up to two months ahead', () => {
		speak('en');
		expect(countdown(on(6, 18), now)).toBe('Today');
		expect(countdown(on(6, 19), now)).toBe('Tomorrow');
		expect(countdown(on(6, 26), now)).toBe('In 8 days');
		expect(countdown(on(8, 17), now)).toBe('In 60 days');
		expect(countdown(on(8, 18), now)).toBeUndefined();
		expect(countdown(on(6, 17), now)).toBeUndefined();
	});

	it('names what it counts down to where that could be mistaken', () => {
		speak('en');
		expect(examCountdown(on(7, 7), now)).toBe('Exam in 19 days');
		speak('it');
		expect(examCountdown(on(7, 7), now)).toBe('Esame tra 19 giorni');
		expect(examCountdown(on(6, 19), now)).toBe('Esame domani');
	});
});

describe('outcomeStatus', () => {
	it('waits for publication before telling the grade', () => {
		speak('en');
		const grade = { kind: 'numeric', value: 28, cumLaude: false } as const;
		expect(outcomeStatus({ grade, published: false }).status).toBe('pending');
		expect(outcomeStatus({ grade: { kind: 'unknown' }, published: true }).status).toBe('pending');
		expect(outcomeStatus({ grade, published: true })).toEqual({ status: 'success', label: '28' });
	});

	it('marks honours in gold and a fail as danger', () => {
		speak('en');
		const honours = { kind: 'numeric', value: 30, cumLaude: true } as const;
		expect(outcomeStatus({ grade: honours, published: true }).status).toBe('gold');
		expect(outcomeStatus({ grade: { kind: 'not_passed' }, published: true }).status).toBe('danger');
		expect(outcomeStatus({ grade: { kind: 'absent' }, published: true }).status).toBe('neutral');
	});
});

describe('locationLine', () => {
	it('joins whatever is known', () => {
		expect(locationLine({ room: 'U4-08', place: 'Piazza della Scienza 4' })).toBe(
			'U4-08 · Piazza della Scienza 4'
		);
		expect(locationLine({ room: null, place: 'U6' })).toBe('U6');
		expect(locationLine({ room: null, place: null })).toBeNull();
	});
});

describe('modeLabel', () => {
	it('names the usual ways in the app’s language', () => {
		speak('en');
		expect(modeLabel('Esame in Presenza')).toBe('In person');
		expect(modeLabel('Esame a Distanza')).toBe('Online');
		speak('it');
		expect(modeLabel('ESAME IN PRESENZA')).toBe('In presenza');
	});

	it('keeps any other way in Esse3’s words, in sentence case', () => {
		expect(modeLabel('Esame in Presenza e a Distanza')).toBe('Esame in presenza e a distanza');
		expect(modeLabel(null)).toBeNull();
	});
});
