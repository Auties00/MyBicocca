<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconCircleDashed from '~icons/ph/circle-dashed';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import type { PlanEditor } from '../editor.svelte.ts';
	import { ruleTitle, selectionText, yearTitle } from '../labels.ts';
	import { isSatisfied, type EditableRule } from '../rules.ts';

	interface Props {
		editor: PlanEditor;
		/** The step on screen, from 0. */
		index: number;
		onselect: (index: number) => void;
		onsubmit: () => void;
		canSubmit: boolean;
		/** Submit is the view's primary action: the plan can be sent and differs from the current one. */
		primary: boolean;
		/** Why the plan can't be sent yet. */
		hint: string | undefined;
	}

	let { editor, index, onselect, onsubmit, canSubmit, primary, hint }: Props = $props();

	const offset = $derived(editor.choosesPath ? 1 : 0);
	const rules = $derived(editor.rules ?? []);
	const done = $derived(rules.filter(isSatisfied).length);

	/* The rules in their order, under a heading for each year. */
	const years = $derived(
		rules.reduce<{ year: number; items: { rule: EditableRule; number: number }[] }[]>(
			(groups, rule, number) => {
				const last = groups.at(-1);
				if (last?.year === rule.courseYear) last.items.push({ rule, number });
				else groups.push({ year: rule.courseYear, items: [{ rule, number }] });
				return groups;
			},
			[]
		)
	);
</script>

<nav aria-label={m.studyplanedit_steps_label()}>
	<ol class="list">
		{#if editor.choosesPath}
			{@const chosen = editor.selectedSchemaId !== null}
			<li>
				<button
					type="button"
					class={['item', { current: index === 0 }]}
					aria-current={index === 0 ? 'step' : undefined}
					onclick={() => onselect(0)}
				>
					<span class={['mark', chosen ? 'ok' : 'todo']}>
						{#if chosen}<IconCheckCircle aria-hidden="true" />{:else}<IconWarningCircle
								aria-hidden="true"
							/>{/if}
					</span>
					<span class="text">
						<span class="name">{m.studyplanedit_choose_path()}</span>
						<span class="meta">
							{chosen ? m.studyplanedit_path_chosen() : m.studyplanedit_path_missing()}
						</span>
					</span>
				</button>
			</li>
		{/if}
		{#each years as group (group.year)}
			<li class="year">
				<span class="year-title" aria-hidden="true">{yearTitle(group.year)}</span>
				<ol class="list">
					{#each group.items as { rule, number } (rule.choiceId)}
						{@const step = number + offset}
						{@const ok = isSatisfied(rule)}
						<li>
							<button
								type="button"
								class={['item', { current: index === step }]}
								aria-current={index === step ? 'step' : undefined}
								onclick={() => onselect(step)}
							>
								<span class={['mark', ok ? 'ok' : 'todo']}>
									{#if ok}<IconCheckCircle aria-hidden="true" />{:else}<IconWarningCircle
											aria-hidden="true"
										/>{/if}
								</span>
								<span class="text">
									<span class="name">
										<span class="sr-only">{yearTitle(group.year)}:</span>
										{ruleTitle(rule, number)}
									</span>
									<span class="meta">
										<span class="sr-only">
											{ok ? m.studyplanedit_rule_done() : m.studyplanedit_rule_todo()}:
										</span>
										{selectionText(rule)}
									</span>
								</span>
							</button>
						</li>
					{/each}
				</ol>
			</li>
		{:else}
			{#if editor.loading}
				<li class="pending" aria-hidden="true">
					<IconCircleDashed />
					<span>{m.studyplanedit_loading_rules()}</span>
				</li>
			{:else if !editor.rules}
				<li class="pending">
					<IconCircleDashed aria-hidden="true" />
					<span>{m.studyplanedit_rules_later()}</span>
				</li>
			{/if}
		{/each}
	</ol>
</nav>

{#if rules.length > 0}
	<div class="submit">
		<p class="done">{m.studyplanedit_rules_done({ done, total: rules.length })}</p>
		<Button
			variant={primary ? 'solid' : 'tonal'}
			icon={IconPaperPlane}
			full
			disabled={!canSubmit}
			onclick={onsubmit}
		>
			{m.studyplanedit_submit_plan()}
		</Button>
		{#if hint}<p class="hint">{hint}</p>{/if}
	</div>
{/if}

<style>
	.list {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.year {
		display: grid;
		gap: 4px;
		margin-block-start: var(--sp-3);
	}

	.year-title {
		padding-inline: 10px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.item {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: 10px;
		inline-size: 100%;
		padding: 9px 10px;
		border-radius: var(--r-sm);
		color: var(--text);
		text-align: start;
		cursor: pointer;
		touch-action: manipulation;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.item:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.item.current {
		background: var(--container);
		color: var(--on-container);
	}

	.mark {
		display: flex;
		margin-block-start: 1px;
	}

	.mark :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.ok {
		color: var(--success);
	}

	.todo {
		color: var(--warning);
	}

	.text {
		display: grid;
		gap: 1px;
		min-inline-size: 0;
	}

	.name {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		font: var(--type-ui-strong);
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.current .meta {
		color: inherit;
		opacity: 0.8;
	}

	.pending {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: 10px;
		margin-block-start: var(--sp-3);
		padding: 9px 10px;
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.pending :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-subtle);
	}

	.submit {
		display: grid;
		gap: 10px;
		margin-block-start: var(--sp-5);
		padding-block-start: var(--sp-4);
		border-block-start: 1px solid var(--hairline);
	}

	.done {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
