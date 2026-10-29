import { describe, expect, it } from 'vitest';
import {
	headlinePercent,
	minimumPercent,
	registerTitle,
	type Classroom,
	type CourseAttendance,
	type Register
} from './model.ts';

function classroom(percentage: number, requirementProgress: number): Classroom {
	return { percentage, requirementProgress, lessonsAttended: 0, hours: 0, status: 'in-progress' };
}

function register(recordedPercentage: number | null): Register {
	return { cmId: 1, name: 'Presenze', attended: null, recordedPercentage };
}

function course(overrides: Partial<CourseAttendance>): CourseAttendance {
	return {
		slug: 'c',
		name: 'Corso',
		code: null,
		year: 1,
		semester: 'first',
		credits: 6,
		teacher: null,
		classroom: null,
		registers: [],
		modules: [],
		elearningId: null,
		...overrides
	};
}

describe('minimumPercent', () => {
	it('is attendance over progress while the requirement is unmet', () => {
		expect(minimumPercent(classroom(38, 50.67))).toBe(75);
	});

	it('is unknown before any progress, which would divide by zero', () => {
		expect(minimumPercent(classroom(0, 0))).toBeNull();
	});

	it('is not stated once met, when the ratio only bounds it', () => {
		expect(minimumPercent(classroom(90, 100))).toBeNull();
		expect(minimumPercent(classroom(95, 120))).toBeNull();
	});
});

describe('registerTitle', () => {
	it('drops the Presenze prefix and sets the rest in sentence case', () => {
		expect(registerTitle('Presenze LABORATORIO')).toBe('Laboratorio');
		expect(registerTitle('  presenze esercitazioni turno A ')).toBe('Esercitazioni turno a');
	});

	it('reads as nothing when the name is only the prefix', () => {
		expect(registerTitle('Presenze')).toBe('');
		expect(registerTitle('   ')).toBe('');
	});

	it('keeps a name without the prefix', () => {
		expect(registerTitle('Laboratorio di reti')).toBe('Laboratorio di reti');
	});
});

describe('headlinePercent', () => {
	it('prefers the classroom, rounded', () => {
		expect(
			headlinePercent(course({ classroom: classroom(88.6, 100), registers: [register(40)] }))
		).toBe(89);
	});

	it('falls back to the first register that recorded anything', () => {
		expect(headlinePercent(course({ registers: [register(null), register(66.67)] }))).toBe(67);
	});

	it('is null when no source recorded anything', () => {
		expect(headlinePercent(course({ registers: [register(null)] }))).toBeNull();
	});
});
