import { academicYear } from '#lib/data/academicYear.ts';
import { esse3DateTime } from '../../dates.ts';
import { DemoCareer, DemoStudent } from '../../persona.ts';
import { esse3Error, json, route, type DemoRequest } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

/**
 * One academic year of the demo career, as `/carriere/{stuId}/iscrizioni` sends it: a regular
 * student in course, with the partial income exemption of the Android demo.
 */
function year(academicYear: number, courseYear: number) {
	const { career } = DemoStudent;
	const enrolledOn = new Date(academicYear, 8, courseYear === 1 ? 18 : 2);
	return {
		persId: DemoStudent.personId,
		stuId: career.id,
		cdsId: career.programId,
		aaOrdId: career.academicYear,
		pdsId: 9999,
		iscrId: 740000 + courseYear,
		matId: career.enrollmentTraitId,
		matricola: DemoStudent.studentNumber,
		staMatCod: 'A',
		aaRegId: career.academicYear,
		aaIscrId: academicYear,
		dataIscr: esse3DateTime(enrolledOn),
		facCod: 'DISCO',
		facDes: 'Dipartimento di Informatica, Sistemistica e Comunicazione',
		cdsCod: career.easyStaffProgramCode,
		cdsDes: DemoCareer.degree,
		tipoCorsoCod: career.courseTypeCode,
		tipoCorsoDes: 'Corso di Laurea',
		pdsCod: 'GEN',
		indirizzoDes: 'PERCORSO COMUNE',
		ordinamentoCod: '2020',
		ordinamentoDes: 'Ordinamento 2020',
		durataCorso: 3,
		valoreMin: String(DemoCareer.totalCredits),
		annoCorso: courseYear,
		anniFc: 0,
		tipoIscrCod: 'IC',
		tipoIscrDes: 'In corso',
		staIscrCod: 'A',
		condFlg: 0,
		ricFlg: 0,
		attlauFlg: 0,
		ateneoCod: '70141',
		ateneoDes: 'Università degli Studi di Milano-Bicocca',
		sedeId: 1,
		sedeDes: 'Milano',
		linguaDid: 'Italiano',
		normCod: 'E3101Q-20',
		dataIns: esse3DateTime(enrolledOn),
		dataMod: esse3DateTime(new Date(academicYear, 9, 1)),
		claMurstCod: 'L-31',
		claMurstDes: 'Scienze e tecnologie informatiche',
		ptFlg: 0,
		fasciaId: 4,
		sospFlg: 0,
		fasciaMensaId: 2,
		tipoEsoCod: 'EPR',
		tipoEsoDes: 'Esonero parziale per reddito',
		tipoStuCod: 'TP',
		tipoStuDes: 'Studente a tempo pieno'
	};
}

const ours = (request: DemoRequest) => Number(request.params.stuId) === DemoStudent.career.id;

/* Esse3 answers a career without enrolments with a 404, which the SDK reads as an empty list. */
const none = () => esse3Error(404, 'Nessuna iscrizione trovata');

export const routes = [
	route(
		`GET ${API}/carriere-service-v1/carriere/:stuId/iscrizioni`,
		(request) => {
			if (!ours(request)) return none();
			const first = DemoStudent.career.academicYear;
			/* Every year from the career's first to the current one, the newest first. */
			const years = Array.from({ length: academicYear() - first + 1 }, (_, index) =>
				year(first + index, index + 1)
			);
			return json(years.reverse());
		},
		{ empty: none }
	)
];
