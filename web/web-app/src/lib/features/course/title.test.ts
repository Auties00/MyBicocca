import { describe, expect, it } from 'vitest';
import { coverTitle } from './title.ts';

describe('coverTitle', () => {
	it('sets the last word apart', () => {
		expect(coverTitle('Algoritmi e Strutture Dati')).toEqual({
			head: 'Algoritmi e Strutture',
			tail: 'Dati',
			tag: null
		});
		expect(coverTitle('Fisica')).toEqual({ head: '', tail: 'Fisica', tag: null });
	});

	it('keeps a course’s number with the word it counts', () => {
		expect(coverTitle('Analisi Matematica II')).toEqual({
			head: 'Analisi',
			tail: 'Matematica II',
			tag: null
		});
		expect(coverTitle('Programmazione 1')).toEqual({
			head: '',
			tail: 'Programmazione 1',
			tag: null
		});
	});

	it('keeps what Moodle appends to a teaching apart as a tag', () => {
		expect(coverTitle('Reti e Sistemi Operativi (blended)')).toEqual({
			head: 'Reti e Sistemi',
			tail: 'Operativi',
			tag: 'blended'
		});
		expect(coverTitle('Basi di Dati - T1')).toMatchObject({ tail: 'Dati', tag: 'T1' });
		expect(coverTitle('Erasmus 2025-2026')).toMatchObject({ tail: '2025-2026', tag: null });
		expect(coverTitle('E3102Q - E3101Q - Comunicazioni')).toMatchObject({
			tail: 'Comunicazioni',
			tag: null
		});
	});

	it('leaves a title that is only a tag alone', () => {
		expect(coverTitle('(Laboratorio)')).toEqual({ head: '', tail: '(Laboratorio)', tag: null });
	});
});
