<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
</script>

<!-- DegreeHero's geometry: the category, the grade at the kit's numeral size, the status. -->
<div class="hero" aria-hidden="true">
	<span class="overline"><Skeleton variant="text" width="8rem" /></span>
	<div class="stat">
		<span class="label"><Skeleton variant="text" width="3rem" /></span>
		<Skeleton width="7rem" height="clamp(3rem, 2.2rem + 3vw, 4rem)" radius="var(--r-sm)" />
	</div>
	<Skeleton width="6.5rem" height="26px" radius="var(--r-pill)" />
</div>

<style>
	.hero {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.overline {
		font: var(--type-overline);
	}

	.stat {
		display: grid;
		gap: 6px;
	}

	.label {
		font: var(--type-ui);
	}

	@supports (corner-shape: squircle) {
		.hero {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
