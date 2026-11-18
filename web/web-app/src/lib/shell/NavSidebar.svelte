<script lang="ts">
	import { page } from '$app/state';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import IconSidebar from '~icons/ph/sidebar-simple';
	import IconCaretUpDown from '~icons/ph/caret-up-down';
	import { m } from '#lib/paraglide/messages.js';
	import Avatar from '#lib/ui/Avatar.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Kbd from '#lib/ui/Kbd.svelte';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import {
		primaryDestinations,
		secondaryDestinations,
		type Destination
	} from '#lib/ui/destinations.ts';
	import Wordmark from '#lib/ui/Wordmark.svelte';
	import type { AccountModel } from './account.ts';
	import AccountMenu from './AccountMenu.svelte';
	import { activeDestination, shellHref } from './nav.ts';
	import { shell } from './shell.svelte.ts';

	interface Props {
		/** rail: icons with small labels (medium, or expanded and collapsed); sidebar: labelled list. */
		mode: 'rail' | 'sidebar';
		/** Whether the sidebar can be toggled here (expanded size class). */
		collapsible: boolean;
		account: AccountModel;
	}

	let { mode, collapsible, account }: Props = $props();

	const active = $derived(activeDestination(page.url.pathname));
	const primaryIndex = $derived(primaryDestinations.findIndex((d) => d.id === active));
	const secondaryIndex = $derived(secondaryDestinations.findIndex((d) => d.id === active));
	const me = $derived(account.accounts.find((a) => a.id === account.activeId));
	const career = $derived(me?.careers.find((c) => c.id === me.selectedCareerId));
</script>

{#snippet item(destination: Destination)}
	{@const current = destination.id === active}
	<li>
		<a
			class={['item', { current }]}
			href={shellHref(destination.path)}
			aria-current={current ? 'page' : undefined}
		>
			<span class="icon" aria-hidden="true">
				<destination.icon class="regular" />
				<destination.iconActive class="filled" />
			</span>
			<span class="label">{destination.label()}</span>
		</a>
	</li>
{/snippet}

<nav class={['side', mode]} aria-label={m.shell_nav_label()}>
	<div class="brand">
		<a class="home" href={shellHref('calendar')} aria-label="MyBicocca">
			<Wordmark variant={mode === 'rail' ? 'mark' : 'full'} />
		</a>
		{#if collapsible}
			<IconButton
				icon={IconSidebar}
				label={mode === 'rail' ? m.shell_expand_sidebar() : m.shell_collapse_sidebar()}
				shortcut={['mod', 'b']}
				size="sm"
				tooltipPlacement={mode === 'rail' ? 'right' : 'bottom'}
				onclick={() => shell.toggleSidebar()}
			/>
		{/if}
	</div>

	{#if mode === 'sidebar'}
		<button type="button" class="search" onclick={() => (shell.paletteOpen = true)}>
			<IconMagnifyingGlass aria-hidden="true" />
			<span class="search-text">{m.shell_search()}</span>
			<Kbd keys={['mod', 'k']} />
		</button>
	{:else}
		<div class="rail-search">
			<IconButton
				icon={IconMagnifyingGlass}
				label={m.shell_search()}
				shortcut={['mod', 'k']}
				tooltipPlacement="right"
				onclick={() => (shell.paletteOpen = true)}
			/>
		</div>
	{/if}

	<div class="group" style:--index={Math.max(0, primaryIndex)}>
		<span class={['indicator', { hidden: primaryIndex < 0 }]} aria-hidden="true"></span>
		<ul class="items" role="list">
			{#each primaryDestinations as destination (destination.id)}
				{@render item(destination)}
			{/each}
		</ul>
	</div>

	{#if mode === 'sidebar'}
		<div class="group secondary" style:--index={Math.max(0, secondaryIndex)}>
			<span class={['indicator', { hidden: secondaryIndex < 0 }]} aria-hidden="true"></span>
			<ul class="items" role="list">
				{#each secondaryDestinations as destination (destination.id)}
					{@render item(destination)}
				{/each}
			</ul>
		</div>
	{/if}

	<div class="foot">
		{#if me}
			<AccountMenu model={account} placement={mode === 'rail' ? 'right-end' : 'top-start'}>
				{#snippet trigger(props)}
					{#if mode === 'sidebar'}
						<button type="button" class="account" {...props}>
							<Avatar name={me.name} src={me.photo} size={36} />
							<span class="account-text">
								<span class="account-name">{me.name}</span>
								{#if career}<span class="account-career">{career.title}</span>{/if}
							</span>
							<IconCaretUpDown class="account-caret" aria-hidden="true" />
						</button>
					{:else}
						<button
							type="button"
							class="avatar-button"
							{...props}
							aria-label={m.shell_account_menu({ name: me.name })}
							{@attach tooltip(me.name, { placement: 'right' })}
						>
							<Avatar name={me.name} src={me.photo} size={40} />
						</button>
					{/if}
				{/snippet}
			</AccountMenu>
		{/if}
	</div>
</nav>

<style>
	.side {
		--item-h: 40px;
		--gap: 2px;
		position: sticky;
		inset-block-start: 0;
		display: flex;
		flex-direction: column;
		gap: 4px;
		block-size: 100dvh;
		padding: 12px 12px max(12px, var(--safe-bottom));
		overflow-y: auto;
		scrollbar-width: none;
		box-shadow: inset -1px 0 0 var(--hairline);
	}

	.sidebar {
		inline-size: var(--sidebar-w);
	}

	.rail {
		--item-h: 60px;
		--gap: 4px;
		inline-size: var(--rail-w);
		align-items: stretch;
		padding-inline: 8px;
	}

	.brand {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
		min-block-size: 44px;
		padding-inline: 8px 0;
		margin-block-end: 8px;
	}

	.rail .brand {
		flex-direction: column;
		justify-content: center;
		padding: 4px 0 0;
		gap: 10px;
	}

	.home {
		display: flex;
		align-items: center;
		min-block-size: 36px;
		padding: 4px;
		margin: -4px;
		border-radius: var(--r-sm);
		color: inherit;
		text-decoration: none;
	}

	/* Arriving from the sign-in pages, their wordmark flies into this one (authTransitions.ts). */
	@media (prefers-reduced-motion: no-preference) {
		:global(:root:active-view-transition-type(auth-enter)) .home :global(.wordmark) {
			view-transition-name: wordmark;
		}
	}

	.search {
		display: flex;
		align-items: center;
		gap: 10px;
		min-block-size: 38px;
		margin-block-end: 12px;
		padding: 0 8px 0 12px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text-muted);
		font: var(--type-ui);
		font-weight: 400;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	.search :global(svg) {
		inline-size: 17px;
		block-size: 17px;
	}

	.search-text {
		flex: 1;
		text-align: start;
	}

	@media (hover: hover) {
		.search:hover {
			box-shadow: inset 0 0 0 1px var(--hairline-strong);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.rail-search {
		display: grid;
		place-items: center;
		margin-block-end: 8px;
	}

	.group {
		--pad-top: 0px;
		position: relative;
	}

	.items {
		position: relative;
		display: grid;
		gap: var(--gap);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.secondary {
		--pad-top: 12px;
		margin-block-start: 12px;
		padding-block-start: var(--pad-top);
		border-block-start: 1px solid var(--hairline);
	}

	.indicator {
		position: absolute;
		inset-inline: 0;
		inset-block-start: var(--pad-top);
		block-size: var(--item-h);
		border-radius: var(--r-sm);
		background: var(--container);
		transform: translateY(calc(var(--index) * (var(--item-h) + var(--gap))));
		transition:
			transform var(--dur-spring) var(--spring-standard),
			opacity var(--dur-fast) var(--ease-out);
		pointer-events: none;
	}

	.rail .indicator {
		inset-inline: 50%;
		inline-size: 56px;
		block-size: 32px;
		margin-inline-start: -28px;
		margin-block-start: 6px;
		border-radius: var(--r-pill);
	}

	.hidden {
		opacity: 0;
	}

	.item {
		position: relative;
		display: flex;
		align-items: center;
		gap: 12px;
		block-size: var(--item-h);
		padding-inline: 10px;
		border-radius: var(--r-sm);
		color: var(--text-muted);
		font: var(--type-ui);
		text-decoration: none;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.item:not(.current):hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.item.current {
		color: var(--on-container);
		font-weight: 600;
	}

	.icon {
		display: grid;
		place-items: center;
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.icon :global(svg) {
		grid-area: 1 / 1;
		inline-size: var(--icon);
		block-size: var(--icon);
		transition: opacity 120ms var(--ease-out);
	}

	.icon :global(.filled),
	.current .icon :global(.regular) {
		opacity: 0;
	}

	.current .icon :global(.filled) {
		opacity: 1;
	}

	.item:active .icon {
		transform: scale(0.88);
		transition-duration: var(--dur-instant);
	}

	.rail .item {
		flex-direction: column;
		justify-content: flex-start;
		gap: 4px;
		padding: 6px 0 0;
		font-size: 0.75rem;
		font-weight: 500;
	}

	.rail .item:hover {
		background: none;
	}

	.rail .icon {
		inline-size: 56px;
		block-size: 32px;
		border-radius: var(--r-pill);
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.rail .item:not(.current):hover .icon {
			background: var(--state-hover);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.rail .current {
		color: var(--text);
		font-weight: 600;
	}

	.rail .current .icon {
		color: var(--on-container);
	}

	.rail .label {
		max-inline-size: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.foot {
		display: grid;
		margin-block-start: auto;
		padding-block-start: 12px;
	}

	.rail .foot {
		justify-items: center;
	}

	.account {
		display: flex;
		align-items: center;
		gap: 10px;
		inline-size: 100%;
		padding: 8px;
		border-radius: var(--r-md);
		text-align: start;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.account:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.account[aria-expanded='true'] {
		background: var(--state-press);
	}

	.account-text {
		flex: 1;
		display: grid;
		min-inline-size: 0;
	}

	.account-name {
		font: var(--type-ui-strong);
	}

	.account-career {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.account :global(.account-caret) {
		inline-size: 16px;
		block-size: 16px;
		color: var(--text-subtle);
	}

	.avatar-button {
		display: grid;
		place-items: center;
		inline-size: 48px;
		block-size: 48px;
		border-radius: 50%;
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.avatar-button:active {
		transform: scale(0.94);
		transition-duration: var(--dur-instant);
	}

	@media print {
		.side {
			display: none;
		}
	}
</style>
