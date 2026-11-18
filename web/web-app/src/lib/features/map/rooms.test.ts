import type { EasyStaffRoomShowcaseRoom } from '@mybicocca/data-api/easystaff';
import { describe, expect, it } from 'vitest';
import { buildings, findBuilding } from './catalog.ts';
import { buildingName, pinLabel } from './names.ts';
import {
	buildingRooms,
	buildingSites,
	groupByFloor,
	withShowcase,
	type MapRoom,
	type RoomsCatalog
} from './rooms.ts';

const building = (code: string) => {
	const found = buildings.find((entry) => entry.code === code);
	if (!found) throw new Error(code);
	return found;
};

const room = (code: string, name: string, capacity: number | null = 40) => ({
	code,
	name,
	capacity
});

const catalog: RoomsCatalog = {
	U06: [room('U6-02', 'U6-02'), room('U6-10', 'U6-10 con Podio'), room('U6-01a', 'U6-01a')],
	'U06-GIURISPRUDENZA': [room('GIURI-2117', 'Aula  2117', 30)],
	'U05-MATERIALI': [room('U5-2107', 'U5-2107', 20)],
	U01: [room('U1-01', 'U1- 01 con Podio', 163), room('all', 'Tutte'), room('U1-02', 'U1-02', 0)],
	LIB: [room('LAB-1', 'Laboratorio 1')]
};

describe('findBuilding', () => {
	it('finds a building by code, old name or one of its sites', () => {
		expect(findBuilding('u01')?.code).toBe('U01');
		expect(findBuilding('U6')?.code).toBe('U06');
		expect(findBuilding('U5')?.code).toBe('U05-MATERIALI');
		expect(findBuilding('U14-DISCO')?.code).toBe('U14');
		expect(findBuilding('U-BG')?.code).toBe('U-BG');
		expect(findBuilding('LIB')).toBeUndefined();
	});
});

describe('names', () => {
	it('drops the old name, restores final accents and writes it in title case', () => {
		expect(buildingName(building('U01'))).toBe('Atlas');
		expect(buildingName(building('U06'))).toBe('Agorà');
		expect(buildingName(building('U26'))).toBe('Agon (PalaBicocca)');
		expect(buildingName(building('U38'))).toBe('Villa Serena');
		expect(pinLabel(building('U09'))).toBe('U9');
		expect(pinLabel(building('U-BG'))).toBe('BG');
	});
});

describe('buildingRooms', () => {
	it("joins the departments' sites to the building they stand in", () => {
		expect(buildingSites(building('U06'), catalog)).toEqual(['U06', 'U06-GIURISPRUDENZA']);
		expect(buildingSites(building('U05-MATERIALI'), catalog)).toEqual(['U05-MATERIALI']);
	});

	it('sorts names naturally, cleans them and drops the "all" entry', () => {
		expect(buildingRooms(building('U06'), catalog).map((entry) => entry.name)).toEqual([
			'Aula 2117',
			'U6-01a',
			'U6-02',
			'U6-10 con Podio'
		]);
		const atlas = buildingRooms(building('U01'), catalog);
		expect(atlas.map((entry) => entry.code)).toEqual(['U1-01', 'U1-02']);
		expect(atlas[0].name).toBe('U1-01 con Podio');
		expect(atlas[1].capacity).toBeNull();
	});
});

const showcaseRoom = (
	code: string,
	floor: string | null,
	capacity: number | null
): EasyStaffRoomShowcaseRoom => ({
	code,
	name: code,
	description: null,
	capacity,
	typeName: null,
	googleMapsLink: null,
	floor,
	isAccessible: false,
	accessibilityNotes: null,
	isInclusionValidated: false,
	interactive360Link: null,
	fixedEquipment: [],
	mobileEquipment: []
});

describe('floors', () => {
	it('takes floors and missing seats from the showcase, and groups lowest first', () => {
		const rooms: MapRoom[] = [
			{ code: 'A', name: 'A', capacity: null, building: 'U01', floor: null },
			{ code: 'B', name: 'B', capacity: 50, building: 'U01', floor: null },
			{ code: 'C', name: 'C', capacity: 20, building: 'U01', floor: null },
			{ code: 'D', name: 'D', capacity: 20, building: 'U01', floor: null }
		];
		const showcase = new Map([
			['A', showcaseRoom('A', 'T', 120)],
			['B', showcaseRoom('B', '-1', 0)],
			['C', showcaseRoom('C', 'Piano 1', null)]
		]);
		const merged = withShowcase(rooms, showcase);
		expect(merged.map((entry) => [entry.floor, entry.capacity])).toEqual([
			[0, 120],
			[-1, 50],
			[1, 20],
			[null, 20]
		]);
		expect(groupByFloor(merged).map((group) => [group.floor, group.rooms.length])).toEqual([
			[-1, 1],
			[0, 1],
			[1, 1],
			[null, 1]
		]);
	});
});
