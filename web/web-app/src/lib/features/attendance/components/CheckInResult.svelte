<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import type { CheckInSource } from '../checkInFlow.svelte.ts';
	import { outcomeView } from '../labels.ts';
	import type { CheckInOutcome } from '../outcome.ts';

	interface Props {
		outcome: CheckInOutcome;
		/** What the student would correct: the lesson code, or the link. */
		source: CheckInSource;
		level: 2 | 3;
		ondone: () => void;
		/** Sends the same check-in again (a request that failed) or goes back to fix it. */
		onretry: () => void;
	}

	let { outcome, source, level, ondone, onretry }: Props = $props();

	const shown = $derived(outcomeView(outcome));
	const resend = $derived(outcome.kind === 'failed' && outcome.error !== undefined);
</script>

<Outcome icon={shown.icon} tone={shown.tone} title={shown.title} body={shown.detail} {level}>
	{#snippet actions()}
		<!-- A failure the student can fix leads to the fix; anything else ends here. -->
		{#if shown.retry}
			<Button variant="solid" onclick={onretry}>
				{resend
					? m.common_retry()
					: source === 'code'
						? m.attendance_edit_code()
						: m.attendance_edit_link()}
			</Button>
			<Button onclick={ondone}>{m.attendance_done()}</Button>
		{:else}
			<Button variant="solid" onclick={ondone}>{m.attendance_done()}</Button>
		{/if}
	{/snippet}
</Outcome>
