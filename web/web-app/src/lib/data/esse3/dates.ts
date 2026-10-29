import { LocalDate, LocalDateTime } from '@mybicocca/data-api/runtime';

/**
 * Esse3 sends most dates as `dd/MM/yyyy` strings, often with a ` HH:mm:ss` filler (00:00:00 or
 * 23:59:59), and some as ISO dates. Blank or unreadable input is null, never an exception.
 */
export function esse3Date(text: string | null | undefined): LocalDate | null {
	const value = text?.trim();
	if (value === undefined || value === '') return null;
	const day = value.split(/[ T]/, 1)[0];
	return LocalDate.parse(day, 'dd/MM/yyyy') ?? LocalDate.parse(day) ?? null;
}

/** An Esse3 `dd/MM/yyyy HH:mm[:ss]` (or ISO) date and time; null when blank or unreadable. */
export function esse3DateTime(text: string | null | undefined): LocalDateTime | null {
	const value = text?.trim();
	if (value === undefined || value === '') return null;
	return (
		LocalDateTime.parse(value, 'dd/MM/yyyy HH:mm:ss') ??
		LocalDateTime.parse(value, 'dd/MM/yyyy HH:mm') ??
		LocalDateTime.parse(value) ??
		null
	);
}
