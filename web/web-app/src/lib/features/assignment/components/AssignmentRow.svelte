<script lang="ts">
	import IconCaretRight from '~icons/ph/caret-right';
	import IconCheckBold from '~icons/ph/check-bold';
	import IconClipboardText from '~icons/ph/clipboard-text-fill';
	import IconClockCountdown from '~icons/ph/clock-countdown-fill';
	import IconLockSimple from '~icons/ph/lock-simple-fill';
	import IconMinusCircle from '~icons/ph/minus-circle-fill';
	import IconNotePencil from '~icons/ph/note-pencil-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Badge from '#lib/ui/Badge.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import type { Icon } from '#lib/ui/types.ts';
	import { gradeParts, rowStatus, stageLabel } from '../labels.ts';
	import { assignmentPath } from '../links.ts';
	import type { Standing } from '../model.ts';
	import type { ListedAssignment } from '../rows.ts';
	import DueTile from './DueTile.svelte';

	interface Props {
		row: ListedAssignment;
		courseId: number;
		now: number;
		/** Whether the table keeps a column for grades: some row in the list has one. */
		grades: boolean;
	}

	let { row, courseId, now, grades }: Props = $props();

	/* The kit's glyph per tone would put a check on work still to do: each state draws its own. */
	const ICONS: Partial<Record<Standing, Icon>> = {
		todo: IconClipboardText,
		late: IconClockCountdown,
		draft: IconNotePencil,
		missed: IconLockSimple,
		none: IconMinusCircle
	};

	const submission = $derived(row.submission);
	/* The course's colour for what is still to do; grey for what is done or out of reach. */
	const ACTIVE: ReadonlySet<Standing> = new Set(['todo', 'draft', 'late', 'upcoming']);
	const tone = $derived.by(() => {
		if (row.standing === undefined) return 'quiet';
		if (row.urgency !== 'calm') return row.urgency;
		return ACTIVE.has(row.standing) ? 'pending' : 'quiet';
	});
	const line = $derived(submission && rowStatus(row.schedule, submission, row.config, now));
	const stage = $derived(row.standing && stageLabel(row.standing));
	const grade = $derived(submission?.grade ? gradeParts(submission.grade) : null);
</script>

<a
	class={['row', { grades }]}
	href={assignmentPath(courseId, row.assignment.id)}
	aria-busy={submission === undefined && !row.failed}
>
	<DueTile due={row.schedule.due} {tone} {now} />
	<span class="main">
		<span class="name">{row.assignment.name}</span>
		{#if line}
			<span class={['status', row.urgency]}>{line}</span>
		{:else if row.failed}
			<span class="status">{m.assignment_status_unknown()}</span>
		{:else}
			<span class="status loading">
				<Skeleton width="58%" height="0.85em" />
				<span class="sr-only">{m.assignment_status_loading()}</span>
			</span>
		{/if}
	</span>
	<span class="stage">
		{#if stage && row.standing}
			<StatusChip status={stage.tone} label={stage.label} icon={ICONS[row.standing]} size="sm" />
		{:else if !row.failed}
			<Skeleton width="104px" height="22px" radius="999px" />
		{/if}
	</span>
	<span class="grade">
		{#if grade}
			<span class="mark"
				>{grade.value}{#if grade.max}<small>/{grade.max}</small>{/if}</span
			>
		{:else if submission?.stage === 'submitted'}
			<span class="check" title={m.assignment_stage_submitted()}><IconCheckBold /></span>
		{:else if submission?.stage === 'draft'}
			<span class="draft"><Badge tone="info">{m.assignment_stage_draft()}</Badge></span>
		{/if}
	</span>
	<IconCaretRight class="chevron" aria-hidden="true" />
</a>

<style>
	.row {
		position: relative;
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		grid-auto-columns: auto;
		grid-auto-flow: column;
		align-items: center;
		gap: 14px;
		min-block-size: 76px;
		padding: 12px 14px 12px 12px;
		border-radius: var(--r-sm);
		background: var(--surface);
		color: var(--text);
		text-decoration: none;
		-webkit-tap-highlight-color: transparent;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		/* The list rows' state layers, over the row's own fill. */
		.row:hover {
			background-image: linear-gradient(var(--state-hover), var(--state-hover));
			transition-duration: 0s;
		}
	}

	.row:active {
		background-image: linear-gradient(var(--state-press), var(--state-press));
	}

	.row:focus-visible {
		z-index: 1;
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.main {
		display: grid;
		gap: 3px;
		min-inline-size: 0;
	}

	.name {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		font: var(--type-row-title);
		letter-spacing: var(--track-row-title);
		overflow-wrap: anywhere;
	}

	.status {
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}

	.status.soon {
		color: var(--warning);
	}

	.status.overdue {
		color: var(--danger);
	}

	/* As tall as the line it stands for, so nothing moves when the status arrives. */
	.loading {
		display: flex;
		align-items: center;
		block-size: 1lh;
	}

	.stage {
		display: none;
	}

	.grade {
		display: flex;
		justify-content: flex-end;
		min-inline-size: 0;
	}

	.grade:empty {
		display: none;
	}

	.mark {
		font: 500 1.375rem / 1 var(--font-display);
		font-variant-numeric: lining-nums tabular-nums;
		letter-spacing: -0.01em;
		color: var(--text);
		white-space: nowrap;
	}

	.mark small {
		margin-inline-start: 1px;
		color: var(--text-muted);
		font: 500 0.875rem / 1 var(--font-ui);
	}

	.check {
		display: grid;
		place-items: center;
		inline-size: 28px;
		block-size: 28px;
		border-radius: 50%;
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.check :global(svg) {
		inline-size: 15px;
		block-size: 15px;
	}

	.row :global(.chevron) {
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-subtle);
	}

	/* A table from the width where the state and the grade fit in their own columns. */
	@container assignments (width >= 640px) {
		.row {
			grid-template-columns: 52px minmax(0, 1fr) 168px 18px;
			gap: 18px;
		}

		.row.grades {
			grid-template-columns: 52px minmax(0, 1fr) 168px 72px 18px;
		}

		.stage {
			display: flex;
		}

		.grade,
		.grade:empty {
			display: none;
		}

		.grades .grade,
		.grades .grade:empty {
			display: flex;
		}

		.check,
		.draft {
			display: none;
		}
	}
</style>
