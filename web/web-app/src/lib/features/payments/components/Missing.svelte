<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';

	interface Props {
		title: string;
		body: string;
		back: { href: ResolvedPathname; label: string };
	}

	let { title, body, back }: Props = $props();
</script>

<svelte:head>
	<title>{title} · {m.app_name()}</title>
</svelte:head>

<!-- A link to an item the list no longer has: another career's, or one Esse3 dropped. -->
<EmptyState size="page" icon={IconMagnifyingGlass} {title} {body}>
	{#snippet action()}
		<Button href={back.href}>{back.label}</Button>
	{/snippet}
</EmptyState>
