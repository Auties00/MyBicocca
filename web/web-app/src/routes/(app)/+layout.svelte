<script lang="ts">
	import type { Snippet } from 'svelte';
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { session } from '#lib/data/session.svelte.ts';
	import ReauthPrompt from '#lib/features/auth/components/ReauthPrompt.svelte';
	import { useSearch } from '#lib/features/search/search.svelte.ts';
	import AppLockGate from '#lib/features/settings/components/security/AppLockGate.svelte';
	import { careerTitle } from '#lib/format/names.ts';
	import { m } from '#lib/paraglide/messages.js';
	import AppShell from '#lib/shell/AppShell.svelte';
	import { sessionAccountModel } from '#lib/shell/sessionAccounts.ts';
	import { toast } from '#lib/ui/toast.svelte.ts';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	const account = $derived(sessionAccountModel());

	useSearch();

	/*
	 * Keyed on the account and its career, not on session.current: signing the same account in again
	 * reopens its clients, and the shell, the page and what was typed on it must stay through that.
	 */
	const ready = $derived(session.career !== undefined && !session.needsCareerPick);

	// Signing out here or in another tab leaves the signed-in pages.
	$effect(() => {
		if (!session.loaded) return;
		if (session.active === undefined) void goto(resolve('login'), { replace: true });
		else if (session.needsCareerPick) void goto(resolve('login/career'), { replace: true });
	});

	// Account events (expired sign-in, career changes) surface once as toasts. The active account's
	// expired sign-in stays for ReauthPrompt, which signs it in again in place.
	$effect(() => {
		for (const event of session.events) {
			if (event.type === 'reauth_required' && event.accountId === session.activeId) continue;
			const name = session.accounts.find((a) => a.id === event.accountId)?.displayName ?? '';
			if (event.type === 'reauth_required') {
				toast.show({ message: m.shell_reauth_required({ name }), tone: 'warning', duration: 0 });
			} else if (event.type === 'new_career') {
				toast.info(m.shell_new_career({ name: careerTitle(event.career) }));
			} else if (event.type === 'selected_career_ended') {
				toast.info(m.shell_career_ended({ name: careerTitle(event.career) }));
			} else {
				toast.show({ message: m.shell_career_missing(), tone: 'warning' });
			}
			session.dismissEvent(event);
		}
	});
</script>

{#if account && ready}
	<AppShell {account}>
		{@render children()}
	</AppShell>
	<ReauthPrompt />
	<AppLockGate />
{/if}
