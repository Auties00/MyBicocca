<script lang="ts">
	import IconCheck from '~icons/ph/check';
	import IconDoor from '~icons/ph/door';
	import IconLockSimple from '~icons/ph/lock-simple';
	import IconMapPin from '~icons/ph/map-pin';
	import IconSealCheck from '~icons/ph/seal-check';
	import IconStack from '~icons/ph/stack';
	import IconUsers from '~icons/ph/users-three';
	import IconWheelchair from '~icons/ph/wheelchair';
	import { now } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { romeTime } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Chip from '#lib/ui/Chip.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { BuildingData } from '../buildingData.svelte.ts';
	import type { Building } from '../catalog.ts';
	import { equipmentLabel, floorLabel, kindLabel } from '../labels.ts';
	import { buildingHref, directionsHref } from '../links.ts';
	import { buildingTitle } from '../names.ts';
	import type { RoomsCatalog } from '../rooms.ts';
	import { plainText, roomType } from '../text.ts';
	import { getMapView } from '../view.svelte.ts';
	import DayStrip from './DayStrip.svelte';
	import Level from './Level.svelte';
	import LevelHeader from './LevelHeader.svelte';
	import StatusLine from './StatusLine.svelte';

	interface Props {
		building: Building;
		code: string;
		catalog: Query<RoomsCatalog>;
		data: BuildingData;
	}

	let { building, code, catalog, data }: Props = $props();

	const view = getMapView();
	const title = $derived(buildingTitle(building));
	const room = $derived(data.rooms?.find((entry) => entry.code === code));
	const details = $derived(data.details(code));
	const status = $derived(data.status(code));
	const entries = $derived(data.schedule?.get(code) ?? []);
	const time = $derived(now.current.getTime());
	const next = $derived(entries.find((entry) => !entry.closure && entry.start > time));

	const description = $derived(plainText(details?.description ?? null));
	const notes = $derived(plainText(details?.accessibilityNotes ?? null));
	const type = $derived(roomType(details?.typeName ?? null));
	const equipment = $derived(
		[...(details?.fixedEquipment ?? []), ...(details?.mobileEquipment ?? [])]
			.filter((item) => item.status === '0')
			.map((item) => equipmentLabel(item.name))
	);
	const accessible = $derived(
		!!details && (details.isAccessible || details.isInclusionValidated || notes !== null)
	);

	const roomPending = $derived(data.rooms === undefined && !catalog.error);
	/*
	 * Everything under the status card waits for both the bookings and the details, then shows at
	 * once: arriving one after the other they would push each other down.
	 */
	const settled = $derived(
		data.sites?.length === 0 ||
			((data.occupation.data !== undefined || data.occupation.error !== undefined) &&
				(data.showcase.data !== undefined || data.showcase.error !== undefined))
	);
	const loadingRoom = new LoadingGate(() => roomPending);
	const loadingBelow = new LoadingGate(() => !roomPending && !settled);
	const below = $derived(settled && !loadingBelow.visible);

	const bookingsFailed = $derived(!!data.occupation.error && !data.occupation.data);
	const detailsFailed = $derived(!!data.showcase.error && !data.showcase.data);
	const failure = $derived(
		bookingsFailed && detailsFailed
			? m.map_room_all_failed()
			: bookingsFailed
				? m.map_room_occupation_failed()
				: detailsFailed
					? m.map_room_details_failed()
					: undefined
	);

	function retry() {
		if (bookingsFailed) void data.occupation.refresh();
		if (detailsFailed) void data.showcase.refresh();
	}
</script>

<svelte:head>
	<title>{room?.name ?? code} · {title} · {m.app_name()}</title>
</svelte:head>

<Level key={`room:${building.code}/${code}`}>
	{#snippet head()}
		<LevelHeader
			title={room?.name ?? code}
			back={{ href: buildingHref(building.code, view.filter), label: title }}
		>
			{#snippet actions()}
				<Button
					variant="tonal"
					size="sm"
					icon={IconMapPin}
					href={directionsHref(building)}
					label={m.map_directions_hint({ building: title })}
				>
					{m.map_directions()}
				</Button>
			{/snippet}
		</LevelHeader>
	{/snippet}

	{#if catalog.error && !catalog.data}
		<ErrorState
			message={`${m.map_rooms_load_failed()} ${errorMessage(catalog.error)}`}
			onretry={catalog.refresh}
			retrying={catalog.refreshing}
		/>
	{:else if data.rooms === undefined || loadingRoom.visible}
		<div class="stack" aria-hidden="true">
			<div class="card status-card">
				<span class="sketch-status"><Skeleton variant="text" width="60%" /></span>
				<p class="context"><Skeleton variant="text" width="45%" /></p>
				<Skeleton height="calc(18px + 1.2em + 6px)" radius="var(--r-sm)" />
			</div>
			<div class="pills">
				<Skeleton width="96px" height="32px" radius="999px" />
				<Skeleton width="84px" height="32px" radius="999px" />
			</div>
		</div>
	{:else if room === undefined}
		<EmptyState
			size="panel"
			title={m.map_room_not_found({ building: title })}
			body={m.map_room_not_found_body()}
		>
			{#snippet action()}
				<Button variant="tonal" href={buildingHref(building.code, view.filter)}>{title}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<div class="stack">
			<!-- Without the bookings there is no status to show: the banner takes the card's place. -->
			{#if !(below && bookingsFailed)}
				<section class="card status-card" aria-label={m.map_room_status_now()}>
					<StatusLine {status} size="lg" />
					<!-- Always a line, sketched while the bookings load: the strip under it never moves. -->
					<p class="context">
						{#if !data.schedule}
							<Skeleton variant="text" width="55%" />
						{:else if status?.kind === 'busy'}
							{status.entry.title}
						{:else if next}
							{m.map_room_next({ title: next.title })}
						{:else}
							{m.map_room_nothing_next()}
						{/if}
					</p>
					<DayStrip {entries} now={time} pending={!data.schedule} />
				</section>
			{/if}

			{#if !below}
				<div class="pills" aria-hidden="true">
					<Skeleton width="96px" height="32px" radius="999px" />
					<Skeleton width="84px" height="32px" radius="999px" />
				</div>
				<div class="today" aria-hidden="true">
					{#each { length: 3 }, index (index)}
						<div class="slot">
							<Skeleton variant="text" lines={2} width="40px" />
							<Skeleton variant="text" lines={2} />
						</div>
					{/each}
				</div>
			{:else}
				{#if failure}
					<Banner tone="warning" message={failure}>
						{#snippet actions()}
							<Button
								variant="ghost"
								size="sm"
								loading={data.occupation.refreshing || data.showcase.refreshing}
								onclick={retry}
							>
								{m.common_retry()}
							</Button>
						{/snippet}
					</Banner>
				{/if}

				<ul class="pills" aria-label={m.map_room_facts()}>
					{#if room.floor !== null}
						<li><Chip icon={IconStack}>{floorLabel(room.floor)}</Chip></li>
					{/if}
					{#if room.capacity !== null}
						<li><Chip icon={IconUsers}>{m.map_seat_count({ count: room.capacity })}</Chip></li>
					{/if}
					{#if type}
						<li><Chip icon={IconDoor}>{type}</Chip></li>
					{/if}
				</ul>

				{#if !bookingsFailed}
					<section class="section">
						<h2 class="heading">{m.map_room_today()}</h2>
						{#if entries.length === 0}
							<p class="free-all-day">{m.map_room_no_activities()}</p>
						{:else}
							<ol class="today">
								{#each entries as entry (entry.id)}
									{@const current = entry.start <= time && time < entry.end}
									<li
										class={['slot', { current, past: entry.end <= time, closure: entry.closure }]}
									>
										<span class="times">
											<time datetime={new Date(entry.start).toISOString()}>
												{romeTime(entry.start)}
											</time>
											<time datetime={new Date(entry.end).toISOString()}>
												{romeTime(entry.end)}
											</time>
										</span>
										<span class="what">
											{#if entry.closure}
												<span class="title">
													<IconLockSimple aria-hidden="true" />
													{m.map_room_closure()}
												</span>
											{:else}
												<span class="title">{entry.title}</span>
												{#if entry.kind || entry.teachers.length > 0}
													<span class="meta">
														{[entry.kind ? kindLabel(entry.kind) : '', entry.teachers.join(', ')]
															.filter(Boolean)
															.join(' · ')}
													</span>
												{/if}
											{/if}
										</span>
										{#if current && !entry.closure}
											<span class="now">{m.map_room_in_progress()}</span>
										{/if}
									</li>
								{/each}
							</ol>
						{/if}
					</section>
				{/if}

				{#if description}
					<section class="section">
						<h2 class="heading">{m.map_room_description()}</h2>
						<p class="card text">{description}</p>
					</section>
				{/if}

				{#if accessible && details}
					<section class="section">
						<h2 class="heading">{m.map_room_accessibility()}</h2>
						<div class="card access">
							{#if details.isAccessible}
								<p class="fact"><IconWheelchair aria-hidden="true" />{m.map_room_barrier_free()}</p>
							{/if}
							{#if details.isInclusionValidated}
								<p class="fact"><IconSealCheck aria-hidden="true" />{m.map_room_validated()}</p>
							{/if}
							{#if notes}<p class="text">{notes}</p>{/if}
						</div>
					</section>
				{/if}

				{#if equipment.length > 0}
					<section class="section">
						<h2 class="heading">{m.map_room_equipment()}</h2>
						<ul class="pills">
							{#each equipment as item (item)}
								<li><Chip icon={IconCheck} size="sm">{item}</Chip></li>
							{/each}
						</ul>
					</section>
				{/if}
			{/if}
		</div>
	{/if}
</Level>

<style>
	.stack {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-1) var(--sp-1) 0;
	}

	.card {
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.status-card {
		display: grid;
		gap: var(--sp-3);
	}

	.sketch-status {
		display: flex;
		font: var(--type-title-s);
	}

	.context {
		margin-block-start: calc(-1 * var(--sp-2));
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.pills {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.section {
		display: grid;
		gap: var(--sp-2);
	}

	.heading {
		padding-inline: var(--sp-2);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.text {
		font: var(--type-body);
		font-size: 0.9375rem;
		white-space: pre-line;
		overflow-wrap: anywhere;
	}

	.access {
		display: grid;
		gap: var(--sp-2);
	}

	.fact {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		font: var(--type-ui);
	}

	.fact :global(svg) {
		flex: none;
		inline-size: 20px;
		block-size: 20px;
		color: var(--success);
	}

	.access .text {
		color: var(--text-muted);
	}

	.free-all-day {
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--success-container);
		color: var(--on-success-container);
		font: var(--type-ui);
	}

	.today {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.slot {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: start;
		gap: var(--sp-3);
		padding: 12px 14px;
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.slot:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.slot:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.past {
		color: var(--text-muted);
	}

	.past .title {
		color: var(--text-muted);
	}

	.slot.current {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.times {
		display: grid;
		min-inline-size: 3.2em;
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	.times time + time {
		color: var(--text-muted);
		font-weight: 500;
	}

	.slot.current .times time + time,
	.slot.current .meta {
		color: inherit;
		opacity: 0.8;
	}

	.what {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		font: var(--type-ui);
		overflow-wrap: anywhere;
	}

	.closure .title {
		display: flex;
		align-items: center;
		gap: 6px;
	}

	.meta {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.now {
		padding: 2px 8px;
		border-radius: var(--r-pill);
		background: var(--danger-solid);
		color: var(--on-accent);
		font: var(--type-label);
		font-weight: 600;
		white-space: nowrap;
	}
</style>
