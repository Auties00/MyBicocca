<script lang="ts">
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { NavItem } from '../attempt.svelte.ts';
	import Navigator from './Navigator.svelte';

	interface Props {
		items: readonly NavItem[];
		/** The page shown, 0-based. */
		page: number;
		sequential: boolean;
		onselect: (item: NavItem) => void;
		finishDisabled: boolean;
		onfinish: () => void;
	}

	let { items, page, sequential, onselect, finishDisabled, onfinish }: Props = $props();
</script>

<!-- Wide screens: every question at a glance beside the page, with Finish always at hand. -->
<div class="card">
	<h2 class="title">{m.quiz_navigator()}</h2>
	{#if items.length > 0}
		<Navigator {items} {page} {sequential} {onselect} />
	{:else}
		<div class="skeleton" aria-hidden="true">
			{#each { length: 10 }, index (index)}
				<Skeleton width="44px" height="44px" radius="var(--r-sm)" />
			{/each}
		</div>
	{/if}
	<Button variant="solid" full icon={IconPaperPlane} disabled={finishDisabled} onclick={onfinish}>
		{m.quiz_finish()}
	</Button>
</div>

<style>
	.card {
		position: sticky;
		inset-block-start: calc(var(--bar-h) + var(--safe-top) + 24px);
		display: grid;
		gap: 18px;
		padding: 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.title {
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		color: var(--text-muted);
		text-transform: uppercase;
	}

	.skeleton {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
	}
</style>
