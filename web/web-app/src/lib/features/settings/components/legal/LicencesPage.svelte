<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import Badge from '#lib/ui/Badge.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import SearchField from '#lib/ui/SearchField.svelte';
	import data from '../../legal/licences.json';
	import SectionPage from '../SectionPage.svelte';

	interface Licence {
		name: string;
		version: string;
		licence: string;
		url?: string;
		text?: string;
	}

	const app: Licence = data.app;
	const packages: readonly Licence[] = data.packages;

	let search = $state('');

	const shown = $derived.by(() => {
		const needle = search.trim().toLowerCase();
		if (needle === '') return packages;
		return packages.filter(
			(item) =>
				item.name.toLowerCase().includes(needle) || item.licence.toLowerCase().includes(needle)
		);
	});

	/** How many packages use each licence, most used first. */
	const kinds = [
		...packages.reduce(
			(count, item) => count.set(item.licence, (count.get(item.licence) ?? 0) + 1),
			new Map<string, number>()
		)
	].sort((a, b) => b[1] - a[1]);
</script>

{#snippet licence(item: Licence)}
	<details class="item">
		<summary class="summary">
			<span class="titles">
				<h3 class="name" translate="no">{item.name}</h3>
				<span class="version" translate="no">{item.version}</span>
			</span>
			<Badge>{item.licence}</Badge>
			<span class="caret"><IconCaretDown aria-hidden="true" /></span>
		</summary>
		<div class="body">
			{#if item.text}
				<pre class="text" translate="no">{item.text}</pre>
			{:else}
				<p class="missing">{m.settings_licences_no_text({ licence: item.licence })}</p>
			{/if}
			{#if item.url}
				<a class="source" href={item.url} rel="external noopener noreferrer" target="_blank">
					{m.settings_licences_source()}
					<IconArrowSquareOut aria-hidden="true" />
				</a>
			{/if}
		</div>
	</details>
{/snippet}

<SectionPage title={m.settings_licences_title()} subtitle={m.settings_licences_page_subtitle()}>
	<section class="own" aria-labelledby="licences-own">
		<h2 class="own-title" id="licences-own">{m.settings_licences_own_title()}</h2>
		<p class="own-body">{m.settings_licences_own_body()}</p>
		<div class="list">{@render licence(app)}</div>
	</section>

	<section class="packages" aria-labelledby="licences-packages">
		<div class="head">
			<h2 class="heading" id="licences-packages">
				{m.settings_licences_packages({ count: packages.length })}
			</h2>
			<p class="kinds">
				{#each kinds as [kind, count] (kind)}
					<span class="kind"><span translate="no">{kind}</span> {count}</span>
				{/each}
			</p>
			<SearchField bind:value={search} label={m.settings_licences_search()} />
		</div>

		{#if shown.length > 0}
			<div class="list">
				{#each shown as item (item.name)}
					{@render licence(item)}
				{/each}
			</div>
		{:else}
			<EmptyState
				icon={IconMagnifyingGlass}
				title={m.settings_licences_none({ query: search.trim() })}
			/>
		{/if}
	</section>
</SectionPage>

<style>
	.own {
		display: grid;
		gap: 8px;
		padding: 20px;
		border-radius: var(--r-xl);
		background: var(--container);
		color: var(--on-container);
	}

	.own-title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.own-body {
		max-inline-size: 60ch;
		text-wrap: pretty;
	}

	.own .list {
		margin-block-start: 8px;
	}

	.packages {
		display: grid;
		gap: var(--sp-4);
	}

	.head {
		display: grid;
		gap: 8px;
	}

	.heading {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.kinds {
		display: flex;
		flex-wrap: wrap;
		gap: 4px 14px;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		font-variant-numeric: tabular-nums;
	}

	.kind span {
		color: var(--text);
		font-weight: 550;
	}

	.list {
		display: grid;
		gap: 2px;
	}

	.item {
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
		content-visibility: auto;
		contain-intrinsic-size: auto 56px;
	}

	.item:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.item:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.summary {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 56px;
		padding: 8px 16px;
		border-radius: inherit;
		list-style: none;
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.summary::-webkit-details-marker {
		display: none;
	}

	.summary:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		.summary:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.titles {
		display: flex;
		flex: 1;
		flex-wrap: wrap;
		align-items: baseline;
		gap: 0 8px;
		min-inline-size: 0;
	}

	.name {
		overflow-wrap: anywhere;
		color: var(--text);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.version {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.caret {
		display: flex;
		color: var(--text-subtle);
		transition: transform var(--dur-base) var(--ease-out);
	}

	.caret :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	details[open] .caret {
		transform: rotate(180deg);
	}

	.body {
		display: grid;
		gap: 12px;
		padding: 0 16px 16px;
	}

	.text {
		max-block-size: 18rem;
		margin: 0;
		padding: 12px 14px;
		overflow: auto;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font:
			400 0.75rem / 1.55 ui-monospace,
			'Cascadia Code',
			'SF Mono',
			Menlo,
			Consolas,
			monospace;
		white-space: pre-wrap;
		overflow-wrap: anywhere;
	}

	.missing {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.source {
		display: inline-flex;
		align-items: center;
		justify-self: start;
		gap: 4px;
		color: var(--accent-text);
		font: var(--type-ui-strong);
		text-decoration: none;
	}

	.source :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	@media (hover: hover) {
		.source:hover {
			text-decoration: underline;
			text-underline-offset: 0.18em;
		}
	}
</style>
