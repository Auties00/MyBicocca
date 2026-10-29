import { createContext } from 'svelte';
import type { Query } from '#lib/data/query.svelte.ts';
import type { CheckInFlow } from './checkInFlow.svelte.ts';
import type { CourseAttendance } from './model.ts';

/** What the attendance layout shares with the course and check-in it shows. */
export interface AttendanceShell {
	/** The course list stays beside the open pane, which then carries its own heading. */
	readonly wide: boolean;
	readonly courses: Query<CourseAttendance[]>;
	readonly flow: CheckInFlow | undefined;
}

export const [getAttendanceShell, setAttendanceShell] = createContext<AttendanceShell>();
