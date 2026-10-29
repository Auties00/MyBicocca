import type {
	Esse3TeachingUnitLogStudyPlanWebList,
	Esse3TeachingUnitWithQuestionnaire,
	Esse3TranscriptRowWithQuestionnaireStatus
} from '@mybicocca/data-api/esse3';
import { italianTitleCase, personName } from '#lib/format/names.ts';

/** Esse3's `statoLink`: what is left to fill in for an activity. */
export type ActivityStatus = 'todo' | 'partial' | 'done' | 'misconfigured';

export interface QuestionnaireActivity {
	/** `adsceId`, the record-book row. */
	id: number;
	code: string | null;
	name: string;
	credits: number;
	status: ActivityStatus;
}

const STATUSES: Readonly<Record<number, ActivityStatus>> = {
	1: 'done',
	2: 'partial',
	3: 'todo',
	4: 'misconfigured'
};

/**
 * Whether a questionnaire waits to be filled in, as Esse3 counts it: not started or half done. A
 * misconfigured one may be fillable or not, so it isn't counted.
 */
export function isPending(status: ActivityStatus): boolean {
	return status === 'todo' || status === 'partial';
}

/**
 * The activities with a questionnaire, those not done first (misconfigured ones among them, for
 * the student to check) and in the record book's order otherwise. Rows without questionnaires
 * (`statoLink` 0 or missing) are left out.
 */
export function toActivities(
	rows: readonly Esse3TranscriptRowWithQuestionnaireStatus[]
): QuestionnaireActivity[] {
	const activities: QuestionnaireActivity[] = [];
	for (const row of rows) {
		const status = row.linkState === null ? undefined : STATUSES[row.linkState];
		if (status === undefined) continue;
		activities.push({
			id: row.activityChoiceId,
			code: row.activityCode,
			name: italianTitleCase(row.activityDescription),
			credits: row.weight,
			status
		});
	}
	return activities.sort((a, b) => Number(a.status === 'done') - Number(b.status === 'done'));
}

export interface TeachingUnit {
	/** 1-based position among the activity's units, as the compile page's URL names it. */
	index: number;
	name: string;
	lecturer: string | null;
	/** The class the student belongs to ("Turno A-L"), when the course is split. */
	partition: string | null;
	done: boolean;
	/** The token that starts a compilation for this unit (`tagsValdid`). */
	tags: string;
}

export interface ActivityUnits {
	/** Absent while the evaluation window is closed. */
	questionnaire: { id: number; configId: number } | null;
	title: string | null;
	anonymous: boolean;
	units: TeachingUnit[];
}

/** Esse3's partition code for a course that isn't split. */
const NO_PARTITION = 'N0';

function nonBlank(text: string | null | undefined): string | null {
	const trimmed = text?.trim();
	return trimmed ? trimmed : null;
}

function toUnit(row: Esse3TeachingUnitLogStudyPlanWebList, index: number): TeachingUnit | null {
	const tags = nonBlank(row.tagsValidationDid);
	const name = nonBlank(row.teachingUnitDescription) ?? nonBlank(row.activityDescription);
	if (tags === null || name === null) return null;
	const lecturer = [row.lecturersName, row.lecturersSurname]
		.map(nonBlank)
		.filter((part) => part !== null)
		.join(' ');
	const partition = nonBlank(row.domicilePartialDescription);
	return {
		index,
		name: italianTitleCase(name),
		lecturer: lecturer ? personName(lecturer) : null,
		partition: partition && row.domicilePartialCode !== NO_PARTITION ? partition : null,
		done: row.linkState === 1,
		tags
	};
}

/**
 * The units of an activity that can be evaluated. A unit without its `tagsValdid` token can't
 * start a compilation, so it is left out; positions count every row, so they stay stable.
 */
export function toActivityUnits(response: Esse3TeachingUnitWithQuestionnaire): ActivityUnits {
	const { questionnaireId, questionConfigId } = response;
	return {
		questionnaire:
			questionnaireId !== null && questionConfigId !== null
				? { id: questionnaireId, configId: questionConfigId }
				: null,
		title: nonBlank(response.questionnaireDescription),
		anonymous: response.anonymousFlag === 1,
		units: response.teachingUnitLogWebStudyPlanList
			.map((row, index) => toUnit(row, index + 1))
			.filter((unit) => unit !== null)
	};
}

/** Who or what a unit evaluates, as its row and the compile page title it. */
export function unitTitle(unit: TeachingUnit): string {
	return unit.lecturer ?? unit.name;
}

/**
 * A module's own name, without the course's that Esse3 repeats before it: "Teoria" for "Basi di
 * Dati - Teoria" in Basi di Dati. Only a dash or a colon marks a module: "Basi di Dati Avanzate"
 * is a course of its own.
 */
export function moduleName(name: string, course: string): string {
	if (!name.toLocaleLowerCase('it').startsWith(course.toLocaleLowerCase('it'))) return name;
	const rest = name.slice(course.length);
	const separator = /^\s*[-–—:·]+\s*/u.exec(rest);
	const own = separator ? rest.slice(separator[0].length) : '';
	return own || name;
}
