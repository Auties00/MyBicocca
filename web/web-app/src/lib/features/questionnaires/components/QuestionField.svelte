<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import { m } from '#lib/paraglide/messages.js';
	import TextField from '#lib/ui/TextField.svelte';
	import Textarea from '#lib/ui/Textarea.svelte';
	import {
		isAnswered,
		wordsMissing,
		type Answer,
		type AnswerOption,
		type Question
	} from '../page.ts';
	import { isTypingTarget } from '#lib/ui/platform.ts';

	interface Props {
		question: Question;
		/** Its place on the page, from 1. */
		number: number;
		answer: Answer;
		/** A blocked "Next" left it unanswered, or its "other" unexplained. */
		invalid: boolean;
		onpick: (optionId: number) => void;
		ontext: (text: string) => void;
	}

	let { question, number, answer, invalid, onpick, ontext }: Props = $props();

	const id = $props.id();
	const textId = `${id}-text`;
	const noteId = `${id}-note`;
	const errorId = `${id}-error`;

	const kind = $derived(question.kind);
	const multiple = $derived(kind.type === 'multiple');
	const numeric = $derived(kind.type === 'scale' && kind.numeric);
	/* An agreement scale reads left to right where there is room; other choices stay a list. */
	const row = $derived(kind.type === 'scale' && !kind.numeric && question.options.length <= 5);
	const max = $derived(kind.type === 'multiple' ? kind.max : null);
	const full = $derived(max !== null && max > 0 && answer.selected.length >= max);
	const pickedOther = $derived(
		question.options.find((option) => option.needsText && answer.selected.includes(option.id))
	);
	/* An "other" option picked without its words: the message goes under its field. */
	const unexplained = $derived(invalid && wordsMissing(question, answer));
	const describedby = $derived(
		[question.note && noteId, invalid && !unexplained && errorId].filter(Boolean).join(' ') ||
			undefined
	);

	const optionLabel = (option: AnswerOption) => option.text || m.questionnaires_other_option();

	/** 1 to 9, and 0 for the tenth: the step with that label, or else in that place. */
	function digits(event: KeyboardEvent) {
		if (event.defaultPrevented || event.ctrlKey || event.metaKey || event.altKey) return;
		if (!/^[0-9]$/.test(event.key)) return;
		if (isTypingTarget(event.target)) return;
		const wanted = event.key === '0' ? '10' : event.key;
		const option =
			question.options.find((item) => item.text === wanted) ??
			(numeric ? undefined : question.options[Number(wanted) - 1]);
		if (!option) return;
		event.preventDefault();
		if (!answer.selected.includes(option.id) || multiple) onpick(option.id);
		const group = event.currentTarget as HTMLElement;
		group.querySelector<HTMLInputElement>(`input[value="${option.id}"]`)?.focus();
	}
</script>

<div
	class={['question', { invalid, answered: isAnswered(question, answer) }]}
	data-question={question.id}
>
	<div class="head">
		<span class="number" aria-hidden="true">{number}</span>
		<div class="words">
			<p class="text" id={textId}>{question.text}</p>
			{#if question.note}<p class="note" id={noteId}>{question.note}</p>{/if}
			{#if kind.type === 'multiple' && !question.note}
				<p class="note">
					{max !== null && max > 0
						? m.questionnaires_choose_up_to({ count: max })
						: m.questionnaires_choose_many()}
				</p>
			{/if}
		</div>
		{#if !question.mandatory}<span class="optional">{m.questionnaires_optional()}</span>{/if}
	</div>

	{#if kind.type === 'text'}
		<Textarea
			label={question.text}
			hideLabel
			rows={3}
			bind:value={() => answer.text, ontext}
			placeholder={m.questionnaires_free_text_placeholder()}
			error={invalid ? m.questionnaires_missing_answer() : undefined}
		/>
	{:else}
		<div
			class={['options', { numeric, row }]}
			role={multiple ? 'group' : 'radiogroup'}
			aria-labelledby={textId}
			aria-describedby={describedby}
			aria-required={!multiple && question.mandatory ? true : undefined}
			aria-invalid={invalid || undefined}
			onkeydown={digits}
		>
			{#each question.options as option, index (option.id)}
				{@const selected = answer.selected.includes(option.id)}
				{@const end =
					index === 0
						? m.questionnaires_scale_disagree()
						: index === question.options.length - 1
							? m.questionnaires_scale_agree()
							: null}
				<label class={['option', { selected }]}>
					<input
						class="native"
						type={multiple ? 'checkbox' : 'radio'}
						name={`q${question.id}`}
						value={option.id}
						checked={selected}
						disabled={multiple && full && !selected}
						onclick={() => onpick(option.id)}
					/>
					{#if numeric}
						<span class="step"
							>{option.text}{#if end}<span class="sr-only">, {end}</span>{/if}</span
						>
					{:else}
						<span class={['knob', { square: multiple }]} aria-hidden="true">
							{#if multiple}<IconCheck />{/if}
						</span>
						<span class="label">{optionLabel(option)}</span>
					{/if}
				</label>
			{/each}
		</div>
		{#if numeric}
			<div class="anchors" aria-hidden="true">
				<span>{m.questionnaires_scale_disagree()}</span>
				<span>{m.questionnaires_scale_agree()}</span>
			</div>
		{/if}
		{#if pickedOther}
			<div class="specify" data-specify>
				<TextField
					type="text"
					label={m.questionnaires_specify_label({ option: optionLabel(pickedOther) })}
					hideLabel
					bind:value={() => answer.text, ontext}
					placeholder={m.questionnaires_specify_placeholder()}
					error={unexplained ? m.questionnaires_missing_text() : undefined}
				/>
			</div>
		{/if}
		{#if invalid && !unexplained}
			<p class="error" id={errorId}>
				<IconWarningCircle aria-hidden="true" />
				{m.questionnaires_missing_answer()}
			</p>
		{/if}
	{/if}
</div>

<style>
	.question {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5) var(--sp-5) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		container-type: inline-size;
		scroll-margin-block: calc(var(--shell-inset-top) + var(--sp-4)) 120px;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	.invalid {
		box-shadow:
			0 0 0 1.5px var(--danger),
			var(--elev-1);
	}

	.head {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: start;
		gap: var(--sp-3);
	}

	.number {
		display: grid;
		place-items: center;
		min-inline-size: 26px;
		block-size: 26px;
		padding-inline: 6px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	.answered .number {
		background: var(--container);
		color: var(--on-container);
	}

	.invalid .number {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.words {
		display: grid;
		gap: 4px;
		padding-block-start: 2px;
	}

	.text {
		font: var(--type-body-strong);
		text-wrap: pretty;
	}

	.note {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	.optional {
		margin-block-start: 2px;
		padding: 2px 8px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-label);
		white-space: nowrap;
	}

	/* The inputs stay real and focusable; the label draws them. */
	.native {
		position: absolute;
		inline-size: 1px;
		block-size: 1px;
		opacity: 0;
		pointer-events: none;
	}

	.options {
		display: grid;
		gap: 6px;
	}

	.option {
		position: relative;
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		min-block-size: var(--hit-min);
		padding: 10px 14px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		color: var(--text);
		font: var(--type-ui);
		cursor: pointer;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			box-shadow var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.option:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.option:has(.native:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.option.selected {
		background: var(--container);
		box-shadow: inset 0 0 0 1.5px color-mix(in oklch, var(--accent) 55%, transparent);
		color: var(--on-container);
	}

	.option:has(.native:disabled) {
		cursor: not-allowed;
		opacity: 0.5;
	}

	.label {
		flex: 1;
		min-inline-size: 0;
		text-wrap: pretty;
	}

	.knob {
		position: relative;
		display: grid;
		flex: none;
		place-items: center;
		inline-size: 20px;
		block-size: 20px;
		border-radius: 50%;
		box-shadow: inset 0 0 0 2px var(--border-strong);
		color: var(--on-accent);
		transition:
			box-shadow var(--dur-fast) var(--ease-out),
			background-color var(--dur-fast) var(--ease-out);
	}

	.knob.square {
		border-radius: 6px;
	}

	.knob::after {
		content: '';
		inline-size: 8px;
		block-size: 8px;
		border-radius: 50%;
		background: var(--on-accent);
		transform: scale(0);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.knob.square::after {
		display: none;
	}

	.knob :global(svg) {
		inline-size: 13px;
		block-size: 13px;
		transform: scale(0);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.selected .knob {
		background: var(--accent);
		box-shadow: none;
	}

	.selected .knob::after,
	.selected .knob :global(svg) {
		transform: scale(1);
	}

	/* An agreement scale from 520 px: the steps side by side, the knob over each label. */
	@container (width >= 520px) {
		.row {
			grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
			grid-auto-flow: column;
		}

		.row .option {
			flex-direction: column;
			justify-content: flex-start;
			gap: var(--sp-2);
			padding: 14px 10px 12px;
			text-align: center;
		}

		.row .label {
			flex: none;
			font: var(--type-label);
			font-size: 0.8125rem;
		}
	}

	/* A numeric scale: round steps in one row, or two rows of five on a phone. */
	.numeric {
		grid-template-columns: repeat(5, minmax(0, 1fr));
		gap: 8px;
	}

	@container (width >= 460px) {
		.numeric {
			grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
			grid-auto-flow: column;
			gap: 6px;
		}
	}

	.numeric .option {
		justify-content: center;
		aspect-ratio: 1;
		min-block-size: 0;
		max-inline-size: 52px;
		justify-self: center;
		inline-size: 100%;
		padding: 0;
		border-radius: 50%;
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	.numeric .option.selected {
		background: var(--accent);
		box-shadow: none;
		color: var(--on-accent);
		animation: pop var(--dur-spring) var(--spring-pop);
	}

	@keyframes pop {
		from {
			transform: scale(0.86);
		}
	}

	.anchors {
		display: flex;
		justify-content: space-between;
		gap: var(--sp-4);
		margin-block-start: calc(-1 * var(--sp-2));
		color: var(--text-muted);
		font: var(--type-label);
	}

	.specify {
		margin-block-start: calc(-1 * var(--sp-1));
	}

	.error {
		display: flex;
		align-items: center;
		gap: 6px;
		margin-block-start: calc(-1 * var(--sp-1));
		color: var(--danger);
		font: var(--type-ui);
	}

	.error :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
	}

	@media (prefers-reduced-motion: reduce) {
		.numeric .option.selected {
			animation: none;
		}
	}
</style>
