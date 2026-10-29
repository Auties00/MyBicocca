<script lang="ts">
	import IconFlagFill from '~icons/ph/flag-fill';
	import { m } from '#lib/paraglide/messages.js';
	import type { NavItem } from '../attempt.svelte.ts';
	import type { Answered } from '../questions.ts';

	interface Props {
		items: readonly NavItem[];
		page: number;
		/** Sequential quizzes can't jump: the tiles only show where the student stands. */
		sequential: boolean;
		onselect: (item: NavItem) => void;
	}

	let { items, page, sequential, onselect }: Props = $props();

	const STATES: Record<Answered, () => string> = {
		complete: m.quiz_nav_answered,
		partial: m.quiz_nav_partial,
		empty: m.quiz_nav_unanswered
	};

	function label(item: NavItem): string {
		return [
			m.quiz_question({ number: item.number }),
			STATES[item.answered](),
			item.flagged && m.quiz_nav_flagged()
		]
			.filter(Boolean)
			.join(', ');
	}
</script>

<div class="navigator">
	<ol class="tiles">
		{#each items as item (item.slot)}
			{@const classes = [
				'tile',
				item.answered,
				{ flagged: item.flagged, here: item.page === page }
			]}
			<li>
				{#if sequential}
					<span class={classes} role="img" aria-label={label(item)}>
						{item.number}
						{#if item.flagged}<IconFlagFill class="flag" aria-hidden="true" />{/if}
					</span>
				{:else}
					<button
						type="button"
						class={classes}
						aria-label={label(item)}
						aria-current={item.page === page ? 'step' : undefined}
						onclick={() => onselect(item)}
					>
						{item.number}
						{#if item.flagged}<IconFlagFill class="flag" aria-hidden="true" />{/if}
					</button>
				{/if}
			</li>
		{/each}
	</ol>
	<ul class="legend" aria-hidden="true">
		<li><span class="swatch complete"></span>{m.quiz_nav_answered()}</li>
		<li><span class="swatch partial"></span>{m.quiz_nav_partial()}</li>
		<li><span class="swatch empty"></span>{m.quiz_nav_unanswered()}</li>
		<li><IconFlagFill class="legend-flag" />{m.quiz_nav_flagged()}</li>
	</ul>
</div>

<style>
	.navigator {
		display: grid;
		gap: 16px;
	}

	.tiles {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(44px, 1fr));
		gap: 6px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.tile {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 100%;
		aspect-ratio: 1;
		max-inline-size: 52px;
		border: 0;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		line-height: 1;
		font-variant-numeric: tabular-nums;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-instant) var(--ease-out);
	}

	button.tile {
		cursor: pointer;
		touch-action: manipulation;
	}

	@media (hover: hover) {
		button.tile:hover {
			box-shadow: inset 0 0 0 1px var(--border-strong);
		}
	}

	button.tile:active {
		transform: scale(0.94);
	}

	.complete {
		background: var(--course-bar);
		box-shadow: none;
		color: var(--surface);
	}

	.partial {
		background: linear-gradient(
			to top,
			var(--course-bar) 0 50%,
			color-mix(in oklch, var(--course-container) 70%, var(--surface)) 50% 100%
		);
		box-shadow: none;
		color: var(--text);
	}

	/* The page shown: a bar under the number, so it never reads as the keyboard's focus ring. */
	.here::after {
		content: '';
		position: absolute;
		inset-inline: 30%;
		inset-block-end: 6px;
		block-size: 3px;
		border-radius: 2px;
		background: currentColor;
	}

	.tile :global(.flag) {
		position: absolute;
		inset: -5px -5px auto auto;
		inline-size: 16px;
		block-size: 16px;
		padding: 2px;
		border-radius: 50%;
		background: var(--surface-raised);
		box-shadow: var(--elev-1);
		color: var(--danger);
	}

	.legend {
		display: flex;
		flex-wrap: wrap;
		gap: 6px 14px;
		margin: 0;
		padding: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		list-style: none;
	}

	.legend li {
		display: inline-flex;
		align-items: center;
		gap: 6px;
	}

	.swatch {
		inline-size: 12px;
		block-size: 12px;
		border-radius: 3px;
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
	}

	.swatch.complete {
		background: var(--course-bar);
		box-shadow: none;
	}

	.swatch.partial {
		background: linear-gradient(to top, var(--course-bar) 0 50%, var(--course-container) 50% 100%);
		box-shadow: none;
	}

	.legend :global(.legend-flag) {
		inline-size: 12px;
		block-size: 12px;
		color: var(--danger);
	}
</style>
