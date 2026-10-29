<script lang="ts">
	import type { Snippet } from 'svelte';
	import { resolve } from '$app/paths';
	import type { ResolvedPathname } from '$app/types';
	import { m } from '#lib/paraglide/messages.js';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { getSettingsLayout } from '../layout.ts';

	interface Props {
		title: string;
		subtitle?: string;
		/** A page under another section (Release notes under About); otherwise the list of settings. */
		parent?: { href: ResolvedPathname; label: string };
		actions?: Snippet;
		meta?: Snippet;
		children: Snippet;
	}

	let { title, subtitle, parent, actions, meta, children }: Props = $props();

	const layout = getSettingsLayout();

	// On two panes the list of settings is already beside the section: no way back to it needed.
	const back = $derived(
		parent ??
			(layout.twoPanes ? undefined : { href: resolve('settings'), label: m.shell_settings() })
	);
</script>

<svelte:head>
	<title>{title} · {m.app_name()}</title>
</svelte:head>

<Page width="reading">
	<PageHeader {title} {subtitle} {back} {actions} {meta} />
	{@render children()}
</Page>
