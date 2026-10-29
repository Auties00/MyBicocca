<script lang="ts">
	import IconBookOpen from '~icons/ph/book-open';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { isOpen, type Career } from '#lib/data/account.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { careerTitle } from '#lib/format/names.ts';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { levelLabel, statusLabel } from '../careerLabels.ts';

	interface Props {
		career: Career;
		/** The radio group's name. */
		name: string;
		checked: boolean;
		onselect: () => void;
	}

	let { career, name, checked, onselect }: Props = $props();

	const level = $derived(levelLabel(career.level));
	const status = $derived(statusLabel(career.status));
</script>

<label class={['option', { checked, ended: !isOpen(career.status) }]}>
	<input class="input" type="radio" {name} value={career.id} {checked} onchange={onselect} />
	<span class={['pip', { blank: !level }]} aria-hidden="true">
		{#if level}{level.short}{:else}<IconBookOpen />{/if}
	</span>
	<span class="text">
		<span class="title">
			{career.description ? careerTitle(career) : m.auth_career_fallback({ id: career.id })}
			{#if level}<span class="sr-only">({level.full})</span>{/if}
		</span>
		<span class="detail code">
			{m.auth_career_details({
				number: career.studentNumber,
				year: academicYearLabel(career.academicYear)
			})}
		</span>
		{#if status}
			<span class="status">
				<StatusChip status={status.tone} label={status.label} icon={status.icon} size="sm" />
			</span>
		{/if}
	</span>
	<span class="check" aria-hidden="true"><IconCheckCircle /></span>
</label>

<style>
	.option {
		position: relative;
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: start;
		gap: 14px;
		padding: 16px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--border);
		cursor: pointer;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	@media (hover: hover) {
		.option:not(.checked):hover {
			background: var(--surface-hover);
			transition-duration: 0s, var(--dur-spring);
		}
	}

	.option:active {
		transform: scale(0.985);
	}

	.checked {
		background: var(--container);
		color: var(--on-container);
		box-shadow: inset 0 0 0 1.5px var(--accent);
	}

	.option:has(.input:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.option:has(.input:disabled) {
		cursor: default;
	}

	/*
	 * The radio covers the card, so a card focused by the arrow keys scrolls fully into view, clear
	 * of the sticky Continue bar below.
	 */
	.input {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		margin: 0;
		opacity: 0;
		cursor: inherit;
		scroll-margin-block: 16px calc(88px + var(--safe-bottom));
	}

	.pip {
		display: grid;
		place-items: center;
		min-inline-size: 36px;
		block-size: 36px;
		padding-inline: 6px;
		border-radius: var(--r-sm);
		background: var(--container);
		color: var(--accent-text);
		font: 650 0.875rem / 1 var(--font-ui);
		letter-spacing: 0.02em;
	}

	.pip.blank {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.checked .pip {
		background: var(--accent);
		color: var(--on-accent);
	}

	.pip :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		padding-block-start: 1px;
	}

	.title {
		display: -webkit-box;
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 3;
		line-clamp: 3;
		overflow-wrap: anywhere;
	}

	.detail {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.checked .detail {
		color: inherit;
		opacity: 0.8;
	}

	.status {
		margin-block-start: 6px;
	}

	.check {
		display: grid;
		color: var(--accent);
		opacity: 0;
		transform: scale(0.6);
		transition:
			opacity var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.check :global(svg) {
		inline-size: 24px;
		block-size: 24px;
	}

	.checked .check {
		opacity: 1;
		transform: none;
	}

	/* Ended careers recede by colour, not opacity, so their text keeps its contrast. */
	.ended:not(.checked) .title {
		color: var(--text-muted);
	}

	.ended:not(.checked) .pip {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	@media (prefers-reduced-motion: reduce) {
		.option:active {
			transform: none;
		}
	}
</style>
