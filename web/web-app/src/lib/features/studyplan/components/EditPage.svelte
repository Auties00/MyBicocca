<script lang="ts">
	import { untrack } from 'svelte';
	import { beforeNavigate, goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import type { ResolvedPathname } from '$app/types';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconLock from '~icons/ph/lock-simple-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { mutate } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import { StudyPlanData } from '../data.svelte.ts';
	import { discardEditor, planEditor } from '../editor.svelte.ts';
	import { failureText, pathName, planSubtitle } from '../labels.ts';
	import type { Approval } from '../path.ts';
	import { planInvalidations } from '../queries.ts';
	import { isSatisfied, submitBody, submitFailure } from '../rules.ts';
	import { WizardSteps } from '../steps.svelte.ts';
	import EditSkeleton from './EditSkeleton.svelte';
	import PathStep from './PathStep.svelte';
	import RuleStep from './RuleStep.svelte';
	import StepList from './StepList.svelte';
	import SubmitDialog from './SubmitDialog.svelte';
	import SubmitOutcome from './SubmitOutcome.svelte';
	import WizardBar from './WizardBar.svelte';

	const data = new StudyPlanData();
	const planHref = resolve('services/study-plan');
	const editHref = resolve('services/study-plan/edit');

	/*
	 * The outcome of a sent plan, kept in its history entry too: coming back to that entry shows
	 * the outcome again rather than a new draft.
	 */
	type OutcomeState = { studyplanSubmitted?: Approval };
	let sent = $state<Approval>();
	const submitted = $derived(sent ?? (page.state as OutcomeState).studyplanSubmitted);
	const editor = $derived.by(() => {
		const scope = session.current;
		if (submitted || !scope || !data.request || !data.path) return undefined;
		return planEditor(scope, data.request, data.path);
	});

	/* Without a path to pick, the rules of the current schema load at once. */
	$effect(() => {
		const current = editor;
		if (
			current &&
			!current.choosesPath &&
			current.selectedSchemaId !== null &&
			!current.rules &&
			!current.loading &&
			!current.error
		) {
			untrack(() => void current.load());
		}
	});

	const pending = $derived(
		data.status === 'pending' || (data.path === undefined && data.pathError === undefined)
	);
	const gate = new LoadingGate(() => pending);
	const loading = $derived(pending || gate.visible);

	const rules = $derived(editor?.rules ?? []);
	const offset = $derived(editor?.choosesPath ? 1 : 0);
	const total = $derived(Math.max(1, offset + rules.length));

	let wizard = $state<HTMLElement>();

	const steps = new WizardSteps({
		total: () => total,
		settled: () => !!editor && !editor.loading && (!!editor.rules || editor.choosesPath),
		href: (step) => (step === 0 ? editHref : resolve(`services/study-plan/edit?step=${step}`)),
		/* A new step is announced by moving focus to its heading. */
		onarrive: () =>
			wizard?.querySelector<HTMLElement>('[data-step-heading]')?.focus({ preventScroll: true })
	});

	const index = $derived(steps.index);
	const rule = $derived(index >= offset ? rules[index - offset] : undefined);
	const last = $derived(rule !== undefined && index === total - 1);
	/** The chosen path has no rules: nothing comes after the path step. */
	const deadEnd = $derived(editor?.rules?.length === 0);

	const canAdvance = $derived.by(() => {
		if (!editor) return false;
		if (rule) return isSatisfied(rule);
		return editor.selectedSchemaId !== null && !deadEnd && !editor.loading;
	});
	const canSubmit = $derived(!!editor?.complete && network.online);
	/** The draft is the plan the student already has: sending it would only recreate it. */
	const unchanged = $derived(!!editor && editor.request.planId > 0 && !editor.dirty);
	/** Submit is the view's primary action once the plan can be sent and changes something. */
	const sendable = $derived(canSubmit && !unchanged);
	const hint = $derived(
		!network.online
			? m.studyplanedit_offline()
			: editor && !editor.complete
				? m.studyplanedit_incomplete()
				: undefined
	);

	const subtitle = $derived(
		loading
			? undefined
			: data.path?.windowEnd
				? m.studyplan_window_until({ date: formatDate(data.path.windowEnd, 'long') })
				: data.plan !== undefined && data.path
					? planSubtitle(data.plan, data.path)
					: undefined
	);

	/*
	 * Two panes once the editor has room for the step list beside a readable step. It measures
	 * its own width: on desktops it shares the screen with the services list.
	 */
	const TWO_PANE = 760;
	let wide = $state(false);

	function measure(node: HTMLElement) {
		const observer = new ResizeObserver(([entry]) => {
			wide = entry.contentBoxSize[0].inlineSize >= TWO_PANE;
		});
		observer.observe(node);
		return () => observer.disconnect();
	}

	/* The bar's main button; in two panes the step list holds Submit. */
	const barAction = $derived<'next' | 'submit' | undefined>(
		deadEnd ? undefined : !last ? 'next' : wide ? undefined : 'submit'
	);

	/** From the path, the rules load first; the step changes once they are there. */
	async function next() {
		const current = editor;
		if (!current) return;
		if (!rule && !(await current.load())) return;
		if ((current.rules?.length ?? 0) > 0) await steps.go(index + 1, true);
	}

	let submitOpen = $state(false);
	let sending = false;

	async function send(): Promise<string | undefined> {
		const scope = session.current;
		const current = editor;
		const picked = current?.rules;
		if (!scope || !current || !picked) return m.studyplanedit_submit_error_generic();
		sending = true;
		try {
			await mutate(
				() =>
					scope.api.esse3.plans.postStudentPlan(
						current.request.studentId,
						submitBody(picked, current.chosen)
					),
				{ invalidate: planInvalidations(scope) }
			);
			const approval = current.chosen?.approval ?? 'unknown';
			discardEditor(current);
			sent = approval;
			const state: OutcomeState = { studyplanSubmitted: approval };
			void steps.finish(editHref, state as App.PageState);
			return undefined;
		} catch (error) {
			return failureText(submitFailure(toDataError(error)));
		} finally {
			sending = false;
		}
	}

	/* Leaving with unsent picks asks first; the steps themselves are free to move between. */
	let exitOpen = $state(false);
	type Destination =
		| { kind: 'back'; delta: number }
		| { kind: 'app'; href: ResolvedPathname }
		| { kind: 'away'; href: string };
	let destination: Destination | undefined;
	let leaving = false;

	beforeNavigate((navigation) => {
		const current = editor;
		if (leaving || submitted || !current) return;
		const to = navigation.to?.url;
		if (navigation.type !== 'leave' && to?.pathname === page.url.pathname) return;
		if (sending) {
			navigation.cancel();
			return;
		}
		if (!current.dirty) {
			discardEditor(current);
			return;
		}
		navigation.cancel();
		if (navigation.type === 'leave' || !to) return;
		destination =
			navigation.type === 'popstate'
				? { kind: 'back', delta: navigation.delta }
				: to.origin === location.origin
					? { kind: 'app', href: `${to.pathname}${to.search}${to.hash}` as ResolvedPathname }
					: { kind: 'away', href: to.href };
		exitOpen = true;
	});

	function exit() {
		if (editor) discardEditor(editor);
		leaving = true;
		const target = destination;
		destination = undefined;
		if (!target) void goto(planHref);
		else if (target.kind === 'back') history.go(target.delta);
		else if (target.kind === 'away') location.assign(target.href);
		else void goto(target.href);
	}

	registerShortcuts(() => [{ keys: [['esc']], label: m.studyplanedit_keys_exit() }]);

	/*
	 * Escape leaves the editor, through the same question when something would be lost. Its
	 * default (a close request) would otherwise dismiss that question as soon as it opens.
	 */
	function keydown(event: KeyboardEvent) {
		if (event.key !== 'Escape' || event.defaultPrevented || submitOpen || exitOpen) return;
		if (event.target instanceof Element && event.target.closest('dialog, [popover]')) return;
		event.preventDefault();
		void goto(planHref);
	}
</script>

<svelte:head>
	<title>{m.studyplan_edit_path()} · {m.app_name()}</title>
</svelte:head>

<svelte:window onkeydown={keydown} />

{#snippet backToPlan()}
	<Button icon={IconArrowLeft} href={planHref}>{m.studyplanedit_back_to_plan()}</Button>
{/snippet}

<!-- A focused flow: on phones the wizard's bar takes the tab bar's place until the plan is sent. -->
<Page width="dashboard" tabBar={submitted !== undefined}>
	<PageHeader
		title={m.studyplan_edit_path()}
		{subtitle}
		back={{ href: planHref, label: m.shell_service_study_plan() }}
		loading={loading && { eyebrow: false }}
	/>

	<div {@attach measure}>
		{#if submitted}
			<SubmitOutcome approval={submitted} {planHref} />
		{:else if loading}
			<EditSkeleton label={m.common_loading()} {wide} />
		{:else if data.plan === undefined && data.error}
			<ErrorState
				title={m.studyplan_load_failed()}
				message={errorMessage(data.error)}
				retrying={data.refreshing}
				onretry={() => void data.retry()}
			/>
		{:else if data.pathError}
			<ErrorState
				title={m.studyplanedit_unavailable_title()}
				message={errorMessage(data.pathError)}
				onretry={() => void data.retry()}
			/>
		{:else if data.path && !data.path.editingOpen}
			<EmptyState
				size="page"
				icon={IconCalendarX}
				title={m.studyplanedit_closed_title()}
				body={m.studyplanedit_closed_body()}
				action={backToPlan}
			/>
		{:else if !editor || (!editor.choosesPath && editor.selectedSchemaId === null)}
			<EmptyState
				size="page"
				icon={IconLock}
				title={m.studyplanedit_unavailable_title()}
				body={m.studyplanedit_unavailable_body()}
				action={backToPlan}
			/>
		{:else if !editor.choosesPath && !editor.rules}
			{#if editor.error}
				<ErrorState
					title={m.studyplanedit_rules_failed()}
					message={errorMessage(editor.error)}
					retrying={editor.loading}
					onretry={() => void editor.load()}
				/>
			{:else}
				<EditSkeleton label={m.studyplanedit_loading_rules()} {wide} />
			{/if}
		{:else if !editor.choosesPath && rules.length === 0}
			<EmptyState
				size="page"
				icon={IconLock}
				title={m.studyplanedit_no_rules_title()}
				body={m.studyplanedit_no_rules_body()}
				action={backToPlan}
			/>
		{:else}
			<div class={['wizard', { wide }]}>
				{#if wide}
					<aside class="side">
						<StepList
							{editor}
							{index}
							{canSubmit}
							primary={sendable}
							{hint}
							onselect={(step) => void steps.go(step, false)}
							onsubmit={() => (submitOpen = true)}
						/>
					</aside>
				{/if}
				<div class="main" bind:this={wizard}>
					{#if rule}
						{#key rule.choiceId}
							<RuleStep {editor} {rule} number={index - offset} />
						{/key}
					{:else}
						<PathStep {editor} onretry={() => void next()} />
					{/if}
					<WizardBar
						{index}
						total={editor.choosesPath && !editor.rules?.length ? undefined : total}
						action={barAction}
						primary={barAction === 'next' ? !(wide && sendable) : sendable}
						enabled={barAction === 'next' ? canAdvance : canSubmit}
						advancing={editor.loading}
						{hint}
						onback={() => steps.back()}
						onaction={() => (barAction === 'next' ? void next() : (submitOpen = true))}
					/>
				</div>
			</div>
			<SubmitDialog
				bind:open={submitOpen}
				{editor}
				pathName={pathName(data.path ?? null)}
				{unchanged}
				onconfirm={send}
			/>
		{/if}
	</div>
</Page>

<ConfirmDialog
	bind:open={exitOpen}
	title={m.studyplan_exit_confirm_title()}
	message={m.studyplan_exit_warning()}
	confirmLabel={m.studyplanedit_exit()}
	cancelLabel={m.studyplanedit_keep_editing()}
	tone="danger"
	onconfirm={exit}
	oncancel={() => (destination = undefined)}
/>

<style>
	.wizard {
		display: grid;
		gap: var(--sp-6);
		align-items: start;
	}

	.wizard.wide {
		grid-template-columns: clamp(232px, 26%, 280px) minmax(0, 760px);
		gap: var(--sp-8);
	}

	.side {
		position: sticky;
		inset-block-start: calc(var(--shell-inset-top) + var(--sp-4));
	}

	.main {
		display: grid;
		gap: var(--sp-6);
		min-inline-size: 0;
	}
</style>
