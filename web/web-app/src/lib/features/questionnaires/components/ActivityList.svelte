<script lang="ts">
	import IconChecks from '~icons/ph/checks';
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import { activityLine } from '../format.ts';
	import { activityHref } from '../links.ts';
	import type { QuestionnaireActivity } from '../model.ts';
	import type { Questionnaires } from '../questionnaires.svelte.ts';
	import StatusGlyph from './StatusGlyph.svelte';

	interface Props {
		questionnaires: Questionnaires;
		/** The activity open beside the list. */
		selected?: number;
	}

	let { questionnaires, selected }: Props = $props();

	const todoId = $props.id();
	const doneId = `${todoId}-done`;
</script>

<!-- Unnamed: the section around it names the group already. -->
{#snippet rows(activities: QuestionnaireActivity[])}
	<ListGroup>
		{#each activities as activity (activity.id)}
			<ListRow
				title={activity.name}
				description={activityLine(activity)}
				href={activityHref(activity.id)}
				selected={activity.id === selected}
			>
				{#snippet leading()}<StatusGlyph status={activity.status} />{/snippet}
			</ListRow>
		{/each}
	</ListGroup>
{/snippet}

<div class="list">
	<section class="section" aria-labelledby={todoId}>
		<SectionHeader
			id={todoId}
			title={m.questionnaires_todo_heading()}
			description={questionnaires.outstanding.length > 0
				? m.questionnaires_todo_description()
				: undefined}
		/>
		{#if questionnaires.outstanding.length > 0}
			{@render rows(questionnaires.outstanding)}
		{:else}
			<p class="settled">
				<IconChecks aria-hidden="true" />
				<span>
					<span class="settled-title">{m.questionnaires_all_done_title()}</span>
					<span class="settled-body">{m.questionnaires_all_done_body()}</span>
				</span>
			</p>
		{/if}
	</section>

	{#if questionnaires.done.length > 0}
		<section class="section" aria-labelledby={doneId}>
			<SectionHeader id={doneId} title={m.questionnaires_done_heading()} />
			{@render rows(questionnaires.done)}
		</section>
	{/if}
</div>

<style>
	.list {
		display: grid;
		gap: var(--sp-7);
	}

	.section {
		display: grid;
		gap: var(--sp-4);
	}

	.settled {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-3);
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.settled :global(svg) {
		flex: none;
		inline-size: 22px;
		block-size: 22px;
		margin-block-start: 1px;
		color: var(--success);
	}

	.settled > span {
		display: grid;
		gap: 2px;
	}

	.settled-title {
		font: var(--type-ui-strong);
	}

	.settled-body {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}
</style>
