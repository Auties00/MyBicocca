/**
 * The academic year a date falls in, by the September it starts: 2026 for 2026/27. Lessons and
 * study plans roll over in September; Android rolls enrollments over in August (`rollover: 8`).
 */
export function academicYear(date: Date = new Date(), rollover = 9): number {
	return date.getMonth() + 1 >= rollover ? date.getFullYear() : date.getFullYear() - 1;
}

/** `2026/27` for 2026, as Esse3 and EasyStaff write academic years. */
export function academicYearLabel(year: number): string {
	return `${year}/${String((year + 1) % 100).padStart(2, '0')}`;
}

/** `2627` for 2026, the prefix of Moodle course idnumbers. */
export function academicYearCode(year: number): string {
	return `${String(year % 100).padStart(2, '0')}${String((year + 1) % 100).padStart(2, '0')}`;
}
