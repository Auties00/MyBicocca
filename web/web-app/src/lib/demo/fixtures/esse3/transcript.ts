import { DemoStudent } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

export const routes = [
	route(
		`GET ${API}/libretto-service-v2/libretti`,
		() => {
			const { career } = DemoStudent;
			return json([
				{
					stuId: career.id,
					matId: career.enrollmentTraitId,
					cdsCod: career.easyStaffProgramCode,
					cdsId: career.programId,
					aaOrdId: career.academicYear,
					pdsCod: 'GEN',
					pdsId: 9999,
					staStuCod: career.statusCode,
					staMatCod: 'A',
					annoCorso: 2,
					aaIscrId: career.academicYearEnrollmentId,
					durataAnni: 3,
					ultimoAnnoFlg: 0,
					tipoCorsoCod: career.courseTypeCode,
					aaRegId: career.academicYear,
					ptFlg: 0
				}
			]);
		},
		{ empty: () => esse3Error(404, 'Nessun libretto trovato') }
	)
];
