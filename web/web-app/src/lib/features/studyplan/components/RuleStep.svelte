<script lang="ts">
	import IconInfo from '~icons/ph/info';
	import IconProhibit from '~icons/ph/prohibit';
	import { italianTitleCase } from '#lib/format/names.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Checkbox from '#lib/ui/Checkbox.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import type { PlanEditor } from '../editor.svelte.ts';
	import { localized, requirementText, ruleTitle, selectionText, yearTitle } from '../labels.ts';
	import {
		capReached,
		isSatisfied,
		pickLock,
		swapOut,
		type EditableCourse,
		type EditableRule
	} from '../rules.ts';
	import CreditChip from './CreditChip.svelte';

	interface Props {
		editor: PlanEditor;
		rule: EditableRule;
		/** The rule's position among the rules, from 0. */
		number: number;
	}

	let { editor, rule, number }: Props = $props();

	const id = $props.id();
	const rules = $derived(editor.rules ?? []);
	const title = $derived(ruleTitle(rule, number));
	const satisfied = $derived(isSatisfied(rule));
	const preNote = $derived(localized(rule.preNote, rule.preNoteEn));
	const postNote = $derived(localized(rule.postNote, rule.postNoteEn));

	/*
	 * Said once for the rule rather than on every dimmed row, and after the list, so it never
	 * moves the rows being picked.
	 */
	const cap = $derived.by(() => {
		const reached = capReached(rules, rule);
		if (reached === 'full') return m.studyplanedit_cap_full();
		if (reached === 'partial') return m.studyplanedit_cap_partial();
		return undefined;
	});

	/** The pick a one-pick rule just swapped, said aloud: the status alone wouldn't change. */
	let swapped = $state('');

	function pick(course: EditableCourse) {
		const previous = swapOut(rules, rule, course);
		editor.toggle(rule.choiceId, course.choiceId);
		swapped = previous
			? m.studyplanedit_swapped({
					added: italianTitleCase(course.name),
					removed: italianTitleCase(previous.name)
				})
			: '';
	}
</script>

<section class="step" aria-labelledby="{id}-title">
	<header class="intro">
		<p class="overline">{yearTitle(rule.courseYear)}</p>
		<h2 id="{id}-title" class="title" tabindex="-1" data-step-heading>{title}</h2>
		<p class="status" aria-live="polite">
			<span>{requirementText(rule)}</span>
			<span class="sr-only">
				{satisfied ? m.studyplanedit_rule_done() : m.studyplanedit_rule_todo()}:
			</span>
			<StatusChip
				size="sm"
				status={satisfied ? 'success' : 'warning'}
				label={selectionText(rule)}
			/>
		</p>
		<p class="sr-only" aria-live="polite">{swapped || cap || ''}</p>
	</header>

	{#if preNote}
		<p class="note"><IconInfo aria-hidden="true" /><span>{preNote}</span></p>
	{/if}

	<ListGroup label={title}>
		{#each rule.courses as course (course.choiceId)}
			{@const lock = course.mandatory ? undefined : pickLock(rules, rule, course)}
			<ListRow
				title={italianTitleCase(course.name)}
				description={lock === 'elsewhere'
					? `${course.code} · ${m.studyplanedit_course_elsewhere()}`
					: course.code}
				disabled={lock !== undefined}
			>
				{#snippet leading()}
					<CreditChip credits={course.credits} selected={course.selected && !course.mandatory} />
				{/snippet}
				{#snippet control()}
					<Checkbox
						checked={course.selected}
						disabled={course.mandatory || lock !== undefined}
						onchange={() => pick(course)}
					/>
				{/snippet}
			</ListRow>
		{/each}
	</ListGroup>

	{#if cap}
		<p class="cap"><IconProhibit aria-hidden="true" /><span>{cap}</span></p>
	{/if}

	{#if postNote}
		<p class="note"><IconInfo aria-hidden="true" /><span>{postNote}</span></p>
	{/if}
</section>

<style>
	.step {
		display: grid;
		gap: var(--sp-5);
	}

	.intro {
		display: grid;
		gap: 8px;
	}

	.overline {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.title {
		font: var(--type-title-m);
		font-size: 1.375rem;
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
		outline: none;
	}

	.status {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 6px 10px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.cap {
		display: flex;
		align-items: flex-start;
		gap: 10px;
		max-inline-size: 64ch;
		margin-block-start: calc(var(--sp-2) - var(--sp-5));
		padding-inline: 14px;
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.875rem;
		text-wrap: pretty;
	}

	.cap :global(svg) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		margin-block-start: 1px;
	}

	.note {
		display: flex;
		align-items: flex-start;
		gap: 10px;
		max-inline-size: 64ch;
		padding: 12px 14px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.875rem;
		text-wrap: pretty;
	}

	.note :global(svg) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		margin-block-start: 1px;
		color: var(--info);
	}
</style>
