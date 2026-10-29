import { academicYear, academicYearLabel } from '#lib/data/academicYear.ts';
import { esse3Date, today } from '../../dates.ts';
import { demoPdf } from '../../pdf.ts';
import { DemoCareer, DemoStudent } from '../../persona.ts';
import { bytes, empty, html, route, withCookies, type DemoRequest } from '../../router.ts';
import { DEMO_PASSWORD, DEMO_USERNAME } from '../../tokens.ts';

const PORTAL = 'https://s3w.si.unimib.it';
const IDP = 'https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO';
const ACS = `${PORTAL}/Shibboleth.sso/SAML2/POST`;

interface DemoCertificate {
	configurationId: number;
	documentId: number;
	type: string;
	/** The portal's names, Italian then English: it links each declaration in both. */
	names: readonly [string, string];
	solarYear: number | null;
}

/** The four self-certifications of the Android demo; fees declarations for the last two years. */
function certificates(): DemoCertificate[] {
	const lastYear = today().getFullYear() - 1;
	return [
		{
			configurationId: 101,
			documentId: 1,
			type: 'I',
			names: ['Certificato di iscrizione', 'Enrolment certificate'],
			solarYear: null
		},
		{
			configurationId: 102,
			documentId: 2,
			type: 'I',
			names: ['Certificato di iscrizione con esami', 'Enrolment certificate with exams'],
			solarYear: null
		},
		...[lastYear, lastYear - 1].map((solarYear) => ({
			configurationId: 103,
			documentId: 3,
			type: 'TAX',
			names: ['Dichiarazione delle tasse pagate', 'Statement of fees paid'] as const,
			solarYear
		}))
	];
}

function link(certificate: DemoCertificate, name: string): string {
	const query = new URLSearchParams({
		cert_conf_id: String(certificate.configurationId),
		doc_id: String(certificate.documentId),
		tipo_cert_cod: certificate.type,
		annosolare: certificate.solarYear === null ? '' : String(certificate.solarYear),
		firma_dig_flg: '1'
	});
	return `<a href="MessaggiCertificato.do?${query.toString().replaceAll('&', '&amp;')}">${name}</a>`;
}

/** The portal's list page, its table reduced to what the scraper reads. */
function listPage(list: readonly DemoCertificate[]): string {
	const rows = list
		.map(
			(certificate) =>
				`<tr><td>${link(certificate, certificate.names[0])}</td><td>${link(certificate, certificate.names[1])}</td></tr>`
		)
		.join('');
	return `<!doctype html><html lang="it"><head><title>Certificati</title></head><body><h1>Autocertificazioni</h1><table>${rows}</table></body></html>`;
}

const ORDINALS = ['primo', 'secondo', 'terzo', 'quarto', 'quinto'];

function print(certificate: DemoCertificate): Uint8Array<ArrayBuffer> {
	const { fullName, studentNumber, fiscalCode, career } = DemoStudent;
	const current = academicYear();
	const year = academicYearLabel(current);
	const courseYear = current - career.academicYear + 1;
	const subject = [
		`Il sottoscritto ${fullName}, codice fiscale ${fiscalCode},`,
		`matricola ${studentNumber}, consapevole delle sanzioni penali previste`,
		"dall'art. 76 del D.P.R. 445/2000, dichiara:"
	];
	const body =
		certificate.type === 'TAX'
			? [
					`di aver versato nell'anno solare ${certificate.solarYear} le tasse universitarie`,
					`dovute per il corso di laurea in ${DemoCareer.degree}.`
				]
			: [
					`di essere iscritto per l'anno accademico ${year} al ${ORDINALS[courseYear - 1] ?? `${courseYear}°`} anno`,
					`del corso di laurea in ${DemoCareer.degree}.`,
					...(certificate.configurationId === 102
						? ['Gli esami sostenuti sono riportati nel libretto allegato.']
						: [])
				];
	return demoPdf(
		certificate.names[0],
		[...subject, '', ...body, '', `Milano, ${esse3Date(today())}`],
		{ footer: 'Documento firmato digitalmente - copia dimostrativa senza valore' }
	);
}

const page = (body: string) => html(`<!doctype html><html><body>${body}</body></html>`);

function findCertificate(request: DemoRequest): DemoCertificate | undefined {
	const params = request.url.searchParams;
	const year = params.get('annosolare');
	return certificates().find(
		(item) =>
			String(item.configurationId) === params.get('cert_conf_id') &&
			(item.solarYear === null ? !year : String(item.solarYear) === year)
	);
}

/** What the IdP sends back for the demo's credentials: the SAML form, or its login form again. */
async function credentials(request: DemoRequest): Promise<Response> {
	const form = await request.form();
	if (form.get('j_username') !== DEMO_USERNAME || form.get('j_password') !== DEMO_PASSWORD) {
		return page('<form method="post"><input name="j_username"><input name="j_password"></form>');
	}
	return page(
		`<form method="post" action="${ACS}"><input type="hidden" name="RelayState" value="demo"><input type="hidden" name="SAMLResponse" value="ZGVtbw=="></form>`
	);
}

export const routes = [
	route(
		`GET ${PORTAL}/auth/studente/Certificati/ListaCertificati.do`,
		() => html(listPage(certificates())),
		{ empty: () => html(listPage([])) }
	),
	route(`GET ${PORTAL}/auth/studente/Certificati/MessaggiCertificato.do`, (request) => {
		const certificate = findCertificate(request);
		return certificate === undefined
			? page('<p>Certificato non disponibile.</p>')
			: bytes(print(certificate), 'application/pdf');
	}),
	// The portal's single sign-on, as `esse3WebLogin` walks it.
	route(`GET ${PORTAL}/auth/studente/HomePageStudente.do`, () => page('')),
	route(`POST ${IDP}?execution=e1s1`, () => empty(302)),
	route(`GET ${IDP}?execution=e1s2`, () => page('')),
	route(`POST ${IDP}?execution=e1s2`, () => empty(302)),
	route(`POST ${IDP}?execution=e1s3`, credentials),
	// The portal's session cookie, kept by the jar: later calls skip the sign-in.
	route(`POST ${ACS}`, () => withCookies(page(''), 'JSESSIONID=demo-portal; Path=/; Secure'))
];
