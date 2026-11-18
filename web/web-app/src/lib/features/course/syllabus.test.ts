import { describe, expect, it } from 'vitest';
import {
	buildSyllabus,
	courseLevel,
	courseMetadata,
	parseProgramme,
	semester,
	teachesIn
} from './syllabus.ts';

const body = (html: string) =>
	new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html').body;

describe('parseProgramme', () => {
	it('reads bold-led paragraphs with their topics on separate lines', () => {
		expect(
			parseProgramme(
				body(
					'<p><strong>Parte generale</strong><br>Analisi asintotica<br>Ordinamento</p>' +
						'<p><strong>Laboratorio</strong><br>Progetto finale</p>'
				)
			)
		).toEqual([
			{ title: 'Parte generale', items: ['Analisi asintotica', 'Ordinamento'] },
			{ title: 'Laboratorio', items: ['Progetto finale'] }
		]);
	});

	it('takes a short plain paragraph as the title of the list after it', () => {
		expect(
			parseProgramme(body('<p>Modulo A</p><ul><li>Grafi</li><li>Alberi</li></ul><p>Fine.</p>'))
		).toEqual([{ title: 'Modulo A', items: ['Grafi', 'Alberi', 'Fine.'] }]);
	});

	it('numbers parts written as a one-item ordered list before their topics', () => {
		expect(
			parseProgramme(body('<ol start="2"><li>Ricorsione</li></ol><ul><li>Torri di Hanoi</li></ul>'))
		).toEqual([{ title: '2. Ricorsione', items: ['Torri di Hanoi'] }]);
	});

	it('splits nested lists into parts', () => {
		expect(
			parseProgramme(
				body('<ul><li>Probabilità<ul><li>Eventi</li><li>Bayes</li></ul></li><li>Stima</li></ul>')
			)
		).toEqual([{ title: 'Probabilità', items: ['Eventi', 'Bayes', 'Stima'] }]);
	});

	it('reads dash outlines, titles on their own lines', () => {
		expect(
			parseProgramme(body('<p>Introduzione<br>- Storia<br>- Strumenti<br>Metodi<br>- Prove</p>'))
		).toEqual([
			{ title: 'Introduzione', items: ['Storia', 'Strumenti'] },
			{ title: 'Metodi', items: ['Prove'] }
		]);
	});

	it('splits long run-on topics at sentences and commas', () => {
		const long =
			'Spazi vettoriali: basi, dimensione, sottospazi, somme dirette e prodotti interni definiti sugli spazi reali e complessi di dimensione finita';
		const [part] = parseProgramme(body(`<ul><li>${long}</li><li>${long}</li></ul>`));
		expect(part.title).toBe('Spazi vettoriali');
		expect(part.items.slice(0, 3)).toEqual(['basi', 'dimensione', 'sottospazi']);
	});

	it('joins a line that continues the topic before it', () => {
		expect(parseProgramme(body('<p><b>Reti</b><br>Livello di<br>trasporto</p>'))).toEqual([
			{ title: 'Reti', items: ['Livello di trasporto'] }
		]);
	});
});

describe('buildSyllabus', () => {
	it('matches fields by their title in either language and prefers the custom fields', () => {
		const syllabus = buildSyllabus(
			[
				{ title: 'Obiettivi formativi', htmlContent: '<p>Capire</p>' },
				{ title: 'Assessment method', htmlContent: '<p>Written exam</p>' },
				{ title: 'Lingua di insegnamento', htmlContent: '<p>Inglese</p>' },
				{ title: 'Prerequisiti', htmlContent: '<p> </p>' }
			],
			courseMetadata({
				customFields: [
					{ shortName: 'cfu', value: ' 8 ' },
					{ shortName: 'ore', value: '0' },
					{ shortName: 'periodo', value: 'Primo Semestre' },
					{ shortName: 'tipologiacds', value: 'Laurea Magistrale' }
				]
			} as never)
		);
		expect(syllabus).toMatchObject({
			credits: 8,
			hours: null,
			language: 'Inglese',
			level: 'master',
			semester: 'first',
			objectives: '<p>Capire</p>',
			assessment: '<p>Written exam</p>',
			prerequisites: null
		});
	});
});

describe('levels and terms', () => {
	it('reads the degree level and the teaching period', () => {
		expect(courseLevel('Laurea Triennale')).toBe('bachelor');
		expect(courseLevel('Dottorato di ricerca')).toBe('doctorate');
		expect(semester('II semestre')).toBe('second');
		expect(semester('Annuale')).toBe('annual');
		expect(semester(null)).toBeNull();
	});

	it('maps terms onto September-to-August months', () => {
		expect([8, 0, 1, 2].map((month) => teachesIn('first', month))).toEqual([
			true,
			true,
			true,
			false
		]);
		expect(teachesIn('second', 6)).toBe(true);
		expect(teachesIn('annual', 7)).toBe(true);
	});
});
