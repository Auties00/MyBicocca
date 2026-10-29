<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Attachment } from 'svelte/attachments';
	import { getMapView } from '../view.svelte.ts';
	import { getSheetHead, hasSheetHead } from './sheetHead.ts';

	interface Props {
		/**
		 * The level's place: `directory`, or the id of the building or room it shows. Its scroll is
		 * given back when the user comes up to it again.
		 */
		key: string;
		/** Stays in view: on phones it is what the lowered sheet shows. */
		head: Snippet;
		children: Snippet;
		/** Names the level for page transitions; one level on screen at a time takes it. */
		transition?: boolean;
	}

	let { key, head, children, transition = true }: Props = $props();

	const view = getMapView();
	/* Only the level in the phone sheet measures its head, for the sheet's lowered height. */
	const measureHead = hasSheetHead() ? getSheetHead() : undefined;

	/*
	 * The offset is followed as it scrolls: by the time the level is torn down its element has
	 * left the page and reads 0. A new level starts at the top, so nothing is read (and no layout
	 * forced) as it mounts.
	 */
	const keepScroll: Attachment<HTMLElement> = (node) => {
		const restored = view.scrollToRestore(key);
		if (restored !== undefined) node.scrollTop = restored;
		let top = restored ?? 0;
		const track = () => (top = node.scrollTop);
		node.addEventListener('scroll', track, { passive: true });
		return () => {
			node.removeEventListener('scroll', track);
			view.rememberScroll(key, top);
		};
	};
</script>

<div class="level" data-vt-page={transition ? '' : undefined}>
	<div class="head" data-level-head {@attach measureHead}>
		{@render head()}
	</div>
	<div class={['body', { scrollable: view.scrollable }]} data-level-body {@attach keepScroll}>
		{@render children()}
	</div>
</div>

<style>
	.level {
		display: flex;
		flex-direction: column;
		min-block-size: 0;
		block-size: 100%;
		container: level / inline-size;
		timeline-scope: --level-body;
	}

	.head {
		position: relative;
		z-index: 1;
		flex: none;
		padding: var(--sp-2) var(--sp-4) var(--sp-3);
	}

	/* A hairline under the head once the body has scrolled under it. */
	.head::after {
		content: '';
		position: absolute;
		inset: auto 0 0;
		block-size: 1px;
		background: var(--hairline);
		opacity: 0;
		pointer-events: none;
	}

	@supports (animation-timeline: scroll()) {
		.head::after {
			animation: hairline linear both;
			animation-timeline: --level-body;
			animation-range: 0 16px;
		}
	}

	@keyframes hairline {
		to {
			opacity: 1;
		}
	}

	.body {
		flex: 1;
		min-block-size: 0;
		opacity: var(--level-body-opacity, 1);
		transition: opacity var(--dur-base) var(--ease-out);
		padding: 0 var(--sp-3) var(--level-end, var(--sp-4));
		overflow: hidden;
		overscroll-behavior: contain;
		scroll-timeline: --level-body block;
		scrollbar-gutter: stable;
		scrollbar-width: thin;
	}

	.scrollable {
		overflow-y: auto;
	}
</style>
