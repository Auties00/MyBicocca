<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconCheck from '~icons/ph/check-bold';
	import IconX from '~icons/ph/x-bold';
	import { bookHref } from '#lib/features/exams/links.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import type { Choice } from '../model.ts';

	interface Props {
		choice: Choice;
		/** The activity's code, to book its next call after a rejection. */
		code: string | null;
		level: 2 | 3;
		/** The heading, for the confirmation to give the focus to as it closes. */
		heading?: HTMLElement;
	}

	let { choice, code, level, heading = $bindable() }: Props = $props();

	const accepted = $derived(choice === 'accepted');
</script>

<!-- Takes the place of the decision it reports, so the answer lands where the question was. -->
<Outcome
	icon={accepted ? IconCheck : IconX}
	tone={accepted ? 'success' : 'neutral'}
	title={accepted ? m.results_accepted_title() : m.results_rejected_title()}
	body={accepted ? m.results_accepted_body() : m.results_rejected_body()}
	{level}
	bind:heading
>
	{#snippet actions()}
		{#if accepted}
			<Button variant="outline" trailingIcon={IconArrowRight} href={shellHref('profile/exams')}>
				{m.results_go_transcript()}
			</Button>
		{:else}
			<Button variant="solid" icon={IconCalendarPlus} href={bookHref({ course: code })}>
				{m.results_book_call()}
			</Button>
		{/if}
	{/snippet}
</Outcome>
