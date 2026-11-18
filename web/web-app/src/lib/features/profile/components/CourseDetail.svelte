<script lang="ts">
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconCalculator from '~icons/ph/calculator';
	import IconCalendarCheck from '~icons/ph/calendar-check';
	import IconCalendarX from '~icons/ph/calendar-x';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass-duotone';
	import IconScroll from '~icons/ph/scroll-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import type { ExamGrade } from '#lib/data/esse3/grades.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { bookHref } from '#lib/features/exams/links.ts';
	import { italianTitleCase } from '#lib/format/names.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCredits } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Grade from '#lib/ui/Grade.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { attempts, outcomeLabel, type Attempt, type OutcomeLabel } from '../attempts.ts';
	import { getCourseFrame } from '../courseFrame.ts';
	import { gradeText } from '../format.ts';
	import { Libretto } from '../libretto.svelte.ts';
	import { rowPrerequisitesQuery, rowQuery, rowTestsQuery } from '../queries.ts';
	import { courseKey } from '../transcript.ts';
	import { yearTitle } from './labels.ts';

	interface Props {
		/** `adsceId`. */
		id: number;
	}

	let { id }: Props = $props();

	const libretto = new Libretto();
	const frame = getCourseFrame();
	const valid = $derived(Number.isSafeInteger(id) && id > 0);

	const row = $derived(libretto.all?.find((candidate) => candidate.id === id));
	/* The row's header (year, title, facts) is held at its final size until the rows arrive. */
	const loadingRow = $derived(libretto.all === undefined && !libretto.failed && valid);
	const passed = $derived(row?.state === 'passed');
	const tests = query(() => session.current && valid && rowTestsQuery(session.current, id));
	const live = query(() => session.current && valid && rowQuery(session.current, id));
	/* Esse3 answers the check with a 422 on passed rows: it is asked only of the others. */
	const prerequisites = query(
		() => session.current && row && !passed && rowPrerequisitesQuery(session.current, id)
	);

	const list = $derived(tests.data && attempts(tests.data));
	/*
	 * The body shows once what it holds is in, the prerequisite check included for a course still
	 * to take: arriving together, nothing lands above content already on screen and pushes it.
	 */
	const settled = $derived(
		passed || prerequisites.data !== undefined || prerequisites.status === 'error'
	);
	const ready = $derived(
		row !== undefined && settled && (list !== undefined || tests.status === 'error')
	);
	const bookable = $derived(live.data?.bookableCallsNumber ?? row?.bookableCalls ?? 0);
	const title = $derived(row ? italianTitleCase(row.name) : m.profile_course_label());
	const view = $derived(page.url.searchParams.get('view') === 'credits' ? '?view=credits' : '');
	const back = $derived({
		href: resolve(`profile/exams${view}`),
		label: m.profile_libretto_title()
	});
	function attemptTitle(attempt: Attempt): string {
		if (attempt.callDate) return formatDate(attempt.callDate, 'long');
		return attempt.session ?? m.profile_course_attempt();
	}

	/* A judgment is Esse3's own word for it; the rest is a grade. */
	function labelGrade(label: Exclude<OutcomeLabel, { kind: 'judgment' }>): ExamGrade {
		switch (label.kind) {
			case 'laude':
				return { kind: 'numeric', value: 30, cumLaude: true };
			case 'grade':
				return { kind: 'numeric', value: label.value, cumLaude: false };
			case 'passed':
				return { kind: 'passed' };
		}
	}
</script>

{#snippet body()}
	{#if libretto.failed && !row}
		<ErrorState
			title={m.profile_load_error()}
			message={libretto.error ? errorMessage(libretto.error) : m.profile_course_load_failed()}
			onretry={libretto.refresh}
			retrying={libretto.refreshing}
		/>
	{:else if !valid || (libretto.all !== undefined && !row)}
		<EmptyState
			size="page"
			icon={IconMagnifyingGlass}
			title={m.profile_course_not_found_title()}
			body={m.profile_course_not_found_body()}
		>
			{#snippet action()}
				<Button href={back.href}>{m.profile_libretto_open()}</Button>
			{/snippet}
		</EmptyState>
	{:else if !ready || !row}
		<div class="course" aria-busy="true">
			<Skeleton variant="text" width="40%" />
			<div class="skeletons">
				<Skeleton height="64px" radius="var(--r-sm)" />
				<Skeleton height="64px" radius="var(--r-sm)" />
			</div>
		</div>
	{:else}
		<div class="course">
			{#if passed}
				<div class={['outcome', { laude: row.grade?.cumLaude }]}>
					<IconCheckCircle class="check" aria-hidden="true" />
					<div class="when">
						<p class="passed">{m.profile_course_passed()}</p>
						{#if row.date}<p class="date">{formatDate(row.date, 'long')}</p>{/if}
					</div>
					{#if row.grade}
						<p class={['mark', { laude: row.grade.cumLaude }]}>
							<span aria-hidden="true">{gradeText(row.grade)}</span>
							<span class="sr-only">{gradeText(row.grade, true)}</span>
						</p>
					{:else}
						<p class="mark word">{m.common_grade_passed_full()}</p>
					{/if}
				</div>
			{/if}

			{#if prerequisites.data === 'unsatisfied'}
				<Banner tone="warning" message={m.profile_course_prereq_unmet()} />
			{/if}

			<section class="attempts">
				{#if list === undefined}
					<ErrorState
						title={m.profile_course_load_failed()}
						message={tests.error ? errorMessage(tests.error) : m.profile_course_load_failed()}
						onretry={tests.refresh}
						retrying={tests.refreshing}
					/>
				{:else if list.length > 0}
					<SectionHeader title={m.profile_course_attempts()} variant="label" />
					<ListGroup label={m.profile_course_attempts()}>
						{#each list as attempt (attempt.id)}
							{@const label = outcomeLabel(attempt.outcome)}
							<ListRow
								title={attemptTitle(attempt)}
								description={attempt.status ?? attempt.session ?? undefined}
							>
								{#snippet leading()}
									<span class={['dot', { ok: attempt.outcome?.passed }]} aria-hidden="true"></span>
								{/snippet}
								{#snippet trailing()}
									{#if label?.kind === 'judgment'}
										<span class="judgment">{label.text}</span>
									{:else if label}
										<Grade grade={labelGrade(label)} />
									{/if}
								{/snippet}
							</ListRow>
						{/each}
					</ListGroup>
				{:else if !passed}
					<EmptyState
						size="panel"
						icon={IconScroll}
						title={m.profile_course_no_exams_title()}
						body={m.profile_course_no_exams_body()}
					/>
				{/if}
			</section>

			{#if !passed}
				<div class="cta">
					{#if bookable > 0}
						<Button
							variant="solid"
							size="lg"
							full
							icon={IconCalendarCheck}
							trailingIcon={IconArrowRight}
							href={bookHref({ course: courseKey(row) })}
						>
							{m.profile_course_go_to_appelli()}
						</Button>
						<p class="hint">{m.profile_course_bookable({ count: bookable })}</p>
					{:else}
						<Button size="lg" full icon={IconCalendarX} disabled>
							{m.profile_course_no_appelli()}
						</Button>
					{/if}
					<Button
						variant="ghost"
						icon={IconCalculator}
						href={resolve(`profile/simulator?exam=${row.id}`)}
					>
						{m.profile_course_simulate()}
					</Button>
				</div>
			{/if}
		</div>
	{/if}
{/snippet}

<svelte:head>
	<title>{title} · {m.profile_libretto_title()} · {m.app_name()}</title>
</svelte:head>

{#snippet facts()}
	{#if row}
		<dl class="facts">
			<div>
				<dt class="sr-only">{m.profile_fact_credits()}</dt>
				<dd>{formatCredits(row.credits)}</dd>
			</div>
			{#if row.academicYear !== null}
				<div>
					<dt class="sr-only">{m.profile_fact_academic_year()}</dt>
					<dd>{academicYearLabel(row.academicYear)}</dd>
				</div>
			{/if}
			{#if row.code}
				<div>
					<dt class="sr-only">{m.profile_fact_code()}</dt>
					<dd class="code" translate="no">{row.code}</dd>
				</div>
			{/if}
		</dl>
	{:else if loadingRow}
		<div class="facts" aria-hidden="true">
			<Skeleton width="68px" height="26px" radius="var(--r-pill)" />
			<Skeleton width="80px" height="26px" radius="var(--r-pill)" />
			<Skeleton width="104px" height="26px" radius="var(--r-pill)" />
		</div>
	{/if}
{/snippet}

{#if frame.docked}
	<article class="detail pane">
		<DetailHeader
			title={loadingRow ? '' : title}
			eyebrow={row ? yearTitle(row.year) : undefined}
			loading={loadingRow}
		>
			{@render facts()}
		</DetailHeader>
		{@render body()}
	</article>
{:else}
	<!-- While the rows load, bars and the facts' skeleton hold the header's size. -->
	<div class="detail">
		<PageHeader
			title={loadingRow ? '' : title}
			eyebrow={row ? yearTitle(row.year) : undefined}
			loading={loadingRow ? { title: 1, subtitle: false } : false}
			{back}
			meta={row || loadingRow ? facts : undefined}
		/>
		{@render body()}
	</div>
{/if}

<style>
	.detail {
		display: grid;
		gap: var(--sp-6);
		inline-size: 100%;
		max-inline-size: var(--w-reading);
		margin-inline: auto;
	}

	.pane {
		margin-inline: 0;
		padding-block: var(--sp-2) var(--sp-8);
	}

	.facts {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
		margin: 0;
	}

	.facts dd {
		margin: 0;
		padding: 3px 10px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text-muted);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}

	.facts .code {
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.02em;
	}

	.course {
		display: grid;
		gap: var(--sp-6);
	}

	.outcome {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: 14px;
		padding: 18px 20px;
		border-radius: var(--r-lg);
		background: var(--success-container);
		color: var(--on-success-container);
	}

	/* Honours: the gold of 30 cum laude takes over the card. */
	.outcome.laude {
		background: var(--gold-container);
		color: var(--on-gold-container);
	}

	.outcome.laude :global(.check) {
		color: var(--gold);
	}

	.outcome :global(.check) {
		inline-size: 28px;
		block-size: 28px;
		color: var(--success);
	}

	.passed {
		font: var(--type-title-s);
	}

	.date {
		font: var(--type-meta);
		opacity: 0.85;
	}

	.mark {
		font: var(--type-numeral-l);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.mark.word {
		font: var(--type-title-l);
	}

	.mark.laude {
		color: var(--gold);
	}

	.attempts {
		display: grid;
		gap: var(--sp-3);
	}

	.skeletons {
		display: grid;
		gap: 2px;
	}

	.dot {
		display: block;
		inline-size: 10px;
		block-size: 10px;
		margin-inline: 4px;
		border-radius: 50%;
		background: var(--border-strong);
	}

	.dot.ok {
		background: var(--success-solid);
	}

	.judgment {
		font: 600 1.0625rem / 1.2 var(--font-ui);
		white-space: nowrap;
	}

	.cta {
		display: grid;
		gap: 8px;
		justify-items: center;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
