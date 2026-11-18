import type {
	Esse3ChoiceRegulation,
	Esse3ChoiceRegulationWindow,
	Esse3PlanSchema,
	Esse3StudyPath,
	Esse3StudyPlanHeader
} from '@mybicocca/data-api/esse3';
import type { LocalDate } from '@mybicocca/data-api/runtime';
import { esse3Date } from '#lib/data/esse3/dates.ts';

/** One facet of a path (percorso, orientamento, profilo, part-time): at least one text is set. */
export interface Facet {
	code: string | null;
	description: string | null;
	/** The English name, when the structure catalog has one. */
	descriptionEn: string | null;
}

/** How Esse3 approves a plan compiled on a schema (`tipoApprovazione` 0, 1, 2). */
export type Approval = 'automatic' | 'manual' | 'if-compliant' | 'unknown';

/** A path the student can compile the plan on: one plan schema of the choice regulation. */
export interface PathOption {
	schemaId: number;
	schemaCode: string | null;
	schemaDescription: string | null;
	schemaDescriptionEn: string | null;
	percorso: Facet | null;
	orientamento: Facet | null;
	profilo: Facet | null;
	partTime: Facet | null;
	/** The schema the current plan was compiled on. */
	isCurrent: boolean;
	approval: Approval;
	/** A server-side condition the schema is gated by, which only Esse3 can evaluate. */
	conditionNote: string | null;
	languages: { it: string; en: string | null }[];
}

/** The student's path and what it allows, as Esse3's plan header and choice regulation say. */
export interface StudyPath {
	percorso: Facet | null;
	orientamento: Facet | null;
	profilo: Facet | null;
	partTime: Facet | null;
	regulationId: number | null;
	currentSchemaId: number | null;
	options: PathOption[];
	/** A compilation window of the regulation is open today. */
	editingOpen: boolean;
	/** The last day of the open window, when it has one. */
	windowEnd: LocalDate | null;
	/** Open, and more than one path to pick from. */
	choiceAvailable: boolean;
}

const dayKey = (date: LocalDate) => date.year * 10_000 + date.month * 100 + date.day;

/** Whether `today` falls between two Esse3 dates, a missing bound being open. */
function within(start: string | null, end: string | null, today: LocalDate): boolean {
	const from = esse3Date(start);
	const to = esse3Date(end);
	const day = dayKey(today);
	return (from === null || dayKey(from) <= day) && (to === null || day <= dayKey(to));
}

/** The compilation window open `today`, if any. */
export function openWindow(
	windows: readonly Esse3ChoiceRegulationWindow[],
	today: LocalDate
): Esse3ChoiceRegulationWindow | undefined {
	return windows.find((window) => within(window.startDate, window.endDate, today));
}

/** For a career with no plan yet: the regulation of its newest ordinamento and revision. */
export function pickRegulation(
	regulations: readonly Esse3ChoiceRegulation[]
): Esse3ChoiceRegulation | undefined {
	const rank = (regulation: Esse3ChoiceRegulation) =>
		(regulation.academicYearOrderId ?? 0) * 10_000 + (regulation.academicYearRevisionId ?? 0);
	return regulations
		.filter((regulation) => regulation.choiceRegulationId !== null)
		.reduce<Esse3ChoiceRegulation | undefined>(
			(best, regulation) =>
				best === undefined || rank(regulation) > rank(best) ? regulation : best,
			undefined
		);
}

function text(value: string | null | undefined): string | null {
	const trimmed = value?.trim();
	return trimmed ? trimmed : null;
}

function facet(
	code: string | null | undefined,
	description: string | null | undefined,
	descriptionEn: string | null = null
): Facet | null {
	const cleanCode = text(code);
	const cleanDescription = text(description);
	if (cleanCode === null && cleanDescription === null) return null;
	return { code: cleanCode, description: cleanDescription, descriptionEn: text(descriptionEn) };
}

/** What makes two schemas the same choice: those differing only by approval are duplicates. */
function pathTuple(schema: Esse3PlanSchema): string {
	return JSON.stringify([
		schema.studyPlanCode,
		text(schema.orientationCode),
		text(schema.professionCode),
		text(schema.aptCode)
	]);
}

function approval(type: number | null): Approval {
	switch (type) {
		case 0:
			return 'automatic';
		case 1:
			return 'manual';
		case 2:
			return 'if-compliant';
		default:
			return 'unknown';
	}
}

function toOption(
	schema: Esse3PlanSchema,
	isCurrent: boolean,
	catalog: ReadonlyMap<string, Esse3StudyPath>
): PathOption {
	const entry = schema.studyPlanCode === null ? undefined : catalog.get(schema.studyPlanCode);
	return {
		schemaId: schema.schemaId ?? 0,
		schemaCode: text(schema.schemaCode),
		schemaDescription: text(schema.schemaDescription),
		schemaDescriptionEn: text(schema.schemaDescriptionEnglish),
		percorso: facet(
			schema.studyPlanCode,
			schema.studyPlanDescription ?? entry?.studyPlanDescription,
			entry?.studyPlanDescriptionEnglish
		),
		orientamento: facet(schema.orientationCode, schema.orientationDescription),
		profilo: facet(schema.professionCode, schema.professionDescription),
		partTime: facet(schema.aptCode, schema.aptDescription),
		isCurrent,
		approval: approval(schema.approvalType),
		conditionNote: text(schema.conditionDescription),
		languages: (entry?.teachingLanguages ?? []).flatMap((language) => {
			const it = text(language.teachingLanguageDescription) ?? text(language.teachingLanguageCode);
			return it === null ? [] : [{ it, en: text(language.teachingLanguageDescriptionEnglish) }];
		})
	};
}

export interface PathSources {
	/** The plan header (asked with every optional field); undefined when there is no plan. */
	header: Esse3StudyPlanHeader | undefined;
	regulationId: number | null;
	schemas: readonly Esse3PlanSchema[];
	windows: readonly Esse3ChoiceRegulationWindow[];
	catalog: readonly Esse3StudyPath[];
	today: LocalDate;
}

/**
 * The path from the plan header and the regulation's schemas. Percorso and part-time live on the
 * header, orientamento and profilo only on the current schema. The options are the schemas a
 * student can pick today (shown on the web and inside their validity) plus the current one even
 * when hidden or expired, one per distinct path. Of the schemas sharing the current path, the
 * plan's own stands for it: another one may differ in approval, and resubmitting the same path
 * must not turn an approved plan into one awaiting approval.
 */
export function buildStudyPath(sources: PathSources): StudyPath {
	const { header, schemas, today } = sources;
	const catalog = new Map(
		sources.catalog.flatMap((entry) =>
			entry.studyPlanCode === null ? [] : [[entry.studyPlanCode, entry] as const]
		)
	);
	const current =
		header?.schemaId === null || header?.schemaId === undefined
			? undefined
			: schemas.find((schema) => schema.schemaId === header.schemaId);
	const currentTuple = current === undefined ? undefined : pathTuple(current);

	const percorsoCode = header?.studyPlanChoiceCode ?? header?.studyPlanStudentCode;
	const percorso = facet(
		percorsoCode,
		header?.studyPlanChoiceDescription ??
			header?.studyPlanDescription ??
			current?.studyPlanDescription,
		percorsoCode ? catalog.get(percorsoCode)?.studyPlanDescriptionEnglish : null
	);

	const seen = new Set<string>();
	const options: PathOption[] = [];
	for (const schema of schemas) {
		const tuple = pathTuple(schema);
		const pickable =
			schema.webViewFlag !== 0 &&
			within(schema.evaluationStartDate, schema.evaluationEndDate, today);
		if ((!pickable && tuple !== currentTuple) || seen.has(tuple)) continue;
		seen.add(tuple);
		const isCurrent = current !== undefined && tuple === currentTuple;
		options.push(toOption(isCurrent ? current : schema, isCurrent, catalog));
	}

	const window = openWindow(sources.windows, today);
	const editingOpen = window !== undefined;
	return {
		percorso,
		orientamento: facet(current?.orientationCode, current?.orientationDescription),
		profilo: facet(current?.professionCode, current?.professionDescription),
		partTime: facet(
			header?.aptCode ?? current?.aptCode,
			header?.aptDescription ?? current?.aptDescription
		),
		regulationId: sources.regulationId,
		currentSchemaId: current?.schemaId ?? null,
		options,
		editingOpen,
		windowEnd: window === undefined ? null : esse3Date(window.endDate),
		choiceAvailable: editingOpen && options.length > 1
	};
}

/** A facet's name: the English one in English when there is one, else the description or code. */
export function facetLabel(value: Facet, english: boolean): string {
	return (english ? value.descriptionEn : null) ?? value.description ?? value.code ?? '';
}
