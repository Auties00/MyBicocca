import type {
	Esse3ForeignTitlePerson,
	Esse3HighSchoolDiplomaPerson,
	Esse3ItalianTitlePerson,
	Esse3PersonTitles
} from '@mybicocca/data-api/esse3';
import type { LocalDate } from '@mybicocca/data-api/runtime';
import { esse3Date } from '#lib/data/esse3/dates.ts';

/** In the order of a life: school, then Italian degrees, then foreign ones. */
export const TITLE_CATEGORIES = ['high_school', 'italian', 'foreign'] as const;
export type TitleCategory = (typeof TITLE_CATEGORIES)[number];

/** Esse3's `staTit*Cod`: C awarded ("conseguito"), I hypothesised ("in ipotesi"). */
export type TitleStatus = 'awarded' | 'hypothesised' | 'unknown';

export type TitleField =
	| 'academic_year'
	| 'graduation_year'
	| 'award_date'
	| 'session'
	| 'achievement_years'
	| 'duration_years'
	| 'grade_average'
	| 'credits'
	| 'institution'
	| 'city'
	| 'province'
	| 'country'
	| 'same_university'
	| 'institution_code'
	| 'language'
	| 'thesis_title'
	| 'study_path'
	| 'equivalent_path'
	| 'deposit_type'
	| 'value_declaration'
	| 'evaluated'
	| 'recognized';

/** A value as Esse3 holds it, formatted for the reader's locale when shown. */
export type TitleValue =
	| { kind: 'text'; text: string }
	| { kind: 'number'; value: number }
	| { kind: 'years'; count: number }
	| { kind: 'date'; date: LocalDate }
	| { kind: 'flag'; set: boolean };

export interface TitleAttribute {
	field: TitleField;
	value: TitleValue;
}

export interface TitleGrade {
	value: number;
	/** The scale's top, such as 100 or 110; null when Esse3 gives none. */
	base: number | null;
}

/** One qualification of the person: a high-school diploma, an Italian or a foreign degree. */
export interface AcademicTitle {
	/** Stable across loads: `sup-…`, `titit-…`, `titstra-…` after Esse3's own ids. */
	id: string;
	category: TitleCategory;
	status: TitleStatus;
	typeDescription: string | null;
	subject: string | null;
	institution: string | null;
	country: string | null;
	year: string | null;
	grade: TitleGrade | null;
	/** A grade in words (some foreign or older titles), when there is no number. */
	gradeText: string | null;
	cumLaude: boolean;
	attributes: TitleAttribute[];
}

const text = (value: string | null): string | null => {
	const trimmed = value?.trim();
	return trimmed ? trimmed : null;
};

function status(code: string | null): TitleStatus {
	switch (code?.trim().toUpperCase()) {
		case 'C':
			return 'awarded';
		case 'I':
			return 'hypothesised';
		default:
			return 'unknown';
	}
}

/** A grade only when there is one: Esse3 writes 0 for "none". */
function grade(value: number | null, base: number | null): TitleGrade | null {
	if (value === null || value <= 0) return null;
	return { value, base: base !== null && base > 0 ? base : null };
}

/** Collects the attributes Esse3 filled, in the order given. */
class Attributes {
	readonly list: TitleAttribute[] = [];

	text(field: TitleField, value: string | null): this {
		const trimmed = text(value);
		if (trimmed !== null) this.list.push({ field, value: { kind: 'text', text: trimmed } });
		return this;
	}

	/** A year, a session or a code that Esse3 sends as a number: shown as written. */
	label(field: TitleField, value: number | null): this {
		return value === null || value <= 0 ? this : this.text(field, String(value));
	}

	number(field: TitleField, value: number | null): this {
		if (value !== null && value > 0) this.list.push({ field, value: { kind: 'number', value } });
		return this;
	}

	years(field: TitleField, count: number | null): this {
		if (count !== null && count > 0) this.list.push({ field, value: { kind: 'years', count } });
		return this;
	}

	date(field: TitleField, value: string | null): this {
		const date = esse3Date(value);
		if (date !== null) this.list.push({ field, value: { kind: 'date', date } });
		else this.text(field, value);
		return this;
	}

	flag(field: TitleField, value: number | null): this {
		if (value === 0 || value === 1)
			this.list.push({ field, value: { kind: 'flag', set: value === 1 } });
		return this;
	}
}

function highSchool(row: Esse3HighSchoolDiplomaPerson, index: number): AcademicTitle {
	const institution = text(row.schoolName) ?? text(row.schoolDescription);
	const country = text(row.deliveryNationDescription);
	return {
		id: `sup-${row.id ?? index}`,
		category: 'high_school',
		status: status(row.italianTitleStatusCode),
		typeDescription: text(row.higherTitleTypesDescription),
		subject: null,
		institution,
		country,
		year: row.highSchoolGraduationYear === null ? null : String(row.highSchoolGraduationYear),
		grade: grade(row.grade, row.maxGrade),
		gradeText: text(row.alphanumericGrade),
		cumLaude: row.cumLaudeFlag === 1,
		attributes: new Attributes()
			.label('graduation_year', row.highSchoolGraduationYear)
			.date('award_date', row.highSchoolGraduationDate)
			.years('duration_years', row.anniScolarita)
			.text('institution', institution)
			.text('city', row.schoolMunicipalityDescription)
			.text('province', row.schoolMunicipalityAbbreviation)
			.text('country', country)
			.text('institution_code', row.schoolMiurCode)
			.text('language', row.teachingLanguageDescription)
			.text('deposit_type', row.depositTypesDescription)
			.flag('value_declaration', row.valueDeclarationFlag)
			.flag('evaluated', row.evaluatedFlag).list
	};
}

function italian(row: Esse3ItalianTitlePerson, index: number): AcademicTitle {
	const institution = text(row.p06UniversitiesDescription);
	const year =
		text(row.p06AcademicYearDescription) ??
		(row.academicYearTitleAward === null ? null : String(row.academicYearTitleAward));
	return {
		id: `titit-${row.italianTitleId ?? index}`,
		category: 'italian',
		status: status(row.italianTitleStatusCode),
		typeDescription: text(row.italianTitleTypesDescription),
		subject: text(row.vDecodeTitleTypeCodeDescription) ?? text(row.courseOfStudyDescription),
		institution,
		country: null,
		year,
		grade: grade(row.grade, row.baseGrade),
		gradeText: null,
		cumLaude: row.cumLaude === 1,
		attributes: new Attributes()
			.text('academic_year', year)
			.date('award_date', row.titleDeliveryDate)
			.text('session', row.session)
			.years('achievement_years', row.achievementYearsNumber)
			.number('grade_average', row.gradesAverage)
			.number('credits', row.credits)
			.text('institution', institution)
			.flag('same_university', row.sameUniversityFlag)
			.text('institution_code', row.p06UniversitiesIstatCode)
			.text('language', row.languageDescription)
			.text('thesis_title', row.thesisTitle)
			.text('study_path', row.studyPath)
			.text('deposit_type', row.depositTypesDescription)
			.flag('evaluated', row.evaluatedFlag).list
	};
}

function foreign(row: Esse3ForeignTitlePerson, index: number): AcademicTitle {
	const institution =
		text(row.p06SiteDescription) ??
		text(row.universityDescription) ??
		text(row.p01ForeignTestDescription);
	const country = text(row.p01NationDescription);
	const year =
		text(row.p06TeachingActivityDescription) ??
		(row.academicYearAwardId === null ? null : String(row.academicYearAwardId));
	return {
		id: `titstra-${row.foreignTitleId ?? index}`,
		category: 'foreign',
		status: status(row.foreignTitleStatusCode),
		typeDescription: text(row.titleStatusTypesDescription),
		subject: text(row.foreignCourseOfStudy),
		institution,
		country,
		year,
		grade: grade(row.grade, row.baseGrade),
		gradeText: text(row.alphanumericGrade),
		cumLaude: row.cumLaude === 1,
		attributes: new Attributes()
			.text('academic_year', year)
			.date('award_date', row.titleDeliveryDate)
			.years('duration_years', row.durationYears)
			.text('institution', institution)
			.text('city', row.deliveryForeignCity)
			.text('country', country)
			.text('language', row.teachingLanguageDescription)
			.text('equivalent_path', row.equivalentPath)
			.text('deposit_type', row.depositTypesDescription)
			.flag('value_declaration', row.valueDeclarationFlag)
			.flag('evaluated', row.evaluatedFlag)
			.flag('recognized', row.equivalentTitleFlag).list
	};
}

/** The person's qualifications: diplomas, then Italian, then foreign titles, each in Esse3's order. */
export function toTitles(titles: Esse3PersonTitles): AcademicTitle[] {
	return [
		...titles.SUP.map(highSchool),
		...titles.italianTitle.map(italian),
		...titles.foreignTitle.map(foreign)
	];
}

/** The title's name: what was studied, else its kind, else where. */
export function titleHeadline(title: AcademicTitle): string | null {
	return title.subject ?? title.typeDescription ?? title.institution;
}

/** Kind, place, country and year, each once and none repeating the headline. */
export function titleDetails(title: AcademicTitle): string[] {
	const headline = titleHeadline(title);
	const parts = [
		title.typeDescription,
		title.institution,
		title.category === 'foreign' ? title.country : null,
		title.year
	];
	return parts.filter(
		(part, index): part is string =>
			part !== null && part !== headline && parts.indexOf(part) === index
	);
}

/**
 * When the title was earned, as a sortable `yyyymmdd`: its award date, else the end of the last
 * year its year names (2024 for "2023/2024"); null when Esse3 gives neither.
 */
function earnedOn(title: AcademicTitle): number | null {
	for (const { field, value } of title.attributes) {
		if (field === 'award_date' && value.kind === 'date') {
			return value.date.year * 10_000 + value.date.month * 100 + value.date.day;
		}
	}
	const years = title.year?.match(/\d{4}/g);
	return years ? Number(years[years.length - 1]) * 10_000 + 1231 : null;
}

/**
 * The most recent title: the one earned last, by award date or else by year. Titles that say
 * neither count as older than those that do; among equals the later in the list wins.
 */
export function latestTitle(titles: readonly AcademicTitle[]): AcademicTitle | undefined {
	let latest: AcademicTitle | undefined;
	let latestOn = Number.NEGATIVE_INFINITY;
	for (const title of titles) {
		const on = earnedOn(title) ?? Number.NEGATIVE_INFINITY;
		if (latest === undefined || on >= latestOn) {
			latest = title;
			latestOn = on;
		}
	}
	return latest;
}

export interface TitleGroup {
	category: TitleCategory;
	titles: AcademicTitle[];
}

/** Titles by category, in {@link TITLE_CATEGORIES} order, leaving out empty ones. */
export function groupTitles(titles: readonly AcademicTitle[]): TitleGroup[] {
	return TITLE_CATEGORIES.map((category) => ({
		category,
		titles: titles.filter((title) => title.category === category)
	})).filter((group) => group.titles.length > 0);
}
