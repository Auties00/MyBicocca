<script lang="ts" module>
	import { createContext } from 'svelte';
	import type { ResolvedPathname } from '$app/types';

	/** What a detail's header offers beside its title: stepping through the list, closing. */
	export interface DetailControls {
		/** Whether the detail sits beside the list. */
		readonly twoPanes: boolean;
		/** The neighbours of the open item, where the list has an order (`items`). */
		readonly steps: { previous?: ResolvedPathname; next?: ResolvedPathname } | undefined;
		readonly closeHref: ResolvedPathname | undefined;
		/** Opens a neighbour in place of the open item; focus stays on the control that asked. */
		step(href: ResolvedPathname, control: HTMLElement): void;
		/** Closes the open item as Escape does, back to the list without a new history entry. */
		close(): void;
	}

	export const [getDetailControls, setDetailControls, hasDetailControls] =
		createContext<DetailControls>();
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import { afterNavigate, goto } from '$app/navigation';
	import { m } from '#lib/paraglide/messages.js';
	import KeyHints, { type KeyHint } from './KeyHints.svelte';
	import { isTypingTarget } from './platform.ts';
	import { expanded } from './media.ts';
	import { registerShortcuts } from './shortcuts.svelte.ts';

	interface Props {
		/** The list (master) pane. */
		list: Snippet;
		/** The open item. Its presence is what "a detail is open" means: pass it only when a detail route or selection exists. */
		detail?: Snippet;
		/** Two-pane layout with nothing selected (e.g. an overview). Not shown on one pane. */
		placeholder?: Snippet;
		/** Width of the list pane on two panes, in px. */
		listWidth?: number;
		/**
		 * The narrowest the detail may be beside the list, in px. The panes split once the
		 * component's own width holds the list, the gap and this, from the expanded size class up.
		 */
		detailMin?: number;
		/** List and detail stay together, centred, the detail at reading width (settings). */
		centered?: boolean;
		/** Accessible names of the two regions. */
		listLabel: string;
		detailLabel?: string;
		/**
		 * Whether list and detail are side by side, for pages that render differently beside the
		 * list (`bind:twoPanes`). Undefined until the first layout, which comes before any paint.
		 */
		twoPanes?: boolean;
		/**
		 * The open item's link, as the list's rows link it. With it, on two panes, focus moves to
		 * the detail's heading (or its `[data-detail-focus]` element) when an item opens; on any
		 * layout it goes back to the item's row when the item closes.
		 */
		current?: ResolvedPathname;
		/**
		 * The items' links in list order: J and K open the next and the previous one, as a hint
		 * under the placeholder says, and so do the arrows of the detail's DetailHeader.
		 */
		items?: readonly ResolvedPathname[];
		/** The page's own shortcuts, in that hint after J and K. */
		keyHints?: readonly KeyHint[];
		/**
		 * The list with nothing open: Escape, and the close button of the detail's DetailHeader,
		 * close the detail there without a new history entry.
		 */
		closeHref?: ResolvedPathname;
	}

	let {
		list,
		detail,
		placeholder,
		listWidth = 360,
		detailMin = 480,
		centered = false,
		listLabel,
		detailLabel,
		twoPanes = $bindable(),
		current,
		items,
		keyHints,
		closeHref
	}: Props = $props();

	/* Below the expanded size class every screen shows one level, whatever room is left. */

	let listPane = $state<HTMLElement>();
	let detailPane = $state<HTMLElement>();

	/*
	 * CSS decides the layout (see the styles); this only reports it. A hidden pane has no box, so
	 * two panes means both have one. Resize observations arrive after layout and before paint.
	 */
	$effect(() => {
		const list = listPane;
		const detail = detailPane;
		if (!list || !detail) return;
		let listShown = false;
		let detailShown = false;
		const observer = new ResizeObserver((entries) => {
			for (const entry of entries) {
				if (entry.target === list) listShown = entry.contentRect.width > 0;
				else detailShown = entry.contentRect.width > 0;
			}
			twoPanes = listShown && detailShown;
		});
		observer.observe(list);
		observer.observe(detail);
		return () => observer.disconnect();
	});

	/* An overview may load data of its own: mount it only where it can show. */
	const showPlaceholder = $derived(twoPanes ?? expanded.current);

	/*
	 * Side by side now, read from the panes' styles: the observer reports a navigation's layout
	 * only after it, and a page whose panes split as an item opens has changed it.
	 */
	function split(): boolean {
		return !!listPane?.checkVisibility() && !!detailPane?.checkVisibility();
	}

	/** The visible link to an item in the list (a list may draw an item twice, for two layouts). */
	function rowLink(href: ResolvedPathname): HTMLElement | undefined {
		if (!listPane) return undefined;
		return [...listPane.querySelectorAll<HTMLElement>(`a[href="${CSS.escape(href)}"]`)].find(
			(link) => link.checkVisibility()
		);
	}

	function detailTarget(): HTMLElement | undefined {
		const target =
			detailPane?.querySelector<HTMLElement>('[data-detail-focus]') ??
			detailPane?.querySelector<HTMLElement>('h1, h2, h3');
		if (target && target.tabIndex < 0 && !target.hasAttribute('tabindex')) target.tabIndex = -1;
		return target ?? undefined;
	}

	/* Undefined at first: mounted by a navigation with an item open, the item takes the focus. */
	let previous: ResolvedPathname | undefined;
	let stepping = false;
	/* The header's step button that opened the current item, which keeps the focus. */
	let stepper: HTMLElement | undefined;
	/* Whether the entry before this one in history is the list, so Escape can go back to it. */
	let openedFromList = false;

	const index = $derived(current === undefined || !items ? -1 : items.indexOf(current));
	/*
	 * Whether J and K lead anywhere: from an open item to another, or on two panes to the first.
	 * On one pane with nothing open the list is the page, and Tab walks it.
	 */
	const steppable = $derived(
		!!items?.length && (current === undefined ? twoPanes === true : items.length > 1 || index < 0)
	);

	setDetailControls({
		get twoPanes() {
			return twoPanes === true;
		},
		/* As J and K: from an item the list doesn't show (filtered out), the last or the first. */
		get steps() {
			if (!steppable || current === undefined || !items) return undefined;
			if (index < 0) return { previous: items.at(-1), next: items[0] };
			return { previous: items[index - 1], next: items[index + 1] };
		},
		get closeHref() {
			return closeHref;
		},
		step(href, control) {
			stepper = control;
			void goto(href, { replace: true });
		},
		close
	});

	registerShortcuts(() => [
		...(steppable ? [{ keys: [['j'], ['k']], label: m.ui_keys_step() }] : []),
		...(closeHref && detail ? [{ keys: [['esc']], label: m.ui_keys_close() }] : [])
	]);

	function close() {
		if (!closeHref) return;
		if (openedFromList) history.back();
		else void goto(closeHref, { replace: true });
	}

	afterNavigate((navigation) => {
		const from = previous;
		const step = stepping;
		const control = stepper;
		previous = current;
		stepping = false;
		stepper = undefined;
		/* Stepping replaces the open item: the entry before it is still what it was. */
		if (!step && !control) {
			const source = navigation.from?.url;
			openedFromList =
				navigation.type !== 'popstate' &&
				current !== undefined &&
				closeHref !== undefined &&
				source != null &&
				source.pathname + source.search === closeHref;
		}
		/*
		 * A detail drawn anew for the next item has the same control in its new header; at the end
		 * of the list it is disabled, and the other direction takes the focus.
		 */
		if (control) {
			const same = control.isConnected
				? control
				: detailPane?.querySelector<HTMLElement>(
						`[data-detail-step="${control.dataset.detailStep}"]`
					);
			const stepTo = same?.matches(':disabled')
				? detailPane?.querySelector<HTMLElement>('[data-detail-step]:not(:disabled)')
				: same;
			if (stepTo) {
				stepTo.focus({ preventScroll: true });
				return;
			}
		}
		if (navigation.type === 'enter' || from === current) return;
		/*
		 * Stepping keeps focus in the list; opening moves it to the item beside the list (alone, the
		 * item is the page, and the shell focuses its title); closing gives it back to the item's row.
		 * Moved here, synchronously, the shell leaves it where it is (see pageTransitions).
		 */
		const row = step ? current : current === undefined ? from : undefined;
		if (row === undefined && !split()) return;
		const target = row ? rowLink(row) : detailTarget();
		target?.focus({ preventScroll: true });
		if (row) target?.scrollIntoView({ block: 'nearest' });
	});

	/** J and K step through the items, Escape closes the open one, as in mail clients. */
	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey) return;
		if (isTypingTarget(event.target)) return;
		if (event.target instanceof Element && event.target.closest('dialog, [popover]')) return;
		if (event.key === 'Escape') {
			if (!detail || !closeHref) return;
			event.preventDefault();
			close();
			return;
		}
		if ((event.key !== 'j' && event.key !== 'k') || !steppable || !items) return;
		event.preventDefault();
		const delta = event.key === 'j' ? 1 : -1;
		const next =
			index < 0
				? delta > 0
					? 0
					: items.length - 1
				: Math.min(items.length - 1, Math.max(0, index + delta));
		if (items[next] === current) return;
		stepping = twoPanes === true;
		void goto(items[next], { replace: current !== undefined });
	}
</script>

<svelte:window onkeydown={items || closeHref ? keydown : undefined} />

<div
	class={['list-detail', { centered }]}
	style:--list="{listWidth}px"
	style:--detail-min="{detailMin}px"
>
	<div class="frame">
		<div class={['split', { open: !!detail }]}>
			<section class="list" aria-label={listLabel} bind:this={listPane}>{@render list()}</section>
			<!-- Beside the list, page transitions animate only this pane (see AppShell). -->
			<section
				class="detail"
				aria-label={detail ? detailLabel : undefined}
				data-vt-page={twoPanes ? '' : undefined}
				bind:this={detailPane}
			>
				{#if detail}
					{@render detail()}
				{:else if placeholder && showPlaceholder}
					{@render placeholder()}
					{#if items?.length}<div class="hints"><KeyHints more={keyHints} /></div>{/if}
				{/if}
			</section>
		</div>
	</div>
</div>

<style>
	/*
	 * One tree for both layouts, so crossing the threshold never remounts a pane: scroll, focus
	 * and unsent forms survive a resize. The threshold is the component's own width, through a
	 * container query whose condition can't read custom properties: the frame sets its font size
	 * to the threshold, `1em` in the condition is the query container's font size, and the panes
	 * get the inherited size back.
	 */
	.list-detail {
		--inherited-font-size: 1em;
		--list-pane: var(--list);
		--gap: var(--sp-7);
	}

	@media (width >= 1440px) {
		.list-detail {
			--list-pane: calc(var(--list) + 40px);
			--gap: var(--sp-8);
		}
	}

	.frame {
		container: list-detail / inline-size;
		font-size: calc(var(--list-pane) + var(--gap) + var(--detail-min));
	}

	.split {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		font-size: var(--inherited-font-size);
	}

	.list,
	.detail {
		min-inline-size: 0;
	}

	.list {
		container: list / inline-size;
	}

	.detail {
		container: detail / inline-size;
	}

	.hints {
		margin-block-start: var(--sp-6);
	}

	/* One pane: an open item replaces the list; with none open, the list is all there is. */
	.split.open > .list,
	.split:not(.open) > .detail {
		display: none;
	}

	@media (width >= 1024px) {
		@container list-detail (width >= 1em) {
			.split {
				grid-template-columns: var(--list-pane) minmax(0, 1fr);
				align-items: start;
				gap: var(--gap);
			}

			.split.open > .list,
			.split:not(.open) > .detail {
				display: block;
			}

			.list {
				position: sticky;
				inset-block-start: calc(var(--topbar-h) + var(--safe-top));
				max-block-size: calc(100dvh - var(--topbar-h) - var(--safe-top));
				overflow: auto;
				overscroll-behavior: contain;
				scrollbar-width: thin;
				scrollbar-gutter: stable;
				padding-block-end: var(--sp-6);
			}

			.centered .split {
				grid-template-columns: var(--list-pane) minmax(0, var(--w-reading));
				justify-content: center;
			}
		}
	}

	/* On paper one level, whatever the sheet's width: the open item, else the list. */
	@media print {
		.split,
		.centered .split {
			grid-template-columns: minmax(0, 1fr);
		}

		.split.open > .list {
			display: none;
		}

		.list {
			position: static;
			max-block-size: none;
			overflow: visible;
		}
	}
</style>
