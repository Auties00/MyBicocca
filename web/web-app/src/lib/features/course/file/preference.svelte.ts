import type { ViewableKind } from '../files.ts';

/** How a kind of file opens without asking: in the viewer here, or saved to the device. */
export type OpenChoice = 'view' | 'download';

type Choices = Partial<Record<ViewableKind, OpenChoice>>;

const KEY = 'mybicocca:file-open';
const KINDS: readonly ViewableKind[] = ['pdf', 'image', 'video', 'audio', 'html', 'text', 'zip'];

function read(): Choices {
	try {
		const stored: unknown = JSON.parse(localStorage.getItem(KEY) ?? '{}');
		if (typeof stored !== 'object' || stored === null) return {};
		const choices: Choices = {};
		for (const kind of KINDS) {
			const value = (stored as Record<string, unknown>)[kind];
			if (value === 'view' || value === 'download') choices[kind] = value;
		}
		return choices;
	} catch {
		return {};
	}
}

/**
 * The remembered way to open each kind of file, per device as on Android. Settings › Files lists
 * and resets the same choices; a change in another tab arrives here too.
 */
class FileOpenPreferences {
	choices = $state.raw<Choices>(typeof localStorage === 'undefined' ? {} : read());

	constructor() {
		if (typeof window === 'undefined') return;
		window.addEventListener('storage', (event) => {
			if (event.key === KEY || event.key === null) this.choices = read();
		});
	}

	get(kind: ViewableKind): OpenChoice | undefined {
		return this.choices[kind];
	}

	/** Remembers `choice` for `kind`, or forgets it with `undefined` ("ask every time"). */
	set(kind: ViewableKind, choice: OpenChoice | undefined): void {
		const next = { ...this.choices };
		if (choice === undefined) delete next[kind];
		else next[kind] = choice;
		this.choices = next;
		try {
			localStorage.setItem(KEY, JSON.stringify(next));
		} catch {
			// Private windows may refuse storage: the choice still holds for this visit.
		}
	}
}

export const fileOpenPreferences = new FileOpenPreferences();
