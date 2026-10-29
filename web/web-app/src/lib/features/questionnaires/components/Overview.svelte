<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconUsers from '~icons/ph/users-three';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { activityLine } from '../format.ts';
	import { activityHref, compileHref } from '../links.ts';
	import { unitTitle } from '../model.ts';
	import { ActivityUnitsQuery, useQuestionnairesView } from '../questionnaires.svelte.ts';
	import StatusGlyph from './StatusGlyph.svelte';

	const view = useQuestionnairesView();
	const list = view.questionnaires.query;
	const loaded = $derived(list.data !== undefined);
	const gate = new LoadingGate(() => !loaded && list.status === 'pending');
	const next = $derived(view.questionnaires.pending[0]);
	/* The same request its page makes, so opening it is instant. */
	const units = new ActivityUnitsQuery(() => next?.id);
	const waiting = $derived.by(() => {
		const data = units.units;
		return data?.questionnaire ? data.units.filter((unit) => !unit.done) : [];
	});
	const meta = $derived(
		next &&
			[
				activityLine(next),
				waiting.length > 0 ? m.questionnaires_overview_waiting({ count: waiting.length }) : ''
			]
				.filter(Boolean)
				.join(' · ')
	);
	/*
	 * One lecturer left: straight to their questionnaire, which waits for the student's go before
	 * anything starts. Otherwise the course, to pick whom to evaluate.
	 */
	const only = $derived(next && waiting.length === 1 ? waiting[0] : undefined);

	const id = $props.id();
</script>

{#if loaded}
	<div class="overview">
		{#if next}
			<section class="next" aria-labelledby="{id}-next">
				<StatusGlyph status={next.status} size={48} />
				<div class="words">
					<p class="overline">{m.questionnaires_overview_next()}</p>
					<h2 class="name" id="{id}-next">{next.name}</h2>
					<p class="meta">{meta}</p>
				</div>
				<div class="action">
					{#if only}
						<Button
							variant="solid"
							trailingIcon={IconArrowRight}
							href={compileHref(next.id, only.index)}
							label={m.questionnaires_overview_start_label({ name: unitTitle(only) })}
						>
							{m.questionnaires_overview_start()}
						</Button>
					{:else}
						<Button variant="solid" trailingIcon={IconArrowRight} href={activityHref(next.id)}>
							{m.questionnaires_overview_open()}
						</Button>
					{/if}
				</div>
			</section>
		{/if}

		<section class="rules" aria-labelledby="{id}-rules">
			<h2 class="rules-title" id="{id}-rules">{m.questionnaires_rules_heading()}</h2>
			<p class="rule">
				<span class="icon"><IconUsers aria-hidden="true" /></span>
				{m.questionnaires_rule_units()}
			</p>
		</section>
	</div>
{:else if gate.visible}
	<div class="overview" aria-hidden="true">
		<div class="next">
			<Skeleton variant="circle" width="48px" />
			<div class="words">
				<Skeleton variant="text" width="5rem" />
				<Skeleton variant="text" width="60%" height="1.6rem" />
				<Skeleton variant="text" width="45%" />
			</div>
			<div class="action"><Skeleton width="96px" height="var(--control-h)" /></div>
		</div>
		<div class="rules">
			<span class="rules-title"><Skeleton variant="text" width="7rem" /></span>
			<div class="rule">
				<Skeleton variant="circle" width="26px" />
				<Skeleton variant="text" width="88%" />
			</div>
		</div>
	</div>
{/if}

<style>
	.overview {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: 560px;
		padding-block: var(--sp-2) var(--sp-4);
	}

	.next,
	.rules {
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.next {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-4);
	}

	.words {
		display: grid;
		gap: 2px;
	}

	.overline {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.name {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	.meta {
		min-block-size: 1lh;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.action {
		grid-column: 2;
	}

	.rules {
		display: grid;
		gap: var(--sp-3);
	}

	.rules-title {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.rule {
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
		background: var(--container);
		color: var(--on-container);
	}

	.icon :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	@supports (corner-shape: squircle) {
		.next,
		.rules {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
