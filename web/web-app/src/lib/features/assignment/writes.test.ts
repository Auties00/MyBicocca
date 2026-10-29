import { describe, expect, it } from 'vitest';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { CourseFile } from '#lib/features/course/content.ts';
import {
	hasChanges,
	planSave,
	startSubmission,
	type DraftFile,
	type EditedSubmission
} from './writes.ts';

const handedIn = (name: string): DraftFile => ({
	key: name,
	kind: 'kept',
	file: { url: `https://elearning.unimib.it/webservice/pluginfile.php/1/f/${name}` } as CourseFile
});
const picked = (name: string): DraftFile => ({
	key: name,
	kind: 'picked',
	file: new File(['x'], name)
});

const AREA =
	'https://elearning.unimib.it/webservice/pluginfile.php/700/assignsubmission_onlinetext/submissions_onlinetext/51';
const WITH_FIGURE = `<p>Schema:</p><p><img src="${AREA}/figura.png?token=t" alt="schema"></p>`;
const WITH_FIGURE_CLEAN = `<p>Schema:</p><p><img src="${AREA}/figura.png" alt="schema"></p>`;
const STORED = '<p>Schema:</p><p><img src="@@PLUGINFILE@@/figura.png" alt="schema"></p>';

function edit(overrides: Partial<EditedSubmission>): EditedSubmission {
	return {
		textEnabled: true,
		filesEnabled: true,
		originalHtml: null,
		originalFormat: 1,
		initialHtml: '',
		html: '',
		files: [],
		originalFileCount: 0,
		...overrides
	};
}

const drafted = { originalHtml: WITH_FIGURE, initialHtml: WITH_FIGURE_CLEAN };

describe('planSave', () => {
	it('sends edited text as the editor wrote it, its own pictures by placeholder', () => {
		const plan = planSave(
			edit({
				...drafted,
				html: `<p>Schema <strong>nuovo</strong>:</p><p><img src="${AREA}/figura.png" alt="schema"></p>`
			})
		);
		expect(plan.text).toEqual({
			html: '<p>Schema <strong>nuovo</strong>:</p><p><img src="@@PLUGINFILE@@/figura.png" alt="schema"></p>',
			format: 1
		});
	});

	it('sends untouched text as Moodle stored it when only files change, or Moodle would erase it', () => {
		const plan = planSave(
			edit({ ...drafted, html: WITH_FIGURE_CLEAN, files: [picked('relazione.pdf')] })
		);
		expect(plan.text).toEqual({ html: STORED, format: 1 });
		expect(plan.files?.map((file) => file.key)).toEqual(['relazione.pdf']);
	});

	it('keeps the format Moodle gave untouched text', () => {
		const untouched = { ...drafted, html: WITH_FIGURE_CLEAN };
		expect(planSave(edit({ ...untouched, originalFormat: 4 })).text?.format).toBe(4);
		expect(planSave(edit({ ...untouched, originalFormat: 0 })).text?.format).toBe(0);
	});

	it('sends a blank text when there is none, so files alone still save', () => {
		const plan = planSave(edit({ files: [picked('a.pdf')] }));
		expect(plan.text).toEqual({ html: '', format: 1 });
		expect(plan.hasContent).toBe(true);
	});

	it('clears text the student emptied', () => {
		const plan = planSave(
			edit({
				originalHtml: '<p>a</p>',
				initialHtml: '<p>a</p>',
				files: [handedIn('a.pdf')],
				originalFileCount: 1
			})
		);
		expect(plan.text?.html).toBe('');
	});

	it('sends no text where the assignment takes none', () => {
		expect(planSave(edit({ textEnabled: false, files: [picked('a.pdf')] })).text).toBeNull();
	});

	it('leaves unchanged files alone while the text carries the submission', () => {
		const plan = planSave(
			edit({ html: '<p>nuovo</p>', files: [handedIn('a.pdf')], originalFileCount: 1 })
		);
		expect(plan.files).toBeNull();
	});

	it('sends the whole file area when it changed: kept files go up again with the new', () => {
		const plan = planSave(
			edit({ files: [handedIn('a.pdf'), picked('b.pdf')], originalFileCount: 1 })
		);
		expect(plan.files?.map((file) => file.key)).toEqual(['a.pdf', 'b.pdf']);
	});

	it('sends an empty area when every file was removed, so Moodle clears them', () => {
		const plan = planSave(edit({ html: '<p>solo testo</p>', files: [], originalFileCount: 2 }));
		expect(plan.files).toEqual([]);
		expect(plan.hasContent).toBe(true);
	});

	it('sends unchanged files when a blank text can’t carry the save', () => {
		const plan = planSave(
			edit({
				originalHtml: '<p>a</p>',
				initialHtml: '<p>a</p>',
				files: [handedIn('a.pdf')],
				originalFileCount: 1
			})
		);
		expect(plan.files?.map((file) => file.key)).toEqual(['a.pdf']);
	});

	it('has no content with blank text and no files', () => {
		expect(planSave(edit({})).hasContent).toBe(false);
		expect(planSave(edit({ textEnabled: false, html: '<p>ignorato</p>' })).hasContent).toBe(false);
	});
});

describe('hasChanges', () => {
	it('sees edited text and changed files', () => {
		const base = {
			initialHtml: '<p>a</p>',
			html: '<p>a</p>',
			files: [handedIn('a.pdf')],
			originalFileCount: 1
		};
		expect(hasChanges(edit(base))).toBe(false);
		expect(hasChanges(edit({ ...base, html: '<p>b</p>' }))).toBe(true);
		expect(hasChanges(edit({ ...base, files: [] }))).toBe(true);
		expect(hasChanges(edit({ ...base, files: [picked('a.pdf')] }))).toBe(true);
	});
});

describe('startSubmission', () => {
	/** A session whose Moodle answers the start with these warnings. */
	const answering = (...codes: string[]) =>
		({
			api: {
				moodle: (call: (moodle: unknown) => Promise<unknown>) =>
					call({
						assign: {
							startSubmission: async () => ({
								submissionId: 0,
								warnings: codes.map((warningCode) => ({
									item: 'assign',
									itemId: 1,
									warningCode,
									message: warningCode
								}))
							})
						}
					})
			}
		}) as unknown as SessionScope;

	it('starts the clock, or finds it running already', async () => {
		await expect(startSubmission(answering(), 1)).resolves.toBe('started');
		await expect(startSubmission(answering('opensubmissionexists'), 1)).resolves.toBe('started');
	});

	it("tells a site that doesn't time assignments, and passes Moodle's refusals on", async () => {
		await expect(startSubmission(answering('timelimitnotenabled'), 1)).resolves.toBe('untimed');
		await expect(
			startSubmission(answering('submissionnotopen', 'timelimitnotenabled'), 1)
		).rejects.toMatchObject({ error: { code: 'submissionnotopen' } });
	});
});
