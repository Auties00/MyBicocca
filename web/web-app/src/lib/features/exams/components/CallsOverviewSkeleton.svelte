<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="overview" aria-hidden="true">
	<div class="head">
		<span class="title"><Skeleton variant="text" width="44%" /></span>
		<span class="hint"><Skeleton variant="text" width="58%" /></span>
	</div>
	<div class="body">
		<div class="month">
			<span class="month-name"><Skeleton variant="text" width="40%" /></span>
			<!-- The weekday row and six weeks of 44 px days. -->
			<Skeleton height="292px" radius="12px" />
			<span class="legend"><Skeleton variant="text" width="70%" /></span>
		</div>
		<div class="agenda">
			<span class="agenda-title"><Skeleton variant="text" width="60%" /></span>
			<Skeleton height="64px" radius="var(--r-lg) var(--r-lg) var(--r-2xs) var(--r-2xs)" />
			<Skeleton height="64px" radius="var(--r-2xs) var(--r-2xs) var(--r-lg) var(--r-lg)" />
		</div>
	</div>
</div>

<style>
	.overview {
		display: grid;
		gap: var(--sp-5);
		container: overview-skeleton / inline-size;
	}

	.head {
		display: grid;
		gap: 4px;
	}

	.title {
		font: var(--type-title-m);
	}

	.hint,
	.legend {
		font: var(--type-meta);
	}

	.body {
		display: grid;
		gap: var(--sp-5);
		align-items: start;
	}

	@container overview-skeleton (width >= 600px) {
		.body {
			grid-template-columns: minmax(300px, 340px) minmax(0, 1fr);
		}
	}

	.month {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-3) var(--sp-4) var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.month-name {
		display: flex;
		justify-content: center;
		padding-block: 8px;
		font: var(--type-title-s);
	}

	.legend {
		padding-block-start: var(--sp-3);
		border-block-start: 1px solid var(--hairline);
	}

	.agenda {
		display: grid;
		gap: 2px;
	}

	.agenda-title {
		margin-block-end: calc(var(--sp-3) - 2px);
		font: var(--type-title-s);
	}
</style>
