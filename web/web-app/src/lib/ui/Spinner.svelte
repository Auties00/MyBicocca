<script lang="ts">
	interface Props {
		/** Diameter in px. */
		size?: number;
		/** Announced to screen readers; omit when the surrounding control already says it. */
		label?: string;
	}

	let { size = 18, label }: Props = $props();
</script>

<svg
	class="spinner"
	width={size}
	height={size}
	viewBox="0 0 24 24"
	role={label ? 'img' : undefined}
	aria-label={label}
	aria-hidden={label ? undefined : 'true'}
>
	<circle class="track" cx="12" cy="12" r="9.5" />
	<circle class="arc" cx="12" cy="12" r="9.5" pathLength="100" />
</svg>

<style>
	.spinner {
		animation: spin 0.8s linear infinite;
	}

	circle {
		fill: none;
		stroke: currentColor;
		stroke-width: 2.5;
	}

	.track {
		opacity: 0.2;
	}

	.arc {
		stroke-linecap: round;
		stroke-dasharray: 28 100;
	}

	@media (prefers-reduced-motion: reduce) {
		.spinner {
			animation-duration: 1.6s;
		}
	}

	@keyframes spin {
		to {
			transform: rotate(1turn);
		}
	}
</style>
