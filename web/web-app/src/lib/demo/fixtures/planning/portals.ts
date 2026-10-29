import { json, route } from '../../router.ts';
import { PLANNING_API, demoService, type DemoService } from './catalog.ts';

const PORTAL = { id: 2, Nome: 'Sportelli Informativi', Codice: 'unimib-segreteria' };

/**
 * A desk's form as the portal sends it (integers for flags and sections, as on the wire): the
 * portal-wide fields, the GDPR consent under its untranslated label, the desk's own question, and
 * a staff-only field blocked to the public.
 */
function form(item: DemoService) {
	const field = (id: number, rest: Record<string, unknown>) => ({
		id,
		valori: null,
		predefinito: null,
		primaria: 0,
		obbligatorio: 0,
		servizio: null,
		cliente: 2,
		software: 3,
		valore_auth: null,
		identificativo: 0,
		blocked: 0,
		public_editable: 1,
		posizione: 1,
		...rest
	});
	const own =
		item.id === 55
			? field(56, {
					label: 'Modalità di ritiro',
					codice: 'modalita_ritiro',
					tipo: 'select',
					valori: { persona: 'Di persona', delega: 'Con delega' },
					obbligatorio: 1,
					servizio: item.id,
					posizione: 2
				})
			: field(52, {
					label: "Motivazione dell'appuntamento e corso di interesse",
					codice: 'note_aggiuntive',
					tipo: 'textarea',
					placeholder: "Motivazione dell'appuntamento e corso di interesse",
					obbligatorio: 1,
					servizio: item.id,
					posizione: 2
				});
	return [
		field(6, {
			label: 'Email',
			codice: 'email',
			tipo: 'email',
			placeholder: 'Inserire la email',
			primaria: 1,
			obbligatorio: 1,
			valore_auth: 0
		}),
		field(8, {
			label: 'Cognome e Nome',
			codice: 'cognome_nome',
			tipo: 'text',
			predefinito: '',
			placeholder: 'Inserire cognome e nome',
			obbligatorio: 1,
			identificativo: 1
		}),
		field(10, {
			label: 'Matricola',
			codice: 'matricola',
			tipo: 'text',
			predefinito: '',
			placeholder: 'Inserire numero di matricola',
			posizione: 2
		}),
		field(11, {
			label: 'gdpr_label',
			codice: 'gdpr',
			tipo: 'checkbox',
			valori:
				'https://www.unimib.it/sites/default/files/Informativa_per_studenti_e_utenti_formazione_e_servizi_-_REV8_27-09-21.pdf',
			obbligatorio: 1,
			posizione: 100
		}),
		own,
		field(90, {
			label: 'Esito colloquio',
			codice: 'esito',
			tipo: 'text',
			blocked: 1,
			posizione: 'backoffice'
		})
	];
}

export const routes = [
	route(
		`GET ${PLANNING_API}/cliente/unimib-segreteria/formData/:service`,
		(request) => {
			const item = demoService(Number(request.params.service));
			return item ? json(form(item)) : json([]);
		},
		{ empty: () => json([]) }
	),
	route(`GET ${PLANNING_API}/cliente/show/unimib-segreteria`, () =>
		json({
			...PORTAL,
			Logo: null,
			config: {
				general: [{ captcha_control: false }, { prenotazioni_contemporanee: false }],
				griglia: [{ mostra_ora_fine: true }],
				login_pubblico: [{ stato: 'facoltativo' }, { url: '0' }]
			},
			colors: { scritte: '#ffffff' }
		})
	)
];
