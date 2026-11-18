<script lang="ts">
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { degreeHref, type Degrees } from '../degrees.svelte.ts';
	import type { AcademicTitle } from '../degrees.ts';
	import {
		categoryIcons,
		categoryLabel,
		detailsLine,
		gradeLine,
		gradeShort,
		headline,
		titleStatusLabel
	} from '../degreeView.ts';
	import { useDocumentsView } from '../view.ts';
	import { joinParts } from '../text.ts';

	interface Props {
		degrees: Degrees;
		/** The title open beside the list. */
		selected?: string;
	}

	let { degrees, selected }: Props = $props();

	/*
	 * Where a row has the room the grade is a column of its own, easy to compare down the list;
	 * in the narrow list beside a detail it leads the line instead, leaving the title the width.
	 */
	const view = useDocumentsView();

	/* A projected title says so first: it isn't awarded yet. */
	function line(title: AcademicTitle): string {
		const status = title.status === 'hypothesised' ? titleStatusLabel(title.status) : undefined;
		return joinParts([view.wide ? gradeLine(title) : undefined, status, detailsLine(title)]);
	}
</script>

<div class="list">
	{#each degrees.groups as group (group.category)}
		<ListGroup title={categoryLabel(group.category)} level={2}>
			{#each group.titles as title (title.id)}
				{@const href = degreeHref(title.id)}
				<ListRow
					title={headline(title)}
					description={line(title) || undefined}
					meta={view.wide ? undefined : gradeShort(title, true)}
					icon={categoryIcons[title.category]}
					iconTone="accent"
					{href}
					selected={href === selected}
				/>
			{/each}
		</ListGroup>
	{/each}
</div>

<style>
	.list {
		display: grid;
		gap: var(--sp-5);
	}
</style>
