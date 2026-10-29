import type { MoodleCourseModule } from '@mybicocca/data-api/elearning';
import IconArticle from '~icons/ph/article';
import IconBook from '~icons/ph/book';
import IconBookOpen from '~icons/ph/book-open';
import IconCalendarCheck from '~icons/ph/calendar-check';
import IconCalendarPlus from '~icons/ph/calendar-plus';
import IconChalkboardTeacher from '~icons/ph/chalkboard-teacher';
import IconChartBar from '~icons/ph/chart-bar';
import IconChats from '~icons/ph/chats';
import IconClipboard from '~icons/ph/clipboard';
import IconClipboardText from '~icons/ph/clipboard-text';
import IconCube from '~icons/ph/cube';
import IconDatabase from '~icons/ph/database';
import IconExam from '~icons/ph/exam';
import IconFile from '~icons/ph/file';
import IconFileAudio from '~icons/ph/file-audio';
import IconFileCode from '~icons/ph/file-code';
import IconFileDoc from '~icons/ph/file-doc';
import IconFileHtml from '~icons/ph/file-html';
import IconFileImage from '~icons/ph/file-image';
import IconFilePdf from '~icons/ph/file-pdf';
import IconFilePpt from '~icons/ph/file-ppt';
import IconFileText from '~icons/ph/file-text';
import IconFileVideo from '~icons/ph/file-video';
import IconFileXls from '~icons/ph/file-xls';
import IconFileZip from '~icons/ph/file-zip';
import IconFolder from '~icons/ph/folder';
import IconHandPointing from '~icons/ph/hand-pointing';
import IconLink from '~icons/ph/link';
import IconPackage from '~icons/ph/package';
import IconPlayCircle from '~icons/ph/play-circle';
import IconPlugs from '~icons/ph/plugs';
import IconPuzzlePiece from '~icons/ph/puzzle-piece';
import IconUserCheck from '~icons/ph/user-check';
import IconUsersThree from '~icons/ph/users-three';
import IconVideoCamera from '~icons/ph/video-camera';
import type { Icon } from '#lib/ui/types.ts';
import { isVideo } from '../content.ts';
import { classifyFile, type FileKind } from '../files.ts';

/** The glyph of a kind of file. */
export function fileIcon(kind: FileKind): Icon {
	switch (kind.type) {
		case 'pdf':
			return IconFilePdf;
		case 'office':
			return kind.app === 'word' ? IconFileDoc : kind.app === 'excel' ? IconFileXls : IconFilePpt;
		case 'zip':
			return IconFileZip;
		case 'image':
			return IconFileImage;
		case 'video':
			return IconFileVideo;
		case 'audio':
			return IconFileAudio;
		case 'html':
			return IconFileHtml;
		case 'text':
			return IconFileCode;
		case 'unknown':
			return IconFile;
	}
}

const BY_TYPE: Readonly<Record<string, Icon>> = {
	quiz: IconExam,
	assign: IconClipboardText,
	forum: IconChats,
	hsuforum: IconChats,
	folder: IconFolder,
	url: IconLink,
	page: IconFileText,
	book: IconBook,
	lesson: IconChalkboardTeacher,
	choice: IconChartBar,
	choicegroup: IconChartBar,
	feedback: IconClipboard,
	wiki: IconArticle,
	glossary: IconBookOpen,
	workshop: IconUsersThree,
	lti: IconPlugs,
	webex: IconVideoCamera,
	data: IconDatabase,
	attendance: IconUserCheck,
	scheduler: IconCalendarCheck,
	reservation: IconCalendarPlus,
	h5pactivity: IconPuzzlePiece,
	scorm: IconPackage,
	wooclap: IconHandPointing
};

/** The glyph of an activity: its type's, or its first file's for a resource. */
export function moduleIcon(module: MoodleCourseModule): Icon {
	if (module.modName === 'resource') {
		const first = module.contents.find((content) => content.type !== 'url');
		return first === undefined ? IconFile : fileIcon(classifyFile(first.fileName, first.mimeType));
	}
	if (module.modName !== 'h5pactivity' && module.modName !== 'scorm' && isVideo(module)) {
		return IconPlayCircle;
	}
	return BY_TYPE[module.modName] ?? IconCube;
}
