import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
import { describe, expect, it } from 'vitest';
import { editionsFor, knownTurni } from './courses.ts';

const course = (id: number, idNumber: string, hidden = false) =>
	({ id, idNumber, hidden, fullName: `Course ${id}`, displayName: null }) as MoodleEnrolledCourse;

describe('editionsFor', () => {
	it('orders newest year first, visible before hidden, the base course before its streams', () => {
		const editions = editionsFor(
			[
				course(1, '2425-2-E3101Q106'),
				course(2, '2526-2-E3101Q106-T2'),
				course(3, '2526-2-E3101Q106'),
				course(4, '2526-2-E3101Q106-T1'),
				course(7, '2526-2-E3101Q106-T3', true),
				course(5, '2526-2-E3101Q999'),
				course(6, 'E3101Q106')
			],
			'e3101q106'
		);
		expect(editions.map((edition) => [edition.id, edition.periodLabel])).toEqual([
			[3, '2025/26'],
			[4, '2025/26 T1'],
			[2, '2025/26 T2'],
			[7, '2025/26 T3'],
			[1, '2024/25']
		]);
	});
});

describe('knownTurni', () => {
	it('names the turno of a course followed in a single stream', () => {
		const turni = knownTurni([
			course(1, '2526-2-E3101Q113-T2'),
			course(2, '2526-2-E3101Q128-T1'),
			course(3, '2526-2-E3101Q128-T2'),
			course(4, '2526-2-E3101Q103')
		]);
		expect([...turni]).toEqual([['E3101Q113', 2]]);
	});
});
