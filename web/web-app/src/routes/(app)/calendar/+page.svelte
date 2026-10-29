<script lang="ts">
	import CalendarView from '#lib/features/calendar/components/CalendarView.svelte';
	import { m } from '#lib/paraglide/messages.js';
	import Page from '#lib/ui/Page.svelte';
</script>

<svelte:head>
	<title>{m.calendar_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="full" bleed fill>
	<CalendarView />
</Page>
