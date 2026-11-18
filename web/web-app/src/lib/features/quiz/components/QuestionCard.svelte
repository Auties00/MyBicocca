<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconChatText from '~icons/ph/chat-centered-text';
	import IconFlag from '~icons/ph/flag';
	import IconFlagFill from '~icons/ph/flag-fill';
	import IconInfo from '~icons/ph/info';
	import IconLightbulb from '~icons/ph/lightbulb';
	import IconSealCheck from '~icons/ph/seal-check';
	import IconWarning from '~icons/ph/warning-circle';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import Textarea from '#lib/ui/Textarea.svelte';
	import type { Status } from '#lib/ui/types.ts';
	import { verdictLabels } from '../format.ts';
	import { verdictOf, type Verdict } from '../model.ts';
	import { htmlToEssay, textToHtml, type Question, type Response } from '../questions.ts';
	import ChoiceAnswer from './ChoiceAnswer.svelte';
	import ClozeAnswer from './ClozeAnswer.svelte';

	interface Props {
		question: Question;
		mode: 'attempt' | 'review';
		response?: Response;
		flagged?: boolean;
		/** While the page saves, loads or submits. */
		disabled?: boolean;
		/** The attempt on the e-learning site, for questions answered there. */
		moodleUrl?: `https://${string}`;
		onanswer?: (name: string, value: string) => void;
		onflag?: () => void;
	}

	let {
		question,
		mode,
		response = {},
		flagged = false,
		disabled = false,
		moodleUrl,
		onanswer,
		onflag
	}: Props = $props();

	const review = $derived(mode === 'review');
	const body = $derived(question.body);
	const info = $derived(body.kind === 'info');
	const verdict = $derived(review ? verdictOf(question.state) : null);
	const STATUS: Record<Verdict, Status> = {
		correct: 'success',
		partial: 'warning',
		wrong: 'danger',
		unanswered: 'neutral',
		pending: 'pending'
	};
	const points = $derived(
		question.maxMark !== null && question.maxMark > 0
			? m.quiz_points({
					count: question.maxMark,
					value: formatNumber(question.maxMark, { maximumFractionDigits: 2 })
				})
			: null
	);
	const score = $derived(
		review && question.mark !== null && question.maxMark !== null && question.maxMark > 0
			? m.quiz_score({
					mark: formatNumber(question.mark, { maximumFractionDigits: 2 }),
					max: formatNumber(question.maxMark, { maximumFractionDigits: 2 })
				})
			: null
	);
	const headingId = $props.id();

	/*
	 * Essays Moodle keeps as HTML are written as plain text and sent as HTML. What the student
	 * types stays as typed: converting it back on every key would eat trailing spaces.
	 */
	let essayDraft = $state<string>();
	const essayText = $derived.by(() => {
		if (body.kind !== 'essay' || body.name === null) return '';
		const value = response[body.name] ?? body.value;
		return body.html ? htmlToEssay(value) : value;
	});

	function answerEssay(name: string, html: boolean, text: string) {
		essayDraft = text;
		onanswer?.(name, html ? textToHtml(text) : text);
	}
</script>

<!-- Focusable from script only: the navigator and the submit check bring the student here. -->
<article
	class={['question', { info, review }, verdict && `verdict-${verdict}`]}
	id="question-{question.slot}"
	aria-labelledby={headingId}
	tabindex="-1"
>
	<header class="head">
		<h2 class="number" id={headingId}>
			{#if info}
				<IconInfo aria-hidden="true" />{m.quiz_information()}
			{:else}
				{m.quiz_question({ number: question.number ?? '' })}
			{/if}
		</h2>
		{#if points && !review}<span class="points">{points}</span>{/if}
		<span class="end">
			{#if verdict}
				<StatusChip status={STATUS[verdict]} label={verdictLabels[verdict]()} size="sm" />
			{/if}
			{#if score}<span class="score">{score}</span>{/if}
			{#if !review && !info && onflag}
				<Button
					size="sm"
					variant={flagged ? 'tonal' : 'ghost'}
					icon={flagged ? IconFlagFill : IconFlag}
					label={m.quiz_flag_question()}
					pressed={flagged}
					{disabled}
					onclick={onflag}
				>
					{m.quiz_flag()}
				</Button>
			{/if}
		</span>
	</header>

	{#if question.prompt}
		<div class="prompt"><TeacherHtml html={question.prompt} /></div>
	{/if}

	{#if body.kind === 'single' || body.kind === 'multi'}
		<ChoiceAnswer
			multi={body.kind === 'multi'}
			options={body.options}
			name={body.kind === 'single' ? body.name : ''}
			{response}
			{review}
			{disabled}
			{onanswer}
		/>
	{:else if body.kind === 'text'}
		{@const gap = body.gap}
		<div class={['text-answer', review && gap.mark]}>
			<TextField
				label={m.quiz_answer_label()}
				value={response[gap.name] ?? gap.value}
				inputmode={gap.numeric ? 'decimal' : 'text'}
				autocomplete="off"
				spellcheck={false}
				readonly={review}
				{disabled}
				oninput={(event) => onanswer?.(gap.name, event.currentTarget.value)}
			/>
		</div>
	{:else if body.kind === 'essay'}
		{#if review || body.name === null}
			<div class="essay-response">
				<p class="caption">{m.quiz_your_answer()}</p>
				{#if body.html ? htmlToEssay(body.value) : body.value.trim()}
					<TeacherHtml html={body.html ? body.value : textToHtml(body.value)} />
				{:else}
					<p class="nothing">{m.quiz_no_answer_given()}</p>
				{/if}
			</div>
		{:else}
			{@const name = body.name}
			<Textarea
				label={m.quiz_essay_label()}
				placeholder={m.quiz_essay_placeholder()}
				value={essayDraft ?? essayText}
				rows={6}
				maxRows={20}
				{disabled}
				oninput={(event) => answerEssay(name, body.html, event.currentTarget.value)}
			/>
		{/if}
	{:else if body.kind === 'cloze'}
		<ClozeAnswer html={body.html} gaps={body.gaps} {response} {review} {disabled} {onanswer} />
	{:else if body.kind === 'unsupported'}
		<div class="unsupported">
			<TeacherHtml html={body.html} />
		</div>
		{#if !review}
			<div class="note">
				<IconWarning class="note-icon" aria-hidden="true" />
				<div class="note-text">
					<p>{m.quiz_unsupported()}</p>
					{#if moodleUrl}
						<Button size="sm" variant="tonal" href={moodleUrl} trailingIcon={IconArrowSquareOut}>
							{m.quiz_open_moodle()}
						</Button>
					{/if}
				</div>
			</div>
		{:else if moodleUrl}
			<!-- What the student placed may only show on the site, as for images to drag onto. -->
			<div class="elsewhere">
				<Button size="sm" variant="ghost" href={moodleUrl} trailingIcon={IconArrowSquareOut}>
					{m.quiz_see_on_moodle()}
				</Button>
			</div>
		{/if}
	{/if}

	{#if question.validation && !review}
		<p class="validation" role="status">
			<IconWarning aria-hidden="true" />{question.validation}
		</p>
	{/if}

	{#if review && (question.rightAnswer || question.feedback || question.comment)}
		<div class="outcome">
			{#if question.rightAnswer}
				<section class="tile right">
					<h3 class="tile-title"><IconSealCheck aria-hidden="true" />{m.quiz_right_answer()}</h3>
					<TeacherHtml html={question.rightAnswer} size="sm" />
				</section>
			{/if}
			{#if question.feedback}
				<section class="tile feedback">
					<h3 class="tile-title"><IconLightbulb aria-hidden="true" />{m.quiz_feedback()}</h3>
					<TeacherHtml html={question.feedback} size="sm" />
				</section>
			{/if}
			{#if question.comment}
				<section class="tile comment">
					<h3 class="tile-title"><IconChatText aria-hidden="true" />{m.quiz_teacher_comment()}</h3>
					<TeacherHtml html={question.comment} size="sm" />
				</section>
			{/if}
		</div>
	{/if}
</article>

<style>
	.question {
		display: grid;
		gap: 16px;
		min-inline-size: 0;
		padding: 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		scroll-margin-block: calc(var(--topbar-h) + 24px);
	}

	.question:focus {
		outline: none;
	}

	@container (width >= 560px) {
		.question {
			padding: 24px 28px;
		}
	}

	.info {
		background: color-mix(in oklch, var(--course-container) 45%, var(--surface));
		box-shadow: none;
	}

	.review.verdict-correct {
		box-shadow:
			var(--elev-1),
			inset 3px 0 0 var(--success);
	}

	.review.verdict-partial {
		box-shadow:
			var(--elev-1),
			inset 3px 0 0 var(--warning);
	}

	.review.verdict-wrong {
		box-shadow:
			var(--elev-1),
			inset 3px 0 0 var(--danger);
	}

	.head {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 6px 12px;
		min-block-size: 32px;
	}

	.number {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.number :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.points,
	.score {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.end {
		display: inline-flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: flex-end;
		gap: 8px 12px;
		margin-inline-start: auto;
	}

	.prompt {
		min-inline-size: 0;
	}

	.prompt :global(.prose) {
		font-size: 1.0625rem;
	}

	/* An item the student dragged into a gap, as it sits there on the site. */
	.prompt :global(mark) {
		padding: 1px 8px;
		border-radius: var(--r-pill);
		background: var(--course-container);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, var(--course-bar) 40%, transparent);
		color: var(--text);
		box-decoration-break: clone;
	}

	.text-answer {
		max-inline-size: 420px;
	}

	.text-answer.correct :global(.box) {
		box-shadow: inset 0 0 0 1.5px var(--success);
		background: var(--success-container);
	}

	.text-answer.incorrect :global(.box) {
		box-shadow: inset 0 0 0 1.5px var(--danger);
		background: var(--danger-container);
	}

	.text-answer.partial :global(.box) {
		box-shadow: inset 0 0 0 1.5px var(--warning);
		background: var(--warning-container);
	}

	.essay-response {
		display: grid;
		gap: 6px;
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
	}

	.caption {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.nothing {
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}

	.unsupported {
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		opacity: 0.85;
	}

	/* What the student would drag, one chip each. */
	.unsupported :global(ul) {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
		margin: 8px 0 0;
		padding: 0;
		list-style: none;
	}

	.unsupported :global(li) {
		margin: 0;
		padding: 4px 12px;
		border-radius: var(--r-pill);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		font: var(--type-ui);
	}

	.note {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: 10px 12px;
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--warning-container);
		color: var(--on-warning-container);
		font: var(--type-ui);
	}

	.note :global(.note-icon) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.elsewhere {
		margin-block-start: -8px;
	}

	.note-text {
		display: grid;
		justify-items: start;
		gap: 10px;
	}

	.validation {
		display: flex;
		align-items: center;
		gap: 6px;
		color: var(--danger);
		font: var(--type-meta);
		font-weight: 550;
	}

	.validation :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		flex-shrink: 0;
	}

	.outcome {
		display: grid;
		gap: 2px;
	}

	.tile {
		display: grid;
		gap: 6px;
		padding: 14px 16px;
		border-radius: var(--r-2xs);
	}

	.tile:first-child {
		border-start-start-radius: var(--r-md);
		border-start-end-radius: var(--r-md);
	}

	.tile:last-child {
		border-end-start-radius: var(--r-md);
		border-end-end-radius: var(--r-md);
	}

	.tile-title {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.tile-title :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.right {
		background: var(--gold-container);
	}

	.right .tile-title {
		color: var(--on-gold-container);
	}

	.feedback {
		background: var(--surface-sunken);
	}

	.feedback .tile-title {
		color: var(--text-muted);
	}

	.comment {
		background: var(--info-container);
	}

	.comment .tile-title {
		color: var(--on-info-container);
	}
</style>
