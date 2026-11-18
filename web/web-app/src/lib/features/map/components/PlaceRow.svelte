<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import ListRow from '#lib/ui/ListRow.svelte';

	interface Props {
		/** The place's id (`building:U01`): going back up a level focuses its row. */
		place: string;
		href: ResolvedPathname;
		title: string;
		description?: string;
		meta?: string;
		leading: Snippet;
		/** Lit because its pin is pointed at on the map. */
		highlighted?: boolean;
		/** The building open beside the list. */
		selected?: boolean;
		/** The pointer or keyboard focus entered (true) or left (false) the row. */
		onhover?: (inside: boolean) => void;
	}

	let {
		place,
		href,
		title,
		description,
		meta,
		leading,
		highlighted = false,
		selected = false,
		onhover
	}: Props = $props();
</script>

<ListRow
	{href}
	{title}
	{description}
	{meta}
	{leading}
	{highlighted}
	{selected}
	titleLines={1}
	lines={1}
	data-place={place}
	onpointerenter={(event) => event.pointerType === 'mouse' && onhover?.(true)}
	onpointerleave={(event) => event.pointerType === 'mouse' && onhover?.(false)}
	onfocus={(event) => event.currentTarget.matches(':focus-visible') && onhover?.(true)}
	onblur={() => onhover?.(false)}
/>
