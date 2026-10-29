<script lang="ts">
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconSealQuestion from '~icons/ph/seal-question-duotone';
	import { session } from '#lib/data/session.svelte.ts';
	import { bookingHref } from '#lib/features/exams/links.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import { decide, decisionFailure } from '../decide.ts';
	import { detailSubtitle, gradeInWords, resultTitle } from '../format.ts';
	import { attempts, visibleChoice, type Choice, type ExamResult } from '../model.ts';
	import { useResultsView } from '../results.svelte.ts';
	import ArchiveRow from './ArchiveRow.svelte';
	import DecisionCard from './DecisionCard.svelte';
	import DetailSkeleton from './DetailSkeleton.svelte';
	import OutcomePanel from './OutcomePanel.svelte';
	import ResultHero from './ResultHero.svelte';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();

	const view = useResultsView();
	const query = view.results.query;
	const result = $derived(view.results.find(id));
	const loaded = $derived(query.data !== undefined);
	const gate = new LoadingGate(() => query.status === 'pending');

	/** The decision taken here, reported where it was asked until the student moves on. */
	let outcome = $state<{ id: string; choice: Choice }>();
	let confirming = $state<Choice>();
	let deciding = $state(false);
	let outcomeHeading = $state<HTMLElement>();
	/* The dialog keeps its words while it closes over a result that just changed. */
	let asked = $state<ExamResult>();

	const decided = $derived(outcome?.id === id ? outcome.choice : undefined);
	/*
	 * An answer whose reply didn't decode counts as taken: it stands until the reload says the
	 * result still waits, and then the decision is offered again.
	 */
	const confirmed = $derived(
		decided !== undefined && result !== undefined && (result.choice === decided || !result.pending)
	);
	const others = $derived(
		result ? attempts(result, [...view.results.split.pending, ...view.results.split.archived]) : []
	);
	/* A grade accepted, or left standing past its deadline, belongs to the record book. */
	const recorded = $derived(
		result !== undefined && (visibleChoice(result) === 'accepted' || result.lapsed)
	);
	/* Under the page's title alone, or under the pane's beside the list. */
	const headingLevel = $derived(view.wide ? 3 : 2);
	const title = $derived(result ? resultTitle(result) : m.results_title());
	const uid = $props.id();

	function choose(choice: Choice) {
		asked = result;
		confirming = choice;
	}

	async function confirm(choice: Choice) {
		const scope = session.current;
		if (!scope || !asked) return;
		deciding = true;
		try {
			await decide(scope, asked, choice);
		} finally {
			deciding = false;
		}
		outcome = { id: asked.id, choice };
		confirming = undefined;
	}

	const dialogMessage = (choice: Choice) => {
		if (!asked) return '';
		const words = { exam: resultTitle(asked), grade: gradeInWords(asked.grade) };
		return choice === 'accepted' ? m.results_accept_body(words) : m.results_reject_body(words);
	};

	function guard(event: BeforeUnloadEvent) {
		if (deciding) event.preventDefault();
	}
</script>

<svelte:head>
	<title>{result ? `${title} · ` : ''}{m.results_title()} · {m.app_name()}</title>
</svelte:head>

<svelte:window onbeforeunload={guard} />

{#if !loaded}
	<div class="detail" aria-busy="true">
		<DetailHeader title="" loading />
		{#if gate.visible}<DetailSkeleton />{/if}
	</div>
{:else if !result}
	<EmptyState
		size="page"
		icon={IconSealQuestion}
		title={m.results_not_found_title()}
		body={m.results_not_found_body()}
	>
		{#snippet action()}
			<Button href={shellHref('services/results')}>{m.results_back_to_list()}</Button>
		{/snippet}
	</EmptyState>
{:else}
	<article class="detail">
		<DetailHeader
			{title}
			eyebrow={result.code ?? undefined}
			eyebrowCode
			subtitle={detailSubtitle(result)}
		/>

		<ResultHero {result} chip={!confirmed} />

		{#if decided && confirmed}
			<OutcomePanel
				choice={decided}
				code={result.code}
				level={headingLevel}
				bind:heading={outcomeHeading}
			/>
		{:else if result.pending}
			{#if decided}
				<Banner
					tone="warning"
					title={m.results_unconfirmed_title()}
					message={m.results_unconfirmed_body()}
					role="status"
				>
					{#snippet actions()}
						<Button
							variant="outline"
							size="sm"
							loading={query.refreshing}
							onclick={() => query.refresh()}
						>
							{m.results_check_again()}
						</Button>
					{/snippet}
				</Banner>
			{/if}
			<DecisionCard {result} level={headingLevel} onchoose={choose} />
		{/if}

		{#if result.note}
			<blockquote class="note">
				<p class="note-label">{m.results_note_label()}</p>
				<p class="note-text">{result.note}</p>
			</blockquote>
		{/if}

		{#if others.length > 0}
			<section class="attempts" aria-labelledby="{uid}-attempts">
				<SectionHeader
					id="{uid}-attempts"
					title={m.results_attempts_heading()}
					level={headingLevel}
					variant="label"
				/>
				<ListGroup>
					{#each others as attempt (attempt.id)}
						<ArchiveRow result={attempt} attempt />
					{/each}
				</ListGroup>
			</section>
		{/if}

		<!-- The booking's page holds the rest: the call, the room, the certificate of attendance. -->
		<div class="related">
			<Button
				variant="ghost"
				size="sm"
				trailingIcon={IconArrowRight}
				href={bookingHref(result.booking)}
			>
				{m.results_booking_link()}
			</Button>
			{#if recorded && !confirmed}
				<Button
					variant="ghost"
					size="sm"
					trailingIcon={IconArrowRight}
					href={shellHref('profile/exams')}
				>
					{m.results_go_transcript()}
				</Button>
			{/if}
		</div>
	</article>
{/if}

<ConfirmDialog
	open={confirming === 'accepted'}
	title={m.results_accept_title()}
	message={dialogMessage('accepted')}
	confirmLabel={m.results_accept_confirm()}
	onconfirm={() => confirm('accepted')}
	errorMessage={decisionFailure}
	oncancel={() => (confirming = undefined)}
	returnFocus={() => outcomeHeading}
/>
<ConfirmDialog
	open={confirming === 'rejected'}
	title={m.results_reject_title()}
	message={dialogMessage('rejected')}
	confirmLabel={m.results_reject_confirm()}
	tone="danger"
	onconfirm={() => confirm('rejected')}
	errorMessage={decisionFailure}
	oncancel={() => (confirming = undefined)}
	returnFocus={() => outcomeHeading}
/>

<style>
	.detail {
		display: grid;
		gap: var(--sp-4);
		max-inline-size: var(--w-reading);
	}

	.attempts {
		display: grid;
		gap: var(--sp-2);
		margin-block-start: var(--sp-2);
	}

	.related {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-1) var(--sp-2);
		margin-inline-start: -12px;
	}

	.note {
		display: grid;
		gap: 4px;
		margin: 0;
		padding: var(--sp-3) var(--sp-4);
		border-inline-start: 3px solid var(--accent);
		border-radius: var(--r-2xs) var(--r-sm) var(--r-sm) var(--r-2xs);
		background: var(--surface-sunken);
	}

	.note-label {
		color: var(--text-muted);
		font: var(--type-label);
	}

	.note-text {
		white-space: pre-line;
		overflow-wrap: anywhere;
		text-wrap: pretty;
	}
</style>
