import type { MoodleCourseModule } from '@mybicocca/data-api/elearning';
import { formatDate, formatTime } from '#lib/format/dates.ts';
import { htmlToLine } from '#lib/format/html.ts';
import { formatBytes, formatPercent } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import { dueAt, isVideo, moduleFiles, opensAt } from '../../content.ts';
import { classifyFile, type FileKind } from '../../files.ts';
import { progressRatio, type VideoProgress } from '../../video/progress.ts';

/** Moodle's badge after a forum's name ("2 messaggi non letti") is shown when it is this short. */
const BADGE_MAX_CHARS = 30;

/** A file's kind as the course page names it: the format for documents, else the family. */
export function kindLabel(kind: FileKind, name: string): string {
	switch (kind.type) {
		case 'pdf':
			return 'PDF';
		case 'office':
			return kind.app === 'word' ? 'DOC' : kind.app === 'excel' ? 'XLS' : 'PPT';
		case 'zip':
			return 'ZIP';
		case 'video':
			return m.course_kind_video();
		case 'audio':
			return m.course_kind_audio();
		case 'image':
			return m.course_kind_image();
		case 'html':
			return m.course_kind_html();
		case 'text': {
			const extension = /\.([a-z0-9]+)$/i.exec(name)?.[1];
			return extension ? extension.toUpperCase() : m.course_kind_text();
		}
		case 'unknown':
			return m.course_kind_file();
	}
}

const when = (date: Date) => ({ date: formatDate(date, 'dayMonth'), time: formatTime(date) });

function deadline(module: MoodleCourseModule, now: Date, withOpening: boolean): string | null {
	const due = dueAt(module);
	if (due !== null) {
		return due < now ? m.course_deadline_closed(when(due)) : m.course_deadline_due(when(due));
	}
	const opens = withOpening ? opensAt(module) : null;
	return opens !== null && opens > now ? m.course_deadline_from(when(opens)) : null;
}

function hostOf(url: string | null | undefined): string | null {
	try {
		return url ? new URL(url).hostname.replace(/^www\./, '') : null;
	} catch {
		return null;
	}
}

const join = (...parts: (string | null)[]) => parts.filter((part) => part).join(' · ');

const LABELS: Readonly<Record<string, () => string>> = {
	page: m.course_module_page,
	book: m.course_module_book,
	lesson: m.course_module_lesson,
	wiki: m.course_module_wiki,
	glossary: m.course_module_glossary,
	workshop: m.course_module_workshop,
	wooclap: m.course_module_wooclap,
	lti: m.course_module_lti,
	webex: m.course_module_webex,
	data: m.course_module_database,
	attendance: m.course_module_attendance,
	scheduler: m.course_module_scheduler,
	h5pactivity: m.course_module_interactive,
	scorm: m.course_module_package
};

/** The line under an activity's name: its kind, and its size, deadline or progress. */
export function moduleMeta(
	module: MoodleCourseModule,
	progress: VideoProgress | undefined,
	language: string,
	now: Date
): string {
	if (isVideo(module) && LABELS[module.modName] === undefined) {
		if (progress?.completed) return m.course_module_watched();
		const ratio = progressRatio(progress);
		return ratio > 0.01
			? m.course_module_watched_percent({ percent: formatPercent(ratio) })
			: m.course_module_video();
	}
	switch (module.modName) {
		case 'resource': {
			const files = moduleFiles(module, language);
			const first = files[0];
			if (first === undefined) return m.course_kind_file();
			const size = files.reduce((sum, file) => sum + (file.size ?? 0), 0);
			return join(
				kindLabel(classifyFile(first.name, first.mime), first.name),
				size > 0 ? formatBytes(size) : null
			);
		}
		case 'quiz':
			return join(m.course_module_quiz(), deadline(module, now, false));
		case 'assign':
			return join(m.course_module_assign(), deadline(module, now, true));
		case 'choice':
			return join(m.course_module_choice(), deadline(module, now, false));
		case 'feedback':
			return join(m.course_module_feedback(), deadline(module, now, false));
		case 'choicegroup':
			return join(m.course_module_choice_group(), deadline(module, now, true));
		case 'reservation':
			return join(m.course_module_reservation(), deadline(module, now, true));
		case 'folder': {
			const count = moduleFiles(module, language).length;
			return count > 0 ? m.course_module_folder_count({ count }) : m.course_module_folder();
		}
		case 'forum':
		case 'hsuforum': {
			const badge = module.afterLink ? htmlToLine(module.afterLink) : '';
			return join(
				m.course_module_forum(),
				badge !== '' && badge.length <= BADGE_MAX_CHARS ? badge : null
			);
		}
		case 'url':
			return join(
				m.course_module_link(),
				hostOf(module.contents.find((content) => content.type === 'url')?.fileUrl)
			);
		default:
			return LABELS[module.modName]?.() ?? (module.modPlural || m.course_module_activity());
	}
}
