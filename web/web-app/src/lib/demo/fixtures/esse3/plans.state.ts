import { days, esse3Date, fromAndroid } from '../../dates.ts';
import { DemoCareer, DemoStudent } from '../../persona.ts';
import { demoState } from '../../state.ts';
import { ruleOffer } from './choiceRules.studyplan.ts';

/** The ids the regulation of `choiceRules.studyplan.ts` lists. */
const REGULATION_ID = 820001;
const PREAPPROVED_SCHEMA = 830001;
const MANUAL_SCHEMA = 830002;

export interface DemoPlanActivity {
	id: number;
	code: string;
	name: string;
	credits: number;
	/** 0 for the activities tied to no year (free choice). */
	year: number;
}

export interface DemoPlan {
	id: number;
	schemaId: number;
	/** Esse3's state: A approved, P proposed (waiting for the office). */
	state: 'A' | 'P';
	changed: Date;
	activities: DemoPlanActivity[];
}

/** The career's plans, newest last: at first the approved standard plan of the Android demo. */
export function demoPlans(): DemoPlan[] {
	return demoState('esse3.plans', () => [
		{
			id: 810001,
			schemaId: PREAPPROVED_SCHEMA,
			state: 'A',
			changed: fromAndroid(2025, 10, 14),
			activities: DemoCareer.activities().map(({ id, code, name, credits, year }) => ({
				id,
				code,
				name,
				credits,
				year
			}))
		}
	]);
}

export function demoPlan(id: number): DemoPlan | undefined {
	return demoPlans().find((plan) => plan.id === id);
}

/** A posted plan, as the SDK encodes it. */
export interface PostedPlan {
	pdsSceCod: string | null;
	tipoRegsce: number;
	attivita: { adCod: string | null; ordNum: number | null }[];
}

/**
 * Inserts a posted plan the way Esse3 does with `cancelValidPlanFlag`: the valid plan is
 * cancelled and the new one takes its place, approved at once unless its path asks the office.
 */
export function submitDemoPlan(body: PostedPlan): DemoPlan {
	const plans = demoPlans();
	const manual = body.tipoRegsce === 1;
	const plan: DemoPlan = {
		id: Math.max(...plans.map((item) => item.id)) + 1,
		schemaId: manual ? MANUAL_SCHEMA : PREAPPROVED_SCHEMA,
		state: manual ? 'P' : 'A',
		changed: days(0),
		activities: body.attivita.flatMap(({ adCod, ordNum }) => {
			const offered = adCod ? ruleOffer(adCod, ordNum) : undefined;
			return offered ? [offered] : [];
		})
	};
	plans.splice(0, plans.length, plan);
	return plan;
}

/** The plan's header, as `GET piani/:stuId` lists it. */
export function planHeader(plan: DemoPlan) {
	const { career } = DemoStudent;
	return {
		stuId: career.id,
		pianoId: plan.id,
		matId: career.enrollmentTraitId,
		regsceId: REGULATION_ID,
		schemaId: plan.schemaId,
		stato: plan.state,
		statoDes: plan.state === 'A' ? 'Approvato' : 'Proposto',
		dataUltimaVarStato: esse3Date(plan.changed),
		tipoPiano: 'S',
		coorte: career.academicYear,
		cdsStuId: career.programId,
		cdsStuCod: career.easyStaffProgramCode,
		cdsStuDes: career.description,
		aaOrdStuId: career.academicYear,
		pdsStuCod: 'GGG',
		pdsDes: 'PERCORSO COMUNE'
	};
}
