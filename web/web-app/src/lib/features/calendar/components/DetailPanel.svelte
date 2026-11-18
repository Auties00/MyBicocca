<script lang="ts">
	import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { editionsFor } from '../courses.ts';
	import { detailHeading, editionsLine } from '../format.ts';
	import type { CalendarEvent } from '../model.ts';
	import EventDetail from './EventDetail.svelte';

	interface Props {
		/** Undefined when the event is no longer on the calendar. */
		event: CalendarEvent | undefined;
		courses: readonly MoodleEnrolledCourse[] | undefined;
		onclose: () => void;
		/** Shows a back arrow to the day's agenda, in the docked pane. */
		onback?: () => void;
		dense?: boolean;
	}

	let { event, courses, onclose, onback, dense = false }: Props = $props();

	const headingId = $props.id();
	/* Another event starts on its details, never on the previous one's course list. */
	let page = $derived.by<'event' | 'courses'>(() => {
		void event?.id;
		return 'event';
	});

	const heading = $derived(event && detailHeading(event));
	const editions = $derived(
		event && courses && event.activityCode ? editionsFor(courses, event.activityCode).length : 0
	);
</script>

<section class={['panel', { dense }]} aria-labelledby={headingId}>
	<header class="head">
		{#if page === 'courses'}
			<IconButton icon={IconArrowLeft} label={m.ui_back()} onclick={() => (page = 'event')} />
		{:else if onback}
			<IconButton icon={IconArrowLeft} label={m.calendar_back_to_agenda()} onclick={onback} />
		{/if}
		<div class="titles">
			{#if page === 'courses'}
				<h2 id={headingId} tabindex="-1">{m.calendar_course_picker_title()}</h2>
				<p class="subtitle">{editionsLine(editions)}</p>
			{:else if heading}
				<h2 id={headingId} tabindex="-1" lang="it">{heading.title}</h2>
				<p class="subtitle">
					{#if heading.cancelled}<strong class="cancelled">{m.calendar_detail_cancelled()}</strong>
						·
					{/if}{heading.kind}
				</p>
			{:else}
				<h2 id={headingId} tabindex="-1">{m.calendar_detail_title_fallback()}</h2>
			{/if}
		</div>
		<IconButton icon={IconX} label={m.calendar_detail_close()} onclick={onclose} data-close />
	</header>
	<div class="body">
		{#if event}
			<EventDetail {event} {courses} bind:page {dense} />
		{:else}
			<p class="gone">{m.calendar_event_gone()}</p>
		{/if}
	</div>
</section>

<style>
	.panel {
		display: flex;
		flex-direction: column;
		min-block-size: 0;
	}

	.head {
		display: flex;
		align-items: flex-start;
		gap: 4px;
		padding: 14px 10px 10px 20px;
	}

	.head:has(> :global(:first-child:not(.titles))) {
		padding-inline-start: 10px;
	}

	.titles {
		flex: 1;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		padding-block: 6px;
	}

	h2 {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		overflow-wrap: break-word;
		hyphens: auto;
		outline: none;
	}

	h2:focus-visible {
		border-radius: var(--r-2xs);
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.dense h2 {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.subtitle {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.cancelled {
		color: var(--danger);
		font-weight: 600;
	}

	.body {
		flex: 1;
		min-block-size: 0;
		overflow: auto;
		padding: 4px 20px 20px;
		overscroll-behavior: contain;
		scrollbar-width: thin;
	}

	.gone {
		color: var(--text-muted);
		font: var(--type-body);
	}
</style>
