<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { OutlineSection } from '../../content.ts';

	interface Props {
		outline: readonly OutlineSection[];
		completion: ReadonlyMap<number, { done: number; tracked: number }>;
		onselect: (card: OutlineSection) => void;
		/** The section the page just opened: marked until the reader scrolls on their own. */
		pinned?: number;
	}

	let { outline, completion, onselect, pinned = $bindable() }: Props = $props();

	/** The section at the top of the screen. */
	let spied = $state<number>();
	/** At the end of the page, which can't lift the last sections to the top: the lowest showing. */
	let last = $state<number>();
	const current = $derived(pinned ?? last ?? spied);

	const SCROLL_KEYS = new Set(['ArrowUp', 'ArrowDown', 'PageUp', 'PageDown', 'Home', 'End', ' ']);

	function elementOf(card: OutlineSection) {
		return document.getElementById(`section-${card.section.id}`);
	}

	function spy() {
		const visible: Record<number, number> = {};
		const observer = new IntersectionObserver(
			(entries) => {
				for (const entry of entries) {
					const id = Number(entry.target.id.replace('section-', ''));
					if (entry.isIntersecting) visible[id] = entry.boundingClientRect.top;
					else delete visible[id];
				}
				const top = Object.entries(visible).sort((a, b) => a[1] - b[1])[0];
				if (top !== undefined) spied = Number(top[0]);
			},
			{ rootMargin: '-120px 0px -50% 0px' }
		);
		for (const card of outline) {
			const element = elementOf(card);
			if (element) observer.observe(element);
		}

		let frame = 0;
		const measure = () => {
			frame = 0;
			const root = document.documentElement;
			if (window.scrollY + window.innerHeight < root.scrollHeight - 1) {
				last = undefined;
				return;
			}
			let lowest: number | undefined;
			for (const card of outline) {
				const top = elementOf(card)?.getBoundingClientRect().top;
				if (top !== undefined && top < window.innerHeight) lowest = card.section.id;
			}
			last = lowest;
		};
		const scroll = () => {
			frame ||= requestAnimationFrame(measure);
		};
		window.addEventListener('scroll', scroll, { passive: true });
		return () => {
			observer.disconnect();
			window.removeEventListener('scroll', scroll);
			cancelAnimationFrame(frame);
		};
	}

	/* A pin lasts until the reader scrolls: by wheel, touch or keys at once, or by any scroll that
	   follows the one the page made to reach the section. */
	$effect(() => {
		if (pinned === undefined) return;
		let settled = false;
		const release = () => (pinned = undefined);
		const key = (event: KeyboardEvent) => SCROLL_KEYS.has(event.key) && release();
		const ended = () => (settled = true);
		const scroll = () => settled && release();
		window.addEventListener('wheel', release, { passive: true });
		window.addEventListener('touchmove', release, { passive: true });
		window.addEventListener('keydown', key);
		window.addEventListener('scrollend', ended);
		window.addEventListener('scroll', scroll, { passive: true });
		return () => {
			window.removeEventListener('wheel', release);
			window.removeEventListener('touchmove', release);
			window.removeEventListener('keydown', key);
			window.removeEventListener('scrollend', ended);
			window.removeEventListener('scroll', scroll);
		};
	});
</script>

<nav class="index" aria-label={m.course_index_label()} {@attach spy}>
	<p class="heading">{m.course_index_title()}</p>
	<ol>
		{#each outline as card (card.section.id)}
			{@const done = completion.get(card.section.id)}
			<li>
				<a
					href="#section-{card.section.id}"
					class={{ current: current === card.section.id }}
					aria-current={current === card.section.id ? 'location' : undefined}
					onclick={(event) => {
						event.preventDefault();
						onselect(card);
					}}
				>
					<span class="ordinal">{formatNumber(card.ordinal)}</span>
					<span class="name"
						>{card.title ?? m.course_section_fallback({ number: card.ordinal })}</span
					>
					{#if done && done.tracked > 0 && done.done === done.tracked}
						<IconCheck class="check" aria-label={m.course_completed()} />
					{/if}
				</a>
			</li>
		{/each}
	</ol>
</nav>

<style>
	.index {
		display: none;
	}

	@container page (width >= 1000px) {
		.index {
			position: sticky;
			inset-block-start: calc(var(--topbar-h) + var(--control-h-lg) + 24px);
			display: grid;
			gap: 10px;
			max-block-size: calc(100dvh - var(--topbar-h) - var(--control-h-lg) - 48px);
			overflow-y: auto;
			scrollbar-width: thin;
		}
	}

	.heading {
		padding-inline: 10px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	ol {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	a {
		display: grid;
		grid-template-columns: 22px minmax(0, 1fr) auto;
		align-items: baseline;
		gap: 8px;
		padding: 7px 10px;
		border-radius: var(--r-sm);
		color: var(--text-muted);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		a:hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	a.current {
		background: var(--course-container);
		color: var(--course-ink);
	}

	.ordinal {
		font-variant-numeric: tabular-nums;
		color: var(--text-subtle);
	}

	.current .ordinal {
		color: inherit;
	}

	.name {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	a :global(.check) {
		inline-size: 12px;
		block-size: 12px;
		color: var(--success);
	}
</style>
