import type { Building } from './catalog.ts';

const ACCENTED: Record<string, string> = { A: 'À', E: 'È', I: 'Ì', O: 'Ò', U: 'Ù' };

/** The old name a building is still called by, from "ATLAS ex U1": "U1". */
export function buildingAlias(building: Building): string | undefined {
	return /\bex\s+(U\d+)\b/i.exec(building.name)?.[1].toUpperCase();
}

/**
 * The building's own name, without the old one, in title case as the rest of the app writes
 * places: "ATLAS ex U1" is "Atlas". The registry writes a final accent as an apostrophe
 * ("AGORA'"), which becomes the accented vowel ("Agorà"); words in mixed case stay as written.
 */
export function buildingName(building: Building): string {
	return building.name
		.replace(/\s+ex\s+U\d+\b/i, '')
		.replace(/([AEIOU])'(?=\s|$)/g, (_, vowel: string) => ACCENTED[vowel])
		.replace(/\p{Lu}{2,}/gu, (word) => word.charAt(0) + word.slice(1).toLocaleLowerCase('it-IT'))
		.trim();
}

/** What a pin says: the old name, or the code without its "U-" prefix ("BG"). */
export function pinLabel(building: Building): string {
	return buildingAlias(building) ?? building.code.replace(/^U-/, '');
}

/**
 * One line naming the building, "U1 – Atlas", or its name alone when it has no old name. A dash:
 * the UI face's narrow word space and small middle dot would run "U1 · Atlas" into one word.
 */
export function buildingTitle(building: Building): string {
	const alias = buildingAlias(building);
	return alias ? `${alias} – ${buildingName(building)}` : buildingName(building);
}

/** Room names as typed in EasyStaff, without stray spaces: "Aula  2117", "U1- 01 con Podio". */
export function roomName(name: string): string {
	return name
		.replace(/\s+/g, ' ')
		.replace(/(\S)- (?=\d)/g, '$1-')
		.trim();
}
