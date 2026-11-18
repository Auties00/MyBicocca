<script lang="ts">
	import type { ChipTone, CourseSlot, Icon } from './types.ts';

	interface Props {
		icon: Icon;
		/** Fill colour; `course` takes precedence and uses that course's hue. */
		tone?: ChipTone;
		course?: CourseSlot;
		size?: 'sm' | 'md' | 'lg';
	}

	let { icon: Glyph, tone = 'neutral', course, size = 'md' }: Props = $props();
</script>

<span class={['chip', size, course ? 'course' : tone]} data-course={course} aria-hidden="true">
	<Glyph />
</span>

<style>
	.chip {
		--size: 40px;
		--glyph: 20px;
		display: inline-grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: var(--size);
		block-size: var(--size);
		border-radius: calc(var(--size) * 0.3);
		background: var(--chip-bg);
		color: var(--chip-fg);
		box-shadow: var(--inner-light);
	}

	.chip :global(svg) {
		inline-size: var(--glyph);
		block-size: var(--glyph);
	}

	.sm {
		--size: 32px;
		--glyph: 16px;
	}

	.lg {
		--size: 48px;
		--glyph: 24px;
	}

	.neutral {
		--chip-bg: var(--surface-sunken);
		--chip-fg: var(--text-muted);
	}

	.accent {
		--chip-bg: var(--container);
		--chip-fg: var(--accent-text);
	}

	.info {
		--chip-bg: var(--info-container);
		--chip-fg: var(--info);
	}

	.success {
		--chip-bg: var(--success-container);
		--chip-fg: var(--success);
	}

	.warning {
		--chip-bg: var(--warning-container);
		--chip-fg: var(--warning);
	}

	.danger {
		--chip-bg: var(--danger-container);
		--chip-fg: var(--danger);
	}

	.gold {
		--chip-bg: var(--gold-container);
		--chip-fg: var(--gold);
	}

	.violet {
		--chip-bg: var(--violet-container);
		--chip-fg: var(--violet);
	}

	.course {
		--chip-bg: var(--course-container);
		--chip-fg: var(--course-ink);
	}
</style>
