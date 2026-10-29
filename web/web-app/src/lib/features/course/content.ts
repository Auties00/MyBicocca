import type {
	MoodleCourseModule,
	MoodleCourseSection,
	MoodleModuleContent
} from '@mybicocca/data-api/elearning';
import { stripMlang } from '#lib/features/elearning/mlang.ts';
import { htmlToLine, htmlToText, isRenderableHtml } from '#lib/format/html.ts';

/** Bodyless labels up to this long head the activities after them. */
const HEADER_MAX_CHARS = 60;

const KALVIDRES = /\/mod\/kalvidres\/view\.php\?(?:[^#]*&)?id=(\d+)/i;

export type ContentBlock =
	| { kind: 'group'; key: string; title: string | null; modules: MoodleCourseModule[] }
	| { kind: 'note'; key: string; html: string }
	| {
			kind: 'subsection';
			key: string;
			title: string;
			summary: string | null;
			blocks: ContentBlock[];
	  };

/** A section that earns a card on the Content tab. */
export interface OutlineSection {
	section: MoodleCourseSection;
	/** 1-based, over the cards shown. */
	ordinal: number;
	/** Null when the teacher left it unnamed: the page says "Section N". */
	title: string | null;
	summary: string | null;
	blocks: ContentBlock[];
	counts: TypeCount[];
}

export type CountKind =
	'video' | 'pdf' | 'file' | 'quiz' | 'assign' | 'forum' | 'link' | 'folder' | 'page' | 'other';

export interface TypeCount {
	kind: CountKind;
	count: number;
}

export type Family = 'media' | 'document' | 'interactive' | 'neutral';

/** A file a course offers, as the file viewer and the download routing take it. */
export interface CourseFile {
	/** Moodle's address, without any token. */
	url: string;
	name: string;
	/** The folder inside the activity, `/` at its root. */
	folder: string;
	mime: string | null;
	size: number | null;
	/** Epoch milliseconds. */
	modified: number | null;
}

/** Where opening an activity leads. */
export type ModuleTarget =
	| { kind: 'quiz'; id: number }
	| { kind: 'assign'; id: number }
	| { kind: 'forum'; id: number }
	| { kind: 'video'; cmId: number }
	| { kind: 'file'; file: CourseFile }
	| { kind: 'folder'; files: CourseFile[] }
	| { kind: 'link'; url: string }
	| { kind: 'external'; url: string }
	| { kind: 'none' };

/** A name without the copy suffixes Moodle adds, "(copia)" and "(copy)", which teachers leave. */
export function cleanName(name: string): string {
	return name.replace(/(?:\s*\((?:copia|copy)\))+\s*$/i, '').trim();
}

/** A name as shown: multilang resolved, copy suffixes dropped, tags gone. */
export function displayName(name: string, language: string): string {
	const resolved = stripMlang(name, language);
	return cleanName(/[<&]/.test(resolved) ? htmlToLine(resolved) : resolved);
}

/** The cmId of the Kaltura video an activity plays: a Kaltura resource, or a link to one. */
export function kalturaCmId(module: MoodleCourseModule): number | null {
	if (module.modName === 'kalvidres') return module.id;
	for (const url of [module.url, ...module.contents.map((content) => content.fileUrl)]) {
		const match = url == null ? null : KALVIDRES.exec(url);
		if (match !== null) return Number(match[1]);
	}
	return null;
}

export function isVideo(module: MoodleCourseModule): boolean {
	return (
		module.modName === 'h5pactivity' || module.modName === 'scorm' || kalturaCmId(module) !== null
	);
}

const DOCUMENTS = new Set(['resource', 'folder', 'book', 'page']);
const INTERACTIVE = new Set([
	'quiz',
	'assign',
	'choice',
	'workshop',
	'lesson',
	'feedback',
	'wooclap',
	'lti',
	'reservation',
	'choicegroup',
	'data',
	'attendance',
	'scheduler'
]);

export function family(module: MoodleCourseModule): Family {
	if (isVideo(module) || module.modName === 'webex') return 'media';
	if (DOCUMENTS.has(module.modName)) return 'document';
	if (INTERACTIVE.has(module.modName)) return 'interactive';
	return 'neutral';
}

/** Hidden from this student by a restriction; still listed, with the restriction. */
export function isLocked(module: MoodleCourseModule): boolean {
	return module.userVisible === false;
}

/** Moodle's completion says complete (1) or passed (2); a failed attempt (3) isn't done. */
export function isCompleted(module: MoodleCourseModule): boolean {
	const state = module.completionData?.state;
	return state === 1 || state === 2;
}

/** Tracked by Moodle's completion, manually (1) or automatically (2). */
export function isTracked(module: MoodleCourseModule): boolean {
	return (module.completion ?? 0) > 0 || module.completionData?.hasCompletion === true;
}

/** Ticked by the student rather than by a rule: the only completion a client may set. */
export function isManuallyTracked(module: MoodleCourseModule): boolean {
	const data = module.completionData;
	return data !== null ? data.hasCompletion && !data.isAutomatic : module.completion === 1;
}

function dateOf(module: MoodleCourseModule, ...ids: string[]): Date | null {
	for (const id of ids) {
		const found = module.dates.find((date) => date.dataId === id && date.timestamp > 0);
		if (found !== undefined) return new Date(found.timestamp * 1000);
	}
	return null;
}

/** When the activity is due or closes. */
export function dueAt(module: MoodleCourseModule): Date | null {
	return dateOf(module, 'duedate', 'timeclose', 'close');
}

/** When the activity opens for submissions. */
export function opensAt(module: MoodleCourseModule): Date | null {
	return dateOf(module, 'allowsubmissionsfromdate', 'timeopen', 'open');
}

function toFile(content: MoodleModuleContent, fallbackName: string): CourseFile | null {
	if (content.fileUrl === null) return null;
	return {
		url: content.fileUrl,
		name: content.fileName || fallbackName,
		folder: content.filePath || '/',
		mime: content.mimeType,
		size: content.fileSize > 0 ? content.fileSize : null,
		modified: content.timeModified > 0 ? content.timeModified * 1000 : null
	};
}

/** The files an activity bundles, in its order. */
export function moduleFiles(module: MoodleCourseModule, language: string): CourseFile[] {
	const name = displayName(module.name, language);
	return module.contents
		.filter((content) => content.type !== 'url')
		.map((content) => toFile(content, name))
		.filter((file) => file !== null);
}

/** The resource's or page's HTML entry: its `index.html`, else its first HTML file. */
function htmlEntry(module: MoodleCourseModule): MoodleModuleContent | undefined {
	const files = module.contents.filter(
		(content) => content.fileUrl !== null && content.type !== 'url'
	);
	return (
		files.find((content) => content.fileName.toLowerCase() === 'index.html') ??
		files.find(
			(content) =>
				/\.html?$/i.test(content.fileName) || content.mimeType?.startsWith('text/html') === true
		)
	);
}

/** Where opening `module` leads; first match wins, as on Android. */
export function moduleTarget(module: MoodleCourseModule, language: string): ModuleTarget {
	const instance = module.instance;
	if (module.modName === 'quiz' && instance !== null) return { kind: 'quiz', id: instance };
	if (module.modName === 'assign' && instance !== null) return { kind: 'assign', id: instance };
	if (module.modName === 'forum' && instance !== null) return { kind: 'forum', id: instance };
	const video = kalturaCmId(module);
	if (video !== null) return { kind: 'video', cmId: video };
	if (module.modName === 'subsection' || module.modName === 'label') return { kind: 'none' };
	const name = displayName(module.name, language);
	if (module.modName === 'page' || module.modName === 'resource') {
		const entry = htmlEntry(module);
		const file = entry === undefined ? null : toFile(entry, name);
		if (file !== null) return { kind: 'file', file: { ...file, mime: 'text/html' } };
	}
	const files = moduleFiles(module, language);
	if (module.modName === 'folder' && files.length > 0) return { kind: 'folder', files };
	if (module.modName === 'resource' && files.length > 1) return { kind: 'folder', files };
	if (module.modName === 'resource' && files.length === 1) return { kind: 'file', file: files[0] };
	if (module.modName === 'url') {
		const target = module.contents.find((content) => content.type === 'url')?.fileUrl ?? module.url;
		if (target) return { kind: 'link', url: target };
	}
	return module.url ? { kind: 'external', url: module.url } : { kind: 'none' };
}

function linkedSectionId(module: MoodleCourseModule): number | null {
	if (!module.customData) return null;
	try {
		const data: unknown = JSON.parse(module.customData);
		if (typeof data !== 'object' || data === null || !('sectionid' in data)) return null;
		const id = Number(data.sectionid);
		return Number.isFinite(id) ? id : null;
	} catch {
		return null;
	}
}

const shown = (module: MoodleCourseModule) =>
	module.visible !== 0 && module.visibleOnCoursePage !== 0;

/**
 * A label that only names what follows: no link, image or table, at most 60 characters, and no
 * closing punctuation, which marks a sentence. Moodle copies a label's text into its name, so
 * "<p>Esercitazioni</p>" arrives as a description too.
 */
function headingOf(module: MoodleCourseModule, name: string): string | null {
	const description = module.description ?? '';
	if (/<a\s|<img|<table/i.test(description)) return null;
	const text = isRenderableHtml(description) ? htmlToText(description) : name;
	if (text.length < 1 || text.length > HEADER_MAX_CHARS || /\n/.test(text)) return null;
	return /[.!?;]$/.test(text) ? null : cleanName(text);
}

/**
 * A section's content in reading order: runs of activities under the label that heads them,
 * teacher notes, and subsections inlined. Activities hidden from the course page are left out,
 * as Moodle does. A label that only names what follows heads the next run; any other label is
 * a note.
 */
export function contentBlocks(
	section: MoodleCourseSection,
	resolve: (module: MoodleCourseModule) => MoodleCourseSection | undefined,
	language: string
): ContentBlock[] {
	const blocks: ContentBlock[] = [];
	let title: string | null = null;
	let run: MoodleCourseModule[] = [];
	const flush = () => {
		if (run.length > 0) {
			blocks.push({ kind: 'group', key: `g${run[0].id}`, title, modules: run });
			run = [];
		}
		title = null;
	};
	for (const module of section.modules) {
		if (!shown(module)) continue;
		if (module.modName === 'subsection') {
			const child = resolve(module);
			flush();
			const childTitle =
				displayName(module.name, language) ||
				(child === undefined ? '' : displayName(child.name, language));
			if (child !== undefined || childTitle !== '') {
				blocks.push({
					kind: 'subsection',
					key: `s${module.id}`,
					title: childTitle,
					summary: child !== undefined && isRenderableHtml(child.summary) ? child.summary : null,
					blocks: child === undefined ? [] : contentBlocks(child, resolve, language)
				});
			}
			continue;
		}
		if (module.modName === 'label') {
			const name = displayName(module.name, language);
			const heading = headingOf(module, name);
			flush();
			if (heading !== null) {
				title = heading;
				continue;
			}
			const note =
				module.description && isRenderableHtml(module.description)
					? module.description
					: module.name;
			if (isRenderableHtml(note)) blocks.push({ kind: 'note', key: `n${module.id}`, html: note });
			continue;
		}
		run.push(module);
	}
	flush();
	return blocks;
}

function countKind(module: MoodleCourseModule): CountKind {
	if (isVideo(module)) return 'video';
	switch (module.modName) {
		case 'resource':
			return module.contents[0]?.mimeType?.includes('pdf') ? 'pdf' : 'file';
		case 'quiz':
		case 'assign':
		case 'forum':
		case 'folder':
		case 'page':
			return module.modName;
		case 'url':
			return 'link';
		default:
			return 'other';
	}
}

/** Every activity of `blocks`, subsections included, in reading order. */
export function blockModules(blocks: readonly ContentBlock[]): MoodleCourseModule[] {
	return blocks.flatMap((block) =>
		block.kind === 'group'
			? block.modules
			: block.kind === 'subsection'
				? blockModules(block.blocks)
				: []
	);
}

/** The three most frequent activity types of a section: "26 video · 8 PDF · 1 quiz". */
export function typeCounts(blocks: readonly ContentBlock[]): TypeCount[] {
	const counts = new Map<CountKind, number>();
	for (const module of blockModules(blocks)) {
		const kind = countKind(module);
		counts.set(kind, (counts.get(kind) ?? 0) + 1);
	}
	return [...counts.entries()]
		.map(([kind, count]) => ({ kind, count }))
		.sort((a, b) => b.count - a.count)
		.slice(0, 3);
}

/**
 * The Content tab's cards: visible sections that aren't subsections inlined elsewhere and show
 * something, a summary or an activity on the course page.
 */
export function courseOutline(
	sections: readonly MoodleCourseSection[],
	language: string
): OutlineSection[] {
	const children = sections.filter((section) => section.component === 'mod_subsection');
	const byId = new Map(children.map((child) => [child.id, child]));
	const byItem = new Map(
		children.filter((child) => child.itemId !== null).map((child) => [child.itemId, child])
	);
	const resolve = (module: MoodleCourseModule) => {
		const linked = linkedSectionId(module);
		return (
			(linked === null ? undefined : byId.get(linked)) ??
			(module.instance === null ? undefined : byItem.get(module.instance))
		);
	};
	return sections
		.filter(
			(section) =>
				section.visible !== 0 &&
				section.component !== 'mod_subsection' &&
				(isRenderableHtml(section.summary) || section.modules.some(shown))
		)
		.map((section, index) => {
			const blocks = contentBlocks(section, resolve, language);
			const title = displayName(section.name, language);
			return {
				section,
				ordinal: index + 1,
				title: title === '' ? null : title,
				summary: isRenderableHtml(section.summary) ? section.summary : null,
				blocks,
				counts: typeCounts(blocks)
			};
		});
}

/** One recording of the course's playlist. */
export interface PlaylistItem {
	cmId: number;
	title: string;
	sectionId: number;
	sectionTitle: string | null;
	sectionOrdinal: number;
	/** Moodle's completion; watching it here to 90 % counts too (see `progress.ts`). */
	completed: boolean;
	/** The activity's own completion is ticked by the student, so watching may tick it. */
	manual: boolean;
}

/** Every recording on the Content tab, in course order, subsections included. */
export function coursePlaylist(
	outline: readonly OutlineSection[],
	language: string
): PlaylistItem[] {
	const items: PlaylistItem[] = [];
	const seen = new Set<number>();
	for (const card of outline) {
		for (const module of blockModules(card.blocks)) {
			const cmId = kalturaCmId(module);
			if (cmId === null || seen.has(cmId)) continue;
			seen.add(cmId);
			items.push({
				cmId,
				title: displayName(module.name, language),
				sectionId: card.section.id,
				sectionTitle: card.title,
				sectionOrdinal: card.ordinal,
				completed: isCompleted(module),
				manual: module.modName === 'kalvidres' && isManuallyTracked(module)
			});
		}
	}
	return items;
}

/** The activity with `cmId` and the card it sits in. */
export function findModule(
	outline: readonly OutlineSection[],
	cmId: number
): { module: MoodleCourseModule; card: OutlineSection } | undefined {
	for (const card of outline) {
		const module = blockModules(card.blocks).find((candidate) => candidate.id === cmId);
		if (module !== undefined) return { module, card };
	}
	return undefined;
}

/** How many tracked activities the student completed, of how many. */
export function completionSummary(
	outline: readonly OutlineSection[],
	watched: (cmId: number) => boolean
): { done: number; tracked: number } {
	let done = 0;
	let tracked = 0;
	for (const card of outline) {
		for (const module of blockModules(card.blocks)) {
			if (!isTracked(module)) continue;
			tracked++;
			if (isCompleted(module) || watched(module.id)) done++;
		}
	}
	return { done, tracked };
}
