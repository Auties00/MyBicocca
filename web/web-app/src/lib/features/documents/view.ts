import { createContext } from 'svelte';

/** How a documents page lays out its levels, for the panes inside it to fit in. */
export interface DocumentsView {
	/** List and detail side by side; otherwise each level has the screen to itself. */
	readonly wide: boolean;
}

export const [useDocumentsView, provideDocumentsView] = createContext<DocumentsView>();
