import { acceptsFile } from './filetypes.ts';
import type { FileRules } from './model.ts';
import type { DraftFile } from './writes.ts';

export function draftName(file: DraftFile): string {
	return file.file.name;
}

export function draftSize(file: DraftFile): number | null {
	return file.file.size;
}

/** Why the editor won't take a file the student picked. */
export type FileProblem =
	{ kind: 'too-large'; limit: number } | { kind: 'empty' } | { kind: 'type' };

/**
 * Checks a picked file against the assignment's rules before any upload, as Moodle would after
 * it. Files already handed in passed Moodle's checks once and aren't judged again.
 */
export function problemOf(file: DraftFile, rules: FileRules): FileProblem | null {
	if (file.kind === 'kept') return null;
	if (file.file.size === 0) return { kind: 'empty' };
	if (rules.maxBytes !== null && file.file.size > rules.maxBytes) {
		return { kind: 'too-large', limit: rules.maxBytes };
	}
	if (!acceptsFile(rules.types, file.file.name, file.file.type)) return { kind: 'type' };
	return null;
}

export interface Added {
	files: DraftFile[];
	/** Names of listed files a pick replaced: Moodle keeps one file per name. */
	replaced: string[];
	/** Picks the rules refuse, with why: they take no place in the list. */
	rejected: { name: string; problem: FileProblem }[];
	/** Names of picks over the assignment's file count. */
	dropped: string[];
}

/**
 * Adds picked files to the list once they pass the rules. One with the name of a listed file
 * replaces it (a new version); where a single file is allowed, a new pick takes its place; the
 * rest are added while the count allows.
 */
export function addPicked(
	current: readonly DraftFile[],
	picked: readonly File[],
	rules: FileRules,
	key: (file: File) => string
): Added {
	const files = [...current];
	const added = new Set<string>();
	const result: Added = { files, replaced: [], rejected: [], dropped: [] };
	for (const file of picked) {
		const entry: DraftFile = { key: key(file), kind: 'picked', file };
		const problem = problemOf(entry, rules);
		if (problem !== null) {
			result.rejected.push({ name: file.name, problem });
			continue;
		}
		const same = files.findIndex((existing) => draftName(existing) === file.name);
		const swap =
			same >= 0 ? same : rules.maxFiles === 1 && files.length === 1 && added.size === 0 ? 0 : -1;
		if (swap >= 0) {
			const name = draftName(files[swap]);
			if (!result.replaced.includes(name)) result.replaced.push(name);
			files[swap] = entry;
		} else if (files.length < rules.maxFiles) {
			files.push(entry);
		} else {
			result.dropped.push(file.name);
			continue;
		}
		added.add(entry.key);
	}
	return result;
}
