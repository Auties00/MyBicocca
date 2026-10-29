<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';

	interface Props {
		open: boolean;
		/** Why the submission failed; while absent, it is under way. */
		error: string | undefined;
		onretry: () => void;
		onback: () => void;
	}

	let { open, error, onretry, onback }: Props = $props();
</script>

<!-- Time ran out: the attempt is being submitted as it stands, and nothing can interrupt it. -->
<Dialog {open} title={m.quiz_time_up_title()} size="sm" role="alertdialog" dismissible={false}>
	{#if error}
		<p class="body" role="alert">{error}</p>
	{:else}
		<p class="body"><Spinner size={20} />{m.quiz_time_up_body()}</p>
	{/if}
	{#snippet footer()}
		{#if error}
			<Button variant="ghost" onclick={onback}>{m.quiz_back_to_quiz()}</Button>
			<Button variant="solid" onclick={onretry}>{m.common_retry()}</Button>
		{/if}
	{/snippet}
</Dialog>

<style>
	.body {
		display: flex;
		align-items: center;
		gap: 10px;
		color: var(--text-muted);
	}
</style>
