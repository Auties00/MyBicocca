import { esse3Date } from '#lib/data/esse3/dates.ts';
import { toDate } from '#lib/format/dates.ts';

/** An Esse3 date as a local midnight; null when blank or unreadable. */
export function day(text: string | null | undefined): Date | null {
	const date = esse3Date(text);
	return date === null ? null : toDate(date);
}

export function nonBlank(text: string | null | undefined): string | null {
	const value = text?.trim();
	return value === undefined || value === '' ? null : value;
}

/** Ascending, with the missing values last in their original order. */
export function ascending<T extends Date | number>(a: T | null, b: T | null): number {
	if (a === null || b === null) return a === b ? 0 : a === null ? 1 : -1;
	return Number(a) - Number(b);
}

/** Descending, with the missing values last in their original order. */
export function descending<T extends Date | number>(a: T | null, b: T | null): number {
	if (a === null || b === null) return a === b ? 0 : a === null ? 1 : -1;
	return Number(b) - Number(a);
}

/** `25/26` for 2025, the short form a year tile shows. */
export function shortYear(year: number): string {
	const two = (value: number) => String(value % 100).padStart(2, '0');
	return `${two(year)}/${two(year + 1)}`;
}
