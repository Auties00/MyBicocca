<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { teachesIn, type CourseLevel, type Semester, type Syllabus } from '../../syllabus.ts';

	interface Props {
		syllabus: Syllabus;
	}

	let { syllabus }: Props = $props();

	const LEVELS: readonly CourseLevel[] = ['bachelor', 'master', 'doctorate'];
	const levelShort: Record<CourseLevel, () => string> = {
		bachelor: m.course_level_bachelor_short,
		master: m.course_level_master_short,
		doctorate: m.course_level_doctorate_short
	};
	const levelName: Record<CourseLevel, () => string> = {
		bachelor: m.course_level_bachelor,
		master: m.course_level_master,
		doctorate: m.course_level_doctorate
	};
	const semesterName: Record<Semester, () => string> = {
		first: m.course_semester_first,
		second: m.course_semester_second,
		annual: m.course_semester_annual
	};
	const semesterRange: Record<Semester, () => string> = {
		first: m.course_semester_first_range,
		second: m.course_semester_second_range,
		annual: m.course_semester_annual_range
	};

	/** September to August: the academic year. */
	const MONTHS = [8, 9, 10, 11, 0, 1, 2, 3, 4, 5, 6, 7];
	const monthNames = MONTHS.map((month) => ({
		month,
		initial: formatDate(new Date(2025, month, 1), 'monthYear')
			.charAt(0)
			.toLocaleUpperCase()
	}));

	/** The three bands of a flag, by the language's name; other languages get the course's hues. */
	const flag = $derived.by(() => {
		const language = syllabus.language?.toLowerCase() ?? '';
		if (/ital|^it\b/.test(language)) return ['#009246', '#f4f5f0', '#ce2b37'];
		if (/ingl|engl|^en\b/.test(language)) return ['#012169', '#f4f5f0', '#c8102e'];
		return null;
	});

	const language = $derived(
		syllabus.language === null
			? null
			: syllabus.language.charAt(0).toLocaleUpperCase() + syllabus.language.slice(1).toLowerCase()
	);
</script>

<section class="info" aria-labelledby="course-info-title">
	<h2 class="heading" id="course-info-title">{m.course_info_title()}</h2>

	{#if syllabus.credits !== null || syllabus.hours !== null}
		<dl class="stats">
			{#if syllabus.credits !== null}
				<div class="stat">
					<dt class="caption">{m.course_credits_caption()}</dt>
					<dd class="value">
						<span class="number">{formatNumber(syllabus.credits)}</span>
						<span class="unit">{m.course_credits_unit()}</span>
					</dd>
				</div>
			{/if}
			{#if syllabus.hours !== null}
				<div class="stat">
					<dt class="caption">{m.course_hours_caption()}</dt>
					<dd class="value">
						<span class="number">{formatNumber(syllabus.hours)}</span>
						<span class="unit">{m.course_hours_unit()}</span>
					</dd>
				</div>
			{/if}
		</dl>
	{/if}

	{#if language !== null || syllabus.level !== null}
		<dl class="pair">
			{#if language !== null}
				<div class="fact">
					<dt class="caption">{m.course_language_caption()}</dt>
					<dd class="fact-value">
						<span class={['flag', { custom: flag === null }]} aria-hidden="true">
							{#each flag ?? ['', '', ''] as color, index (index)}
								<span style:background={color || undefined}></span>
							{/each}
						</span>
						{language}
					</dd>
				</div>
			{/if}
			{#if syllabus.level !== null}
				{@const level = syllabus.level}
				<div class="fact">
					<dt class="caption">{m.course_level_caption()}</dt>
					<dd class="fact-value">
						<span class="pips" aria-hidden="true">
							{#each LEVELS as option (option)}
								<span class={['pip', { on: option === level }]}>{levelShort[option]()}</span>
							{/each}
						</span>
						{levelName[level]()}
					</dd>
				</div>
			{/if}
		</dl>
	{/if}

	{#if syllabus.semester !== null}
		{@const term = syllabus.semester}
		<div class="term">
			<ol class="months" aria-hidden="true">
				{#each monthNames as { month, initial } (month)}
					<li class={{ on: teachesIn(term, month) }}>
						<span class="initial">{initial}</span>
						<span class="tick"></span>
					</li>
				{/each}
			</ol>
			<p class="term-name">
				<strong>{semesterName[term]()}</strong>
				<span class="range">{semesterRange[term]()}</span>
			</p>
		</div>
	{/if}
</section>

<style>
	.info {
		display: grid;
		gap: 20px;
		padding: 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.heading {
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		color: var(--text-muted);
	}

	.stats,
	.pair {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12px;
	}

	.stat,
	.fact {
		display: flex;
		flex-direction: column-reverse;
		justify-content: flex-end;
		gap: 4px;
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--course-container);
	}

	.value {
		display: flex;
		align-items: baseline;
		gap: 6px;
	}

	.number {
		color: var(--course-ink);
		font: var(--type-numeral-l);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.unit {
		font: var(--type-ui-strong);
		color: var(--course-ink);
	}

	.caption {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.fact {
		background: var(--surface-sunken);
	}

	.fact-value {
		display: grid;
		gap: 10px;
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
	}

	.flag {
		display: flex;
		inline-size: 36px;
		block-size: 24px;
		overflow: hidden;
		border-radius: var(--r-2xs);
		box-shadow: 0 0 0 1px var(--hairline);
	}

	.flag > span {
		flex: 1;
	}

	.custom > span:nth-child(1) {
		background: var(--course-bar);
	}

	.custom > span:nth-child(2) {
		background: var(--surface);
	}

	.custom > span:nth-child(3) {
		background: var(--gold);
	}

	.pips {
		display: flex;
		gap: 4px;
	}

	.pip {
		display: grid;
		place-items: center;
		min-inline-size: 28px;
		block-size: 24px;
		padding-inline: 6px;
		border-radius: var(--r-pill);
		box-shadow: inset 0 0 0 1px var(--border);
		color: var(--text-muted);
		font: var(--type-label);
		font-weight: 650;
	}

	.pip.on {
		background: var(--course-bar);
		box-shadow: none;
		color: var(--surface);
	}

	.term {
		display: grid;
		gap: 12px;
	}

	.months {
		display: grid;
		grid-template-columns: repeat(12, minmax(0, 1fr));
		gap: 4px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.months li {
		display: grid;
		justify-items: center;
		gap: 6px;
		color: var(--text-subtle);
		font: var(--type-label);
	}

	.months li.on {
		color: var(--text);
		font-weight: 700;
	}

	.tick {
		inline-size: 100%;
		block-size: 10px;
		border-radius: var(--r-pill);
		background: var(--track);
	}

	.on .tick {
		background: var(--course-bar);
	}

	.term-name {
		display: flex;
		flex-wrap: wrap;
		align-items: baseline;
		gap: 4px 10px;
		font: var(--type-ui);
	}

	.range {
		color: var(--text-muted);
		font-style: italic;
	}
</style>
