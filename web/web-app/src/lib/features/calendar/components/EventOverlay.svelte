<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import Overlay from '#lib/ui/Overlay.svelte';
	import type { CalendarController } from '../controller.svelte.ts';
	import { detailSubtitle, displayTitle, editionsLine } from '../format.ts';
	import EventDetail from './EventDetail.svelte';

	interface Props {
		ctl: CalendarController;
	}

	let { ctl }: Props = $props();

	const selected = $derived(ctl.selected);
	const courses = $derived(ctl.modalPage === 'courses');
</script>

<!-- The open event on phones (a bottom sheet) and tablets (a dialog). -->
<Overlay
	open={ctl.nav.eventId !== null && (selected !== undefined || ctl.missing)}
	title={courses
		? m.calendar_course_picker_title()
		: selected
			? displayTitle(selected)
			: m.calendar_detail_title_fallback()}
	subtitle={courses ? editionsLine(ctl.editions) : selected ? detailSubtitle(selected) : undefined}
	onback={courses ? () => (ctl.modalPage = 'event') : undefined}
	onclose={() => ctl.close()}
>
	{#if selected}
		<EventDetail event={selected} courses={ctl.data.courses} bind:page={ctl.modalPage} />
	{:else}
		<p class="gone">{m.calendar_event_gone()}</p>
	{/if}
</Overlay>

<style>
	.gone {
		color: var(--text-muted);
	}
</style>
