<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="card" aria-hidden="true">
	<div class="photo"><Skeleton variant="block" height="100%" radius="0" /></div>
	<div class="body">
		<div class="text">
			<Skeleton width="60%" />
			<Skeleton width="40%" />
			<Skeleton width="50%" />
		</div>
		<Skeleton variant="circle" width="52px" />
	</div>
</div>

<style>
	.card {
		overflow: hidden;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.photo {
		aspect-ratio: 2 / 1;
	}

	.body {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		padding: 14px 16px 16px;
	}

	.text {
		display: grid;
		gap: 8px;
		flex: 1;
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
