<script lang="ts">
	import Skeleton from './Skeleton.svelte';
</script>

<!-- BookingCard's geometry: date, two lines of text, the footer's two facts. -->
<div class="card" aria-hidden="true">
	<div class="main">
		<Skeleton width="52px" height="52px" radius="15px" />
		<div class="text">
			<span class="title"><Skeleton variant="text" width="70%" /></span>
			<span class="subtitle"><Skeleton variant="text" width="35%" /></span>
		</div>
	</div>
	<div class="footer">
		<Skeleton variant="text" width="88px" />
		<Skeleton variant="text" width="64px" />
	</div>
</div>

<style>
	.card {
		display: flex;
		flex-direction: column;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.main {
		display: flex;
		align-items: center;
		gap: 14px;
		min-block-size: 84px;
		padding: 16px;
	}

	.text {
		display: grid;
		flex: 1;
		gap: 2px;
	}

	.title {
		font: var(--type-title-s);
	}

	.subtitle {
		font: var(--type-meta);
	}

	.footer {
		display: flex;
		justify-content: space-between;
		gap: 12px;
		padding: 10px 16px;
		border-radius: 0 0 var(--r-lg) var(--r-lg);
		background: color-mix(in oklch, var(--text) 4%, transparent);
		font: var(--type-ui);
	}

	.footer > :global(*) {
		inline-size: auto;
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}

		.footer {
			corner-shape: squircle;
			border-radius: 0 0 calc(var(--r-lg) * 1.5) calc(var(--r-lg) * 1.5);
		}
	}
</style>
