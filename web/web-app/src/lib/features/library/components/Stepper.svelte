<script lang="ts" module>
	import type { ResolvedPathname } from '$app/types';

	export interface StepItem {
		label: string;
		/** What was chosen there, once it was. */
		value?: string;
		/** Going back to the step; absent for the current one and those ahead. */
		href?: ResolvedPathname;
	}
</script>

<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import { m } from '#lib/paraglide/messages.js';

	interface Props {
		steps: readonly StepItem[];
		current: number;
	}

	let { steps, current }: Props = $props();

	/* A done step keeps its second line while its value loads, so the steps never grow. */
	const NBSP = String.fromCharCode(160);
</script>

<nav class="stepper" aria-label={m.library_steps()}>
	<ol style:--count={steps.length} style:--done={current}>
		{#each steps as item, index (item.label)}
			{@const done = index < current}
			<li class={['step', { done, current: index === current }]}>
				{#if done && item.href}
					<a
						class="inner"
						href={item.href}
						title={item.value ? `${item.label}: ${item.value}` : item.label}
					>
						<span class="mark"><IconCheck aria-hidden="true" /></span>
						<span class="text">
							<span class="label">{item.label}</span>
							<span class="value">{item.value ?? NBSP}</span>
						</span>
					</a>
				{:else}
					<span class="inner" aria-current={index === current ? 'step' : undefined}>
						<span class="mark">{index + 1}</span>
						<span class="text"><span class="label">{item.label}</span></span>
					</span>
				{/if}
			</li>
		{/each}
	</ol>
</nav>

<style>
	.stepper {
		container-type: inline-size;
	}

	ol {
		display: grid;
		grid-template-columns: repeat(var(--count), minmax(0, 1fr));
		gap: 6px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.step {
		position: relative;
		min-inline-size: 0;
		padding-block-start: 10px;
	}

	/* A progress track above the steps: filled up to the current one. */
	.step::before {
		content: '';
		position: absolute;
		inset-block-start: 0;
		inset-inline: 0;
		block-size: 4px;
		border-radius: 2px;
		background: var(--hairline-strong);
	}

	.step.done::before,
	.step.current::before {
		background: var(--accent);
	}

	.inner {
		display: flex;
		align-items: flex-start;
		gap: 8px;
		min-inline-size: 0;
		padding: 6px 6px 6px 0;
		border-radius: var(--r-sm);
		color: var(--text-muted);
		text-decoration: none;
	}

	a.inner {
		color: var(--text);
	}

	@media (hover: hover) {
		a.inner:hover .label {
			text-decoration: underline;
		}
	}

	.mark {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 22px;
		block-size: 22px;
		border-radius: 50%;
		background: var(--surface-sunken);
		font: 600 0.75rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.mark :global(svg) {
		inline-size: 12px;
		block-size: 12px;
	}

	.done .mark {
		background: var(--container);
		color: var(--on-container);
	}

	.current .mark {
		background: var(--accent);
		color: var(--on-accent);
	}

	.text {
		display: grid;
		min-inline-size: 0;
	}

	.label {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.current .label {
		color: var(--text);
	}

	.value {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	/*
	 * Narrow: only the current step shows its words; the others are their marks, their words kept
	 * for screen readers so each link still says where it goes.
	 */
	@container (width < 560px) {
		ol {
			display: flex;
		}

		.step {
			flex: 0 0 44px;
		}

		.step.current {
			flex: 1 1 auto;
		}

		.step:not(.current) .text {
			position: absolute;
			inline-size: 1px;
			block-size: 1px;
			overflow: hidden;
			clip-path: inset(50%);
			white-space: nowrap;
		}

		a.inner {
			min-block-size: 36px;
		}
	}
</style>
