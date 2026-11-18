<script lang="ts">
	import { resolve } from '$app/paths';
	import IconCalendarX from '~icons/ph/calendar-x';
	import IconCalendarXDuotone from '~icons/ph/calendar-x-duotone';
	import IconGraduationCap from '~icons/ph/graduation-cap';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { courseHeading, percentText, statusLabel, statusTone } from '../labels.ts';
	import { minimumPercent, registerTitle, type Classroom, type Register } from '../model.ts';
	import { getAttendanceShell } from '../shell.ts';
	import Gauge from './Gauge.svelte';

	interface Props {
		slug: string;
	}

	let { slug }: Props = $props();

	const shell = getAttendanceShell();
	const courses = shell.courses;
	const course = $derived(courses.data?.find((item) => item.slug === slug));
	const heading = $derived(course && courseHeading(course));
	const id = $props.id();
	/* Beside the list the pane names the course (h2); alone, the page's title does (h1). */
	const level = $derived(shell.wide ? 3 : 2);
	const back = resolve('services/attendance');

	/*
	 * The source the course is summed up by leads with the large ring, as in the list: the
	 * classroom's record, else the first register that counted anything. The others follow as
	 * compact cards, side by side where there is room; a classroom without records closes them.
	 */
	const sources = $derived.by(() => {
		if (course === undefined) return undefined;
		const lead: Register | undefined = course.classroom
			? undefined
			: course.registers.find((register) => register.recordedPercentage !== null);
		return {
			lead,
			registers: course.registers.filter((register) => register !== lead),
			classroomEmpty: course.classroom === null && lead !== undefined
		};
	});

	function registerName(register: Register): string {
		return registerTitle(register.name) || m.attendance_tab_sessions();
	}

	function classroomLabel(classroom: Classroom, minimum: number | null): string {
		const parts = [`${m.attendance_caption_attendance()} ${percentText(classroom.percentage)}`];
		if (minimum !== null) parts.push(m.attendance_minimum({ percent: percentText(minimum) }));
		return parts.join(', ');
	}
</script>

{#snippet elearning(id: number)}
	<Button variant="tonal" size="sm" icon={IconGraduationCap} href={resolve(`elearning/${id}`)}>
		{m.attendance_open_elearning()}
	</Button>
{/snippet}

{#snippet sourceHead(kind: string, name: string, headingId: string)}
	<header class="source-head">
		<span class="eyebrow">{kind}</span>
		<svelte:element this={`h${level}`} id={headingId} class="source-title">{name}</svelte:element>
	</header>
{/snippet}

{#snippet classroomLead(classroom: Classroom)}
	{@const minimum = minimumPercent(classroom)}
	<section class="source lead" aria-labelledby="{id}-classroom">
		{@render sourceHead(
			m.attendance_source_classroom(),
			m.attendance_tab_lessons(),
			`${id}-classroom`
		)}
		<div class="lead-body">
			<Gauge
				percent={classroom.percentage}
				caption={m.attendance_caption_attendance()}
				tone={classroom.requirementProgress >= 100 ? 'success' : 'warning'}
				minimum={minimum ?? undefined}
				label={classroomLabel(classroom, minimum)}
			/>
			<div class="facts">
				<div class="standing">
					<StatusChip
						status={statusTone[classroom.status]}
						label={statusLabel[classroom.status]()}
					/>
					{#if minimum !== null}
						<span class="minimum">
							<svg class="minimum-glyph" viewBox="0 0 20 12" aria-hidden="true">
								<path d="M2 10.5A12 12 0 0 1 18 3.5" />
							</svg>
							{m.attendance_minimum({ percent: percentText(minimum) })}
						</span>
					{/if}
				</div>
				<dl class="stats">
					<div>
						<dt>{m.attendance_lessons_attended()}</dt>
						<dd>{formatNumber(classroom.lessonsAttended)}</dd>
					</div>
					<div>
						<dt>{m.attendance_hours_attended()}</dt>
						<dd>{formatNumber(classroom.hours)}</dd>
					</div>
				</dl>
			</div>
		</div>
	</section>
{/snippet}

{#snippet registerLead(register: Register, recorded: number)}
	<section class="source lead" aria-labelledby="{id}-{register.cmId}">
		{@render sourceHead(
			m.attendance_source_register(),
			registerName(register),
			`${id}-${register.cmId}`
		)}
		<div class="lead-body">
			<Gauge
				percent={recorded}
				caption={m.attendance_caption_recorded()}
				tone="neutral"
				label={m.attendance_recorded_percent({ percent: percentText(recorded) })}
			/>
			{#if register.attended !== null}
				<div class="facts">
					<p class="count">{m.attendance_presences({ count: register.attended })}</p>
				</div>
			{/if}
		</div>
	</section>
{/snippet}

{#snippet compact(
	kind: string,
	name: string,
	headingId: string,
	recorded: number | null,
	count: string | undefined,
	note: string | undefined
)}
	<section class="source compact" aria-labelledby={headingId}>
		{#if recorded !== null}
			<!-- The ring's caption sits under it, as inside the large one: the number is said once. -->
			<div class="ring">
				<Gauge
					size="small"
					percent={recorded}
					tone="neutral"
					label={m.attendance_recorded_percent({ percent: percentText(recorded) })}
				/>
				<span class="ring-caption" aria-hidden="true">{m.attendance_caption_recorded()}</span>
			</div>
		{:else}
			<span class="blank" aria-hidden="true"><IconCalendarX /></span>
		{/if}
		<div class="compact-text">
			{@render sourceHead(kind, name, headingId)}
			{#if count}<p class="count">{count}</p>{/if}
			{#if note}<p class="note">{note}</p>{/if}
		</div>
	</section>
{/snippet}

{#snippet registerCompact(register: Register)}
	{@const recorded = register.recordedPercentage}
	{@render compact(
		m.attendance_source_register(),
		registerName(register),
		`${id}-${register.cmId}`,
		recorded,
		recorded === null || register.attended === null
			? undefined
			: m.attendance_presences({ count: register.attended }),
		recorded === null ? m.attendance_register_no_records() : undefined
	)}
{/snippet}

{#snippet body()}
	<!-- Beside the list, the list carries the notice. -->
	{#if courses.error && courses.data && !shell.wide}
		<ErrorState
			variant="stale"
			message={errorMessage(courses.error)}
			updatedAt={courses.updatedAt ? new Date(courses.updatedAt) : undefined}
			onretry={courses.refresh}
			retrying={courses.refreshing}
		/>
	{/if}
	{#if course && sources}
		<div class="sources">
			{#if course.classroom}
				{@render classroomLead(course.classroom)}
			{:else if sources.lead && sources.lead.recordedPercentage !== null}
				{@render registerLead(sources.lead, sources.lead.recordedPercentage)}
			{:else}
				<section class="source" aria-label={m.attendance_source_classroom()}>
					<EmptyState
						size="panel"
						icon={IconCalendarXDuotone}
						title={m.attendance_no_classroom()}
						body={m.attendance_easystaff_no_records()}
					/>
				</section>
			{/if}
			{#if sources.registers.length > 0 || sources.classroomEmpty}
				<div class="others">
					{#each sources.registers as register (register.cmId)}
						{@render registerCompact(register)}
					{/each}
					{#if sources.classroomEmpty}
						{@render compact(
							m.attendance_source_classroom(),
							m.attendance_tab_lessons(),
							`${id}-classroom`,
							null,
							undefined,
							m.attendance_easystaff_no_records()
						)}
					{/if}
				</div>
			{/if}
		</div>
	{:else if courses.data !== undefined}
		<EmptyState
			size="page"
			icon={IconMagnifyingGlass}
			title={m.attendance_course_missing()}
			body={m.attendance_course_missing_body()}
		>
			{#snippet action()}
				<Button variant="tonal" href={back}>{m.attendance_back_to_list()}</Button>
			{/snippet}
		</EmptyState>
	{:else if courses.status === 'error' && courses.error}
		<ErrorState
			title={m.attendance_load_failed()}
			message={errorMessage(courses.error)}
			onretry={courses.refresh}
			retrying={courses.refreshing}
		/>
	{:else}
		<!-- The classroom's card in outline, so the one that loads lands where it stood. -->
		<div class="sources" role="status">
			<span class="sr-only">{m.attendance_loading()}</span>
			<div class="source lead" aria-hidden="true">
				<div class="source-head">
					<span class="eyebrow"><Skeleton variant="text" width="9em" /></span>
					<span class="source-title"><Skeleton variant="text" width="5em" /></span>
				</div>
				<div class="lead-body">
					<Skeleton variant="circle" width="var(--gauge-size, min(100%, 212px))" />
					<div class="facts">
						<div class="standing">
							<Skeleton width="7.5rem" height="28px" radius="var(--r-pill)" />
							<span class="minimum"><Skeleton variant="text" width="7em" /></span>
						</div>
						<div class="stats">
							{#each [0, 1] as cell (cell)}
								<div>
									<span class="stat-label"><Skeleton variant="text" /></span>
									<span class="stat-value"><Skeleton variant="text" /></span>
								</div>
							{/each}
						</div>
					</div>
				</div>
			</div>
		</div>
	{/if}
{/snippet}

<section class="course" aria-labelledby={shell.wide ? id : undefined}>
	<!-- The course's lines are bars until the list that names it arrives. -->
	<DetailHeader
		{id}
		title={heading?.title ?? (courses.data === undefined ? '' : m.attendance_course_label())}
		eyebrow={heading?.eyebrow || undefined}
		subtitle={heading?.teacher}
		loading={courses.data === undefined && courses.status === 'pending' && { actions: true }}
	>
		{#snippet actions()}
			{#if course?.elearningId != null}{@render elearning(course.elearningId)}{/if}
		{/snippet}
	</DetailHeader>
	{@render body()}
</section>

<style>
	.course {
		display: grid;
		gap: var(--sp-6);
	}

	.sources {
		display: grid;
		gap: var(--sp-4);
	}

	.others {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 280px), 1fr));
		gap: var(--sp-3);
	}

	.source {
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.lead {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5) var(--sp-5) var(--sp-6);
		container: source / inline-size;
	}

	.source-head {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.eyebrow {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.source-title {
		margin: 0;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.lead-body {
		display: grid;
		justify-items: center;
		gap: var(--sp-5);
	}

	.facts {
		display: grid;
		justify-items: center;
		gap: var(--sp-4);
		inline-size: 100%;
	}

	.standing {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: center;
		gap: var(--sp-2) var(--sp-4);
	}

	/*
	 * With room, the ring stands on the left and what it means fills the rest: the standing from
	 * the start, the stats across the whole column.
	 */
	@container source (width >= 480px) {
		.lead-body {
			--gauge-size: clamp(168px, 36cqi, 212px);
			grid-template-columns: auto minmax(0, 1fr);
			align-items: center;
			justify-items: stretch;
			gap: var(--sp-7);
			padding-inline-start: var(--sp-2);
		}

		.facts {
			justify-items: stretch;
		}

		.standing {
			justify-content: start;
		}
	}

	.minimum {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	/* The ring's outer arc in small: the minimum names it. */
	.minimum-glyph {
		inline-size: 20px;
		block-size: 12px;
		fill: none;
		stroke: currentColor;
		stroke-width: 2;
		stroke-linecap: round;
	}

	.stats {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		inline-size: 100%;
		margin: 0;
		border-block-start: 1px solid var(--hairline);
	}

	.stats > div {
		display: flex;
		flex-direction: column-reverse;
		align-items: center;
		gap: 2px;
		padding-block-start: var(--sp-4);
	}

	.stats > div + div {
		border-inline-start: 1px solid var(--hairline);
	}

	.stats dt,
	.stat-label {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-align: center;
	}

	.stats dd,
	.stat-value {
		margin: 0;
		font: var(--type-title-m);
		font-variant-numeric: tabular-nums;
		letter-spacing: var(--track-title-m);
	}

	.count {
		margin: 0;
		font: var(--type-ui-strong);
	}

	.compact {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: var(--sp-4);
		padding: var(--sp-4) var(--sp-5) var(--sp-4) var(--sp-4);
	}

	.ring {
		display: grid;
		justify-items: center;
		gap: var(--sp-1);
		inline-size: 88px;
	}

	.ring-caption {
		color: var(--text-muted);
		font: var(--type-label);
		text-align: center;
		text-wrap: balance;
	}

	.compact-text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.compact-text .source-head {
		margin-block-end: var(--sp-1);
	}

	.note {
		margin: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	/* A register with nothing counted keeps a ring's place, dashed like an empty one in the list. */
	.blank {
		display: grid;
		place-items: center;
		inline-size: 88px;
		block-size: 88px;
		border: 2px dashed var(--track);
		border-radius: 50%;
		color: var(--text-subtle);
	}

	.blank :global(svg) {
		inline-size: 26px;
		block-size: 26px;
	}

	/* The outline's stats, about as wide as what they stand for. */
	.stat-label {
		inline-size: 8em;
	}

	.stat-value {
		inline-size: 2.5em;
	}
</style>
