import { QueryMatcher, Tier, type FoldedText, type Match } from '../search/matcher.ts';
import { resolveKey, type CatalogIndex } from './index.ts';
import type {
	CatalogEdition,
	CatalogSearchCategory,
	CatalogSearchCourse,
	CatalogSearchNode,
	CatalogSearchResult
} from './model.ts';

/** Hits shown at once, shared between the branches of the level searched. */
export const HIT_BUDGET = 40;

/** A subsequence lit in more pieces than this is letters picked across a title, not an abbreviation. */
const SCATTERED_PIECES = 3;

const NO_HIT = Number.MAX_SAFE_INTEGER;

/** The rank's span of one band: tier, place and kind fit below it. */
const BAND = 64;

/**
 * How good a hit is, lower first. Hits fall in bands: a substring, word prefixes or initials; then
 * abbreviations and typos; then letters scattered across a title. Only the best band present is
 * shown, so a few close matches never drown in loose ones. Within a band the matcher's tier
 * orders; then where the match starts (the title's start, a word's, inside a word); then a
 * category comes before a course: it gathers courses.
 */
function rankOf(match: Match, text: FoldedText, course: boolean): number {
	const band =
		match.tier <= Tier.acronym
			? 0
			: match.tier === Tier.subsequence && match.ranges.length > SCATTERED_PIECES
				? 2
				: 1;
	const start = match.ranges[0]?.[0] ?? 0;
	const place = start === 0 ? 0 : text.tokens.some((token) => token.start === start) ? 1 : 2;
	return band * BAND + match.tier * 8 + place * 2 + (course ? 1 : 0);
}

const bandOf = (rank: number) => Math.floor(rank / BAND);

interface CategoryHit {
	kind: 'category';
	at: number;
	match: Match;
	rank: number;
}

interface CourseHit {
	kind: 'course';
	at: number;
	name: Match | null;
	code: Match | null;
	rank: number;
	editions: CatalogEdition[];
}

/** A branch of the level searched, sharing the hit budget with the others. */
interface Branch {
	/** Its top category; -1 for the courses directly in the level. */
	root: number;
	hits: (CategoryHit | CourseHit)[];
	shown: number;
}

/**
 * Editions of one teaching among the hits join in one: the newest stands for them and lists the
 * others. Editions share their activity code, or their name under the same programme, whose code
 * may change from a year to the next.
 */
function mergeEditions(index: CatalogIndex, hits: CourseHit[]): CourseHit[] {
	const parent = hits.map((_, i) => i);
	const find = (i: number): number => {
		while (parent[i] !== i) i = parent[i] = parent[parent[i]];
		return i;
	};
	const join = (a: number, b: number) => {
		parent[find(a)] = find(b);
	};
	const byCode = new Map<string, number>();
	const byName = new Map<string, number[]>();
	hits.forEach((hit, i) => {
		const course = index.courses[hit.at];
		if (course.codeKey !== null) {
			const first = byCode.get(course.codeKey);
			if (first === undefined) byCode.set(course.codeKey, i);
			else join(i, first);
		}
		if (course.nameKey !== null) {
			const bucket = byName.get(course.nameKey) ?? [];
			// One name twice in a year is two teachings (two years of study), not two editions.
			for (const j of bucket) if (index.courses[hits[j].at].year !== course.year) join(i, j);
			bucket.push(i);
			byName.set(course.nameKey, bucket);
		}
	});
	const groups = Map.groupBy(hits.keys(), find);
	const newestFirst = (a: number, b: number) => {
		const x = index.courses[hits[a].at];
		const y = index.courses[hits[b].at];
		return (y.year ?? -1) - (x.year ?? -1) || x.order - y.order;
	};
	return [...groups.values()].map((members) => {
		const [newest, ...others] = members.toSorted(newestFirst);
		return {
			...hits[newest],
			rank: Math.min(...members.map((i) => hits[i].rank)),
			editions: others.map((i) => {
				const { id, name, code, year } = index.courses[hits[i].at];
				return { course: { id, name, code }, year };
			})
		};
	});
}

/** Splits `budget` hits between branches: the small ones show whole, the big ones share the rest. */
function allocate(branches: Branch[], budget: number): void {
	let left = budget;
	const bySize = branches.toSorted((a, b) => a.hits.length - b.hits.length);
	bySize.forEach((branch, i) => {
		const share = Math.max(1, Math.floor(left / (bySize.length - i)));
		branch.shown = Math.min(branch.hits.length, share);
		left = Math.max(0, left - branch.shown);
	});
}

/**
 * The catalog searched with the five-tier matcher (substring, word prefixes, acronym,
 * abbreviation, typos), at the root across every area, inside a level only below it. Course codes
 * match only as written (a substring or a prefix): a code one digit off is another course.
 *
 * The answer is a pruned tree of the best hits: the best band of matches only, the editions of a
 * teaching as one hit, and at most {@link HIT_BUDGET} hits split between the level's branches,
 * each overflowing branch ending in a row that searches it alone. Siblings come strongest first,
 * the catalog's order breaking ties; categories before courses.
 */
export function searchCatalog(
	index: CatalogIndex,
	query: string,
	scope: string | null
): CatalogSearchResult {
	const matcher = new QueryMatcher(query);
	const at = scope === null ? undefined : resolveKey(index, scope);
	const result: CatalogSearchResult = {
		query,
		scope,
		unknownScope: scope !== null && at === undefined,
		area: at === undefined ? null : index.nodes[index.nodes[at].area].key,
		tree: [],
		total: 0,
		branches: [],
		capped: false
	};
	if (matcher.isEmpty || result.unknownScope) return result;

	const categoryHits: CategoryHit[] = [];
	let courseHits: CourseHit[] = [];
	const matchCourse = (course: number) => {
		const { foldedName, foldedCode } = index.courses[course];
		const name = matcher.matchFolded(foldedName);
		const code = matcher.matchFolded(foldedCode, Tier.prefix);
		if (name === null && code === null) return;
		const nameRank = name ? rankOf(name, foldedName, true) : NO_HIT;
		const codeRank = code ? rankOf(code, foldedCode, true) : NO_HIT;
		const rank = Math.min(nameRank, codeRank);
		// Only the matches of the course's best band light up: an exact code, not a typo in the name.
		const lit = (match: Match | null, own: number) => (bandOf(own) === bandOf(rank) ? match : null);
		courseHits.push({
			kind: 'course',
			at: course,
			name: lit(name, nameRank),
			code: lit(code, codeRank),
			rank,
			editions: []
		});
	};
	const visit = (node: number) => {
		const { folded } = index.nodes[node];
		const match = matcher.matchFolded(folded);
		if (match)
			categoryHits.push({ kind: 'category', at: node, match, rank: rankOf(match, folded, false) });
		for (const course of index.nodes[node].courses) matchCourse(course);
		for (const child of index.nodes[node].children) visit(child);
	};
	const roots =
		at === undefined
			? index.sections.flatMap((section) => section.areas)
			: index.nodes[at].children;
	roots.forEach(visit);
	if (at !== undefined) index.nodes[at].courses.forEach(matchCourse);

	let best = Number.POSITIVE_INFINITY;
	for (const hit of [...categoryHits, ...courseHits]) best = Math.min(best, bandOf(hit.rank));
	const inBand = <T extends { rank: number }>(hits: T[]) =>
		hits.filter((hit) => bandOf(hit.rank) === best);
	courseHits = mergeEditions(index, inBand(courseHits));
	const categories = inBand(categoryHits);
	result.total = categories.length + courseHits.length;
	if (result.total === 0) return result;

	// Which branch each hit lies in: the top category below the level searched.
	const branchOf = (node: number): number => {
		let top = node;
		while (index.nodes[top].parent !== (at ?? -1)) top = index.nodes[top].parent;
		return top;
	};
	const branches = new Map<number, Branch>();
	const branch = (root: number) => {
		let found = branches.get(root);
		if (!found) branches.set(root, (found = { root, hits: [], shown: 0 }));
		return found;
	};
	for (const hit of categories) branch(branchOf(hit.at)).hits.push(hit);
	for (const hit of courseHits) {
		const parent = index.courses[hit.at].parent;
		branch(parent === at ? -1 : branchOf(parent)).hits.push(hit);
	}
	const orderOf = (hit: CategoryHit | CourseHit) =>
		hit.kind === 'course' ? index.courses[hit.at].order : index.nodes[hit.at].order;
	for (const { hits } of branches.values()) {
		hits.sort((a, b) => a.rank - b.rank || orderOf(a) - orderOf(b));
	}
	allocate([...branches.values()], HIT_BUDGET);
	result.branches = [...branches.values()]
		.filter((branch) => branch.root >= 0)
		.toSorted((a, b) => index.nodes[a.root].order - index.nodes[b.root].order)
		.map(({ root, hits }) => ({
			key: index.nodes[index.nodes[root].target].key,
			name: index.nodes[root].name,
			count: hits.length
		}));

	// The tree of the hits kept: every category on their way up, ranked by its best hit below.
	const rankBelow = new Map<number, number>();
	const keptCategories = new Map<number, CategoryHit>();
	const keptCourses = new Map<number, CourseHit>();
	const climb = (from: number, rank: number) => {
		for (let node = from; node >= 0 && node !== at; node = index.nodes[node].parent) {
			rankBelow.set(node, Math.min(rankBelow.get(node) ?? NO_HIT, rank));
		}
	};
	for (const { hits, shown } of branches.values()) {
		for (const hit of hits.slice(0, shown)) {
			if (hit.kind === 'course') {
				keptCourses.set(hit.at, hit);
				climb(index.courses[hit.at].parent, hit.rank);
			} else {
				keptCategories.set(hit.at, hit);
				climb(hit.at, hit.rank);
			}
		}
	}
	// A category hit on the way to kept hits is on screen anyway: it counts as shown.
	for (const hit of categories) {
		if (rankBelow.has(hit.at)) keptCategories.set(hit.at, hit);
	}
	for (const branch of branches.values()) {
		branch.shown = branch.hits.filter((hit) =>
			hit.kind === 'course' ? keptCourses.has(hit.at) : keptCategories.has(hit.at)
		).length;
	}

	const byRankBelow = (a: number, b: number) =>
		(rankBelow.get(a) ?? NO_HIT) - (rankBelow.get(b) ?? NO_HIT) ||
		index.nodes[a].order - index.nodes[b].order;
	const byRank = (a: CourseHit, b: CourseHit) =>
		a.rank - b.rank || index.courses[a.at].order - index.courses[b.at].order;

	const courseNode = (hit: CourseHit): CatalogSearchCourse => {
		const { id, name, code, year } = index.courses[hit.at];
		return {
			kind: 'course',
			course: { id, name, code },
			year,
			nameRanges: hit.name?.ranges ?? [],
			codeRanges: hit.code?.ranges ?? [],
			editions: hit.editions
		};
	};
	const coursesIn = (courses: readonly number[]) =>
		courses
			.flatMap((course) => keptCourses.get(course) ?? [])
			.toSorted(byRank)
			.map(courseNode);
	const categoryNode = (node: number): CatalogSearchCategory => {
		const { children, courses, target, name } = index.nodes[node];
		const tree: CatalogSearchNode[] = [
			...children
				.filter((child) => rankBelow.has(child))
				.toSorted(byRankBelow)
				.map(categoryNode),
			...coursesIn(courses)
		];
		const overflow = branches.get(node);
		if (overflow && overflow.hits.length > overflow.shown) {
			tree.push({
				kind: 'more',
				key: index.nodes[target].key,
				name,
				count: overflow.hits.length - overflow.shown
			});
		}
		const hit = keptCategories.get(node);
		return {
			kind: 'category',
			key: index.nodes[target].key,
			name,
			nameRanges: hit?.match.ranges ?? [],
			match: hit !== undefined,
			children: tree
		};
	};

	result.tree = [
		...roots
			.filter((root) => rankBelow.has(root))
			.toSorted(byRankBelow)
			.map(categoryNode),
		...(at === undefined ? [] : coursesIn(index.nodes[at].courses))
	];
	const direct = branches.get(-1);
	result.capped = direct !== undefined && direct.hits.length > direct.shown;
	return result;
}
