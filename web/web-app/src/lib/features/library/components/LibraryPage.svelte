<script lang="ts">
	import IconArmchair from '~icons/ph/armchair';
	import IconEnvelope from '~icons/ph/envelope-simple';
	import IconGlobe from '~icons/ph/globe-simple';
	import IconMapPin from '~icons/ph/map-pin';
	import IconMapTrifold from '~icons/ph/map-trifold';
	import IconPhone from '~icons/ph/phone';
	import IconBooks from '~icons/ph/books-duotone';
	import IconNavigation from '~icons/ph/navigation-arrow';
	import { Clock } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { findBuilding } from '#lib/features/map/catalog.ts';
	import { buildingHref } from '#lib/features/map/links.ts';
	import { formatRelative } from '#lib/format/dates.ts';
	import { romeWallClock } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { ExternalHref } from '#lib/ui/types.ts';
	import { libraryLinks } from '../account.svelte.ts';
	import { crowdLabel, statusChangeLabel } from '../format.ts';
	import { bookHref, homeHref, loginHref } from '../links.ts';
	import { refreshWhileVisible } from '../live.svelte.ts';
	import {
		bookableLibraries,
		buildingReference,
		cleanAddress,
		cleanPlace,
		forecastSlots,
		phoneText,
		toLiveStatus
	} from '../model.ts';
	import { librariesQuery, liveDataQuery, siteQuery } from '../queries.ts';
	import ForecastChart from './ForecastChart.svelte';
	import OccupancyRing from './OccupancyRing.svelte';
	import OpeningHours from './OpeningHours.svelte';

	interface Props {
		siteId: string;
	}

	let { siteId }: Props = $props();

	const clock = new Clock(30_000);
	const libraries = query(() => session.current && librariesQuery(session.current));
	const live = query(() => session.current && liveDataQuery(session.current, siteId));
	const site = query(() => session.current && siteQuery(session.current, siteId));
	refreshWhileVisible(() => live.refresh());

	const card = $derived(
		libraries.data
			? bookableLibraries(libraries.data).find((item) => item.id === siteId)
			: undefined
	);
	const name = $derived(site.data?.primaryName.trim() || card?.name);
	/* Undefined until a listing or the site page tells; null once neither can. */
	const place = $derived(
		site.data
			? cleanPlace(site.data.secondaryName)
			: card
				? card.place
				: site.error
					? null
					: undefined
	);
	const picture = $derived(
		site.data
			? (site.data.pictures[0] ?? null)
			: card
				? card.picture
				: site.error
					? null
					: undefined
	);
	const missing = $derived(live.error?.kind === 'not_found' || site.error?.kind === 'not_found');

	const wall = $derived(romeWallClock(clock.now));
	const status = $derived(live.data ? toLiveStatus(live.data) : undefined);
	const slots = $derived(live.data ? forecastSlots(live.data) : []);
	const change = $derived(status ? statusChangeLabel(status, wall.date) : null);
	const statusGate = new LoadingGate(() => !live.data && !live.error);
	const siteGate = new LoadingGate(() => !site.data && !site.error);

	const link = $derived(session.current && libraryLinks.of(session.current.account));
	const bookTarget = $derived(link?.auth ? bookHref(siteId) : loginHref(bookHref(siteId)));

	const address = $derived(cleanAddress(site.data?.address ?? null));
	const reference = $derived(buildingReference(place ?? null));
	const building = $derived(reference ? findBuilding(reference) : undefined);
	const directions = $derived(
		site.data?.latitude != null && site.data.longitude != null
			? (`https://www.google.com/maps/dir/?api=1&destination=${site.data.latitude},${site.data.longitude}` as ExternalHref)
			: undefined
	);
	const website = $derived(
		site.data?.websiteUrl && /^https?:\/\//.test(site.data.websiteUrl)
			? (site.data.websiteUrl as ExternalHref)
			: undefined
	);

	let broken = $state(false);
</script>

<svelte:head>
	<title>{name ? `${name} · ` : ''}{m.library_title()} · {m.app_name()}</title>
</svelte:head>

{#snippet bookButton(size: 'md' | 'lg')}
	<Button variant="solid" {size} icon={IconArmchair} href={bookTarget}>
		{link?.auth || link === undefined ? m.library_book_seat() : m.library_verify_to_book()}
	</Button>
{/snippet}

<Page width="dashboard">
	<PageHeader
		title={name ?? m.library_title()}
		subtitle={place ?? undefined}
		back={{ href: homeHref(), label: m.library_title() }}
		loading={place === undefined && { eyebrow: false }}
	>
		{#snippet actions()}
			{#if !missing}<div class="header-book">{@render bookButton('md')}</div>{/if}
		{/snippet}
	</PageHeader>

	{#if missing}
		<EmptyState
			size="page"
			icon={IconBooks}
			title={m.library_not_found_title()}
			body={m.library_not_found_body()}
		>
			{#snippet action()}
				<Button href={homeHref()}>{m.library_all_libraries()}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<div class="layout">
			<div class="column">
				<section
					class="card status"
					aria-labelledby={status ? 'library-state' : undefined}
					aria-busy={!status && !live.error}
				>
					{#if live.error && !live.data}
						<ErrorState
							title={m.library_status_loading_failed()}
							message={errorMessage(live.error)}
							onretry={live.refresh}
							retrying={live.refreshing}
						/>
					{:else if !status}
						<!-- Shaped like the library the listing says it is: ring and chart only while open. -->
						<div class={['status-row', { ringed: card?.open !== false }]}>
							{#if statusGate.visible}
								{#if card?.open !== false}<Skeleton variant="circle" width="112px" />{/if}
								<div class="status-text">
									<Skeleton width="50%" />
									<Skeleton width="70%" />
									<Skeleton width="40%" />
								</div>
							{/if}
						</div>
						{#if card?.open !== false}
							<div class="forecast" aria-hidden="true">
								<div class="forecast-placeholder">
									{#if statusGate.visible}<Skeleton variant="block" height="100%" />{/if}
								</div>
							</div>
						{/if}
					{:else}
						{#if live.error}
							<ErrorState
								variant="stale"
								message={errorMessage(live.error)}
								updatedAt={live.updatedAt ? new Date(live.updatedAt) : undefined}
								onretry={live.refresh}
								retrying={live.refreshing}
							/>
						{/if}
						<div class={['status-row', { ringed: status.open && status.occupancy !== null }]}>
							{#if status.open && status.occupancy !== null}
								<OccupancyRing percent={status.occupancy} size="lg" />
							{/if}
							<div class="status-text">
								<p class={['state', { open: status.open }]} id="library-state">
									<span class="dot" aria-hidden="true"></span>
									{status.open ? m.library_open_now() : m.library_closed()}
								</p>
								{#if status.open && status.occupancy !== null}
									<p class="crowd">{crowdLabel(status.occupancy)}</p>
								{/if}
								{#if change}<p class="change">{change}</p>{/if}
								{#if live.updatedAt}
									<p class="updated">
										{m.ui_updated_ago({
											time: formatRelative(Math.min(live.updatedAt, clock.now), new Date(clock.now))
										})}
									</p>
								{/if}
							</div>
						</div>

						{#if status.open}
							<div class="forecast">
								<div class="forecast-head">
									<h2 class="card-title">{m.library_today_occupancy()}</h2>
									<p class="hint">{m.library_forecast_caption()}</p>
								</div>
								<ForecastChart {slots} day={wall.date} now={wall.minutes} />
							</div>
						{/if}
					{/if}
				</section>

				{#if !site.data && !site.error}
					<div class="card info-placeholder" aria-hidden="true">
						{#if siteGate.visible}
							<Skeleton width="40%" />
							<Skeleton width="85%" />
							<Skeleton width="55%" />
							<Skeleton width="60%" />
						{/if}
					</div>
				{:else if site.data && (address || site.data.phone || site.data.email || website)}
					<section class="card info" aria-labelledby="library-info">
						<h2 class="card-title" id="library-info">{m.library_info()}</h2>
						<dl class="facts">
							{#if address}
								<div class="fact">
									<dt>
										<IconMapPin aria-hidden="true" /><span class="sr-only"
											>{m.library_address()}</span
										>
									</dt>
									<dd>
										<span>{address}</span>
										<span class="links">
											{#if building && reference}
												<a href={buildingHref(building.code)}>
													<IconMapTrifold aria-hidden="true" />{m.library_on_map({
														building: reference
													})}
												</a>
											{/if}
											{#if directions}
												<a href={directions} target="_blank" rel="external noopener noreferrer">
													<IconNavigation aria-hidden="true" />{m.library_directions()}
												</a>
											{/if}
										</span>
									</dd>
								</div>
							{/if}
							{#if site.data.phone}
								<div class="fact">
									<dt>
										<IconPhone aria-hidden="true" /><span class="sr-only">{m.library_phone()}</span>
									</dt>
									<dd>
										<a href="tel:{site.data.phone}" translate="no">{phoneText(site.data.phone)}</a>
									</dd>
								</div>
							{/if}
							{#if site.data.email}
								<div class="fact">
									<dt>
										<IconEnvelope aria-hidden="true" /><span class="sr-only"
											>{m.library_email()}</span
										>
									</dt>
									<dd><a href="mailto:{site.data.email}" translate="no">{site.data.email}</a></dd>
								</div>
							{/if}
							{#if website}
								<div class="fact">
									<dt>
										<IconGlobe aria-hidden="true" /><span class="sr-only"
											>{m.library_website()}</span
										>
									</dt>
									<dd>
										<a
											href={website}
											target="_blank"
											rel="external noopener noreferrer"
											translate="no"
										>
											{website.replace(/^https?:\/\/(www\.)?/, '').replace(/\/$/, '')}
										</a>
									</dd>
								</div>
							{/if}
						</dl>
					</section>
				{/if}
			</div>

			<div class="column">
				{#if picture === undefined}
					<div class="photo" aria-hidden="true"></div>
				{:else if picture && !broken}
					<img
						class="photo"
						src={picture}
						alt=""
						width="720"
						height="315"
						decoding="async"
						onerror={() => (broken = true)}
					/>
				{/if}

				<OpeningHours {siteId} />
			</div>
		</div>

		<div class="dock">{@render bookButton('lg')}</div>
	{/if}
</Page>

<style>
	.layout,
	.column {
		display: grid;
		align-content: start;
		gap: var(--sp-5);
		min-inline-size: 0;
	}

	/* One column: the photo heads the page, then how busy it is, where it is and its hours. */
	@container page (width < 860px) {
		.column {
			display: contents;
		}

		.photo {
			order: -1;
			aspect-ratio: 21 / 8;
			max-block-size: 220px;
		}
	}

	/* Wide: how busy it is and where on the left, the photo and the hours on the right. */
	@container page (width >= 860px) {
		.layout {
			grid-template-columns: minmax(0, 3fr) minmax(0, 2fr);
			column-gap: var(--sp-6);
		}
	}

	/* Phones: the booking button stays at hand above the tab bar, as Android pins it. */
	.dock {
		display: none;
	}

	@media (width < 600px) {
		.header-book {
			display: none;
		}

		.dock {
			position: sticky;
			inset-block-end: calc(var(--shell-inset-bottom) + 10px);
			z-index: var(--z-sticky);
			display: grid;
			padding: 8px;
			border-radius: var(--r-xl);
			background: var(--surface-raised);
			box-shadow: var(--elev-3), var(--inner-light);
		}
	}

	.info-placeholder {
		block-size: 260px;
		align-content: start;
	}

	.card {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.card-title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.status-row {
		display: flex;
		align-items: center;
		gap: var(--sp-5);
		min-block-size: 76px;
	}

	.status-row.ringed {
		min-block-size: 112px;
	}

	.status-text {
		display: grid;
		gap: 4px;
		flex: 1;
		min-inline-size: 0;
	}

	.state {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		color: var(--danger);
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.state.open {
		color: var(--success);
	}

	.dot {
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: currentColor;
	}

	.crowd {
		font: var(--type-body-strong);
	}

	.change {
		color: var(--text-muted);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}

	.updated {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.forecast {
		display: grid;
		gap: var(--sp-3);
		padding-block-start: var(--sp-4);
		border-block-start: 1px solid var(--hairline);
	}

	.forecast-head {
		display: grid;
		gap: 2px;
	}

	.forecast-placeholder {
		block-size: 240px;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.photo {
		display: block;
		inline-size: 100%;
		block-size: auto;
		aspect-ratio: 16 / 7;
		object-fit: cover;
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
	}

	.info {
		gap: var(--sp-2);
	}

	.facts {
		display: grid;
		margin: 0;
	}

	.fact {
		display: grid;
		grid-template-columns: 20px minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-3);
		padding: 10px 0;
		border-block-start: 1px solid var(--hairline);
		font: var(--type-ui);
	}

	.fact:first-child {
		border-block-start: 0;
	}

	dt {
		color: var(--text-muted);
		padding-block-start: 1px;
	}

	dd {
		display: grid;
		gap: 6px;
		margin: 0;
		overflow-wrap: anywhere;
	}

	.links {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2) var(--sp-4);
	}

	a {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		color: var(--accent-text);
		font-weight: 600;
		text-decoration: none;
	}

	a :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		a:hover {
			text-decoration: underline;
		}
	}
</style>
