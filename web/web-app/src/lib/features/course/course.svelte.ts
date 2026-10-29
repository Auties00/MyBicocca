import { createContext } from 'svelte';
import type {
	MoodleCourse,
	MoodleCourseSection,
	MoodleCoursesByFieldResponse,
	MoodleEnrolledCourse,
	MoodleWebCourseInfo,
	MoodleWebStaffMember
} from '@mybicocca/data-api/elearning';
import { accountKey } from '#lib/data/keys.ts';
import { invalidate, query, type Query, type QueryOptions } from '#lib/data/query.svelte.ts';
import { session, type SessionScope } from '#lib/data/session.svelte.ts';
import {
	periodLabel,
	parseCourseCode,
	type CourseCode
} from '#lib/features/elearning/courseCode.ts';
import { favouritesOf } from '#lib/features/elearning/favourites.svelte.ts';
import { stripMlang } from '#lib/features/elearning/mlang.ts';
import { enrolledCoursesQuery } from '#lib/features/elearning/queries.ts';
import { italianTitleCase } from '#lib/format/names.ts';
import { getLocale } from '#lib/paraglide/runtime.js';
import { courseSlot } from '#lib/ui/courseSlot.ts';
import type { CourseSlot } from '#lib/ui/types.ts';
import {
	coursePlaylist,
	courseOutline,
	type OutlineSection,
	type PlaylistItem
} from './content.ts';
import { courseByIdQuery, courseContentsQuery, courseInfoQuery } from './queries.ts';
import { videoProgressQuery, type ProgressMap } from './video/progress.ts';

/** A member of the course's staff, in the page's order, with the role it is listed under. */
export interface StaffMember extends MoodleWebStaffMember {
	role: 'teacher' | 'tutor' | 'assistant' | 'staff';
}

function staffRole(title: string): StaffMember['role'] {
	const lower = title.trim().toLowerCase();
	if (lower.startsWith('docent') || lower.startsWith('teacher')) return 'teacher';
	if (lower.startsWith('tutor')) return 'tutor';
	if (lower.startsWith('esercitat')) return 'assistant';
	return 'staff';
}

/** Moodle names some courses in capitals, as Esse3 does; they read better in title case. */
function readable(name: string): string {
	return /\p{Ll}/u.test(name) ? name : italianTitleCase(name);
}

/**
 * Everything the course pages share: the course record, its contents and public page, the
 * account's video progress, and what derives from them. The layout creates it from the route's
 * course id; every page under `/elearning/[course]` reads it with `getCourse()`.
 */
export class CourseState {
	readonly #id: () => number;
	readonly language = getLocale();

	readonly contents: Query<MoodleCourseSection[]>;
	readonly record: Query<MoodleCoursesByFieldResponse>;
	readonly info: Query<MoodleWebCourseInfo>;
	readonly enrolled: Query<MoodleEnrolledCourse[]>;
	readonly progress: Query<ProgressMap>;

	readonly slot: CourseSlot;
	readonly outline: OutlineSection[] | undefined;
	readonly playlist: PlaylistItem[];
	/** The course as the course list knows it, when the student is enrolled. */
	readonly membership: MoodleEnrolledCourse | undefined;
	readonly course: MoodleCourse | undefined;
	readonly title: string | undefined;
	readonly code: CourseCode;
	/** "2025/26", or null for spaces outside the academic years. */
	readonly period: string | null;
	readonly staff: StaffMember[];
	/** The teacher the hero names: the first listed as such, else whoever comes first. */
	readonly teacher: string | undefined;
	/** Whether the course is loading, which the hero's shapes show by turning. */
	readonly loading: boolean;
	readonly favourite: boolean;

	/** Call while a component initializes: the queries live as long as it does. */
	constructor(id: () => number) {
		this.#id = id;
		const signedIn = <T>(build: (scope: SessionScope) => QueryOptions<T>) =>
			query(() => {
				const scope = session.current;
				return scope && build(scope);
			});
		this.contents = signedIn((scope) => courseContentsQuery(scope, id()));
		this.record = signedIn((scope) => courseByIdQuery(scope, id()));
		this.info = signedIn((scope) => courseInfoQuery(scope, id()));
		this.enrolled = signedIn(enrolledCoursesQuery);
		this.progress = signedIn(videoProgressQuery);

		this.slot = $derived(courseSlot(id()));
		this.outline = $derived(this.contents.data && courseOutline(this.contents.data, this.language));
		this.playlist = $derived(this.outline ? coursePlaylist(this.outline, this.language) : []);
		this.membership = $derived(this.enrolled.data?.find((course) => course.id === id()));
		this.course = $derived(this.record.data?.courses[0]);
		this.title = $derived.by(() => {
			const name = this.membership?.fullName ?? this.course?.fullName;
			return name === undefined ? undefined : readable(stripMlang(name, this.language).trim());
		});
		this.code = $derived(
			parseCourseCode(this.membership?.idNumber ?? this.course?.idNumber ?? null)
		);
		this.period = $derived(periodLabel(this.code));
		this.staff = $derived(
			(this.info.data?.staff ?? []).flatMap((group) =>
				group.members.map((member) => ({ ...member, role: staffRole(group.role) }))
			)
		);
		this.teacher = $derived(
			(this.staff.find((member) => member.role === 'teacher') ?? this.staff[0])?.name ??
				this.course?.contacts[0]?.fullName
		);
		this.loading = $derived(
			this.contents.refreshing ||
				this.record.refreshing ||
				(this.contents.data === undefined && this.contents.status === 'pending')
		);
		this.favourite = $derived.by(() => {
			const scope = session.current;
			const course = this.membership;
			return (
				scope !== undefined &&
				course !== undefined &&
				favouritesOf(scope.account.id).has({
					id: course.id,
					moodleFavourite: course.isFavourite === true
				})
			);
		});
	}

	get id(): number {
		return this.#id();
	}

	/**
	 * Reloads what the course pages show, as the cover's refresh button asks: the course itself,
	 * and the tabs' quizzes, assignments with their statuses, and forums.
	 */
	refresh = async (): Promise<void> => {
		const account = session.current?.account;
		if (account !== undefined) {
			invalidate(
				accountKey(account, 'moodle.quiz.getQuizzesByCourses', this.id),
				accountKey(account, 'moodle.assign.getAssignments', this.id),
				accountKey(account, 'moodle.assign.getSubmissionStatus'),
				accountKey(account, 'moodle.forum.getForumsByCourses', this.id)
			);
		}
		await Promise.all([this.contents.refresh(), this.record.refresh(), this.info.refresh()]);
	};

	toggleFavourite = (): void => {
		const scope = session.current;
		if (scope === undefined || this.membership === undefined) return;
		void favouritesOf(scope.account.id).set([this.id], !this.favourite);
	};
}

export const [getCourse, setCourse] = createContext<CourseState>();
