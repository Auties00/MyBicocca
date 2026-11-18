import { esse3DateTime } from '../../dates.ts';
import { DemoStudent } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

/** The diploma the demo student enrolled with, the summer before the first year. */
function diploma() {
	const year = DemoStudent.career.academicYear;
	return {
		persId: String(DemoStudent.personId),
		id: 1,
		annoMaturita: year,
		dataMaturita: esse3DateTime(new Date(year, 6, 10)),
		anniScolarita: 5,
		scuolaDes: 'Liceo Scientifico Statale di Milano',
		scuolaComuDes: 'Milano',
		scuolaComuSigla: 'MI',
		scuolaCodMiur: 'MIPS01000X',
		naziConsegDes: 'Italia',
		linguaDidDes: 'Italiano',
		tipiTitoloSupDes: 'Liceo scientifico, opzione scienze applicate',
		staTitItCod: 'C',
		statiTitItDes: 'Conseguito',
		voto: 92,
		votoMax: 100,
		votoMin: 60,
		lodeFlg: 0,
		dichiarazValoreFlg: 0
	};
}

const titles = (withDiploma: boolean) => ({
	persId: String(DemoStudent.personId),
	SUP: withDiploma ? [diploma()] : [],
	TITIT: [],
	TITSTRA: []
});

export const routes = [
	route(
		`GET ${API}/anagrafica-service-v2/titoli/:personId`,
		({ params }) =>
			Number(params.personId) === DemoStudent.personId
				? json(titles(true))
				: esse3Error(404, 'Persona non trovata'),
		{ empty: () => json(titles(false)) }
	)
];
