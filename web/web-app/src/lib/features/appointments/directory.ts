import type { EasyStaffPlanningService } from '@mybicocca/data-api/easystaff';

/**
 * The portal lists one group per faculty area, too granular for a directory: the app folds them
 * into four sections, in this order.
 */
export type SectionId = 'career' | 'teaching' | 'deliveries' | 'other';

export const SECTION_ORDER: readonly SectionId[] = ['career', 'teaching', 'deliveries', 'other'];

export function isSectionId(value: string | null): value is SectionId {
	return value !== null && (SECTION_ORDER as readonly string[]).includes(value);
}

/** The section of a portal group, or of the service name when it has no group. */
export function sectionOf(groupOrName: string): SectionId {
	const text = groupOrName.trim().toLocaleLowerCase('it-IT');
	if (text.startsWith('carriere studenti')) return 'career';
	if (text.startsWith('didattica')) return 'teaching';
	if (text.startsWith('ritiro')) return 'deliveries';
	return 'other';
}

export function serviceSection(service: { name: string; group: string | null }): SectionId {
	return sectionOf(service.group ?? service.name);
}

const CAREER_SUFFIX = /\s*\(gestione carriere\)$/i;
const PREFIXES = [/^didattica\s*-\s*/i, /^gestione carriere\s*-\s*/i];

/**
 * The service name without the faculty grouping the portal repeats in it ("Didattica - Scienze",
 * "Psicologia (Gestione Carriere)"): the section already says it.
 */
export function displayName(name: string): string {
	let text = name.trim();
	for (const prefix of PREFIXES) text = text.replace(prefix, '');
	return text.replace(CAREER_SUFFIX, '').trim() || name.trim();
}

/** What a service is about, for its icon: the first keyword found in its name and group wins. */
export type ServiceKind =
	| 'badge'
	| 'diploma'
	| 'doctorate'
	| 'specialization'
	| 'grants'
	| 'international'
	| 'internship'
	| 'orientation'
	| 'career'
	| 'teaching'
	| 'desk';

const KINDS: ReadonlyArray<readonly [RegExp, ServiceKind]> = [
	[/badge/i, 'badge'],
	[/pergamen|diplom/i, 'diploma'],
	[/dottorat/i, 'doctorate'],
	[/specializzazion/i, 'specialization'],
	[/diritto allo studio/i, 'grants'],
	[/international|erasmus/i, 'international'],
	[/stage|tirocin/i, 'internship'],
	[/orientamento/i, 'orientation'],
	[/carrier/i, 'career'],
	[/didattica/i, 'teaching']
];

export function serviceKind(service: { name: string; group: string | null }): ServiceKind {
	const text = `${service.name} ${service.group ?? ''}`;
	return KINDS.find(([pattern]) => pattern.test(text))?.[1] ?? 'desk';
}

export interface DirectorySection {
	id: SectionId;
	services: EasyStaffPlanningService[];
}

/** The services by section, in the fixed order, each sorted by display name; empty ones left out. */
export function directorySections(
	services: readonly EasyStaffPlanningService[],
	locale: string
): DirectorySection[] {
	const collator = new Intl.Collator(locale, { sensitivity: 'base', numeric: true });
	return SECTION_ORDER.flatMap((id) => {
		const inSection = services
			.filter((service) => serviceSection(service) === id)
			.sort((a, b) => collator.compare(displayName(a.name), displayName(b.name)));
		return inSection.length > 0 ? [{ id, services: inSection }] : [];
	});
}

const fold = (text: string) =>
	text.normalize('NFD').replace(/\p{M}/gu, '').toLocaleLowerCase('it-IT');

/** The services whose name or group holds every word of `text`, accents and case ignored. */
export function searchServices(
	services: readonly EasyStaffPlanningService[],
	text: string
): EasyStaffPlanningService[] {
	const words = fold(text).split(/\s+/).filter(Boolean);
	if (words.length === 0) return [...services];
	return services.filter((service) => {
		const haystack = fold(`${service.name} ${service.group ?? ''}`);
		return words.every((word) => haystack.includes(word));
	});
}

/** The appointment length in whole minutes. */
export function durationMinutes(seconds: number): number {
	return Math.max(1, Math.round(seconds / 60));
}
