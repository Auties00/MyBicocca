import type { MoodleWebCatalog, MoodleWebCatalogCategory } from '@mybicocca/data-api/elearning';
import { parseCourseCode } from '../courseCode.ts';
import { foldText, type FoldedText } from '../search/matcher.ts';
import type {
	CatalogArea,
	CatalogCategory,
	CatalogCourse,
	CatalogLevel,
	CatalogRoot
} from './model.ts';

export interface IndexedNode {
	key: string;
	name: string;
	url: string | null;
	/** -1 for an area. */
	parent: number;
	/** The area's node index. */
	area: number;
	children: number[];
	courses: number[];
	/** Courses anywhere below. */
	total: number;
	/**
	 * Where opening it leads: itself, or for an empty cross-listing (a category listed again
	 * under another parent, with nothing in it) the listing that holds its contents.
	 */
	target: number;
	folded: FoldedText;
	/** Position in the catalog, depth first: breaks ties between equally good hits. */
	order: number;
	/** The academic year it belongs to (2026 for "A.A. 2026-2027"), its own or an ancestor's. */
	year: number | null;
	/**
	 * The category holding the academic years this one lies under (a programme's "Insegnamenti"):
	 * a teaching keeps its name there from one year to the next while its code may change.
	 */
	anchor: number;
}

export interface IndexedCourse extends CatalogCourse {
	parent: number;
	foldedName: FoldedText;
	foldedCode: FoldedText;
	order: number;
	/** The academic year of the edition, from its code or its category. */
	year: number | null;
	/** What every edition of the teaching shares: its activity code and stream, when it has one. */
	codeKey: string | null;
	/** Its name under its anchor, shared by its editions even across a change of code. */
	nameKey: string | null;
}

/** The catalog flattened into arrays that point at each other by index, for the worker. */
export interface CatalogIndex {
	fetchedAt: string;
	sections: { name: string; areas: number[] }[];
	nodes: IndexedNode[];
	courses: IndexedCourse[];
	byKey: Map<string, number>;
}

/** "A.A. 2026-2027", "Edizione 2025/2026", "A.Y. 2024-2025": a category of one academic year. */
const ACADEMIC_YEAR = /\b((?:19|20)\d\d)\s*[-/]\s*(?:(?:19|20)\d\d|\d\d)\b/;

/** An activity code worth matching editions by: letters and digits, not a bare number. */
const ACTIVITY_CODE = /^(?=.*[a-z])(?=.*\d)[a-z0-9]{5,}$/i;

function codeKeyOf(code: string): string | null {
	const parsed = parseCourseCode(code);
	if (parsed.academicYear === null || parsed.code === null || !ACTIVITY_CODE.test(parsed.code)) {
		return null;
	}
	return `${parsed.code.toLowerCase()}|${parsed.stream ?? ''}`;
}

/** `12510` from `…/course/index.php?categoryid=12510`, else the page's slug (`area-giuridica`). */
export function nodeKey(url: string | null): string | null {
	if (url === null) return null;
	const id = /[?&]categoryid=(\d+)/.exec(url);
	if (id) return id[1];
	const slug = /^https?:\/\/[^/]+\/([^?#]+?)\/?(?:[?#].*)?$/.exec(url);
	return slug ? slug[1] : null;
}

export function buildCatalogIndex(catalog: MoodleWebCatalog): CatalogIndex {
	const nodes: IndexedNode[] = [];
	const courses: IndexedCourse[] = [];
	let order = 0;
	const add = (category: MoodleWebCatalogCategory, parent: number, area: number): number => {
		const index = nodes.length;
		const name = category.name.trim();
		const above = parent < 0 ? undefined : nodes[parent];
		const ownYear = ACADEMIC_YEAR.exec(name);
		const node: IndexedNode = {
			key: nodeKey(category.url) ?? `n${index}`,
			name,
			url: category.url,
			parent,
			area: area < 0 ? index : area,
			children: [],
			courses: [],
			total: 0,
			target: index,
			folded: foldText(name),
			order: order++,
			year: ownYear ? Number(ownYear[1]) : (above?.year ?? null),
			anchor: ownYear ? parent : (above?.anchor ?? -1)
		};
		nodes.push(node);
		for (const course of category.courses) {
			const courseName = course.name.trim();
			const code = course.code.trim();
			const foldedName = foldText(courseName);
			node.courses.push(courses.length);
			courses.push({
				id: course.id,
				name: courseName,
				code,
				parent: index,
				foldedName,
				foldedCode: foldText(code),
				order: order++,
				year: parseCourseCode(code).academicYear ?? node.year,
				codeKey: codeKeyOf(code),
				nameKey: node.anchor < 0 ? null : `${node.anchor}|${foldedName.folded}`
			});
		}
		for (const child of category.categories) node.children.push(add(child, index, node.area));
		node.total = node.courses.length + node.children.reduce((sum, i) => sum + nodes[i].total, 0);
		return index;
	};
	const sections = catalog.areas.map((area) => ({
		name: area.name.trim(),
		areas: area.categories.map((category) => add(category, -1, -1))
	}));
	const byKey = new Map<string, number>();
	const isEmpty = (node: IndexedNode) => node.children.length === 0 && node.courses.length === 0;
	nodes.forEach((node, index) => {
		const known = byKey.get(node.key);
		if (known === undefined || (isEmpty(nodes[known]) && !isEmpty(node)))
			byKey.set(node.key, index);
	});
	const areaByName = new Map(
		sections.flatMap((section) => section.areas.map((index) => [nodes[index].name, index] as const))
	);
	for (const [index, node] of nodes.entries()) {
		if (!isEmpty(node)) continue;
		// A whole area listed again under another (Scuole di Specializzazione under Area Medica)
		// has its own address; other names repeat across programmes and can't be trusted.
		const canonical = byKey.get(node.key) ?? index;
		const target = canonical !== index ? canonical : (areaByName.get(node.name) ?? index);
		node.target = target;
	}
	return { fetchedAt: catalog.fetchedAt, sections, nodes, courses, byKey };
}

/** The category a list row stands for: an empty cross-listing shows what it leads to. */
export function categoryOf(index: CatalogIndex, at: number): CatalogCategory {
	const node = index.nodes[at];
	const target = index.nodes[node.target];
	return {
		key: target.key,
		name: node.name,
		categories: target.children.length,
		courses: target.total
	};
}

export function catalogRoot(index: CatalogIndex): CatalogRoot {
	return {
		fetchedAt: index.fetchedAt,
		sections: index.sections.map((section) => ({
			name: section.name,
			areas: section.areas.map((at): CatalogArea => ({
				...categoryOf(index, at),
				section: section.name
			}))
		}))
	};
}

/** The node a key addresses, followed to its contents; undefined when the catalog lacks it. */
export function resolveKey(index: CatalogIndex, key: string): number | undefined {
	const at = index.byKey.get(key);
	return at === undefined ? undefined : index.nodes[at].target;
}

/** From the area down to `at`, both included. */
export function pathTo(index: CatalogIndex, at: number): number[] {
	const path: number[] = [];
	for (let node = at; node >= 0; node = index.nodes[node].parent) path.push(node);
	return path.reverse();
}

export function catalogLevel(index: CatalogIndex, key: string): CatalogLevel | undefined {
	const at = resolveKey(index, key);
	if (at === undefined) return undefined;
	const node = index.nodes[at];
	const path = pathTo(index, at);
	const area = index.nodes[node.area];
	const siblings =
		node.parent < 0
			? (index.sections.find((section) => section.areas.includes(at))?.areas ?? [at])
			: index.nodes[node.parent].children;
	return {
		key: node.key,
		name: node.name,
		area: { key: area.key, name: area.name },
		ancestors: path
			.slice(0, -1)
			.map((i) => ({ key: index.nodes[i].key, name: index.nodes[i].name })),
		siblings: siblings.map((i) => categoryOf(index, i)),
		categories: node.children.map((i) => categoryOf(index, i)),
		courses: node.courses.map((i) => {
			const { id, name, code } = index.courses[i];
			return { id, name, code };
		})
	};
}
