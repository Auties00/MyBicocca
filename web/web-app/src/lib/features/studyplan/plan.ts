import type {
	Esse3State3,
	Esse3StudyPlan,
	Esse3StudyPlanActivity,
	Esse3StudyPlanHeader
} from '@mybicocca/data-api/esse3';
import type { LocalDate } from '@mybicocca/data-api/runtime';
import { esse3Date } from '#lib/data/esse3/dates.ts';

export type PlanType = 'standard' | 'individual' | 'unknown';

/** One activity the student has in plan. */
export interface PlanCourse {
	/** The in-plan row's id: `adsceId`, else the choice ids Esse3 falls back on. */
	id: number;
	name: string;
	code: string | null;
	credits: number;
	/** The degree year; 0 for activities tied to no year. */
	year: number;
}

/** The student's study plan as the page shows it. */
export interface StudyPlan {
	planId: number;
	studentId: number;
	type: PlanType;
	state: Esse3State3 | null;
	lastChange: LocalDate | null;
	regulationId: number | null;
	schemaId: number | null;
	courses: PlanCourse[];
}

export interface PlanYear {
	/** 0 gathers the activities tied to no year. */
	year: number;
	courses: PlanCourse[];
	credits: number;
	/** Credits of the year's passed activities. */
	earned: number;
}

function planType(type: string | null): PlanType {
	if (type === 'S') return 'standard';
	if (type === 'I') return 'individual';
	return 'unknown';
}

/**
 * Esse3 returns the whole choice tree of the plan: only the activities actually chosen
 * (`sceltaFlg` other than 0) are in it. Rows without an id or a name can't be shown.
 */
function toCourse(activity: Esse3StudyPlanActivity): PlanCourse | undefined {
	if (activity.choiceFlag === 0) return undefined;
	const key = activity.contextualizedTeachingActivityKey;
	const id =
		activity.activityChoiceId ?? activity.choiceActivityId ?? activity.teachingActivityChoiceId;
	const name = activity.activityTranscriptDescription?.trim() || key?.activityDescription?.trim();
	if (id === null || !name) return undefined;
	return {
		id,
		name,
		code: activity.activityTranscriptCode?.trim() || key?.activityCode?.trim() || null,
		credits: activity.weight ?? 0,
		year: activity.courseYear ?? 0
	};
}

/** The plan of `header`, read from `plan` (asked with every optional field). */
export function toStudyPlan(header: Esse3StudyPlanHeader, plan: Esse3StudyPlan): StudyPlan {
	return {
		planId: header.planId ?? plan.planId ?? 0,
		studentId: header.studentId ?? plan.studentId ?? 0,
		type: planType(header.planType ?? plan.planType),
		state: header.state ?? plan.state,
		lastChange: esse3Date(header.lastStateChangeDate ?? plan.lastStateChangeDate),
		regulationId: header.choiceRegulationId ?? plan.choiceRegulationId,
		schemaId: header.schemaId ?? plan.schemaId,
		courses: plan.activity.flatMap((activity) => toCourse(activity) ?? [])
	};
}

/** The plan's years in order, with the year-less activities last; `passed` tells earned credits. */
export function groupByYear(
	courses: readonly PlanCourse[],
	passed: (course: PlanCourse) => boolean = () => false
): PlanYear[] {
	const years = new Map<number, PlanCourse[]>();
	for (const course of courses) {
		const year = Math.max(0, course.year);
		years.set(year, [...(years.get(year) ?? []), course]);
	}
	const credits = (items: readonly PlanCourse[]) =>
		items.reduce((sum, course) => sum + course.credits, 0);
	return [...years.entries()]
		.sort(([a], [b]) => (a === 0 ? 1 : b === 0 ? -1 : a - b))
		.map(([year, items]) => ({
			year,
			courses: items,
			credits: credits(items),
			earned: credits(items.filter(passed))
		}));
}

/** Esse3 writes path names in capitals ("PERCORSO COMUNE"): those read as a sentence. */
export function sentenceCase(text: string): string {
	const trimmed = text.trim();
	if (/\p{Ll}/u.test(trimmed)) return trimmed;
	const lower = trimmed.toLocaleLowerCase('it-IT');
	return lower.charAt(0).toLocaleUpperCase('it-IT') + lower.slice(1);
}
