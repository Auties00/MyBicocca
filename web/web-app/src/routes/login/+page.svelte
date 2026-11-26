<script lang="ts">
	import { afterNavigate } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { session } from '#lib/data/session.svelte.ts';
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

<div data-auth-column="narrow">
	<LoginForm {back} />
</div>
