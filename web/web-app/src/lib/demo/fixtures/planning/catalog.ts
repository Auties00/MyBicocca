import { json, route, type DemoRequest } from '../../router.ts';

/**
 * The information desks' portal (Portale Planning, portal 2) as the demo sees it: the services,
 * groups, durations and horizons of the real portal (October 2026), so the directory folds them
 * the same way. The Android demo's "Segreteria studenti" stays, with the persona's booking at it,
 * and a video-call desk shows the online flow.
 */
export const PLANNING_API = 'https://gestioneorari.didattica.unimib.it/portaleplanningAPI/api';

export interface DemoArea {
	id: number;
	code: string;
	name: string;
	address: string;
}

export const DemoAreas = {
	careers: {
		id: 11,
		code: 'U17',
		name: 'Edificio U17 - Segreterie studenti',
		address: 'Piazzetta Difesa per le Donne - 20126 Milano'
	},
	civitas: {
		id: 82,
		code: 'U7',
		name: 'Edificio U7 - CIVITAS',
		address: 'Via Bicocca degli Arcimboldi, 8 - 20126 Milano'
	},
	medicine: {
		id: 83,
		code: 'U8',
		name: 'Edificio U8 - Monza',
		address: 'Via Cadore, 48 - 20900 Monza'
	},
	online: {
		id: 14,
		code: 'ONLINE',
		name: 'Sportello online',
		address: ''
	}
} satisfies Record<string, DemoArea>;

/** When a service can be booked, for the availability fixture. */
export type DemoAvailability =
	/** Weekday mornings, Tuesday and Thursday afternoons too, from tomorrow. */
	| 'weekdays'
	/** Every weekday, morning and afternoon, from tomorrow. */
	| 'daily'
	/** Only from the fifth week on: the picker has to look past the current month. */
	| 'later'
	/** Nothing at all within the booking horizon. */
	| 'none';

export interface DemoService {
	id: number;
	name: string;
	group: string | null;
	minutes: number;
	description: string;
	area: DemoArea;
	maxRangeDays: number;
	availability: DemoAvailability;
	virtual?: boolean;
}

const service = (
	id: number,
	name: string,
	group: string | null,
	minutes: number,
	area: DemoArea,
	options: Partial<
		Pick<DemoService, 'description' | 'maxRangeDays' | 'availability' | 'virtual'>
	> = {}
): DemoService => ({
	id,
	name,
	group,
	minutes,
	area,
	description: options.description ?? '',
	maxRangeDays: options.maxRangeDays ?? 30,
	availability: options.availability ?? 'weekdays',
	virtual: options.virtual
});

const SOCIOLOGY = 'Didattica - Area di Sociologia e Giurisprudenza';
/** Teaching desks book a week ahead at most, as on the portal. */
const WEEK = { maxRangeDays: 7 };

export const DemoServices: readonly DemoService[] = [
	service(1, 'Segreteria studenti', 'Segreterie studenti', 20, DemoAreas.careers, {
		description: '<p>Immatricolazioni, iscrizioni, certificati e carriera.</p>',
		availability: 'daily'
	}),
	service(
		50,
		'Didattica - Analisi dei Processi sociali / Turismo, territorio e sviluppo locale',
		SOCIOLOGY,
		10,
		DemoAreas.civitas,
		WEEK
	),
	service(37, 'Didattica - Area Giuridica', SOCIOLOGY, 15, DemoAreas.civitas, WEEK),
	service(
		40,
		'Didattica - Medicina e Chirurgia',
		'Didattica - Area Medica',
		15,
		DemoAreas.medicine,
		WEEK
	),
	service(51, 'Didattica - Progest', SOCIOLOGY, 10, DemoAreas.civitas, WEEK),
	service(
		53,
		'Didattica - Scienze del Turismo e Comunità locale/ Servizio Sociale',
		SOCIOLOGY,
		10,
		DemoAreas.civitas,
		WEEK
	),
	service(
		52,
		"Didattica - Sociologia / Scienze dell'Organizzazione / Sicurezza, devianza, gestione dei rischi",
		SOCIOLOGY,
		10,
		DemoAreas.civitas,
		WEEK
	),
	service(20, 'Diritto allo Studio', 'Diritto allo Studio', 10, DemoAreas.careers),
	service(
		62,
		'Economia / Statistica / Giurisprudenza (Gestione Carriere)',
		'Carriere Studenti - Area Economia, Statistica, Giurisprudenza',
		10,
		DemoAreas.careers
	),
	service(
		61,
		'Gestione carriere - Medicina e Chirurgia',
		'Carriere Studenti - Area Medica',
		10,
		DemoAreas.medicine
	),
	service(
		60,
		'Gestione carriere - Scienze MM.FF.NN.',
		'Carriere Studenti - Area Scienze',
		5,
		DemoAreas.careers
	),
	service(14, 'Ritiro Badge', 'Ritiro Badge', 5, DemoAreas.careers, {
		description: '<p>Ritiro della tessera universitaria. Porta un documento di identità valido.</p>'
	}),
	service(
		55,
		'Ritiro Diplomi originali di Laurea e Post Laurea',
		'Ritiro Diplomi originali di Laurea e Post Laurea',
		5,
		DemoAreas.careers,
		{ maxRangeDays: 60, availability: 'later' }
	),
	service(
		63,
		'Scienze della Formazione / Psicologia / Sociologia (Gestione Carriere)',
		'Carriere Studenti - Area Formazione, Psicologia e Sociologia',
		15,
		DemoAreas.careers
	),
	service(54, 'Scuole di Specializzazione', 'Scuole di Specializzazione', 10, DemoAreas.medicine, {
		availability: 'none'
	}),
	service(36, 'Sportello Stage', 'Sportello Stage', 15, DemoAreas.civitas),
	service(70, 'Bicocca International - Erasmus+', 'Internazionalizzazione', 30, DemoAreas.online, {
		description:
			'<p>Erasmus+, Exchange e doppie lauree. Il colloquio si svolge in videoconferenza.</p>',
		virtual: true
	})
];

export function demoService(id: number): DemoService | undefined {
	return DemoServices.find((item) => item.id === id);
}

function catalogEntry(item: DemoService) {
	return {
		id: item.id,
		type: item.name,
		durata: item.minutes * 60,
		description: item.description,
		risoluzione_griglia_pubblica: 0,
		durate_select: [item.minutes * 60],
		booking_public_method: 0,
		resource_grid: 0,
		raggruppamento: item.group ?? '',
		servizio_breve: 0,
		reservation_type: 'CONFERMATA',
		numero_risorse: 1,
		multi_prenotazione: false
	};
}

function areaEntry(area: DemoArea, offered: readonly DemoService[]) {
	return {
		id: area.id,
		cliente: 2,
		area_code: area.code,
		area_name: area.name,
		address: area.address,
		google_map_link: '',
		public_hidden: 0,
		daspo: null,
		referente: null,
		servizi: offered.map((item) => ({
			id: item.id,
			cliente: 2,
			type: item.name,
			durata: item.minutes * 60,
			description: item.description,
			add_prior: 1,
			edit_prior: 1,
			add_prior_limit_hour: '12:00:00',
			edit_prior_limit_hour: '12:00:00',
			max_range: item.maxRangeDays,
			booking_limit_number: 1,
			booking_limit_interval: 7,
			booking_limit_only_future: 1,
			virtuale: item.virtual === true ? 1 : 0,
			raggruppamento: item.group ?? '',
			daspo: '',
			libera_posto: 0,
			servizio_breve: 0,
			reservation_type: 'CONFERMATA',
			multi_prenotazione: 0
		}))
	};
}

/** The service the area listing is filtered by: the front-end sends it as a cookie. */
function filteredService(request: DemoRequest): number | undefined {
	const cookie = request.headers.get('cookie') ?? '';
	const raw = /(?:^|;\s*)entry-filters=([^;]*)/.exec(cookie)?.[1];
	if (!raw) return undefined;
	try {
		const filters = JSON.parse(decodeURIComponent(raw)) as { servizio?: { id?: unknown } };
		return typeof filters.servizio?.id === 'number' ? filters.servizio.id : undefined;
	} catch {
		return undefined;
	}
}

export const routes = [
	route(`GET ${PLANNING_API}/servizi/2`, () => json({ servizi: DemoServices.map(catalogEntry) }), {
		empty: () => json({ servizi: [] })
	}),
	route(
		`GET ${PLANNING_API}/aree/2`,
		(request) => {
			const id = filteredService(request);
			const areas = Object.values(DemoAreas as Record<string, DemoArea>);
			if (id === undefined) return json({ aree: areas.map((area) => areaEntry(area, [])) });
			// Filtered by a service, the portal lists its area with every service offered there.
			const offered = demoService(id);
			if (offered === undefined) return json({ aree: [] });
			const here = DemoServices.filter((item) => item.area.id === offered.area.id);
			return json({ aree: [areaEntry(offered.area, here)] });
		},
		{ empty: () => json({ aree: [] }) }
	)
];
