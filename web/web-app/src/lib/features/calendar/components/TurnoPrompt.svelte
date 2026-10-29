<script lang="ts">
	import IconUsersThree from '~icons/ph/users-three';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import { displayTitle } from '../format.ts';
	import type { TurnoChoice } from '../turni.svelte.ts';
	import type { SplitCourse } from '../turni.ts';

	interface Props {
		/** A course on screen whose lessons come in several turni, none of them chosen yet. */
		course: SplitCourse;
		/** A turno, or `all` when the prompt is dismissed: it asks once per course. */
		onchoose: (choice: TurnoChoice) => void;
		/** Phones: one row, so the timeline keeps its room. */
		compact?: boolean;
	}

	let { course, onchoose, compact = false }: Props = $props();

	const name = $derived(displayTitle(course.lesson));
	const question = $derived(
		m.calendar_turno_prompt_title({ course: name, count: course.turni.length })
	);
</script>

{#snippet choices()}
	<div class="choices" role="group" aria-label={m.calendar_turno_prompt_label({ course: name })}>
		{#each course.turni as turno (turno)}
			<Button variant="solid" size="sm" onclick={() => onchoose(turno)}>T{turno}</Button>
		{/each}
	</div>
{/snippet}

{#if compact}
	<div class="row">
		<IconUsersThree class="glyph" aria-hidden="true" />
		<p class="text">
			<span class="sr-only">{question}. {m.calendar_turno_prompt_message()}</span>
			<span class="caption" aria-hidden="true">{m.calendar_detail_turno()}</span>
			<span class="course" lang="it" aria-hidden="true">{name}</span>
		</p>
		{@render choices()}
		<button class="close" type="button" aria-label={m.ui_dismiss()} onclick={() => onchoose('all')}>
			<IconX aria-hidden="true" />
		</button>
	</div>
{:else}
	<Banner
		tone="accent"
		icon={IconUsersThree}
		title={question}
		message={m.calendar_turno_prompt_message()}
		ondismiss={() => onchoose('all')}
		actions={choices}
	/>
{/if}

<style>
	.choices {
		display: flex;
		gap: 6px;
	}

	.row {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 6px 6px 6px 12px;
		border-radius: var(--r-md);
		background: var(--container);
		color: var(--on-container);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, var(--accent-text) 14%, transparent);
	}

	.row :global(.glyph) {
		flex: none;
		inline-size: 20px;
		block-size: 20px;
		color: var(--accent-text);
	}

	.text {
		flex: 1;
		display: grid;
		min-inline-size: 0;
	}

	.caption {
		font: 600 0.625rem / 1.3 var(--font-ui);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		opacity: 0.85;
	}

	.course {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.close {
		display: grid;
		flex: none;
		place-items: center;
		inline-size: 36px;
		block-size: 36px;
		border-radius: var(--r-pill);
		color: inherit;
		opacity: 0.75;
		touch-action: manipulation;
	}

	.close :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (hover: hover) {
		.close:hover {
			background: var(--state-hover);
			opacity: 1;
		}
	}

	.close:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}
</style>
