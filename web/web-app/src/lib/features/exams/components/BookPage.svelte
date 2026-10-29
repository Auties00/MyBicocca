<script lang="ts">
	import { afterNavigate, goto } from '$app/navigation';
	import { page } from '$app/state';
	import IconCalendarBlank from '~icons/ph/calendar-blank-duotone';
	import IconCalendarCheck from '~icons/ph/calendar-check-duotone';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconCalendarDots from '~icons/ph/calendar-dots';
	import IconListBullets from '~icons/ph/list-bullets';
	import IconX from '~icons/ph/x';
	import { now } from '#lib/data/clock.svelte.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import { toIsoDate } from '#lib/format/days.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import Chip from '#lib/ui/Chip.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SearchField from '#lib/ui/SearchField.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { SegmentedOption } from '#lib/ui/types.ts';
	import { callKindLabel, longWhen } from '../format.ts';
	import { bookHref, bookingHref, type BookView } from '../links.ts';
	import {
		groupCalls,
		matchesCourse,
		splitBookings,
		toBooking,
		toExamCall,
		type Booking,
		type ExamCall
	} from '../model.ts';
	import { outcomes } from '../outcomes.svelte.ts';
	import { bookingsQuery, examCallsQuery } from '../queries.ts';
	import BookedPanel from './BookedPanel.svelte';
	import CallDetail from './CallDetail.svelte';
	import CallList from './CallList.svelte';
	import CallDetailSkeleton from './CallDetailSkeleton.svelte';
	import CallListSkeleton from './CallListSkeleton.svelte';
	import CallsOverview from './CallsOverview.svelte';
	import CallsOverviewSkeleton from './CallsOverviewSkeleton.svelte';

	/** From this many courses on, the list gets a search field. */
	const SEARCH_FROM = 4;
	/* The calendar's month (300) and agenda (280) side by side, with the gap between them. */
	const ROOMY = 600;

	const calls = query(() => session.current && examCallsQuery(session.current));
	const bookings = query(() => session.current && bookingsQuery(session.current));

	let search = $state('');
	let twoPanes = $state<boolean>();
	let roomy = $state(false);
	const wide = $derived(twoPanes === true);
	/* The calendar's picked day, kept while a call covers it. */
	let calendarDay = $state<string>();

	const all = $derived(
		(calls.data ?? []).map(toExamCall).filter((call): call is ExamCall => call !== undefined)
	);
	const groups = $derived(groupCalls(all, getLocale()));
	const upcoming = $derived(
		splitBookings(
			(bookings.data ?? []).map(toBooking).filter((b): b is Booking => b !== undefined),
			now.current
		).upcoming
	);

	const course = $derived(page.url.searchParams.get('course'));
	const slug = $derived(page.url.searchParams.get('call'));
	const view = $derived<BookView>(
		page.url.searchParams.get('view') === 'calendar' ? 'calendar' : 'list'
	);
	const focused = $derived(course ? groups.find((group) => group.key === course) : undefined);
	const shown = $derived(
		(course ? groups.filter((group) => group.key === course) : groups).filter((group) =>
			matchesCourse(group, search)
		)
	);
	const order = $derived(shown.flatMap((group) => group.calls));
	/* While loading, whether the search field will come: so its placeholder holds its place. */
	const searchable = $derived(
		!course && (calls.data === undefined || groups.length >= SEARCH_FROM)
	);
	const shownBookings = $derived.by(() => {
		const activities = new Set(order.map((item) => item.key.activityId));
		return upcoming.filter((booking) => activities.has(booking.key.activityId));
	});

	const call = $derived(slug ? all.find((item) => item.slug === slug) : undefined);
	const booked = $derived(
		slug && session.current ? outcomes.booked(session.current, slug) : undefined
	);
	const alreadyBooked = $derived(
		slug && !call && !booked ? upcoming.find((item) => item.slug === slug) : undefined
	);

	/*
	 * The bookings sit among their course's calls: the list waits for them too, rather than
	 * growing rows under the reader when they come. Failing, they leave the calls alone.
	 */
	const gate = new LoadingGate(() => calls.status === 'pending' || bookings.status === 'pending');
	const loading = $derived(
		calls.data === undefined || bookings.status === 'pending' || gate.visible
	);
	const updatedAt = $derived(calls.updatedAt ? new Date(calls.updatedAt) : undefined);

	const subtitle = $derived.by(() => {
		if (calls.data === undefined) return undefined;
		if (course) {
			return focused
				? m.exams_calls_count({ count: focused.calls.length })
				: m.exams_no_calls_short();
		}
		return all.length === 0
			? m.exams_no_calls_short()
			: `${m.exams_calls_count({ count: all.length })} · ${m.exams_exams_count({ count: groups.length })}`;
	});

	/* On one pane an open call is the page: its name is the title and Back returns to the list. */
	const single = $derived(!wide && slug !== null);
	/*
	 * Where the calendar can't sit beside the list but a pane has room for it, the page offers it
	 * in the list's place. Two panes show both, so there is nothing to choose.
	 */
	const switchable = $derived(!wide && roomy);
	const calendar = $derived(switchable && view === 'calendar');
	const views = $derived<SegmentedOption<BookView>[]>([
		{ value: 'list', label: m.exams_view_list(), icon: IconListBullets },
		{ value: 'calendar', label: m.exams_view_calendar(), icon: IconCalendarDots }
	]);
	const headerCall = $derived(booked ?? call);
	const pending = $derived(calls.data === undefined && calls.status === 'pending');
	const title = $derived.by(() => {
		if (!single) return m.exams_book_title();
		if (headerCall) return headerCall.title || m.exams_call_unknown();
		return pending ? '' : m.exams_book_title();
	});
	const headerSubtitle = $derived(
		single && headerCall
			? [callKindLabel(headerCall.kind, headerCall.callType), headerCall.code]
					.filter(Boolean)
					.join(' · ')
			: subtitle
	);
	const back = $derived(
		single
			? { href: bookHref({ course, view }), label: m.exams_book_title() }
			: { href: shellHref('services/exams'), label: m.exams_title() }
	);

	const href = (item: ExamCall) => bookHref({ course, call: item.slug, view });

	/* The calendar shows the day of the call last opened, once the call closes. */
	afterNavigate(() => {
		if (call?.date) calendarDay = toIsoDate(call.date);
	});

	function show(next: BookView) {
		void goto(bookHref({ course, view: next }), { replace: true, reset: false });
	}

	/** Whether a pane has room for the calendar, its month beside its agenda. */
	function measure(node: HTMLElement) {
		const observer = new ResizeObserver(([entry]) => (roomy = entry.contentRect.width >= ROOMY));
		observer.observe(node);
		return () => observer.disconnect();
	}
</script>

<svelte:head>
	<title>{title || m.exams_book_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard" focusOnQuery>
	<PageHeader
		{title}
		subtitle={headerSubtitle}
		{back}
		loading={pending && { eyebrow: false, title: single ? 2 : 1 }}
		meta={!single && (switchable || course) ? controls : undefined}
	/>

	{#if calls.error && calls.data}
		<ErrorState
			variant="stale"
			message={errorMessage(calls.error)}
			{updatedAt}
			onretry={calls.refresh}
			retrying={calls.refreshing}
		/>
	{/if}

	{#if calls.status === 'error' && calls.error && !calls.data}
		<ErrorState
			title={m.exams_calls_failed()}
			message={errorMessage(calls.error)}
			onretry={calls.refresh}
			retrying={calls.refreshing}
		/>
	{:else if !loading && all.length === 0 && !booked}
		<EmptyState
			size="page"
			icon={IconCalendarBlank}
			title={m.exams_no_calls_title()}
			body={m.exams_no_calls_body()}
		>
			{#snippet action()}
				<Button href={shellHref('services/exams')}>{m.exams_back_to_bookings()}</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<div {@attach measure}>
			<ListDetail
				listWidth={320}
				detailMin={424}
				bind:twoPanes
				listLabel={calendar ? m.exams_overview_title() : m.exams_calls_label()}
				detailLabel={slug !== null ? m.exams_call_label() : m.exams_overview_title()}
				detail={slug !== null ? detail : undefined}
				current={slug === null ? undefined : bookHref({ course, call: slug, view })}
				items={order.map(href)}
				closeHref={bookHref({ course, view })}
			>
				{#snippet list()}
					<!-- Beside the detail, picking a call keeps the list where it was. -->
					<div data-sveltekit-reset={wide ? 'false' : undefined}>
						{#if loading}
							<div
								class="loading"
								aria-busy="true"
								aria-label={calendar ? m.exams_overview_title() : m.exams_calls_label()}
							>
								{#if gate.visible && calendar}
									<div class="list" aria-hidden="true">
										{#if searchable}<Skeleton height="44px" radius="var(--r-pill)" />{/if}
										<CallsOverviewSkeleton />
									</div>
								{:else if gate.visible}
									<CallListSkeleton search={searchable} />
								{/if}
							</div>
						{:else}
							<div class="list">
								{#if groups.length >= SEARCH_FROM && !course}
									<SearchField
										label={m.exams_filter_label()}
										placeholder={m.exams_filter_placeholder()}
										bind:value={search}
									/>
								{/if}
								{#if shown.length > 0 && calendar}
									<CallsOverview
										calls={order}
										booked={shownBookings}
										now={now.current}
										{href}
										bind:picked={calendarDay}
									/>
								{:else if shown.length > 0}
									<CallList
										groups={shown}
										booked={upcoming}
										selected={slug ?? undefined}
										{href}
										now={now.current}
									/>
								{:else if course && !focused}
									<EmptyState
										icon={IconCalendarX}
										title={m.exams_no_course_calls_title({ course })}
										body={m.exams_no_calls_body()}
									>
										{#snippet action()}
											<Button href={bookHref({ view })}>{m.exams_show_all()}</Button>
										{/snippet}
									</EmptyState>
								{:else}
									<EmptyState title={m.exams_no_match({ query: search.trim() })} />
								{/if}
							</div>
						{/if}
					</div>
				{/snippet}
				{#snippet placeholder()}
					{#if loading}
						<div class="loading" aria-busy="true" aria-label={m.exams_overview_title()}>
							{#if gate.visible}<CallsOverviewSkeleton />{/if}
						</div>
					{:else}
						<CallsOverview
							calls={order}
							booked={shownBookings}
							now={now.current}
							{href}
							bind:picked={calendarDay}
						/>
					{/if}
				{/snippet}
			</ListDetail>
		</div>
	{/if}
</Page>

{#snippet controls()}
	<div class="controls">
		{#if switchable}
			<SegmentedControl label={m.exams_view_label()} value={view} options={views} onchange={show} />
		{/if}
		{#if course}
			<Chip trailingIcon={IconX} href={bookHref({ view })}>
				{m.exams_only_course({ course: focused?.title || course })}
				<span class="sr-only">· {m.exams_show_all()}</span>
			</Chip>
		{/if}
	</div>
{/snippet}

{#snippet detail()}
	{#if loading}
		<div class="loading call" aria-busy="true" aria-label={m.exams_call_label()}>
			<DetailHeader title="" loading />
			{#if gate.visible}<CallDetailSkeleton />{/if}
		</div>
	{:else if headerCall && session.current}
		<div class="call">
			<DetailHeader
				eyebrow={headerCall.code ?? undefined}
				eyebrowCode
				title={headerCall.title || m.exams_call_unknown()}
				subtitle={callKindLabel(headerCall.kind, headerCall.callType)}
			/>
			{#if booked}
				<BookedPanel call={booked} level={wide ? 3 : 2} />
			{:else if call}
				{#key call.slug}
					<CallDetail scope={session.current} {call} now={now.current} />
				{/key}
			{/if}
		</div>
	{:else if alreadyBooked}
		<EmptyState
			icon={IconCalendarCheck}
			title={m.exams_already_booked()}
			body={alreadyBooked.at ? longWhen(alreadyBooked.at, alreadyBooked.timed) : undefined}
		>
			{#snippet action()}
				<Button variant="solid" href={bookingHref(alreadyBooked.slug)}>
					{m.exams_view_booking()}
				</Button>
			{/snippet}
		</EmptyState>
	{:else}
		<EmptyState
			icon={IconCalendarX}
			title={m.exams_call_missing_title()}
			body={m.exams_call_missing_body()}
		>
			{#snippet action()}
				<Button href={bookHref({ course })}>{m.exams_all_calls()}</Button>
			{/snippet}
		</EmptyState>
	{/if}
{/snippet}

<style>
	.controls {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-2) var(--sp-3);
	}

	.list {
		display: grid;
		gap: var(--sp-4);
	}

	.loading {
		min-block-size: 480px;
	}

	.call {
		display: grid;
		gap: var(--sp-5);
		align-content: start;
		max-inline-size: var(--w-reading);
	}
</style>
