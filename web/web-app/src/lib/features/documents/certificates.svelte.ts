import { createContext } from 'svelte';
import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { toCertificates, type Certificate } from './certificates.ts';
import { certificatesQuery } from './queries.ts';

/** The search parameter naming the certificate previewed beside the list. */
export const OPEN_PARAM = 'open';

/** The self-certifications on offer, shared by the list and the preview beside it. */
export class Certificates {
	readonly query = query(() => session.current && certificatesQuery(session.current));
	readonly list: Certificate[] = $derived(toCertificates(this.query.data ?? []));
	readonly hrefs: ResolvedPathname[] = $derived(this.list.map((item) => certificateHref(item.key)));

	find(key: string): Certificate | undefined {
		return this.list.find((item) => item.key === key);
	}
}

export const [useCertificates, provideCertificates] = createContext<Certificates>();

/** The certificates page with `key` previewed beside the list. */
export function certificateHref(key: string): ResolvedPathname {
	return resolve(`/(app)/services/certificates?${OPEN_PARAM}=${encodeURIComponent(key)}`);
}
