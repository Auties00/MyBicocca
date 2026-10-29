<script lang="ts">
	import { resolve } from '$app/paths';
	import IconBookOpen from '~icons/ph/book-open-text-duotone';
	import IconCaretDown from '~icons/ph/caret-down';
	import IconFilePdf from '~icons/ph/file-pdf';
	import IconPencil from '~icons/ph/pencil-simple-line';
	import IconPrinter from '~icons/ph/printer';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
	import { toLibrettoRow, type LibrettoRow } from '#lib/features/profile/transcript.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { openPdf } from '#lib/ui/files.ts';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Menu, { type MenuEntry } from '#lib/ui/Menu.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import { StudyPlanData } from '../data.svelte.ts';
	import { planSubtitle } from '../labels.ts';
	import { groupByYear } from '../plan.ts';
	import PlanSkeleton from './PlanSkeleton.svelte';
	import PlanSummary from './PlanSummary.svelte';
	import PrintSheet from './PrintSheet.svelte';
	import YearGroup from './YearGroup.svelte';

	const data = new StudyPlanData();
	const records = query(() => session.current && recordBookRowsQuery(session.current));

	/* The path decides whether editing is offered: the page waits for it too, so nothing moves. */
	const pending = $derived(data.status === 'pending' || data.path === undefined);
	const gate = new LoadingGate(() => pending);
	/** The skeleton holds the plan's place from the start; it fades in only after 150 ms. */
	const loading = $derived(pending || gate.visible);

	const plan = $derived(data.plan);
	const updatedAt = $derived(data.updatedAt ? new Date(data.updatedAt) : undefined);
	const stale = $derived(data.error !== undefined && plan !== undefined);
	/** Editing needs a fresh plan: a stale one may predate the plan it would replace. */
	const editable = $derived(data.request !== undefined && !stale);
	/* Edit path is a header action, so whether the window is open never moves the page. */
	const canEdit = $derived(!!plan && editable);
	const windowText = $derived(
		data.path?.windowEnd
			? m.studyplan_window_until({ date: formatDate(data.path.windowEnd, 'long') })
			: m.studyplan_window_open()
	);
	/* The two printouts share one menu, so Edit path fits on their row at any width. */
	const printouts: MenuEntry[] = [
		{
			label: m.studyplan_print_page(),
			description: m.studyplan_print_page_hint(),
			icon: IconPrinter,
			onselect: () => window.print()
		},
		{
			label: m.studyplan_official_pdf(),
			description: m.studyplan_official_pdf_hint(),
			icon: IconFilePdf,
			onselect: () => void openOfficial()
		}
	];

	const byCode = $derived(
		new Map(
			(records.data ?? []).flatMap((row) => {
				const item = toLibrettoRow(row);
				return item.code ? [[item.code.toUpperCase(), item] as const] : [];
			})
		)
	);
	const recordOf = (code: string | null): LibrettoRow | undefined =>
		code ? byCode.get(code.toUpperCase()) : undefined;
	const years = $derived(
		plan ? groupByYear(plan.courses, (course) => recordOf(course.code)?.state === 'passed') : []
	);
	const earned = $derived(
		records.data === undefined ? undefined : years.reduce((sum, year) => sum + year.earned, 0)
	);

	/* The subtitle lands complete, once both the plan and the path are known. */
	const subtitleLoading = $derived(
		plan === undefined ? data.status === 'pending' : data.path === undefined
	);
	const subtitle = $derived(
		plan === undefined || data.path === undefined ? undefined : planSubtitle(plan, data.path)
	);

	const editHref = resolve('services/study-plan/edit');

	const student = $derived({
		name: session.current?.account.displayName ?? '',
		number: session.current?.career.studentNumber ?? '',
		degree: session.current?.career.description ?? ''
	});

	let downloading = $state(false);
	/** Why the official PDF couldn't be opened; the banner stays while Retry runs. */
	let pdfError = $state<string>();

	async function openOfficial() {
		const scope = session.current;
		if (!scope || !plan || downloading) return;
		downloading = true;
		const { studentId, planId } = plan;
		try {
			await openPdf(
				() => scope.api.esse3.plans.getPlanPrint(studentId, planId),
				`piano_di_studi_${planId}.pdf`,
				m.studyplan_pdf_waiting()
			);
			pdfError = undefined;
		} catch (error) {
			const failure = toDataError(error);
			pdfError =
				failure.kind === 'rejected' || failure.kind === 'not_found'
					? m.studyplan_print_unavailable()
					: errorMessage(failure);
		} finally {
			downloading = false;
		}
	}
</script>

{#snippet compile()}
	<Button variant="solid" icon={IconPencil} href={editHref}>
		{m.studyplan_compile()}
	</Button>
{/snippet}

<svelte:head>
	<title>{m.shell_service_study_plan()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<PageHeader
		title={m.shell_service_study_plan()}
		{subtitle}
		back={{ href: shellHref('services'), label: m.shell_services() }}
		loading={subtitleLoading && { eyebrow: false }}
	>
		{#snippet actions()}
			{#if plan !== null && !(plan === undefined && data.error)}
				<Menu label={m.studyplan_print_menu()} items={printouts} placement="bottom-end">
					{#snippet trigger(props)}
						<Button
							{...props}
							icon={IconPrinter}
							trailingIcon={IconCaretDown}
							disabled={!plan}
							loading={downloading}
						>
							{m.studyplan_print()}
						</Button>
					{/snippet}
				</Menu>
				{#if canEdit}
					<Button variant="solid" icon={IconPencil} href={editHref} {@attach tooltip(windowText)}>
						{m.studyplan_edit_path()}
					</Button>
				{/if}
			{/if}
		{/snippet}
	</PageHeader>

	{#if pdfError}
		<Banner
			tone="danger"
			title={m.studyplan_pdf_failed()}
			message={pdfError}
			role="alert"
			ondismiss={() => (pdfError = undefined)}
		>
			{#snippet actions()}
				<Button size="sm" loading={downloading} onclick={openOfficial}>{m.common_retry()}</Button>
			{/snippet}
		</Banner>
	{/if}

	{#if stale && data.error}
		<ErrorState
			variant="stale"
			message={errorMessage(data.error)}
			{updatedAt}
			retrying={data.refreshing}
			onretry={() => void data.retry()}
		/>
	{/if}

	{#if loading}
		<PlanSkeleton />
	{:else if plan}
		<PlanSummary {plan} {years} {earned} />

		<section class="years" aria-label={m.studyplan_years_label()}>
			{#each years as group (group.year)}
				<YearGroup {group} {recordOf} known={records.data !== undefined} />
			{/each}
		</section>

		<PrintSheet {plan} {years} {student} path={planSubtitle(plan, data.path ?? null) ?? ''} />
	{:else if plan === null}
		<EmptyState
			size="page"
			icon={IconBookOpen}
			title={m.studyplan_empty_title()}
			body={editable ? m.studyplan_empty_compile_body() : m.studyplan_empty_body()}
			action={editable ? compile : undefined}
		/>
	{:else if data.error}
		<ErrorState
			message={errorMessage(data.error)}
			title={m.studyplan_load_failed()}
			retrying={data.refreshing}
			onretry={() => void data.retry()}
		/>
	{/if}
</Page>

<style>
	.years {
		display: grid;
		gap: var(--sp-6);
	}

	@container page (width >= 1000px) {
		.years {
			grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
			align-items: start;
		}
	}
</style>
