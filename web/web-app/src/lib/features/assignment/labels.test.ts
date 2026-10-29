import { describe, expect, it } from 'vitest';
import { m } from '#lib/paraglide/messages.js';
import {
	countdownOf,
	editorContext,
	lateNote,
	partsText,
	rowStatus,
	stageLabel
} from './labels.ts';
import type { Schedule, Standing, Submission, SubmissionConfig } from './model.ts';

const HOUR = 3_600_000;
const DAY = 24 * HOUR;
const NOW = new Date(2026, 9, 27, 12).getTime();

const config: SubmissionConfig = {
	text: { wordLimit: null },
	files: null,
	unsupported: [],
	drafts: true,
	statement: null,
	timeLimit: null
};
const schedule = (overrides: Partial<Schedule> = {}): Schedule => ({
	opens: null,
	due: NOW + 5 * DAY,
	originalDue: null,
	cutoff: null,
	...overrides
});
const submission = (overrides: Partial<Submission> = {}): Submission => ({
	stage: 'new',
	state: 'new',
	attempt: 1,
	savedAt: null,
	text: null,
	textFormat: 1,
	files: [],
	grade: null,
	previous: null,
	canEdit: true,
	canSubmit: false,
	extension: null,
	startedAt: null,
	timeLimit: null,
	plugins: [],
	...overrides
});

describe('rowStatus', () => {
	it('words each state of a submission', () => {
		expect(
			rowStatus(
				schedule({ due: NOW - DAY }),
				submission({ stage: 'submitted', state: 'submitted', savedAt: NOW - 2 * DAY }),
				config,
				NOW
			)
		).not.toContain(m.assignment_late());
		expect(
			rowStatus(
				schedule({ due: NOW - 3 * DAY }),
				submission({ stage: 'submitted', state: 'submitted', savedAt: NOW - DAY }),
				config,
				NOW
			)
		).toContain(` · ${m.assignment_late()}`);
		expect(
			rowStatus(schedule(), submission({ stage: 'draft', state: 'draft' }), config, NOW)
		).toMatch(new RegExp(`^${m.assignment_draft_saved()} · `));
		expect(
			rowStatus(
				schedule(),
				submission({
					stage: 'graded',
					grade: { value: 27, max: 30, display: null, gradedAt: null, feedback: null, files: [] }
				}),
				config,
				NOW
			)
		).toBe(m.assignment_graded());
	});

	it('says when it opens, or that nothing goes in online', () => {
		expect(
			rowStatus(schedule({ opens: NOW + DAY }), submission({ canEdit: false }), config, NOW)
		).toMatch(/^Apre il /);
		expect(rowStatus(schedule(), submission(), { ...config, text: null, files: null }, NOW)).toBe(
			m.assignment_nothing_online()
		);
	});

	it('tells a late window with a cut-off from one that never closes, and a closed one', () => {
		expect(
			rowStatus(schedule({ due: NOW - DAY, cutoff: NOW + 2 * DAY }), submission(), config, NOW)
		).toMatch(/ · in ritardo fino al /);
		expect(rowStatus(schedule({ due: NOW - DAY }), submission(), config, NOW)).toContain(
			m.assignment_late_open()
		);
		expect(
			rowStatus(schedule({ due: NOW - DAY }), submission({ canEdit: false }), config, NOW)
		).toMatch(/^Non consegnato · chiuso il /);
		expect(rowStatus(schedule({ due: null }), submission(), config, NOW)).toBe(
			m.assignment_no_deadline()
		);
	});
});

describe('stageLabel', () => {
	it('gives every standing its own wording and tone', () => {
		const standings: Standing[] = [
			'graded',
			'submitted',
			'draft',
			'none',
			'upcoming',
			'todo',
			'late',
			'lapsed',
			'missed'
		];
		const labels = Object.fromEntries(
			standings.map((standing) => [standing, stageLabel(standing)])
		);
		expect(labels.todo).toEqual({ label: m.assignment_stage_todo(), tone: 'accent' });
		expect(labels.late).toEqual({ label: m.assignment_stage_late(), tone: 'danger' });
		expect(labels.graded.tone).toBe('success');
		expect(labels.lapsed).toEqual(labels.missed);
	});
});

describe('countdowns', () => {
	it('counts down to the due date, then up from it with the late window', () => {
		expect(countdownOf(schedule({ due: NOW + 2 * HOUR }), submission(), 'open', NOW)).toEqual({
			text: 'Mancano 2 ore'
		});
		const late = countdownOf(
			schedule({ due: NOW - 18 * HOUR, cutoff: NOW + DAY }),
			submission(),
			'late',
			NOW
		);
		expect(late?.text).toBe('Scaduto da 18 ore');
		expect(late?.late).toContain('1 giorno');
		expect(
			countdownOf(schedule(), submission({ stage: 'submitted', state: 'submitted' }), 'open', NOW)
		).toBeNull();
	});

	it('gives the editor the deadline and the hand-in dialog the late notice', () => {
		expect(editorContext(schedule({ due: NOW + 3 * HOUR }), 'open', NOW)).toMatch(
			/ · mancano 3 ore$/
		);
		expect(editorContext(schedule({ due: NOW - HOUR }), 'late', NOW)).toMatch(
			new RegExp(` · ${m.assignment_context_late_open()}$`)
		);
		expect(lateNote(schedule(), 'open')).toBeNull();
		expect(lateNote(schedule({ due: NOW - HOUR }), 'late')).toBe(
			m.assignment_submit_late_note_open()
		);
	});
});

describe('partsText', () => {
	it('lists what to hand in, a line per part', () => {
		expect(
			partsText({
				...config,
				text: { wordLimit: 300 },
				files: { maxFiles: 1, maxBytes: null, types: ['.pdf'] },
				statement: 'Mio lavoro.'
			})
		).toEqual([
			m.assignment_part_text_limit({ limit: 300 }),
			`${m.assignment_editor_limit_files({ count: 1 })} · PDF`,
			m.assignment_part_statement()
		]);
		expect(partsText(config)).toEqual([m.assignment_part_text()]);
	});
});
