<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="ticket" aria-hidden="true">
	<div class="main">
		<span class="overline"><Skeleton variant="text" width="44%" /></span>
		<span class="day"><Skeleton variant="text" width="58%" /></span>
		<div class="big">
			<Skeleton width="96px" height="40px" radius="10px" />
			<Skeleton width="120px" height="40px" radius="10px" />
		</div>
	</div>
	<div class="stub">
		{#each { length: 4 }, index (index)}
			<span class="fact">
				<Skeleton variant="text" width="50%" />
				<Skeleton variant="text" width="80%" />
			</span>
		{/each}
	</div>
</div>
<div class="actions" aria-hidden="true">
	<Skeleton width="124px" height="44px" radius="var(--r-sm)" />
	<Skeleton width="164px" height="44px" radius="var(--r-sm)" />
</div>

<style>
	.ticket {
		display: grid;
		gap: 2px;
	}

	.main,
	.stub {
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.main {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5) var(--sp-5) var(--sp-6);
		border-radius: var(--r-lg) var(--r-lg) var(--r-2xs) var(--r-2xs);
	}

	.overline {
		font: var(--type-overline);
	}

	.day {
		font: var(--type-title-m);
	}

	.big {
		display: flex;
		gap: var(--sp-6);
		margin-block-start: var(--sp-2);
		padding-block-end: 18px;
	}

	.stub {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: var(--sp-4) var(--sp-5);
		padding: var(--sp-5);
		border-radius: var(--r-2xs) var(--r-2xs) var(--r-lg) var(--r-lg);
	}

	.fact {
		display: grid;
		gap: 2px;
		font: var(--type-body-strong);
	}

	.actions {
		display: flex;
		gap: var(--sp-2);
		margin-block-start: var(--page-gap);
	}
</style>
