<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import IconCheck from '~icons/ph/check-bold';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import { submittedText } from '../labels.ts';
	import type { Approval } from '../path.ts';

	interface Props {
		approval: Approval;
		planHref: ResolvedPathname;
	}

	let { approval, planHref }: Props = $props();
</script>

<Outcome icon={IconCheck} title={m.studyplanedit_submitted_title()} body={submittedText(approval)}>
	{#snippet actions()}
		<Button variant="solid" href={planHref}>{m.studyplanedit_back_to_plan()}</Button>
	{/snippet}
</Outcome>
