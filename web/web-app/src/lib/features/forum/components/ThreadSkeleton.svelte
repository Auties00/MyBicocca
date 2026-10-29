<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';

	/* Shown only while the posts load, so the gate just holds the bones back for a quick load. */
	const gate = new LoadingGate(() => true);
</script>

<div class="skeleton" aria-busy="true" aria-label={m.forum_thread_loading()}>
	{#if gate.visible}
		<div class="card opening">
			<span class="head">
				<Skeleton variant="circle" width="44px" />
				<span class="lines">
					<Skeleton variant="text" width="40%" />
					<Skeleton variant="text" width="22%" height="0.75em" />
				</span>
			</span>
			<Skeleton variant="text" lines={4} />
		</div>
		{#each { length: 2 }, index (index)}
			<div class="card reply">
				<span class="head">
					<Skeleton variant="circle" width="36px" />
					<span class="lines">
						<Skeleton variant="text" width="34%" />
						<Skeleton variant="text" width="18%" height="0.75em" />
					</span>
				</span>
				<Skeleton variant="text" lines={2} />
			</div>
		{/each}
	{/if}
</div>

<style>
	.skeleton {
		display: grid;
		gap: 12px;
	}

	.card {
		display: grid;
		gap: 12px;
		padding: 16px;
		border-radius: var(--r-lg);
		background: var(--surface);
	}

	.opening {
		min-block-size: 200px;
		padding: 20px 22px;
		border-radius: var(--r-xl);
	}

	.reply {
		margin-inline-start: 28px;
		min-block-size: 110px;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 12px;
	}

	.lines {
		display: grid;
		flex: 1;
		gap: 6px;
	}
</style>
