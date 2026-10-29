import { academicYear } from '#lib/data/academicYear.ts';
import { days, esse3Date } from '../../dates.ts';
import { DemoStudent } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';
import { demoState } from '../../state.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api/tasse-service-v1';

interface DemoInvoice {
	id: number;
	/** The academic year billed, by its September. */
	year: number;
	installment: string;
	amount: number;
	issued: Date;
	expires: Date;
	paidOn: Date | null;
}

/** Invoices paid in the demo (pagoPA's checkout stands for the payment), with the day. */
export const demoPaid = () => demoState('esse3.tuitionFees.paid', () => new Map<number, Date>());

/** `month` (1–12) and `day` of the calendar year `year`. */
const on = (year: number, month: number, day: number) => new Date(year, month - 1, day);

/**
 * Two years of fees, as in the Android demo: all paid but this year's third instalment. Each year's
 * instalments fall inside it: the first at enrolment in the summer, the others through the year.
 */
function invoices(): DemoInvoice[] {
	const current = academicYear();
	const previous = current - 1;
	const paid = demoPaid();
	const list: DemoInvoice[] = [
		{
			id: 6003,
			year: current,
			installment: 'Terza rata',
			amount: 612,
			issued: days(-20),
			expires: days(27),
			paidOn: null
		},
		{
			id: 6002,
			year: current,
			installment: 'Seconda rata',
			amount: 612,
			issued: days(-48),
			expires: days(-12),
			paidOn: days(-15)
		},
		{
			id: 6001,
			year: current,
			installment: 'Prima rata',
			amount: 156,
			issued: on(current, 7, 15),
			expires: on(current, 9, 30),
			paidOn: on(current, 9, 2)
		},
		{
			id: 5003,
			year: previous,
			installment: 'Terza rata',
			amount: 598,
			issued: on(current, 4, 28),
			expires: on(current, 6, 15),
			paidOn: on(current, 6, 1)
		},
		{
			id: 5002,
			year: previous,
			installment: 'Seconda rata',
			amount: 598,
			issued: on(current, 1, 13),
			expires: on(current, 2, 17),
			paidOn: on(current, 2, 11)
		},
		{
			id: 5001,
			year: previous,
			installment: 'Prima rata',
			amount: 156,
			issued: on(previous, 7, 15),
			expires: on(previous, 9, 30),
			paidOn: on(previous, 9, 18)
		}
	];
	return list.map((invoice) => ({
		...invoice,
		paidOn: invoice.paidOn ?? paid.get(invoice.id) ?? null
	}));
}

/** The first instalment holds the regional tax and the stamp duty; the others, the contribution. */
function items(invoice: DemoInvoice): Array<{ name: string; amount: number }> {
	return invoice.amount < 200
		? [
				{ name: 'Tassa regionale per il diritto allo studio', amount: 140 },
				{ name: 'Imposta di bollo', amount: 16 }
			]
		: [{ name: 'Contributo onnicomprensivo', amount: invoice.amount }];
}

const nothing = () => esse3Error(404, 'Nessun dato trovato');

export const routes = [
	route(
		`GET ${API}/lista-fatture`,
		() =>
			json(
				invoices().map((invoice) => ({
					fattId: invoice.id,
					persId: DemoStudent.personId,
					aaId: invoice.year,
					importoFattura: invoice.amount,
					importoPag: invoice.paidOn === null ? 0 : invoice.amount,
					pagatoFlg: invoice.paidOn === null ? 0 : 1,
					fattScadutaFlg: 0,
					fattAnnullata: 0,
					dataEmissione: esse3Date(invoice.issued),
					scadFattura: esse3Date(invoice.expires),
					dataPagamento: invoice.paidOn === null ? null : esse3Date(invoice.paidOn),
					desMav1: `${invoice.installment} a.a. ${invoice.year}/${String((invoice.year + 1) % 100).padStart(2, '0')}`,
					pagopaEnabled: 1,
					pagopaImmediato: invoice.paidOn === null ? 1 : 0,
					pagopaAvviso: invoice.paidOn === null ? 1 : 0,
					iuv: `0000000${invoice.id}`,
					codiceAvviso: `30100000000000${invoice.id}`,
					codFis: DemoStudent.fiscalCode,
					cognome: DemoStudent.lastName.toUpperCase(),
					nome: DemoStudent.firstName.toUpperCase(),
					visWebFlg: 1
				}))
			),
		{ empty: nothing }
	),
	route(
		`GET ${API}/addebiti-studente`,
		(request) => {
			const start = Number(request.url.searchParams.get('start') ?? 0);
			const limit = Number(request.url.searchParams.get('limit') ?? 50);
			const rows = invoices().flatMap((invoice) =>
				items(invoice).map((item, index) => ({
					fattId: invoice.id,
					stuId: DemoStudent.career.id,
					aaId: invoice.year,
					voceId: invoice.id * 10 + index,
					voceDes: item.name,
					importoVoce: item.amount,
					rataDes: invoice.installment,
					scadenzaAddebito: esse3Date(invoice.expires),
					scadFattura: esse3Date(invoice.expires),
					importoFattura: invoice.amount,
					pagatoFlg: invoice.paidOn === null ? 0 : 1,
					fattScadutaFlg: 0,
					fattAnnullata: 0
				}))
			);
			const page = rows.slice(start, start + limit);
			return page.length === 0 ? nothing() : json(page);
		},
		{ empty: nothing }
	)
];
