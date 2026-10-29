<script lang="ts">
	import { untrack } from 'svelte';
	import IconPlus from '~icons/ph/plus-bold';
	import IconCheck from '~icons/ph/check-bold';
	import { m } from '#lib/paraglide/messages.js';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import type { CatalogCourse } from '../../catalog/model.ts';
	import { courseHref } from '../../links.ts';

	interface Props {
		course: CatalogCourse;
		enrolled: boolean;
		offline: boolean;
		onenrol: (course: CatalogCourse) => void;
		/** What the button says to assistive tech; "Iscriviti a <name>" by default. */
		label?: string;
	}

	let { course, enrolled, offline, onenrol, label }: Props = $props();

	/* The check pops when this button turns into it, not whenever an enrolled row is drawn. */
	const idleAtFirst = untrack(() => !enrolled);

	const enrolLabel = $derived(
		offline
			? m.elearning_enrol_offline()
			: (label ?? m.elearning_enrol_course({ name: course.name }))
	);
</script>

{#if enrolled}
	<a
		class={['knob', 'enrolled', { pop: idleAtFirst }]}
		href={courseHref(course.id)}
		data-enrolled={course.id}
		aria-label={m.elearning_enrolled_open({ name: course.name })}
		{@attach tooltip(m.elearning_open_course())}
	>
		<IconCheck aria-hidden="true" />
		<span class="text">{m.elearning_enrolled()}</span>
	</a>
{:else}
	<button
		type="button"
		class="knob"
		aria-label={enrolLabel}
		aria-disabled={offline}
		{@attach tooltip(offline ? m.elearning_enrol_offline() : m.elearning_enrol())}
		onclick={() => {
			if (!offline) onenrol(course);
		}}
	>
		<IconPlus aria-hidden="true" />
	</button>
{/if}

<style>
	/* Tonal: with a knob on every row, a solid fill would outshout the names it serves. */
	.knob {
		--fill: light-dark(
			oklch(92.5% calc(var(--area-c) * 0.45) var(--area-h)),
			oklch(33% calc(var(--area-c) * 0.5) var(--area-h))
		);
		--fill-hover: light-dark(
			oklch(89.5% calc(var(--area-c) * 0.55) var(--area-h)),
			oklch(37% calc(var(--area-c) * 0.6) var(--area-h))
		);
		--on-fill: light-dark(
			oklch(40% var(--area-c) var(--area-h)),
			oklch(86% calc(var(--area-c) * 0.6) var(--area-h))
		);
		position: relative;
		display: inline-grid;
		grid-auto-flow: column;
		place-items: center;
		gap: 6px;
		min-inline-size: 38px;
		block-size: 38px;
		border-radius: var(--r-pill);
		background: var(--fill);
		color: var(--on-fill);
		text-decoration: none;
		cursor: pointer;
		touch-action: manipulation;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	.knob::after {
		content: '';
		position: absolute;
		inset: -3px;
	}

	.knob :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.knob[aria-disabled='true'] {
		opacity: 0.4;
		cursor: not-allowed;
	}

	@media (hover: hover) {
		.knob:hover:not([aria-disabled='true']) {
			background: var(--fill-hover);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.knob:active:not([aria-disabled='true']) {
		transform: scale(0.9);
		transition-duration: var(--dur-instant);
	}

	.enrolled {
		padding-inline: 10px 12px;
		background: var(--success-container);
		color: var(--on-success-container);
		font: var(--type-ui-strong);
	}

	.enrolled.pop {
		animation: pop var(--dur-spring) var(--spring-pop);
	}

	.enrolled :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@container (width < 420px) {
		.enrolled {
			padding-inline: 0;
			inline-size: 38px;
		}

		.text {
			display: none;
		}
	}

	@keyframes pop {
		from {
			transform: scale(0.6);
			opacity: 0;
		}
	}
</style>
