import { describe, expect, it } from 'vitest';
import type { CalendarEvent, LessonEvent } from './model.ts';
import { counterpart, splitCourses } from './turni.ts';

const lesson = (
	title: string,
	activityCode: string | null,
	turno: number | null,
	id = title,
	date = '2026-10-27',
	start = 600,
	end = 720
) => ({ id, source: 'lesson', title, activityCode, turno, date, start, end }) as LessonEvent;

describe('splitCourses', () => {
	it('lists the courses whose lessons come in more than one turno', () => {
		const courses = splitCourses([
			lesson('ANALISI E PROGETTO DI ALGORITMI (T2 LEZ + ESE + LAB)', 'E3101Q113', 2),
			lesson('ANALISI E PROGETTO DI ALGORITMI (T1 LEZ + ESE + LAB)', 'E3101Q113', 1),
			lesson('RICERCA OPERATIVA (T1 FRONTALE TURNO 1)', 'E3101Q128', 1),
			lesson('BASI DI DATI (TU FRONTALE TURNO UNICO)', 'E3101Q103', null),
			lesson('RETI (T2 FRONTALE TURNO 2)', null, 2),
			{ source: 'exam', title: 'RETI (T1)', activityCode: 'E3101Q128' } as CalendarEvent
		]);
		expect([...courses.values()].map((course) => [course.activityCode, course.turni])).toEqual([
			['E3101Q113', [1, 2]]
		]);
	});
});

describe('counterpart', () => {
	const t1 = lesson('APA (T1 LEZ)', 'E3101Q113', 1, 'a', '2026-10-27', 630, 750);
	it("finds the chosen turno's lesson that overlaps the hidden one most", () => {
		const events = [
			t1,
			lesson('APA (T2 LEZ)', 'E3101Q113', 2, 'b', '2026-10-27', 870, 990),
			lesson('APA (T2 LEZ)', 'E3101Q113', 2, 'c', '2026-10-27', 600, 720),
			lesson('APA (T2 LEZ)', 'E3101Q113', 2, 'd', '2026-10-28', 630, 750),
			lesson('RO (T2 FRONTALE TURNO 2)', 'E3101Q128', 2, 'e', '2026-10-27', 630, 750)
		];
		expect(counterpart(t1, 2, events)?.id).toBe('c');
	});

	it('finds nothing when the chosen turno has no lesson in that slot', () => {
		const events = [t1, lesson('APA (T2 LEZ)', 'E3101Q113', 2, 'b', '2026-10-27', 870, 990)];
		expect(counterpart(t1, 2, events)).toBeUndefined();
	});
});
