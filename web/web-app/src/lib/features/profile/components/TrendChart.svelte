<script lang="ts" module>
	/* The line draws itself in once per session, as count-ups do. */
	const session = { drawn: false };
</script>

<script lang="ts">
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { italianTitleCase } from '#lib/format/names.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatAverage } from '#lib/format/grades.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { GRADE_MAX, GRADE_MIN, nearestIndex, yearSpans } from '../chart.ts';
	import type { TrendPoint } from '../transcript.ts';
	import { gradeText } from '../format.ts';

	interface Props {
		/** In the order they were passed; undefined while they load. Two make a trend. */
		points: readonly TrendPoint[] | undefined;
		/** Esse3's arithmetic average, drawn as a reference line. */
		average: number | null;
	}

	let { points, average }: Props = $props();

	const PAD = { top: 18, right: 14, bottom: 30, left: 36 };
	/** How far from a point a tap still picks it. */
	const REACH = 24;
	const GRID = [
		{ value: GRADE_MIN, label: '18' },
		{ value: 24, label: '24' },
		{ value: GRADE_MAX, label: '30L' }
	];

	const id = $props.id();
	const draw = !session.drawn;
	session.drawn = true;

	let root = $state<HTMLElement>();
	let width = $state(0);
	let boxHeight = $state(0);
	let tipWidth = $state(0);
	let tipHeight = $state(0);
	/* Selections belong to the points they were made on: new data clears them. */
	let picked = $state.raw<{ on: readonly TrendPoint[]; index: number }>();
	let hovered = $state<number>();

	/** The points drawn: none until there are two. */
	const series = $derived(points && points.length >= 2 ? points : []);
	/*
	 * The plot's own height, which its skeleton and the "too few" line share; it grows past it when
	 * the card is stretched beside a taller column.
	 */
	const natural = $derived(width >= 560 ? 248 : 208);
	const height = $derived(Math.max(natural, boxHeight));
	const plotWidth = $derived(Math.max(0, width - PAD.left - PAD.right));
	const plotHeight = $derived(height - PAD.top - PAD.bottom);
	const baseline = $derived(yOf(GRADE_MIN));
	const xs = $derived(
		series.map((_, index) => PAD.left + (plotWidth * index) / Math.max(1, series.length - 1))
	);
	const ys = $derived(series.map((point) => yOf(point.value)));
	const line = $derived(
		xs.map((x, i) => `${i === 0 ? 'M' : 'L'}${x.toFixed(1)} ${ys[i].toFixed(1)}`).join(' ')
	);
	const area = $derived(
		xs.length === 0
			? ''
			: `${line} L${xs[xs.length - 1].toFixed(1)} ${baseline} L${xs[0].toFixed(1)} ${baseline} Z`
	);
	const bands = $derived(
		yearSpans(series).map((span) => {
			const start = edge(span.first);
			const end = edge(span.last + 1);
			return { year: span.year, start, end, center: (start + end) / 2 };
		})
	);
	const selected = $derived(picked?.on === series ? picked.index : undefined);
	const shown = $derived(hovered ?? selected);
	const point = $derived(shown === undefined ? undefined : series[shown]);
	const tip = $derived.by(() => {
		if (shown === undefined) return undefined;
		const x = xs[shown];
		const y = ys[shown];
		const left = Math.min(Math.max(x - tipWidth / 2, 0), Math.max(0, width - tipWidth));
		const above = y - tipHeight - 14;
		return { left, top: above >= 0 ? above : y + 14 };
	});

	function yOf(value: number): number {
		return PAD.top + plotHeight * (1 - (value - GRADE_MIN) / (GRADE_MAX - GRADE_MIN));
	}

	/** Where the band of points from `index` begins: halfway from the previous point. */
	function edge(index: number): number {
		if (index <= 0) return PAD.left;
		if (index >= series.length) return PAD.left + plotWidth;
		return (xs[index - 1] + xs[index]) / 2;
	}

	function describe(at: TrendPoint): string {
		return m.profile_chart_point({
			name: italianTitleCase(at.row.name),
			date: formatDate(at.date, 'long'),
			grade: gradeText(at.grade, true)
		});
	}

	function select(index: number | undefined) {
		picked = index === undefined ? undefined : { on: series, index };
	}

	function localX(event: MouseEvent | PointerEvent): number {
		return event.clientX - (root?.getBoundingClientRect().left ?? 0);
	}

	function keydown(event: KeyboardEvent) {
		const last = series.length - 1;
		const current = selected ?? last;
		let next: number | undefined;
		if (event.key === 'ArrowRight' || event.key === 'ArrowUp') next = Math.min(last, current + 1);
		else if (event.key === 'ArrowLeft' || event.key === 'ArrowDown')
			next = Math.max(0, current - 1);
		else if (event.key === 'Home') next = 0;
		else if (event.key === 'End') next = last;
		else if (event.key === 'Escape' && selected !== undefined) {
			event.preventDefault();
			select(undefined);
			return;
		}
		if (next === undefined) return;
		event.preventDefault();
		hovered = undefined;
		select(next);
	}
</script>

<svelte:window
	onpointerdown={(event) => {
		if (selected !== undefined && event.target instanceof Node && !root?.contains(event.target))
			select(undefined);
	}}
/>

<figure class="chart">
	<div
		class="box"
		bind:clientWidth={width}
		bind:clientHeight={boxHeight}
		style:min-block-size="{natural}px"
	>
		{#if points === undefined}
			<div class="fill"><Skeleton height="100%" radius="var(--r-md)" /></div>
		{:else if series.length === 0}
			<p class="fill few">{m.profile_trend_min_exams()}</p>
		{:else}
			<div
				class="plot fill"
				bind:this={root}
				role="slider"
				tabindex="0"
				aria-label={m.profile_chart_label({
					count: series.length,
					first: formatDate(series[0].date, 'medium'),
					last: formatDate(series[series.length - 1].date, 'medium')
				})}
				aria-orientation="horizontal"
				aria-valuemin={1}
				aria-valuemax={series.length}
				aria-valuenow={(selected ?? series.length - 1) + 1}
				aria-valuetext={describe(series[selected ?? series.length - 1])}
				onpointermove={(event) => {
					if (event.pointerType !== 'touch') hovered = nearestIndex(xs, localX(event));
				}}
				onpointerleave={() => (hovered = undefined)}
				onclick={(event) => {
					const index = nearestIndex(xs, localX(event), REACH);
					select(index === selected ? undefined : index);
					hovered = undefined;
				}}
				onfocus={() => {
					if (selected === undefined && root?.matches(':focus-visible')) select(series.length - 1);
				}}
				onblur={() => select(undefined)}
				onkeydown={keydown}
			>
				{#if width > 0}
					<svg {width} {height} viewBox="0 0 {width} {height}" aria-hidden="true">
						<defs>
							<linearGradient id="{id}-fill" x1="0" y1="0" x2="0" y2="1">
								<stop class="fill-top" offset="0" />
								<stop class="fill-bottom" offset="1" />
							</linearGradient>
						</defs>
						{#each bands as band, index (band.year)}
							{#if index % 2 === 1}
								<rect
									class="band"
									x={band.start}
									y={PAD.top - 8}
									width={band.end - band.start}
									height={baseline - PAD.top + 8}
								/>
							{/if}
							{#if band.end - band.start >= 56 || bands.length === 1}
								<text class="year" x={band.center} y={height - 8} text-anchor="middle">
									{academicYearLabel(band.year)}
								</text>
							{/if}
						{/each}
						{#each GRID as rule (rule.value)}
							<line
								class="grid"
								x1={PAD.left}
								x2={PAD.left + plotWidth}
								y1={yOf(rule.value)}
								y2={yOf(rule.value)}
							/>
							<text class="axis" x={PAD.left - 10} y={yOf(rule.value)} text-anchor="end">
								{rule.label}
							</text>
						{/each}
						{#if average !== null && average >= GRADE_MIN && average <= GRADE_MAX}
							<line
								class="average"
								x1={PAD.left}
								x2={PAD.left + plotWidth}
								y1={yOf(average)}
								y2={yOf(average)}
							/>
						{/if}
						<g class={['series', { draw }]}>
							<path class="area" d={area} fill="url(#{id}-fill)" />
							<path class="line" d={line} />
							{#each series as at, index (at.row.id)}
								<circle
									class={['dot', { laude: at.grade.cumLaude }]}
									cx={xs[index]}
									cy={ys[index]}
									r="3.5"
								/>
							{/each}
						</g>
						{#if shown !== undefined}
							<line class="cursor" x1={xs[shown]} x2={xs[shown]} y1={PAD.top - 8} y2={baseline} />
							<circle class="ring" cx={xs[shown]} cy={ys[shown]} r="6.5" />
							<circle class="hole" cx={xs[shown]} cy={ys[shown]} r="3" />
						{/if}
					</svg>
				{/if}
				{#if point && tip}
					<div
						class="tip"
						aria-hidden="true"
						bind:offsetWidth={tipWidth}
						bind:offsetHeight={tipHeight}
						style:translate="{tip.left}px {tip.top}px"
					>
						<p class="name">{italianTitleCase(point.row.name)}</p>
						<p class="date">{formatDate(point.date, 'long')}</p>
						<p class={['grade', { laude: point.grade.cumLaude }]}>
							{m.profile_grade_label({ grade: gradeText(point.grade, true) })}
						</p>
					</div>
				{/if}
			</div>
		{/if}
	</div>
	{#if series.length > 0}
		<!-- Tables ignore the 1 px clip of .sr-only: the wrapper takes it. -->
		<div class="sr-only">
			<table>
				<caption>{m.profile_chart_caption()}</caption>
				<thead>
					<tr>
						<th scope="col">{m.profile_chart_exam()}</th>
						<th scope="col">{m.profile_chart_date()}</th>
						<th scope="col">{m.profile_chart_grade()}</th>
					</tr>
				</thead>
				<tbody>
					{#each series as at (at.row.id)}
						<tr>
							<th scope="row">{italianTitleCase(at.row.name)}</th>
							<td>{formatDate(at.date, 'long')}</td>
							<td>{gradeText(at.grade, true)}</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	{/if}
	<!-- Its line is kept while the average loads, so the chart does not grow when it arrives. -->
	<figcaption class="legend">
		{#if average !== null}
			<span class="swatch" aria-hidden="true"></span>
			{m.profile_chart_average({ value: formatAverage(average) })}
		{/if}
	</figcaption>
</figure>

<style>
	/* Grows into a stretched card's body (a flex column): the box grows and the plot with it. */
	.chart {
		display: flex;
		flex: 1;
		flex-direction: column;
		gap: 10px;
		margin: 0;
	}

	.box {
		position: relative;
		flex: 1 0 auto;
	}

	/* Out of the flow, so the box's height comes from the layout and never from what it draws. */
	.fill {
		position: absolute;
		inset: 0;
	}

	.few {
		display: grid;
		place-items: center;
		padding: var(--sp-4);
		color: var(--text-muted);
		font: italic 400 1.0625rem / 1.4 var(--font-display);
		text-align: center;
		text-wrap: balance;
	}

	.plot {
		border-radius: var(--r-md);
		cursor: crosshair;
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
	}

	.plot:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 4px;
	}

	svg {
		display: block;
		overflow: visible;
	}

	.band {
		fill: color-mix(in oklch, var(--text) 3.5%, transparent);
	}

	.grid {
		stroke: var(--hairline-strong);
		stroke-width: 1;
	}

	text {
		fill: var(--text-subtle);
		font: 500 11px / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		dominant-baseline: central;
	}

	.year {
		dominant-baseline: auto;
		letter-spacing: 0.02em;
	}

	/* Gold is for honours (the 30L dots): the reference line stays neutral. */
	.average {
		stroke: var(--text-subtle);
		stroke-width: 1.25;
		stroke-dasharray: 4 4;
	}

	.fill-top {
		stop-color: var(--accent);
		stop-opacity: 0.2;
	}

	.fill-bottom {
		stop-color: var(--accent);
		stop-opacity: 0;
	}

	.line {
		fill: none;
		stroke: var(--accent);
		stroke-width: 2.5;
		stroke-linecap: round;
		stroke-linejoin: round;
	}

	.dot {
		fill: var(--accent);
	}

	.dot.laude {
		fill: var(--gold);
		stroke: var(--surface);
		stroke-width: 1.5;
	}

	.cursor {
		stroke: var(--border-strong);
		stroke-width: 1;
		stroke-dasharray: 2 3;
	}

	.ring {
		fill: var(--accent);
	}

	.hole {
		fill: var(--surface);
	}

	.draw {
		animation: draw 900ms var(--ease-out-expo) both;
	}

	@keyframes draw {
		from {
			clip-path: inset(-8px 100% -8px 0);
		}

		to {
			clip-path: inset(-8px -8px -8px 0);
		}
	}

	.tip {
		position: absolute;
		inset-block-start: 0;
		inset-inline-start: 0;
		z-index: 1;
		display: grid;
		gap: 2px;
		max-inline-size: min(240px, 100%);
		padding: 10px 12px;
		border-radius: var(--r-md);
		background: var(--surface-raised);
		box-shadow: var(--elev-3), var(--inner-light);
		pointer-events: none;
	}

	.name {
		display: -webkit-box;
		overflow: hidden;
		font: var(--type-ui-strong);
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
	}

	.date {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.grade {
		margin-block-start: 4px;
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}

	.grade.laude {
		color: var(--gold);
	}

	.legend {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		align-self: flex-end;
		min-block-size: 1lh;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.swatch {
		inline-size: 16px;
		border-block-start: 1.5px dashed var(--text-subtle);
	}

	@media (prefers-reduced-motion: reduce) {
		.draw {
			animation: none;
		}
	}
</style>
