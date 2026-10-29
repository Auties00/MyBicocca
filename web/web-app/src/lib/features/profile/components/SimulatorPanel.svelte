<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconEquals from '~icons/ph/equals-bold';
	import IconTrendDown from '~icons/ph/trend-down-bold';
	import IconTrendUp from '~icons/ph/trend-up-bold';
	import IconX from '~icons/ph/x';
	import { formatAverage } from '#lib/format/grades.ts';
	import { formatCredits } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import {
		LAUDE_GRADE,
		parseCredits,
		parseGrade,
		parseTarget,
		projectAverage,
		requiredGrade,
		type AverageKind,
		type SimulationBase
	} from '../simulator.ts';

	interface Props {
		base: SimulationBase;
		onkind: (kind: AverageKind) => void;
		/** The exam picked from the list, whose credits fill the field. */
		exam?: { name: string; credits: number };
		onclearexam?: () => void;
		/** The credits field, which a picked exam fills. */
		credits: string;
	}

	let { base, onkind, exam, onclearexam, credits = $bindable() }: Props = $props();

	const ids = $props.id();

	let grade = $state('');
	/* Set for the grade on screen: editing the grade clears it. */
	let laude = $state(false);
	let target = $state('');

	const weighted = $derived(base.kind === 'weighted');
	const parsed = $derived(parseGrade(grade));
	/* A 30 with the laude switched on counts 31, as typing 31 does (the chart's convention). */
	const value = $derived.by(() => {
		if (parsed.kind !== 'valid') return undefined;
		return parsed.value === 30 && laude ? LAUDE_GRADE : parsed.value;
	});
	const creditsInput = $derived(parseCredits(credits));
	const cfu = $derived(creditsInput.kind === 'valid' ? creditsInput.value : undefined);
	const projected = $derived(
		value === undefined ? null : projectAverage(base, value, weighted ? cfu : undefined)
	);
	const delta = $derived(
		projected === null || base.current === null ? null : projected - base.current
	);
	/* Two decimals is what the average shows: a change below that reads as none. */
	const trend = $derived(
		delta === null ? null : Math.abs(delta) < 0.005 ? 'same' : delta > 0 ? 'up' : 'down'
	);

	const goal = $derived(parseTarget(target));
	const needed = $derived(
		goal === null ? null : requiredGrade(base, goal, weighted ? cfu : undefined)
	);

	const basis = $derived.by(() => {
		const { rollup } = base;
		if (rollup === null || base.current === null) return undefined;
		return weighted
			? m.profile_hyp_subtitle_weighted({ credits: formatCredits(rollup.gradedCredits) })
			: m.profile_hyp_subtitle_arithmetic({ count: rollup.gradedExams });
	});

	const gradeError = $derived(
		parsed.kind === 'low'
			? m.profile_hyp_grade_invalid()
			: parsed.kind === 'high'
				? m.profile_hyp_grade_too_high()
				: undefined
	);

	const prompt = $derived.by(() => {
		if (base.current === null) return m.profile_hyp_no_average();
		if (value === undefined) return m.profile_hyp_prompt();
		if (weighted && cfu === undefined) return m.profile_hyp_prompt_credits();
		return undefined;
	});

	const answer = $derived.by(() => {
		if (target.trim() === '') return undefined;
		if (goal === null) return { tone: 'danger', text: m.profile_target_invalid() };
		if (needed === null) {
			return base.current === null
				? { tone: 'neutral', text: m.profile_hyp_no_average() }
				: { tone: 'neutral', text: m.profile_target_needs_credits() };
		}
		const amount = weighted && cfu !== undefined ? formatCredits(cfu) : undefined;
		switch (needed.kind) {
			case 'any':
				return { tone: 'success', text: m.profile_target_any({ target: formatAverage(goal) }) };
			case 'unreachable':
				return { tone: 'danger', text: m.profile_target_unreachable() };
			case 'grade':
				if (needed.value === LAUDE_GRADE) {
					return {
						tone: 'gold',
						text: amount
							? m.profile_target_laude_credits({ credits: amount })
							: m.profile_target_laude()
					};
				}
				return {
					tone: 'accent',
					text: amount
						? m.profile_target_grade_credits({ grade: needed.value, credits: amount })
						: m.profile_target_grade({ grade: needed.value })
				};
		}
	});

	const digits = (event: InputEvent) => {
		if (event.data !== null && /\D/.test(event.data)) event.preventDefault();
	};
</script>

<div class="panel">
	<SegmentedControl
		label={m.profile_simulator_kind()}
		full
		options={[
			{ value: 'weighted', label: m.profile_simulator_weighted() },
			{ value: 'arithmetic', label: m.profile_simulator_arithmetic() }
		]}
		value={base.kind}
		onchange={onkind}
	/>

	<section class="result" aria-labelledby="{ids}-result">
		<div class="heading">
			<h2 class="caption" id="{ids}-result">
				{weighted ? m.profile_weighted_average() : m.profile_arithmetic_average()}
			</h2>
			{#if basis}<p class="basis">{basis}</p>{/if}
		</div>
		<div class={['figures', { projecting: projected !== null }]}>
			<p class="figure now">
				<span class="sr-only">{m.profile_hyp_now()}</span>
				{base.current === null ? '—' : formatAverage(base.current)}
			</p>
			<span class="arrow" aria-hidden="true"><IconArrowRight /></span>
			<p class="figure then" aria-hidden={projected === null}>
				<span class="sr-only">{m.profile_hyp_then()}</span>
				{projected === null ? '' : formatAverage(projected)}
			</p>
		</div>
		<div class="status" aria-live="polite">
			{#if delta !== null && trend !== null}
				<p class={['delta', trend]}>
					{#if trend === 'up'}<IconTrendUp aria-hidden="true" />
					{:else if trend === 'down'}<IconTrendDown aria-hidden="true" />
					{:else}<IconEquals aria-hidden="true" />{/if}
					<span aria-hidden="true">
						{trend === 'same' ? '±' : trend === 'up' ? '+' : '−'}{formatAverage(Math.abs(delta))}
					</span>
					<span class="sr-only">
						{trend === 'up'
							? m.profile_hyp_delta_up({ delta: formatAverage(delta) })
							: trend === 'down'
								? m.profile_hyp_delta_down({ delta: formatAverage(-delta) })
								: m.profile_hyp_delta_same()}
					</span>
				</p>
			{:else if prompt}
				<p class="prompt">{prompt}</p>
			{/if}
		</div>
	</section>

	{#if exam}
		<p class="exam">
			<span class="exam-name">{exam.name}</span>
			<span class="exam-credits">{formatCredits(exam.credits)}</span>
			{#if onclearexam}
				<button
					type="button"
					class="clear"
					onclick={onclearexam}
					aria-label={m.profile_pick_clear()}
				>
					<IconX aria-hidden="true" />
				</button>
			{/if}
		</p>
	{/if}

	<div class={['inputs', { weighted }]}>
		<TextField
			label={m.profile_hyp_grade_label()}
			bind:value={
				() => grade,
				(next) => {
					grade = next;
					laude = false;
				}
			}
			inputmode="numeric"
			pattern="[0-9]*"
			maxlength={2}
			autocomplete="off"
			placeholder={m.profile_hyp_grade_placeholder()}
			error={gradeError}
			onbeforeinput={digits}
		/>
		{#if weighted}
			<TextField
				label={m.profile_hyp_cfu_label()}
				bind:value={credits}
				inputmode="numeric"
				pattern="[0-9]*"
				maxlength={2}
				autocomplete="off"
				placeholder={m.profile_hyp_cfu_placeholder()}
				error={creditsInput.kind === 'zero' ? m.profile_hyp_cfu_invalid() : undefined}
				onbeforeinput={digits}
			/>
		{/if}
	</div>
	<Switch
		name="laude"
		label={m.profile_hyp_laude()}
		description={m.profile_hyp_laude_hint()}
		checked={value === LAUDE_GRADE}
		disabled={parsed.kind !== 'valid' || parsed.value < 30}
		onchange={(event) => {
			laude = event.currentTarget.checked;
			if (!laude && grade.trim() === String(LAUDE_GRADE)) grade = '30';
			else if (laude) grade = '30';
		}}
	/>

	<section class="target" aria-labelledby="{ids}-target">
		<h2 class="title" id="{ids}-target">{m.profile_target_title()}</h2>
		<TextField
			label={m.profile_target_label()}
			bind:value={target}
			inputmode="decimal"
			maxlength={5}
			autocomplete="off"
			placeholder={m.profile_target_placeholder()}
		/>
		<div class="answer-slot" aria-live="polite">
			{#if answer}
				<p class={['answer', answer.tone]}>{answer.text}</p>
			{/if}
		</div>
	</section>
</div>

<style>
	.panel {
		display: grid;
		gap: var(--sp-5);
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.caption,
	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.heading {
		display: grid;
		gap: 2px;
	}

	.result {
		display: grid;
		gap: 10px;
	}

	.figures {
		display: flex;
		align-items: baseline;
		flex-wrap: wrap;
		gap: 4px 12px;
		min-block-size: 3.25rem;
	}

	.figure {
		font: var(--type-numeral-l);
		font-size: 2.75rem;
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
		transition:
			opacity var(--dur-base) var(--ease-out),
			transform var(--dur-slow) var(--spring-standard);
	}

	.arrow,
	.then {
		opacity: 0;
		transform: translateX(-8px);
	}

	.arrow {
		display: inline-flex;
		align-self: center;
		color: var(--text-subtle);
		transition:
			opacity var(--dur-base) var(--ease-out),
			transform var(--dur-slow) var(--spring-standard);
	}

	.arrow :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.projecting .now {
		color: var(--text-muted);
	}

	.projecting .arrow,
	.projecting .then {
		opacity: 1;
		transform: none;
	}

	.then {
		color: var(--accent-text);
	}

	.status {
		min-block-size: 28px;
	}

	.delta {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		padding: 3px 10px 3px 8px;
		border-radius: var(--r-pill);
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	.delta :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.delta.up {
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.delta.down {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.delta.same {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.prompt,
	.basis {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.prompt {
		padding-block: 4px;
		text-wrap: pretty;
	}

	.exam {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 6px 6px 6px 14px;
		border-radius: var(--r-pill);
		background: var(--container);
		color: var(--on-container);
		font: var(--type-ui);
	}

	.exam-name {
		flex: 1;
		min-inline-size: 0;
		overflow: hidden;
		font-weight: 600;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.exam-credits {
		opacity: 0.8;
		white-space: nowrap;
	}

	.clear {
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		border: 0;
		border-radius: 50%;
		background: transparent;
		color: inherit;
		cursor: pointer;
	}

	.clear :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		.clear:hover {
			background: var(--state-hover);
		}
	}

	.inputs {
		display: grid;
		gap: var(--sp-3);
		align-items: start;
	}

	.inputs.weighted {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}

	.target {
		display: grid;
		gap: var(--sp-3);
		padding-block-start: var(--sp-5);
		border-block-start: 1px solid var(--hairline);
	}

	.answer-slot {
		min-block-size: 1.5rem;
	}

	.answer {
		padding: 10px 14px;
		border-radius: var(--r-md);
		font: var(--type-ui);
		text-wrap: pretty;
	}

	.answer.accent {
		background: var(--container);
		color: var(--on-container);
	}

	.answer.gold {
		background: var(--gold-container);
		color: var(--on-gold-container);
	}

	.answer.success {
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.answer.danger {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.answer.neutral {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	@media (prefers-reduced-motion: reduce) {
		.figure,
		.arrow {
			transition: opacity var(--dur-fast) linear;
		}

		.arrow,
		.then {
			transform: none;
		}
	}
</style>
