<script lang="ts">
	import type { Snippet } from 'svelte';
	import { beforeNavigate } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import IconGraduationCap from '~icons/ph/graduation-cap';
	import IconQrCode from '~icons/ph/qr-code';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader, { type HeaderSkeleton } from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { Href, Icon } from '#lib/ui/types.ts';
	import { checkInFlow } from '../checkInFlow.svelte.ts';
	import { checkInSubtitle, courseHeading } from '../labels.ts';
	import { attendanceQuery } from '../queries.ts';
	import { setAttendanceShell } from '../shell.ts';
	import CourseList from './CourseList.svelte';
	import { compact } from '#lib/ui/media.ts';

	interface Props {
		/** The open level: a course, or the check-in (also the default pane beside the list). */
		children: Snippet;
	}

	interface Header {
		title: string;
		eyebrow?: string;
		subtitle?: string;
		back?: { href: Href; label: string };
		/** `pending`: the course's own action, its place held until the course is known. */
		action?: { href: Href; label: string; icon: Icon; variant: 'solid' | 'tonal' } | 'pending';
		/** Bars for the lines not known yet. */
		loading?: boolean | HeaderSkeleton;
	}

	let { children }: Props = $props();

	const CHECK_IN = '/(app)/services/attendance/check-in';

	/* The courses beside a course or the check-in: where that needs it, the services column folds. */

	const courses = query(() => session.current && attendanceQuery(session.current));
	const flow = $derived(session.current && checkInFlow(session.current));
	let twoPanes = $state<boolean>();
	const wide = $derived(twoPanes === true);

	setAttendanceShell({
		get wide() {
			return wide;
		},
		courses,
		get flow() {
			return flow;
		}
	});

	const slug = $derived(page.params.course);
	const course = $derived(
		slug === undefined ? undefined : courses.data?.find((item) => item.slug === slug)
	);
	const heading = $derived(course && courseHeading(course));
	const pending = $derived(courses.data === undefined && courses.status === 'pending');

	/* Where one level fits, the page shows the list, a course or the check-in. */
	const level = $derived<'list' | 'course' | 'check-in'>(
		slug !== undefined ? 'course' : page.route.id === CHECK_IN ? 'check-in' : 'list'
	);
	const docked = $derived(compact.current && level === 'list');

	const listHref = resolve('services/attendance');
	const checkInHref = resolve('services/attendance/check-in');
	const courseHref = (slug: string) => resolve(`services/attendance/${slug}`);
	const toList = { href: listHref, label: m.attendance_title() };

	const count = $derived(
		courses.data !== undefined && courses.data.length > 0
			? m.attendance_to_attend({ count: courses.data.length })
			: pending
				? undefined
				: m.attendance_desc()
	);

	/*
	 * One header whatever the size, so the top bar's title and Back change in place rather than
	 * through a header leaving as another arrives. Beside the list it names the page and the pane
	 * names its level; alone, it names the level.
	 */
	const header = $derived.by((): Header => {
		if (wide || level === 'list') {
			const checkInOpen = wide && level !== 'course';
			return {
				title: m.attendance_title(),
				subtitle: count,
				back: { href: shellHref('services'), label: m.shell_services() },
				action:
					checkInOpen || compact.current
						? undefined
						: {
								href: checkInHref,
								label: m.attendance_check_in(),
								icon: IconQrCode,
								variant: 'solid'
							},
				loading: pending && { eyebrow: false }
			};
		}
		if (level === 'course') {
			return {
				title: heading?.title ?? (pending ? '' : m.attendance_course_label()),
				eyebrow: heading?.eyebrow,
				subtitle: heading?.teacher,
				back: toList,
				action: pending
					? 'pending'
					: course?.elearningId == null
						? undefined
						: {
								href: resolve(`elearning/${course.elearningId}`),
								label: m.attendance_open_elearning(),
								icon: IconGraduationCap,
								variant: 'tonal'
							},
				loading: pending
			};
		}
		return {
			title: m.attendance_check_in(),
			subtitle: flow ? checkInSubtitle(flow.phase) : undefined,
			back: toList
		};
	});

	const documentTitle = $derived(
		[
			slug !== undefined
				? (heading?.title ?? m.attendance_course_label())
				: level === 'check-in'
					? m.attendance_check_in()
					: undefined,
			m.attendance_title(),
			m.app_name()
		]
			.filter(Boolean)
			.join(' · ')
	);

	/* An outcome that has been seen is cleared on the way out, so coming back finds the form. */
	beforeNavigate((navigation) => {
		const ended = flow;
		if (
			ended?.phase.name === 'result' &&
			ended.noticed() &&
			navigation.to?.url.pathname !== page.url.pathname
		) {
			navigation.complete.then(
				() => ended.reset(),
				() => undefined
			);
		}
	});
</script>

<svelte:head>
	<title>{documentTitle}</title>
</svelte:head>

{#snippet headerAction()}
	{#if header.action === 'pending'}
		<Skeleton width="11.5rem" height="var(--control-h)" radius="var(--r-sm)" />
	{:else if header.action}
		<Button variant={header.action.variant} icon={header.action.icon} href={header.action.href}>
			{header.action.label}
		</Button>
	{/if}
{/snippet}

{#snippet list()}
	<div class={['list', { docked }]}>
		<CourseList {courses} selected={wide ? slug : undefined} dense={wide} />
	</div>
{/snippet}

<Page width="dashboard">
	<PageHeader
		title={header.title}
		eyebrow={header.eyebrow}
		subtitle={header.subtitle}
		back={header.back}
		actions={header.action ? headerAction : undefined}
		loading={header.loading}
	/>
	<!-- Beside the list the check-in is the default pane: the index route shows it there. -->
	<ListDetail
		{list}
		detail={level === 'list' ? undefined : children}
		placeholder={children}
		listWidth={320}
		detailMin={424}
		bind:twoPanes
		listLabel={m.attendance_courses_label()}
		current={slug === undefined ? undefined : courseHref(slug)}
		items={courses.data?.map((item) => courseHref(item.slug))}
		closeHref={listHref}
	/>
	{#if docked}
		<div class="dock">
			<Button variant="solid" size="lg" full icon={IconQrCode} href={checkInHref}>
				{m.attendance_check_in()}
			</Button>
		</div>
	{/if}
</Page>

<style>
	.list {
		max-inline-size: var(--w-reading);
	}

	/* The last rows scroll clear of the docked button. */
	.docked {
		padding-block-end: calc(var(--control-h-lg) + var(--sp-4));
	}

	/*
	 * Above the tab bar, where the thumb is: Android's footer button. Fixed rather than following
	 * the list, so it stands still while the list loads in.
	 */
	.dock {
		position: fixed;
		inset-inline: var(--gutter);
		inset-block-end: calc(var(--shell-inset-bottom) + var(--sp-3));
		z-index: var(--z-sticky);
		border-radius: var(--r-sm);
		box-shadow: var(--elev-2);
	}
</style>
