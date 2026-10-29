import { demoPdf } from '../../pdf.ts';
import { DemoStudent } from '../../persona.ts';
import { bytes, esse3Error, json, route, type DemoRequest } from '../../router.ts';
import { demoPlan, submitDemoPlan, type DemoPlan, type PostedPlan } from './plans.state.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api/piani-service-v1/piani/:stuId';

function printout(plan: DemoPlan): Uint8Array<ArrayBuffer> {
	const { fullName, studentNumber, career } = DemoStudent;
	const activities = plan.activities.map(
		(activity) =>
			`${activity.year > 0 ? `${activity.year}° anno` : 'A scelta'} - ${activity.code} ${activity.name} - ${activity.credits} CFU`
	);
	return demoPdf('Piano di studio', [
		`Studente: ${fullName} - matricola ${studentNumber}`,
		`Corso di studio: ${career.description} - percorso comune`,
		`Stato del piano: ${plan.state === 'A' ? 'approvato' : 'proposto'}`,
		'',
		...activities
	]);
}

const student = (request: DemoRequest) => Number(request.params.stuId) === DemoStudent.career.id;

export const routes = [
	route(`GET ${API}/:pianoId/stampa`, (request) => {
		const plan = student(request) ? demoPlan(Number(request.params.pianoId)) : undefined;
		return plan ? bytes(printout(plan), 'application/pdf') : esse3Error(404, 'Piano non trovato');
	}),
	/*
	 * Esse3 checks the schema's condition on submit: the Data Science path is full, which the
	 * editor warns about before sending.
	 */
	route(`POST ${API}`, async (request) => {
		if (!student(request)) return esse3Error(403, 'Security failed');
		const body = await request.json<PostedPlan>();
		if (body.attivita.length === 0) return esse3Error(422, 'Il piano non contiene attività');
		if (body.pdsSceCod === 'DSC') {
			return esse3Error(
				422,
				'Condizione dello schema non soddisfatta: posti esauriti per il percorso Data Science.'
			);
		}
		const plan = submitDemoPlan(body);
		return json({ stuId: DemoStudent.career.id, pianoId: plan.id }, { status: 201 });
	})
];
