<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCheck from '~icons/ph/check-bold';
	import IconLock from '~icons/ph/lock-simple';
	import IconSeal from '~icons/ph/seal-check';
	import IconWarning from '~icons/ph/warning-circle';
	import { m } from '#lib/paraglide/messages.js';
	import Badge from '#lib/ui/Badge.svelte';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import type { PlanEditor } from '../editor.svelte.ts';
	import { approvalText, optionDetails, optionTitle } from '../labels.ts';

	interface Props {
		editor: PlanEditor;
		/** Loads the chosen path's rules again and moves on. */
		onretry: () => void;
	}

	let { editor, onretry }: Props = $props();

	const name = $props.id();
	const chosen = $derived(editor.selectedSchemaId !== null);
</script>

<section class="step" aria-labelledby="{name}-title">
	<header class="intro">
		<h2 id="{name}-title" class="title" tabindex="-1" data-step-heading>
			{m.studyplanedit_choose_path()}
		</h2>
		<p class="status" aria-live="polite">
			<span>{m.studyplanedit_path_pick()}</span>
			<StatusChip
				size="sm"
				status={chosen ? 'success' : 'warning'}
				label={chosen ? m.studyplanedit_path_chosen() : m.studyplanedit_path_mandatory()}
			/>
		</p>
		<p class="hint">{m.studyplanedit_path_hint()}</p>
	</header>

	<fieldset class="options">
		<legend class="sr-only">{m.studyplanedit_choose_path()}</legend>
		{#each editor.options as option (option.schemaId)}
			{@const selected = editor.selectedSchemaId === option.schemaId}
			{@const details = optionDetails(option)}
			{@const approval = approvalText(option.approval)}
			<label class={['option', { selected }]}>
				<input
					class="input"
					type="radio"
					{name}
					value={option.schemaId}
					checked={selected}
					aria-labelledby="{name}-{option.schemaId}"
					aria-describedby="{name}-{option.schemaId}-more"
					onchange={() => editor.select(option.schemaId)}
				/>
				<span class="text">
					<span class="heading" id="{name}-{option.schemaId}">
						<span class="label">{optionTitle(option)}</span>
						{#if option.isCurrent}
							<Badge tone="accent" variant={selected ? 'outline' : 'soft'}>
								{m.studyplanedit_current()}
							</Badge>
						{/if}
					</span>
					<span class="more" id="{name}-{option.schemaId}-more">
						{#if details}<span class="details">{details}</span>{/if}
						{#if approval}
							<span class="line"><IconSeal aria-hidden="true" />{approval}</span>
						{/if}
						{#if option.conditionNote}
							<span class="line condition">
								<IconWarning aria-hidden="true" />
								{m.studyplanedit_condition({ note: option.conditionNote })}
							</span>
						{/if}
					</span>
				</span>
				<span class="knob" aria-hidden="true"><IconCheck /></span>
			</label>
		{/each}
	</fieldset>

	{#if editor.error}
		<Banner tone="warning" message={m.studyplanedit_rules_not_loaded()} role="alert">
			{#snippet actions()}
				<Button
					size="sm"
					variant="ghost"
					icon={IconArrowClockwise}
					loading={editor.loading}
					onclick={onretry}
				>
					{m.common_retry()}
				</Button>
			{/snippet}
		</Banner>
	{:else if editor.rules?.length === 0}
		<p class="empty" role="status">
			<IconLock aria-hidden="true" />{m.studyplanedit_path_no_rules()}
		</p>
	{/if}
</section>

<style>
	.step {
		display: grid;
		gap: var(--sp-5);
	}

	.intro {
		display: grid;
		gap: 8px;
	}

	.title {
		font: var(--type-title-m);
		font-size: 1.375rem;
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
		outline: none;
	}

	.status {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 6px 10px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.hint {
		max-inline-size: 60ch;
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.875rem;
		text-wrap: pretty;
	}

	.options {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		border: 0;
		min-inline-size: 0;
	}

	.option {
		position: relative;
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		align-items: center;
		gap: 14px;
		padding: 14px 16px;
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
		cursor: pointer;
		touch-action: manipulation;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.option:first-of-type {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.option:last-of-type {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	@media (hover: hover) {
		.option:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.option.selected {
		background: var(--container);
	}

	.option:has(.input:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.input {
		position: absolute;
		opacity: 0;
		pointer-events: none;
	}

	.text {
		display: grid;
		gap: 4px;
		min-inline-size: 0;
	}

	.more {
		display: grid;
		gap: 4px;
	}

	.more:empty {
		display: none;
	}

	.heading {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 4px 8px;
	}

	.label {
		font: var(--type-body-strong);
		font-size: 1rem;
		line-height: 1.3;
	}

	.selected .label {
		color: var(--on-container);
	}

	.details {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.line {
		display: flex;
		align-items: flex-start;
		gap: 6px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.line :global(svg) {
		flex: none;
		inline-size: 15px;
		block-size: 15px;
		margin-block-start: 1px;
	}

	.condition {
		color: var(--warning);
	}

	.knob {
		display: grid;
		place-items: center;
		inline-size: 24px;
		block-size: 24px;
		border: 2px solid var(--border-strong);
		border-radius: 50%;
		color: transparent;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			border-color var(--dur-fast) var(--ease-out);
	}

	.knob :global(svg) {
		inline-size: 13px;
		block-size: 13px;
		transform: scale(0.4);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.selected .knob {
		border-color: var(--accent);
		background: var(--accent);
		color: var(--on-accent);
	}

	.selected .knob :global(svg) {
		transform: scale(1);
	}

	.empty {
		display: flex;
		align-items: center;
		gap: 8px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.empty :global(svg) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
	}
</style>
