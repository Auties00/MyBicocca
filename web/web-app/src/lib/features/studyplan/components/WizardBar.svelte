<script lang="ts">
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';

	interface Props {
		/** The step on screen, from 0, of `total`; the total is unknown before the rules load. */
		index: number;
		total: number | undefined;
		/** The bar's main button: on to the next step, send the plan, or none at all. */
		action: 'next' | 'submit' | undefined;
		/** The main button is the view's primary action, so it's solid. */
		primary: boolean;
		enabled: boolean;
		/** The next step is loading (the chosen path's rules). */
		advancing: boolean;
		/** Why the plan can't be sent yet, beside Submit. */
		hint: string | undefined;
		onback: () => void;
		onaction: () => void;
	}

	let { index, total, action, primary, enabled, advancing, hint, onback, onaction }: Props =
		$props();

	/*
	 * Keyboard focus scrolls a row into view above the bar rather than under it: while the bar is
	 * shown, the document's scroll padding reserves the height it covers. The browser scrolls the
	 * checkbox, not its row, hence the half row more.
	 */
	const ROW_BELOW_CONTROL = 36;

	let height = $state(0);

	function reserve(node: HTMLElement) {
		const root = document.documentElement;
		const before = root.style.scrollPaddingBlockEnd;
		const update = () => {
			const covered = parseFloat(getComputedStyle(node).bottom) + node.offsetHeight;
			root.style.scrollPaddingBlockEnd = `${Math.ceil(covered) + ROW_BELOW_CONTROL}px`;
		};
		const observer = new ResizeObserver(update);
		observer.observe(node);
		window.addEventListener('resize', update);
		return () => {
			observer.disconnect();
			window.removeEventListener('resize', update);
			root.style.scrollPaddingBlockEnd = before;
		};
	}
</script>

<div class="bar" bind:offsetHeight={height} {@attach reserve}>
	<div class="inner">
		{#if index > 0}
			<Button variant="tonal" icon={IconArrowLeft} label={m.ui_back()} onclick={onback} />
		{/if}
		<p class="progress">
			{#if total !== undefined && total > 1}
				<span class="dots" aria-hidden="true">
					{#each { length: total }, dot (dot)}
						<span class={['dot', { active: dot === index, done: dot < index }]}></span>
					{/each}
				</span>
			{/if}
			<span class="count">
				{total === undefined
					? m.studyplanedit_step({ current: index + 1 })
					: m.studyplanedit_step_of({ current: index + 1, total })}
			</span>
		</p>
		{#if action === 'next'}
			<Button
				variant={primary ? 'solid' : 'tonal'}
				trailingIcon={IconArrowRight}
				disabled={!enabled}
				loading={advancing}
				onclick={onaction}
			>
				{m.studyplanedit_next()}
			</Button>
		{:else if action === 'submit'}
			<Button
				variant={primary ? 'solid' : 'tonal'}
				icon={IconPaperPlane}
				disabled={!enabled}
				onclick={onaction}
			>
				{m.studyplanedit_submit_plan()}
			</Button>
		{/if}
	</div>
	{#if action === 'submit' && hint}<p class="hint">{hint}</p>{/if}
</div>
<!-- Where the bar is fixed at the screen's foot, this keeps the last rows clear of it. -->
<div class="spacer" style:--bar-h="{height}px"></div>

<style>
	.bar {
		position: sticky;
		inset-block-end: calc(var(--shell-inset-bottom) + var(--sp-4));
		z-index: var(--z-sticky);
		display: grid;
		gap: 6px;
		padding: 10px;
		border-radius: var(--r-xl);
		background: var(--surface-raised);
		box-shadow: var(--elev-3), var(--inner-light);
		container-type: inline-size;
	}

	.spacer {
		display: none;
	}

	/*
	 * On phones the bar takes the place of the tab bar, which the editor hides: at the foot of the
	 * screen, as wide, the same material, and only as tall as its buttons, so the rows keep the
	 * screen.
	 */
	@media (width < 600px) {
		.bar {
			position: fixed;
			inset-inline: calc(var(--gutter) - 6px);
			inset-block-end: calc(var(--shell-inset-bottom) + 10px);
			padding: 6px;
			background: var(--veil-raised);
			-webkit-backdrop-filter: var(--veil-filter);
			backdrop-filter: var(--veil-filter);
			box-shadow: var(--elev-1), var(--inner-light);
		}

		.spacer {
			display: block;
			block-size: var(--bar-h);
		}
	}

	.inner {
		display: flex;
		align-items: center;
		gap: 10px;
		min-block-size: var(--control-h);
	}

	.progress {
		flex: 1;
		display: flex;
		align-items: center;
		gap: 10px;
		min-inline-size: 0;
		padding-inline-start: 6px;
	}

	.dots {
		display: flex;
		align-items: center;
		gap: 5px;
		overflow: hidden;
	}

	.dot {
		flex: none;
		inline-size: 7px;
		block-size: 7px;
		border-radius: var(--r-pill);
		background: var(--border-strong);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.dot.done {
		background: var(--accent-text);
		opacity: 0.55;
	}

	.dot.active {
		inline-size: 22px;
		background: var(--accent);
	}

	.count {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.hint {
		padding-inline: 8px;
		padding-block-end: 2px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	@container (width < 400px) {
		.dots {
			display: none;
		}
	}
</style>
