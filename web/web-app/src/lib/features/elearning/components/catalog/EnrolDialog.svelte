<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconBookOpen from '~icons/ph/book-open-text';
	import IconKey from '~icons/ph/key';
	import type { SessionScope } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import type { CatalogCourse } from '../../catalog/model.ts';
	import { bareCode } from '../../courseCode.ts';
	import { enrolFailure, enrolMutation, needsKey } from '../../enrolment.ts';
	import { moodleCourseUrl } from '../../links.ts';

	interface Props {
		scope: SessionScope;
		offline: boolean;
		onenrolled: (course: CatalogCourse) => void;
	}

	let { scope, offline, onenrolled }: Props = $props();

	const enrol = $derived(enrolMutation(scope));
	let open = $state(false);
	let course = $state.raw<CatalogCourse>();
	let key = $state('');
	let askKey = $state(false);
	let keyInput = $state<HTMLInputElement>();

	const wantsKey = $derived(needsKey(enrol.error));
	const failure = $derived(enrol.error && enrolFailure(enrol.error));

	/** Asks to enrol in `next`, afresh: no key or error carried over from the last course. */
	export function show(next: CatalogCourse) {
		course = next;
		key = '';
		askKey = false;
		enrol.reset();
		open = true;
	}

	async function submit(event: SubmitEvent) {
		event.preventDefault();
		if (!course || enrol.pending || offline) return;
		const result = await enrol.run(course.id, key.trim());
		if (result.ok) {
			open = false;
			onenrolled(course);
		} else if (needsKey(result.error)) {
			askKey = true;
			queueMicrotask(() => keyInput?.select());
		}
	}
</script>

<Dialog
	bind:open
	title={m.elearning_enrol_title()}
	size="sm"
	dismissible={!enrol.pending}
	onclose={() => enrol.reset()}
	returnFocus={() =>
		course && document.querySelector<HTMLElement>(`[data-enrolled="${course.id}"]`)}
>
	{#if course}
		<form id="enrol-form" class="form" onsubmit={submit}>
			<div class="course">
				<span class="chip" aria-hidden="true"><IconBookOpen /></span>
				<div class="names">
					<p class="name">{course.name}</p>
					<p class="code" translate="no">{bareCode(course.code)}</p>
				</div>
			</div>
			<p class="body">{m.elearning_enrol_body()}</p>

			{#if askKey || wantsKey}
				<TextField
					label={m.elearning_enrol_key()}
					description={m.elearning_enrol_key_hint()}
					icon={IconKey}
					bind:value={key}
					bind:input={keyInput}
					autocomplete="off"
					spellcheck={false}
					error={wantsKey && failure ? failure : undefined}
				/>
			{:else}
				<button type="button" class="have-key" onclick={() => (askKey = true)}>
					<IconKey aria-hidden="true" />
					{m.elearning_enrol_have_key()}
				</button>
			{/if}

			{#if failure && !wantsKey}
				<Banner tone="danger" role="alert" message={failure} title={m.elearning_enrol_failed()} />
			{/if}
			{#if offline}
				<Banner tone="warning" message={m.elearning_enrol_offline()} />
			{/if}
		</form>
	{/if}
	{#snippet footer()}
		{#if course}
			<span class="view">
				<Button variant="ghost" trailingIcon={IconArrowSquareOut} href={moodleCourseUrl(course.id)}>
					{m.elearning_enrol_view_on_moodle()}
				</Button>
			</span>
		{/if}
		<Button variant="ghost" disabled={enrol.pending} onclick={() => (open = false)}>
			{m.ui_cancel()}
		</Button>
		<Button
			variant="solid"
			type="submit"
			form="enrol-form"
			loading={enrol.pending}
			disabled={offline}
		>
			{m.elearning_enrol()}
		</Button>
	{/snippet}
</Dialog>

<style>
	.form {
		display: grid;
		gap: 16px;
	}

	.course {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: 12px;
		padding: 12px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
	}

	.chip {
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		border-radius: 12px;
		background: light-dark(
			oklch(92% calc(var(--area-c, 0.1) * 0.45) var(--area-h, 15)),
			oklch(32% calc(var(--area-c, 0.1) * 0.5) var(--area-h, 15))
		);
		color: light-dark(
			oklch(42% var(--area-c, 0.1) var(--area-h, 15)),
			oklch(82% calc(var(--area-c, 0.1) * 0.7) var(--area-h, 15))
		);
	}

	.chip :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.name {
		font: var(--type-body-strong);
		text-wrap: balance;
	}

	.code {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.04em;
	}

	.body {
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.have-key {
		display: inline-flex;
		align-items: center;
		justify-self: start;
		gap: 6px;
		margin: -4px -6px;
		padding: 4px 6px;
		border-radius: var(--r-xs);
		color: var(--accent-text);
		font: var(--type-ui-strong);
		cursor: pointer;
	}

	.have-key :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		.have-key:hover {
			text-decoration: underline;
			text-underline-offset: 3px;
		}
	}

	.view {
		margin-inline-end: auto;
	}

	@media (width < 600px) {
		.view {
			flex-basis: 100%;
			order: 1;
		}
	}
</style>
