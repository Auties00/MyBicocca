import { decodeEasyStaffPlanningService } from '@mybicocca/data-api/easystaff';
import { describe, expect, it } from 'vitest';
import {
	directorySections,
	displayName,
	searchServices,
	sectionOf,
	serviceKind
} from './directory.ts';

const service = (id: number, type: string, raggruppamento = '') => {
	const decoded = decodeEasyStaffPlanningService({ id, type, durata: 1200, raggruppamento });
	if (decoded === undefined) throw new Error('Not a service');
	return decoded;
};

describe('sectionOf', () => {
	it('folds the portal groups into the four sections, by prefix and in any case', () => {
		expect(sectionOf('Carriere Studenti')).toBe('career');
		expect(sectionOf('carriere studenti - Scienze')).toBe('career');
		expect(sectionOf('Didattica - Psicologia')).toBe('teaching');
		expect(sectionOf('Ritiro documenti')).toBe('deliveries');
		expect(sectionOf('Segreterie studenti')).toBe('other');
		expect(sectionOf('Carriere')).toBe('other');
	});
});

describe('displayName', () => {
	it('drops the grouping the portal repeats in service names', () => {
		expect(displayName('Didattica - Scienze')).toBe('Scienze');
		expect(displayName('Gestione carriere - Economia e Statistica')).toBe('Economia e Statistica');
		expect(displayName('Psicologia (Gestione Carriere)')).toBe('Psicologia');
		expect(displayName('  Ritiro Badge ')).toBe('Ritiro Badge');
	});

	it('keeps a name that would be left empty', () => {
		expect(displayName('Didattica - ')).toBe('Didattica -');
	});
});

describe('serviceKind', () => {
	it('picks the first keyword found in the name and group', () => {
		expect(serviceKind({ name: 'Ritiro Badge', group: 'Carriere Studenti' })).toBe('badge');
		expect(serviceKind({ name: 'Ritiro Pergamene di laurea', group: null })).toBe('diploma');
		expect(serviceKind({ name: 'Bicocca International - Erasmus+', group: null })).toBe(
			'international'
		);
		expect(serviceKind({ name: 'Scienze', group: 'Didattica' })).toBe('teaching');
		expect(serviceKind({ name: 'Segreteria studenti', group: null })).toBe('desk');
	});
});

describe('directorySections', () => {
	const services = [
		service(1, 'Segreteria studenti', 'Segreterie studenti'),
		service(2, 'Gestione carriere - Scienze', 'Carriere Studenti'),
		service(3, 'Psicologia (Gestione Carriere)', 'Carriere Studenti'),
		service(4, 'Didattica - Economia', 'Didattica'),
		service(5, 'Ritiro Badge')
	];

	it('orders the sections and their services by display name, leaving empty ones out', () => {
		const sections = directorySections(services, 'it-IT');
		expect(sections.map((section) => section.id)).toEqual([
			'career',
			'teaching',
			'deliveries',
			'other'
		]);
		expect(sections[0].services.map((item) => item.id)).toEqual([3, 2]);
		expect(directorySections(services.slice(3, 4), 'it-IT').map((section) => section.id)).toEqual([
			'teaching'
		]);
	});

	it('files a service with no group by its own name', () => {
		const deliveries = directorySections(services, 'it-IT').find(
			(section) => section.id === 'deliveries'
		);
		expect(deliveries?.services.map((item) => item.id)).toEqual([5]);
	});
});

describe('searchServices', () => {
	const services = [
		service(1, 'Ufficio Diritto allo Studio', 'Diritto allo Studio'),
		service(2, 'Orientamento in ingresso', 'Orientamento'),
		service(3, 'Didattica - Università e società', 'Didattica')
	];

	it('matches every word, ignoring case and accents, in the name or the group', () => {
		expect(searchServices(services, 'diritto STUDIO').map((item) => item.id)).toEqual([1]);
		expect(searchServices(services, 'universita').map((item) => item.id)).toEqual([3]);
		expect(searchServices(services, 'didattica societa').map((item) => item.id)).toEqual([3]);
		expect(searchServices(services, 'orientamento studio')).toEqual([]);
		expect(searchServices(services, '  ')).toHaveLength(3);
	});
});
