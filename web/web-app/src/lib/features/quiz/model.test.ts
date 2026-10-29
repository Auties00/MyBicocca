import { describe, expect, it } from 'vitest';
import type {
	MoodleCourseModule,
	MoodleQuiz,
	MoodleQuizAttempt
} from '@mybicocca/data-api/elearning';
import {
	asksCheck,
	attemptDeadline,
	closing,
	combinedOverallFeedback,
	groupQuizzes,
	matches,
	nextToClose,
	overallFeedbackShown,
	pageCount,
	quizAction,
	ranOut,
	scaledGrade,
	scheduleOf,
	sectionNumber,
	showsCountdown,
	showsProgress,
	timerPhase,
	verdictCounts
} from './model.ts';

const NOW = new Date(2026, 9, 27, 10, 0);
const at = (date: Date) => Math.floor(date.getTime() / 1000);

function quiz(overrides: Partial<MoodleQuiz> = {}): MoodleQuiz {
	return {
		id: 1,
		courseModule: 100,
		course: 5,
		name: 'Quiz',
		timeOpen: 0,
		timeClose: 0,
		timeLimit: 0,
		attempts: 0,
		sumGrades: 10,
		grade: 30,
		hasQuestions: 1,
		...overrides
	} as MoodleQuiz;
}

function attempt(overrides: Partial<MoodleQuizAttempt> = {}): MoodleQuizAttempt {
	return {
		id: 1,
		attempt: 1,
		state: 'finished',
		preview: 0,
		timeStart: at(NOW) - 600,
		timeCheckState: null,
		...overrides
	} as MoodleQuizAttempt;
}

const module = (id: number, modName = 'quiz') => ({ id, modName }) as MoodleCourseModule;

describe('closing', () => {
	it('reads today, tomorrow, within two weeks in days, then as a date', () => {
		expect(closing(new Date(2026, 9, 27, 18), NOW).kind).toBe('today');
		expect(closing(new Date(2026, 9, 28, 9), NOW).kind).toBe('tomorrow');
		expect(closing(new Date(2026, 10, 10, 9), NOW)).toMatchObject({ kind: 'days', days: 14 });
		expect(closing(new Date(2026, 10, 11, 9), NOW).kind).toBe('date');
	});
});

describe('groupQuizzes', () => {
	const quizzes = [
		quiz({ id: 1, courseModule: 11 }),
		quiz({ id: 2, courseModule: 12 }),
		quiz({ id: 3, courseModule: 13 })
	];

	it('groups by section in the order of the course page, the rest last', () => {
		const groups = groupQuizzes(quizzes, [
			{ key: 'a', ordinal: 1, title: 'Intro', modules: [module(12), module(40, 'resource')] },
			{ key: 'b', ordinal: 2, title: null, modules: [] },
			{ key: 'c', ordinal: 3, title: 'Week 2', modules: [module(11)] }
		]);
		expect(groups.map((group) => [group.key, group.kind, group.ordinal])).toEqual([
			['a', 'section', 1],
			['c', 'section', 3],
			['other', 'other', null]
		]);
		expect(groups[2].items.map((item) => item.quiz.id)).toEqual([3]);
	});

	it('makes one group of everything when no section lists a quiz', () => {
		const groups = groupQuizzes(quizzes, [
			{ key: 'a', ordinal: 1, title: null, modules: [module(40, 'resource')] }
		]);
		expect(groups).toHaveLength(1);
		expect(groups[0].kind).toBe('all');
	});
});

describe('the list of quizzes', () => {
	const open = (id: number, closesIn: number) =>
		({ quiz: quiz({ id, timeClose: at(NOW) + closesIn }), module: undefined }) as const;
	const group = (...items: ReturnType<typeof open>[]) => ({
		key: 'a',
		kind: 'section' as const,
		ordinal: 1,
		title: null,
		items
	});

	it('filters what is left to do and what closes within two days', () => {
		const soon = open(1, 3600);
		const later = open(2, 5 * 86400);
		const closed = open(3, -60);
		expect([soon, later, closed].filter((item) => matches(item, 'todo', NOW))).toEqual([
			soon,
			later
		]);
		expect([soon, later, closed].filter((item) => matches(item, 'soon', NOW))).toEqual([soon]);
	});

	it('picks the quiz closing first, unless the list is short or starts with it', () => {
		const first = open(1, 5 * 86400);
		const next = open(2, 3600);
		const third = open(3, 9 * 86400);
		expect(nextToClose([group(first, next, third)], NOW, first)).toBe(next);
		expect(nextToClose([group(next, first, third)], NOW, next)).toBeUndefined();
		expect(nextToClose([group(first, next)], NOW, first)).toBeUndefined();
	});
});

describe('scheduleOf', () => {
	it('states an opening still ahead, else the closing, and nothing for a quiz always open', () => {
		const opens = at(NOW) + 3600;
		const closes = at(NOW) + 86400;
		expect(scheduleOf(quiz({ timeOpen: opens, timeClose: closes }), NOW)?.kind).toBe('opens');
		expect(scheduleOf(quiz({ timeOpen: at(NOW) - 60, timeClose: closes }), NOW)?.kind).toBe(
			'closes'
		);
		expect(scheduleOf(quiz({ timeClose: at(NOW) - 60 }), NOW)?.kind).toBe('closed');
		expect(scheduleOf(quiz({ timeOpen: at(NOW) - 60 }), NOW)).toBeNull();
	});
});

describe('sectionNumber', () => {
	it("keeps the Content tab's number, unless the title starts with one", () => {
		const section = { kind: 'section' as const, ordinal: 3 };
		expect(sectionNumber({ ...section, title: 'Grafi' })).toBe(3);
		expect(sectionNumber({ ...section, title: null })).toBe(3);
		expect(sectionNumber({ ...section, title: '2. Variabili' })).toBeNull();
		expect(sectionNumber({ kind: 'other', ordinal: null, title: null })).toBeNull();
	});
});

describe('showsProgress', () => {
	it('needs two quizzes, at least half of them tracked', () => {
		expect(showsProgress({ done: 1, tracked: 1, total: 1 })).toBe(false);
		expect(showsProgress({ done: 0, tracked: 2, total: 4 })).toBe(true);
		expect(showsProgress({ done: 0, tracked: 1, total: 4 })).toBe(false);
	});
});

describe('quizAction', () => {
	it('resumes an open attempt even after the quiz closed', () => {
		const open = attempt({ state: 'inprogress', attempt: 2 });
		const closed = quiz({ timeClose: at(NOW) - 60 });
		expect(quizAction(closed, [attempt(), open], NOW)).toEqual({ kind: 'resume', attempt: open });
	});

	it('blocks before opening, after closing, and once attempts run out', () => {
		expect(quizAction(quiz({ timeOpen: at(NOW) + 60 }), [], NOW)).toMatchObject({
			reason: 'upcoming'
		});
		expect(quizAction(quiz({ timeClose: at(NOW) - 60 }), [], NOW)).toMatchObject({
			reason: 'closed'
		});
		expect(quizAction(quiz({ attempts: 1 }), [attempt()], NOW)).toMatchObject({
			reason: 'exhausted'
		});
	});

	it('leaves previews out of the attempts used and numbers the next one', () => {
		const used = [attempt({ attempt: 1 }), attempt({ attempt: 2, preview: 1 })];
		expect(quizAction(quiz({ attempts: 2 }), used, NOW)).toEqual({ kind: 'start', number: 2 });
	});

	it('follows Moodle when it refuses access or a new attempt', () => {
		const access = { canAttempt: true, preventAccessReasons: ['Password'] };
		expect(quizAction(quiz(), [], NOW, access)).toMatchObject({ reason: 'denied' });
		const wait = {
			endtime: null,
			ispreflightcheckrequired: true,
			preventnewattemptreasons: ['Potrai iniziare un nuovo tentativo dopo le 11:00']
		};
		expect(quizAction(quiz(), [attempt()], NOW, undefined, wait)).toMatchObject({
			reason: 'denied'
		});
	});

	it('sends an open attempt that asks for a check, such as a password, to the site', () => {
		const open = attempt({ state: 'inprogress' });
		const check = { endtime: null, ispreflightcheckrequired: true, preventnewattemptreasons: [] };
		expect(quizAction(quiz(), [open], NOW, undefined, check)).toEqual({
			kind: 'site',
			attempt: open
		});
		expect(
			quizAction(quiz(), [open], NOW, undefined, { ...check, ispreflightcheckrequired: false })
		).toEqual({ kind: 'resume', attempt: open });
	});

	it('reads the check as about an attempt only while it is open', () => {
		const check = { ispreflightcheckrequired: true };
		expect(asksCheck(attempt({ state: 'inprogress' }), check)).toBe(true);
		expect(asksCheck(attempt({ state: 'finished' }), check)).toBe(false);
		expect(asksCheck(undefined, check)).toBe(false);
	});
});

describe('grades and pages', () => {
	it('puts raw marks on the quiz scale', () => {
		expect(scaledGrade(7.5, quiz({ sumGrades: 10, grade: 30 }))).toBe(22.5);
		expect(scaledGrade(7.5, quiz({ sumGrades: 0 }))).toBe(7.5);
		expect(scaledGrade(null, quiz())).toBeNull();
	});

	it('counts the page breaks of a layout', () => {
		expect(pageCount('1,2,0,3,0')).toBe(2);
		expect(pageCount('')).toBeNull();
	});
});

describe('attempt timing', () => {
	const start = at(NOW) - 600;

	it("ends when Moodle's access rules say, and until they answer, at its check time", () => {
		const record = { timeCheckState: start + 1800 };
		expect(attemptDeadline(record, { endtime: start + 3600 })).toBe((start + 3600) * 1000);
		expect(attemptDeadline(record, undefined)).toBe((start + 1800) * 1000);
		expect(attemptDeadline(record, { endtime: 0 })).toBeNull();
		expect(attemptDeadline({ timeCheckState: null }, undefined)).toBeNull();
	});

	it('tells an attempt in progress whose time Moodle says has run out', () => {
		const open = { state: 'inprogress', timeCheckState: start + 1800 } as const;
		const now = NOW.getTime();
		expect(ranOut(open, { endtime: at(NOW) - 1 }, now)).toBe(true);
		expect(ranOut(open, { endtime: at(NOW) + 60 }, now)).toBe(false);
		expect(ranOut(open, { endtime: 0 }, now)).toBe(false);
		expect(ranOut({ ...open, state: 'overdue' }, { endtime: at(NOW) - 1 }, now)).toBe(false);
	});

	it('shows an untimed countdown only in the last hour', () => {
		const now = NOW.getTime();
		expect(showsCountdown({ timeLimit: 0 }, now + 2 * 3600_000, now)).toBe(false);
		expect(showsCountdown({ timeLimit: 0 }, now + 1800_000, now)).toBe(true);
		expect(showsCountdown({ timeLimit: 600 }, now + 2 * 3600_000, now)).toBe(true);
	});

	it('warns from five minutes and alarms in the last one', () => {
		expect(timerPhase(6 * 60_000)).toBe('calm');
		expect(timerPhase(5 * 60_000)).toBe('warning');
		expect(timerPhase(59_000)).toBe('danger');
		expect(timerPhase(0)).toBe('expired');
	});
});

describe('overallFeedbackShown', () => {
	const now = NOW.getTime();
	const finished = { state: 'finished', timeFinish: at(NOW) - 3600 } as const;
	const withFeedback = (overrides: Partial<MoodleQuiz> = {}) =>
		quiz({ hasFeedback: 1, reviewOverallFeedback: 0x01110, timeClose: 0, ...overrides });

	it('shows nothing for a quiz without feedback, or before an attempt is finished', () => {
		expect(overallFeedbackShown(quiz({ hasFeedback: 0 }), [finished], now)).toBe(false);
		expect(overallFeedbackShown(withFeedback(), [], now)).toBe(false);
	});

	it('follows the review settings for the time each attempt is in', () => {
		const afterClose = withFeedback({ reviewOverallFeedback: 0x00010 });
		expect(overallFeedbackShown(afterClose, [finished], now)).toBe(false);
		expect(overallFeedbackShown({ ...afterClose, timeClose: at(NOW) - 60 }, [finished], now)).toBe(
			true
		);
		const immediately = withFeedback({ reviewOverallFeedback: 0x01000 });
		expect(
			overallFeedbackShown(immediately, [{ ...finished, timeFinish: at(NOW) - 60 }], now)
		).toBe(true);
		expect(overallFeedbackShown(immediately, [finished], now)).toBe(false);
	});

	it('hides it while an attempt is in progress, whatever the settings', () => {
		const open = { state: 'inprogress', timeFinish: 0 } as const;
		expect(overallFeedbackShown(withFeedback(), [finished, open], now)).toBe(false);
		expect(
			overallFeedbackShown(withFeedback({ reviewOverallFeedback: null }), [finished, open], now)
		).toBe(false);
	});

	it("needs every attempt to allow it, and takes Moodle's default when the settings are hidden", () => {
		const later = withFeedback({ reviewOverallFeedback: 0x00100 });
		expect(
			overallFeedbackShown(later, [finished, { ...finished, timeFinish: at(NOW) - 30 }], now)
		).toBe(false);
		expect(
			overallFeedbackShown(withFeedback({ reviewOverallFeedback: null }), [finished], now)
		).toBe(true);
	});
});

describe('overallFeedbackShown with combined review options', () => {
	const now = NOW.getTime();
	const finished = { state: 'finished', timeFinish: at(NOW) - 3600 } as const;
	const open = { state: 'inprogress', timeFinish: 0 } as const;
	const hidden = quiz({ hasFeedback: 1, reviewOverallFeedback: null, timeClose: 0 });

	it("takes Moodle's answer when the settings are hidden and it is the same for every attempt", () => {
		expect(overallFeedbackShown(hidden, [finished], now, { some: true, all: true })).toBe(true);
		expect(overallFeedbackShown(hidden, [finished], now, { some: false, all: false })).toBe(false);
	});

	it('falls back to the default rule for each attempt when only some allow it', () => {
		const mixed = { some: true, all: false };
		expect(overallFeedbackShown(hidden, [finished], now, mixed)).toBe(true);
		expect(overallFeedbackShown(hidden, [finished, open], now, mixed)).toBe(false);
	});

	it('ignores it when the quiz states its own settings', () => {
		const stated = quiz({ hasFeedback: 1, reviewOverallFeedback: 0x00010, timeClose: 0 });
		expect(overallFeedbackShown(stated, [finished], now, { some: true, all: true })).toBe(false);
	});
});

describe('combinedOverallFeedback', () => {
	it('reads the overall-feedback option of both lists', () => {
		const option = (name: string, value: number) => ({ name, value });
		expect(
			combinedOverallFeedback({
				someoptions: [option('feedback', 1), option('overallfeedback', 1)],
				alloptions: [option('feedback', 1), option('overallfeedback', 0)]
			})
		).toEqual({ some: true, all: false });
	});
});

describe('verdictCounts', () => {
	it('counts manual and automatic grading alike and skips descriptions', () => {
		expect(
			verdictCounts(['gradedright', 'mangrright', 'gradedpartial', 'gaveup', 'needsgrading', null])
		).toEqual({ correct: 2, partial: 1, wrong: 0, unanswered: 1, pending: 1 });
	});
});
