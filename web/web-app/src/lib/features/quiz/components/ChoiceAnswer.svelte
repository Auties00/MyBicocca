<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import IconMinus from '~icons/ph/minus-bold';
	import IconX from '~icons/ph/x-bold';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { m } from '#lib/paraglide/messages.js';
	import type { ChoiceOption, Response } from '../questions.ts';

	interface Props {
		multi: boolean;
		options: readonly ChoiceOption[];
		/** The radios' shared name, for single choice. */
		name: string;
		response: Response;
		/** Read-only, with Moodle's marks: a review. */
		review: boolean;
		disabled?: boolean;
		onanswer?: (name: string, value: string) => void;
	}

	let { multi, options, name, response, review, disabled = false, onanswer }: Props = $props();

	const id = $props.id();

	function isChecked(option: ChoiceOption): boolean {
		if (multi) return (response[option.name] ?? (option.checked ? '1' : '0')) === '1';
		const chosen = response[name] ?? options.find((candidate) => candidate.checked)?.value;
		return chosen === option.value;
	}

	const anyChecked = $derived(options.some(isChecked));
</script>

<fieldset class="choices" disabled={review || disabled}>
	<legend class="legend">{multi ? m.quiz_choose_many() : m.quiz_choose_one()}</legend>
	<div class="options">
		{#each options as option, index (`${option.name}-${option.value}-${index}`)}
			{@const checked = isChecked(option)}
			<label
				class={[
					'option',
					{ checked, correct: review && option.mark === 'correct' },
					{ incorrect: review && option.mark === 'incorrect' },
					{ partial: review && option.mark === 'partial' }
				]}
			>
				<input
					class="input"
					type={multi ? 'checkbox' : 'radio'}
					name={multi ? option.name : `${id}-${name}`}
					value={option.value}
					{checked}
					onchange={(event) => {
						if (multi) onanswer?.(option.name, event.currentTarget.checked ? '1' : '0');
						else onanswer?.(name, option.value);
					}}
				/>
				<span class={['knob', multi ? 'square' : 'round']} aria-hidden="true">
					{#if multi}<IconCheck class="tick" />{:else}<span class="dot"></span>{/if}
				</span>
				<span class="label"><TeacherHtml html={option.label} /></span>
				{#if review && option.mark === 'correct'}
					<span class="mark good"
						><IconCheck aria-hidden="true" /><span class="sr-only">{m.quiz_verdict_correct()}</span
						></span
					>
				{:else if review && option.mark === 'partial'}
					<span class="mark half"
						><IconMinus aria-hidden="true" /><span class="sr-only">{m.quiz_verdict_partial()}</span
						></span
					>
				{:else if review && option.mark === 'incorrect'}
					<span class="mark bad"
						><IconX aria-hidden="true" /><span class="sr-only">{m.quiz_verdict_wrong()}</span></span
					>
				{/if}
			</label>
		{/each}
	</div>
	{#if !multi && !review && anyChecked}
		<button type="button" class="clear" {disabled} onclick={() => onanswer?.(name, '-1')}>
			{m.quiz_clear_choice()}
		</button>
	{/if}
</fieldset>

<style>
	.choices {
		display: grid;
		gap: 8px;
		min-inline-size: 0;
		margin: 0;
		padding: 0;
		border: 0;
	}

	.legend {
		margin-block-end: 8px;
		padding: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.options {
		display: grid;
		gap: 8px;
	}

	.option {
		position: relative;
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: start;
		gap: 14px;
		min-block-size: var(--hit-min);
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--border);
		cursor: pointer;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			box-shadow var(--dur-fast) var(--ease-out);
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
	}

	@media (hover: hover) {
		.choices:not(:disabled) .option:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	/* The choice is state: the course's colour, as the navigator's answered tiles. */
	.checked,
	.choices:not(:disabled) .checked:hover {
		background: var(--course-container);
		box-shadow: inset 0 0 0 1.5px var(--course-bar);
	}

	.choices:disabled .option {
		cursor: default;
	}

	.correct,
	.choices:disabled .correct {
		background: var(--success-container);
		box-shadow: inset 0 0 0 1.5px var(--success);
	}

	.incorrect,
	.choices:disabled .incorrect {
		background: var(--danger-container);
		box-shadow: inset 0 0 0 1.5px var(--danger);
	}

	.partial {
		background: var(--warning-container);
		box-shadow: inset 0 0 0 1.5px var(--warning);
	}

	.input {
		position: absolute;
		opacity: 0;
		inline-size: 1px;
		block-size: 1px;
	}

	.knob {
		display: grid;
		place-items: center;
		inline-size: 24px;
		block-size: 24px;
		margin-block-start: 1px;
		background: var(--surface);
		box-shadow: inset 0 0 0 1.5px var(--border-strong);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.round {
		border-radius: 50%;
	}

	.square {
		border-radius: 7px;
	}

	.knob :global(.tick),
	.dot {
		color: var(--surface);
		transform: scale(0);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.knob :global(.tick) {
		inline-size: 14px;
		block-size: 14px;
	}

	/* Radios stay round, with a dot; only checkboxes are squares with a tick. */
	.dot {
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: currentColor;
	}

	.input:checked + .knob {
		background: var(--course-bar);
		box-shadow: none;
	}

	.input:checked + .knob :global(.tick),
	.input:checked + .knob .dot {
		transform: scale(1);
	}

	.input:focus-visible + .knob {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.correct .input:checked + .knob {
		background: var(--success-solid);
	}

	.incorrect .input:checked + .knob {
		background: var(--danger-solid);
	}

	.correct .knob :global(.tick),
	.correct .dot,
	.incorrect .knob :global(.tick),
	.incorrect .dot {
		color: oklch(100% 0 0);
	}

	.label {
		min-inline-size: 0;
		padding-block-start: 1px;
	}

	.label :global(.prose) {
		font-size: 0.9375rem;
	}

	.mark {
		display: grid;
		place-items: center;
		inline-size: 24px;
		block-size: 24px;
		border-radius: 50%;
	}

	.mark :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.good {
		background: var(--success-solid);
		color: oklch(100% 0 0);
	}

	.bad {
		background: var(--danger-solid);
		color: oklch(100% 0 0);
	}

	.half {
		background: var(--warning-solid);
		color: var(--on-warning-solid);
	}

	.clear {
		justify-self: start;
		min-block-size: var(--hit-min);
		padding: 4px 8px;
		margin-inline-start: -8px;
		border: 0;
		border-radius: var(--r-sm);
		background: none;
		color: var(--course-ink);
		font: var(--type-ui);
		cursor: pointer;
	}

	@media (hover: hover) {
		.clear:hover {
			background: var(--state-hover);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.knob,
		.knob :global(.tick),
		.dot {
			transition: none;
		}
	}
</style>
