import { createContext } from 'svelte';
import type { Attachment } from 'svelte/attachments';

/**
 * Set by the phone sheet for the level inside it: attached to the level's head, it reports the
 * head's height, which the lowered sheet keeps in view.
 */
export const [getSheetHead, setSheetHead, hasSheetHead] = createContext<Attachment<HTMLElement>>();
