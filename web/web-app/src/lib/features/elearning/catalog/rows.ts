import type { CatalogSearchNode, CatalogSearchResult, CatalogSearchRow } from './model.ts';

/** Past this many rows the list stops, whatever the hit budget let through. */
export const MAX_SEARCH_ROWS = 160;

export interface SearchRows {
	rows: CatalogSearchRow[];
	/** Hits among the rows: courses (editions of a teaching once) and matching categories. */
	hits: number;
	/** Rows were left out past {@link MAX_SEARCH_ROWS}. */
	capped: boolean;
}

interface Placed {
	node: CatalogSearchNode;
	trail: string[];
	/** The row's path from the top, folded waypoints included: its id. */
	path: string;
}

/** A node's step in a row's path; a cross-listing can repeat a key under one parent, not a name. */
function step(node: CatalogSearchNode): string {
	switch (node.kind) {
		case 'category':
			return `c${node.key}:${node.name}`;
		case 'course':
			return `x${node.course.id}`;
		case 'more':
			return `m${node.key}`;
	}
}

/**
 * The result tree as rows, depth first, each with the rails of its ancestors. A waypoint (a
 * category shown only because hits lie below it) holding a single category folds into it, its name
 * joining that row's trail: "Insegnamenti › A.A. 2026-2027 › 2° anno" is one row, not three. From
 * `foldFrom` levels down every waypoint folds, so on a phone a hit never sits more than a few
 * indents in. The top level never folds: it says which area or branch a hit is in.
 *
 * A row's id is its path, so a row that a longer query keeps is the same row.
 */
export function searchRows(result: CatalogSearchResult, foldFrom = Infinity): SearchRows {
	const rows: CatalogSearchRow[] = [];
	let hits = 0;
	let capped = false;

	const place = (
		nodes: readonly CatalogSearchNode[],
		depth: number,
		trail: string[],
		path: string
	): Placed[] =>
		nodes.flatMap((node) =>
			node.kind === 'category' &&
			!node.match &&
			depth > 0 &&
			(depth >= foldFrom || (node.children.length === 1 && node.children[0].kind === 'category'))
				? place(node.children, depth, [...trail, node.name], `${path}/${step(node)}`)
				: [{ node, trail, path: `${path}/${step(node)}` }]
		);

	const emit = (
		nodes: readonly CatalogSearchNode[],
		depth: number,
		rails: boolean[],
		area: string | null,
		parent: string
	) => {
		const placed = place(nodes, depth, [], parent);
		placed.forEach(({ node, trail, path }, i) => {
			if (rows.length >= MAX_SEARCH_ROWS) {
				capped = true;
				return;
			}
			const last = i === placed.length - 1;
			// Only the root's top level, the areas, lies in no area: each is its own.
			const base = { id: path, rails, last, trail, area: area ?? '' };
			switch (node.kind) {
				case 'category': {
					const { children, ...category } = node;
					const rowArea = area ?? category.key;
					rows.push({ ...base, ...category, area: rowArea });
					if (category.match) hits++;
					emit(children, depth + 1, [...rails, !last], rowArea, path);
					break;
				}
				case 'course':
					rows.push({ ...base, ...node });
					hits++;
					break;
				case 'more':
					rows.push({ ...base, ...node });
					break;
			}
		});
	};

	emit(result.tree, 0, [], result.area, '');
	return { rows, hits, capped };
}
