<script lang="ts">
	import { afterNavigate } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { session } from '#lib/data/session.svelte.ts';
	import LoginCover from '#lib/features/auth/components/LoginCover.svelte';
	import LoginForm from '#lib/features/auth/components/LoginForm.svelte';
	import { m } from '#lib/paraglide/messages.js';

	/*
	 * Read once: signing in makes someone signed in before the page leaves, and the page must not
	 * turn into "add an account" under the cross-fade.
	 */
	const adding = session.current !== undefined;

	let cameFromApp = $state(false);
	afterNavigate((navigation) => {
		cameFromApp =
			navigation.type !== 'enter' &&
			navigation.from?.url.pathname.startsWith(resolve('login')) === false;
	});

	const back = adding
		? {
				href: resolve('calendar'),
				onclick: (event: MouseEvent) => {
					if (!cameFromApp) return;
					event.preventDefault();
					history.back();
				}
			}
		: undefined;
</script>

<svelte:head>
	<title>{adding ? m.auth_add_title() : m.auth_sign_in_button()} · {m.app_name()}</title>
</svelte:head>

<div class={['spread', { adding }]} data-auth-column={adding ? 'narrow' : 'spread'}>
	{#if !adding}<LoginCover />{/if}
	<LoginForm {back} />
</div>

<style>
	.spread {
		display: grid;
		gap: 32px;
		inline-size: 100%;
		max-inline-size: 440px;
		margin-inline: auto;
	}

	@media (width >= 600px) {
		.spread {
			gap: 36px;
		}
	}

	@media (width >= 1024px) {
		.spread:not(.adding) {
			grid-template-columns: minmax(0, 1fr) minmax(400px, 440px);
			align-items: center;
			gap: clamp(56px, 7vw, 128px);
			max-inline-size: none;
		}
	}
</style>
