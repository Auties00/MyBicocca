import { decodeEsse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import {
	archiveByYear,
	attempts,
	splitResults,
	summarizeArchive,
	toResult,
	visibleChoice
} from './model.ts';

const TODAY = new Date(2026, 9, 27, 15, 30);

const pad = (value: number) => String(value).padStart(2, '0');

/** `dd/MM/yyyy`, with ` HH:mm:ss` when given, `offset` days from TODAY. */
function at(offset: number, time?: string): string {
	const date = new Date(2026, 9, 27 + offset);
	const day = `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
	return time === undefined ? day : `${day} ${time}`;
}

let next = 900;

/** A published result as `/prenotazioni?filter=pubblId=gt=0` sends it, waiting for a decision. */
function row(fields: Record<string, unknown> = {}) {
	const value = decodeEsse3ExamSessionEnrollment({
		applistaId: next++,
		cdsId: 10,
		adId: 20,
		appId: 1,
		adStuCod: 'E3101Q119',
		adStuDes: 'LABORATORIO DI PROGRAMMAZIONE WEB',
		desAppello: 'LABORATORIO DI PROGRAMMAZIONE WEB',
		dataOraTurno: at(-6, '09:30:00'),
		pubblId: 3,
		presaVisione: 'N',
		dataRifEsitoStu: at(5, '23:59:59'),
		esito: { votoEsa: 27, superatoFlg: 1, assenteFlg: 0, ritiratoFlg: 0 },
		...fields
	});
	if (value === undefined) throw new Error('not an object');
	return value;
}

const result = (fields: Record<string, unknown> = {}) => {
	const value = toResult(row(fields), TODAY);
	if (value === undefined) throw new Error('no call key');
	return value;
};

describe('toResult', () => {
	it('counts the days left to decide, 0 on the last day, only while a decision waits', () => {
		expect(result().daysLeft).toBe(5);
		expect(result({ dataRifEsitoStu: at(0, '23:59:59') }).daysLeft).toBe(0);
		expect(result({ dataRifEsitoStu: at(-1) }).daysLeft).toBeNull();
		expect(result({ presaVisione: 'A' }).daysLeft).toBeNull();
	});

	it('waits for a decision only on a grade or a pass, never on a failure or an absence', () => {
		expect(result().pending).toBe(true);
		expect(result({ esito: { superatoFlg: 1 } }).pending).toBe(true);
		expect(result({ esito: { superatoFlg: 0, votoEsa: 0 } }).pending).toBe(false);
		expect(result({ esito: { assenteFlg: 1 } }).pending).toBe(false);
		expect(result({ esito: { assenteFlg: 1 } }).daysLeft).toBeNull();
	});

	it('tells a missed deadline apart from an answer or a failure', () => {
		expect(result({ dataRifEsitoStu: at(-1) }).lapsed).toBe(true);
		expect(result({ dataRifEsitoStu: at(-1), presaVisione: 'A' }).lapsed).toBe(false);
		expect(result({ dataRifEsitoStu: at(-1), esito: { superatoFlg: 0, votoEsa: 0 } }).lapsed).toBe(
			false
		);
		expect(result({ dataRifEsitoStu: at(-1), presaVisione: null }).lapsed).toBe(false);
	});

	it('names the exam by the activity, title-cased, and keeps a call name only when it adds to it', () => {
		const same = result({ desAppello: 'Laboratorio di  programmazione web' });
		expect(same.title).toBe('Laboratorio di Programmazione Web');
		expect(same.callDescription).toBeNull();
		expect(result({ desAppello: 'Appello straordinario' }).callDescription).toBe(
			'Appello straordinario'
		);
		expect(result({ desAppello: 'LABORATORIO DI PROG WEB-scritto completo' }).callDescription).toBe(
			'Scritto completo'
		);
		expect(result({ adStuDes: ' ', desAppello: 'BASI DI DATI' }).title).toBe('Basi di Dati');
		expect(result({ adStuDes: null, desAppello: null }).title).toBe('');
	});

	it('addresses a result by its booking id, else by its call', () => {
		expect(result({ applistaId: 4242 }).id).toBe('4242');
		expect(result({ applistaId: 4242 }).booking).toBe('10-20-1');
		expect(result({ applistaId: null }).id).toBe('10-20-1');
		expect(toResult(row({ appId: null }), TODAY)).toBeUndefined();
	});

	it('reads the day of the exam with or without a time', () => {
		expect(result({ dataOraTurno: at(-6, '09:30:00') }).takenOn).toEqual(
			new Date(2026, 9, 21, 9, 30)
		);
		expect(result({ dataOraTurno: at(-6) }).takenOn).toEqual(new Date(2026, 9, 21));
		expect(result({ dataOraTurno: null }).takenOn).toBeNull();
	});
});

describe('splitResults', () => {
	it('puts the soonest deadline first and keeps the archive in Esse3’s order', () => {
		const rows = [
			row({ applistaId: 1, dataRifEsitoStu: at(9) }),
			row({ applistaId: 2, presaVisione: 'A' }),
			row({ applistaId: 3, dataRifEsitoStu: at(2) }),
			row({ applistaId: 4, appId: null }),
			row({ applistaId: 5, presaVisione: 'R' })
		];
		const { pending, archived } = splitResults(rows, TODAY);
		expect(pending.map((item) => item.id)).toEqual(['3', '1']);
		expect(archived.map((item) => item.id)).toEqual(['2', '5']);
	});
});

describe('attempts', () => {
	it('lists the other sittings of the same activity, the latest first', () => {
		const failed = result({ applistaId: 1, dataOraTurno: '08/06/2025 09:30', presaVisione: 'R' });
		const passed = result({ applistaId: 2, dataOraTurno: '06/07/2025 09:30', presaVisione: 'A' });
		const absent = result({ applistaId: 3, dataOraTurno: null, esito: { assenteFlg: 1 } });
		const other = result({ applistaId: 4, adStuCod: 'E3101Q103' });
		const all = [failed, passed, absent, other];
		expect(attempts(failed, all).map((item) => item.id)).toEqual(['2', '3']);
		expect(attempts(other, all)).toEqual([]);
	});

	it('matches by name when Esse3 gives no code, and never matches nameless results', () => {
		const named = [
			result({ applistaId: 1, adStuCod: null }),
			result({ applistaId: 2, adStuCod: null }),
			result({ applistaId: 3, adStuCod: null, adStuDes: 'BASI DI DATI', desAppello: null })
		];
		expect(attempts(named[0], named).map((item) => item.id)).toEqual(['2']);
		const nameless = [1, 2].map((applistaId) =>
			result({ applistaId, adStuCod: null, adStuDes: null, desAppello: null })
		);
		expect(attempts(nameless[0], nameless)).toEqual([]);
	});
});

describe('visibleChoice', () => {
	it('shows an answer only for outcomes there was something to answer', () => {
		expect(visibleChoice(result({ presaVisione: 'R' }))).toBe('rejected');
		expect(visibleChoice(result({ presaVisione: 'A', esito: { superatoFlg: 1 } }))).toBe(
			'accepted'
		);
		expect(
			visibleChoice(result({ presaVisione: 'R', esito: { superatoFlg: 0, votoEsa: 0 } }))
		).toBeNull();
		expect(visibleChoice(result({ presaVisione: 'N' }))).toBeNull();
	});
});

describe('archiveByYear', () => {
	it('groups by academic year, which starts in September, the latest first and undated last', () => {
		const archived = [
			result({ dataOraTurno: '15/09/2026 09:00' }),
			result({ dataOraTurno: null }),
			result({ dataOraTurno: '20/07/2026 09:00' }),
			result({ dataOraTurno: '10/02/2026 09:00' }),
			result({ dataOraTurno: '01/09/2025 09:00' })
		];
		expect(archiveByYear(archived).map((group) => [group.year, group.results.length])).toEqual([
			[2026, 1],
			[2025, 3],
			[null, 1]
		]);
	});
});

describe('summarizeArchive', () => {
	it('counts what stands, what failed or was rejected, and the absences', () => {
		const archived = [
			result({ esito: { assenteFlg: 1 } }),
			result({ presaVisione: 'A' }),
			result({ presaVisione: 'R' }),
			result({ esito: { superatoFlg: 0, votoEsa: 0 } }),
			result({ presaVisione: null, esito: { superatoFlg: 1 } }),
			result({ esito: { ritiratoFlg: 1 } })
		];
		expect(summarizeArchive(archived)).toEqual({ passed: 2, notPassed: 2, absences: 2 });
	});
});
