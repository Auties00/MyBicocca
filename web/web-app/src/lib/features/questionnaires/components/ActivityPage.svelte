<script lang="ts">
	import IconChalkboardTeacher from '~icons/ph/chalkboard-teacher';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconClipboard from '~icons/ph/clipboard-text-duotone';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Avatar from '#lib/ui/Avatar.svelte';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { requestStart } from '../compilation.svelte.ts';
	import { unitLine } from '../format.ts';
	import { compileHref, questionnairesHref } from '../links.ts';
	import { unitTitle, type TeachingUnit } from '../model.ts';
	import { ActivityUnitsQuery, useQuestionnairesView } from '../questionnaires.svelte.ts';
	import BeforeYouStart from './BeforeYouStart.svelte';
	import UnitsSkeleton from './UnitsSkeleton.svelte';

	interface Props {
		/** The activity's `adsceId`, from the URL; undefined when the URL names none. */
		id: number | undefined;
	}

	let { id }: Props = $props();

	const view = useQuestionnairesView();
	const list = view.questionnaires.query;
	const unitsQuery = new ActivityUnitsQuery(() => id);
	const query = unitsQuery.query;

	const activity = $derived(view.questionnaires.find(id));
	const data = $derived(unitsQuery.units);
	const listLoaded = $derived(list.data !== undefined);
	const listPending = $derived(!listLoaded && list.status === 'pending');
	const missing = $derived(id === undefined || (listLoaded && activity === undefined));
	const loaded = $derived(data !== undefined);
	const gate = new LoadingGate(() => !missing && query.status === 'pending');
	const failed = $derived(!loaded && query.status === 'error' && query.error !== undefined);
	/* The list's own failure has its banner over the list, or doesn't matter on its own page. */
	const staleError = $derived(loaded ? query.error : undefined);
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);

	const title = $derived(activity?.name ?? (listPending ? '' : m.questionnaires_title()));
	const subtitle = $derived(data && (data.title ?? m.questionnaires_units_subtitle()));
	/* The name and code come with the list, the subtitle with the units: bars until then. */
	const headerLoading = $derived(
		listPending || (!missing && !loaded && query.status === 'pending')
	);
	const open = $derived(data?.questionnaire != null);
	const waiting = $derived(data?.units.filter((unit) => !unit.done) ?? []);
	const allDone = $derived(data !== undefined && data.units.length > 0 && waiting.length === 0);

	/** A plain click starts the compilation at once; anything else opens it waiting. */
	function startOnArrival(unit: TeachingUnit, event: MouseEvent) {
		if (id === undefined) return;
		if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
			return;
		}
		requestStart(`${id}/${unit.index}`);
	}
</script>

<svelte:head>
	{#if activity}<title>{activity.name} · {m.questionnaires_title()} · {m.app_name()}</title>{/if}
</svelte:head>

{#snippet unitAction(unit: TeachingUnit)}
	{#if unit.done}
		<StatusChip status="success" size="sm" label={m.questionnaires_unit_done()} />
	{:else if open && id !== undefined}
		<Button
			variant="solid"
			size="sm"
			href={compileHref(id, unit.index)}
			disabled={!network.online}
			label={m.questionnaires_unit_compile_label({ name: unitTitle(unit) })}
			onclick={(event) => startOnArrival(unit, event)}
		>
			{m.questionnaires_unit_compile()}
		</Button>
	{:else}
		<span class="unavailable">{m.questionnaires_unit_unavailable()}</span>
	{/if}
{/snippet}

<div class="frame">
	{#if !view.wide}
		<PageHeader
			{title}
			{subtitle}
			eyebrow={activity?.code ?? undefined}
			eyebrowCode
			back={{ href: questionnairesHref(), label: m.questionnaires_title() }}
			loading={headerLoading}
		/>
		{#if staleError}
			<div class="stale">
				<ErrorState
					variant="stale"
					message={errorMessage(staleError)}
					{updatedAt}
					onretry={query.refresh}
					retrying={query.refreshing}
				/>
			</div>
		{/if}
	{/if}

	{#if missing}
		<EmptyState
			size="page"
			icon={IconClipboard}
			title={m.questionnaires_not_found_title()}
			body={m.questionnaires_not_found_body()}
		>
			{#snippet action()}
				<Button href={questionnairesHref()}>{m.questionnaires_back_to_list()}</Button>
			{/snippet}
		</EmptyState>
	{:else if failed && query.error}
		<ErrorState
			title={m.questionnaires_units_load_failed()}
			message={errorMessage(query.error)}
			onretry={query.refresh}
			retrying={query.refreshing}
		/>
	{:else}
		<article class="activity">
			<DetailHeader
				{title}
				eyebrow={activity?.code ?? undefined}
				eyebrowCode
				{subtitle}
				loading={headerLoading}
			/>
			{#if view.wide}
				{#if staleError}
					<ErrorState
						variant="stale"
						message={errorMessage(staleError)}
						{updatedAt}
						onretry={query.refresh}
						retrying={query.refreshing}
					/>
				{/if}
			{/if}

			{#if !data}
				<div aria-busy="true">
					{#if gate.visible}<UnitsSkeleton facts={activity?.status !== 'done'} />{/if}
				</div>
			{:else}
				<!-- A misconfigured questionnaire says why it can't be filled in; a closed window, why not yet. -->
				{#if activity?.status === 'misconfigured'}
					<Banner tone="warning" message={m.questionnaires_misconfigured()} />
				{:else if !open && waiting.length > 0}
					<Banner tone="info" message={m.questionnaires_window_closed()} />
				{/if}
				{#if open && waiting.length > 0 && !network.online}
					<Banner tone="neutral" icon={IconWifiSlash} message={m.questionnaires_offline_hint()} />
				{/if}

				{#if data.units.length === 0}
					<EmptyState icon={IconChalkboardTeacher} title={m.questionnaires_units_none()} />
				{:else}
					<ListGroup title={m.questionnaires_units_heading()} level={view.wide ? 3 : 2}>
						{#each data.units as unit (unit.index)}
							<ListRow title={unitTitle(unit)} description={unitLine(unit, activity?.name)}>
								{#snippet leading()}
									{#if unit.lecturer}
										<Avatar name={unit.lecturer} tone="neutral" />
									{:else}
										<IconChip icon={IconChalkboardTeacher} />
									{/if}
								{/snippet}
								{#snippet trailing()}{@render unitAction(unit)}{/snippet}
							</ListRow>
						{/each}
					</ListGroup>
				{/if}

				{#if allDone}
					<p class="done">
						<IconCheckCircle aria-hidden="true" />
						{m.questionnaires_units_all_done()}
					</p>
				{:else if open && waiting.length > 0}
					<BeforeYouStart anonymous={data.anonymous} level={view.wide ? 3 : 2} />
				{/if}
			{/if}
		</article>
	{/if}
</div>

<style>
	.frame {
		display: grid;
		gap: var(--page-gap);
	}

	.stale {
		margin-block-start: calc(var(--page-gap) * -0.5);
	}

	.activity {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: var(--w-reading);
	}

	.unavailable {
		color: var(--text-muted);
		font: var(--type-ui);
		white-space: nowrap;
	}

	.done {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.done :global(svg) {
		flex: none;
		inline-size: 20px;
		block-size: 20px;
		color: var(--success);
	}
</style>
