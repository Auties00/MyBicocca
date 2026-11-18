<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconCheck from '~icons/ph/check-bold';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';

	interface Props {
		/** Who or what was evaluated. */
		name: string;
		/** What to fill in next, if anything is left. */
		next?: { label: string; href: ResolvedPathname; onclick?: (event: MouseEvent) => void };
		listHref: ResolvedPathname;
	}

	let { name, next, listHref }: Props = $props();

	const body = $derived(
		next
			? m.questionnaires_sent_body({ name })
			: `${m.questionnaires_sent_body({ name })} ${m.questionnaires_sent_all_done()}`
	);
</script>

<Outcome icon={IconCheck} title={m.questionnaires_sent_title()} {body}>
	{#snippet actions()}
		{#if next}
			<Button variant="solid" trailingIcon={IconArrowRight} href={next.href} onclick={next.onclick}>
				{next.label}
			</Button>
			<Button href={listHref}>{m.questionnaires_back_to_list()}</Button>
		{:else}
			<Button variant="solid" href={listHref}>{m.questionnaires_back_to_list()}</Button>
		{/if}
	{/snippet}
</Outcome>
