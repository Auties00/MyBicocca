import type {
	MoodleCourseModule,
	MoodleCourseSection,
	MoodleModuleContent
} from '@mybicocca/data-api/elearning';
import { describe, expect, it } from 'vitest';
import {
	cleanName,
	contentBlocks,
	courseOutline,
	kalturaCmId,
	moduleTarget,
	typeCounts
} from './content.ts';

let next = 1;

function content(overrides: Partial<MoodleModuleContent>): MoodleModuleContent {
	return {
		type: 'file',
		fileName: 'slide.pdf',
		filePath: '/',
		fileSize: 1000,
		fileUrl:
			'https://elearning.unimib.it/webservice/pluginfile.php/1/mod_resource/content/1/slide.pdf',
		content: null,
		timeCreated: 0,
		timeModified: 0,
		sortOrder: 0,
		mimeType: 'application/pdf',
		isExternalFile: false,
		repositoryType: null,
		userId: 0,
		author: '',
		license: '',
		tags: [],
		...overrides
	};
}

function module(overrides: Partial<MoodleCourseModule>): MoodleCourseModule {
	const id = next++;
	return {
		id,
		url: `https://elearning.unimib.it/mod/resource/view.php?id=${id}`,
		name: `Activity ${id}`,
		instance: id + 1000,
		contextId: null,
		description: null,
		visible: 1,
		userVisible: true,
		availabilityInfo: null,
		visibleOnCoursePage: 1,
		modIcon: '',
		modName: 'resource',
		purpose: 'content',
		branded: null,
		modPlural: 'File',
		availability: null,
		indent: 0,
		onclick: null,
		afterLink: null,
		activityBadge: null,
		customData: null,
		noViewLink: null,
		completion: 0,
		completionData: null,
		downloadContent: 1,
		dates: [],
		groupMode: 0,
		contents: [content({})],
		contentsInfo: null,
		...overrides
	};
}

function section(modules: MoodleCourseModule[], overrides: Partial<MoodleCourseSection> = {}) {
	const id = next++;
	return {
		id,
		name: `Section ${id}`,
		visible: 1,
		summary: '',
		summaryFormat: 1,
		section: id,
		hiddenByNumSections: 0,
		userVisible: true,
		availabilityInfo: null,
		component: null,
		itemId: null,
		modules,
		...overrides
	} satisfies MoodleCourseSection;
}

const none = () => undefined;

describe('contentBlocks', () => {
	it('groups activities under the short label that names them', () => {
		const slides = module({});
		const exercises = module({});
		const blocks = contentBlocks(
			section([
				module({ modName: 'label', name: 'Esercitazioni', description: '<p>Esercitazioni</p>' }),
				slides,
				exercises
			]),
			none,
			'it'
		);
		expect(blocks).toEqual([
			{ kind: 'group', key: `g${slides.id}`, title: 'Esercitazioni', modules: [slides, exercises] }
		]);
	});

	it('keeps sentences, links and long text as notes, ending the run before them', () => {
		const before = module({});
		const after = module({});
		const sentence = '<p>Le slide escono dopo la lezione.</p>';
		const blocks = contentBlocks(
			section([
				before,
				module({ modName: 'label', name: 'Le slide escono', description: sentence }),
				module({ modName: 'label', name: 'x', description: '<p><a href="https://x.it">x</a></p>' }),
				after
			]),
			none,
			'it'
		);
		expect(blocks.map((block) => block.kind)).toEqual(['group', 'note', 'note', 'group']);
		expect(blocks[1]).toMatchObject({ html: sentence });
		expect(blocks[3]).toMatchObject({ title: null, modules: [after] });
	});

	it('leaves out activities hidden from the course page and empty labels', () => {
		const shown = module({});
		const blocks = contentBlocks(
			section([
				module({ visibleOnCoursePage: 0 }),
				module({ visible: 0 }),
				module({ modName: 'label', name: '', description: '<p><br></p>' }),
				shown
			]),
			none,
			'it'
		);
		expect(blocks).toEqual([{ kind: 'group', key: `g${shown.id}`, title: null, modules: [shown] }]);
	});

	it('inlines a subsection, found by its link or by its item id', () => {
		const inner = module({});
		const child = section([inner], {
			component: 'mod_subsection',
			itemId: 77,
			name: 'Laboratorio'
		});
		const byLink = module({
			modName: 'subsection',
			name: '',
			customData: `{"sectionid":"${child.id}"}`
		});
		const byItem = module({ modName: 'subsection', name: 'Extra', instance: 77 });
		const outline = courseOutline([section([byLink, byItem]), child], 'it');
		expect(outline).toHaveLength(1);
		const [first, second] = outline[0].blocks;
		expect(first).toMatchObject({ kind: 'subsection', title: 'Laboratorio' });
		expect(second).toMatchObject({ kind: 'subsection', title: 'Extra' });
		expect(first.kind === 'subsection' && first.blocks[0]).toMatchObject({ modules: [inner] });
	});
});

describe('courseOutline', () => {
	it('numbers only the sections that show something, with copy suffixes dropped', () => {
		const outline = courseOutline(
			[
				section([], { name: 'Vuota' }),
				section([module({})], { name: 'Settimana 1 (copia)' }),
				section([module({})], { visible: 0 }),
				section([], { name: '', summary: '<p>Benvenuti</p>' })
			],
			'it'
		);
		expect(outline.map(({ ordinal, title }) => [ordinal, title])).toEqual([
			[1, 'Settimana 1'],
			[2, null]
		]);
	});
});

describe('moduleTarget', () => {
	it('routes Moodle activities to their pages', () => {
		expect(moduleTarget(module({ modName: 'quiz', instance: 5 }), 'it')).toEqual({
			kind: 'quiz',
			id: 5
		});
		expect(moduleTarget(module({ modName: 'forum', instance: 6 }), 'it')).toEqual({
			kind: 'forum',
			id: 6
		});
		expect(moduleTarget(module({ modName: 'hsuforum' }), 'it').kind).toBe('external');
	});

	it('plays Kaltura resources and links to them', () => {
		const resource = module({ modName: 'kalvidres', contents: [] });
		expect(moduleTarget(resource, 'it')).toEqual({ kind: 'video', cmId: resource.id });
		const link = module({
			modName: 'url',
			url: 'https://elearning.unimib.it/mod/kalvidres/view.php?forceview=1&id=4242'
		});
		expect(kalturaCmId(link)).toBe(4242);
	});

	it('opens a page or a resource by its HTML entry, a single file, or several as a folder', () => {
		const page = module({
			modName: 'page',
			name: 'Note',
			contents: [content({ fileName: 'index.html', mimeType: null })]
		});
		expect(moduleTarget(page, 'it')).toMatchObject({
			kind: 'file',
			file: { name: 'index.html', mime: 'text/html' }
		});
		expect(moduleTarget(module({}), 'it')).toMatchObject({ kind: 'file' });
		const many = module({ contents: [content({}), content({ fileName: 'b.pdf' })] });
		expect(moduleTarget(many, 'it')).toMatchObject({ kind: 'folder' });
	});

	it('takes a link’s resolved target, and sends anything else to Moodle', () => {
		const link = module({
			modName: 'url',
			contents: [content({ type: 'url', fileUrl: 'https://visualgo.net/it' })]
		});
		expect(moduleTarget(link, 'it')).toEqual({ kind: 'link', url: 'https://visualgo.net/it' });
		const lti = module({
			modName: 'lti',
			url: 'https://elearning.unimib.it/mod/lti/view.php?id=9'
		});
		expect(moduleTarget(lti, 'it')).toEqual({ kind: 'external', url: lti.url });
	});
});

describe('typeCounts', () => {
	it('keeps the three most frequent kinds, subsections included', () => {
		const video = () => module({ modName: 'kalvidres', contents: [] });
		const child = section([video(), video()], { component: 'mod_subsection', itemId: 9 });
		const parent = section([
			module({}),
			module({ modName: 'quiz' }),
			module({ modName: 'folder' }),
			module({ modName: 'folder' }),
			video(),
			module({ modName: 'subsection', name: 'Sub', instance: 9 })
		]);
		const [card] = courseOutline([parent, child], 'it');
		expect(typeCounts(card.blocks)).toEqual([
			{ kind: 'video', count: 3 },
			{ kind: 'folder', count: 2 },
			{ kind: 'pdf', count: 1 }
		]);
	});
});

describe('cleanName', () => {
	it('drops repeated copy suffixes', () => {
		expect(cleanName('Slide 1 (copia) (copy)')).toBe('Slide 1');
	});
});
