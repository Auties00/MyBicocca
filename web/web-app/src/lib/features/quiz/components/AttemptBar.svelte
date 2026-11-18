<script lang="ts">
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import type { SaveState } from '../attempt.svelte.ts';
	import type { TimerPhase } from '../model.ts';
	import SaveStatus from './SaveStatus.svelte';
	import Timer from './Timer.svelte';

	interface Props {
		course: string | undefined;
		name: string;
		saveState: SaveState;
		onretry: () => void;
		/** The countdown, when it shows: milliseconds left and how urgent they are. */
		countdown: { left: number; phase: TimerPhase } | null;
		/**
		 * Finish in the bar, where the page itself doesn't offer it yet: on tablets, before the
		 * last page (the dock has it there, the side card from 1024 px).
		 */
		finish: boolean;
		finishDisabled: boolean;
		onfinish: () => void;
		onexit: () => void;
	}

	let {
		course,
		name,
		saveState,
		onretry,
		countdown,
		finish,
		finishDisabled,
		onfinish,
		onexit
	}: Props = $props();
</script>

<header class="bar">
	<IconButton icon={IconX} label={m.quiz_exit()} onclick={onexit} />
	<div class="titles">
		<p class="course">{course ?? ''}</p>
		<p class="name">{name}</p>
	</div>
	<SaveStatus state={saveState} {onretry} />
	{#if countdown}
		<Timer left={countdown.left} phase={countdown.phase} />
	{/if}
	{#if finish}
		<span class="finish">
			<Button
				variant="solid"
				size="sm"
				icon={IconPaperPlane}
				disabled={finishDisabled}
				onclick={onfinish}
			>
				{m.quiz_finish()}
			</Button>
		</span>
	{/if}
</header>

<style>
	.bar {
		position: sticky;
		inset-block-start: 0;
		z-index: var(--z-topbar);
		display: flex;
		align-items: center;
		gap: 8px 12px;
		min-block-size: calc(var(--bar-h) + var(--safe-top));
		padding: var(--safe-top) var(--gutter) 0 max(8px, calc(var(--gutter) - 8px));
		border-block-end: 1px solid var(--hairline);
		background: var(--veil);
		backdrop-filter: var(--veil-filter);
	}

	.titles {
		display: grid;
		flex: 1 1 auto;
		min-inline-size: 0;
	}

	.course {
		overflow: hidden;
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-overflow: ellipsis;
		text-transform: uppercase;
		white-space: nowrap;
	}

	.name {
		overflow: hidden;
		font: var(--type-title-s);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.finish {
		display: none;
	}

	@media (600px <= width < 1024px) {
		.finish {
			display: inline-flex;
		}
	}
</style>
