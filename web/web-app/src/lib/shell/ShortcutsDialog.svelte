<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Kbd from '#lib/ui/Kbd.svelte';
	import { pageShortcuts, type PageShortcut } from '#lib/ui/shortcuts.svelte.ts';
	import { allDestinations } from '#lib/ui/destinations.ts';
	import { shell } from './shell.svelte.ts';

	const general = $derived<PageShortcut[]>([
		{ label: m.shell_shortcut_palette(), keys: [['mod', 'k']] },
		{ label: m.shell_shortcut_search(), keys: [['/']] },
		{ label: m.shell_action_sidebar(), keys: [['mod', 'b']] },
		{ label: m.shell_shortcut_close(), keys: [['esc']] },
		{ label: m.shell_shortcut_help(), keys: [['?']] }
	]);

	/* The keys of what is on screen now, read only while the dialog shows. */
	const page = $derived(shell.shortcutsOpen ? pageShortcuts() : []);
</script>

{#snippet group(title: string, rows: readonly PageShortcut[])}
	<section>
		<h3 class="heading">{title}</h3>
		<dl class="list">
			{#each rows as row (row.label)}
				<div class="row">
					<dt>{row.label}</dt>
					<dd>
						{#each row.keys as keys, i (i)}<Kbd {keys} />{/each}
					</dd>
				</div>
			{/each}
		</dl>
	</section>
{/snippet}

<Dialog bind:open={shell.shortcutsOpen} title={m.shell_shortcuts_title()} size="md">
	<div class="columns">
		<div class="stack">
			{#if page.length > 0}{@render group(m.shell_shortcuts_page(), page)}{/if}
			{@render group(m.shell_shortcuts_general(), general)}
		</div>
		<section>
			<h3 class="heading">{m.shell_shortcuts_go()}</h3>
			<dl class="list">
				{#each allDestinations as destination (destination.id)}
					<div class="row">
						<dt>{destination.label()}</dt>
						<dd>
							<Kbd keys={['g']} />
							<span class="then">{m.shell_shortcut_then()}</span>
							<Kbd keys={[destination.key]} />
						</dd>
					</div>
				{/each}
			</dl>
		</section>
	</div>
</Dialog>

<style>
	.columns {
		display: grid;
		gap: 24px;
	}

	@media (width >= 600px) {
		.columns {
			grid-template-columns: 1fr 1fr;
			gap: 32px;
		}
	}

	.stack {
		display: grid;
		gap: 24px;
		align-content: start;
	}

	.heading {
		margin-block-end: 8px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.list {
		display: grid;
		margin: 0;
	}

	.row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16px;
		min-block-size: 40px;
		border-block-end: 1px solid var(--hairline);
	}

	.row:last-child {
		border-block-end: 0;
	}

	dt {
		font: var(--type-ui);
	}

	dd {
		display: flex;
		align-items: center;
		gap: 6px;
		margin: 0;
	}

	.then {
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
