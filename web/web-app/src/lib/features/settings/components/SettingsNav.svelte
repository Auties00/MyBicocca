<script lang="ts">
	import { page } from '$app/state';
	import { m } from '#lib/paraglide/messages.js';
	import IconChip from '#lib/ui/IconChip.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import { listed, settingsGroups, type SettingsEntry } from '../directory.ts';

	const path = $derived(page.url.pathname);
	const within = (href: string) => path === href || path.startsWith(`${href}/`);
	// A section reached by its address (the command palette, a link) stays marked as the one open.
	const shown = (entry: SettingsEntry) => listed(entry) || ('href' in entry && within(entry.href));
</script>

{#snippet body(entry: SettingsEntry)}
	<IconChip icon={entry.icon} tone={entry.tone} size="sm" />
	<span class="text">
		<span class="label">{entry.title()}</span>
		<span class="value">{entry.description()}</span>
	</span>
{/snippet}

<nav class="nav" aria-label={m.settings_nav_label()}>
	{#each settingsGroups as group (group.id)}
		<section class="group" aria-labelledby="settings-nav-{group.id}">
			<h2 class="heading" id="settings-nav-{group.id}">{group.title()}</h2>
			<ul class="items" role="list">
				{#each group.entries.filter(shown) as entry (entry.id)}
					<li>
						{#if 'href' in entry}
							{@const current = within(entry.href)}
							<a
								class={['item', { current }]}
								href={entry.href}
								aria-current={current ? 'page' : undefined}
							>
								{@render body(entry)}
							</a>
						{:else}
							<label class="item">
								{@render body(entry)}
								<Switch bind:checked={entry.get, entry.set} />
							</label>
						{/if}
					</li>
				{/each}
			</ul>
		</section>
	{/each}
</nav>

<style>
	.nav {
		display: grid;
		gap: var(--sp-5);
		align-content: start;
	}

	.group {
		display: grid;
		gap: 4px;
	}

	.heading {
		padding-inline: 10px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.items {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.item {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 52px;
		padding: 6px 10px 6px 6px;
		border-radius: var(--r-md);
		color: var(--text);
		text-decoration: none;
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.item:not(.current):hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.item.current {
		background: var(--container);
		color: var(--on-container);
	}

	.item:focus-visible {
		outline-offset: -2px;
	}

	label.item:has(:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.text {
		display: grid;
		flex: 1;
		min-inline-size: 0;
	}

	.label {
		font: var(--type-ui);
	}

	.current .label {
		font-weight: 600;
	}

	.value {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.current .value {
		color: inherit;
		opacity: 0.8;
	}
</style>
