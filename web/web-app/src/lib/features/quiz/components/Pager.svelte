<script lang="ts">
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';

	interface Props {
		/** `lg` under the questions on wide screens; `md` in the phone's dock. */
		size: 'md' | 'lg';
		/** A previous page the quiz lets the student go back to. */
		back: boolean;
		/** The page is the attempt's last: Finish replaces Next. */
		last: boolean;
		/** The page has loaded, so its next page is known. */
		ready: boolean;
		/** Saving, loading or submitting. */
		busy: boolean;
		online: boolean;
		onprevious: () => void;
		onnext: () => void;
		onfinish: () => void;
	}

	let { size, back, last, ready, busy, online, onprevious, onnext, onfinish }: Props = $props();

	const disabled = $derived(busy || !online);
</script>

{#if size === 'md'}<span class="spacer"></span>{/if}
{#if back}
	{#if size === 'lg'}
		<Button variant="tonal" size="lg" icon={IconArrowLeft} {disabled} onclick={onprevious}>
			{m.quiz_previous()}
		</Button>
	{:else}
		<IconButton
			variant="tonal"
			icon={IconArrowLeft}
			label={m.quiz_previous()}
			hideTooltip
			{disabled}
			onclick={onprevious}
		/>
	{/if}
{/if}
{#if size === 'lg'}<span class="spacer"></span>{/if}
{#if last}
	<Button variant="solid" {size} icon={IconPaperPlane} {disabled} onclick={onfinish}>
		{m.quiz_finish()}
	</Button>
{:else if ready}
	<Button
		variant="solid"
		{size}
		trailingIcon={IconArrowRight}
		{disabled}
		loading={busy}
		onclick={onnext}
	>
		{m.quiz_next()}
	</Button>
{/if}

<style>
	.spacer {
		flex: 1;
	}
</style>
