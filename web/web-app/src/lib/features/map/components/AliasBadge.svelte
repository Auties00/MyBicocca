<script lang="ts">
	import type { Building } from '../catalog.ts';
	import { pinLabel } from '../names.ts';

	interface Props {
		building: Building;
		size?: 'md' | 'lg';
		/** Lit, as its pin is: pointed at on the map. */
		active?: boolean;
	}

	let { building, size = 'md', active = false }: Props = $props();

	const label = $derived(pinLabel(building));
</script>

<span class={['badge', size, { active, long: label.length > 3 }]} aria-hidden="true">{label}</span>

<style>
	.badge {
		display: grid;
		place-items: center;
		flex: none;
		inline-size: 36px;
		block-size: 36px;
		border-radius: 50%;
		background: var(--container);
		color: var(--on-container);
		font: 650 0.8125rem / 1 var(--font-ui);
		letter-spacing: -0.01em;
		font-variant-numeric: tabular-nums;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.long {
		font-size: 0.6875rem;
	}

	.lg {
		inline-size: 48px;
		block-size: 48px;
		font-size: 1rem;
	}

	.lg.long {
		font-size: 0.8125rem;
	}

	.active {
		background: var(--accent);
		color: var(--on-accent);
		transform: scale(1.06);
	}
</style>
