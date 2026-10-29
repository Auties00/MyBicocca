import { DemoCareer } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';
import { demoPlan, demoPlans, planHeader } from './plans.state.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api/piani-service-v1/piani/:stuId';

const nothing = () => esse3Error(404, 'Nessun piano trovato');

/** The career's plans: the Android demo's approved one, or the one the student last submitted. */
export const routes = [
	route(`GET ${API}`, () => json(demoPlans().map(planHeader)), { empty: nothing }),
	route(
		`GET ${API}/:pianoId`,
		(request) => {
			const plan = demoPlan(Number(request.params.pianoId));
			if (!plan) return nothing();
			return json({
				...planHeader(plan),
				attivita: plan.activities.map((activity) => ({
					adsceId: activity.id,
					sceltaFlg: 1,
					adLibCod: activity.code,
					adLibDes: activity.name,
					peso: activity.credits,
					annoCorso: activity.year,
					chiaveADContestualizzata: {
						cdsId: DemoCareer.courseOfStudyId,
						adId: activity.id,
						adCod: activity.code,
						adDes: activity.name
					}
				}))
			});
		},
		{ empty: nothing }
	)
];
