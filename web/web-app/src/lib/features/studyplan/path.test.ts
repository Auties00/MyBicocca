import {
	decodeEsse3ChoiceRegulation,
	decodeEsse3ChoiceRegulationWindow,
	decodeEsse3PlanSchema,
	decodeEsse3StudyPath,
	decodeEsse3StudyPlanHeader
} from '@mybicocca/data-api/esse3';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import {
	buildStudyPath,
	facetLabel,
	openWindow,
	pickRegulation,
	type PathSources
} from './path.ts';

function wire<T>(decode: (json: unknown) => T | undefined, json: unknown): T {
	const value = decode(json);
	if (value === undefined) throw new Error('not an object');
	return value;
}

const TODAY = new LocalDate(2026, 10, 27);

const window = (start: string | null, end: string | null) =>
	wire(decodeEsse3ChoiceRegulationWindow, { finregsceId: 1, dataInizio: start, dataFine: end });

const schema = (fields: Record<string, unknown>) =>
	wire(decodeEsse3PlanSchema, { webViewFlg: 1, tipoApprovazione: 0, ...fields });

const header = wire(decodeEsse3StudyPlanHeader, {
	pianoId: 3,
	schemaId: 10,
	regsceId: 7,
	pdsStuCod: 'GGG',
	pdsDes: 'PERCORSO COMUNE'
});

function sources(fields: Partial<PathSources>): PathSources {
	return {
		header,
		regulationId: 7,
		schemas: [],
		windows: [],
		catalog: [],
		today: TODAY,
		...fields
	};
}

describe('openWindow', () => {
	it('finds the window that includes today, open bounds included', () => {
		expect(openWindow([window('01/10/2026', '26/10/2026')], TODAY)).toBeUndefined();
		expect(openWindow([window('27/10/2026', '27/10/2026')], TODAY)).toBeDefined();
		expect(openWindow([window(null, '30/11/2026 23:59:59')], TODAY)).toBeDefined();
		expect(openWindow([window('28/10/2026', null)], TODAY)).toBeUndefined();
		expect(openWindow([], TODAY)).toBeUndefined();
	});
});

describe('pickRegulation', () => {
	it('takes the newest ordinamento and revision among those with an id', () => {
		const regulations = [
			{ regsceId: 1, aaOrdId: 2024, aaRevisioneId: 3 },
			{ regsceId: 2, aaOrdId: 2025, aaRevisioneId: 0 },
			{ regsceId: null, aaOrdId: 2030, aaRevisioneId: 0 },
			{ regsceId: 4, aaOrdId: 2025, aaRevisioneId: 1 }
		].map((json) => wire(decodeEsse3ChoiceRegulation, json));
		expect(pickRegulation(regulations)?.choiceRegulationId).toBe(4);
		expect(pickRegulation([])).toBeUndefined();
	});
});

describe('buildStudyPath', () => {
	const current = schema({
		schemaId: 10,
		pdsCod: 'GGG',
		pdsDes: 'PERCORSO COMUNE',
		orientCod: 'ORI',
		orientDes: 'Orientamento A'
	});

	it('keeps the schemas pickable today plus the current one, one per path', () => {
		const path = buildStudyPath(
			sources({
				schemas: [
					{ ...current, webViewFlag: 0, evaluationEndDate: '01/01/2020' },
					schema({ schemaId: 11, pdsCod: 'GGG', orientCod: 'ORI', tipoApprovazione: 1 }),
					schema({ schemaId: 12, pdsCod: 'DSC', tipoApprovazione: 2 }),
					schema({ schemaId: 13, pdsCod: 'OLD', dataFineVal: '01/01/2026' }),
					schema({ schemaId: 14, pdsCod: 'HID', webViewFlg: 0 }),
					schema({ schemaId: 15, pdsCod: 'NUL', webViewFlg: null })
				]
			})
		);
		expect(path.options.map((option) => [option.schemaId, option.isCurrent])).toEqual([
			[10, true],
			[12, false],
			[15, false]
		]);
		expect(path.options[1].approval).toBe('if-compliant');
		expect(path.currentSchemaId).toBe(10);
	});

	it("keeps the plan's own schema for its path, wherever Esse3 lists it", () => {
		const path = buildStudyPath(
			sources({
				schemas: [
					schema({ schemaId: 11, pdsCod: 'GGG', orientCod: 'ORI', tipoApprovazione: 1 }),
					schema({ schemaId: 12, pdsCod: 'DSC' }),
					current
				]
			})
		);
		expect(path.options.map((option) => [option.schemaId, option.isCurrent])).toEqual([
			[10, true],
			[12, false]
		]);
		expect(path.options[0].approval).toBe('automatic');
		expect(path.currentSchemaId).toBe(10);
	});

	it('reads percorso and part-time from the header, orientamento from the current schema', () => {
		const path = buildStudyPath(sources({ schemas: [current] }));
		expect(path.percorso).toEqual({
			code: 'GGG',
			description: 'PERCORSO COMUNE',
			descriptionEn: null
		});
		expect(path.orientamento?.description).toBe('Orientamento A');
		expect(path.partTime).toBeNull();
	});

	it('fills names and languages from the structure catalog', () => {
		const catalog = [
			wire(decodeEsse3StudyPath, {
				pdsCod: 'DSC',
				pdsDes: 'DATA SCIENCE',
				pdsDesEng: 'Data science',
				lingueDidattica: [
					{ linguaDidCod: 'ENG', linguaDidDes: 'Inglese', linguaDidDesEng: 'English' }
				]
			})
		];
		const path = buildStudyPath(
			sources({ schemas: [schema({ schemaId: 12, pdsCod: 'DSC' })], catalog })
		);
		const [option] = path.options;
		expect(option.percorso).toEqual({
			code: 'DSC',
			description: 'DATA SCIENCE',
			descriptionEn: 'Data science'
		});
		expect(option.languages).toEqual([{ it: 'Inglese', en: 'English' }]);
		expect(option.percorso && facetLabel(option.percorso, true)).toBe('Data science');
	});

	it('opens editing during a window and offers a choice only between several paths', () => {
		const open = [window('01/10/2026', '15/11/2026')];
		const single = buildStudyPath(sources({ schemas: [current], windows: open }));
		expect(single).toMatchObject({ editingOpen: true, choiceAvailable: false });
		expect(single.windowEnd?.toString()).toBe('2026-11-15');
		const several = buildStudyPath(
			sources({ schemas: [current, schema({ schemaId: 12, pdsCod: 'DSC' })], windows: open })
		);
		expect(several.choiceAvailable).toBe(true);
		expect(buildStudyPath(sources({ schemas: [current] })).editingOpen).toBe(false);
	});

	it('works without a plan: no current path, every pickable schema an option', () => {
		const path = buildStudyPath(
			sources({ header: undefined, schemas: [current, schema({ schemaId: 12, pdsCod: 'DSC' })] })
		);
		expect(path.percorso).toBeNull();
		expect(path.currentSchemaId).toBeNull();
		expect(path.options).toHaveLength(2);
	});
});
