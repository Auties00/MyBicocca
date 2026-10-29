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
	import { afterNavigate, beforeNavigate } from '$app/navigation';
	import { page } from '$app/state';
	import { m } from '#lib/paraglide/messages.js';
	import Page from '#lib/ui/Page.svelte';
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

	beforeNavigate(({ from, to }) => {
		switching =
			tabOf(from?.route.id) !== undefined &&
			tabOf(to?.route.id) !== undefined &&
			from?.params?.course === to?.params?.course;
	});

	afterNavigate(({ type }) => {
		if (!switching || type === 'popstate' || tabbar === undefined) return;
		// A panel opens at its top, under the bar, whatever the scroll of the one before.
		const style = getComputedStyle(tabbar);
		const stuck = parseFloat(style.top) || 0;
		const hero = tabbar.previousElementSibling?.getBoundingClientRect().bottom;
		if (hero !== undefined && tabbar.getBoundingClientRect().top <= stuck + 1) {
			const rest = hero + (parseFloat(style.marginBlockStart) || 0);
			window.scrollTo({ top: window.scrollY + rest - stuck, behavior: 'instant' });
		}
	});
</script>

{#if tab}
	<Page width="dashboard">
		<div class="course" data-course={course.slot} bind:clientWidth={width}>
			<CourseHero {course} />
			<!-- A tab keeps focus: the panel changes under it. -->
			<div class="tabbar" bind:this={tabbar} data-sveltekit-reset="false">
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
{:else}
	{@render children()}
{/if}

<style>
	.course {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
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
		padding-block-start: var(--sp-6);
		min-inline-size: 0;
	}
</style>
