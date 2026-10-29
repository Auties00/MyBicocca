<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<div class="pass" aria-hidden="true">
	<div class="main">
		<Skeleton variant="text" width="34%" />
		<span class="day"><Skeleton variant="text" width="62%" /></span>
		<div class="big">
			<Skeleton width="104px" height="56px" radius="10px" />
			<Skeleton width="60%" height="56px" radius="10px" />
		</div>
	</div>
	<div class="stub">
		<Skeleton width="148px" height="148px" radius="14px" />
		<div class="lines">
			<Skeleton variant="text" width="50%" />
			<span class="code"><Skeleton variant="text" width="70%" /></span>
			<Skeleton variant="text" lines={2} />
		</div>
	</div>
</div>

<style>
	.pass {
		display: grid;
		gap: 2px;
	}

	.main,
	.stub {
		padding: var(--sp-5);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.main {
		display: grid;
		gap: var(--sp-3);
		padding-block-end: var(--sp-6);
		border-radius: var(--r-lg) var(--r-lg) 0 0;
	}

	.day {
		font: var(--type-title-m);
	}

	.big {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: var(--sp-6);
		margin-block-start: var(--sp-2);
	}

	.stub {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: var(--sp-5);
		border-radius: 0 0 var(--r-lg) var(--r-lg);
	}

	.lines {
		display: grid;
		gap: var(--sp-2);
		font: var(--type-meta);
	}

	.code {
		font: 600 1.375rem / 1.2 var(--font-ui);
	}
</style>
