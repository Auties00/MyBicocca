import { DemoStudent } from '../../persona.ts';
import { bytes, esse3Error, json, route } from '../../router.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

/** Stands in for the Esse3 photo: the initials on the brand red, as the Android demo draws it. */
async function initialsAvatar(): Promise<Blob> {
	const size = 512;
	const canvas = new OffscreenCanvas(size, size);
	const context = canvas.getContext('2d');
	if (context === null) return new Blob();
	context.fillStyle = '#A71E3B';
	context.fillRect(0, 0, size, size);
	context.fillStyle = '#FFFFFF';
	context.font = `bold ${size * 0.42}px sans-serif`;
	context.textAlign = 'center';
	context.textBaseline = 'middle';
	context.fillText(`${DemoStudent.firstName[0]}${DemoStudent.lastName[0]}`, size / 2, size / 2);
	return canvas.convertToBlob({ type: 'image/png' });
}

function person() {
	return {
		persId: DemoStudent.personId,
		cognome: DemoStudent.lastName.toUpperCase(),
		nome: DemoStudent.firstName.toUpperCase(),
		dataNascita: '01/01/2003',
		comuNascDes: 'MILANO',
		comuNascSigla: 'MI',
		naziNascDes: 'ITALIA',
		codFis: DemoStudent.fiscalCode,
		sesso: 'M',
		userId: DemoStudent.recordUserId,
		naziResDes: 'ITALIA',
		comuResDes: 'MILANO',
		comuResSigla: 'MI',
		viaRes: 'Piazza della Scienza',
		numCivRes: '1',
		capRes: '20126',
		email: 'marco.rossi@example.com',
		emailAte: DemoStudent.email,
		cellulare: '+39 333 000 0000',
		codCittadinanza: 'IT',
		desCittadinanza: 'ITALIANA'
	};
}

const isStudent = (personId: string) => Number(personId) === DemoStudent.personId;

export const routes = [
	route(`GET ${API}/anagrafica-service-v2/persone/:personId/`, ({ params }) =>
		isStudent(params.personId) ? json(person()) : esse3Error(404, 'Persona non trovata')
	),
	route(
		`GET ${API}/anagrafica-service-v2/persone/:personId/foto`,
		async ({ params }) =>
			isStudent(params.personId)
				? bytes(await initialsAvatar(), 'image/png')
				: esse3Error(404, 'Foto non trovata'),
		{ empty: () => esse3Error(404, 'Foto non presente') }
	)
];
