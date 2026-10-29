import { academicYear } from '#lib/data/academicYear.ts';
import { days, esse3Date, esse3DateTime } from '../../dates.ts';
import { DemoCareer, DemoCourses, DemoStudent, type DemoActivity } from '../../persona.ts';
import { esse3Error, json, route, type DemoRequest } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';
const LIBRETTO = `${API}/libretto-service-v2/libretti/:matId`;

/** Activities whose prerequisites are not met yet: the networks course needs Operating Systems. */
const UNMET_PREREQUISITES = new Set([20102, 20106]);

const isStudent = ({ params }: DemoRequest) =>
	Number(params.matId) === DemoStudent.career.enrollmentTraitId;

const notFound = () => esse3Error(404, 'Libretto non trovato');

const passed = (activity: DemoActivity) => activity.passedOn !== null;

/**
 * The academic year an activity was attended in: the one its exam fell in once passed, else its
 * degree year's, counted from the year the career began.
 */
const yearOf = (activity: DemoActivity) =>
	activity.passedOn === null
		? DemoStudent.career.academicYear + Math.max(0, activity.year - 1)
		: academicYear(activity.passedOn);

function outcome(activity: DemoActivity) {
	if (activity.passedOn === null) return null;
	const graded = activity.grade !== null;
	return {
		modValCod: graded ? 'V' : 'G',
		supEsaFlg: 1,
		voto: activity.grade,
		lodeFlg: activity.cumLaude ? 1 : 0,
		tipoGiudCod: graded ? null : 'IDO',
		tipoGiudDes: graded ? null : 'Idoneo',
		dataEsa: esse3DateTime(activity.passedOn),
		aaSupId: yearOf(activity)
	};
}

function row(activity: DemoActivity, order: number) {
	const state = passed(activity) ? 'S' : activity.attending ? 'F' : 'P';
	return {
		matId: DemoStudent.career.enrollmentTraitId,
		ord: order,
		adsceId: activity.id,
		stuId: DemoStudent.career.id,
		pianoId: 880001,
		itmId: 890000 + order,
		adCod: activity.code,
		adDes: activity.name,
		annoCorso: activity.year,
		stato: state,
		statoDes: { S: 'Superata', F: 'Frequentata', P: 'Pianificata' }[state],
		tipoEsaCod: activity.grade === null && passed(activity) ? 'GI' : 'VO',
		tipoEsaDes: activity.grade === null && passed(activity) ? 'Giudizio' : 'Voto',
		ricId: 0,
		peso: activity.credits,
		aaFreqId: state === 'P' ? null : yearOf(activity),
		dataFreq: state === 'P' ? null : esse3Date(new Date(yearOf(activity) + 1, 0, 31)),
		freqUffFlg: 0,
		freqObbligFlg: 0,
		gruppoVotoId: 1,
		gruppoVotoMinVoto: 18,
		gruppoVotoMaxVoto: 30,
		gruppoVotoLodeFlg: 1,
		esito: outcome(activity),
		sovranFlg: 0,
		debitoFlg: 0,
		genAutoFlg: 0,
		genRicSpecFlg: 0,
		numAppelliPrenotabili: activity.attending ? 2 : 0,
		superataFlg: passed(activity) ? 1 : 0,
		numPrenotazioni: 0
	};
}

const rows = () => DemoCareer.activities().map((activity, index) => row(activity, index + 1));

function activity(request: DemoRequest): DemoActivity | undefined {
	if (!isStudent(request)) return undefined;
	const id = Number(request.params.adsceId);
	return DemoCareer.activities().find((candidate) => candidate.id === id);
}

/** Esse3's averages on base 30 and their projection on the graduation base of 110. */
function averages(activities: readonly DemoActivity[]) {
	const graded = activities.filter((item) => passed(item) && item.grade !== null);
	if (graded.length === 0) return [];
	const points = (item: DemoActivity) => item.grade ?? 0;
	const arithmetic = graded.reduce((sum, item) => sum + points(item), 0) / graded.length;
	const credits = graded.reduce((sum, item) => sum + item.credits, 0);
	const weighted = graded.reduce((sum, item) => sum + points(item) * item.credits, 0) / credits;
	const average = (type: 'A' | 'P', base: 30 | 110, value: number) => ({
		definizioneBase: base === 30 ? 'CDSORD' : 'CDS',
		tipoMediaCod: type,
		base,
		tipoOk: 1,
		media: Math.round(((value * base) / 30) * 100) / 100
	});
	return [
		average('A', 30, arithmetic),
		average('P', 30, weighted),
		average('A', 110, arithmetic),
		average('P', 110, weighted)
	];
}

function stats(activities: readonly DemoActivity[]) {
	const done = activities.filter(passed);
	const attended = activities.filter((item) => item.attending);
	const sum = (list: readonly DemoActivity[]) =>
		list.reduce((total, item) => total + item.credits, 0);
	return {
		matId: DemoStudent.career.enrollmentTraitId,
		umPesoCod: 'CFU',
		umPesoDes: 'Crediti Formativi Universitari',
		umPesoMin: DemoCareer.totalCredits,
		umPesoMax: DemoCareer.totalCredits,
		umPesoSuperato: sum(done),
		umPesoFrequentato: sum(attended),
		umPesoPianificato: sum(activities) - sum(done) - sum(attended),
		umPesoPiano: sum(activities),
		numAdLibretto: activities.length,
		numAdPiano: activities.length,
		numAdSuperate: done.length,
		numAdFrequentate: attended.length,
		numAdPianificate: activities.length - done.length - attended.length,
		gruppoVoto: {
			cod: 'TRENTESIMI',
			des: 'Voti in trentesimi',
			minPunti: 18,
			maxPunti: 30,
			lodeFlg: 1
		},
		medie: averages(activities)
	};
}

/**
 * The sittings recorded on a row: one per passed exam (Analisi took two, the first failed at the
 * written test) and a passed midterm of Basi di dati, which is being attended.
 */
function tests(item: DemoActivity) {
	const midtermOf = DemoCareer.activityId(DemoCourses.databases);
	if (item.passedOn === null && item.id !== midtermOf) return [];
	const test = (id: number, date: Date, body: Record<string, unknown>) => ({
		adregId: item.id * 10 + id,
		adsceId: item.id,
		matId: DemoStudent.career.enrollmentTraitId,
		tipoAppCod: 'PF',
		sesId: 1,
		sesDes: 'Appello ordinario',
		dataApp: esse3Date(date),
		...body
	});
	const result = (grade: number | null, ok: boolean, date: Date, laude = false) => ({
		modValCod: grade === null ? 'G' : 'V',
		supEsaFlg: ok ? 1 : 0,
		voto: grade,
		lodeFlg: laude ? 1 : 0,
		tipoGiudDes: grade === null && ok ? 'Idoneo' : null,
		dataEsa: esse3Date(date)
	});
	if (item.passedOn === null) {
		const midterm = days(-24);
		return [
			test(1, midterm, {
				tipoAppCod: 'PP',
				sesDes: 'Prova in itinere',
				staRegDes: 'Prova parziale superata',
				esitoParziale: result(27.5, true, midterm)
			})
		];
	}
	const list = [
		test(2, item.passedOn, {
			staRegCod: 'L',
			staRegDes: 'Verbalizzato',
			esitoFinale: result(item.grade, true, item.passedOn, item.cumLaude)
		})
	];
	if (item.code === 'E3101Q103') {
		const first = new Date(item.passedOn.getFullYear(), item.passedOn.getMonth() - 1, 18);
		list.push(
			test(1, first, {
				sesDes: 'Appello straordinario',
				staRegCod: 'P',
				staRegDes: 'Non superato',
				esitoScr: result(15, false, first)
			})
		);
	}
	return list;
}

export const routes = [
	route(`GET ${LIBRETTO}/righe/`, (request) => (isStudent(request) ? json(rows()) : notFound()), {
		empty: () => esse3Error(404, 'Nessuna riga di libretto trovata')
	}),
	route(`GET ${LIBRETTO}/righe/:adsceId`, (request) => {
		const item = activity(request);
		if (item === undefined) return esse3Error(404, 'Riga di libretto non trovata');
		const order = DemoCareer.activities().indexOf(item) + 1;
		return json(row(item, order));
	}),
	route(
		`GET ${LIBRETTO}/righe/:adsceId/prove`,
		(request) => {
			const item = activity(request);
			return item === undefined
				? esse3Error(404, 'Riga di libretto non trovata')
				: json(tests(item));
		},
		{ empty: () => esse3Error(404, 'Nessuna prova trovata') }
	),
	route(`GET ${LIBRETTO}/righe/:adsceId/prop`, (request) => {
		const item = activity(request);
		if (item === undefined) return esse3Error(404, 'Riga di libretto non trovata');
		if (passed(item)) return esse3Error(422, 'Attività didattica già superata');
		return json({ esito: UNMET_PREREQUISITES.has(item.id) ? 0 : 1 });
	}),
	route(
		`GET ${LIBRETTO}/stats`,
		(request) => (isStudent(request) ? json(stats(DemoCareer.activities())) : notFound()),
		{ empty: () => json(stats([])) }
	),
	route(
		`GET ${LIBRETTO}/medie`,
		(request) => (isStudent(request) ? json(averages(DemoCareer.activities())) : notFound()),
		{ empty: () => esse3Error(404, 'Nessuna media trovata') }
	)
];
