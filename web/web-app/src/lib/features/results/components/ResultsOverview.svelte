<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconCheck from '~icons/ph/check-bold';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import IconX from '~icons/ph/x-bold';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import { resultHref } from '../format.ts';
	import { summarizeArchive } from '../model.ts';
	import { useResultsView } from '../results.svelte.ts';

	const view = useResultsView();
	const pending = $derived(view.results.split.pending);
	const summary = $derived(summarizeArchive(view.results.split.archived));
	const loaded = $derived(view.results.query.data !== undefined);
	const id = $props.id();

	const counts = $derived(
		[
			{ label: m.results_count_passed(), value: summary.passed },
			{ label: m.results_count_not_passed(), value: summary.notPassed },
			{ label: m.results_count_absences(), value: summary.absences }
		].filter((count) => count.value > 0)
	);
</script>

<!--
	Beside the list with nothing open. With grades to decide: what waits, and what accepting or
	rejecting means. With none: what the archive holds, since the list already says all is settled.
-->
{#if loaded}
	<div class="overview">
		{#if pending.length > 0}
			<div class="lead">
				<p class="statement">{m.results_overview_pending({ count: pending.length })}</p>
				<p class="hint">{m.results_overview_select()}</p>
				<div><Button href={resultHref(pending[0].id)}>{m.results_overview_decide()}</Button></div>
			</div>

			<section class="card rules" aria-labelledby="{id}-rules">
				<h2 class="overline" id="{id}-rules">{m.results_rules_heading()}</h2>
				<ul class="items" role="list">
					<li>
						<span class="icon accept"><IconCheck aria-hidden="true" /></span>
						{m.results_rule_accept()}
					</li>
					<li>
						<span class="icon reject"><IconX aria-hidden="true" /></span>
						{m.results_rule_reject()}
					</li>
					<li>
						<span class="icon"><IconHourglass aria-hidden="true" /></span>
						{m.results_rule_deadline()}
					</li>
				</ul>
			</section>
		{:else}
			<section class="card summary" aria-labelledby="{id}-summary">
				<h2 class="overline" id="{id}-summary">{m.results_archive_summary_heading()}</h2>
				{#if counts.length > 0}
					<dl class="counts">
						{#each counts as count (count.label)}
							<div class="count">
								<dt>{count.label}</dt>
								<dd>{formatNumber(count.value)}</dd>
							</div>
						{/each}
					</dl>
				{/if}
				<div class="footer">
					<Button
						variant="ghost"
						size="sm"
						trailingIcon={IconArrowRight}
						href={shellHref('profile/exams')}
					>
						{m.results_go_transcript()}
					</Button>
				</div>
			</section>

			<p class="hint">{m.results_overview_select()}</p>
		{/if}
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
	}

	.lead > div {
		margin-block-start: var(--sp-2);
	}

	.card {
		display: grid;
		gap: var(--sp-3);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.overline {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.items {
		display: grid;
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.items li {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-3);
		text-wrap: pretty;
	}

	.icon {
		display: grid;
		flex: none;
		place-items: center;
		inline-size: 26px;
		block-size: 26px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.icon :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.accept {
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.reject {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.counts {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
		gap: var(--sp-4);
		margin: 0;
	}

	.count {
		display: flex;
		flex-direction: column-reverse;
		justify-content: flex-end;
		gap: 2px;
	}

	.count dt {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: balance;
	}

	.count dd {
		margin: 0;
		font: var(--type-title-m);
		font-size: 1.75rem;
		line-height: 1.1;
		font-variant-numeric: tabular-nums;
	}

	.footer {
		margin-inline-start: -12px;
		margin-block-end: calc(-1 * var(--sp-2));
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
