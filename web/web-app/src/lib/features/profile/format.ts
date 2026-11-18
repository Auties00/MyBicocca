import type { ExamGrade } from '#lib/data/esse3/grades.ts';
import { formatGrade } from '#lib/format/grades.ts';
import type { Grade } from './transcript.ts';

/** A grade out of thirty: "28", "30L", or spelled out with `full` ("30 e lode"). */
export function gradeText(grade: Grade, full = false): string {
	return formatGrade({ kind: 'numeric', ...grade }, { full });
}

/** A record-book row's outcome as the kit's Grade draws it: its grade, or a pass without one. */
export function rowGrade(grade: Grade | null, passed: boolean): ExamGrade {
	if (grade) return { kind: 'numeric', ...grade };
	return passed ? { kind: 'passed' } : { kind: 'unknown' };
}
