<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import Card from '#lib/ui/Card.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';

	const YEARS = [7, 8, 6];
</script>

<div class="skeleton" role="status" aria-label={m.studyplan_loading()}>
	<Card padding="lg">
		<div class="summary">
			<div class="head">
				<div class="stat">
					<span class="ui"><Skeleton variant="text" width="8rem" /></span>
					<Skeleton width="6.5rem" height="2.5rem" />
					<span class="meta"><Skeleton variant="text" width="10rem" /></span>
				</div>
				<Skeleton width="7rem" height="28px" radius="var(--r-pill)" />
			</div>
			<div class="meter">
				<Skeleton height="8px" radius="var(--r-pill)" />
				<span class="meta"><Skeleton variant="text" width="11rem" /></span>
			</div>
		</div>
	</Card>
	<div class="years">
		{#each YEARS as rows, index (index)}
			<div class="group">
				<div class="segment head-segment">
					<Skeleton width="5rem" height="26px" />
					<Skeleton width="9rem" height="18px" />
				</div>
				{#each { length: rows }, row (row)}
					<div class="segment row">
						<Skeleton width="44px" height="44px" />
						<div class="lines">
							<span class="name">
								<Skeleton variant="text" width={row % 2 === 0 ? '70%' : '55%'} />
							</span>
							<span class="meta"><Skeleton variant="text" width="5.5rem" /></span>
						</div>
					</div>
				{/each}
			</div>
		{/each}
	</div>
</div>

<style>
	.skeleton {
		display: grid;
		gap: var(--page-gap, var(--sp-6));
	}

	.summary {
		display: grid;
		gap: var(--sp-5);
	}

	.head {
		display: flex;
		justify-content: space-between;
		gap: var(--sp-4);
	}

	.stat {
		display: grid;
		gap: 6px;
	}

	.meter {
		display: grid;
		gap: 8px;
	}

	.ui {
		font: var(--type-ui);
	}

	.meta {
		font: var(--type-meta);
	}

	.name {
		font-size: 0.9375rem;
		line-height: 1.35;
	}

	.years {
		display: grid;
		gap: var(--sp-6);
	}

	.group {
		display: grid;
		gap: 2px;
	}

	.segment {
		background: var(--surface);
		border-radius: var(--r-2xs);
	}

	.head-segment {
		display: grid;
		justify-items: start;
		gap: 2px;
		padding: 18px 16px 14px;
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.row {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 68px;
		padding: 10px 16px;
	}

	.row:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.lines {
		flex: 1;
		display: grid;
		gap: 2px;
		padding-block: 4px;
	}

	@media (pointer: fine) {
		.row {
			min-block-size: 64px;
			padding-block: 8px;
		}
	}

	@container page (width >= 1000px) {
		.years {
			grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
			align-items: start;
		}
	}
</style>
