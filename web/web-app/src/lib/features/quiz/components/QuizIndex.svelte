<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import IconExam from '~icons/ph/exam';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { groupTitle } from '../format.ts';
	import { completionOf, sectionNumber, type QuizGroup } from '../model.ts';

	interface Props {
		groups: readonly QuizGroup[];
		onselect: (group: QuizGroup) => void;
	}

	let { groups, onselect }: Props = $props();

	let current = $state<string>();

	/* The entry of the section at the top of the screen stays marked while the list scrolls. */
	function spy() {
		const visible: Record<string, number> = {};
		const observer = new IntersectionObserver(
			(entries) => {
				for (const entry of entries) {
					const key = entry.target.id.replace('quiz-group-', '');
					if (entry.isIntersecting) visible[key] = entry.boundingClientRect.top;
					else delete visible[key];
				}
				const top = Object.entries(visible).sort((a, b) => a[1] - b[1])[0];
				if (top !== undefined) current = top[0];
			},
			{ rootMargin: '-120px 0px -50% 0px' }
		);
		for (const group of groups) {
			const element = document.getElementById(`quiz-group-${group.key}`);
			if (element) observer.observe(element);
		}
		return () => observer.disconnect();
	}
</script>

<nav class="index" aria-label={m.quiz_index_label()} {@attach spy}>
	<p class="heading">{m.quiz_index_title()}</p>
	<ol>
		{#each groups as group (group.key)}
			{@const number = sectionNumber(group)}
			{@const done = completionOf(group.items)}
			<li>
				<a
					href="#quiz-group-{group.key}"
					class={{ current: current === group.key }}
					aria-current={current === group.key ? 'location' : undefined}
					onclick={(event) => {
						event.preventDefault();
						onselect(group);
					}}
				>
					<span class="ordinal" aria-hidden="true">
						{#if number === null}<IconExam />{:else}{formatNumber(number)}{/if}
					</span>
					<span class="name">{groupTitle(group)}</span>
					{#if done.tracked > 0}
						{#if done.done === done.total}
							<IconCheck class="check" aria-hidden="true" />
						{:else}
							<span class="done" aria-hidden="true">
								{formatNumber(done.done)}/{formatNumber(done.total)}
							</span>
						{/if}
						<span class="sr-only">{m.quiz_index_progress(done)}</span>
					{/if}
				</a>
			</li>
		{/each}
	</ol>
</nav>

<style>
	.index {
		display: grid;
		gap: 10px;
		max-block-size: calc(100dvh - var(--topbar-h) - var(--control-h-lg) - 48px);
		overflow-y: auto;
		scrollbar-width: thin;
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
		align-items: center;
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
		display: grid;
		place-items: center start;
		color: var(--text-subtle);
		font-variant-numeric: tabular-nums;
	}

	.ordinal :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.current .ordinal {
		color: inherit;
	}

	.name {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.done {
		color: var(--text-subtle);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	a :global(.check) {
		inline-size: 12px;
		block-size: 12px;
		color: var(--success);
	}
</style>
