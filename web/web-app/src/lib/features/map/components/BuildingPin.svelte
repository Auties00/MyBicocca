<script lang="ts" module>
	import { pinShape, type Head } from '../pinShape.ts';

	const IDLE = { head: 36, tail: 9 };
	const SELECTED = { head: 46, tail: 12 };
	const upright = {
		idle: pinShape(IDLE.head, IDLE.tail),
		selected: pinShape(SELECTED.head, SELECTED.tail)
	};

	/** How far an idle pin rises above its building's point, in px. */
	export const PIN_HEIGHT = upright.idle.height;

	/** The head of the pin standing on (`x`, `y`), border included, as crowded pins are spread. */
	export function pinHead(x: number, y: number, selected: boolean): Head {
		const shape = upright[selected ? 'selected' : 'idle'];
		const size = selected ? SELECTED : IDLE;
		return { x, y: y - (shape.height - shape.headY), r: size.head / 2 + 1 };
	}
</script>

<script lang="ts">
	import type { Building } from '../catalog.ts';
	import { buildingName, buildingTitle, pinLabel } from '../names.ts';
	import type { Offset } from '../pinShape.ts';

	interface Props {
		building: Building;
		x: number;
		y: number;
		/** How far the head moves from over its point, so it doesn't cover a neighbour's. */
		offset: Offset | undefined;
		/** Stacking order among the pins; the selected and the pointed-at one rise above all. */
		layer: number;
		/** Whether keyboard focus stops on it: not while it sits off screen or under a panel. */
		focusable: boolean;
		selected: boolean;
		highlighted: boolean;
		dimmed: boolean;
		onselect: () => void;
		onhover: (inside: boolean) => void;
	}

	let {
		building,
		x,
		y,
		offset,
		layer,
		focusable,
		selected,
		highlighted,
		dimmed,
		onselect,
		onhover
	}: Props = $props();

	/* A head moved aside leans its tail back to the tip; one moved up stands on a longer tail. */
	const shape = $derived.by(() => {
		const size = selected ? SELECTED : IDLE;
		if (!offset || (offset.dx === 0 && offset.dy === 0)) {
			return upright[selected ? 'selected' : 'idle'];
		}
		return pinShape(size.head, size.tail - offset.dy, offset.dx);
	});
	const label = $derived(pinLabel(building));
</script>

<div
	class={['anchor', { selected, highlighted, dimmed }]}
	style:transform={`translate(${Math.round(x)}px, ${Math.round(y)}px)`}
	style:z-index={selected ? 1000 : highlighted ? 999 : layer}
>
	<button
		type="button"
		class="pin"
		aria-label={buildingTitle(building)}
		aria-current={selected ? 'location' : undefined}
		tabindex={focusable ? undefined : -1}
		style:--w={`${shape.width}px`}
		style:--h={`${shape.height}px`}
		style:--head-x={`${shape.headX}px`}
		style:--head-y={`${shape.headY}px`}
		style:--tip-x={`${shape.tipX}px`}
		style:--r={`${(selected ? SELECTED : IDLE).head / 2}px`}
		onclick={onselect}
		onpointerenter={(event) => event.pointerType === 'mouse' && onhover(true)}
		onpointerleave={(event) => event.pointerType === 'mouse' && onhover(false)}
		onfocus={() => onhover(true)}
		onblur={() => onhover(false)}
	>
		{#key selected}
			<svg class="shape" width={shape.width} height={shape.height} aria-hidden="true">
				<path class="ring" d={shape.path} />
				<path class="outline" d={shape.path} />
			</svg>
		{/key}
		<span class={['label', { long: label.length > 3 }]} aria-hidden="true">{label}</span>
		<span class="tag" aria-hidden="true">{buildingName(building)}</span>
	</button>
</div>

<style>
	.anchor {
		position: absolute;
		inset: 0 auto auto 0;
		inline-size: 0;
		block-size: 0;
	}

	/* Only the drawn pin takes the pointer: a leaning pin's box spans empty corners. */
	.pin {
		position: absolute;
		inset-inline-start: calc(-1 * var(--tip-x));
		inset-block-end: 0;
		inline-size: var(--w);
		block-size: var(--h);
		padding: 0;
		border: 0;
		background: none;
		cursor: pointer;
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
		pointer-events: none;
		transform-origin: var(--tip-x) 100%;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			opacity var(--dur-base) var(--ease-out);
	}

	.pin:focus-visible {
		outline: none;
	}

	.shape {
		display: block;
		overflow: visible;
		filter: drop-shadow(0 1.5px 1.5px oklch(0% 0 0 / 0.3));
	}

	.outline {
		fill: var(--accent);
		stroke: var(--surface);
		stroke-width: 2;
		pointer-events: visiblePainted;
		transition: fill var(--dur-fast) var(--ease-out);
	}

	/* Keyboard focus rings the pin outside its border, which hover never does. */
	.ring {
		fill: none;
		stroke: var(--focus);
		stroke-width: 0;
	}

	.pin:focus-visible .ring {
		stroke-width: 9;
	}

	.selected .shape {
		animation: pop var(--dur-spring) var(--spring-pop);
	}

	.selected .outline {
		fill: light-dark(var(--c12), var(--c11));
		stroke-width: 2.5;
	}

	.label {
		position: absolute;
		inset-inline-start: var(--head-x);
		inset-block-start: var(--head-y);
		color: var(--on-accent);
		font: 700 0.8125rem / 1 var(--font-ui);
		letter-spacing: -0.01em;
		white-space: nowrap;
		transform: translate(-50%, -50%);
		pointer-events: none;
	}

	.long {
		font-size: 0.6875rem;
	}

	.selected .label {
		color: light-dark(var(--on-accent), var(--c1));
		font-size: 0.9375rem;
	}

	.selected .long {
		font-size: 0.8125rem;
	}

	.tag {
		position: absolute;
		inset-inline-start: calc(var(--head-x) + var(--r) + 3px);
		inset-block-start: var(--head-y);
		padding: 3px 8px;
		border-radius: var(--r-pill);
		background: var(--surface-raised);
		box-shadow: var(--elev-2);
		color: var(--text);
		font: var(--type-label);
		font-weight: 600;
		white-space: nowrap;
		opacity: 0;
		transform: translate(-4px, -50%);
		transition:
			opacity var(--dur-fast) var(--ease-out),
			transform var(--dur-base) var(--ease-out);
		pointer-events: none;
	}

	.highlighted .pin,
	.pin:focus-visible {
		transform: scale(1.1);
	}

	.highlighted .tag,
	.selected .tag,
	.pin:focus-visible .tag {
		opacity: 1;
		transform: translate(0, -50%);
	}

	.dimmed .pin {
		opacity: 0.35;
		transform: scale(0.88);
	}

	@media (hover: hover) {
		.pin:hover {
			transform: scale(1.1);
		}

		.pin:hover .tag {
			opacity: 1;
			transform: translate(0, -50%);
		}
	}

	.pin:active {
		transform: scale(0.94);
		transition-duration: var(--dur-instant);
	}

	@keyframes pop {
		from {
			transform: scale(0.78);
		}
	}
</style>
