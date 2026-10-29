import {
	decodeEsse3TeachingUnitWithQuestionnaire,
	decodeEsse3TranscriptRowWithQuestionnaireStatus
} from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { isPending, moduleName, toActivities, toActivityUnits } from './model.ts';

function row(adsceId: number, adDes: string, statoLink: number | null) {
	const value = decodeEsse3TranscriptRowWithQuestionnaireStatus({
		matId: 1,
		adsceId,
		adCod: `E3101Q${adsceId}`,
		adDes,
		stato: 'P',
		peso: 8,
		statoLink
	});
	if (value === undefined) throw new Error('not an object');
	return value;
}

describe('toActivities', () => {
	it('puts what is left to fill in first, keeping the record book order otherwise', () => {
		const activities = toActivities([
			row(1, 'ALGORITMI E STRUTTURE DATI', 1),
			row(2, 'BASI DI DATI', 3),
			row(3, 'PROBABILITA E STATISTICA', 1),
			row(4, 'SISTEMI OPERATIVI', 2),
			row(5, 'LINGUAGGI DI PROGRAMMAZIONE', 4)
		]);
		expect(activities.map((activity) => [activity.id, activity.status])).toEqual([
			[2, 'todo'],
			[4, 'partial'],
			[5, 'misconfigured'],
			[1, 'done'],
			[3, 'done']
		]);
	});

	it('leaves out activities without questionnaires', () => {
		expect(toActivities([row(1, 'STAGE', 0), row(2, 'PROVA FINALE', null)])).toEqual([]);
	});

	it('writes Esse3 capitals in title case', () => {
		expect(toActivities([row(1, 'BASI DI DATI', 3)])[0].name).toBe('Basi di Dati');
	});
});

describe('isPending', () => {
	it('counts questionnaires not started or half done, not misconfigured ones', () => {
		expect(
			['todo', 'partial', 'done', 'misconfigured'].map((status) => isPending(status as never))
		).toEqual([true, true, false, false]);
	});
});

describe('moduleName', () => {
	it('drops the course name Esse3 repeats before a module', () => {
		expect(moduleName('Basi di Dati - Teoria', 'Basi di Dati')).toBe('Teoria');
		expect(moduleName('Basi di Dati – Laboratorio', 'Basi di Dati')).toBe('Laboratorio');
		expect(moduleName('BASI DI DATI: LABORATORIO', 'Basi di Dati')).toBe('LABORATORIO');
	});

	it('keeps a name that only starts like the course, or is the course', () => {
		expect(moduleName('Basi di Dati Avanzate', 'Basi di Dati')).toBe('Basi di Dati Avanzate');
		expect(moduleName('Basi di Dati', 'Basi di Dati')).toBe('Basi di Dati');
		expect(moduleName('Laboratorio', 'Basi di Dati')).toBe('Laboratorio');
	});
});

function units(rows: Record<string, unknown>[], fields: Record<string, unknown> = {}) {
	const value = decodeEsse3TeachingUnitWithQuestionnaire({
		adsceId: 20,
		questionarioId: 7001,
		questConfigId: 7101,
		questionarioDes: 'Valutazione della didattica',
		anonimoFlg: 1,
		udLogPdsListWeb: rows,
		...fields
	});
	if (value === undefined) throw new Error('not an object');
	return value;
}

const unitRow = (fields: Record<string, unknown> = {}) => ({
	udDes: 'BASI DI DATI - TEORIA',
	adDes: 'BASI DI DATI',
	docentiNome: 'PAOLO',
	docentiCognome: 'FERRI',
	domPartCod: 'N0',
	domPartDes: 'NESSUN PARTIZIONAMENTO',
	statoLink: 3,
	tagsValdid: 'UD=1',
	...fields
});

describe('toActivityUnits', () => {
	it('names the lecturer, and the class only when the course is split', () => {
		const [whole, split] = toActivityUnits(
			units([unitRow(), unitRow({ domPartCod: 'T2', domPartDes: 'Turno M-Z' })])
		).units;
		expect(whole.lecturer).toBe('Paolo Ferri');
		expect(whole.partition).toBeNull();
		expect(split.partition).toBe('Turno M-Z');
	});

	it('leaves out units that cannot start a compilation, keeping the others in place', () => {
		const result = toActivityUnits(
			units([unitRow({ tagsValdid: ' ' }), unitRow({ statoLink: 1, tagsValdid: 'UD=2' })])
		);
		expect(result.units).toHaveLength(1);
		expect(result.units[0]).toMatchObject({ index: 2, done: true, tags: 'UD=2' });
	});

	it('falls back to the activity name without a unit name, and to none without a lecturer', () => {
		const [unit] = toActivityUnits(
			units([unitRow({ udDes: null, docentiNome: null, docentiCognome: ' ' })])
		).units;
		expect(unit.name).toBe('Basi di Dati');
		expect(unit.lecturer).toBeNull();
	});

	it('has no questionnaire while the evaluation window is closed', () => {
		const closed = toActivityUnits(units([unitRow()], { questConfigId: null }));
		expect(closed.questionnaire).toBeNull();
		expect(toActivityUnits(units([unitRow()])).questionnaire).toEqual({ id: 7001, configId: 7101 });
	});
});
