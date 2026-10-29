<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { rowStatus } from '../labels.ts';
	import { assignmentPath } from '../links.ts';
	import type { ListedAssignment } from '../rows.ts';
	import DueTile from './DueTile.svelte';

	interface Props {
		/** A pending assignment with a deadline ahead, its status known. */
		row: ListedAssignment;
		courseId: number;
		now: number;
	}

	let { row, courseId, now }: Props = $props();

	const line = $derived(row.submission && rowStatus(row.schedule, row.submission, row.config, now));
</script>

<a class="next" href={assignmentPath(courseId, row.assignment.id)}>
	<DueTile
		due={row.schedule.due}
		tone={row.urgency === 'calm' ? 'pending' : row.urgency}
		{now}
		size="sm"
	/>
	<span class="text">
		<span class="overline">{m.assignment_next_due()}</span>
		<span class="name">{row.assignment.name}</span>
		{#if line}<span class={['when', row.urgency]}>{line}</span>{/if}
	</span>
</a>

<style>
	.next {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: 12px;
		padding: 12px 14px;
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: var(--elev-1);
		color: inherit;
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.next:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.next:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.text {
		display: grid;
		gap: 1px;
		min-inline-size: 0;
	}

	.overline {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	/* Courses repeat long names that differ only at the end ("… laboratorio 3", "… 4"). */
	.name {
		display: -webkit-box;
		overflow: hidden;
		font: var(--type-ui-strong);
		overflow-wrap: anywhere;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 3;
		line-clamp: 3;
	}

	.when {
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}

	.soon {
		color: var(--warning);
	}

	.overdue {
		color: var(--danger);
	}
</style>
