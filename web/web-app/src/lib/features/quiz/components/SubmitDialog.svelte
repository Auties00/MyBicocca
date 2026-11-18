<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconFlagFill from '~icons/ph/flag-fill';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import IconWarningCircle from '~icons/ph/warning-circle';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { NavItem, SaveState } from '../attempt.svelte.ts';
	import { quizErrorMessage } from '../format.ts';
	import type { SubmitCheck } from '../submit.svelte.ts';

	interface Props {
		check: SubmitCheck;
		/** Takes the student to a question left unanswered or flagged. */
		onvisit: (item: NavItem) => void;
		online: boolean;
		/** Whether the answers are saved, to say so when the check fails on saving them. */
		saveState: SaveState;
	}

	let { check, onvisit, online, saveState }: Props = $props();

	const checkFailure = $derived.by(() => {
		if (check.failure === undefined) return undefined;
		if (saveState === 'offline') return m.quiz_save_offline_long();
		if (saveState === 'error') return m.quiz_save_failed_long();
		return quizErrorMessage(check.failure);
	});

	const unanswered = $derived(check.items?.filter((item) => item.answered !== 'complete') ?? []);
	const flagged = $derived(check.items?.filter((item) => item.flagged) ?? []);

	function visit(item: NavItem) {
		check.close();
		onvisit(item);
	}
</script>

<Dialog
	bind:open={check.open}
	title={m.quiz_submit_title()}
	size="sm"
	role="alertdialog"
	dismissible={!check.submitting}
	onclose={() => check.close()}
>
	<div class="body">
		{#if check.items}
			{#if unanswered.length === 0}
				<p class="all"><IconCheckCircle aria-hidden="true" />{m.quiz_submit_all_answered()}</p>
			{:else}
				<section class="group">
					<h3 class="heading warn">
						<IconWarningCircle aria-hidden="true" />{m.quiz_submit_unanswered({
							count: unanswered.length
						})}
					</h3>
					<ul class="chips">
						{#each unanswered as item (item.slot)}
							<li>
								<button
									type="button"
									class="chip"
									onclick={() => visit(item)}
									aria-label={m.quiz_go_to_question({ number: item.number })}
								>
									{item.number}
								</button>
							</li>
						{/each}
					</ul>
				</section>
			{/if}
			{#if flagged.length > 0}
				<section class="group">
					<h3 class="heading">
						<IconFlagFill aria-hidden="true" class="flag" />{m.quiz_submit_flagged({
							count: flagged.length
						})}
					</h3>
					<ul class="chips">
						{#each flagged as item (item.slot)}
							<li>
								<button
									type="button"
									class="chip"
									onclick={() => visit(item)}
									aria-label={m.quiz_go_to_question({ number: item.number })}
								>
									{item.number}
								</button>
							</li>
						{/each}
					</ul>
				</section>
			{/if}
		{:else if check.checking}
			<div class="checking" aria-busy="true">
				<p class="muted">{m.quiz_submit_checking()}</p>
				<Skeleton width="70%" />
				<Skeleton width="45%" />
			</div>
		{/if}
		<p class="final">{m.quiz_submit_body()}</p>
		{#if check.failure && check.items}
			<Banner tone="danger" message={quizErrorMessage(check.failure)} role="alert" />
		{:else if check.failure}
			<!-- The check failed, saving the answers most likely: it runs again only when asked. -->
			<Banner tone="danger" message={checkFailure} role="alert">
				{#snippet actions()}
					<Button
						variant="ghost"
						size="sm"
						disabled={check.submitting}
						onclick={() => void check.check()}
					>
						{m.common_retry()}
					</Button>
				{/snippet}
			</Banner>
		{/if}
	</div>
	{#snippet footer()}
		<Button
			variant="ghost"
			disabled={check.submitting}
			onclick={() => check.close()}
			data-autofocus
		>
			{m.quiz_keep_working()}
		</Button>
		<Button
			variant="solid"
			icon={IconPaperPlane}
			loading={check.submitting}
			disabled={check.checking || !online}
			onclick={() => void check.confirm()}
		>
			{m.quiz_submit_confirm()}
		</Button>
	{/snippet}
</Dialog>

<style>
	.body {
		display: grid;
		gap: 16px;
	}

	.all,
	.heading {
		display: flex;
		align-items: center;
		gap: 8px;
		font: var(--type-ui-strong);
	}

	.all {
		color: var(--success);
	}

	.all :global(svg),
	.heading :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
	}

	.warn {
		color: var(--warning);
	}

	.heading :global(.flag) {
		color: var(--danger);
	}

	.group {
		display: grid;
		gap: 10px;
	}

	.chips {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.chip {
		display: grid;
		place-items: center;
		min-inline-size: 40px;
		block-size: 40px;
		padding-inline: 10px;
		border: 0;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		line-height: 1;
		font-variant-numeric: tabular-nums;
		cursor: pointer;
	}

	@media (hover: hover) {
		.chip:hover {
			box-shadow: inset 0 0 0 1px var(--border-strong);
		}
	}

	.checking {
		display: grid;
		gap: 8px;
	}

	.muted {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.final {
		color: var(--text-muted);
		font: var(--type-body);
	}
</style>
