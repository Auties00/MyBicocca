<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<!-- The open-calls row's size, so the past bookings under it stay put when the calls arrive. -->
<div class="row" aria-hidden="true">
	<Skeleton width="40px" height="40px" radius="12px" />
	<div class="text">
		<span class="title"><Skeleton variant="text" width="56%" /></span>
		<span class="meta"><Skeleton variant="text" width="72%" /></span>
	</div>
</div>

<style>
	.row {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 60px;
		padding: 10px 16px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.text {
		display: grid;
		flex: 1;
		gap: 2px;
		padding-block: 4px;
	}

	.title {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		line-height: 1.35;
	}

	.meta {
		font: var(--type-meta);
	}

	@media (pointer: fine) {
		.row {
			min-block-size: 52px;
			padding-block: 8px;
		}
	}
</style>
