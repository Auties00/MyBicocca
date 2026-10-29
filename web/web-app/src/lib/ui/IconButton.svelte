<script lang="ts">
	import type { HTMLAttributes } from 'svelte/elements';
	import Button from './Button.svelte';
	import { tooltip, type TooltipPlacement } from './tooltip.svelte.ts';
	import type { Href, Icon } from './types.ts';

	interface Props extends Omit<HTMLAttributes<HTMLElement>, 'children' | 'onclick' | 'class'> {
		icon: Icon;
		/** Accessible name, also shown as the tooltip. */
		label: string;
		variant?: 'ghost' | 'tonal' | 'outline' | 'solid' | 'danger';
		size?: 'sm' | 'md' | 'lg';
		/** Shown as keycaps in the tooltip and exposed as `aria-keyshortcuts`. */
		shortcut?: readonly string[];
		/** Hide the tooltip where the label is already visible next to the button. */
		hideTooltip?: boolean;
		tooltipPlacement?: TooltipPlacement;
		href?: Href;
		pressed?: boolean;
		disabled?: boolean;
		loading?: boolean;
		type?: 'button' | 'submit' | 'reset';
		onclick?: (event: MouseEvent) => void;
	}

	let {
		icon,
		label,
		variant = 'ghost',
		size = 'md',
		shortcut,
		hideTooltip = false,
		tooltipPlacement,
		...rest
	}: Props = $props();
</script>

<Button
	{...rest}
	{icon}
	{label}
	{variant}
	{size}
	{@attach tooltip(hideTooltip ? undefined : label, { shortcut, placement: tooltipPlacement })}
/>
