<script lang="ts">
	import IconShieldCheck from '~icons/ph/shield-check';
	import IconClockCountdown from '~icons/ph/clock-countdown';
	import { m } from '#lib/paraglide/messages.js';
	import Switch from '#lib/ui/Switch.svelte';

	interface Props {
		checked: boolean;
		disabled?: boolean;
	}

	let { checked = $bindable(), disabled = false }: Props = $props();
</script>

<div class={['remember', { on: checked }]}>
	<span class="glyph" aria-hidden="true">
		{#if checked}<IconShieldCheck />{:else}<IconClockCountdown />{/if}
	</span>
	<Switch
		bind:checked
		{disabled}
		name="remember"
		label={m.auth_remember()}
		description={checked ? m.auth_remember_hint() : m.auth_remember_off_hint()}
	/>
</div>

<style>
	.remember {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: 12px;
		padding: 12px 14px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		transition: background-color var(--dur-base) var(--ease-out);
	}

	.on {
		background: var(--container);
		color: var(--on-container);
	}

	.glyph {
		display: grid;
		margin-block-start: 1px;
		color: var(--text-muted);
	}

	.on .glyph {
		color: var(--accent-text);
	}

	.glyph :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}
</style>
