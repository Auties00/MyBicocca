<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconInfo from '~icons/ph/info';
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconWarning from '~icons/ph/warning';
	import IconWarningCircle from '~icons/ph/warning-circle';
	import IconSeal from '~icons/ph/seal-check';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import type { Icon, Tone } from './types.ts';

	interface Props {
		tone?: Tone;
		title?: string;
		/** Body text; use `children` for rich content. */
		message?: string;
		children?: Snippet;
		/** Overrides the tone's glyph. */
		icon?: Icon;
		/** Buttons or links on the right (below on narrow containers). */
		actions?: Snippet;
		/** Shows a close button. */
		ondismiss?: () => void;
		/** 'status' or 'alert' when the banner appears in response to something just happened. */
		role?: 'status' | 'alert';
	}

	let { tone = 'info', title, message, children, icon, actions, ondismiss, role }: Props = $props();

	const glyphs: Record<Tone, Icon> = {
		neutral: IconInfo,
		info: IconInfo,
		accent: IconSeal,
		success: IconCheckCircle,
		warning: IconWarning,
		danger: IconWarningCircle,
		gold: IconSeal
	};

	const Glyph = $derived(icon ?? glyphs[tone]);
</script>

<div class="wrap">
	<div class={['banner', tone]} {role}>
		<span class="glyph"><Glyph aria-hidden="true" /></span>
		<div class="text">
			{#if title}<p class="title">{title}</p>{/if}
			{#if message}<p class="message">{message}</p>{/if}
			{#if children}<div class="message">{@render children()}</div>{/if}
		</div>
		{#if actions}<div class="actions">{@render actions()}</div>{/if}
		{#if ondismiss}
			<button class="close" type="button" aria-label={m.ui_dismiss()} onclick={ondismiss}>
				<IconX aria-hidden="true" />
			</button>
		{/if}
	</div>
</div>

<style>
	.wrap {
		container-type: inline-size;
	}

	.banner {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto auto;
		align-items: center;
		gap: 4px 12px;
		padding: 12px 14px;
		border-radius: var(--r-md);
		background: var(--n-bg);
		color: var(--n-fg);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, var(--n-icon) 14%, transparent);
	}

	.glyph {
		display: grid;
		align-self: start;
		padding-block-start: 1px;
		color: var(--n-icon);
	}

	.glyph :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.text {
		display: grid;
		gap: 2px;
	}

	.title {
		font: var(--type-ui-strong);
	}

	.message {
		font: var(--type-ui);
		font-weight: 400;
	}

	.title + .message {
		opacity: 0.85;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
		margin-block: -4px;
	}

	.close {
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		margin: -6px -6px -6px 0;
		border-radius: var(--r-pill);
		color: inherit;
		opacity: 0.7;
	}

	.close :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (hover: hover) {
		.close:hover {
			opacity: 1;
			background: var(--state-hover);
		}
	}

	@container (width < 520px) {
		.banner {
			grid-template-columns: auto minmax(0, 1fr) auto;
		}

		.actions {
			grid-column: 2 / -1;
			grid-row: 2;
			margin-block: 6px 0;
		}

		.close {
			grid-column: 3;
			grid-row: 1;
			align-self: start;
		}
	}

	.neutral {
		--n-bg: var(--surface-sunken);
		--n-fg: var(--text);
		--n-icon: var(--text-muted);
	}

	.info {
		--n-bg: var(--info-container);
		--n-fg: var(--on-info-container);
		--n-icon: var(--info);
	}

	.accent {
		--n-bg: var(--container);
		--n-fg: var(--on-container);
		--n-icon: var(--accent-text);
	}

	.success {
		--n-bg: var(--success-container);
		--n-fg: var(--on-success-container);
		--n-icon: var(--success);
	}

	.warning {
		--n-bg: var(--warning-container);
		--n-fg: var(--on-warning-container);
		--n-icon: var(--warning);
	}

	.danger {
		--n-bg: var(--danger-container);
		--n-fg: var(--on-danger-container);
		--n-icon: var(--danger);
	}

	.gold {
		--n-bg: var(--gold-container);
		--n-fg: var(--on-gold-container);
		--n-icon: var(--gold);
	}
</style>
