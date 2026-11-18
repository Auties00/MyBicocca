import { esse3Date } from '../../dates.ts';
import { DemoStudent } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

export const routes = [
	route(
		`GET ${API}/carriere-service-v1/carriere`,
		() => {
			const { career } = DemoStudent;
			return json([
				{
					persId: DemoStudent.personId,
					cognome: DemoStudent.lastName.toUpperCase(),
					nome: DemoStudent.firstName.toUpperCase(),
					sesso: 'M',
					userId: DemoStudent.recordUserId,
					codFis: DemoStudent.fiscalCode,
					emailAte: DemoStudent.email,
					staStuCod: career.statusCode,
					statiStuDes: 'Attivo',
					aaId: career.academicYear,
					aaDes: `${career.academicYear}/${career.academicYear + 1}`,
					dataImm: esse3Date(new Date(career.academicYear, 8, 15)),
					aaIscrId: career.academicYearEnrollmentId,
					matricola: DemoStudent.studentNumber,
					annoCorso: 2,
					sedeId: 1,
					sediDes: 'MILANO',
					staMatCod: 'A',
					tipoIscrCod: 'IC',
					ptFlg: 0,
					sospFlg: 0,
					p06CdsCod: career.easyStaffProgramCode,
					p06CdsDes: career.description,
					matId: career.enrollmentTraitId,
					aaOrdId: career.academicYear,
					pdsId: 9999,
					cdsId: career.programId,
					stuId: career.id,
					aaImm1: career.academicYear,
					aaRegId: career.academicYear
				}
			]);
		},
		{ empty: () => esse3Error(404, 'Nessuna carriera trovata') }
	)
];
