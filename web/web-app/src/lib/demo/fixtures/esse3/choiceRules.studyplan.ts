import { academicYear } from '#lib/data/academicYear.ts';
import { days, esse3Date } from '../../dates.ts';
import { DemoCareer, DemoStudent } from '../../persona.ts';
import { esse3Error, json, route, type DemoRequest } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api/regsce-service-v1/regsce';

/** The ids the plan header of `plans.services.ts` points at. */
const REGULATION_ID = 820001;
const COMMON = 830001;
const COMMON_MANUAL = 830002;
const DATA_SCIENCE = 830003;
const PART_TIME = 830004;
const RETIRED = 830005;

const nothing = () => esse3Error(404, 'Nessun dato trovato');

function regulation() {
	const { career } = DemoStudent;
	return {
		regsceId: REGULATION_ID,
		cdsId: career.programId,
		cdsCod: career.easyStaffProgramCode,
		cdsDes: career.description,
		aaOrdId: career.academicYear,
		coorte: career.academicYear,
		aaRevisioneId: 0,
		stato: 'A'
	};
}

/**
 * The plan schemas: the common path twice, to approve and pre-approved (the plan's), listed in
 * that order as Esse3 lists them; a Data Science path gated by a condition; the common path
 * part-time; a retired one.
 */
function schemas() {
	const base = { regsceId: REGULATION_ID, webViewFlg: 1, statutarioFlg: 0 };
	return [
		{
			...base,
			schemaId: COMMON_MANUAL,
			schemaCod: 'GGG',
			schemaDes: 'DA APPROVARE',
			pdsCod: 'GGG',
			pdsDes: 'PERCORSO COMUNE',
			tipoApprovazione: 1
		},
		{
			...base,
			schemaId: COMMON,
			schemaCod: 'GGG-A',
			schemaDes: 'PREAPPROVATO',
			schemaDesEng: 'PRE-APPROVED',
			pdsCod: 'GGG',
			pdsDes: 'PERCORSO COMUNE',
			tipoApprovazione: 0,
			statutarioFlg: 1
		},
		{
			...base,
			schemaId: DATA_SCIENCE,
			schemaCod: 'E3101Q-DSC',
			schemaDes: 'Percorso Data Science',
			schemaDesEng: 'Data Science path',
			pdsCod: 'DSC',
			pdsDes: 'DATA SCIENCE',
			tipoApprovazione: 1,
			condCod: 'POSTI_DSC',
			condDes: 'Posti limitati: al massimo 40 studenti per anno accademico.'
		},
		{
			...base,
			schemaId: PART_TIME,
			schemaCod: 'E3101Q-GGG-PT',
			schemaDes: 'Percorso comune a tempo parziale',
			schemaDesEng: 'Common path, part-time',
			pdsCod: 'GGG',
			pdsDes: 'PERCORSO COMUNE',
			aptCod: 'PT',
			aptDes: 'Tempo parziale',
			tipoApprovazione: 2
		},
		{
			...base,
			schemaId: RETIRED,
			schemaCod: 'E3101Q-OLD',
			schemaDes: 'Ordinamento precedente',
			pdsCod: 'OLD',
			pdsDes: 'PERCORSO PREVIGENTE',
			tipoApprovazione: 0,
			webViewFlg: 0,
			dataFineVal: esse3Date(days(-400))
		}
	];
}

/** An activity a rule offers: the persona's when it has it, else one of the electives below. */
type Offer = [code: string, name: string, credits: number];

const ELECTIVES: Offer[] = [
	['E3101Q120', 'LABORATORIO DI SISTEMI MOBILI', 3],
	['E3101Q121', 'LABORATORIO DI DATA SCIENCE', 3],
	['E3101Q122', 'COMPUTER GRAFICA', 6],
	['E3101Q123', 'CLOUD COMPUTING', 6],
	['E3101Q124', 'BIOINFORMATICA', 8],
	['E3101Q130', 'ECONOMIA E ORGANIZZAZIONE AZIENDALE', 6],
	['E3101Q131', 'FOTOGRAFIA COMPUTAZIONALE', 6],
	['E3101Q132', 'TEORIA DEI GIOCHI', 6],
	['E3101Q140', 'MACHINE LEARNING', 8],
	['E3101Q141', 'DATA MINING', 6],
	['E3101Q142', 'VISUALIZZAZIONE DEI DATI', 6]
];

function offer(code: string): Offer {
	const activity = DemoCareer.activities().find((item) => item.code === code);
	if (activity) return [activity.code, activity.name, activity.credits];
	const elective = ELECTIVES.find(([candidate]) => candidate === code);
	if (!elective) throw new Error(`No demo activity ${code}`);
	return elective;
}

interface RuleSpec {
	id: number;
	order: number;
	year: number;
	type: 'O' | 'W';
	unit?: 'CFU' | 'BLK';
	min?: number;
	max?: number;
	optional?: boolean;
	description: [it: string, en: string];
	preNote?: [it: string, en: string];
	postNote?: [it: string, en: string];
	codes: string[];
}

function rule(spec: RuleSpec, studyPlanCode: string) {
	const { career } = DemoStudent;
	return {
		sceltaId: spec.id,
		regsceId: REGULATION_ID,
		ordNum: spec.order,
		annoCorso: spec.year,
		des: spec.description[0],
		desEng: spec.description[1],
		tipSce: spec.type,
		tipSceDes: spec.type === 'O' ? 'Obbligatoria' : 'Scelta via web',
		tipUnt: spec.unit ?? 'CFU',
		minUnt: spec.min ?? null,
		maxUnt: spec.max ?? null,
		opzFlg: spec.optional ? 1 : 0,
		notaPre: spec.preNote?.[0] ?? null,
		notaPreEng: spec.preNote?.[1] ?? null,
		notaPost: spec.postNote?.[0] ?? null,
		notaPostEng: spec.postNote?.[1] ?? null,
		blocchi: [
			{
				sceltaId: spec.id,
				blkId: spec.id,
				regsceId: REGULATION_ID,
				attivita: spec.codes.map((code, index) => {
					const [adCod, adDes, peso] = offer(code);
					return {
						sceltaAdId: spec.id * 100 + index + 1,
						sceltaId: spec.id,
						blkId: spec.id,
						peso,
						chiaveADContestualizzata: {
							cdsId: DemoCareer.courseOfStudyId,
							cdsCod: career.easyStaffProgramCode,
							aaOrdId: career.academicYear,
							pdsCod: studyPlanCode,
							aaOffId: academicYear(),
							adId: Number(adCod.slice(-3)) + 30000,
							adCod,
							adDes
						}
					};
				})
			}
		]
	};
}

const yearOf = (year: number) =>
	DemoCareer.activities()
		.filter((activity) => activity.year === year)
		.map((activity) => activity.code);

const LAB = 'E3101Q119';

/** The rules every path shares: the first two years and the free-choice activities. */
function sharedRules(): RuleSpec[] {
	return [
		{
			id: 850001,
			order: 1,
			year: 1,
			type: 'O',
			description: ['Insegnamenti obbligatori del primo anno', 'First-year mandatory courses'],
			codes: yearOf(1)
		},
		{
			id: 850002,
			order: 2,
			year: 2,
			type: 'O',
			description: ['Insegnamenti obbligatori del secondo anno', 'Second-year mandatory courses'],
			codes: yearOf(2).filter((code) => code !== LAB)
		},
		{
			id: 850003,
			order: 3,
			year: 2,
			type: 'W',
			min: 3,
			max: 3,
			description: ['Laboratorio a scelta', 'Elective laboratory'],
			preNote: [
				'Scegli uno dei laboratori da 3 CFU attivati in questo anno accademico.',
				'Choose one of the 3-CFU laboratories running this academic year.'
			],
			codes: [LAB, 'E3101Q120', 'E3101Q121']
		},
		{
			id: 850006,
			order: 6,
			year: 0,
			type: 'W',
			unit: 'BLK',
			max: 2,
			optional: true,
			description: ['Attività a libera scelta', 'Free-choice activities'],
			preNote: [
				'Puoi aggiungere fino a due attività, anche di altri corsi di studio, oltre ai crediti richiesti.',
				'You can add up to two activities, from other degree programmes too, on top of the required credits.'
			],
			codes: ['E3101Q130', 'E3101Q124', 'E3101Q131', 'E3101Q132']
		}
	];
}

function commonRules(): RuleSpec[] {
	return [
		{
			id: 850004,
			order: 4,
			year: 3,
			type: 'O',
			description: ['Insegnamenti obbligatori del terzo anno', 'Third-year mandatory courses'],
			codes: ['E3101Q115', 'E3101Q116', 'E3101Q190', 'E3101Q199']
		},
		{
			id: 850005,
			order: 5,
			year: 3,
			type: 'W',
			min: 12,
			max: 16,
			description: ['Insegnamenti a scelta del terzo anno', 'Third-year electives'],
			postNote: [
				'Gli insegnamenti scelti qui non possono essere scelti anche tra le attività a libera scelta.',
				"Courses chosen here can't also be chosen among the free-choice activities."
			],
			codes: ['E3101Q117', 'E3101Q118', 'E3101Q122', 'E3101Q123', 'E3101Q124']
		}
	];
}

function dataScienceRules(): RuleSpec[] {
	return [
		{
			id: 850014,
			order: 4,
			year: 3,
			type: 'O',
			description: ['Insegnamenti obbligatori del terzo anno', 'Third-year mandatory courses'],
			codes: ['E3101Q115', 'E3101Q140', 'E3101Q190', 'E3101Q199']
		},
		{
			id: 850015,
			order: 5,
			year: 3,
			type: 'W',
			min: 12,
			max: 14,
			description: ['Insegnamenti caratterizzanti Data Science', 'Data Science core courses'],
			codes: ['E3101Q141', 'E3101Q142', 'E3101Q118', 'E3101Q124', 'E3101Q116']
		}
	];
}

function schemaDetails(schemaId: number) {
	const schema = schemas().find((item) => item.schemaId === schemaId);
	if (!schema) return undefined;
	const own = schemaId === DATA_SCIENCE ? dataScienceRules() : commonRules();
	const specs = [...sharedRules(), ...own].sort((a, b) => a.order - b.order);
	return { ...schema, regoleDiScelta: specs.map((spec) => rule(spec, schema.pdsCod)) };
}

/**
 * A submitted activity as the new plan lists it: the persona's own, else an elective placed in
 * the year of the rule (`ordNum`) that offered it.
 */
export function ruleOffer(code: string, order: number | null) {
	const own = DemoCareer.activities().find((activity) => activity.code === code);
	if (own) return { id: own.id, code, name: own.name, credits: own.credits, year: own.year };
	const elective = ELECTIVES.find(([candidate]) => candidate === code);
	if (!elective) return undefined;
	const spec = [...sharedRules(), ...commonRules(), ...dataScienceRules()].find(
		(item) => item.order === order
	);
	const [, name, credits] = elective;
	return { id: Number(code.slice(-3)) + 30000, code, name, credits, year: spec?.year ?? 0 };
}

/** Plan changes open nine days ago and close in nineteen, so the editor can be tried. */
function windows() {
	return [
		{
			finregsceId: 840001,
			regsceId: REGULATION_ID,
			tipoFinestra: 'O',
			dataInizio: esse3Date(days(-374)),
			dataFine: esse3Date(days(-344))
		},
		{
			finregsceId: 840002,
			regsceId: REGULATION_ID,
			tipoFinestra: 'O',
			dataInizio: esse3Date(days(-9)),
			dataFine: esse3Date(days(19))
		}
	];
}

const ours = (request: DemoRequest) => Number(request.params.regsceId) === REGULATION_ID;

export const routes = [
	route(`GET ${API}`, () => json([regulation()]), { empty: nothing }),
	route(`GET ${API}/:regsceId/schemi`, (request) => (ours(request) ? json(schemas()) : nothing()), {
		empty: nothing
	}),
	route(
		`GET ${API}/:regsceId/schemi/:schemaId`,
		(request) => {
			const details = ours(request) ? schemaDetails(Number(request.params.schemaId)) : undefined;
			return details ? json(details) : esse3Error(404, 'Schema non trovato');
		},
		{
			empty: (request) => {
				const details = schemaDetails(Number(request.params.schemaId));
				return details ? json({ ...details, regoleDiScelta: [] }) : nothing();
			}
		}
	),
	route(
		`GET ${API}/:regsceId/finestre`,
		(request) => (ours(request) ? json(windows()) : nothing()),
		{
			empty: nothing
		}
	)
];
