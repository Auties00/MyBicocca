import { describe, expect, it } from 'vitest';
import { decodeEsse3TranscriptTest } from '@mybicocca/data-api/esse3';
import { attempts, outcomeLabel } from './attempts.ts';

function test(body: Record<string, unknown>) {
	const decoded = decodeEsse3TranscriptTest({ adregId: 1, adsceId: 2, matId: 3, ...body });
	if (decoded === undefined) throw new Error('bad fixture');
	return decoded;
}

const result = (body: Record<string, unknown>) => ({ modValCod: 'V', supEsaFlg: 0, ...body });

describe('attempts', () => {
	it('lists the most recent first, undated last', () => {
		const list = attempts([
			test({ adregId: 1, dataApp: '10/01/2025' }),
			test({ adregId: 2 }),
			test({ adregId: 3, dataApp: '12/06/2025' })
		]);
		expect(list.map((attempt) => attempt.id)).toEqual([3, 1, 2]);
	});

	it('takes the final outcome, then the written one, then the partial one', () => {
		const [attempt] = attempts([
			test({
				esitoFinale: result({ supEsaFlg: 0 }),
				esitoScr: result({ voto: 15 }),
				esitoParziale: result({ voto: 27.5, supEsaFlg: 1 })
			})
		]);
		expect(attempt.outcome?.grade).toBe(15);
	});

	it('ignores grades of outcomes judged without one', () => {
		const [attempt] = attempts([
			test({ esitoFinale: result({ modValCod: 'G', voto: 0, supEsaFlg: 1, tipoGiudDes: ' ' }) })
		]);
		expect(attempt.outcome).toMatchObject({ grade: null, judgment: null, passed: true });
	});

	it('blanks empty strings', () => {
		const [attempt] = attempts([test({ sesDes: '  ', staRegDes: ' Verbalizzato ' })]);
		expect(attempt.session).toBeNull();
		expect(attempt.status).toBe('Verbalizzato');
	});
});

describe('outcomeLabel', () => {
	const base = { passed: true, grade: null, cumLaude: false, judgment: null, date: null };

	it('prefers laude, then the grade, the judgment, the bare pass', () => {
		expect(outcomeLabel({ ...base, grade: 30, cumLaude: true })).toEqual({ kind: 'laude' });
		expect(outcomeLabel({ ...base, grade: 27.5, judgment: 'Buono' })).toEqual({
			kind: 'grade',
			value: 27.5
		});
		expect(outcomeLabel({ ...base, judgment: 'Idoneo' })).toEqual({
			kind: 'judgment',
			text: 'Idoneo'
		});
		expect(outcomeLabel(base)).toEqual({ kind: 'passed' });
		expect(outcomeLabel({ ...base, passed: false })).toBeNull();
	});
});
