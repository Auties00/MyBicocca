import { DemoStudent } from '../../persona.ts';
import { empty, json, route } from '../../router.ts';
import { mintDemoJwt } from '../../tokens.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

/** The career segment keys Esse3 lists in the user's session. */
function careerSegment() {
	const { career } = DemoStudent;
	return {
		stuId: career.id,
		matId: career.enrollmentTraitId,
		matricola: DemoStudent.studentNumber,
		cdsId: career.programId,
		cdsDes: career.description,
		staStuCod: career.statusCode,
		staStuDes: 'Attivo',
		staMatCod: 'A',
		staMatDes: 'Attivo'
	};
}

export const routes = [
	route(`GET ${API}/login`, () =>
		json({
			user: {
				// Esse3 sends names in capitals.
				firstName: DemoStudent.firstName.toUpperCase(),
				lastName: DemoStudent.lastName.toUpperCase(),
				sex: 'M',
				codFis: DemoStudent.fiscalCode,
				persId: DemoStudent.personId,
				id: DemoStudent.esse3UserId,
				grpId: 6,
				grpUserTecnicoFlg: 0,
				grpPta: 0,
				grpDes: 'Studenti',
				sessionTimeout: 30,
				userId: DemoStudent.recordUserId,
				trattiCarriera: [careerSegment()]
			},
			authToken: 'DEMO0000000000000000000000000000',
			expPwd: false,
			jwt: null,
			profili: [{ grpId: 6, des: 'Studenti', userId: DemoStudent.recordUserId }]
		})
	),
	route(`GET ${API}/login/jwt/new`, () => json({ jwt: mintDemoJwt() })),
	route(`GET ${API}/logout`, () => empty(200))
];
