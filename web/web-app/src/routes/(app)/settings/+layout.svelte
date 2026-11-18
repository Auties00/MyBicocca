<script lang="ts">
	import type { Snippet } from 'svelte';
	import SettingsNav from '#lib/features/settings/components/SettingsNav.svelte';
	import { setSettingsLayout } from '#lib/features/settings/layout.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ListDetail from '#lib/ui/ListDetail.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	let twoPanes = $state<boolean>();

	setSettingsLayout({
		get twoPanes() {
			return twoPanes;
		}
	});
</script>

<!--
	The list of settings stays beside each section where a form's width (640 px) fits next to it;
	below, the list is a page of its own (the index page) and each section replaces it.
-->
<ListDetail
	centered
	listWidth={288}
	detailMin={640}
	listLabel={m.settings_nav_label()}
	detail={children}
	bind:twoPanes
>
	{#snippet list()}
		<div class="nav"><SettingsNav /></div>
	{/snippet}
</ListDetail>

<style>
	.nav {
		padding: var(--sp-6) 0 var(--sp-2) var(--gutter);
	}
</style>
