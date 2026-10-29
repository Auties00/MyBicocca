<script lang="ts">
	import { page } from '$app/state';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import { m } from '#lib/paraglide/messages.js';
	import Avatar from '#lib/ui/Avatar.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import type { PageChrome } from '#lib/ui/pageChrome.svelte.ts';
	import Wordmark from '#lib/ui/Wordmark.svelte';
	import type { AccountModel } from './account.ts';
	import AccountMenu from './AccountMenu.svelte';
	import { shellHref } from './nav.ts';
	import { shell } from './shell.svelte.ts';

	interface Props {
		chrome: PageChrome;
		/** Phones: the bar is always there, with brand, search and account. */
		compact: boolean;
		account: AccountModel;
	}

	let { chrome, compact, account }: Props = $props();

	const me = $derived(account.accounts.find((a) => a.id === account.activeId));
	/* Profile and Settings open from the account menu: on phones its avatar says you are there. */
	const accountPage = $derived(
		[shellHref('profile'), shellHref('settings')].some(
			(href) => page.url.pathname === href || page.url.pathname.startsWith(`${href}/`)
		)
	);
	/* From 600 px up the bar only appears once the page title has scrolled under it. */
	const shown = $derived(compact || chrome.collapsed);
</script>

<header class={['topbar', { compact, collapsed: chrome.collapsed, shown }]} inert={!shown}>
	<!-- Back, the wordmark and the title share one cell, so Back arriving with the page moves nothing. -->
	<div class={['lead', { back: chrome.back }]}>
		{#if chrome.back}
			<!-- Snapshot: a header swap can clear chrome.back while this button tears down. -->
			{@const back = chrome.back}
			<IconButton icon={IconArrowLeft} label={back.label} href={back.href} />
		{/if}
		{#if compact && !chrome.back}
			<a
				class="brand"
				href={shellHref('calendar')}
				aria-label="MyBicocca"
				aria-hidden={chrome.collapsed}
			>
				<Wordmark />
			</a>
		{/if}
		{#if chrome.title}
			<span class="title" aria-hidden={!chrome.collapsed}>{chrome.title}</span>
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
				<AccountMenu model={account}>
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

	.collapsed::after {
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
		align-items: center;
		justify-items: start;
		min-inline-size: 0;
		min-block-size: var(--control-h);
		opacity: 0;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.lead > :global(*) {
		grid-area: 1 / 1;
	}

	.back .title {
		margin-inline-start: calc(var(--control-h) + 4px);
	}

	.shown .lead {
		opacity: 1;
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

	.collapsed .brand {
		opacity: 0;
		transform: translateY(-6px) scale(0.96);
		pointer-events: none;
	}

	/* Arriving from the sign-in pages, their wordmark flies into this one (authTransitions.ts). */
	@media (prefers-reduced-motion: no-preference) {
		:global(:root:active-view-transition-type(auth-enter)) .brand :global(.wordmark) {
			view-transition-name: wordmark;
		}
	}

	.title {
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
		transition-delay: 40ms;
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
