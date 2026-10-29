<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconPlayCircle from '~icons/ph/play-circle';
	import IconSpeakerHigh from '~icons/ph/speaker-high-fill';
	import { formatPercent } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { PlaylistItem } from '../../content.ts';
	import { coursePath } from '../../links.ts';
	import { isInProgress, progressRatio, type ProgressMap } from '../../video/progress.ts';
	import WavyProgress from '../WavyProgress.svelte';

	interface Props {
		items: readonly PlaylistItem[];
		courseId: number;
		current: number;
		progress: ProgressMap;
		lastSeen: number | undefined;
	}

	let { items, courseId, current, progress, lastSeen }: Props = $props();

	const done = $derived(
		items.filter((item) => item.completed || progress[item.cmId]?.completed).length
	);

	/* The playing item stays in view as the list scrolls within its column. */
	function reveal(list: HTMLElement) {
		void current;
		list.querySelector<HTMLElement>('[aria-current="true"]')?.scrollIntoView({ block: 'nearest' });
	}
</script>

<section class="playlist" aria-labelledby="playlist-title">
	<header class="head">
		<h2 id="playlist-title">{m.course_video_playlist()}</h2>
		<p class="count">{m.course_video_completed_count({ done, total: items.length })}</p>
	</header>
	<ol class="items" {@attach reveal}>
		{#each items as item, index (item.cmId)}
			{@const saved = progress[item.cmId]}
			{@const watched = item.completed || saved?.completed === true}
			{@const playing = item.cmId === current}
			<li>
				<a
					href={coursePath(courseId, 'video', item.cmId)}
					class={{ playing, watched }}
					aria-current={playing ? 'true' : undefined}
					data-sveltekit-replacestate
				>
					<span class="glyph" aria-hidden="true">
						{#if playing}
							<IconSpeakerHigh />
						{:else if watched}
							<IconCheckCircle />
						{:else}
							<IconPlayCircle />
						{/if}
					</span>
					<span class="text">
						<span class="title">
							<span class="sr-only">{index + 1}. </span>{item.title}
						</span>
						<span class="section">
							{item.sectionTitle ?? m.course_section_fallback({ number: item.sectionOrdinal })}
							{#if watched}<span class="sr-only">, {m.course_module_watched()}</span>{/if}
						</span>
						{#if isInProgress(saved) && !watched}
							<span class="bar">
								<WavyProgress
									value={progressRatio(saved)}
									wavy={lastSeen === item.cmId}
									label={m.course_module_watched_percent({
										percent: formatPercent(progressRatio(saved))
									})}
								/>
							</span>
						{/if}
					</span>
				</a>
			</li>
		{/each}
	</ol>
</section>

<style>
	.playlist {
		display: grid;
		grid-template-rows: auto minmax(0, 1fr);
		gap: 10px;
		min-block-size: 0;
	}

	.head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 12px;
		padding-inline: 8px;
	}

	h2 {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.count {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.items {
		display: grid;
		align-content: start;
		gap: 2px;
		margin: 0;
		padding: 0;
		overflow-y: auto;
		list-style: none;
		scrollbar-width: thin;
		overscroll-behavior: contain;
	}

	a {
		display: grid;
		grid-template-columns: 24px minmax(0, 1fr);
		gap: 12px;
		padding: 10px 12px;
		border-radius: var(--r-md);
		color: inherit;
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		a:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	a:focus-visible {
		outline-offset: -2px;
	}

	.playing {
		background: var(--course-container);
	}

	.glyph {
		display: grid;
		padding-block-start: 1px;
		color: var(--text-subtle);
	}

	.glyph :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.watched .glyph {
		color: var(--course-bar);
	}

	.playing .glyph {
		color: var(--course-ink);
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-ui-strong);
		overflow-wrap: anywhere;
	}

	.section {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
		white-space: nowrap;
		text-overflow: ellipsis;
	}

	.bar {
		display: block;
		max-inline-size: 220px;
		margin-block-start: 6px;
	}
</style>
