<script lang="ts">
	import { tick, untrack } from 'svelte';
	import { goto, onNavigate } from '$app/navigation';
	import IconClipboard from '~icons/ph/clipboard-text-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import {
		Compilation,
		compilationKey,
		dropCompilation,
		keepCompilation,
		requestStart,
		resumeCompilation,
		takeStartRequest
	} from '../compilation.svelte.ts';
	import { unitLine } from '../format.ts';
	import { LeaveGuard } from '../leaveGuard.svelte.ts';
	import { activityHref, compileHref, questionnairesHref } from '../links.ts';
	import { unitTitle } from '../model.ts';
	import { isAnswered } from '../page.ts';
	import { ActivityUnitsQuery, useQuestionnairesView } from '../questionnaires.svelte.ts';
	import { isTypingTarget } from '#lib/ui/platform.ts';
	import CompileBar from './CompileBar.svelte';
	import CompileRail from './CompileRail.svelte';
	import IntroStep from './IntroStep.svelte';
	import PageSkeleton from './PageSkeleton.svelte';
	import QuestionField from './QuestionField.svelte';
	import SentStep from './SentStep.svelte';
	import SummaryStep from './SummaryStep.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		/** The activity's `adsceId`, from the URL. */
		activity: number | undefined;
		/** The unit's 1-based position among the activity's units, from the URL. */
		unit: number | undefined;
	}

	let { activity, unit }: Props = $props();

	const view = useQuestionnairesView();
	const unitsQuery = new ActivityUnitsQuery(() => activity);
	const query = unitsQuery.query;
	const data = $derived(unitsQuery.units);
	const info = $derived(view.questionnaires.find(activity));
	const target = $derived(data?.units.find((item) => item.index === unit));

	/* The route remounts this page for another unit, so its props never change. */
	const key = untrack(() => {
		const scope = session.current;
		return scope && activity !== undefined && unit !== undefined
			? compilationKey(scope, activity, unit)
			: undefined;
	});
	const asked = untrack(() => takeStartRequest(`${activity}/${unit}`));
	const resumed = key === undefined ? undefined : resumeCompilation(key);
	let compilation = $state(resumed);
	let autostart = $state(asked && resumed === undefined);

	$effect(() => {
		if (compilation) return;
		const scope = session.current;
		const units = data;
		const picked = target;
		const questionnaire = units?.questionnaire;
		if (!scope || activity === undefined || !units || !questionnaire || !picked || picked.done) {
			return;
		}
		untrack(() => {
			const created = new Compilation(scope, {
				activityChoiceId: activity,
				questionnaireId: questionnaire.id,
				configId: questionnaire.configId,
				unit: picked,
				anonymous: units.anonymous
			});
			compilation = created;
			if (key !== undefined) keepCompilation(key, created);
			if (autostart && network.online) void begin();
			autostart = false;
		});
	});

	/* A compilation outlives a remount of this page, but not the student leaving it. */
	onNavigate((navigation) => {
		if (key !== undefined && navigation.to?.url.pathname !== navigation.from?.url.pathname) {
			dropCompilation(key);
		}
	});

	const step = $derived(compilation?.step);
	const shown = $derived(compilation?.target.unit ?? target);
	const loading = $derived(data === undefined && query.status === 'pending');
	const title = $derived(
		shown ? unitTitle(shown) : loading ? '' : m.questionnaires_compile_title()
	);
	const line = $derived(shown && unitLine(shown, info?.name));
	const subtitle = $derived(
		line ?? data?.title ?? (loading ? undefined : m.questionnaires_units_subtitle())
	);
	/* The course's name comes with the list, the unit's with its own query: bars until then. */
	const headerLoading = $derived(
		loading ||
			(view.questionnaires.query.data === undefined &&
				view.questionnaires.query.status === 'pending')
	);
	const backHref = $derived(activity === undefined ? questionnairesHref() : activityHref(activity));
	const gate = new LoadingGate(() => loading);
	const failed = $derived(
		data === undefined && query.status === 'error' && query.error !== undefined
	);
	const answering = $derived(step?.kind === 'page' || step?.kind === 'summary');
	/* The first page on its way, in the frame it will arrive in, so nothing moves when it does. */
	const preparing = $derived(
		step?.kind === 'starting' || (autostart && (step?.kind === 'ready' || loading))
	);

	/** +1 moving on, -1 going back: the side the next page slides in from. */
	let direction = $state(1);
	let content = $state<HTMLElement>();

	const working = $derived(compilation?.working ?? null);
	const answered = $derived(
		step?.kind === 'page' && compilation
			? step.page.questions.filter((question) =>
					isAnswered(question, compilation?.answerOf(question))
				).length
			: 0
	);
	const bar = $derived.by(() => {
		if (step?.kind === 'summary') {
			return {
				where: m.questionnaires_summary_title(),
				status: step.complete
					? m.questionnaires_summary_ready()
					: m.questionnaires_summary_missing(),
				alert: !step.complete
			};
		}
		if (step?.kind !== 'page' || !compilation) return undefined;
		const missing = compilation.invalid.length;
		return {
			where: m.questionnaires_page_number({ number: step.index + 1 }),
			status:
				missing > 0
					? m.questionnaires_missing_count({ count: missing })
					: m.questionnaires_answered_count({ answered, total: step.page.questions.length }),
			alert: missing > 0
		};
	});
	const failure = $derived.by(() => {
		const failed = compilation?.moveError;
		if (!failed) return undefined;
		return {
			title:
				failed.move === 'next' ? m.questionnaires_move_failed() : m.questionnaires_back_failed(),
			message: errorMessage(failed.error),
			retry: failed.move === 'next'
		};
	});

	/** After a move, the new step's heading takes the focus, from the top of the page. */
	async function settle() {
		await tick();
		window.scrollTo({ top: 0 });
		content?.querySelector<HTMLElement>('[data-step-heading]')?.focus({ preventScroll: true });
	}

	async function begin() {
		const current = compilation;
		if (!current) return;
		direction = 1;
		await current.start();
		if (current.step.kind === 'page') await settle();
	}

	/* The first question a blocked "Next" marked; its words, if those are what it lacks. */
	function focusMissing() {
		const first = compilation?.invalid[0];
		const card = content?.querySelector<HTMLElement>(`[data-question="${first}"]`);
		if (!card) return;
		const smooth = !reducedMotion.current;
		card.scrollIntoView({ block: 'center', behavior: smooth ? 'smooth' : 'auto' });
		const words = card.querySelector<HTMLInputElement>('[data-specify] input');
		const field =
			(words && words.value.trim() === '' ? words : null) ??
			card.querySelector<HTMLElement>('input:checked') ??
			card.querySelector<HTMLElement>('input:not(:disabled), textarea');
		field?.focus({ preventScroll: true });
	}

	async function advance() {
		const current = compilation;
		if (!current) return;
		direction = 1;
		const result = await current.next();
		if (result === 'missing') {
			await tick();
			focusMissing();
		} else if (result === 'moved') {
			await settle();
		}
	}

	async function retreat(index?: number) {
		const current = compilation;
		if (!current || current.working) return;
		direction = -1;
		const before = current.step;
		await current.back(index);
		if (current.step !== before) await settle();
	}

	let sendOpen = $state(false);

	/* Inside the questionnaire Back steps back a page; every way out asks first. */
	const guard = new LeaveGuard(
		() => ({
			open: compilation?.open ?? false,
			busy: compilation?.working === 'send' || sendOpen,
			canGoBack: compilation?.canGoBack ?? false
		}),
		() => void retreat()
	);

	/*
	 * Switching account or career keeps the page, but its compilation belongs to the one before:
	 * the list of the one now in use takes over, rather than a header naming the wrong career.
	 */
	const scopeKey = $derived(
		session.current && activity !== undefined && unit !== undefined
			? compilationKey(session.current, activity, unit)
			: undefined
	);
	$effect(() => {
		if (key === undefined || scopeKey === undefined || scopeKey === key) return;
		untrack(() => {
			const lost = compilation?.open === true;
			guard.release();
			void goto(questionnairesHref(), { replace: true });
			if (lost) toast.show({ message: m.questionnaires_closed_on_switch(), tone: 'warning' });
		});
	});

	function primary() {
		if (step?.kind === 'page') void advance();
		else if (step?.kind === 'summary' && step.complete && network.online) sendOpen = true;
	}

	async function send() {
		await compilation?.send();
	}

	registerShortcuts(() => [
		{ keys: [['mod', 'enter']], label: m.questionnaires_keys_next() },
		{ keys: [['esc']], label: m.questionnaires_keys_leave() }
	]);

	/* Ctrl or ⌘ + Enter moves on; Escape leaves (asking first), unless it is typing's own. */
	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || guard.asking || sendOpen) return;
		const element = event.target instanceof Element ? event.target : null;
		if (element?.closest('dialog, [popover]')) return;
		if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			primary();
		} else if (event.key === 'Escape' && !isTypingTarget(event.target)) {
			event.preventDefault();
			void goto(backHref);
		}
	}

	const nextUp = $derived.by(() => {
		if (step?.kind !== 'sent' || activity === undefined) return undefined;
		const sibling = data?.questionnaire
			? data.units.find((item) => !item.done && item.index !== unit)
			: undefined;
		if (sibling) {
			const siblingKey = `${activity}/${sibling.index}`;
			return {
				label: m.questionnaires_sent_next_unit({ name: unitTitle(sibling) }),
				href: compileHref(activity, sibling.index),
				onclick: (event: MouseEvent) => {
					if (event.button === 0 && !event.ctrlKey && !event.metaKey && !event.shiftKey) {
						requestStart(siblingKey);
					}
				}
			};
		}
		const other = view.questionnaires.pending.find((item) => item.id !== activity);
		return other
			? {
					label: m.questionnaires_sent_next_activity({ name: other.name }),
					href: activityHref(other.id)
				}
			: undefined;
	});
</script>

<svelte:head>
	<title
		>{title || m.questionnaires_compile_title()} · {m.questionnaires_title()} · {m.app_name()}</title
	>
</svelte:head>

<svelte:window onkeydown={keydown} />

{#snippet steps()}
	<div class="content" bind:this={content}>
		{#if step?.kind === 'sent'}
			<SentStep name={title} next={nextUp} listHref={questionnairesHref()} />
		{:else if preparing}
			<PageSkeleton label={m.questionnaires_starting()} />
		{:else if step?.kind === 'start-failed'}
			<ErrorState
				title={m.questionnaires_start_failed_title()}
				message={errorMessage(step.error)}
				onretry={() => void begin()}
			/>
		{:else if (step?.kind === 'page' || step?.kind === 'summary') && compilation && bar}
			<div class="wizard">
				<div class="pages">
					{#if step.kind === 'page'}
						{#key step.page.id}
							<div
								class="page"
								style:--from={direction}
								inert={working !== null}
								aria-busy={working !== null}
							>
								<h2 class="sr-only" tabindex="-1" data-step-heading>
									{m.questionnaires_page_number({ number: step.index + 1 })}
								</h2>
								{#each step.page.sections as section, sectionIndex (section.id)}
									{@const offset = step.page.sections
										.slice(0, sectionIndex)
										.reduce((sum, item) => sum + item.questions.length, 0)}
									<section class="section" aria-label={section.title ?? undefined}>
										{#if section.title}<h3 class="section-title">{section.title}</h3>{/if}
										{#each section.questions as question, index (question.id)}
											<QuestionField
												{question}
												number={offset + index + 1}
												answer={compilation.answerOf(question)}
												invalid={compilation.invalid.includes(question.id)}
												onpick={(option) => compilation?.pick(question, option)}
												ontext={(text) => compilation?.write(question, text)}
											/>
										{/each}
									</section>
								{/each}
							</div>
						{/key}
					{:else}
						<div class="page" style:--from={direction}>
							<SummaryStep
								complete={step.complete}
								anonymous={compilation.target.anonymous}
								visited={compilation.visited}
							/>
						</div>
					{/if}
				</div>

				<CompileBar
					where={bar.where}
					status={bar.status}
					alert={bar.alert}
					online={network.online}
					sending={step.kind === 'summary'}
					blocked={step.kind === 'summary' && !step.complete}
					canBack={compilation.canGoBack}
					{working}
					{failure}
					onback={() => void retreat()}
					onprimary={primary}
					ondismiss={() => {
						if (compilation) compilation.moveError = undefined;
					}}
				/>
			</div>
		{:else if loading}
			{#if gate.visible}
				<IntroStep anonymous={undefined} online={network.online} {backHref} onstart={() => {}} />
			{/if}
		{:else if failed && query.error}
			<ErrorState
				title={m.questionnaires_units_load_failed()}
				message={errorMessage(query.error)}
				onretry={query.refresh}
				retrying={query.refreshing}
			/>
		{:else if !compilation || !data}
			<EmptyState
				size="page"
				icon={IconClipboard}
				title={m.questionnaires_unit_gone_title()}
				body={m.questionnaires_unit_gone_body()}
			>
				{#snippet action()}
					<Button href={backHref}>{m.questionnaires_back_to_units()}</Button>
				{/snippet}
			</EmptyState>
		{:else}
			<IntroStep
				anonymous={data.anonymous}
				online={network.online}
				{backHref}
				onstart={() => void begin()}
			/>
		{/if}
	</div>
{/snippet}

<!-- While the questions are answered, a phone's tab bar gives its place to the questionnaire's bar. -->
<Page width="dashboard" tabBar={!(answering || preparing)}>
	<div class={['layout', { railed: answering || preparing }]}>
		<div class="column">
			<PageHeader
				{title}
				eyebrow={info?.name}
				{subtitle}
				back={{ href: backHref, label: info?.name ?? m.questionnaires_back_to_units() }}
				loading={headerLoading && { title: 1 }}
			/>
			{@render steps()}
		</div>

		{#if answering && compilation}
			<div class="rail">
				<CompileRail
					{compilation}
					online={network.online}
					onrevisit={(index) => void retreat(index)}
				/>
			</div>
		{/if}
	</div>
</Page>

<ConfirmDialog
	bind:open={sendOpen}
	title={m.questionnaires_send_title()}
	message={m.questionnaires_send_body({ name: title })}
	confirmLabel={m.questionnaires_send()}
	onconfirm={send}
	errorMessage={(error) => errorMessage(toDataError(error))}
/>

<ConfirmDialog
	bind:open={guard.asking}
	title={m.questionnaires_exit_title()}
	message={m.questionnaires_exit_body()}
	confirmLabel={m.questionnaires_exit_confirm()}
	cancelLabel={m.questionnaires_exit_cancel()}
	tone="danger"
	onconfirm={() => guard.leave(backHref)}
	oncancel={() => guard.stay()}
/>

<style>
	/* A reading column; from about 1100 px of room, the progress rail beside it. */
	.layout {
		display: grid;
		grid-template-columns: minmax(0, var(--w-reading));
		justify-content: center;
		align-items: start;
		gap: var(--sp-8);
	}

	.column {
		display: flex;
		flex-direction: column;
		gap: var(--page-gap);
		min-inline-size: 0;
	}

	.rail {
		display: none;
		block-size: 100%;
	}

	@container page (width >= 1100px) {
		.railed {
			grid-template-columns: minmax(0, var(--w-reading)) 280px;
		}

		.railed .rail {
			display: block;
		}
	}

	.content {
		display: grid;
		gap: var(--sp-4);
		container: compile / inline-size;
	}

	.wizard {
		display: grid;
		gap: var(--sp-4);
	}

	/*
	 * Clipped at the gutter: a page sliding in would otherwise overhang the screen's edge, and a
	 * phone would scroll sideways while it does.
	 */
	.pages {
		overflow: clip;
		overflow-clip-margin: var(--gutter);
	}

	.page {
		display: grid;
		gap: var(--sp-4);
		animation: arrive var(--dur-slow) var(--ease-out) both;
		transition: opacity var(--dur-fast) var(--ease-out);
	}

	.page[aria-busy='true'] {
		opacity: 0.6;
	}

	.page :global([data-step-heading]) {
		outline: none;
	}

	.section {
		display: grid;
		gap: var(--sp-3);
	}

	.section-title {
		margin-block-end: var(--sp-1);
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	@keyframes arrive {
		from {
			opacity: 0;
			transform: translateX(calc(var(--from) * 24px));
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.page {
			animation: none;
		}
	}
</style>
