<script lang="ts">
	import IconHourglass from '~icons/ph/hourglass-low-duotone';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { toDataError } from '#lib/data/errors.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { formatInstant, quizErrorMessage } from '../format.ts';

	interface Props {
		/** When Moodle stops waiting and abandons the attempt, if it says. */
		deadline: number | null;
		/** The attempt's number, for the confirmation. */
		number: number | null;
		online: boolean;
		/** Submits the attempt; throws when Moodle refuses, for the confirmation to say why. */
		onsubmit: () => Promise<void>;
	}

	let { deadline, number, online, onsubmit }: Props = $props();

	let confirming = $state(false);
</script>

<!-- An overdue attempt takes no more answers, only its submission, within the grace period. -->
<EmptyState
	size="page"
	icon={IconHourglass}
	title={m.quiz_overdue_title()}
	body={deadline === null
		? m.quiz_overdue_body_open()
		: m.quiz_overdue_body({ deadline: formatInstant(new Date(deadline)) })}
>
	{#snippet action()}
		<Button
			variant="solid"
			size="lg"
			icon={IconPaperPlane}
			disabled={!online}
			onclick={() => (confirming = true)}
		>
			{m.quiz_submit_overdue()}
		</Button>
	{/snippet}
</EmptyState>

<ConfirmDialog
	bind:open={confirming}
	title={m.quiz_submit_title()}
	message={number === null
		? m.quiz_overdue_confirm_unnumbered()
		: m.quiz_overdue_confirm({ number })}
	confirmLabel={m.quiz_submit_confirm()}
	onconfirm={onsubmit}
	errorMessage={(error) => quizErrorMessage(toDataError(error))}
/>
