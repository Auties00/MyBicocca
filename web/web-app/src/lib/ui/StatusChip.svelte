<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import IconXCircle from '~icons/ph/x-circle-fill';
	import IconInfo from '~icons/ph/info-fill';
	import IconHourglass from '~icons/ph/hourglass-medium-fill';
	import IconCircleDashed from '~icons/ph/circle-dashed';
	import IconDotOutline from '~icons/ph/dot-outline-fill';
	import IconMedal from '~icons/ph/medal-fill';
	import type { Icon, Status } from './types.ts';

	interface Props {
		status: Status;
		/** Always visible: status is never told by colour alone. */
		label: string;
		/** Overrides the default glyph for the status. */
		icon?: Icon;
		size?: 'sm' | 'md';
	}

	let { status, label, icon, size = 'md' }: Props = $props();

	const glyphs: Record<Status, Icon> = {
		success: IconCheckCircle,
		warning: IconWarningCircle,
		danger: IconXCircle,
		info: IconInfo,
		pending: IconHourglass,
		neutral: IconCircleDashed,
		accent: IconDotOutline,
		gold: IconMedal
	};

	const Glyph = $derived(icon ?? glyphs[status]);
</script>

<span class={['status', status, size]}>
	<Glyph aria-hidden="true" />
	<span class="text">{label}</span>
</span>

<style>
	.status {
		display: inline-flex;
		align-items: center;
		gap: 5px;
		block-size: 26px;
		padding-inline: 8px 10px;
		border-radius: var(--r-pill);
		background: var(--s-bg);
		color: var(--s-fg);
		font: 600 0.8125rem / 1 var(--font-ui);
		white-space: nowrap;
		vertical-align: middle;
	}

	.status :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		color: var(--s-icon, currentColor);
	}

	.text {
		text-box: trim-both cap alphabetic;
	}

	.sm {
		block-size: 22px;
		gap: 4px;
		padding-inline: 6px 8px;
		font-size: 0.75rem;
	}

	.sm :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.success {
		--s-bg: var(--success-container);
		--s-fg: var(--on-success-container);
		--s-icon: var(--success);
	}

	.warning {
		--s-bg: var(--warning-container);
		--s-fg: var(--on-warning-container);
		--s-icon: var(--warning);
	}

	.danger {
		--s-bg: var(--danger-container);
		--s-fg: var(--on-danger-container);
		--s-icon: var(--danger);
	}

	.info {
		--s-bg: var(--info-container);
		--s-fg: var(--on-info-container);
		--s-icon: var(--info);
	}

	.pending,
	.neutral {
		--s-bg: var(--surface-sunken);
		--s-fg: var(--text-muted);
	}

	.accent {
		--s-bg: var(--container);
		--s-fg: var(--on-container);
		--s-icon: var(--accent-text);
	}

	.gold {
		--s-bg: var(--gold-container);
		--s-fg: var(--on-gold-container);
		--s-icon: var(--gold);
	}
</style>
