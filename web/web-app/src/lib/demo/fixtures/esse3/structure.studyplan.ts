import { DemoStudent } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';

const API =
	'https://s3w.si.unimib.it/e3rest/api/struttura-service-v1/corsi/:cdsId/ordinamenti/:aaOrdId/percorsi';

const nothing = () => esse3Error(404, 'Nessun percorso trovato');

const italian = {
	linguaDidId: 1,
	linguaDidCod: 'ITA',
	linguaDidDes: 'Italiano',
	linguaDidDesEng: 'Italian'
};
const english = {
	linguaDidId: 2,
	linguaDidCod: 'ENG',
	linguaDidDes: 'Inglese',
	linguaDidDesEng: 'English'
};

/** The paths of the persona's degree, with their names and teaching languages. */
function paths(aaOrdId: number) {
	const { programId } = DemoStudent.career;
	const path = (
		pdsId: number,
		pdsCod: string,
		pdsDes: string,
		pdsDesEng: string,
		languages: object[]
	) => ({
		cdsId: programId,
		aaOrdId,
		pdsId,
		pdsCod,
		pdsDes,
		pdsDesEng,
		statoCod: 'A',
		webViewFlg: 1,
		lingueDidattica: languages.map((language) => ({
			cdsId: programId,
			aaOrdId,
			pdsId,
			...language
		}))
	});
	return [
		path(1, 'GGG', 'PERCORSO COMUNE', 'COMMON PATH', [italian]),
		path(2, 'DSC', 'DATA SCIENCE', 'DATA SCIENCE', [italian, english])
	];
}

export const routes = [
	route(
		`GET ${API}`,
		(request) =>
			Number(request.params.cdsId) === DemoStudent.career.programId
				? json(paths(Number(request.params.aaOrdId)))
				: nothing(),
		{ empty: nothing }
	)
];
