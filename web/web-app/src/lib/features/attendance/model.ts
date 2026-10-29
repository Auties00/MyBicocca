export type Semester = 'first' | 'second' | 'unknown';

/** EasyBadge's standing against the course's minimum attendance. */
export type ClassroomStatus = 'attending' | 'in-progress' | 'not-attending';

/** Lessons certified with the classroom badge (EasyBadge). */
export interface Classroom {
	/** Share of the lessons held so far the student attended, 0–100. */
	percentage: number;
	lessonsAttended: number;
	hours: number;
	/** Progress towards the course's minimum attendance, 0–100: 100 means met. */
	requirementProgress: number;
	status: ClassroomStatus;
}

/** A Moodle attendance activity of the course's edition this year. */
export interface AttendanceModule {
	courseId: number;
	cmId: number;
	name: string;
}

/** A Moodle attendance register (mod_attendance) summary. */
export interface Register {
	cmId: number;
	/** The activity's name, as the teacher wrote it ("Presenze laboratorio"). */
	name: string;
	attended: number | null;
	/**
	 * Attendance over the sessions taken so far, 0–100. The register's total isn't kept: a lab's
	 * holds every group's sessions, so 10 of 82 would mislead.
	 */
	recordedPercentage: number | null;
}

/** A course the student still has to attend, with what each source records of it. */
export interface CourseAttendance {
	/** The course's URL segment. */
	slug: string;
	name: string;
	code: string | null;
	/** The degree year; 0 when unknown. */
	year: number;
	semester: Semester;
	credits: number;
	teacher: string | null;
	classroom: Classroom | null;
	registers: Register[];
	modules: AttendanceModule[];
	/** The Moodle course of this academic year's edition. */
	elearningId: number | null;
}

/** The URL segment of a course: its Esse3 code, else its name as words joined by dashes. */
export function courseSlug(code: string | null, name: string): string {
	if (code) return code;
	return name
		.normalize('NFD')
		.replace(/\p{M}/gu, '')
		.toLowerCase()
		.replace(/[^a-z0-9]+/g, '-')
		.replace(/^-|-$/g, '');
}

/** The percentage the course is summed up by: the classroom's, else the first register's. */
export function headlinePercent(course: CourseAttendance): number | null {
	if (course.classroom) return Math.round(course.classroom.percentage);
	const register = course.registers.find((item) => item.recordedPercentage !== null);
	return register?.recordedPercentage == null ? null : Math.round(register.recordedPercentage);
}

/**
 * EasyStaff sends no minimum, but its progress is the attendance measured against it: the minimum
 * is attendance / progress, exact while unmet. Once met the ratio only bounds it, so it is not
 * stated then.
 */
export function minimumPercent(classroom: Classroom): number | null {
	const progress = classroom.requirementProgress;
	if (progress <= 0 || progress >= 100) return null;
	return Math.round((classroom.percentage / progress) * 100);
}

/** "Presenze LABORATORIO" reads "Laboratorio"; a name that is only the prefix reads as nothing. */
export function registerTitle(name: string): string {
	let text = name.trim();
	if (text.toLowerCase().startsWith('presenze')) text = text.slice('presenze'.length);
	text = text.trim().toLocaleLowerCase('it-IT');
	return text.charAt(0).toLocaleUpperCase('it-IT') + text.slice(1);
}

/**
 * The hue of a percentage on the vermilion → amber → green ramp of the semantic tones (oklch
 * hues 38, 70, 150): amber sits at half, as on Android's red → orange → green ramp.
 */
export function rampHue(percent: number): number {
	const fraction = Math.min(1, Math.max(0, percent / 100));
	return fraction < 0.5
		? 38 + (70 - 38) * (fraction / 0.5)
		: 70 + (150 - 70) * ((fraction - 0.5) / 0.5);
}
