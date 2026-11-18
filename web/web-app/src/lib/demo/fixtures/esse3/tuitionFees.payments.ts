import { academicYear } from '#lib/data/academicYear.ts';
import { days, esse3Date, fromAndroid } from '../../dates.ts';
import { demoPdf } from '../../pdf.ts';
import { DemoStudent } from '../../persona.ts';
import { bytes, esse3Error, json, route, type DemoRequest } from '../../router.ts';
import { demoPaid, routes as feeRoutes } from './tuitionFees.services.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api/tasse-service-v1';

const nothing = () => esse3Error(404, 'Nessun dato trovato');

/** `isee` for a year with no ISEE presented: Esse3 files it in the top band. */
const NOT_PRESENTED = 99_999_999;

/**
 * One row per enrolment year, from the career's first to this one, as in the Android demo: the
 * earlier years' ISEE is on file, this year's not yet, so the page offers to declare it while the
 * DSU window is open.
 */
function enrolments() {
	const first = DemoStudent.career.academicYear;
	const current = academicYear();
	const row = (year: number, courseYear: number, isee: number, exemption: string | null) => ({
		persId: DemoStudent.personId,
		stuId: DemoStudent.career.id,
		cdsId: DemoStudent.career.programId,
		tipoCorsoCod: DemoStudent.career.courseTypeCode,
		cdsDes: DemoStudent.career.description,
		aaOrdId: first,
		pdsId: 9999,
		aaIscrId: year,
		annoCorso: courseYear,
		anniFc: 0,
		tipoIscrCod: 'IC',
		tipoIscrCodDes: 'In corso',
		fasciaId: isee === NOT_PRESENTED ? 99 : 4,
		partTime: 'N',
		isee,
		durataAnni: 3,
		tipoEsoCod: exemption === null ? null : 'EPR',
		des: exemption,
		pesi: null,
		anniCarr: courseYear,
		merito: null,
		frequenza: null,
		sogliaCfu: null,
		sogliaIsee: 30_000
	});
	return Array.from({ length: current - first + 1 }, (_, index) => {
		const year = first + index;
		return year === current
			? row(year, index + 1, NOT_PRESENTED, null)
			: row(year, index + 1, 24_380 + index * 1_530, 'Esonero parziale per reddito');
	});
}

/** The Android demo's refund, plus one still in progress so both states show. */
function refunds() {
	const previous = academicYear() - 1;
	const refund = (fields: Record<string, unknown>) => ({
		nome: DemoStudent.firstName.toUpperCase(),
		cognome: DemoStudent.lastName.toUpperCase(),
		codFis: DemoStudent.fiscalCode,
		codElabRimb: null,
		fattErrataId: null,
		fattAnnullata: 0,
		pagId: null,
		dataNotifica: null,
		regManFlg: 0,
		desMav2: null,
		...fields
	});
	return [
		refund({
			aaId: academicYear(),
			fattId: 6101,
			importoFattura: 16,
			importoPag: null,
			scadFattura: null,
			numMandatoRimb: null,
			cauRimbCod: 'DUPL',
			desMav1: 'Rimborso imposta di bollo versata due volte',
			dataEmissione: esse3Date(days(-12)),
			dataElab: null,
			dataPagamento: null,
			dataAccredito: null,
			incassatoDa: null,
			notaRimb: 'In attesa del mandato di pagamento',
			rimborsatoFlg: 0
		}),
		refund({
			aaId: previous,
			fattId: 5003,
			importoFattura: 140,
			importoPag: 140,
			scadFattura: null,
			numMandatoRimb: '2025/004417',
			cauRimbCod: 'ISEE',
			desMav1: 'Rimborso per ricalcolo fascia ISEE',
			dataEmissione: esse3Date(fromAndroid(2025, 10, 6)),
			dataElab: esse3Date(fromAndroid(2025, 10, 20)),
			dataPagamento: esse3Date(fromAndroid(2025, 11, 3)),
			dataAccredito: esse3Date(fromAndroid(2025, 11, 5)),
			incassatoDa: 'Accredito su conto corrente',
			notaRimb: null,
			rimborsatoFlg: 1
		})
	];
}

interface DemoInvoice {
	fattId: number;
	aaId: number;
	importoFattura: number;
	pagatoFlg: number;
	dataPagamento: string | null;
	scadFattura: string;
	desMav1: string;
	iuv: string;
	codiceAvviso: string;
}

/** The invoices as the services fixture answers them, so every page tells one story. */
async function invoices(request: DemoRequest): Promise<DemoInvoice[]> {
	const list = feeRoutes.find((candidate) => candidate.segments.at(-1) === 'lista-fatture');
	if (list === undefined) return [];
	return (await list.handle(request)).json();
}

/** pagoPA's documents: its blue, and the university as the creditor. */
const PAGOPA = {
	accent: '0 0.43 0.79',
	issuer: 'Universita degli Studi di Milano-Bicocca - pagoPA'
};

function slip(invoice: DemoInvoice): string[] {
	const year = `${invoice.aaId}/${String((invoice.aaId + 1) % 100).padStart(2, '0')}`;
	return [
		`Fattura n. ${invoice.fattId} - a.a. ${year}`,
		invoice.desMav1,
		`Importo: ${invoice.importoFattura.toFixed(2).replace('.', ',')} EUR`,
		`Scadenza: ${invoice.scadFattura}`,
		`Codice avviso: ${invoice.codiceAvviso}`,
		`IUV: ${invoice.iuv}`,
		`Intestatario: ${DemoStudent.fullName} (${DemoStudent.fiscalCode})`
	];
}

/**
 * The demo's stand-in for the pagoPA checkout: a page of its own (no real payment can start), in
 * the new tab the app opens.
 */
function demoCheckout(invoice: DemoInvoice): string {
	const amount = invoice.importoFattura.toFixed(2).replace('.', ',');
	const page = `<!doctype html><html lang="it"><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>pagoPA · demo</title>
<style>body{margin:0;min-height:100vh;display:grid;place-items:center;background:#f3f6fa;
font:16px/1.5 system-ui,sans-serif;color:#17324d}main{max-width:26rem;margin:24px;padding:32px;
border-radius:16px;background:#fff;box-shadow:0 8px 30px #0001}h1{margin:0 0 8px;color:#0066cc;
font-size:1.5rem}p{margin:8px 0}strong{font-size:1.75rem}</style>
<main><h1>pagoPA</h1><p>Checkout dimostrativo della fattura n. ${invoice.fattId}.</p>
<p><strong>${amount} €</strong></p><p>Nella demo il pagamento risulta eseguito senza che parta
davvero: chiudi questa scheda e torna a MyBicocca.</p></main></html>`;
	return URL.createObjectURL(new Blob([page], { type: 'text/html' }));
}

async function invoiceOf(request: DemoRequest, id: number): Promise<DemoInvoice | undefined> {
	return (await invoices(request)).find((invoice) => invoice.fattId === id);
}

export const routes = [
	route(
		`GET ${API}/parametri-iscrizioni-per-tasse/:stuId`,
		(request) =>
			Number(request.params.stuId) === DemoStudent.career.id ? json(enrolments()) : nothing(),
		{ empty: nothing }
	),
	route(
		`GET ${API}/lista-rimborsi/:persId`,
		(request) =>
			Number(request.params.persId) === DemoStudent.personId ? json(refunds()) : nothing(),
		{ empty: nothing }
	),
	route(`POST ${API}/pagopa/transaction`, async (request) => {
		const { fattId } = await request.json<{ fattId: number }>();
		const invoice = await invoiceOf(request, fattId);
		if (invoice === undefined) return esse3Error(404, 'Fattura non trovata');
		if (invoice.pagatoFlg === 1) return esse3Error(422, 'La fattura risulta già pagata');
		demoPaid().set(fattId, new Date());
		return json({ redirectUrlPagoPA: demoCheckout(invoice) });
	}),
	route(`PUT ${API}/pagopa/avviso/:fattId`, async (request) => {
		const invoice = await invoiceOf(request, Number(request.params.fattId));
		if (invoice === undefined) return esse3Error(404, 'Fattura non trovata');
		return bytes(demoPdf('Avviso di pagamento pagoPA', slip(invoice), PAGOPA), 'application/pdf');
	}),
	route(`GET ${API}/pagopa/quietanza/:fattId/lingua/:lingua`, async (request) => {
		const invoice = await invoiceOf(request, Number(request.params.fattId));
		if (invoice === undefined || invoice.pagatoFlg !== 1)
			return esse3Error(404, 'Quietanza non disponibile');
		const title = request.params.lingua === 'en' ? 'Payment receipt' : 'Quietanza di pagamento';
		return bytes(
			demoPdf(title, [...slip(invoice), `Pagata il ${invoice.dataPagamento}`], PAGOPA),
			'application/pdf'
		);
	}),
	route(
		`GET ${API}/pagopa/transazioni`,
		async (request) => {
			const id = Number(request.url.searchParams.get('fattId'));
			const invoice = await invoiceOf(request, id);
			if (invoice === undefined) return nothing();
			const transaction = {
				fattId: invoice.fattId,
				iuv: invoice.iuv,
				codiceAvviso: invoice.codiceAvviso,
				importo: invoice.importoFattura,
				aaId: invoice.aaId,
				codFis: DemoStudent.fiscalCode
			};
			if (invoice.pagatoFlg === 1) {
				return json([
					{
						...transaction,
						dataTransazione: invoice.dataPagamento,
						esitoCod: 'OK',
						esitoDes: 'Pagamento eseguito',
						statoFinale: 1,
						esitoTransazione: 'PAGAMENTO_ESEGUITO',
						importoPagato: invoice.importoFattura,
						dataPagamento: invoice.dataPagamento,
						pagatoFlg: 1,
						quietanzaStampabile: 1
					}
				]);
			}
			return nothing();
		},
		{ empty: nothing }
	)
];
