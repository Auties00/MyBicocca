import { describe, expect, it } from 'vitest';
import {
	decodeMoodleAssignment,
	decodeMoodleSubmissionStatus,
	type MoodleAssignment
} from '@mybicocca/data-api/elearning';
import {
	awaitsStart,
	briefFiles,
	groupAssignments,
	isBlankHtml,
	matchesFilter,
	nextDue,
	nothingOnline,
	onMoodleOnly,
	phaseOf,
	readConfig,
	readSchedule,
	readSubmission,
	spanOf,
	standingOf,
	submittable,
	timedEnd,
	urgencyOf,
	type Schedule,
	type Submission,
	type SubmissionConfig
} from './model.ts';

const HOUR = 3_600_000;
const DAY = 24 * HOUR;
const NOW = Date.UTC(2026, 9, 27, 12);
const s = (ms: number) => Math.floor(ms / 1000);

function assignment(wire: Record<string, unknown> = {}): MoodleAssignment {
	const decoded = decodeMoodleAssignment({ id: 7, cmid: 70, course: 3, name: 'Consegna', ...wire });
	if (decoded === undefined) throw new Error('decode');
	return decoded;
}

function status(wire: Record<string, unknown>) {
	const decoded = decodeMoodleSubmissionStatus(wire);
	if (decoded === undefined) throw new Error('decode');
	return decoded;
}

const file = (name: string, area = 'submission_files') => ({
	area,
	files: [
		{
			filename: name,
			filepath: '/',
			filesize: 1200,
			fileurl: `https://elearning.unimib.it/webservice/pluginfile.php/1/x/${area}/1/${name}`,
			timemodified: s(NOW),
			mimetype: 'application/pdf'
		}
	]
});

describe('readSubmission', () => {
	it('reads a never-submitted attempt, which Moodle lists with status new', () => {
		const result = readSubmission(
			status({
				lastattempt: {
					submission: { id: 1, status: 'new', timemodified: s(NOW), plugins: [] },
					canedit: true
				}
			})
		);
		expect(result).toMatchObject({ stage: 'new', savedAt: null, canEdit: true, files: [] });
	});

	it('takes files from the file plugin only, not the online text’s embedded ones', () => {
		const result = readSubmission(
			status({
				lastattempt: {
					submission: {
						status: 'draft',
						timemodified: s(NOW),
						plugins: [
							{ type: 'file', name: 'File', fileareas: [file('relazione.pdf')] },
							{
								type: 'onlinetext',
								name: 'Testo',
								fileareas: [file('figura.png', 'submissions_onlinetext')],
								editorfields: [{ name: 'onlinetext', text: '<p>Ciao</p>', format: 1 }]
							}
						]
					}
				}
			})
		);
		expect(result.stage).toBe('draft');
		expect(result.files.map((item) => item.name)).toEqual(['relazione.pdf']);
		expect(result.textFormat).toBe(1);
		expect(result.text).toBe('<p>Ciao</p>');
		expect(result.savedAt).toBe(s(NOW) * 1000);
	});

	it('treats blank online text as none', () => {
		const result = readSubmission(
			status({
				lastattempt: {
					submission: {
						status: 'draft',
						plugins: [
							{
								type: 'onlinetext',
								name: 'Testo',
								editorfields: [{ name: 'onlinetext', text: '<p>&nbsp;</p>', format: 1 }]
							}
						]
					}
				}
			})
		);
		expect(result.text).toBeNull();
	});

	it('is graded once feedback is released, with the mark out of the assignment’s maximum', () => {
		const result = readSubmission(
			status({
				lastattempt: { submission: { status: 'submitted', plugins: [] }, graded: true },
				feedback: {
					grade: { grade: '27.00000' },
					gradefordisplay: '27,00&nbsp;/&nbsp;30,00',
					gradeddate: s(NOW),
					plugins: [
						{
							type: 'comments',
							name: 'Commenti',
							editorfields: [{ name: 'comments', text: '<p>Bene</p>', format: 1 }]
						},
						{ type: 'file', name: 'File', fileareas: [file('correzione.pdf', 'feedback_files')] }
					]
				}
			}),
			assignment({ grade: 30 })
		);
		expect(result.stage).toBe('graded');
		expect(result.state).toBe('submitted');
		expect(result.grade).toMatchObject({ value: 27, max: 30, feedback: '<p>Bene</p>' });
		expect(result.grade?.files.map((item) => item.name)).toEqual(['correzione.pdf']);
	});

	it('keeps a scale’s wording instead of its index', () => {
		const result = readSubmission(
			status({
				lastattempt: { submission: { status: 'submitted', plugins: [] } },
				feedback: { grade: { grade: '3.00000' }, gradefordisplay: 'Buono', plugins: [] }
			}),
			assignment({ grade: -4 })
		);
		expect(result.grade).toMatchObject({ value: null, max: null, display: 'Buono' });
	});
});

describe('readSubmission, attempts', () => {
	const reopened = {
		lastattempt: {
			submission: { status: 'reopened', attemptnumber: 1, plugins: [] },
			canedit: true
		},
		previousattempts: [
			{
				attemptnumber: 0,
				grade: { grade: '12.00000', timemodified: s(NOW - DAY) },
				feedbackplugins: [
					{
						type: 'comments',
						name: 'Commenti',
						editorfields: [{ name: 'comments', text: '<p>Rivedi la parte 2</p>', format: 1 }]
					}
				]
			},
			{ attemptnumber: 1, grade: null, feedbackplugins: [] }
		]
	};

	it('reads a reopened attempt as new, numbered, with the last marked one before it', () => {
		const result = readSubmission(status(reopened), assignment({ grade: 30 }));
		expect(result).toMatchObject({ stage: 'new', state: 'new', attempt: 2, savedAt: null });
		expect(result.previous).toMatchObject({
			number: 1,
			grade: { value: 12, max: 30, feedback: '<p>Rivedi la parte 2</p>' }
		});
	});

	it('keeps the submission’s own state beside a grade, for what can still be done', () => {
		const result = readSubmission(
			status({
				lastattempt: { submission: { status: 'draft', plugins: [] }, canedit: true },
				feedback: { grade: { grade: '20.00000' }, plugins: [] }
			}),
			assignment({ grade: 30 })
		);
		expect(result).toMatchObject({ stage: 'graded', state: 'draft', previous: null });
	});
});

describe('readConfig', () => {
	const configs = [
		{ plugin: 'file', subtype: 'assignsubmission', name: 'enabled', value: '1' },
		{ plugin: 'file', subtype: 'assignsubmission', name: 'maxfilesubmissions', value: '2' },
		{ plugin: 'file', subtype: 'assignsubmission', name: 'maxsubmissionsizebytes', value: '0' },
		{
			plugin: 'file',
			subtype: 'assignsubmission',
			name: 'filetypeslist',
			value: 'archive,.odt, .pdf'
		},
		{ plugin: 'comments', subtype: 'assignsubmission', name: 'enabled', value: '1' },
		{ plugin: 'p2ac', subtype: 'assignsubmission', name: 'enabled', value: '1' },
		{ plugin: 'comments', subtype: 'assignfeedback', name: 'enabled', value: '1' }
	];

	it('reads the file rules, falling back to the site’s cap for size', () => {
		const config = readConfig(assignment({ configs }), [], 50_000_000, 'default');
		expect(config.files).toEqual({
			maxFiles: 2,
			maxBytes: 50_000_000,
			types: ['archive', '.odt', '.pdf']
		});
		expect(config.text).toBeNull();
		expect(config.unsupported).toEqual(['p2ac']);
	});

	it('names unsupported parts as the submission does', () => {
		const config = readConfig(
			assignment({ configs }),
			status({
				lastattempt: {
					submission: {
						status: 'new',
						plugins: [{ type: 'p2ac', name: 'Correttore automatico' }]
					}
				}
			}).lastAttempt?.submission?.plugins ?? [],
			null,
			'default'
		);
		expect(config.unsupported).toEqual(['Correttore automatico']);
	});

	it('falls back to the submission’s parts when the settings are missing', () => {
		const plugins =
			status({
				lastattempt: {
					submission: {
						status: 'new',
						plugins: [
							{ type: 'onlinetext', name: 'Testo' },
							{ type: 'comments', name: 'Commenti' }
						]
					}
				}
			}).lastAttempt?.submission?.plugins ?? [];
		const config = readConfig(assignment(), plugins, null, 'default');
		expect(config.text).toEqual({ wordLimit: null });
		expect(config.files).toBeNull();
	});

	it('uses Moodle’s statement, or the default when Moodle sends none', () => {
		expect(
			readConfig(
				assignment({ requiresubmissionstatement: 1, submissionstatement: 'Mio lavoro.' }),
				[],
				null,
				'default'
			).statement
		).toBe('Mio lavoro.');
		expect(
			readConfig(assignment({ requiresubmissionstatement: 1 }), [], null, 'default').statement
		).toBe('default');
		expect(readConfig(assignment(), [], null, 'default').statement).toBeNull();
	});

	it('reads the word limit only when enabled', () => {
		const words = (enabled: string) =>
			readConfig(
				assignment({
					configs: [
						{ plugin: 'onlinetext', subtype: 'assignsubmission', name: 'enabled', value: '1' },
						{ plugin: 'onlinetext', subtype: 'assignsubmission', name: 'wordlimit', value: '300' },
						{
							plugin: 'onlinetext',
							subtype: 'assignsubmission',
							name: 'wordlimitenabled',
							value: enabled
						}
					]
				}),
				[],
				null,
				''
			).text?.wordLimit;
		expect(words('1')).toBe(300);
		expect(words('0')).toBeNull();
	});

	it('reads a time limit only when set', () => {
		expect(readConfig(assignment({ timelimit: 5400 }), [], null, '').timeLimit).toBe(5_400_000);
		expect(readConfig(assignment({ timelimit: 0 }), [], null, '').timeLimit).toBeNull();
	});
});

describe('schedule', () => {
	const due = NOW + 3 * HOUR;

	it('moves the due date and the cut-off to a later extension', () => {
		const schedule = readSchedule(
			assignment({ duedate: s(due), cutoffdate: s(due + DAY) }),
			due + 2 * DAY
		);
		expect(schedule).toEqual({
			opens: null,
			due: s(due + 2 * DAY) * 1000,
			originalDue: s(due) * 1000,
			cutoff: s(due + 2 * DAY) * 1000
		});
	});

	it('places the window: upcoming, open, late until the cut-off, closed', () => {
		const schedule = readSchedule(
			assignment({
				allowsubmissionsfromdate: s(NOW - DAY),
				duedate: s(NOW),
				cutoffdate: s(NOW + DAY)
			})
		);
		expect(phaseOf(schedule, NOW - 2 * DAY)).toBe('upcoming');
		expect(phaseOf(schedule, NOW - HOUR)).toBe('open');
		expect(phaseOf(schedule, NOW + HOUR)).toBe('late');
		expect(phaseOf(schedule, NOW + 2 * DAY)).toBe('closed');
		expect(phaseOf(schedule, NOW - HOUR, false)).toBe('closed');
	});

	it('keeps late submissions open forever without a cut-off, as Moodle does', () => {
		const schedule = readSchedule(assignment({ duedate: s(NOW - 300 * DAY) }));
		expect(phaseOf(schedule, NOW)).toBe('late');
	});

	it('marks overdue only while it can still be handed in', () => {
		const schedule = readSchedule(assignment({ duedate: s(NOW - HOUR) }));
		expect(urgencyOf(schedule, 'new', NOW, true)).toBe('overdue');
		expect(urgencyOf(schedule, 'new', NOW, false)).toBe('calm');
		expect(urgencyOf(schedule, 'submitted', NOW, true)).toBe('calm');
		const soon = readSchedule(assignment({ duedate: s(NOW + 47 * HOUR) }));
		expect(urgencyOf(soon, 'draft', NOW, true)).toBe('soon');
		const later = readSchedule(assignment({ duedate: s(NOW + 49 * HOUR) }));
		expect(urgencyOf(later, 'new', NOW, true)).toBe('calm');
	});

	it('calms a long-gone due date that late submissions never close', () => {
		const open = readSchedule(assignment({ duedate: s(NOW - 20 * DAY) }));
		expect(urgencyOf(open, 'new', NOW, true)).toBe('calm');
		const cut = readSchedule(assignment({ duedate: s(NOW - 20 * DAY), cutoffdate: s(NOW + DAY) }));
		expect(urgencyOf(cut, 'new', NOW, true)).toBe('overdue');
	});
});

describe('groupAssignments', () => {
	it('splits by dates: upcoming soonest first, undated, past most recent first', () => {
		const list = [
			assignment({ id: 1, name: 'Vecchio', duedate: s(NOW - 10 * DAY) }),
			assignment({ id: 2, name: 'Prossimo', duedate: s(NOW + DAY) }),
			assignment({ id: 3, name: 'Libero' }),
			assignment({ id: 4, name: 'Recente', duedate: s(NOW - DAY) }),
			assignment({ id: 5, name: 'In ritardo', duedate: s(NOW - DAY), cutoffdate: s(NOW + DAY) }),
			assignment({ id: 6, name: 'Dopo', duedate: s(NOW + 5 * DAY) })
		];
		const groups = groupAssignments(list, NOW);
		expect(groups.upcoming.map((item) => item.id)).toEqual([5, 2, 6]);
		expect(groups.undated.map((item) => item.id)).toEqual([3]);
		expect(groups.past.map((item) => item.id)).toEqual([4, 1]);
	});
});

describe('spanOf', () => {
	it('keeps the two largest units', () => {
		expect(spanOf(2 * DAY + 4 * HOUR + 30 * 60_000)).toEqual({ days: 2, hours: 4 });
		expect(spanOf(3 * DAY)).toEqual({ days: 3 });
		expect(spanOf(3 * HOUR + 20 * 60_000 + 59_000)).toEqual({ hours: 3, minutes: 20 });
		expect(spanOf(-12 * 60_000)).toEqual({ minutes: 12 });
	});
});

describe('briefFiles', () => {
	it('lists attachments, and intro files the intro doesn’t already show', () => {
		const files = briefFiles(
			assignment({
				intro:
					'<p><img src="https://elearning.unimib.it/webservice/pluginfile.php/1/mod_assign/intro/schema%201.png"></p>',
				introfiles: [file('schema 1.png').files[0], file('dati.csv').files[0]],
				introattachments: [file('traccia.pdf').files[0]]
			}),
			undefined
		);
		expect(files.map((item) => item.name)).toEqual(['traccia.pdf', 'dati.csv']);
	});

	it('takes the attachments from the status when the list has none', () => {
		const files = briefFiles(
			assignment(),
			status({ assignmentdata: { attachments: { intro: [file('traccia.pdf').files[0]] } } })
		);
		expect(files.map((item) => item.name)).toEqual(['traccia.pdf']);
	});
});

describe('isBlankHtml', () => {
	it('sees through empty paragraphs but not pictures', () => {
		expect(isBlankHtml('<p dir="ltr"><br></p>')).toBe(true);
		expect(isBlankHtml('<p><img src="x.png"></p>')).toBe(false);
		expect(isBlankHtml('<p>a</p>')).toBe(false);
	});
});

describe('standing and filters', () => {
	const config: SubmissionConfig = {
		text: null,
		files: { maxFiles: 1, maxBytes: null, types: [] },
		unsupported: [],
		drafts: true,
		statement: null,
		timeLimit: null
	};
	const schedule = (due: number | null, cutoff: number | null = null): Schedule => ({
		opens: null,
		due,
		originalDue: null,
		cutoff
	});
	const submission = (stage: Submission['stage'], canEdit = true): Submission => ({
		stage,
		state: stage === 'graded' ? 'submitted' : stage,
		attempt: 1,
		savedAt: null,
		text: null,
		textFormat: 1,
		files: [],
		grade: null,
		previous: null,
		canEdit,
		canSubmit: false,
		extension: null,
		startedAt: null,
		timeLimit: null,
		plugins: []
	});

	it('says where each assignment stands', () => {
		expect(standingOf(schedule(NOW + DAY * 5), submission('new'), config, NOW)).toBe('todo');
		expect(standingOf(schedule(NOW - DAY, NOW + DAY), submission('new'), config, NOW)).toBe('late');
		expect(standingOf(schedule(NOW - 30 * DAY), submission('new'), config, NOW)).toBe('lapsed');
		expect(standingOf(schedule(NOW - DAY), submission('new', false), config, NOW)).toBe('missed');
		expect(
			standingOf({ ...schedule(NOW + 9 * DAY), opens: NOW + DAY }, submission('new'), config, NOW)
		).toBe('upcoming');
		expect(
			standingOf(schedule(NOW + DAY), submission('new'), { ...config, files: null }, NOW)
		).toBe('none');
		expect(standingOf(schedule(NOW - DAY), submission('graded', false), config, NOW)).toBe(
			'graded'
		);
		expect(standingOf(schedule(NOW + DAY), submission('draft'), config, NOW)).toBe('draft');
	});

	it('takes a timed assignment here once its clock is started', () => {
		const timed = { ...config, timeLimit: HOUR };
		expect(submittable(timed)).toBe(true);
		expect(onMoodleOnly(timed)).toBe(false);
		expect(awaitsStart(timed, submission('new'))).toBe(true);
		expect(awaitsStart(timed, { ...submission('new'), startedAt: NOW - HOUR })).toBe(false);
		expect(awaitsStart(timed, submission('draft'))).toBe(false);
		expect(awaitsStart(timed, submission('new', false))).toBe(false);
		expect(awaitsStart(config, submission('new'))).toBe(false);
		expect(standingOf(schedule(NOW + DAY), submission('new'), timed, NOW)).toBe('todo');
		expect(onMoodleOnly({ ...config, files: null, unsupported: ['p2ac'] })).toBe(true);
		expect(nothingOnline({ ...config, files: null })).toBe(true);
	});

	it("ends a started timed attempt at its limit, the student's own first, or at the due date", () => {
		const timed = { ...config, timeLimit: HOUR };
		const started = { startedAt: NOW, timeLimit: null };
		expect(timedEnd(schedule(NOW + DAY), started, timed)).toBe(NOW + HOUR);
		expect(timedEnd(schedule(NOW + DAY), { ...started, timeLimit: 2 * HOUR }, timed)).toBe(
			NOW + 2 * HOUR
		);
		expect(timedEnd(schedule(NOW + 10 * 60_000), started, timed)).toBe(NOW + 10 * 60_000);
		expect(timedEnd(schedule(null, NOW + 20 * 60_000), started, timed)).toBe(NOW + 20 * 60_000);
		expect(timedEnd(schedule(NOW + DAY), { startedAt: null, timeLimit: null }, timed)).toBeNull();
		expect(timedEnd(schedule(NOW + DAY), started, config)).toBeNull();
	});

	it('filters what is left to do and what is urgent', () => {
		const todo = {
			schedule: schedule(NOW + 5 * DAY),
			standing: 'todo' as const,
			urgency: 'calm' as const
		};
		const soon = {
			schedule: schedule(NOW + HOUR),
			standing: 'draft' as const,
			urgency: 'soon' as const
		};
		const done = {
			schedule: schedule(NOW + HOUR),
			standing: 'submitted' as const,
			urgency: 'calm' as const
		};
		expect(matchesFilter('todo', todo, NOW)).toBe(true);
		expect(matchesFilter('urgent', todo, NOW)).toBe(false);
		expect(matchesFilter('urgent', soon, NOW)).toBe(true);
		expect(matchesFilter('todo', done, NOW)).toBe(false);
		expect(matchesFilter('all', done, NOW)).toBe(true);
	});

	it('guesses from the dates until the status arrives', () => {
		const unknown = (due: number, cutoff: number | null = null) => ({
			schedule: schedule(due, cutoff),
			standing: undefined,
			urgency: 'calm' as const
		});
		expect(matchesFilter('todo', unknown(NOW + DAY), NOW)).toBe(true);
		expect(matchesFilter('todo', unknown(NOW - DAY), NOW)).toBe(false);
		expect(matchesFilter('todo', unknown(NOW - DAY, NOW + DAY), NOW)).toBe(true);
		expect(matchesFilter('urgent', unknown(NOW + HOUR), NOW)).toBe(true);
	});

	it('finds the next deadline among what is still to hand in', () => {
		const rows = [
			{
				id: 'later',
				schedule: schedule(NOW + 9 * DAY),
				standing: 'todo' as const,
				urgency: 'calm' as const
			},
			{
				id: 'done',
				schedule: schedule(NOW + DAY),
				standing: 'submitted' as const,
				urgency: 'calm' as const
			},
			{
				id: 'late',
				schedule: schedule(NOW - DAY, NOW + 2 * DAY),
				standing: 'late' as const,
				urgency: 'overdue' as const
			},
			{
				id: 'closed',
				schedule: schedule(NOW - DAY),
				standing: 'missed' as const,
				urgency: 'calm' as const
			}
		];
		expect(nextDue(rows, NOW)?.id).toBe('late');
		expect(
			nextDue(
				rows.filter((row) => row.id !== 'late'),
				NOW
			)?.id
		).toBe('later');
		expect(nextDue([rows[1]], NOW)).toBeUndefined();
	});
});
