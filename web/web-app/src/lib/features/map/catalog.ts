import { buildingAlias } from './names.ts';

export type BuildingCategory = 'teaching' | 'library' | 'canteen' | 'admin' | 'sport' | 'other';

/** Every category in the order the filter lists them. */
export const categories: readonly BuildingCategory[] = [
	'teaching',
	'library',
	'canteen',
	'admin',
	'sport',
	'other'
];

/** A university site with a pin. `code` is also EasyStaff's key for its rooms. */
export interface Building {
	code: string;
	/** As the university names it: "ATLAS ex U1". */
	name: string;
	latitude: number;
	longitude: number;
	category: BuildingCategory;
	address: string;
	city: string;
}

const building = (
	code: string,
	name: string,
	[latitude, longitude]: readonly [number, number],
	category: BuildingCategory,
	address: string,
	city: string
): Building => ({ code, name, latitude, longitude, category, address, city });

/**
 * The sites, after Android's hand-curated `assets/buildings.json`: EasyStaff knows rooms but has no
 * coordinates. Ordered as Android lists them: the U buildings by number, then the other sites.
 */
export const buildings: readonly Building[] = [
	building(
		'U01',
		'ATLAS ex U1',
		[45.513237, 9.211776],
		'teaching',
		'Piazza della Scienza',
		'Milano'
	),
	building(
		'U02',
		'QUANTUM ex U2',
		[45.513559, 9.210597],
		'teaching',
		'Piazza della Scienza',
		'Milano'
	),
	building('U03', 'BIOS ex U3', [45.513753, 9.21207], 'teaching', 'Piazza della Scienza', 'Milano'),
	building(
		'U04',
		'TELLUS ex U4',
		[45.514084, 9.210898],
		'teaching',
		'Piazza della Scienza',
		'Milano'
	),
	building(
		'U05-MATERIALI',
		'RATIO ex U5',
		[45.512235, 9.212626],
		'teaching',
		'Via Roberto Cozzi 55',
		'Milano'
	),
	building(
		'U06',
		"AGORA' ex U6",
		[45.518271, 9.213858],
		'admin',
		"Piazza dell'Ateneo Nuovo 1",
		'Milano'
	),
	building(
		'U07',
		'CIVITAS ex U7',
		[45.516978, 9.213226],
		'teaching',
		'Via Bicocca degli Arcimboldi 8',
		'Milano'
	),
	building('U08', 'ASCLEPIO ex U8', [45.603824, 9.260999], 'teaching', 'Via Cadore 48', 'Monza'),
	building(
		'U09',
		"KOINE' ex U9",
		[45.51112, 9.211253],
		'teaching',
		"Viale dell'Innovazione 10",
		'Milano'
	),
	building('U14', 'ABACUS ex U14', [45.523764, 9.219442], 'teaching', 'Viale Sarca 336', 'Milano'),
	building(
		'U16',
		'MAIEUTICA ex U16',
		[45.524461, 9.209124],
		'teaching',
		"Piazza dell'Ateneo Nuovo 1",
		'Milano'
	),
	building('U18', 'YGEIA ex U18', [45.604545, 9.262625], 'teaching', 'Via Cadore', 'Monza'),
	building('U24', 'ZIFERA ex U24', [45.523795, 9.22102], 'teaching', 'Viale Sarca', 'Milano'),
	building(
		'U26',
		'AGON (PalaBicocca) ex U26',
		[45.524826, 9.209358],
		'sport',
		'Via Raffaello Giolli 7A',
		'Milano'
	),
	building('U28', 'KYTOS ex U28', [45.60536, 9.261668], 'teaching', 'Via Cadore', 'Monza'),
	building(
		'U38',
		'VILLA SERENA ex U38',
		[45.601853, 9.261639],
		'teaching',
		'Via Pergolesi',
		'Monza'
	),
	building(
		'U-BG',
		'Sede di Bergamo (Nini da Fano)',
		[45.689813, 9.633153],
		'teaching',
		'Via Alessandro Ninì da Fano 11',
		'Bergamo'
	),
	building(
		'U-MB',
		'Istituti Clinici Zucchi - Villa Maria',
		[45.676271, 9.239491],
		'teaching',
		'Piazza Madonnina 1',
		'Carate Brianza'
	)
];

const byCode = new Map(buildings.map((entry) => [entry.code.toUpperCase(), entry]));

/** The campus square, and the area the basemap covers. */
export const campus = {
	center: [9.212, 45.516] as [number, number],
	zoom: 15.5,
	bounds: [
		[9.189, 45.491],
		[9.653, 45.71]
	] as [[number, number], [number, number]]
};

/**
 * The building a URL or another feature names: its catalogue code ("U01", any case), its old
 * name ("U1", as timetables write it), or one of the EasyStaff sites inside it ("U06-FORMAZIONE").
 */
export function findBuilding(reference: string): Building | undefined {
	const key = reference.trim().toUpperCase();
	return (
		byCode.get(key) ??
		buildings.find((entry) => buildingAlias(entry) === key) ??
		buildings.find((entry) => key.startsWith(`${entry.code.toUpperCase()}-`))
	);
}

/** How many buildings each category has; categories without buildings are left out. */
export function categoryCounts(
	list: readonly Building[] = buildings
): Map<BuildingCategory, number> {
	const counts = new Map<BuildingCategory, number>();
	for (const category of categories) {
		const count = list.filter((entry) => entry.category === category).length;
		if (count > 0) counts.set(category, count);
	}
	return counts;
}

/** Buildings grouped by city, cities in the order their first building comes. */
export function byCity(list: readonly Building[]): Array<[city: string, buildings: Building[]]> {
	const groups = new Map<string, Building[]>();
	for (const entry of list) {
		const group = groups.get(entry.city);
		if (group) group.push(entry);
		else groups.set(entry.city, [entry]);
	}
	return [...groups];
}

/**
 * The buildings of the city that has the most of `list`, so a camera framing them never zooms out
 * to the whole province. Ties go to the city listed first.
 */
export function densestCity(list: readonly Building[]): Building[] {
	let best: Building[] = [];
	for (const [, group] of byCity(list)) if (group.length > best.length) best = group;
	return best;
}
