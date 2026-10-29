<script lang="ts">
	import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
	import IconArmchair from '~icons/ph/armchair';
	import IconBookOpen from '~icons/ph/book-open';
	import IconCalendarX from '~icons/ph/calendar-x';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconClock from '~icons/ph/clock';
	import IconGraduationCap from '~icons/ph/graduation-cap';
	import IconMapPin from '~icons/ph/map-pin';
	import IconNavigationArrow from '~icons/ph/navigation-arrow';
	import IconTicket from '~icons/ph/ticket';
	import IconUser from '~icons/ph/user';
	import IconUsers from '~icons/ph/users';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { enrolledCountQuery } from '#lib/features/exams/queries.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import type { ResolvedPathname } from '$app/types';
	import type { Href, Icon } from '#lib/ui/types.ts';
	import { editionsFor } from '../courses.ts';
	import { dayMonth, detailTime, personName } from '../format.ts';
	import { campusMapLink, links, mapsLink } from '../links.ts';
	import { locationLine, type CalendarEvent } from '../model.ts';
	import { getTurnoPicker, hasTurnoPicker } from '../turni.svelte.ts';

	interface Props {
		event: CalendarEvent;
		/** The e-learning courses, for "Open course"; undefined while they load. */
		courses: readonly MoodleEnrolledCourse[] | undefined;
		/** The course picker replaces the details when a course has several editions. */
		page: 'event' | 'courses';
		/** Smaller actions, for the popover. */
		dense?: boolean;
		/** On a sunken surface (an unfolded agenda row): the rows step up a tone. */
		inset?: boolean;
	}

	let { event, courses, page = $bindable(), dense = false, inset = false }: Props = $props();

	interface Row {
		icon: Icon;
		label: string;
		value: string;
		/** The row opens this page: the place on the campus map. */
		href?: ResolvedPathname;
	}

	interface Action {
		label: string;
		icon: Icon;
		href?: Href;
		onclick?: () => void;
	}

	const picker = hasTurnoPicker() ? getTurnoPicker() : undefined;
	const turni = $derived(picker?.available(event.activityCode) ?? []);
	const turnoOptions = $derived([
		...turni.map((turno) => ({ value: String(turno), label: `T${turno}` })),
		{ value: 'all', label: m.calendar_turno_all() }
	]);

	const editions = $derived(
		courses && event.activityCode ? editionsFor(courses, event.activityCode) : []
	);

	/* The seat count is a slow Esse3 call: made only for an exam on screen. */
	const enrolled = query(() => {
		const scope = session.current;
		if (!scope || event.source !== 'exam' || event.bookingPosition === null) return undefined;
		const [courseOfStudyId, activityId, callId] = event.id.split('_').slice(1).map(Number);
		return enrolledCountQuery(scope, { courseOfStudyId, activityId, callId });
	});

	const rows: Row[] = $derived.by(() => {
		const list: Row[] = [
			{ icon: IconClock, label: m.calendar_detail_time(), value: detailTime(event) }
		];
		const place = locationLine(event);
		if (place) {
			list.push({
				icon: IconMapPin,
				label: m.calendar_detail_location(),
				value: place,
				href: campusMapLink(event.location) ?? undefined
			});
		}
		if (event.source === 'lesson' && event.teachers.length > 0) {
			list.push({
				icon: event.teachers.length > 1 ? IconUsers : IconUser,
				label:
					event.teachers.length > 1 ? m.calendar_detail_teachers() : m.calendar_detail_teacher(),
				value: event.teachers.map(personName).join(', ')
			});
		}
		if (event.source === 'exam') {
			const booking = bookingLine(event.bookingPosition, enrolled.data ?? null, event.bookedAt);
			if (booking)
				list.push({ icon: IconTicket, label: m.calendar_detail_booking(), value: booking });
			if (event.cancellableUntil) {
				list.push({
					icon: IconCalendarX,
					label: m.calendar_detail_cancellable(),
					value: m.calendar_detail_cancellable_until({ date: dayMonth(event.cancellableUntil) })
				});
			}
		}
		if (event.source === 'library' && event.seatName) {
			list.push({ icon: IconArmchair, label: m.calendar_detail_seat(), value: event.seatName });
		}
		if (event.source === 'deadline') {
			const course = courses?.find((entry) => entry.id === event.courseId);
			const name = course?.displayName?.trim() || course?.fullName;
			if (name) {
				list.push({ icon: IconGraduationCap, label: m.calendar_detail_course(), value: name });
			}
		}
		return list;
	});

	/** "Posizione 12 di 96 · prenotato il 18 ottobre", first letter capitalized. */
	function bookingLine(position: number | null, total: number | null, bookedAt: string | null) {
		const parts: string[] = [];
		if (position !== null) {
			parts.push(
				total !== null && total >= position
					? m.calendar_detail_position_of_total({ position, total })
					: m.calendar_detail_position({ position })
			);
		}
		if (bookedAt) parts.push(m.calendar_detail_booked_on({ date: dayMonth(bookedAt) }));
		const line = parts.join(' · ');
		return line ? line.charAt(0).toLocaleUpperCase() + line.slice(1) : null;
	}

	const openCourse: Action | null = $derived.by(() => {
		if (editions.length === 0) return null;
		if (editions.length === 1) {
			return {
				label: m.calendar_action_open_course(),
				icon: IconGraduationCap,
				href: links.course(editions[0].id)
			};
		}
		return {
			label: m.calendar_action_open_course(),
			icon: IconGraduationCap,
			onclick: () => (page = 'courses')
		};
	});

	const maps: Action | null = $derived.by(() => {
		const href = mapsLink(event.location);
		return href ? { label: m.calendar_action_maps(), icon: IconNavigationArrow, href } : null;
	});

	/** The one or two actions: [secondary, primary]. */
	const actions: [Action | null, Action | null] = $derived.by(() => {
		switch (event.source) {
			case 'lesson':
				return openCourse ? [maps, openCourse] : [null, maps];
			case 'exam':
				return [
					openCourse,
					{ label: m.calendar_action_go_to_booking(), icon: IconTicket, href: links.exam(event) }
				];
			case 'library':
				return [
					null,
					{ label: m.calendar_action_go_to_booking(), icon: IconTicket, href: links.library() }
				];
			case 'appointment':
				return [
					maps,
					{ label: m.calendar_action_go_to_booking(), icon: IconTicket, href: links.appointments() }
				];
			case 'deadline':
				return [
					null,
					{
						label: m.calendar_action_open_assignment(),
						icon: IconBookOpen,
						href: links.assignment(event.courseId, event.assignmentId)
					}
				];
		}
	});
</script>

{#if page === 'courses'}
	<ListGroup label={m.calendar_course_picker_title()}>
		{#each editions as edition (edition.id)}
			<ListRow
				title={edition.periodLabel ?? m.calendar_course_picker_transversal()}
				description={edition.name}
				meta={edition.hidden ? m.calendar_course_picker_hidden() : undefined}
				icon={IconBookOpen}
				href={links.course(edition.id)}
				lines={1}
			/>
		{/each}
	</ListGroup>
{:else}
	<div class={['detail', { inset }]}>
		<ul class="rows" role="list">
			{#each rows as row (row.label)}
				<li class="row">
					{#if row.href}
						<a class="link" href={row.href}>
							<IconChip icon={row.icon} size={dense ? 'sm' : 'md'} />
							<span class="text">
								<span class="label">{row.label}</span>
								<span class="value">{row.value}</span>
								<span class="sr-only">, {m.calendar_show_on_map()}</span>
							</span>
							<IconCaretRight class="go" aria-hidden="true" />
						</a>
					{:else}
						<IconChip icon={row.icon} size={dense ? 'sm' : 'md'} />
						<p class="text">
							<span class="label">{row.label}</span>
							<span class="value">{row.value}</span>
						</p>
					{/if}
				</li>
			{/each}
		</ul>
		{#if picker && event.activityCode && turni.length > 1}
			{@const code = event.activityCode}
			{@const chosen = picker.chosen(code)}
			<div class="turno">
				<p class="label" aria-hidden="true">{m.calendar_detail_turno()}</p>
				<SegmentedControl
					options={turnoOptions}
					value={typeof chosen === 'number' ? String(chosen) : 'all'}
					label={m.calendar_detail_turno()}
					full
					onchange={(value) => picker.choose(code, value === 'all' ? 'all' : Number(value))}
				/>
				<p class="hint">{m.calendar_turno_hint()}</p>
			</div>
		{/if}
		{#if actions[0] || actions[1]}
			<div class={['actions', { pair: actions[0] && actions[1] }]}>
				{#each actions as action, index (index)}
					{#if action}
						<Button
							variant={index === 1 ? 'solid' : 'tonal'}
							size={dense ? 'md' : 'lg'}
							icon={action.icon}
							href={action.href}
							onclick={action.onclick}
							full
						>
							{action.label}
						</Button>
					{/if}
				{/each}
			</div>
		{/if}
	</div>
{/if}

<style>
	.detail {
		display: grid;
		gap: var(--sp-5);
	}

	.rows {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	/* Rows sit a step above the sheet in both themes; their icon tiles a step above the rows. */
	.row {
		--row-bg: light-dark(var(--g3), var(--g4));
		--tile-bg: light-dark(var(--surface-raised), var(--g6));
		display: flex;
		align-items: center;
		gap: 14px;
		padding: 12px 14px;
		border-radius: var(--r-2xs);
		background: var(--row-bg);
	}

	.row:has(.link) {
		padding: 0;
	}

	.row :global(.chip.neutral) {
		--chip-bg: var(--tile-bg);
	}

	.row:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.row:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.inset .row {
		--row-bg: light-dark(var(--surface), var(--g4));
		--tile-bg: light-dark(var(--g3), var(--g6));
	}

	.link {
		display: flex;
		flex: 1;
		align-items: center;
		gap: 14px;
		min-inline-size: 0;
		padding: 12px 14px;
		border-radius: inherit;
		color: inherit;
		text-decoration: none;
	}

	@media (hover: hover) {
		.link:hover {
			background: var(--state-hover);
		}
	}

	.link:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.link .text {
		flex: 1;
	}

	.link :global(.go) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-muted);
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.value {
		font: var(--type-body);
		overflow-wrap: anywhere;
	}

	.turno {
		display: grid;
		gap: 8px;
	}

	.turno .label {
		padding-inline: 4px;
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.actions {
		display: grid;
		gap: 6px;
	}

	.pair {
		grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr);
	}
</style>
