import { describe, expect, it } from 'vitest';
import { javaHashCode, lessonPool, lessonSwatchIndex } from './palette.ts';

describe('javaHashCode', () => {
	it('matches Java, overflow included', () => {
		expect(javaHashCode('')).toBe(0);
		expect(javaHashCode('hello')).toBe(99162322);
		expect(javaHashCode('Aa')).toBe(javaHashCode('BB'));
		expect(javaHashCode('polygenelubricants')).toBe(-2147483648);
	});

	it('hashes UTF-16 code units, as Java strings are', () => {
		expect(javaHashCode('à')).toBe(224);
		expect(javaHashCode('😀')).toBe(55357 * 31 + 56832);
	});
});

describe('lessonSwatchIndex', () => {
	it('picks the swatches the Android demo shows for the persona courses', () => {
		expect(lessonSwatchIndex('PROBABILITÀ E STATISTICA', 'x')).toBe(0);
		expect(lessonSwatchIndex('LINGUAGGI DI PROGRAMMAZIONE', 'x')).toBe(1);
		expect(lessonSwatchIndex('BASI DI DATI', 'x')).toBe(2);
		expect(lessonSwatchIndex('ALGORITMI E STRUTTURE DATI', 'x')).toBe(3);
		expect(lessonSwatchIndex('SISTEMI OPERATIVI', 'x')).toBe(5);
	});

	it('ignores case and surrounding blanks, and falls back to the id', () => {
		expect(lessonSwatchIndex('  Basi di Dati ', 'x')).toBe(lessonSwatchIndex('BASI DI DATI', 'y'));
		expect(lessonSwatchIndex('   ', 'lesson_42')).toBe(
			(javaHashCode('lesson_42') & 0x7fffffff) % lessonPool.length
		);
	});

	it('always lands in the pool', () => {
		expect(lessonSwatchIndex('polygenelubricants', 'x')).toBe(0);
		expect(lessonPool).toHaveLength(16);
	});
});
