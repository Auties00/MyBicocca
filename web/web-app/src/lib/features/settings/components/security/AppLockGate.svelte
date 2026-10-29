<script lang="ts">
	import { appLock } from '../../lock/appLock.svelte.ts';
	import LockScreen from './LockScreen.svelte';

	let dialog = $state<HTMLDialogElement>();
	/** Where focus was when the lock came down, to give it back on unlock. */
	let returnTo: HTMLElement | null = null;

	$effect(() => appLock.watch());

	function show() {
		dialog?.showModal();
		dialog?.querySelector<HTMLElement>('[data-autofocus]')?.focus();
	}

	// A modal dialog: the app behind it is inert and hidden until the PIN is right.
	$effect(() => {
		if (!dialog) return;
		if (appLock.locked && !dialog.open) {
			returnTo = document.activeElement instanceof HTMLElement ? document.activeElement : null;
			show();
		} else if (!appLock.locked && dialog.open) {
			dialog.close();
		}
	});

	/*
	 * Chrome lets a second Escape close a modal dialog whatever `cancel` does, when no new user
	 * activation came between the two: a gate closed that way while still locked comes straight back.
	 */
	function closed() {
		if (appLock.locked) {
			show();
			return;
		}
		const target = returnTo?.isConnected ? returnTo : document.querySelector('#main h1');
		if (target instanceof HTMLElement) target.focus({ preventScroll: true });
		returnTo = null;
	}
</script>

<!--
	Mounted once by the signed-in shell. Escape does nothing here: the only way out is the PIN, or
	signing out.
-->
<dialog
	class="gate"
	bind:this={dialog}
	aria-labelledby="app-lock-title"
	closedby="none"
	oncancel={(event) => event.preventDefault()}
	onclose={closed}
>
	{#if appLock.locked}<LockScreen />{/if}
</dialog>

<style>
	.gate {
		position: fixed;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		max-inline-size: none;
		max-block-size: none;
		margin: 0;
		padding: 0;
		border: 0;
		overflow: auto;
		background: var(--bg);
		color: var(--text);
	}

	.gate::backdrop {
		background: var(--bg);
	}
</style>
