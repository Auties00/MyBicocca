<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { degreeHref, useDegrees } from '../degrees.svelte.ts';
	import DegreeHero from './DegreeHero.svelte';
	import HeroSkeleton from './HeroSkeleton.svelte';

	const degrees = useDegrees();
	const latest = $derived(degrees.latest);
	const loading = $derived(degrees.query.data === undefined);
</script>

<!-- Beside the qualifications with none open: how many, and the latest at a glance. -->
{#if loading || latest}
	<div class="overview" aria-busy={loading || undefined}>
		<div class="lead">
			<p class="statement">
				{#if latest}
					{m.documents_degrees_overview({ count: degrees.titles.length })}
				{:else}
					<Skeleton variant="text" width="60%" />
				{/if}
			</p>
			<p class="hint">{m.documents_degrees_overview_select()}</p>
		</div>
		<div class="latest">
			{#if latest}
				<DegreeHero title={latest} />
				<div>
					<Button href={degreeHref(latest.id)} trailingIcon={IconArrowRight}>
						{m.documents_degrees_overview_open()}
					</Button>
				</div>
			{:else}
				<HeroSkeleton />
				<Skeleton width="11rem" height="var(--control-h)" radius="var(--r-sm)" />
			{/if}
		</div>
	</div>
{/if}

<style>
	.overview {
		display: grid;
		gap: var(--sp-6);
		max-inline-size: 560px;
		padding-block: var(--sp-2) var(--sp-4);
	}

	.lead {
		display: grid;
		gap: var(--sp-2);
	}

	.statement {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	.hint {
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.latest {
		display: grid;
		gap: var(--sp-3);
	}
</style>
