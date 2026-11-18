<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import { listed, settingsGroups } from '../directory.ts';
	import Colophon from './Colophon.svelte';
</script>

<svelte:head>
	<title>{m.shell_settings()} · {m.app_name()}</title>
</svelte:head>

<Page width="reading">
	<PageHeader title={m.shell_settings()} subtitle={m.settings_subtitle()} />

	{#each settingsGroups as group (group.id)}
		<ListGroup title={group.title()} description={group.description()}>
			{#each group.entries.filter(listed) as entry (entry.id)}
				{#if 'href' in entry}
					<ListRow
						title={entry.title()}
						description={entry.description()}
						icon={entry.icon}
						iconTone={entry.tone}
						href={entry.href}
						lines={1}
					/>
				{:else}
					<ListRow
						title={entry.title()}
						description={entry.description()}
						icon={entry.icon}
						iconTone={entry.tone}
						lines={1}
					>
						{#snippet control()}<Switch bind:checked={entry.get, entry.set} />{/snippet}
					</ListRow>
				{/if}
			{/each}
		</ListGroup>
	{/each}

	<Colophon />
</Page>
