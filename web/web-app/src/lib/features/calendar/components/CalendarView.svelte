<script lang="ts">
	import '../swatch.css';
	import { m } from '#lib/paraglide/messages.js';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import { CalendarController } from '../controller.svelte.ts';
	import CompactCalendar from './CompactCalendar.svelte';
	import EventOverlay from './EventOverlay.svelte';
	import WideCalendar from './WideCalendar.svelte';

	const ctl = new CalendarController();

	registerShortcuts(() => [
		{ keys: [['t']], label: m.calendar_go_to_today() },
		{ keys: [['d'], ['w'], ['m']], label: m.calendar_keys_views() },
		{ keys: [['left'], ['right']], label: m.calendar_keys_step() },
		...(ctl.view === 'month' ? [] : [{ keys: [['+'], ['-']], label: m.calendar_keys_zoom() }]),
		...(ctl.nav.eventId && ctl.docked
			? [{ keys: [['esc']], label: m.calendar_detail_close() }]
			: [])
	]);
</script>

<svelte:window onkeydown={ctl.keydown} onpagehide={ctl.remember} />

<div class={['calendar', { compact: ctl.compact }]} bind:this={ctl.root}>
	<!-- Nothing for the few frames storage takes: the grid paints once, in its final shape. -->
	{#if ctl.data.primed}
		{#if ctl.compact}
			<CompactCalendar {ctl} />
		{:else}
			<WideCalendar {ctl} />
		{/if}
	{/if}
</div>

{#if ctl.modal}
	<EventOverlay {ctl} />
{/if}

<style>
	/* The page fills the window under the top bar; on phones it runs under the floating tab bar. */
	.calendar {
		--calendar-bg: var(--bg);
		display: flex;
		flex: 1;
		flex-direction: column;
		min-block-size: 480px;
		container: calendar / inline-size;
	}

	.compact {
		--calendar-inline-start: var(--sp-4);
		--calendar-inline-end: var(--sp-4);
		min-block-size: 420px;
	}
</style>
