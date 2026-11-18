<script lang="ts" module>
	/** A ten-lobed "sunny" shape, Android's selected knob, as a path in a 24-unit box. */
	const SUNNY = (() => {
		const points: string[] = [];
		for (let step = 0; step < 120; step++) {
			const angle = (step / 120) * 2 * Math.PI;
			const radius = 10.4 + 1.25 * Math.cos(10 * angle);
			const x = 12 + radius * Math.cos(angle);
			const y = 12 + radius * Math.sin(angle);
			points.push(`${x.toFixed(2)} ${y.toFixed(2)}`);
		}
		return `M${points.join('L')}Z`;
	})();
</script>

<script lang="ts">
	interface Props {
		checked: boolean;
	}

	let { checked }: Props = $props();
</script>

<!-- An empty ring that turns into a filled scalloped badge with a check when chosen. -->
<svg class={['knob', { checked }]} viewBox="0 0 24 24" aria-hidden="true">
	<circle class="ring" cx="12" cy="12" r="10" />
	<path class="sunny" d={SUNNY} />
	<path class="check" d="M7.5 12.4l3 3 6-6.4" />
</svg>

<style>
	.knob {
		flex-shrink: 0;
		inline-size: 26px;
		block-size: 26px;
		overflow: visible;
	}

	.ring {
		fill: none;
		stroke: var(--border-strong);
		stroke-width: 1.5;
		transition: opacity var(--dur-fast) var(--ease-out);
	}

	.sunny {
		fill: var(--accent);
		transform: scale(0.4) rotate(-60deg);
		transform-origin: 12px 12px;
		opacity: 0;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			opacity var(--dur-fast) var(--ease-out);
	}

	.check {
		fill: none;
		stroke: var(--on-accent);
		stroke-width: 2.2;
		stroke-linecap: round;
		stroke-linejoin: round;
		stroke-dasharray: 16;
		stroke-dashoffset: 16;
		transition: stroke-dashoffset var(--dur-slow) var(--ease-out) 80ms;
	}

	.checked .ring {
		opacity: 0;
	}

	.checked .sunny {
		transform: none;
		opacity: 1;
	}

	.checked .check {
		stroke-dashoffset: 0;
	}
</style>
