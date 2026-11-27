<script lang="ts">
	import { afterNavigate, onNavigate } from '$app/navigation';
	import { page } from '$app/state';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import { m } from '#lib/paraglide/messages.js';
	import Avatar from '#lib/ui/Avatar.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import type { PageChrome } from '#lib/ui/pageChrome.svelte.ts';
	import type { Href } from '#lib/ui/types.ts';
	import {
		allDestinations,
		primaryDestinations,
		type DestinationId
	} from '#lib/ui/destinations.ts';
	import Wordmark from '#lib/ui/Wordmark.svelte';
	import type { AccountModel } from './account.ts';
	import AccountMenu from './AccountMenu.svelte';
	import { activeDestination, shellHref } from './nav.ts';
	import { shell } from './shell.svelte.ts';

	interface Props {
		chrome: PageChrome;
		/** Phones: the bar is always there, with brand, search and account. */
		compact: boolean;
		account: AccountModel;
		destinationHref: (id: DestinationId) => string;
	}

	let { chrome, compact, account, destinationHref }: Props = $props();

	const me = $derived(account.accounts.find((a) => a.id === account.activeId));
	const accountHrefs: readonly string[] = [shellHref('profile'), shellHref('settings')];
	const path = $derived(page.url.pathname.replace(/\/$/, ''));
	/* Profile and Settings open from the account menu: on phones its avatar says you are there. */
	const accountPage = $derived(
		accountHrefs.some((href) => path === href || path.startsWith(`${href}/`))
	);
	/* A destination's own page is already named by the navigation: its title never takes the bar. */
	const destinationPage = $derived(allDestinations.some((d) => path === shellHref(d.path)));
	/*
	 * On phones Profile and Settings open from the account menu, over the tab they were opened
	 * from: they are nested pages, named in the bar, and Back returns to that tab.
	 */
	let openedFrom = $state<DestinationId>('calendar');

	/*
	 * The bar's fades follow scrolling within a page. Across a navigation it simply takes the new
	 * page's state, until that page has painted, rather than fading out what the old one showed.
	 */
	let switching = $state(false);
	onNavigate(() => {
		switching = true;
	});
	afterNavigate(({ from }) => {
		const tab = from?.url && activeDestination(from.url.pathname);
		if (tab && primaryDestinations.some((d) => d.id === tab)) openedFrom = tab;
		requestAnimationFrame(() => requestAnimationFrame(() => (switching = false)));
	});
	const accountRoot = $derived(compact && accountHrefs.includes(path));
	/*
	 * Between a navigation and the new page's header reporting, the state still describes the page
	 * left behind: it is dropped rather than shown for a frame or two under the new URL.
	 */
	const current = $derived(
		chrome.header.path === page.url.pathname ? chrome.header : { collapsed: false }
	);
	const header = $derived(
		accountRoot
			? {
					...current,
					back: { href: destinationHref(openedFrom) as Href, label: m.ui_back() }
				}
			: destinationPage
				? { ...current, title: undefined }
				: current
	);
	/* From 600 px up the bar only appears once a subpage's title has scrolled under it. */
	const shown = $derived(compact || (header.collapsed && !destinationPage));
	/* On phones the wordmark gives way to the title; with no title it stays. */
	const brandAway = $derived(header.collapsed && !!header.title);
</script>

<header
	class={[
		'topbar',
		{
			compact,
			collapsed: header.collapsed && shown,
			shown,
			'brand-away': brandAway,
			switching
		}
	]}
	inert={!shown}
>
	<!-- Back, the wordmark and the title share one cell, so Back arriving with the page moves nothing. -->
	<div class={['lead', { back: header.back }]}>
		{#if header.back}
			{@const back = header.back}
			<div class="back-control">
				<IconButton icon={IconArrowLeft} label={back.label} href={back.href} />
			</div>
		{/if}
		{#if compact && !header.back}
			<a
				class={['brand', { restoring: chrome.restoringScroll }]}
				href={shellHref('calendar')}
				aria-label="MyBicocca"
				inert={brandAway || chrome.restoringScroll}
			>
				<Wordmark />
			</a>
		{/if}
		{#if header.title}
			{#key header.title}
				<span class="title" aria-hidden={!header.collapsed}>{header.title}</span>
			{/key}
		{/if}
	</div>
	{#if compact}
		<div class="trail">
			<IconButton
				icon={IconMagnifyingGlass}
				label={m.shell_search()}
				onclick={() => (shell.paletteOpen = true)}
			/>
			{#if me}
				<AccountMenu model={account} destinations>
					{#snippet trigger(props)}
						<button
							type="button"
							class={['avatar', { current: accountPage }]}
							{...props}
							aria-label={m.shell_account_menu({ name: me.name })}
						>
							<Avatar name={me.name} src={me.photo} size={34} />
						</button>
					{/snippet}
				</AccountMenu>
			{/if}
		</div>
	{/if}
</header>

<style>
	.topbar {
		position: sticky;
		inset-block-start: 0;
		z-index: var(--z-topbar);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
		block-size: calc(var(--topbar-h) + var(--safe-top));
		padding: var(--safe-top) 8px 0;
		margin-block-end: calc(-1 * (var(--topbar-h) + var(--safe-top)));
		pointer-events: none;
		transition: background-color var(--dur-base) var(--ease-out);
	}

	.topbar::after {
		content: '';
		position: absolute;
		inset: auto 0 0;
		block-size: 1px;
		background: var(--hairline);
		opacity: 0;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.shown {
		background: var(--veil);
		-webkit-backdrop-filter: var(--veil-filter);
		backdrop-filter: var(--veil-filter);
		pointer-events: auto;
	}

	/* On phones the bar is always there, so a page that asks for it is solid whatever the scroll. */
	:global(.shell:has([data-solid-topbar])) .collapsed,
	:global(.shell:has([data-solid-topbar])) .compact {
		background: var(--bg);
		-webkit-backdrop-filter: none;
		backdrop-filter: none;
	}

	/* On phones the bar is always there and reads as one surface with the page: no divider. */
	.collapsed:not(.compact)::after {
		opacity: 1;
	}

	.compact {
		margin-block-end: 0;
		padding-inline: 12px 8px;
	}

	/* Over a page that fills the viewport (the map), the bar floats like the tab bar. */
	:global(.shell:has([data-page-fill='viewport'])) .compact {
		margin-block-end: calc(-1 * (var(--topbar-h) + var(--safe-top)));
	}

	.lead {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		align-items: center;
		justify-items: start;
		min-inline-size: 0;
		min-block-size: var(--control-h);
		opacity: 0;
		transform: translateY(4px);
		transition:
			opacity var(--dur-fast) var(--ease-out),
			transform var(--dur-base) var(--ease-out);
	}

	.lead > :global(*) {
		grid-area: 1 / 1;
	}

	.back .title {
		margin-inline-start: calc(var(--control-h) + 4px);
	}

	.shown .lead {
		opacity: 1;
		transform: none;
	}

	.back-control {
		display: grid;
	}

	.brand {
		display: flex;
		padding: 6px;
		margin: -6px;
		border-radius: var(--r-sm);
		color: inherit;
		text-decoration: none;
		transition:
			opacity var(--dur-fast) var(--ease-out),
			transform var(--dur-base) var(--ease-out);
	}

	.brand-away .brand {
		opacity: 0;
		transform: translateY(-6px) scale(0.96);
		pointer-events: none;
	}

	.brand.restoring {
		visibility: hidden;
		transition: none;
	}

	/* Arriving from the sign-in pages, their wordmark flies into this one (authTransitions.ts). */
	@media (prefers-reduced-motion: no-preference) {
		:global(:root:active-view-transition-type(auth-enter)) .brand :global(.wordmark) {
			view-transition-name: wordmark;
		}
	}

	.title {
		justify-self: stretch;
		overflow: hidden;
		padding-inline: 4px;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-overflow: ellipsis;
		white-space: nowrap;
		opacity: 0;
		transform: translateY(6px);
		transition:
			opacity var(--dur-fast) var(--ease-out),
			transform var(--dur-base) var(--ease-out);
	}

	.collapsed .title {
		opacity: 1;
		transform: none;
	}

	/* On wide screens the bar enters together; on phones only its leading controls change. */
	.topbar:not(.compact) .title {
		opacity: 1;
		transform: none;
	}

	.switching,
	.switching::after,
	.switching .lead,
	.switching .brand,
	.switching .title {
		transition: none;
	}

	@media (prefers-reduced-motion: reduce) {
		.topbar,
		.topbar::after,
		.lead,
		.brand,
		.title {
			transition: none;
		}

		.lead,
		.brand-away .brand,
		.title {
			transform: none;
		}
	}

	.trail {
		display: flex;
		align-items: center;
		gap: 2px;
	}

	.avatar {
		display: grid;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
		border-radius: 50%;
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.avatar.current :global(.avatar) {
		box-shadow:
			0 0 0 2px var(--bg),
			0 0 0 4px var(--accent);
	}

	.avatar:active {
		transform: scale(0.92);
		transition-duration: var(--dur-instant);
	}

	@media print {
		.topbar {
			display: none;
		}
	}
</style>
