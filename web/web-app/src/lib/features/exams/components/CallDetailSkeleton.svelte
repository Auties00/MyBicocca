<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="call" aria-hidden="true">
	<div class="panel">
		<div class="when">
			<Skeleton width="64px" height="64px" radius="18px" />
			<div class="text">
				<span class="date"><Skeleton variant="text" width="70%" /></span>
				<Skeleton width="96px" height="22px" radius="var(--r-pill)" />
			</div>
		</div>
		<div class="book">
			<Skeleton variant="text" lines={2} />
			<Skeleton width="180px" height="48px" radius="var(--r-sm)" />
		</div>
	</div>
	<Skeleton height="186px" radius="var(--r-lg)" />
</div>

<style>
	.call {
		display: grid;
		gap: var(--sp-5);
	}

	.panel {
		display: grid;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.when {
		display: flex;
		align-items: center;
		gap: var(--sp-4);
		padding: var(--sp-5);
	}

	.text {
		display: grid;
		flex: 1;
		gap: 8px;
	}

	.date {
		font: var(--type-title-m);
	}

	.book {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-4) var(--sp-5) var(--sp-5);
		border-block-start: 1px solid var(--hairline);
	}
</style>
