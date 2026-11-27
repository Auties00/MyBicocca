<script lang="ts" module>
	export type CourseTab = 'overview' | 'content' | 'quizzes' | 'assignments' | 'forums';

	const TABS: Readonly<Record<string, CourseTab>> = {
		'/(app)/elearning/[course]': 'overview',
		'/(app)/elearning/[course]/content': 'content',
		'/(app)/elearning/[course]/quizzes': 'quizzes',
		'/(app)/elearning/[course]/assignments': 'assignments',
		'/(app)/elearning/[course]/forums': 'forums'
	};

	function tabOf(routeId: string | null | undefined): CourseTab | undefined {
		return routeId == null ? undefined : TABS[routeId];
	}
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import { on } from 'svelte/events';
	import { afterNavigate, beforeNavigate } from '$app/navigation';
	import { page } from '$app/state';
	import { m } from '#lib/paraglide/messages.js';
	import { isStuck } from '#lib/shell/tabMemory.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PullToRefresh from '#lib/ui/PullToRefresh.svelte';
	import Tabs from '#lib/ui/Tabs.svelte';
	import { CourseState, setCourse } from '../course.svelte.ts';
	import { coursePath } from '../links.ts';
	import CourseHero from './CourseHero.svelte';

	interface Props {
		courseId: number;
		children: Snippet;
	}

	let { courseId, children }: Props = $props();

	const course = setCourse(new CourseState(() => courseId));
	const tab = $derived(tabOf(page.route.id));

	const items = $derived([
		{ value: 'overview' as const, label: m.course_tab_overview(), href: coursePath(courseId) },
		{
			value: 'content' as const,
			label: m.course_tab_content(),
			href: coursePath(courseId, 'content')
		},
		{
			value: 'quizzes' as const,
			label: m.course_tab_quizzes(),
			href: coursePath(courseId, 'quizzes')
		},
		{
			value: 'assignments' as const,
			label: m.course_tab_assignments(),
			href: coursePath(courseId, 'assignments')
		},
		{ value: 'forums' as const, label: m.course_tab_forums(), href: coursePath(courseId, 'forums') }
	]);

	let tabbar = $state<HTMLElement>();
	/* Phones fit all five tabs, as Android does: the cells share the width, the labels a size smaller. */
	let width = $state(0);
	const compact = $derived(width > 0 && width < 560);
	/* Between two tabs of one course only the panel moves; the cover stays put. */
	let switching = $state(false);
	let wasPinned = false;

	beforeNavigate(({ from, to }) => {
		const oldTab = tabOf(from?.route.id);
		const newTab = tabOf(to?.route.id);
		switching =
			oldTab !== undefined &&
			newTab !== undefined &&
			oldTab !== newTab &&
			from?.params?.course === to?.params?.course;
		wasPinned = switching && !!tabbar && isStuck(tabbar);
	});

	afterNavigate(({ type, to }) => {
		if (!wasPinned || type === 'popstate' || to?.url.hash || !tabbar) return;
		// A stuck bar reports its stuck offset, so measure from the cover above it.
		const style = getComputedStyle(tabbar);
		const stuck = parseFloat(style.top) || 0;
		const hero = tabbar.previousElementSibling?.getBoundingClientRect().bottom;
		if (hero !== undefined) {
			const rest = hero + (parseFloat(style.marginBlockStart) || 0);
			window.scrollTo({ top: Math.ceil(window.scrollY + rest - stuck), behavior: 'instant' });
		}
	});

	function swipeTabs(element: HTMLElement) {
		let start: { id: number; x: number; y: number } | undefined;
		let horizontal = false;

		const down = (event: PointerEvent) => {
			if (event.pointerType === 'mouse' || !event.isPrimary) return;
			// Leave screen-edge gestures and controls with their native behavior.
			if (event.clientX < 20 || event.clientX > window.innerWidth - 20) return;
			if (
				event.target instanceof Element &&
				event.target.closest('input, textarea, select, [contenteditable], audio, video')
			)
				return;
			start = { id: event.pointerId, x: event.clientX, y: event.clientY };
			horizontal = false;
		};
		const move = (event: PointerEvent) => {
			if (event.pointerId !== start?.id || horizontal) return;
			const x = Math.abs(event.clientX - start.x);
			const y = Math.abs(event.clientY - start.y);
			if (y > 12 && y >= x) start = undefined;
			else if (x > 12 && x > y * 1.3) {
				horizontal = true;
				element.setPointerCapture(event.pointerId);
			}
		};
		const release = (event: PointerEvent) => {
			if (event.pointerId !== start?.id) return;
			const dx = event.clientX - start.x;
			const dy = event.clientY - start.y;
			start = undefined;
			if (event.type !== 'pointerup' || !horizontal) return;
			const threshold = Math.min(96, Math.max(48, element.clientWidth * 0.15));
			if (Math.abs(dx) < threshold || Math.abs(dx) <= Math.abs(dy) * 1.3) return;
			const index = items.findIndex((item) => item.value === tab);
			const next = items[index + (dx < 0 ? 1 : -1)];
			if (index < 0 || !next) return;
			// Share the same focus, route and scroll handling as a tap on the tab.
			tabbar?.querySelector<HTMLAnchorElement>(`a[data-value="${next.value}"]`)?.click();
		};
		const off = [
			on(element, 'pointerdown', down),
			on(element, 'pointermove', move),
			on(element, 'pointerup', release),
			on(element, 'pointercancel', release),
			on(element, 'lostpointercapture', (event) => {
				if (event.target === element) release(event);
			})
		];
		return () => off.forEach((remove) => remove());
	}
</script>

{#if tab}
	<PullToRefresh onrefresh={() => course.refresh()}>
		<Page width="dashboard">
			<div
				class="course"
				data-course={course.slot}
				data-solid-topbar
				bind:clientWidth={width}
				{@attach swipeTabs}
			>
				<CourseHero {course} />
				<!-- A tab keeps focus: the panel changes under it. -->
				<div
					class="tabbar"
					bind:this={tabbar}
					data-sveltekit-reset="false"
					data-sticky-join
					data-course-subtabs
				>
					<Tabs
						{items}
						value={tab}
						label={m.course_sections_label()}
						fill={compact}
						dense={compact}
					/>
				</div>
				<div class="panel" data-vt-page={switching ? '' : undefined}>
					{@render children()}
				</div>
			</div>
		</Page>
	</PullToRefresh>
{:else}
	{@render children()}
{/if}

<style>
	.course {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		touch-action: pan-y pinch-zoom;
	}

	.tabbar {
		position: sticky;
		inset-block-start: var(--shell-inset-top);
		z-index: var(--z-sticky);
		margin-block-start: var(--sp-5);
		margin-inline: calc(-1 * var(--gutter));
		padding-inline: var(--gutter);
		background: var(--bg);
	}

	.panel {
		display: grid;
		align-content: start;
		padding-block-start: var(--sp-6);
		min-inline-size: 0;
		/* A short panel must still let the tab strip reach its sticky position. */
		min-block-size: max(0px, calc(100dvh - var(--shell-inset-top) - var(--control-h-lg)));
	}
</style>
