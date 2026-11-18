import type { Range } from '../search/matcher.ts';

/**
 * What the catalog worker answers: small views of the 3.6 MB catalog it keeps, so the page
 * never holds the whole tree. Nodes are addressed by `key`: the Moodle category id where the
 * category has one, else the slug of its page (`area-giuridica` for an area's own page).
 */

/** A category as a list shows it. */
export interface CatalogCategory {
	key: string;
	name: string;
	/** Categories directly inside. */
	categories: number;
	/** Courses anywhere below. */
	courses: number;
}

export interface CatalogCourse {
	id: number;
	name: string;
	/** The course's short name, such as `2627-2-E3101Q106` or `CDS-E1805M-DOC`. */
	code: string;
}

/** A tile of the catalog's root: one area and its accent key. */
export interface CatalogArea extends CatalogCategory {
	section: string;
}

export interface CatalogRoot {
	/** When the catalog was crawled, ISO 8601. */
	fetchedAt: string;
	sections: { name: string; areas: CatalogArea[] }[];
}

/** One level of the browse: a category, its contents and how it was reached. */
export interface CatalogLevel {
	key: string;
	name: string;
	/** The area it lies in, whose accent it wears. */
	area: { key: string; name: string };
	/** From the area down to the parent: the breadcrumb. */
	ancestors: { key: string; name: string }[];
	/** The parent's categories, this one among them; the area tiles' section for an area. */
	siblings: CatalogCategory[];
	categories: CatalogCategory[];
	courses: CatalogCourse[];
}

/** Another edition of a course hit: the same teaching in another year (or stream). */
export interface CatalogEdition {
	course: CatalogCourse;
	/** The September its academic year starts; null when neither its code nor its category says. */
	year: number | null;
}

/**
 * A search's answer as a pruned tree: every node leads to hits, strongest first among siblings.
 * At the root its top level is the areas; inside a level, that level's contents.
 */
export type CatalogSearchNode = CatalogSearchCategory | CatalogSearchCourse | CatalogSearchMore;

export interface CatalogSearchCategory {
	kind: 'category';
	/** Where opening it leads (an empty cross-listing opens the listing that holds its contents). */
	key: string;
	name: string;
	nameRanges: Range[];
	/** A hit itself, or only a waypoint to hits below it. */
	match: boolean;
	children: CatalogSearchNode[];
}

export interface CatalogSearchCourse {
	kind: 'course';
	course: CatalogCourse;
	year: number | null;
	nameRanges: Range[];
	codeRanges: Range[];
	/** The teaching's other editions among the hits, newest first; this row is the newest. */
	editions: CatalogEdition[];
}

/** The hits of a branch left out to make room for the others: searching there shows them. */
export interface CatalogSearchMore {
	kind: 'more';
	/** The category to search in. */
	key: string;
	name: string;
	count: number;
}

/** A category directly inside the level searched, with how many hits lie in it. */
export interface CatalogSearchBranch {
	key: string;
	name: string;
	count: number;
}

export interface CatalogSearchResult {
	query: string;
	/** The level asked for, as asked: null for the whole catalog. */
	scope: string | null;
	/** The level asked for is not in the catalog (an old link): the whole catalog was searched. */
	unknownScope: boolean;
	/** The area the scope lies in, whose accent the rows wear; null at the root. */
	area: string | null;
	tree: CatalogSearchNode[];
	/** Hits found, editions of one teaching counting once. */
	total: number;
	/** The level's categories that hold hits, in the catalog's order: at the root, the areas. */
	branches: CatalogSearchBranch[];
	/** Hits left out with no "more" row to reach them: the query is too broad for the level. */
	capped: boolean;
}

/** One row of the results as drawn: the tree flattened depth first, with its rails. */
export type CatalogSearchRow =
	| (SearchRowBase & Omit<CatalogSearchCategory, 'children'>)
	| (SearchRowBase & CatalogSearchCourse)
	| (SearchRowBase & CatalogSearchMore);

interface SearchRowBase {
	/** Its path from the top: unique in one answer, and the same for a row a longer query keeps. */
	id: string;
	/** Per ancestor depth: whether that ancestor has later siblings, so its rail runs through. */
	rails: boolean[];
	/** The last child of its parent: its elbow turns instead of running on. */
	last: boolean;
	/** The area the row lies in, for its accent. */
	area: string;
	/** Waypoints folded into the row, from the outermost: the path it hangs from. */
	trail: string[];
}
