<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';

	interface Props {
		/** "Pagina 2", or "Riepilogo". */
		where: string;
		/** How the page stands: answers given, or those missing. */
		status: string;
		/** The status asks for attention: answers are missing. */
		alert: boolean;
		online: boolean;
		/** The primary action sends the questionnaire rather than moving on. */
		sending: boolean;
		canBack: boolean;
		/** Disabled with a reason in `status`, such as an incomplete summary. */
		blocked?: boolean;
		working: 'next' | 'back' | 'send' | null;
		/** The last move failed: what and why, and whether the primary action retries it. */
		failure?: { title: string; message: string; retry: boolean };
		onback: () => void;
		onprimary: () => void;
		ondismiss: () => void;
	}

	let {
		where,
		status,
		alert,
		online,
		sending,
		canBack,
		blocked = false,
		working,
		failure,
		onback,
		onprimary,
		ondismiss
	}: Props = $props();

	/* Picking an answer changes the count; only a new page or a problem is worth announcing. */
	const announcement = $derived(
		!online ? `${where}. ${m.questionnaires_offline_band()}` : alert ? `${where}. ${status}` : where
	);

	let height = $state(0);
</script>

<div class="bar" bind:offsetHeight={height}>
	{#if failure}
		<div class="band failure" role="alert">
			<IconWarningCircle class="band-icon" aria-hidden="true" />
			<p class="band-text">
				<strong>{failure.title}</strong>
				<span>{failure.message}</span>
			</p>
			<IconButton icon={IconX} label={m.ui_close()} size="sm" onclick={ondismiss} />
		</div>
	{:else if !online}
		<div class="band offline" data-offline-notice>
			<IconWifiSlash class="band-icon" aria-hidden="true" />
			<p class="band-text">{m.questionnaires_offline_band()}</p>
		</div>
	{/if}
	<div class="row">
		{#if canBack}
			<Button
				variant="tonal"
				icon={IconArrowLeft}
				label={m.questionnaires_previous()}
				loading={working === 'back'}
				disabled={working !== null && working !== 'back'}
				onclick={onback}
			/>
		{/if}
		<p class="where">
			<span class="place">{where}</span>
			<span class={['status', { alert: alert || !online }]}>
				{#if !online}
					<IconWifiSlash aria-hidden="true" />
					<span class="status-text">{m.questionnaires_offline_status()}</span>
				{:else}
					{#if alert}<IconWarningCircle aria-hidden="true" />{/if}
					<span class="status-text">{status}</span>
				{/if}
			</span>
		</p>
		{#if sending}
			<Button
				variant="solid"
				icon={IconPaperPlane}
				disabled={!online || blocked || (working !== null && working !== 'send')}
				loading={working === 'send'}
				aria-keyshortcuts="Control+Enter Meta+Enter"
				onclick={onprimary}
			>
				{m.questionnaires_send()}
			</Button>
		{:else}
			<Button
				variant="solid"
				icon={failure?.retry ? IconArrowClockwise : undefined}
				trailingIcon={failure?.retry ? undefined : IconArrowRight}
				disabled={!online || (working !== null && working !== 'next')}
				loading={working === 'next'}
				aria-keyshortcuts="Control+Enter Meta+Enter"
				onclick={onprimary}
			>
				{failure?.retry ? m.common_retry() : m.questionnaires_next()}
			</Button>
		{/if}
	</div>
	<p class="sr-only" aria-live="polite">{announcement}</p>
</div>
<!-- Where the bar is fixed at the screen's foot, this keeps the last answers clear of it. -->
<div class="spacer" style:--bar-h="{height}px"></div>

<style>
	.bar {
		position: sticky;
		inset-block-end: calc(var(--shell-inset-bottom) + var(--sp-4));
		z-index: var(--z-sticky);
		display: grid;
		border-radius: var(--r-xl);
		background: var(--surface-raised);
		box-shadow: var(--elev-3), var(--inner-light);
	}

	.spacer {
		display: none;
	}

	/*
	 * On phones the bar takes the place of the tab bar, which steps aside while questions are
	 * answered: at the foot of the screen however short the page, as the sheet's bar on Android.
	 */
	@media (width < 600px) {
		.bar {
			position: fixed;
			inset-inline: var(--gutter);
			inset-block-end: calc(var(--shell-inset-bottom) + 10px);
		}

		.spacer {
			display: block;
			block-size: var(--bar-h);
		}
	}

	.row {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 10px;
	}

	.band {
		display: flex;
		align-items: flex-start;
		gap: 10px;
		margin: 6px 6px 0;
		padding: 8px 12px;
		border-radius: calc(var(--r-xl) - 6px) calc(var(--r-xl) - 6px) var(--r-sm) var(--r-sm);
	}

	.failure {
		padding-inline-end: 4px;
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.offline {
		background: var(--warning-container);
		color: var(--on-warning-container);
	}

	.band :global(.band-icon) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		margin-block-start: 2px;
	}

	.band-text {
		display: grid;
		flex: 1;
		min-inline-size: 0;
		padding-block-start: 1px;
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.band-text strong {
		font: var(--type-ui-strong);
	}

	.where {
		display: grid;
		flex: 1;
		min-inline-size: 0;
		padding-inline-start: 6px;
	}

	.place {
		font: var(--type-ui-strong);
	}

	.status {
		display: flex;
		align-items: center;
		gap: 4px;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.status-text {
		min-inline-size: 0;
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
	}

	.status.alert {
		color: var(--danger);
	}

	.status :global(svg) {
		flex: none;
		inline-size: 14px;
		block-size: 14px;
	}

	@media (prefers-reduced-motion: no-preference) {
		.band {
			animation: rise var(--dur-base) var(--ease-out);
		}
	}

	@keyframes rise {
		from {
			opacity: 0;
			transform: translateY(6px);
		}
	}
</style>
