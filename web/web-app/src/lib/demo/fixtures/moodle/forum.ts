import { days, epochSeconds } from '../../dates.ts';
import { DemoCourses, DemoElearning, DemoStudent, type DemoCourse } from '../../persona.ts';
import { json, moodleError, route, type DemoRequest } from '../../router.ts';
import { demoState } from '../../state.ts';
import { draftAreas, unusedDraftId } from './files.assignment.ts';

/**
 * The persona's course forums (`mod_forum_*`): in every course the teacher's announcements and a
 * discussion forum where classmates, the tutor and the student talk, as on Android; Algoritmi also
 * has a Q&A forum nobody has used yet. Posting, editing, deleting, following and starring change
 * the state, and the student may edit or delete a post of theirs for 30 minutes, as Moodle allows.
 */

const SITE = 'https://elearning.unimib.it';
const SERVER = `${SITE}/webservice/rest/server.php`;
const PLACEHOLDER_PICTURE = `${SITE}/theme/image.php/boost/core/1/u/f1`;
const EDIT_WINDOW_S = 30 * 60;
const MINUTE_MS = 60_000;

type ForumType = 'news' | 'general' | 'qanda';

interface DemoForum {
	id: number;
	cmId: number;
	course: DemoCourse;
	type: ForumType;
	name: string;
	intro: string;
	maxAttachments: number;
	maxBytes: number;
	/** 0 optional, 1 forced, 2 automatic, 3 disabled. */
	forceSubscribe: number;
	canCreate: boolean;
}

/** A file attached to a post. */
interface DemoFile {
	name: string;
	size: number;
	mime: string;
	/** Epoch seconds. */
	uploaded: number;
}

interface DemoPost {
	id: number;
	discussion: number;
	parent: number;
	author: string;
	subject: string;
	message: string;
	created: number;
	modified: number;
	attachments: DemoFile[];
	read: boolean;
}

interface DemoDiscussion {
	id: number;
	forum: number;
	pinned: boolean;
	locked: boolean;
	starred: boolean;
	subscribed: boolean;
}

interface ForumState {
	forums: DemoForum[];
	discussions: DemoDiscussion[];
	posts: DemoPost[];
	nextPost: number;
	nextThread: number;
}

/** Many teachers talk only through announcements: in this course the Forums tab shows that case. */
const announcementsOnly: DemoCourse = DemoCourses.statistics;

const generalForumId = (course: DemoCourse) => course.moodleCourseId + 2400;
const qandaForumId = (course: DemoCourse) => course.moodleCourseId + 2800;

const own = (author: string) => author === DemoStudent.fullName;

/** Moodle user ids for the persona's classmates and teachers, stable across calls. */
function userId(author: string): number {
	if (own(author)) return DemoStudent.moodle.userId;
	let hash = 0;
	for (const char of author) hash = (hash * 31 + (char.codePointAt(0) ?? 0)) % 8000;
	return 31000 + hash;
}

const at = (date: Date) => epochSeconds(date);
const minutesAgo = (minutes: number) => at(new Date(Date.now() - minutes * MINUTE_MS));

function pdf(name: string, size: number, uploaded: number): DemoFile {
	return { name, size, mime: 'application/pdf', uploaded };
}

interface Line {
	author: string;
	at: number;
	html: string;
	/** Index of the line it answers; the opening post otherwise. */
	replyTo?: number;
	attachments?: DemoFile[];
	read?: boolean;
}

function seed(): ForumState {
	const forums: DemoForum[] = [];
	const discussions: DemoDiscussion[] = [];
	const posts: DemoPost[] = [];

	function thread(
		forum: DemoForum,
		index: number,
		subject: string,
		lines: Line[],
		flags: Partial<Pick<DemoDiscussion, 'pinned' | 'locked'>> = {}
	) {
		const id = forum.id * 1000 + index;
		const ids = lines.map((_, position) => id * 10 + position);
		discussions.push({
			id,
			forum: forum.id,
			pinned: flags.pinned ?? false,
			locked: flags.locked ?? false,
			starred: false,
			subscribed: lines.some((line) => own(line.author))
		});
		lines.forEach((line, position) =>
			posts.push({
				id: ids[position],
				discussion: id,
				parent: position === 0 ? 0 : ids[line.replyTo ?? 0],
				author: line.author,
				subject: position === 0 ? subject : `Ri: ${subject}`,
				message: line.html,
				created: line.at,
				modified: line.at,
				attachments: line.attachments ?? [],
				read: line.read ?? true
			})
		);
	}

	for (const course of DemoCourses.all) {
		const teacher = course.teacher;
		const topics = DemoElearning.topics.get(course) ?? [];
		const news: DemoForum = {
			id: DemoElearning.forumId(course),
			cmId: DemoElearning.cmId(course, DemoElearning.FORUM),
			course,
			type: 'news',
			name: 'Annunci',
			intro: '<p>Gli avvisi dei docenti: date d’esame, cambi d’aula, materiali pubblicati.</p>',
			maxAttachments: 9,
			maxBytes: 0,
			forceSubscribe: 1,
			canCreate: false
		};
		const general: DemoForum = {
			id: generalForumId(course),
			cmId: DemoElearning.cmId(course, 3),
			course,
			type: 'general',
			name: 'Forum di discussione',
			intro:
				'<p>Domande sugli esercizi, dubbi sulle lezioni, gruppi di studio. Rispondono anche i tutor: prima di aprire una discussione, controllate che non ce ne sia già una sullo stesso tema.</p>',
			maxAttachments: 3,
			maxBytes: 5 * 1024 * 1024,
			forceSubscribe: 0,
			canCreate: true
		};
		forums.push(news);
		if (course !== announcementsOnly) forums.push(general);
		if (course === DemoCourses.algorithms) {
			forums.push({
				id: qandaForumId(course),
				cmId: DemoElearning.cmId(course, 4),
				course,
				type: 'qanda',
				name: 'Domande sul progetto',
				intro:
					'<p>Fate qui le domande sul progetto d’esame: vedrete le risposte dei compagni dopo aver scritto la vostra.</p>',
				maxAttachments: 1,
				maxBytes: 2 * 1024 * 1024,
				forceSubscribe: 0,
				canCreate: true
			});
		}

		thread(
			news,
			0,
			'Calendario degli appelli',
			[
				{
					author: teacher,
					at: at(days(-6, 9, 12)),
					html: `<p>Gentili studenti,</p><p>gli appelli della sessione sono pubblicati su <strong>Esse3</strong>. Le iscrizioni chiudono <strong>tre giorni prima</strong> di ogni prova; aula e orario sono indicati nella pagina dell’appello.</p><p>Buono studio,<br>${teacher}</p>`
				}
			],
			{ pinned: true }
		);
		thread(news, 1, 'Lezione di giovedì in aula U6-08', [
			{
				author: teacher,
				at: at(days(-1, 18, 5)),
				html: '<p>Giovedì la lezione si terrà in <strong>aula U6-08</strong> invece che nella solita aula, per un convegno nell’edificio U7. L’orario resta invariato.</p>',
				read: false
			}
		]);
		if (topics.length > 2) {
			thread(news, 2, `Slide ed esercizi: ${topics[2].toLowerCase()}`, [
				{
					author: teacher,
					at: at(days(-3, 14, 30)),
					html: `<p>Ho pubblicato le slide della settimana su <em>${topics[2].toLowerCase()}</em> e una raccolta di esercizi svolti, che trovate anche nella sezione del corso.</p><ul><li>Slide 3: ${topics[2]}</li><li>Esercizi svolti (con soluzioni commentate)</li></ul>`,
					attachments: [
						pdf('slide_3.pdf', 1_240_000, at(days(-3, 14, 28))),
						pdf('esercizi_svolti_3.pdf', 486_000, at(days(-3, 14, 29)))
					]
				}
			]);
		}
		thread(news, 3, 'Esiti della prova intermedia', [
			{
				author: teacher,
				at: at(days(-12, 17, 40)),
				html: '<p>In allegato gli esiti della prova intermedia. Chi ha ottenuto almeno <strong>18</strong> è esonerato dalla prima parte dello scritto.</p><p>Visione dei compiti martedì alle 14:30 nel mio studio, edificio U14.</p>',
				attachments: [pdf('esiti_prova_intermedia.pdf', 212_000, at(days(-12, 17, 38)))]
			}
		]);

		if (course === announcementsOnly) continue;
		thread(general, 0, 'Dubbio sull’esercizio 3 dell’ultima esercitazione', [
			{
				author: 'Giulia Moretti',
				at: at(days(-2, 18, 40)),
				html: '<p>Buonasera, nell’esercizio 3 non mi è chiaro se si debba considerare anche il caso con <strong>input vuoto</strong>. Qualcuno l’ha risolto?</p>'
			},
			{
				author: 'Andrea Ricci',
				at: at(days(-2, 21, 5)),
				html: '<p>Io l’ho trattato come caso base, il risultato torna con la soluzione del libro.</p>'
			},
			{
				author: 'Giulia Moretti',
				at: at(days(-2, 21, 32)),
				html: '<p>Grazie! Quindi restituisci subito la lista vuota?</p>',
				replyTo: 1
			},
			{
				author: 'Andrea Ricci',
				at: at(days(-2, 22, 10)),
				html: '<p>Esatto, prima ancora del ciclo. Così l’invariante vale anche al primo passo.</p>',
				replyTo: 2,
				read: false
			},
			{
				author: teacher,
				at: at(days(-1, 8, 31)),
				html: '<p>Confermo: l’input vuoto va gestito <em>esplicitamente</em>. Ne riparliamo giovedì a lezione, con un paio di casi limite simili.</p>',
				read: false
			},
			{
				author: DemoStudent.fullName,
				at: minutesAgo(12),
				html: '<p>Grazie, anch’io l’avevo trattato come caso base ma non ero sicuro che bastasse.</p>',
				replyTo: 4
			}
		]);
		thread(general, 1, 'Gruppo di studio in biblioteca', [
			{
				author: 'Federica Sala',
				at: at(days(-4, 14, 22)),
				html: '<p>Ciao a tutti, qualcuno vuole ripassare insieme in biblioteca U6 la prossima settimana?</p>'
			},
			{
				author: DemoStudent.fullName,
				at: at(days(-4, 16, 3)),
				html: '<p>Io ci sono, martedì pomeriggio per me va bene.</p>'
			},
			{
				author: 'Luca Ferrari',
				at: at(days(-3, 9, 15)),
				html: '<p>Martedì va bene anche per me: prenoto io i posti su Affluences, dalle 14 alle 18.</p>',
				replyTo: 1
			}
		]);
		if (topics.length > 1) {
			thread(general, 2, `Appunti della lezione su ${topics[1].toLowerCase()}`, [
				{
					author: 'Andrea Ricci',
					at: at(days(-8, 19, 2)),
					html: `<p>Ho messo in ordine i miei appunti dell’ultima lezione su <em>${topics[1].toLowerCase()}</em>, con gli esempi fatti alla lavagna. Se trovate errori ditemelo!</p>`,
					attachments: [pdf('appunti_lezione_2.pdf', 1_830_000, at(days(-8, 19, 0)))]
				},
				{
					author: 'Federica Sala',
					at: at(days(-7, 10, 48)),
					html: '<p>Grazie mille, utilissimi! Nell’esempio a pagina 3 manca un passaggio, ma il resto torna.</p>'
				}
			]);
		}
		thread(
			general,
			3,
			'Ricevimento del tutor',
			[
				{
					author: course.tutor,
					at: at(days(-15, 11, 0)),
					html: '<p>Il ricevimento è il <strong>mercoledì dalle 16:30 alle 17:30</strong>, su appuntamento via e-mail. Per i dubbi sugli esercizi usate pure questo forum, così la risposta resta a tutti.</p>'
				}
			],
			{ locked: true }
		);
	}

	return { forums, discussions, posts, nextPost: 990_000, nextThread: 100 };
}

const state = () => demoState<ForumState>('moodle.forum', seed);

function forumOf(id: number): DemoForum | undefined {
	return state().forums.find((forum) => forum.id === id);
}

function discussionOf(id: number): DemoDiscussion | undefined {
	return state().discussions.find((discussion) => discussion.id === id);
}

function postsOf(discussion: number): DemoPost[] {
	return state()
		.posts.filter((post) => post.discussion === discussion)
		.sort((a, b) => a.created - b.created || a.id - b.id);
}

function canReply(forum: DemoForum, discussion: DemoDiscussion): boolean {
	return forum.type !== 'news' && !discussion.locked;
}

/** Moodle lets a student change a post only within the editing time, and delete it only unanswered. */
function capabilities(post: DemoPost, forum: DemoForum, discussion: DemoDiscussion) {
	const mine = own(post.author) && Date.now() / 1000 - post.created < EDIT_WINDOW_S;
	const answered = state().posts.some((other) => other.parent === post.id);
	return {
		view: true,
		edit: mine,
		delete: mine && !answered,
		split: false,
		reply: canReply(forum, discussion),
		selfenrol: false,
		export: false,
		controlreadstatus: false,
		canreplyprivately: false
	};
}

function fileUrl(forum: DemoForum, post: DemoPost, file: DemoFile): string {
	return `${SITE}/webservice/pluginfile.php/${900000 + forum.cmId}/mod_forum/attachment/${post.id}/${encodeURIComponent(file.name)}`;
}

function fileRecord(forum: DemoForum, post: DemoPost, file: DemoFile) {
	return {
		filename: file.name,
		filepath: '/',
		filesize: file.size,
		fileurl: fileUrl(forum, post, file),
		timemodified: file.uploaded,
		mimetype: file.mime,
		isexternalfile: false
	};
}

function postAuthor(name: string) {
	const id = userId(name);
	return {
		id,
		fullname: name,
		isdeleted: false,
		groups: [],
		urls: {
			profile: `${SITE}/user/view.php?id=${id}`,
			profileimage: PLACEHOLDER_PICTURE
		}
	};
}

function wirePost(post: DemoPost) {
	const discussion = discussionOf(post.discussion);
	const forum = discussion && forumOf(discussion.forum);
	if (discussion === undefined || forum === undefined) return undefined;
	const words = post.message
		.replace(/<[^>]+>/g, ' ')
		.trim()
		.split(/\s+/)
		.filter(Boolean);
	const view = `${SITE}/mod/forum/discuss.php?d=${discussion.id}#p${post.id}`;
	return {
		id: post.id,
		subject: post.subject,
		replysubject: post.subject.startsWith('Ri: ') ? post.subject : `Ri: ${post.subject}`,
		message: post.message,
		messageformat: 1,
		author: postAuthor(post.author),
		discussionid: discussion.id,
		hasparent: post.parent !== 0,
		parentid: post.parent === 0 ? null : post.parent,
		timecreated: post.created,
		timemodified: post.modified,
		unread: !post.read,
		isdeleted: false,
		isprivatereply: false,
		haswordcount: false,
		wordcount: words.length,
		charcount: words.join(' ').length,
		capabilities: capabilities(post, forum, discussion),
		urls: {
			view,
			viewisolated: `${SITE}/mod/forum/discuss.php?d=${discussion.id}&parent=${post.id}`,
			viewparent:
				post.parent === 0
					? null
					: `${SITE}/mod/forum/discuss.php?d=${discussion.id}#p${post.parent}`,
			edit: `${SITE}/mod/forum/post.php?edit=${post.id}`,
			delete: `${SITE}/mod/forum/post.php?delete=${post.id}`,
			split: null,
			reply: `${SITE}/mod/forum/post.php?reply=${post.id}#mformforum`,
			export: null,
			markasread: null,
			markasunread: null,
			discuss: view
		},
		attachments: post.attachments.map((file) => ({
			contextid: 900000 + forum.cmId,
			component: 'mod_forum',
			filearea: 'attachment',
			itemid: post.id,
			filepath: '/',
			filename: file.name,
			isdir: false,
			isimage: file.mime.startsWith('image/'),
			timemodified: file.uploaded,
			timecreated: file.uploaded,
			filesize: file.size,
			author: post.author,
			license: 'allrightsreserved',
			filenameshort: file.name,
			filesizeformatted: `${Math.max(1, Math.round(file.size / 1024))} KB`,
			icon: file.mime === 'application/pdf' ? 'f/pdf' : 'f/unknown',
			timecreatedformatted: '',
			timemodifiedformatted: '',
			url: fileUrl(forum, post, file),
			urls: { export: null },
			html: { plagiarism: null }
		})),
		messageinlinefiles: [],
		tags: [],
		html: { rating: null, taglist: null, authorsubheading: null }
	};
}

function wireDiscussion(discussion: DemoDiscussion, forum: DemoForum) {
	const thread = postsOf(discussion.id);
	const first = thread[0];
	const last = thread.reduce((latest, post) => (post.created > latest.created ? post : latest));
	return {
		id: first.id,
		name: first.subject,
		groupid: -1,
		timemodified: Math.max(...thread.map((post) => post.modified)),
		usermodified: userId(last.author),
		timestart: 0,
		timeend: 0,
		discussion: discussion.id,
		parent: 0,
		userid: userId(first.author),
		created: first.created,
		modified: first.modified,
		mailed: 1,
		subject: first.subject,
		message: first.message,
		messageformat: 1,
		messagetrust: 0,
		messageinlinefiles: [],
		attachment: first.attachments.length > 0,
		attachments: first.attachments.map((file) => fileRecord(forum, first, file)),
		totalscore: 0,
		mailnow: 0,
		userfullname: first.author,
		usermodifiedfullname: last.author,
		userpictureurl: PLACEHOLDER_PICTURE,
		usermodifiedpictureurl: PLACEHOLDER_PICTURE,
		numreplies: thread.length - 1,
		numunread: thread.filter((post) => !post.read).length,
		pinned: discussion.pinned,
		locked: discussion.locked,
		starred: discussion.starred,
		canreply: canReply(forum, discussion),
		canlock: false,
		canfavourite: true
	};
}

function wireSummary(discussion: DemoDiscussion, forum: DemoForum) {
	const thread = postsOf(discussion.id);
	const view = `${SITE}/mod/forum/discuss.php?d=${discussion.id}`;
	return {
		id: discussion.id,
		forumid: forum.id,
		pinned: discussion.pinned,
		locked: discussion.locked,
		istimelocked: false,
		name: thread[0]?.subject ?? '',
		firstpostid: thread[0]?.id ?? 0,
		times: {
			modified: Math.max(0, ...thread.map((post) => post.modified)),
			start: 0,
			end: 0,
			locked: 0
		},
		userstate: { subscribed: discussion.subscribed, favourited: discussion.starred },
		capabilities: {
			subscribe: forum.forceSubscribe !== 1 && forum.forceSubscribe !== 3,
			move: false,
			pin: false,
			post: canReply(forum, discussion),
			manage: false,
			favourite: true
		},
		urls: {
			view,
			viewlatest: `${view}&parent=${thread.at(-1)?.id ?? 0}`,
			viewfirstunread: null,
			markasread: `${SITE}/mod/forum/markposts.php?f=${forum.id}&d=${discussion.id}&mark=read`,
			subscribe: `${SITE}/mod/forum/subscribe.php?id=${forum.id}&d=${discussion.id}`,
			pin: null
		},
		timed: { istimed: false, visible: true }
	};
}

function wireForum(forum: DemoForum) {
	const discussions = state().discussions.filter((discussion) => discussion.forum === forum.id);
	const unread = discussions.reduce(
		(sum, discussion) => sum + postsOf(discussion.id).filter((post) => !post.read).length,
		0
	);
	return {
		id: forum.id,
		course: forum.course.moodleCourseId,
		type: forum.type,
		name: forum.name,
		intro: forum.intro,
		introformat: 1,
		introfiles: [],
		lang: '',
		duedate: 0,
		cutoffdate: 0,
		assessed: 0,
		assesstimestart: 0,
		assesstimefinish: 0,
		scale: 100,
		grade_forum: 0,
		grade_forum_notify: 0,
		maxbytes: forum.maxBytes,
		maxattachments: forum.maxAttachments,
		forcesubscribe: forum.forceSubscribe,
		trackingtype: 1,
		rsstype: 0,
		rssarticles: 0,
		timemodified: at(days(-120, 10)),
		warnafter: 0,
		blockafter: 0,
		blockperiod: 0,
		completiondiscussions: 0,
		completionreplies: 0,
		completionposts: 0,
		cmid: forum.cmId,
		numdiscussions: discussions.length,
		cancreatediscussions: forum.canCreate,
		lockdiscussionafter: 0,
		istracked: true,
		unreadpostscount: unread
	};
}

/** Moodle's default listing order: pinned, then starred, then the latest activity. */
function listingOrder(a: ReturnType<typeof wireDiscussion>, b: ReturnType<typeof wireDiscussion>) {
	return (
		Number(b.pinned) - Number(a.pinned) ||
		Number(b.starred) - Number(a.starred) ||
		b.timemodified - a.timemodified ||
		b.discussion - a.discussion
	);
}

/** `options[0][name]=…&options[0][value]=…` as a map. */
function options(form: URLSearchParams): Map<string, string> {
	const names = new Map<string, string>();
	const values = new Map<string, string>();
	for (const [key, value] of form) {
		const match = /^options\[(\d+)\]\[(name|value)\]$/.exec(key);
		if (match === null) continue;
		(match[2] === 'name' ? names : values).set(match[1], value);
	}
	const result = new Map<string, string>();
	for (const [index, name] of names) result.set(name, values.get(index) ?? '');
	return result;
}

/** The files of the draft area a write names, as `upload.php` stored them. */
function attachmentsFrom(option: string | undefined): DemoFile[] {
	const itemId = Number(option ?? 0);
	return (draftAreas().get(itemId) ?? []).map((file) => ({
		name: file.name,
		size: file.size,
		mime: file.mime || 'application/octet-stream',
		uploaded: epochSeconds(file.uploadedAt)
	}));
}

/** A new draft area holding a post's files, as Moodle prepares one to edit them. */
function newDraft(files: readonly DemoFile[]): number {
	const itemId = unusedDraftId();
	draftAreas().set(
		itemId,
		files.map((file) => ({
			name: file.name,
			size: file.size,
			mime: file.mime,
			uploadedAt: new Date(file.uploaded * 1000)
		}))
	);
	return itemId;
}

const ok = () => json({ status: true, warnings: [] });

async function located(request: DemoRequest, field: 'discussionid' | 'postid') {
	const form = await request.form();
	const id = Number(form.get(field));
	const post = field === 'postid' ? state().posts.find((entry) => entry.id === id) : undefined;
	const discussion = discussionOf(field === 'postid' ? (post?.discussion ?? 0) : id);
	const forum = discussion && forumOf(discussion.forum);
	return { form, post, discussion, forum };
}

const invalidRecord = () =>
	moodleError('invalidrecord', 'Impossibile trovare il record nella tabella del database');

export const routes = [
	route(
		`POST ${SERVER}?wsfunction=mod_forum_get_forums_by_courses`,
		async (request) => {
			const form = await request.form();
			const ids = [...form.entries()]
				.filter(([key]) => key.startsWith('courseids['))
				.map(([, value]) => Number(value));
			return json(
				state()
					.forums.filter((forum) => ids.length === 0 || ids.includes(forum.course.moodleCourseId))
					.map(wireForum)
			);
		},
		{ empty: () => json([]) }
	),
	route(
		`POST ${SERVER}?wsfunction=mod_forum_get_forum_discussions`,
		async (request) => {
			const form = await request.form();
			const forum = forumOf(Number(form.get('forumid')));
			if (forum === undefined) return invalidRecord();
			const page = Number(form.get('page') ?? 0);
			const perPage = Number(form.get('perpage') ?? 0);
			const all = state()
				.discussions.filter((discussion) => discussion.forum === forum.id)
				.map((discussion) => wireDiscussion(discussion, forum))
				.sort(listingOrder);
			const shown = perPage > 0 ? all.slice(page * perPage, (page + 1) * perPage) : all;
			return json({ discussions: shown, warnings: [] });
		},
		{ empty: () => json({ discussions: [], warnings: [] }) }
	),
	route(`POST ${SERVER}?wsfunction=mod_forum_get_discussion_posts`, async (request) => {
		const { discussion, forum } = await located(request, 'discussionid');
		if (discussion === undefined || forum === undefined) return invalidRecord();
		const descending = (await request.form()).get('sortdirection') === 'DESC';
		const posts = postsOf(discussion.id).flatMap((post) => wirePost(post) ?? []);
		if (descending) posts.reverse();
		return json({ posts, forumid: forum.id, courseid: forum.course.moodleCourseId, warnings: [] });
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_view_forum`, ok),
	route(`POST ${SERVER}?wsfunction=mod_forum_view_forum_discussion`, async (request) => {
		const { discussion } = await located(request, 'discussionid');
		if (discussion === undefined) return invalidRecord();
		for (const post of postsOf(discussion.id)) post.read = true;
		return ok();
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_add_discussion`, async (request) => {
		const form = await request.form();
		const forum = forumOf(Number(form.get('forumid')));
		if (forum === undefined) return invalidRecord();
		if (!forum.canCreate) {
			return moodleError('cannotcreatediscussion', 'Non puoi creare discussioni in questo forum');
		}
		const subject = (form.get('subject') ?? '').trim();
		const message = form.get('message') ?? '';
		if (subject === '' || message.trim() === '') {
			return moodleError('invalidparameter', 'Rilevato valore di parametro non valido');
		}
		const settings = options(form);
		const attachments = attachmentsFrom(settings.get('attachmentsid'));
		const store = state();
		const id = forum.id * 1000 + store.nextThread++;
		const now = at(new Date());
		store.discussions.push({
			id,
			forum: forum.id,
			pinned: false,
			locked: false,
			starred: false,
			subscribed: settings.get('discussionsubscribe') !== '0'
		});
		store.posts.push({
			id: store.nextPost++,
			discussion: id,
			parent: 0,
			author: DemoStudent.fullName,
			subject,
			message,
			created: now,
			modified: now,
			attachments,
			read: true
		});
		return json({ discussionid: id, warnings: [] });
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_add_discussion_post`, async (request) => {
		const { form, post: parent, discussion, forum } = await located(request, 'postid');
		if (parent === undefined || discussion === undefined || forum === undefined) {
			return invalidRecord();
		}
		if (!canReply(forum, discussion)) {
			return moodleError('nopostforum', 'Non puoi rispondere in questa discussione');
		}
		const settings = options(form);
		const store = state();
		const now = at(new Date());
		const post: DemoPost = {
			id: store.nextPost++,
			discussion: discussion.id,
			parent: parent.id,
			author: DemoStudent.fullName,
			subject: (form.get('subject') ?? '').trim() || `Ri: ${parent.subject.replace(/^Ri: /, '')}`,
			message: form.get('message') ?? '',
			created: now,
			modified: now,
			attachments: attachmentsFrom(settings.get('attachmentsid')),
			read: true
		};
		store.posts.push(post);
		if (settings.get('discussionsubscribe') !== '0') discussion.subscribed = true;
		return json({
			postid: post.id,
			warnings: [],
			post: wirePost(post),
			messages: [{ type: 'success', message: 'Il tuo intervento è stato salvato.' }]
		});
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_update_discussion_post`, async (request) => {
		const { form, post, discussion, forum } = await located(request, 'postid');
		if (post === undefined || discussion === undefined || forum === undefined) {
			return invalidRecord();
		}
		if (!capabilities(post, forum, discussion).edit) {
			return moodleError('cannotupdatepost', 'Non è possibile modificare questo intervento');
		}
		const subject = form.get('subject');
		const message = form.get('message');
		if (subject !== null && subject.trim() !== '') post.subject = subject.trim();
		if (message !== null) post.message = message;
		const attachments = options(form).get('attachmentsid');
		if (attachments !== undefined) post.attachments = attachmentsFrom(attachments);
		post.modified = at(new Date());
		return ok();
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_delete_post`, async (request) => {
		const { post, discussion, forum } = await located(request, 'postid');
		if (post === undefined || discussion === undefined || forum === undefined) {
			return invalidRecord();
		}
		if (!capabilities(post, forum, discussion).delete) {
			return moodleError(
				'couldnotdeleteowninreplies',
				'Non è possibile eliminare un intervento che ha già ricevuto risposte'
			);
		}
		const store = state();
		if (post.parent === 0) {
			store.discussions = store.discussions.filter((entry) => entry.id !== discussion.id);
			store.posts = store.posts.filter((entry) => entry.discussion !== discussion.id);
		} else {
			store.posts = store.posts.filter((entry) => entry.id !== post.id);
		}
		return ok();
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_set_subscription_state`, async (request) => {
		const { form, discussion, forum } = await located(request, 'discussionid');
		if (discussion === undefined || forum === undefined) return invalidRecord();
		if (forum.forceSubscribe === 1 || forum.forceSubscribe === 3) {
			return moodleError('cannotchangesubscription', 'Non puoi modificare l’iscrizione');
		}
		discussion.subscribed = form.get('targetstate') === '1';
		return json(wireSummary(discussion, forum));
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_toggle_favourite_state`, async (request) => {
		const { form, discussion, forum } = await located(request, 'discussionid');
		if (discussion === undefined || forum === undefined) return invalidRecord();
		discussion.starred = form.get('targetstate') === '1';
		return json(wireSummary(discussion, forum));
	}),
	route(`POST ${SERVER}?wsfunction=mod_forum_prepare_draft_area_for_post`, async (request) => {
		const { form, post } = await located(request, 'postid');
		if (post === undefined) return invalidRecord();
		const keep = [...form.entries()]
			.filter(([key]) => /^filestokeep\[\d+\]\[filename\]$/.test(key))
			.map(([, value]) => value);
		const files =
			form.get('area') === 'attachment'
				? post.attachments.filter((file) => keep.length === 0 || keep.includes(file.name))
				: [];
		const itemId = newDraft(files);
		return json({
			draftitemid: itemId,
			files: files.map((file) => ({
				filename: file.name,
				filepath: '/',
				filesize: file.size,
				fileurl: `${SITE}/draftfile.php/5/user/draft/${itemId}/${encodeURIComponent(file.name)}`,
				timemodified: file.uploaded,
				mimetype: file.mime,
				isexternalfile: false
			})),
			areaoptions: [],
			messagetext: form.get('area') === 'post' ? post.message : '',
			warnings: []
		});
	})
];
