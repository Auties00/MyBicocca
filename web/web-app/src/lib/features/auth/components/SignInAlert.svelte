<script lang="ts">
	import IconHourglass from '~icons/ph/hourglass-high';
	import IconPlugs from '~icons/ph/plugs';
	import type { SignInFailure } from '#lib/data/signIn.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';

	interface Props {
		/** Refused credentials are told on the password field: nothing shows here for them. */
		failure: SignInFailure | null;
		onretry: () => void;
	}

	let { failure, onretry }: Props = $props();

	const shown = $derived.by(() => {
		switch (failure) {
			case 'no_connection':
				return { tone: 'danger', icon: IconPlugs, text: m.auth_error_no_connection() } as const;
			case 'timeout':
				return { tone: 'warning', icon: IconHourglass, text: m.auth_error_timeout() } as const;
			case 'unknown':
				return { tone: 'danger', icon: undefined, text: m.auth_error_unknown() } as const;
			case 'bad_credentials':
			case null:
				return undefined;
		}
	});
</script>

{#if shown}
	<Banner tone={shown.tone} icon={shown.icon} message={shown.text} role="alert">
		{#snippet actions()}
			<Button size="sm" variant="outline" onclick={onretry}>{m.common_retry()}</Button>
		{/snippet}
	</Banner>
{/if}
