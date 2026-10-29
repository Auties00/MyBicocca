<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import { m } from '#lib/paraglide/messages.js';
	import { sourceName } from '../format.ts';
	import { links } from '../links.ts';
	import { eventSources, type CalendarEvent, type EventSource } from '../model.ts';
	import { inProgressSwatch, lessonPool, sourceSwatches, type Swatch } from '../palette.ts';

	interface Props {
		/** The events on screen, counted per source. */
		events: readonly CalendarEvent[];
		/**
		 * Whether a library account is linked; without one, the legend says how to show seats.
		 * Undefined while that is not known yet.
		 */
		libraryLinked: boolean | undefined;
		/** list: the side column's; inline: one wrapping row under the month grid. */
		variant?: 'list' | 'inline';
	}

	let { events, libraryLinked, variant = 'list' }: Props = $props();

	const headingId = $props.id();

	const counts = $derived(
		events.reduce(
			(map, event) => map.set(event.source, (map.get(event.source) ?? 0) + 1),
			new Map<EventSource, number>()
		)
	);

	/* Lessons take a colour per course: three of the pool stand for them all. */
	const lessonSample: Swatch[] = [lessonPool[3], lessonPool[5], lessonPool[0]];

	function swatches(source: EventSource): Swatch[] {
		return source === 'lesson' ? lessonSample : [sourceSwatches[source]];
	}
</script>

<section class={['legend', variant]} aria-labelledby={headingId}>
	<h2 id={headingId} class="title">{m.calendar_sources()}</h2>
	<ul>
		{#each eventSources as source (source)}
			<li>
				<span class="swatches" aria-hidden="true">
					{#each swatches(source) as swatch, index (index)}
						<span class="calendar-swatch dot" style:--sw-h={swatch.hue} style:--sw-c={swatch.chroma}
						></span>
					{/each}
				</span>
				<span class="name">{sourceName(source)}</span>
				<span class="count">{counts.get(source) ?? ''}</span>
			</li>
		{/each}
		<li>
			<span class="swatches" aria-hidden="true">
				<span
					class="calendar-swatch dot"
					style:--sw-h={inProgressSwatch.hue}
					style:--sw-c={inProgressSwatch.chroma}
				></span>
			</span>
			<span class="name">{m.calendar_in_progress()}</span>
			<span class="count"></span>
		</li>
	</ul>
	{#if libraryLinked === false}
		<a class="hint" href={links.library()}>
			{m.calendar_library_hint()}
			<IconArrowRight aria-hidden="true" />
		</a>
	{/if}
</section>

<style>
	.legend {
		display: grid;
		gap: var(--sp-2);
	}

	.title {
		padding-inline: 6px;
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	ul {
		display: grid;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	li {
		display: grid;
		grid-template-columns: 36px minmax(0, 1fr) auto;
		align-items: center;
		gap: 8px;
		min-block-size: 32px;
		padding-inline: 6px;
		font: var(--type-ui);
	}

	.swatches {
		display: flex;
	}

	.dot {
		inline-size: 12px;
		block-size: 12px;
		border-radius: 4px;
		background: var(--sw-container);
		box-shadow:
			inset 3px 0 0 var(--sw-accent),
			0 0 0 1.5px var(--bg);
	}

	.dot + .dot {
		margin-inline-start: -4px;
	}

	.count {
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
	}

	.hint {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		justify-self: start;
		padding: 4px 6px;
		border-radius: var(--r-2xs);
		color: var(--accent-text);
		font: var(--type-meta);
		font-size: 0.75rem;
		text-decoration: none;
	}

	.hint :global(svg) {
		flex: none;
		inline-size: 14px;
		block-size: 14px;
	}

	@media (hover: hover) {
		.hint:hover {
			text-decoration: underline;
		}
	}

	.hint:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 1px;
	}

	/* Inline: the sources in one wrapping row, the title as its first item. */
	.inline {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 4px 16px;
	}

	.inline .title {
		padding-inline: 0;
	}

	.inline ul {
		display: flex;
		flex-wrap: wrap;
		gap: 4px 14px;
	}

	.inline li {
		display: flex;
		gap: 6px;
		min-block-size: 24px;
		padding-inline: 0;
		font: var(--type-meta);
	}

	.inline .swatches {
		min-inline-size: 0;
	}
</style>
