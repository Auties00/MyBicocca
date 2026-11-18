import type { ExamGrade } from '#lib/data/esse3/grades.ts';
import { m } from '#lib/paraglide/messages.js';
import { cached, intlLocale, type AppLocale } from './locale.ts';

/** A grade out of thirty as badges show it: "28", or "30L" for 30 cum laude. */
export function formatGradeValue(grade: { value: number; cumLaude: boolean }): string {
	return grade.cumLaude ? '30L' : String(grade.value);
}

/**
 * An exam outcome as a short label ("28", "30L", "Id.", "Rit."), or spelled out with `full`
 * for screen readers and details ("30 e lode", "Idoneo"). An unknown outcome is a dash.
 */
export function formatGrade(
	grade: ExamGrade,
	options: { full?: boolean; locale?: AppLocale } = {}
): string {
	const locale = options.locale === undefined ? {} : { locale: options.locale };
	const full = options.full === true;
	switch (grade.kind) {
		case 'numeric':
			if (!grade.cumLaude) return String(grade.value);
			return full ? m.common_grade_cum_laude_full({ grade: grade.value }, locale) : '30L';
		case 'passed':
			return full ? m.common_grade_passed_full({}, locale) : m.common_grade_passed({}, locale);
		case 'not_passed':
			return full
				? m.common_grade_not_passed_full({}, locale)
				: m.common_grade_not_passed({}, locale);
		case 'withdrew':
			return full ? m.common_grade_withdrew_full({}, locale) : m.common_grade_withdrew({}, locale);
		case 'absent':
			return full ? m.common_grade_absent_full({}, locale) : m.common_grade_absent({}, locale);
		case 'unknown':
			return '—';
	}
}

/** An average of grades, always with two decimals: 27,45. */
export function formatAverage(value: number, locale?: AppLocale): string {
	const tag = intlLocale(locale);
	const options = { minimumFractionDigits: 2, maximumFractionDigits: 2 };
	return cached('number', tag, options, () => new Intl.NumberFormat(tag, options)).format(value);
}
