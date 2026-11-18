<script lang="ts">
	import type { EasyStaffPlanningService } from '@mybicocca/data-api/easystaff';
	import IconHeadset from '~icons/ph/headset-duotone';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { intlLocale } from '#lib/format/locale.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SearchField from '#lib/ui/SearchField.svelte';
	import {
		directorySections,
		displayName,
		durationMinutes,
		searchServices,
		serviceKind,
		type SectionId
	} from '../directory.ts';
	import { sectionCaptions, sectionLabels } from '../format.ts';
	import { kindIcons, sectionIcons, sectionTones } from '../icons.ts';
	import { appointmentsHref, bookingHref, directoryHref } from '../links.ts';
	import { useServices } from '../services.svelte.ts';
	import DirectorySkeleton from './DirectorySkeleton.svelte';

	interface Props {
		/** The open section, from the URL. */
		section: SectionId | undefined;
	}

	let { section }: Props = $props();

	/* The sections beside their offices from this width of the page on; narrower, one at a time. */
	const TWO_COLUMNS = 760;

	const services = useServices();
	let search = $state('');
	let wide = $state(false);

	const sections = $derived(
		services.data ? directorySections(services.data, intlLocale()) : undefined
	);
	const open = $derived(
		sections?.find((item) => item.id === section) ?? (wide ? sections?.[0] : undefined)
	);
	const searching = $derived(search.trim() !== '');
	const matches = $derived.by(() => {
		if (!services.data || !searching) return undefined;
		return directorySections(searchServices(services.data, search), intlLocale());
	});
	const matchCount = $derived(matches?.reduce((sum, item) => sum + item.services.length, 0) ?? 0);
	/* On one column an open section is a page of its own, with its own header and way back. */
	const alone = $derived(!wide && !searching && open !== undefined);
	/*
	 * One header whose content follows the state: swapping two headers while the page settles
	 * would leave the top bar without its way back for a moment.
	 */
	const header = $derived(
		alone && open
			? {
					title: sectionLabels[open.id](),
					subtitle: sectionCaptions[open.id](),
					back: { href: directoryHref(), label: m.appointments_directory_title() }
				}
			: {
					title: m.appointments_directory_title(),
					subtitle: m.appointments_directory_subtitle(),
					back: { href: appointmentsHref(), label: m.appointments_title() }
				}
	);
	const gate = new LoadingGate(() => services.status === 'pending');
	const updatedAt = $derived(services.updatedAt ? new Date(services.updatedAt) : undefined);

	function measure(node: HTMLElement) {
		const update = () => {
			wide = node.clientWidth >= TWO_COLUMNS;
		};
		update();
		const observer = new ResizeObserver(update);
		observer.observe(node);
		return () => observer.disconnect();
	}
</script>

{#snippet serviceRows(list: readonly EasyStaffPlanningService[])}
	{#each list as service (service.id)}
		<!-- Office names run long ("Analisi dei Processi sociali / Turismo, territorio e…"): all of it shows. -->
		<ListRow
			title={displayName(service.name)}
			titleLines="all"
			description={m.appointments_minutes({ minutes: durationMinutes(service.durationSeconds) })}
			icon={kindIcons[serviceKind(service)]}
			href={bookingHref(service.id)}
		/>
	{/each}
{/snippet}

<svelte:head>
	<title>{header.title} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<PageHeader title={header.title} subtitle={header.subtitle} back={header.back} />

	{#if services.error && services.data}
		<ErrorState
			variant="stale"
			message={errorMessage(services.error)}
			{updatedAt}
			onretry={services.refresh}
			retrying={services.refreshing}
		/>
	{/if}

	<div class="body" {@attach measure}>
		{#if services.status === 'error' && services.error}
			<ErrorState
				title={m.appointments_directory_failed()}
				message={errorMessage(services.error)}
				onretry={services.refresh}
				retrying={services.refreshing}
			/>
		{:else if !sections}
			<div aria-busy="true" aria-label={m.appointments_directory_title()}>
				{#if gate.visible}<DirectorySkeleton {wide} />{/if}
			</div>
		{:else if sections.length === 0}
			<EmptyState
				size="page"
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
		{:else}
			{#if !alone}
				<div class="search">
					<SearchField
						label={m.appointments_search_label()}
						placeholder={m.appointments_search_label()}
						bind:value={search}
					/>
				</div>
			{/if}

			{#if matches}
				<p class="count" role="status">
					{matchCount > 0 ? m.appointments_search_results({ count: matchCount }) : ''}
				</p>
				{#if matches.length === 0}
					<EmptyState
						size="section"
						icon={IconMagnifyingGlass}
						title={m.appointments_search_empty({ query: search.trim() })}
					/>
				{:else}
					<div class="results">
						{#each matches as group (group.id)}
							<ListGroup title={sectionLabels[group.id]()} level={2}>
								{@render serviceRows(group.services)}
							</ListGroup>
						{/each}
					</div>
				{/if}
			{:else if wide && open}
				<div class="split">
					<nav
						class="sections"
						aria-label={m.appointments_sections_label()}
						data-sveltekit-replacestate
						data-sveltekit-reset="false"
					>
						<ListGroup label={m.appointments_sections_label()}>
							{#each sections as item (item.id)}
								<ListRow
									title={sectionLabels[item.id]()}
									description={sectionCaptions[item.id]()}
									icon={sectionIcons[item.id]}
									iconTone={sectionTones[item.id]}
									href={directoryHref(item.id)}
									selected={item.id === open.id}
								/>
							{/each}
						</ListGroup>
					</nav>
					<section class="offices" aria-label={sectionLabels[open.id]()}>
						<ListGroup
							title={sectionLabels[open.id]()}
							description={m.appointments_section_count({ count: open.services.length })}
							level={2}
						>
							{@render serviceRows(open.services)}
						</ListGroup>
					</section>
				</div>
			{:else if open}
				<div class="offices">
					<ListGroup label={m.appointments_services_label()}>
						{@render serviceRows(open.services)}
					</ListGroup>
				</div>
			{:else}
				<ListGroup label={m.appointments_sections_label()}>
					{#each sections as item (item.id)}
						<ListRow
							title={sectionLabels[item.id]()}
							description={sectionCaptions[item.id]()}
							meta={m.appointments_section_count({ count: item.services.length })}
							icon={sectionIcons[item.id]}
							iconTone={sectionTones[item.id]}
							href={directoryHref(item.id)}
						/>
					{/each}
				</ListGroup>
			{/if}
		{/if}
	</div>
</Page>

<style>
	.body {
		display: grid;
		gap: var(--sp-4);
		align-content: start;
	}

	.search {
		max-inline-size: 480px;
	}

	.count {
		min-block-size: 1lh;
		margin-block-start: calc(-1 * var(--sp-2));
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.results {
		display: grid;
		gap: var(--sp-6);
	}

	.split {
		display: grid;
		grid-template-columns: minmax(260px, 340px) minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-7);
	}

	.sections {
		position: sticky;
		inset-block-start: calc(var(--shell-inset-top) + var(--sp-3));
	}
</style>
