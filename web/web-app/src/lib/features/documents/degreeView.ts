import IconBank from '~icons/ph/bank';
import IconFileText from '~icons/ph/file-text';
import IconGlobe from '~icons/ph/globe-hemisphere-west';
import IconGraduationCap from '~icons/ph/graduation-cap';
import IconMedal from '~icons/ph/medal';
import IconPath from '~icons/ph/path';
import IconStar from '~icons/ph/star';
import IconStudent from '~icons/ph/student';
import { formatDate } from '#lib/format/dates.ts';
import { formatNumber } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Icon } from '#lib/ui/types.ts';
import {
	titleDetails,
	titleHeadline,
	type AcademicTitle,
	type TitleCategory,
	type TitleField,
	type TitleStatus,
	type TitleValue
} from './degrees.ts';
import { FactList, type FactSection } from './factSections.ts';
import { joinParts } from './text.ts';

export const categoryIcons: Record<TitleCategory, Icon> = {
	high_school: IconStudent,
	italian: IconGraduationCap,
	foreign: IconGlobe
};

export function categoryLabel(category: TitleCategory): string {
	switch (category) {
		case 'high_school':
			return m.documents_degrees_category_high_school();
		case 'italian':
			return m.documents_degrees_category_italian();
		case 'foreign':
			return m.documents_degrees_category_foreign();
	}
}

export function titleStatusLabel(status: TitleStatus): string | undefined {
	switch (status) {
		case 'awarded':
			return m.documents_degrees_status_awarded();
		case 'hypothesised':
			return m.documents_degrees_status_hypothesised();
		case 'unknown':
			return undefined;
	}
}

export function headline(title: AcademicTitle): string {
	return titleHeadline(title) ?? m.documents_degrees_headline_fallback();
}

/** The line under the headline in the list: kind, place, country and year. */
export function detailsLine(title: AcademicTitle): string {
	return joinParts(titleDetails(title));
}

/** The detail's subtitle: the first thing besides the headline that names the title. */
export function subtitle(title: AcademicTitle): string {
	return titleDetails(title)[0] ?? m.shell_service_degrees();
}

/**
 * A grade on its scale, "92/100"; in words for a title graded so. With `laude`, honours mark the
 * value as exam grades do ("30L"): "110L/110".
 */
export function gradeShort(title: AcademicTitle, laude = false): string | undefined {
	if (title.grade === null) return title.gradeText ?? undefined;
	const value = formatNumber(title.grade.value, { maximumFractionDigits: 2 });
	const marked = laude && title.cumLaude ? `${value}L` : value;
	return title.grade.base === null ? marked : `${marked}/${formatNumber(title.grade.base)}`;
}

/** A grade with its scale and honours, spelled out: "110/110 with honours". */
export function gradeLine(title: AcademicTitle): string | undefined {
	const grade = gradeShort(title);
	return grade !== undefined && title.cumLaude ? m.common_grade_cum_laude_full({ grade }) : grade;
}

function fieldLabel(field: TitleField, category: TitleCategory): string {
	const school = category === 'high_school';
	switch (field) {
		case 'academic_year':
			return m.documents_degrees_field_academic_year();
		case 'graduation_year':
			return m.documents_degrees_field_graduation_year();
		case 'award_date':
			return m.documents_degrees_field_award_date();
		case 'session':
			return m.documents_degrees_field_session();
		case 'achievement_years':
			return m.documents_degrees_field_achievement_years();
		case 'duration_years':
			return m.documents_degrees_field_duration_years();
		case 'grade_average':
			return m.documents_degrees_field_grade_average();
		case 'credits':
			return m.documents_degrees_field_credits();
		case 'institution':
			return school
				? m.documents_degrees_field_institution_school()
				: m.documents_degrees_field_institution_university();
		case 'city':
			return m.documents_degrees_field_city();
		case 'province':
			return m.documents_degrees_field_province();
		case 'country':
			return m.documents_degrees_field_country();
		case 'same_university':
			return m.documents_degrees_field_same_university();
		case 'institution_code':
			return school
				? m.documents_degrees_field_code_miur()
				: m.documents_degrees_field_code_istat();
		case 'language':
			return m.documents_degrees_field_language();
		case 'thesis_title':
			return m.documents_degrees_field_thesis_title();
		case 'study_path':
			return m.documents_degrees_field_study_path();
		case 'equivalent_path':
			return m.documents_degrees_field_equivalent_path();
		case 'deposit_type':
			return m.documents_degrees_field_deposit_type();
		case 'value_declaration':
			return m.documents_degrees_field_value_declaration();
		case 'evaluated':
			return m.documents_degrees_field_evaluated();
		case 'recognized':
			return m.documents_degrees_field_recognized();
	}
}

function valueText(value: TitleValue): string {
	switch (value.kind) {
		case 'text':
			return value.text;
		case 'number':
			return formatNumber(value.value, { maximumFractionDigits: 2 });
		case 'years':
			return m.documents_years({ count: value.count });
		case 'date':
			return formatDate(value.date, 'long');
		case 'flag':
			return value.set ? m.documents_yes() : m.documents_no();
	}
}

const CODES: readonly TitleField[] = ['institution_code'];

/** Which section each field belongs to, in the order the sections show. */
const SECTIONS: ReadonlyArray<{
	id: string;
	title: () => string;
	icon: Icon;
	fields: readonly TitleField[];
}> = [
	{
		id: 'achievement',
		title: m.documents_degrees_section_achievement,
		icon: IconMedal,
		fields: [
			'academic_year',
			'graduation_year',
			'award_date',
			'session',
			'achievement_years',
			'duration_years'
		]
	},
	{
		id: 'evaluation',
		title: m.documents_degrees_section_evaluation,
		icon: IconStar,
		fields: ['grade_average', 'credits']
	},
	{
		id: 'institution',
		title: m.documents_degrees_section_institution,
		icon: IconBank,
		fields: [
			'institution',
			'city',
			'province',
			'country',
			'same_university',
			'institution_code',
			'language'
		]
	},
	{
		id: 'path',
		title: m.documents_degrees_section_path,
		icon: IconPath,
		fields: ['thesis_title', 'study_path', 'equivalent_path']
	},
	{
		id: 'documentation',
		title: m.documents_degrees_section_documentation,
		icon: IconFileText,
		fields: ['deposit_type', 'value_declaration', 'evaluated', 'recognized']
	}
];

/** What the year beside the status counts: the diploma's year, or a degree's academic year. */
export function heroYearLabel(category: TitleCategory): string {
	return category === 'high_school'
		? m.documents_degrees_field_graduation_year()
		: m.documents_degrees_field_academic_year();
}

/* The years the hero shows beside the status. */
const HERO_YEARS: readonly TitleField[] = ['academic_year', 'graduation_year'];

/**
 * Every field Esse3 filled, by topic. The grade, the status and the year lead the page on their
 * own, so the sections leave them out; empty sections are dropped.
 */
export function titleSections(title: AcademicTitle): FactSection[] {
	return SECTIONS.map((section) => {
		const facts = new FactList();
		for (const attribute of title.attributes) {
			if (!section.fields.includes(attribute.field)) continue;
			const { field, value } = attribute;
			if (HERO_YEARS.includes(field) && value.kind === 'text' && value.text === title.year)
				continue;
			facts.add(fieldLabel(field, title.category), valueText(value), CODES.includes(field));
		}
		return { id: section.id, title: section.title(), icon: section.icon, facts: facts.items };
	}).filter((section) => section.facts.length > 0);
}
