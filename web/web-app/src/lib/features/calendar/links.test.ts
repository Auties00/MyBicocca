import { describe, expect, it, vi } from 'vitest';

vi.mock('$app/paths', () => ({
	resolve: (path: string, params: Record<string, string> = {}) =>
		`/${path.replace(/^\/\(app\)\//, '').replace(/\[(\w+)\]/g, (_, key: string) => params[key])}`
}));

const { campusMapLink, mapsLink } = await import('./links.ts');

describe('mapsLink', () => {
	it('reads the coordinates out of EasyStaff’s embedded map', () => {
		const embed =
			'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2795.2!2d9.2117736!3d45.5130119!2m3';
		expect(mapsLink({ building: 'U24', room: 'U24-C2', mapsUrl: embed })).toBe(
			'https://www.google.com/maps/search/?api=1&query=45.5130119,9.2117736'
		);
	});

	it('opens any other map as it is, or searches for the place', () => {
		expect(mapsLink({ building: null, room: null, mapsUrl: 'https://maps.example/x' })).toBe(
			'https://maps.example/x'
		);
		expect(mapsLink({ building: 'U7', room: 'LAB 713', mapsUrl: null })).toBe(
			'https://www.google.com/maps/search/?api=1&query=U7%20LAB%20713%20Universit%C3%A0%20di%20Milano-Bicocca'
		);
		expect(mapsLink(null)).toBeNull();
	});
});

describe('campusMapLink', () => {
	it('opens the room, or the building, the timetable names', () => {
		expect(campusMapLink({ building: 'U06', room: 'U6-06', mapsUrl: null })).toBe('/map/U06/U6-06');
		expect(campusMapLink({ building: 'U24', room: null, mapsUrl: null })).toBe('/map/U24');
	});

	it('gives nothing for a place the map has no building for', () => {
		expect(campusMapLink({ building: 'Biblioteca di Ateneo', room: 'Sala 1', mapsUrl: null })).toBe(
			null
		);
		expect(campusMapLink(null)).toBeNull();
	});
});
