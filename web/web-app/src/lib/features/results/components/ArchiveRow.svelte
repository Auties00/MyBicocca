<script lang="ts">
	import { formatGrade } from '#lib/format/grades.ts';
	import Grade from '#lib/ui/Grade.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import {
		archiveLine,
		attemptTitle,
		resultHref,
		resultStatus,
		resultTitle,
		spokenGrade
	} from '../format.ts';
	import { visibleChoice, type ExamResult } from '../model.ts';

	interface Props {
		result: ExamResult;
		/** The open result of the list beside its detail. */
		selected?: boolean;
		/** Among the other sittings of one exam: named by its day, described by its outcome. */
		attempt?: boolean;
	}

	let { result, selected = false, attempt = false }: Props = $props();

	const title = $derived(attempt ? attemptTitle(result) : resultTitle(result));
	const description = $derived(attempt ? resultStatus(result)?.label : archiveLine(result));
	/* A grade that is a word ("Respinto") already leads the description when nothing was answered. */
	const said = $derived(
		result.grade.kind !== 'numeric' &&
			result.grade.kind !== 'unknown' &&
			description?.startsWith(formatGrade(result.grade, { full: true })) === true
	);
</script>

<!-- The grade is spoken after the name, so the rows' links don't all start with a number. -->
<ListRow
	{title}
	{description}
	spoken={said ? undefined : spokenGrade(result.grade)}
	href={resultHref(result.id)}
	{selected}
>
	{#snippet leading()}
		<Grade
			grade={result.grade}
			variant="tile"
			spoken={false}
			struck={visibleChoice(result) === 'rejected'}
		/>
	{/snippet}
</ListRow>
