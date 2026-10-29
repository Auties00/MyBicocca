import {
	decodeEsse3ExamSessionEnrollment,
	decodeEsse3ExamSessionTranscript,
	decodeEsse3Invoices,
	decodeEsse3StudentDebit,
	decodeEsse3StudyPlan,
	decodeEsse3StudyPlanHeader
} from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import {
	buildDeadlines,
	currentPlanId,
	invoiceTitle,
	planCodes,
	requiresDecision,
	serviceStatus,
	summarize,
	type Deadline
} from './deadlines.ts';

const TODAY = new Date(2026, 5, 18, 15, 30);

function wire<T>(decode: (json: unknown) => T | undefined, json: Record<string, unknown>): T {
	const value = decode(json);
	if (value === undefined) throw new Error('not an object');
	return value;
}

const pad = (value: number) => String(value).padStart(2, '0');

/** `dd/MM/yyyy` (plus ` HH:mm:ss` with a time) `offset` days from TODAY. */
function at(offset: number, time?: string): string {
	const date = new Date(2026, 5, 18 + offset);
	const day = `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
	return time === undefined ? day : `${day} ${time}`;
}

const callKey = { cdsId: 10, adId: 20, appId: 1 };

const result = (fields: Record<string, unknown>) =>
	wire(decodeEsse3ExamSessionEnrollment, {
		...callKey,
		applistaId: 900,
		adStuDes: 'LABORATORIO DI PROGRAMMAZIONE WEB',
		presaVisione: 'V',
		dataRifEsitoStu: at(5),
		pubblId: 3,
		esito: { votoEsa: 27, superatoFlg: 1 },
		...fields
	});

const booking = (fields: Record<string, unknown>) =>
	wire(decodeEsse3ExamSessionEnrollment, {
		...callKey,
		applistaId: 700,
		adStuDes: 'PROBABILITÀ E STATISTICA',
		dataOraTurno: at(8, '09:30:00'),
		aulaDes: 'U4-08',
		...fields
	});

const call = (fields: Record<string, unknown>) =>
	wire(decodeEsse3ExamSessionTranscript, {
		...callKey,
		adCod: 'E3101Q107',
		adDes: 'BASI DI DATI',
		dataInizioApp: at(12),
		dataInizioIscr: at(-10),
		dataFineIscr: at(8),
		...fields
	});

const invoice = (fields: Record<string, unknown>) =>
	wire(decodeEsse3Invoices, {
		fattId: 6003,
		importoFattura: 612,
		scadFattura: at(12),
		pagatoFlg: 0,
		fattScadutaFlg: 0,
		aaId: 2025,
		...fields
	});

const charge = (fields: Record<string, unknown>) =>
	wire(decodeEsse3StudentDebit, { fattId: 6003, importoVoce: 612, ...fields });

const plan = (codes: string[]) =>
	wire(decodeEsse3StudyPlan, {
		pianoId: 1,
		attivita: codes.map((code) => ({ sceltaFlg: 1, adLibCod: code }))
	});

const ids = (deadlines: Deadline[]) => deadlines.map((item) => item.id);

describe('requiresDecision', () => {
	it('waits for a viewed or unviewed outcome until its deadline, today included', () => {
		expect(requiresDecision(result({ presaVisione: 'N' }), TODAY)).toBe(true);
		expect(requiresDecision(result({ dataRifEsitoStu: at(0) }), TODAY)).toBe(true);
	});

	it('is settled once accepted, rejected, past its deadline or not actionable', () => {
		expect(requiresDecision(result({ presaVisione: 'A' }), TODAY)).toBe(false);
		expect(requiresDecision(result({ presaVisione: 'R' }), TODAY)).toBe(false);
		expect(requiresDecision(result({ dataRifEsitoStu: at(-1) }), TODAY)).toBe(false);
		expect(requiresDecision(result({ dataRifEsitoStu: null }), TODAY)).toBe(false);
		expect(requiresDecision(result({ applistaId: null }), TODAY)).toBe(false);
	});

	it('leaves out what there is nothing to accept: a failure, an absence, an unpublished grade', () => {
		expect(requiresDecision(result({ esito: { superatoFlg: 0 } }), TODAY)).toBe(false);
		expect(requiresDecision(result({ esito: { assenteFlg: 1 } }), TODAY)).toBe(false);
		expect(requiresDecision(result({ pubblId: null }), TODAY)).toBe(false);
		expect(requiresDecision(result({ esito: { superatoFlg: 1 } }), TODAY)).toBe(true);
	});
});

describe('buildDeadlines', () => {
	it('lists a result awaiting a decision by its deadline, with its grade', () => {
		const [item] = buildDeadlines({ results: [result({})] }, TODAY);
		expect(item).toMatchObject({
			source: 'result',
			id: 'result:900',
			applicationListId: 900,
			title: 'LABORATORIO DI PROGRAMMAZIONE WEB',
			days: 5,
			urgent: true,
			grade: { kind: 'numeric', value: 27, cumLaude: false }
		});
		expect(item.date).toEqual(new Date(2026, 5, 23));
	});

	it('names a result by its call when the activity has no name', () => {
		const [item] = buildDeadlines(
			{ results: [result({ adStuDes: ' ', desAppello: 'Appello di giugno' })] },
			TODAY
		);
		expect(item.title).toBe('Appello di giugno');
	});

	it('skips results already decided and rows without a call key', () => {
		const rows = [result({ presaVisione: 'A' }), result({ appId: null, applistaId: 901 })];
		expect(buildDeadlines({ results: rows }, TODAY)).toEqual([]);
	});

	it('marks deadlines urgent within seven days', () => {
		const rows = [
			result({ applistaId: 1, dataRifEsitoStu: at(7) }),
			result({ applistaId: 2, dataRifEsitoStu: at(8) })
		];
		expect(buildDeadlines({ results: rows }, TODAY).map((item) => item.urgent)).toEqual([
			true,
			false
		]);
	});

	it('lists owed invoices, overdue ones always urgent and kept for a week', () => {
		const deadlines = buildDeadlines(
			{
				invoices: [
					invoice({ fattId: 1, scadFattura: at(12) }),
					invoice({ fattId: 2, scadFattura: at(-3), fattScadutaFlg: 1 }),
					invoice({ fattId: 3, scadFattura: at(-8), fattScadutaFlg: 1 }),
					invoice({ fattId: 4, pagatoFlg: 1 }),
					invoice({ fattId: 5, fattAnnullata: 1 }),
					invoice({ fattId: 6, scadFattura: null })
				]
			},
			TODAY
		);
		expect(deadlines.map((item) => [item.id, item.days, item.urgent])).toEqual([
			['fee:2', -3, true],
			['fee:1', 12, false]
		]);
		expect(deadlines[0]).toMatchObject({ source: 'fee', expired: true, amount: 612 });
	});

	it('tells an invoice by its instalment', () => {
		const [item] = buildDeadlines(
			{
				invoices: [invoice({})],
				charges: [charge({ voceDes: 'Contributo onnicomprensivo', rataDes: 'Terza rata' })]
			},
			TODAY
		);
		expect(item).toMatchObject({ title: 'Contributo onnicomprensivo', installment: 'Terza rata' });
	});

	it('names an invoice by its first charge, else by its payment slip', () => {
		const named = invoice({ desMav1: 'MAV PRIMA RATA' });
		expect(
			invoiceTitle(named, [
				charge({ fattId: 9, tassaDes: 'Altro' }),
				charge({ voceDes: 'Contributo onnicomprensivo' })
			])
		).toBe('Contributo onnicomprensivo');
		expect(invoiceTitle(named, [])).toBe('MAV PRIMA RATA');
		expect(invoiceTitle(invoice({}), [])).toBeNull();
	});

	it('lists booked exams from today on, with their time and room', () => {
		const deadlines = buildDeadlines(
			{
				bookings: [
					booking({}),
					booking({ applistaId: 701, dataOraTurno: at(0, '14:00:00') }),
					booking({ applistaId: 702, dataOraTurno: at(-1, '09:00:00') }),
					booking({ applistaId: 703, dataOraTurno: at(3, '00:00:00'), aulaDes: '' })
				]
			},
			TODAY
		);
		expect(deadlines.map((item) => item.days)).toEqual([0, 3, 8]);
		expect(deadlines[2]).toMatchObject({
			source: 'exam',
			id: 'exam:10/20/1/700',
			room: 'U4-08',
			time: new Date(2026, 5, 26, 9, 30)
		});
		expect(deadlines[1]).toMatchObject({ time: null, room: null });
	});

	it('keeps the earliest booking closing per exam', () => {
		const deadlines = buildDeadlines(
			{
				calls: [
					call({ appId: 2, dataFineIscr: at(20), dataInizioApp: at(25) }),
					call({ appId: 1, dataFineIscr: at(8), dataInizioApp: at(12) }),
					call({ adCod: 'E3101Q109', adDes: 'PROBABILITÀ E STATISTICA', dataFineIscr: at(-1) })
				]
			},
			TODAY
		);
		expect(deadlines).toHaveLength(1);
		expect(deadlines[0]).toMatchObject({
			source: 'booking',
			title: 'BASI DI DATI',
			code: 'E3101Q107',
			days: 8,
			callDate: new Date(2026, 5, 30),
			opensAt: null
		});
	});

	it('leaves out the calls of an exam already booked', () => {
		const deadlines = buildDeadlines(
			{
				bookings: [booking({ adId: 20, dataOraTurno: at(12, '09:00:00') })],
				calls: [call({ appId: 2, dataFineIscr: at(20) }), call({ adId: 30, adDes: 'ALTRO' })]
			},
			TODAY
		);
		expect(ids(deadlines)).toEqual(['booking:ALTRO', 'exam:10/20/1/700']);
	});

	it('says when booking opens if it is not open yet', () => {
		const [item] = buildDeadlines({ calls: [call({ dataInizioIscr: at(2) })] }, TODAY);
		expect(item).toMatchObject({ source: 'booking', opensAt: new Date(2026, 5, 20) });
	});

	it('leaves out calls outside the study plan, failing open', () => {
		const calls = [
			call({ adCod: 'E3101Q107', adDes: 'BASI DI DATI' }),
			call({ adCod: 'E3101Q999', adDes: 'CORSO FUORI PIANO' }),
			call({ adCod: null, adDes: 'SENZA CODICE' })
		];
		const titles = (deadlines: Deadline[]) => deadlines.map((item) => item.title);
		expect(titles(buildDeadlines({ calls, plan: plan([' e3101q107 ']) }, TODAY))).toEqual([
			'BASI DI DATI',
			'SENZA CODICE'
		]);
		expect(buildDeadlines({ calls, plan: null }, TODAY)).toHaveLength(3);
		expect(buildDeadlines({ calls, plan: plan([]) }, TODAY)).toHaveLength(3);
		expect(buildDeadlines({ calls }, TODAY)).toHaveLength(3);
	});

	it('keeps a month ahead, sorted by day, same-day entries in source order', () => {
		const deadlines = buildDeadlines(
			{
				calls: [call({ dataFineIscr: at(5) })],
				bookings: [booking({ adId: 21, dataOraTurno: at(5, '09:00:00') })],
				invoices: [invoice({ scadFattura: at(5) }), invoice({ fattId: 1, scadFattura: at(31) })],
				results: [
					result({ dataRifEsitoStu: at(5) }),
					result({ applistaId: 2, dataRifEsitoStu: at(30) })
				]
			},
			TODAY
		);
		expect(ids(deadlines)).toEqual([
			'result:900',
			'fee:6003',
			'exam:10/21/1/700',
			'booking:BASI DI DATI',
			'result:2'
		]);
	});
});

describe('currentPlanId', () => {
	const header = (pianoId: number, stato: string) =>
		wire(decodeEsse3StudyPlanHeader, { pianoId, stato });

	it('prefers the approved plan, else the latest', () => {
		expect(currentPlanId([header(3, 'P'), header(2, 'A'), header(1, 'A')])).toBe(2);
		expect(currentPlanId([header(3, 'B'), header(5, 'P')])).toBe(5);
		expect(currentPlanId([])).toBeNull();
	});
});

describe('planCodes', () => {
	it('reads the chosen activities only', () => {
		const studyPlan = wire(decodeEsse3StudyPlan, {
			attivita: [
				{ sceltaFlg: 1, adLibCod: 'E3101Q106' },
				{ sceltaFlg: 0, adLibCod: 'E3101Q999' },
				{ chiaveADContestualizzata: { adCod: 'e3101q107' } }
			]
		});
		expect([...(planCodes(studyPlan) ?? [])]).toEqual(['E3101Q106', 'E3101Q107']);
	});
});

describe('summarize and serviceStatus', () => {
	const sources = {
		results: [result({}), result({ applistaId: 2, presaVisione: 'A' })],
		invoices: [
			invoice({ fattId: 1, importoFattura: 156 }),
			invoice({ fattId: 2, importoFattura: 50, fattScadutaFlg: 1, scadFattura: at(-2) }),
			invoice({ fattId: 3, pagatoFlg: 1 })
		],
		bookings: [
			booking({ adId: 40 }),
			booking({ applistaId: 9, dataOraTurno: at(-20, '09:00:00') })
		],
		calls: [
			call({ dataFineIscr: at(3) }),
			call({ adDes: 'ALTRO', adCod: 'X', dataFineIscr: at(15) })
		]
	};
	const deadlines = buildDeadlines(sources, TODAY);

	it('counts the urgent ones and points at the earliest', () => {
		const summary = summarize(deadlines);
		expect(summary).toMatchObject({ count: 6, urgent: 3 });
		expect(summary.next?.id).toBe('fee:2');
	});

	it('counts what each service waits for, and when the first of it is due', () => {
		expect(serviceStatus(sources, deadlines, TODAY)).toEqual({
			pendingResults: 1,
			resultsDue: new Date(2026, 5, 23),
			owedFees: 2,
			overdueFees: 1,
			owedAmount: 206,
			feesDue: new Date(2026, 5, 30),
			bookedExams: 1,
			nextExam: new Date(2026, 5, 26),
			closingBookings: 1
		});
	});

	it('takes an unpaid invoice past its date for overdue, flagged or not', () => {
		const status = serviceStatus(
			{ invoices: [invoice({ scadFattura: at(-1) }), invoice({ fattId: 2, scadFattura: at(40) })] },
			[],
			TODAY
		);
		expect(status).toMatchObject({
			owedFees: 2,
			overdueFees: 1,
			feesDue: new Date(2026, 6, 28)
		});
	});

	it('counts the results the timeline lists, no more', () => {
		const results = [result({}), result({ applistaId: 901, appId: null })];
		const listed = buildDeadlines({ results }, TODAY);
		expect(listed).toHaveLength(1);
		expect(serviceStatus({ results }, listed, TODAY).pendingResults).toBe(1);
	});

	it('knows nothing is due when nothing is owed, booked or to decide', () => {
		expect(serviceStatus({}, [], TODAY)).toMatchObject({
			pendingResults: 0,
			resultsDue: null,
			owedFees: 0,
			feesDue: null,
			bookedExams: 0,
			nextExam: null
		});
	});
});
