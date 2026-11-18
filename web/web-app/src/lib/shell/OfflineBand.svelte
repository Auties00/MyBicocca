<script lang="ts" module>
	export type BandStatus = 'online' | 'offline' | 'restored';
</script>

<script lang="ts">
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import IconWifiHigh from '~icons/ph/wifi-high';
	import { m } from '#lib/paraglide/messages.js';

	interface Props {
		/** Bindable: whether the band is up ('restored' while "Back online" lingers). */
		status: BandStatus;
		/**
		 * Told the band's block size, for AppShell's `--shell-band`. Measured by a ResizeObserver
		 * alone: binding offsetHeight would force a layout on every mount.
		 */
		onheight?: (height: number) => void;
	}

	let { status = $bindable(), onheight }: Props = $props();

	/** How long "Di nuovo online" stays before the band leaves. */
	const linger = 1600;

	let timer: ReturnType<typeof setTimeout> | undefined;

	function offline() {
		clearTimeout(timer);
		status = 'offline';
	}

	function online() {
		if (status !== 'offline') return;
		status = 'restored';
		timer = setTimeout(() => (status = 'online'), linger);
	}

	function measure(size: ResizeObserverSize[] | null | undefined) {
		onheight?.(size?.[0].blockSize ?? 0);
	}
</script>

<svelte:window onoffline={offline} ononline={online} />

<!--
	Sits under the top bar on phones and over the content column from 600 px. A page that says it is
	offline in its own words marks that notice `data-offline-notice`, and the band steps aside.
-->
<div class="anchor" role="status" aria-live="polite">
	<div
		class={['band', status]}
		aria-hidden={status === 'online'}
		bind:borderBoxSize={null, measure}
	>
		{#if status === 'restored'}
			<IconWifiHigh aria-hidden="true" />
			<span>{m.shell_online()}</span>
		{:else}
			<IconWifiSlash class="pulse" aria-hidden="true" />
			<span>
				<strong>{m.shell_offline()}</strong><span class="detail"
					>{` · ${m.shell_offline_detail()}`}</span
				>
			</span>
		{/if}
	</div>
</div>

<style>
	.anchor {
		position: fixed;
		inset: var(--band-top) 0 auto var(--nav-w, 0px);
		z-index: var(--z-band);
		display: flex;
		justify-content: center;
		padding-inline: 12px;
		pointer-events: none;
	}

	:global(.shell:has([data-offline-notice])) .anchor {
		display: none;
	}

	.band {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		max-inline-size: 100%;
		padding: 8px 14px 8px 12px;
		/* A pill on one line, a rounded box when a narrow screen wraps it. */
		border-radius: calc(0.5lh + 8px);
		background: var(--band-offline);
		color: var(--on-band);
		font: var(--type-ui);
		font-weight: 400;
		text-wrap: balance;
		box-shadow: var(--elev-3);
		transition:
			transform var(--dur-spring) var(--spring-standard),
			opacity var(--dur-base) var(--ease-out),
			background-color var(--dur-base) var(--ease-out);
	}

	.band strong {
		font-weight: 600;
	}

	/* Phones keep the band to one line: the news, without the reassurance. */
	@media (width < 480px) {
		.detail {
			display: none;
		}
	}

	.band :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
	}

	.band :global(.pulse) {
		animation: pulse 0.9s var(--ease-in-out) infinite alternate;
	}

	.restored {
		background: var(--band-online);
	}

	.online {
		opacity: 0;
		transform: translateY(calc(-100% - 24px));
	}

	@media (prefers-reduced-motion: reduce) {
		.band :global(.pulse) {
			animation: none;
		}
	}

	@media print {
		.anchor {
			display: none;
		}
	}

	@keyframes pulse {
		to {
			opacity: 0.45;
		}
	}
</style>
