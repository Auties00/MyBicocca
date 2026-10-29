<script lang="ts">
	import IconCaretRight from '~icons/ph/caret-right';
	import IconArmchair from '~icons/ph/armchair-duotone';
	import type { AffluencesReservationSiteInfo } from '@mybicocca/data-api/affluences';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import { bookHref } from '../links.ts';
	import { toZone } from '../model.ts';
	import ZoneSwatch from './ZoneSwatch.svelte';

	interface Props {
		siteId: string;
		info: Query<AffluencesReservationSiteInfo>;
	}

	let { siteId, info }: Props = $props();

	const zones = $derived(info.data?.types.map(toZone));
	const gate = new LoadingGate(() => zones === undefined && !info.error);
</script>

{#if info.error && !zones}
	<ErrorState
		title={m.library_zones_loading_failed()}
		message={errorMessage(info.error)}
		onretry={info.refresh}
		retrying={info.refreshing}
	/>
{:else if !zones}
	<ul class="zones" aria-busy="true">
		{#each { length: 4 }, index (index)}
			<li class="zone placeholder">
				{#if gate.visible}
					<Skeleton variant="block" width="40px" height="40px" radius="12px" />
					<span class="text"><Skeleton width="70%" /><Skeleton width="45%" /></span>
				{/if}
			</li>
		{/each}
	</ul>
{:else if zones.length === 0}
	<EmptyState
		size="page"
		icon={IconArmchair}
		title={m.library_no_zones()}
		body={m.library_no_zones_body()}
	/>
{:else}
	<ul class="zones">
		{#each zones as zone (zone.id)}
			<li>
				<a class="zone" href={bookHref(siteId, { zone: zone.id })}>
					<ZoneSwatch color={zone.color} />
					<span class="text">
						<span class="name">{zone.name ?? m.library_zone_fallback()}</span>
						{#if zone.detail}<span class="detail">{zone.detail}</span>{/if}
					</span>
					<IconCaretRight class="chevron" aria-hidden="true" />
				</a>
			</li>
		{/each}
	</ul>
{/if}

<style>
	.zones {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 280px), 1fr));
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.zone {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		block-size: 100%;
		min-block-size: 76px;
		padding: var(--sp-3) var(--sp-4);
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.placeholder {
		background: var(--surface);
	}

	@media (hover: hover) {
		a.zone:hover {
			background: var(--surface-hover);
			transition-duration: 0s, var(--dur-spring);
		}
	}

	a.zone:active {
		transform: scale(0.985);
		transition-duration: var(--dur-instant);
	}

	.text {
		display: grid;
		gap: 2px;
		flex: 1;
		min-inline-size: 0;
	}

	.name {
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.detail {
		display: -webkit-box;
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
	}

	.zone :global(.chevron) {
		flex-shrink: 0;
		color: var(--text-subtle);
	}

	@media (prefers-reduced-motion: reduce) {
		.zone {
			transition: none;
		}
	}
</style>
