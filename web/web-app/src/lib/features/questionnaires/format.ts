import { formatCredits } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import {
	moduleName,
	type ActivityStatus,
	type QuestionnaireActivity,
	type TeachingUnit
} from './model.ts';

const STATUS_LABELS: Record<ActivityStatus, () => string> = {
	todo: m.questionnaires_status_todo,
	partial: m.questionnaires_status_partial,
	done: m.questionnaires_status_done,
	misconfigured: m.questionnaires_status_misconfigured
};

export function statusLabel(status: ActivityStatus): string {
	return STATUS_LABELS[status]();
}

/**
 * "E3101Q107 · 8 CFU", with the status where its section's heading doesn't tell it: a course
 * half done or with a broken questionnaire.
 */
export function activityLine(activity: QuestionnaireActivity): string {
	const parts = [activity.code, activity.credits > 0 ? formatCredits(activity.credits) : null];
	if (activity.status === 'partial' || activity.status === 'misconfigured') {
		parts.push(statusLabel(activity.status));
	}
	return parts.filter((part) => part).join(' · ');
}

/**
 * What tells a lecturer's units apart, under the course's name: "Laboratorio · Turno M-Z" for a
 * unit of Basi di Dati named "Basi di Dati - Laboratorio".
 */
export function unitLine(unit: TeachingUnit, course: string | undefined): string | undefined {
	const module = unit.lecturer !== null && unit.name !== course ? unit.name : null;
	const parts = [module && course ? moduleName(module, course) : module, unit.partition].filter(
		(part) => part !== null
	);
	return parts.length > 0 ? parts.join(' · ') : undefined;
}

/**
 * The list's subtitle: what waits to be filled in, else the misconfigured questionnaires left to
 * check, which the pending count leaves out.
 */
export function pendingLine(pending: number, toCheck: number): string {
	if (pending > 0) return m.questionnaires_pending_count({ count: pending });
	if (toCheck > 0) return m.questionnaires_check_count({ count: toCheck });
	return m.questionnaires_none_pending();
}
