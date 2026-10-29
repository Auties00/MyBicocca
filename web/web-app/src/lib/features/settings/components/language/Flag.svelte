<script lang="ts">
	import IconGlobe from '~icons/ph/globe-simple';

	interface Props {
		/** `system`: a globe, for "follow the device". */
		kind: 'system' | 'it' | 'en';
		/** Plays a short wave, as the chosen flag does on Android. */
		waving?: boolean;
	}

	let { kind, waving = false }: Props = $props();

	const id = $props.id();
</script>

<span class={['flag', kind, { waving }]} aria-hidden="true">
	{#if kind === 'system'}
		<IconGlobe />
	{:else if kind === 'it'}
		<svg viewBox="0 0 3 2" preserveAspectRatio="none">
			<rect width="1" height="2" fill="#009246" />
			<rect x="1" width="1" height="2" fill="#f4f5f0" />
			<rect x="2" width="1" height="2" fill="#ce2b37" />
		</svg>
	{:else}
		<!-- English: the American flag above the diagonal, the British one below it. -->
		<svg viewBox="0 0 60 40">
			<defs>
				<clipPath id="{id}-upper"><path d="M0 0H60L0 40Z" /></clipPath>
				<clipPath id="{id}-lower"><path d="M60 0V40H0Z" /></clipPath>
				<clipPath id="{id}-cross"><path d="M30 20H60V40ZV40H0ZH0V0ZV0H60Z" /></clipPath>
				<pattern id="{id}-stars" width="3" height="3" patternUnits="userSpaceOnUse">
					<circle cx="1.5" cy="1.5" r="0.6" fill="#fff" />
				</pattern>
			</defs>
			<g clip-path="url(#{id}-upper)">
				<rect width="60" height="40" fill="#fff" />
				{#each [0, 2, 4, 6, 8, 10, 12] as stripe (stripe)}
					<rect y={(stripe * 40) / 13} width="60" height={40 / 13} fill="#b22234" />
				{/each}
				<rect width="26" height={(7 * 40) / 13} fill="#3c3b6e" />
				<rect x="0.5" y="0.5" width="25" height="20.5" fill="url(#{id}-stars)" />
			</g>
			<g clip-path="url(#{id}-lower)">
				<rect width="60" height="40" fill="#012169" />
				<path d="M0 0L60 40M60 0L0 40" stroke="#fff" stroke-width="7" />
				<path
					d="M0 0L60 40M60 0L0 40"
					clip-path="url(#{id}-cross)"
					stroke="#c8102e"
					stroke-width="4"
				/>
				<path d="M30 0V40M0 20H60" stroke="#fff" stroke-width="10" />
				<path d="M30 0V40M0 20H60" stroke="#c8102e" stroke-width="6" />
			</g>
		</svg>
	{/if}
</span>

<style>
	.flag {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 42px;
		aspect-ratio: 3 / 2;
		overflow: hidden;
		border-radius: 6px;
		transform-origin: left center;
	}

	.flag > :global(*) {
		grid-area: 1 / 1;
	}

	.flag svg {
		display: block;
		inline-size: 100%;
		block-size: 100%;
	}

	/* The hairline edge sits over the artwork, which would hide an inset shadow of the frame. */
	.flag::after {
		content: '';
		grid-area: 1 / 1;
		inline-size: 100%;
		block-size: 100%;
		border-radius: inherit;
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.system {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.system :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.waving {
		animation: wave 600ms var(--ease-in-out);
	}

	@keyframes wave {
		25% {
			transform: perspective(80px) rotateY(-14deg) skewY(-3deg);
		}
		60% {
			transform: perspective(80px) rotateY(8deg) skewY(2deg);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.waving {
			animation: none;
		}
	}
</style>
