<script lang="ts">
	import IconHeadset from '~icons/ph/headset-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { intlLocale } from '#lib/format/locale.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { directorySections, SECTION_ORDER } from '../directory.ts';
	import { sectionCaptions, sectionLabels } from '../format.ts';
	import { sectionIcons, sectionTones } from '../icons.ts';
	import { directoryHref } from '../links.ts';
	import { useServices } from '../services.svelte.ts';

	interface Props {
		headingId: string;
	}

	let { headingId }: Props = $props();

	const services = useServices();
	const sections = $derived(
		services.data ? directorySections(services.data, intlLocale()) : undefined
	);
	const gate = new LoadingGate(() => services.status === 'pending');
</script>

<section class="card" aria-labelledby={headingId}>
	<h2 class="heading" id={headingId}>{m.appointments_directory_heading()}</h2>
	{#if services.status === 'error' && services.error}
		<ErrorState
			title={m.appointments_directory_failed()}
			message={errorMessage(services.error)}
			onretry={services.refresh}
			retrying={services.refreshing}
		/>
	{:else if sections?.length === 0}
		<div class="none">
			<EmptyState
				icon={IconHeadset}
				title={m.appointments_directory_empty_title()}
				body={m.appointments_directory_empty_body()}
			>
				{#snippet action()}
					<Button onclick={services.refresh} loading={services.refreshing}>
						{m.common_retry()}
					</Button>
				{/snippet}
			</EmptyState>
		</div>
	{:else if sections}
		<ListGroup label={m.appointments_sections_label()}>
			{#each sections as section (section.id)}
				<ListRow
					title={sectionLabels[section.id]()}
					description={sectionCaptions[section.id]()}
					meta={m.appointments_section_count({ count: section.services.length })}
					icon={sectionIcons[section.id]}
					iconTone={sectionTones[section.id]}
					href={directoryHref(section.id)}
				/>
			{/each}
		</ListGroup>
	{:else}
		<div class="loading" aria-busy="true">
			{#if gate.visible}
				{#each SECTION_ORDER as id (id)}
					<div class="row">
						<Skeleton width="40px" height="40px" radius="12px" />
						<div class="lines">
							<Skeleton variant="text" width="45%" />
							<Skeleton variant="text" width="75%" />
						</div>
					</div>
				{/each}
			{/if}
		</div>
	{/if}
</section>

<style>
	.card {
		display: grid;
		gap: var(--sp-4);
		align-content: start;
	}

	.heading {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.none {
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
	}

	.loading {
		display: grid;
		gap: 2px;
		min-block-size: calc(4 * 76px);
	}

	.row {
		display: flex;
		align-items: center;
		gap: 14px;
		min-block-size: 74px;
		padding-inline: 16px;
		border-radius: var(--r-sm);
		background: var(--surface);
	}

	.lines {
		display: grid;
		flex: 1;
		gap: 6px;
		font: var(--type-meta);
	}
</style>
