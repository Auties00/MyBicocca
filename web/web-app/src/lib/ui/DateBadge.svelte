<script lang="ts">
	import { formatDate, formatWeekday } from '#lib/format/dates.ts';
	import { intlLocale } from '#lib/format/locale.ts';
	import type { CourseSlot } from './types.ts';

	interface Props {
		date: Date;
		size?: 'sm' | 'md' | 'lg';
		/** accent (default) for the user's own bookings; neutral for past or informative dates. */
		tone?: 'accent' | 'neutral';
		course?: CourseSlot;
		/** Adds the weekday above the day ("gio"). */
		weekday?: boolean;
	}

	let { date, size = 'md', tone = 'accent', course, weekday = false }: Props = $props();

	const month = $derived(
		new Intl.DateTimeFormat(intlLocale(), { month: 'short' }).format(date).replace('.', '')
	);
	const dayName = $derived(formatWeekday(date, 'short').replace('.', ''));
	const full = $derived(formatDate(date, 'full'));
	const iso = $derived(
		`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
	);
</script>

<time
	class={['badge', size, course ? 'course' : tone]}
	datetime={iso}
	data-course={course}
	aria-label={full}
>
	{#if weekday}<span class="weekday" aria-hidden="true">{dayName}</span>{/if}
	<span class="day" aria-hidden="true">{date.getDate()}</span>
	<span class="month" aria-hidden="true">{month}</span>
</time>

<style>
	.badge {
		--size: 52px;
		display: inline-grid;
		place-content: center;
		justify-items: center;
		flex-shrink: 0;
		inline-size: var(--size);
		min-block-size: var(--size);
		padding-block: 6px;
		border-radius: calc(var(--size) * 0.28);
		background: var(--d-bg);
		color: var(--d-fg);
		box-shadow: var(--inner-light);
		line-height: 1;
	}

	.sm {
		--size: 40px;
	}

	.lg {
		--size: 64px;
	}

	.accent {
		--d-bg: var(--container);
		--d-fg: var(--on-container);
	}

	.neutral {
		--d-bg: var(--surface-sunken);
		--d-fg: var(--text);
	}

	.course {
		--d-bg: var(--course-container);
		--d-fg: var(--course-ink);
	}

	/* A list-level figure: the UI face, so the page's title stays its one serif moment. */
	.day {
		font: 600 calc(var(--size) * 0.4) / 1 var(--font-ui);
		letter-spacing: -0.02em;
		font-variant-numeric: lining-nums tabular-nums;
	}

	.month,
	.weekday {
		font: 600 calc(var(--size) * 0.19) / 1.2 var(--font-ui);
		letter-spacing: 0.06em;
		text-transform: uppercase;
		opacity: 0.8;
	}

	.weekday {
		margin-block-end: 2px;
	}
</style>
