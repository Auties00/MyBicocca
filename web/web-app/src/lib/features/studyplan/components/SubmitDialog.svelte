<script lang="ts">
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import type { PlanEditor } from '../editor.svelte.ts';
	import { approvalText, cfu, optionName } from '../labels.ts';
	import { selectedCourses } from '../rules.ts';

	interface Props {
		open: boolean;
		editor: PlanEditor;
		/** The path's name when no path is chosen in the editor: the current one. */
		pathName: string;
		/** The draft is the current plan as it stands: sending it only recreates it. */
		unchanged: boolean;
		/** Sends the plan; resolves to what went wrong, or nothing once sent. */
		onconfirm: () => Promise<string | undefined>;
	}

	let { open = $bindable(), editor, pathName, unchanged, onconfirm }: Props = $props();

	let busy = $state(false);
	let error = $state<string>();

	const facts = $derived.by((): Fact[] => {
		const picked = selectedCourses(editor.rules ?? []);
		const credits = picked.reduce((sum, { course }) => sum + course.credits, 0);
		const chosen = editor.chosen;
		const approval = chosen ? approvalText(chosen.approval) : undefined;
		return [
			{ label: m.studyplanedit_confirm_path(), value: chosen ? optionName(chosen) : pathName },
			{
				label: m.studyplanedit_confirm_activities(),
				value: `${m.studyplan_activities_count({ count: picked.length })} · ${cfu(credits)}`
			},
			...(approval ? [{ label: m.studyplanedit_confirm_approval(), value: approval }] : [])
		];
	});

	async function confirm() {
		if (busy) return;
		busy = true;
		error = undefined;
		const failure = await onconfirm();
		busy = false;
		if (failure === undefined) open = false;
		else error = failure;
	}
</script>

<Dialog
	bind:open
	title={m.studyplanedit_confirm_title()}
	size="sm"
	role="alertdialog"
	dismissible={!busy}
	onclose={() => (error = undefined)}
>
	<div class="body">
		<p class="message">{m.studyplanedit_confirm_body()}</p>
		<Facts items={facts} />
		{#if unchanged}
			<Banner tone="info" message={m.studyplanedit_confirm_unchanged()} />
		{/if}
		{#if error}
			<Banner tone="danger" title={m.studyplan_submit_failed()} message={error} role="alert" />
		{/if}
	</div>
	{#snippet footer()}
		<Button
			variant="ghost"
			disabled={busy}
			onclick={() => {
				open = false;
				error = undefined;
			}}
			data-autofocus
		>
			{m.ui_cancel()}
		</Button>
		<Button variant="solid" icon={IconPaperPlane} loading={busy} onclick={confirm}>
			{m.studyplanedit_submit_plan()}
		</Button>
	{/snippet}
</Dialog>

<style>
	.body {
		display: grid;
		gap: var(--sp-4);
	}

	.message {
		color: var(--text-muted);
	}
</style>
