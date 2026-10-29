import { createContext } from 'svelte';
import type { Esse3TeachingUnitWithQuestionnaire } from '@mybicocca/data-api/esse3';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { isPending, toActivities, toActivityUnits, type QuestionnaireActivity } from './model.ts';
import { recordBookQuestionnairesQuery, teachingUnitsQuery } from './queries.ts';

/** The career's activities with a questionnaire, split by what is left to fill in. */
export class Questionnaires {
	readonly query = query(() => session.current && recordBookQuestionnairesQuery(session.current));
	readonly activities = $derived(toActivities(this.query.data ?? []));
	/** Not done yet: those waiting, and those Esse3 reports misconfigured. */
	readonly outstanding = $derived(this.activities.filter((activity) => activity.status !== 'done'));
	/** Waiting to be filled in: what the counts count. */
	readonly pending = $derived(this.outstanding.filter((activity) => isPending(activity.status)));
	readonly done = $derived(this.activities.filter((activity) => activity.status === 'done'));

	find(id: number | undefined): QuestionnaireActivity | undefined {
		return id === undefined ? undefined : this.activities.find((activity) => activity.id === id);
	}
}

export class ActivityUnitsQuery {
	readonly query: Query<Esse3TeachingUnitWithQuestionnaire>;
	readonly units = $derived.by(() => {
		const data = this.query.data;
		return data === undefined ? undefined : toActivityUnits(data);
	});

	constructor(activity: () => number | undefined) {
		this.query = query(() => {
			const scope = session.current;
			const id = activity();
			return scope && id !== undefined && teachingUnitsQuery(scope, id);
		});
	}
}

/** How the questionnaires layout shows its pages, for the pages to fit in. */
export interface QuestionnairesView {
	readonly questionnaires: Questionnaires;
	/** List and detail side by side; otherwise each page has the screen to itself. */
	readonly wide: boolean;
}

const [getView, setView] = createContext<QuestionnairesView>();

export { getView as useQuestionnairesView, setView as provideQuestionnairesView };
