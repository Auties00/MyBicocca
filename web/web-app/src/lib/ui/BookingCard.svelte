<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconClock from '~icons/ph/clock';
	import IconMapPin from '~icons/ph/map-pin';
	import Badge from './Badge.svelte';
	import Card from './Card.svelte';
	import DateBadge from './DateBadge.svelte';
	import type { Href, Icon } from './types.ts';

	interface Props {
		/** The booking's own page. */
		href: Href;
		/** Its day; a booking without one yet shows no date. */
		date?: Date;
		title: string;
		/** Language of the title when it differs from the UI's (Esse3's Italian names). */
		titleLang?: string;
		/** One line under the title: the exam's kind, the office's section, the seat. */
		subtitle?: string | Snippet;
		/** Top right: a countdown or a state. accent for what happens today or now. */
		status?: { label: string; tone?: 'neutral' | 'accent' };
		/** Over: a neutral date and a muted title. */
		past?: boolean;
		/** The footer's first fact, which never wraps: "Tue 4 Nov · 09:30". */
		when: string;
		/** The footer's second fact, cut short when long. */
		place?: {
			label: string;
			/** A map pin by default; a camera for a video call. */
			icon?: Icon;
			/** Not known yet ("Room to be confirmed"): set in italics. */
			pending?: boolean;
			/** A proper name (a room, a library), left untranslated. */
			name?: boolean;
		};
	}

	let {
		href,
		date,
		title,
		titleLang,
		subtitle,
		status,
		past = false,
		when,
		place
	}: Props = $props();

	const PlaceIcon = $derived(place?.icon ?? IconMapPin);
</script>

<Card padding="none" {href}>
	<div class={['main', { past }]}>
		{#if date}<DateBadge {date} tone={past ? 'neutral' : 'accent'} />{/if}
		<div class="text">
			<p class="title" lang={titleLang}>{title}</p>
			{#if subtitle}
				<p class="subtitle">
					{#if typeof subtitle === 'string'}{subtitle}{:else}{@render subtitle()}{/if}
				</p>
			{/if}
		</div>
		{#if status}
			<span class="status"><Badge tone={status.tone ?? 'neutral'}>{status.label}</Badge></span>
		{/if}
	</div>
	{#snippet footer()}
		<span class="fact when">
			<IconClock aria-hidden="true" />
			{when}
		</span>
		{#if place}
			<span class={['fact', 'place', { pending: place.pending }]}>
				<PlaceIcon aria-hidden="true" />
				<span translate={place.name ? 'no' : undefined}>{place.label}</span>
			</span>
		{/if}
	{/snippet}
</Card>

<style>
	.main {
		display: flex;
		align-items: center;
		gap: 14px;
		min-block-size: 84px;
		padding: 16px;
	}

	.text {
		display: grid;
		flex: 1;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		display: -webkit-box;
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		text-wrap: pretty;
	}

	.past .title {
		color: var(--text-muted);
	}

	.subtitle {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.status {
		display: flex;
		flex: none;
		align-self: flex-start;
	}

	.fact {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		min-inline-size: 0;
		font-variant-numeric: tabular-nums;
	}

	.fact :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
	}

	/* The time never wraps: a long place gives way, with an ellipsis. */
	.when {
		flex: none;
		white-space: nowrap;
	}

	.place span {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.pending {
		font-style: italic;
	}
</style>
