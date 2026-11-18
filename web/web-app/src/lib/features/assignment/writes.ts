import type { MoodleWarning } from '@mybicocca/data-api/elearning';
import type { FileUpload } from '@mybicocca/data-api/runtime';
import { DataFailure } from '#lib/data/errors.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { CourseFile } from '#lib/features/course/content.ts';
import { withPlaceholders } from '#lib/features/course/editor/editorHtml.ts';
import { fetchFile, fileMime } from '#lib/features/course/file/opener.svelte.ts';
import { pluginfilePath } from '#lib/features/course/files.ts';
import { ONLINE_TEXT_FILES } from './onlineText.ts';

/** A file of the submission being edited: one already handed in, or one picked now. */
export type DraftFile =
	{ key: string; kind: 'kept'; file: CourseFile } | { key: string; kind: 'picked'; file: File };

/** What a save sends: null leaves that part as Moodle has it. */
export interface SavePlan {
	/**
	 * The online text as Moodle stores it, its own files by `@@PLUGINFILE@@` placeholder. It goes
	 * with every save of an assignment that takes one: Moodle saves each part on every save, and
	 * reads a text left out as an empty one.
	 */
	text: { html: string; format: number } | null;
	/** The whole file area, which a save replaces: kept files go up again with the new ones. */
	files: DraftFile[] | null;
	/** Whether the submission would hold anything: Moodle refuses an empty one. */
	hasContent: boolean;
}

export interface EditedSubmission {
	textEnabled: boolean;
	filesEnabled: boolean;
	/** The online text as Moodle has it, HTML. */
	originalHtml: string | null;
	/** The format Moodle gives that text in. */
	originalFormat: number;
	/** The editor's clean HTML for that text before any edit; '' for none. */
	initialHtml: string;
	/** The editor's clean HTML now; '' when blank. */
	html: string;
	files: readonly DraftFile[];
	/** How many files Moodle has now. */
	originalFileCount: number;
}

const FORMAT_HTML = 1;

function filesChanged(edit: EditedSubmission): boolean {
	return (
		edit.files.some((file) => file.kind === 'picked') ||
		edit.files.length !== edit.originalFileCount
	);
}

/**
 * The text to send: as edited, or when untouched as Moodle stored it, so nothing the editor
 * can't show is lost.
 */
function textOf(edit: EditedSubmission): NonNullable<SavePlan['text']> {
	if (edit.html === '') return { html: '', format: FORMAT_HTML };
	if (edit.html !== edit.initialHtml || edit.originalHtml === null) {
		return { html: withPlaceholders(edit.html, ONLINE_TEXT_FILES), format: FORMAT_HTML };
	}
	return {
		html: withPlaceholders(edit.originalHtml, ONLINE_TEXT_FILES),
		format: edit.originalFormat
	};
}

/**
 * Decides what a save sends. Moodle keeps the file area when no files come (and replaces it when
 * they do), but refuses a save whose parts sent are all empty: so the files go when they changed,
 * or when a blank text can't carry the save alone.
 */
export function planSave(edit: EditedSubmission): SavePlan {
	const textHasContent = edit.textEnabled && edit.html !== '';
	return {
		text: edit.textEnabled ? textOf(edit) : null,
		files: !edit.filesEnabled || (!filesChanged(edit) && textHasContent) ? null : [...edit.files],
		hasContent: textHasContent || (edit.filesEnabled && edit.files.length > 0)
	};
}

export function hasChanges(edit: EditedSubmission): boolean {
	return (
		(edit.textEnabled && edit.html !== edit.initialHtml) ||
		(edit.filesEnabled && filesChanged(edit))
	);
}

/** A step of a save, for the progress line. */
export type SaveStep =
	| { kind: 'prepare' }
	| { kind: 'upload'; index: number; total: number; name: string }
	| { kind: 'save' }
	| { kind: 'submit' };

/**
 * The text's draft area that keeps its file area as it is (Moodle's IGNORE_FILE_MERGE): the text
 * is stored as sent and the pictures it shows by placeholder stay. With a real draft area, Moodle
 * would first drop from it every file the text doesn't link by draft address, then replace the
 * text's files with what is left.
 */
const KEEP_TEXT_FILES = -1;

/** Moodle reports refused saves as warnings on a successful answer. */
function refused(warnings: readonly MoodleWarning[]): void {
	const first = warnings[0];
	if (first === undefined) return;
	throw new DataFailure({
		kind: 'rejected',
		service: 'moodle',
		status: 200,
		code: first.warningCode,
		message: first.message
	});
}

/** A file to upload: one Moodle already has is fetched to go up again. */
async function upload(scope: SessionScope, draft: DraftFile): Promise<FileUpload> {
	if (draft.kind === 'picked') {
		return {
			fileName: draft.file.name,
			...(draft.file.type ? { contentType: draft.file.type } : {}),
			bytes: draft.file
		};
	}
	if (pluginfilePath(draft.file.url) === null) {
		throw new DataFailure({ kind: 'not_found', service: 'moodle' });
	}
	const bytes = await fetchFile(scope, draft.file);
	return {
		fileName: draft.file.name,
		contentType: fileMime(draft.file),
		bytes: bytes as Uint8Array<ArrayBuffer>
	};
}

/**
 * Saves the submission (a draft, or the submission itself without drafts), then submits it for
 * grading when `submit` says so. The files Moodle has are fetched and uploaded again with the new
 * ones, since a save replaces the whole file area.
 */
export async function saveSubmission(
	scope: SessionScope,
	assignId: number,
	plan: SavePlan,
	submit: { acceptStatement: boolean } | null,
	onstep: (step: SaveStep) => void
): Promise<void> {
	if (plan.files?.some((file) => file.kind === 'kept')) onstep({ kind: 'prepare' });
	const files = await Promise.all(
		(plan.files ?? []).map(async (draft) => ({
			file: await upload(scope, draft),
			folder: draft.kind === 'kept' ? draft.file.folder : '/'
		}))
	);
	await scope.api.moodle(async (moodle, auth) => {
		let itemId: number | null = null;
		for (const [index, { file, folder }] of files.entries()) {
			onstep({ kind: 'upload', index, total: files.length, name: file.fileName });
			const [uploaded] = await moodle.files.uploadFile(auth.token, file, {
				itemId: itemId ?? 0,
				filePath: folder
			});
			if (uploaded === undefined) throw new DataFailure({ kind: 'decode', detail: 'upload' });
			itemId = uploaded.itemId;
		}
		/* With no file to upload, the area sent is an empty one: the save clears the files. */
		const filesArea =
			plan.files === null
				? null
				: (itemId ?? (await moodle.draftArea.getUnusedDraftItemid(auth)).itemId);
		onstep({ kind: 'save' });
		refused(
			await moodle.assign.saveSubmission(
				assignId,
				{
					filesFilemanager: filesArea,
					onlinetextEditor:
						plan.text === null
							? null
							: { text: plan.text.html, format: plan.text.format, itemId: KEEP_TEXT_FILES }
				},
				auth
			)
		);
		if (submit !== null) {
			onstep({ kind: 'submit' });
			refused(await moodle.assign.submitForGrading(assignId, submit.acceptStatement, auth));
		}
	});
}

/**
 * Starts a timed assignment's clock, which can't be paused or reset once it runs; one already
 * running counts as started. `untimed` when the site doesn't time assignments, whatever their
 * settings say: there is no clock, and the work can begin.
 */
export async function startSubmission(
	scope: SessionScope,
	assignId: number
): Promise<'started' | 'untimed'> {
	return scope.api.moodle(async (moodle, auth) => {
		const { warnings } = await moodle.assign.startSubmission(assignId, auth);
		const untimed = warnings.some((warning) => warning.warningCode === 'timelimitnotenabled');
		refused(
			warnings.filter(
				(warning) =>
					warning.warningCode !== 'timelimitnotenabled' &&
					warning.warningCode !== 'opensubmissionexists'
			)
		);
		return untimed ? 'untimed' : 'started';
	});
}

/** Submits the saved draft for grading, as it is. */
export async function submitForGrading(
	scope: SessionScope,
	assignId: number,
	acceptStatement: boolean
): Promise<void> {
	await scope.api.moodle(async (moodle, auth) =>
		refused(await moodle.assign.submitForGrading(assignId, acceptStatement, auth))
	);
}

export async function removeSubmission(scope: SessionScope, assignId: number): Promise<void> {
	await scope.api.moodle(async (moodle, auth, user) => {
		const result = await moodle.assign.removeSubmission(user.userId, assignId, auth);
		refused(result.warnings);
		if (!result.status) {
			throw new DataFailure({ kind: 'rejected', service: 'moodle', status: 200, message: '' });
		}
	});
}
