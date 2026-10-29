<script lang="ts">
	import IconBooks from '~icons/ph/books';
	import IconChatCircleText from '~icons/ph/chat-circle-text';
	import IconFileText from '~icons/ph/file-text-duotone';
	import IconFilePdf from '~icons/ph/file-pdf';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { ExternalHref, Icon } from '#lib/ui/types.ts';
	import { getCourse } from '../../course.svelte.ts';
	import { buildSyllabus, courseMetadata, pickSyllabus } from '../../syllabus.ts';
	import TeacherHtml from '../TeacherHtml.svelte';
	import InfoPanel from './InfoPanel.svelte';
	import Programme from './Programme.svelte';
	import StaffList from './StaffList.svelte';

	const course = getCourse();
	const info = $derived(course.info);
	const record = $derived(course.record);

	const tab = $derived(info.data && pickSyllabus(info.data.syllabus, course.language));
	const syllabus = $derived(buildSyllabus(tab?.fields ?? [], courseMetadata(course.course)));

	const sections = $derived(
		[
			{ id: 'objectives', title: m.course_objectives(), html: syllabus.objectives },
			{ id: 'summary', title: m.course_summary(), html: syllabus.summary },
			{ id: 'programme', title: m.course_programme(), html: null },
			{ id: 'prerequisites', title: m.course_prerequisites(), html: syllabus.prerequisites },
			{ id: 'teaching', title: m.course_teaching_method(), html: syllabus.teachingMethod },
			{ id: 'material', title: m.course_reference_material(), html: syllabus.referenceMaterial },
			{ id: 'assessment', title: m.course_assessment(), html: syllabus.assessment },
			{ id: 'office-hours', title: m.course_office_hours(), html: syllabus.officeHours },
			{ id: 'goals', title: m.course_sustainable_goals(), html: syllabus.sustainableGoals }
		].filter(
			(section) =>
				section.html !== null ||
				(section.id === 'programme' &&
					(syllabus.programme.length > 0 || syllabus.programmeHtml !== null))
		)
	);

	const hasFacts = $derived(
		syllabus.credits !== null ||
			syllabus.hours !== null ||
			syllabus.language !== null ||
			syllabus.level !== null ||
			syllabus.semester !== null
	);

	interface CourseLink {
		id: string;
		title: string;
		icon: Icon;
		href: string | null | undefined;
	}

	const links = $derived(
		(
			[
				{
					id: 'pdf',
					title: m.course_link_syllabus_pdf(),
					icon: IconFilePdf,
					href: tab?.exportPdfUrl
				},
				{
					id: 'opinions',
					title: m.course_link_opinions(),
					icon: IconChatCircleText,
					href: info.data?.studentOpinionUrl
				},
				{
					id: 'library',
					title: m.course_link_library(),
					icon: IconBooks,
					href: info.data?.bibliographyUrl
				}
			] satisfies CourseLink[]
		).flatMap(({ href, ...link }) =>
			href && /^https?:\/\//.test(href) ? [{ ...link, href: href as ExternalHref }] : []
		)
	);

	const loading = $derived(info.data === undefined && info.status === 'pending');
	const gate = new LoadingGate(() => loading);
	const empty = $derived(
		info.data !== undefined &&
			record.status !== 'pending' &&
			!hasFacts &&
			sections.length === 0 &&
			course.staff.length === 0
	);
	const failed = $derived(info.status === 'error' && info.error !== undefined);
	const stale = $derived(info.error !== undefined && info.data !== undefined);
</script>

<svelte:head>
	<title>{course.title ?? m.course_loading()} · {m.app_name()}</title>
</svelte:head>

{#if stale && info.error}
	<div class="notice">
		<ErrorState
			variant="stale"
			message={errorMessage(info.error)}
			updatedAt={info.updatedAt ? new Date(info.updatedAt) : undefined}
			onretry={info.refresh}
			retrying={info.refreshing}
		/>
	</div>
{/if}

{#if failed && record.status === 'error' && info.error}
	<ErrorState
		title={m.course_overview_failed()}
		message={errorMessage(info.error)}
		onretry={() => {
			void info.refresh();
			void record.refresh();
		}}
		retrying={info.refreshing || record.refreshing}
	/>
{:else if empty}
	<EmptyState
		size="cover"
		icon={IconFileText}
		title={m.course_overview_empty_title()}
		body={m.course_overview_empty_body()}
	/>
{:else if loading}
	<!-- Until the course page arrives the overview is one placeholder: what the record already knows
	     would otherwise show first and be pushed down when the rest comes. -->
	<div class="overview" aria-busy="true" aria-label={m.course_loading()}>
		{#if gate.visible}
			<div class="facts">
				<!-- The information card's rows at their usual heights: heading, figures, facts, term. -->
				<div class="facts-skeleton">
					<Skeleton width="40%" height="16px" />
					{#each [90, 105] as height (height)}
						<div class="pair">
							<Skeleton height="{height}px" radius="var(--r-md)" />
							<Skeleton height="{height}px" radius="var(--r-md)" />
						</div>
					{/each}
					<Skeleton height="64px" radius="var(--r-md)" />
				</div>
			</div>
			<div class="main prose-skeleton">
				{#each [5, 3, 6] as lines, index (index)}
					<div class="block">
						<Skeleton width="30%" height="1.4em" />
						<Skeleton variant="text" {lines} />
					</div>
				{/each}
			</div>
		{/if}
	</div>
{:else}
	<div class="overview">
		{#if hasFacts}
			<div class="facts"><InfoPanel {syllabus} /></div>
		{/if}

		<div class="main">
			{#if failed && info.error}
				<ErrorState
					title={m.course_syllabus_failed()}
					message={errorMessage(info.error)}
					onretry={info.refresh}
					retrying={info.refreshing}
				/>
			{:else}
				{#each sections as section (section.id)}
					<section class="section" aria-labelledby="overview-{section.id}">
						<h2 class="heading" id="overview-{section.id}">{section.title}</h2>
						{#if section.id === 'programme' && syllabus.programme.length > 0}
							<Programme parts={syllabus.programme} />
						{:else if section.id === 'programme' && syllabus.programmeHtml}
							<TeacherHtml html={syllabus.programmeHtml} />
						{:else if section.html}
							<TeacherHtml html={section.html} />
						{/if}
					</section>
				{:else}
					{#if info.data !== undefined}
						<EmptyState
							icon={IconFileText}
							title={m.course_overview_empty_title()}
							body={m.course_overview_empty_body()}
						/>
					{/if}
				{/each}
			{/if}
		</div>

		{#if course.staff.length > 0 || links.length > 0}
			<div class="people">
				{#if course.staff.length > 0}
					<section class="section" aria-labelledby="overview-staff">
						<h2 class="heading" id="overview-staff">{m.course_staff()}</h2>
						<StaffList staff={course.staff} courseTitle={course.title} courseId={course.id} />
					</section>
				{/if}
				{#if links.length > 0}
					<ListGroup label={m.course_links_label()}>
						{#each links as link (link.id)}
							<ListRow title={link.title} icon={link.icon} href={link.href} />
						{/each}
					</ListGroup>
				{/if}
			</div>
		{/if}
	</div>
{/if}

<style>
	.notice {
		margin-block-end: var(--sp-5);
	}

	.overview {
		display: grid;
		grid-template-areas: 'facts' 'main' 'people';
		gap: var(--sp-7);
	}

	.facts {
		grid-area: facts;
	}

	.main {
		grid-area: main;
		display: grid;
		align-content: start;
		gap: var(--sp-7);
		min-inline-size: 0;
		max-inline-size: 36rem;
	}

	.people {
		grid-area: people;
		display: grid;
		align-content: start;
		gap: var(--sp-6);
		min-inline-size: 0;
	}

	/* The page's width decides, not the window's: beside the sidebar a 1024 px window is narrow. */
	@container page (width >= 880px) {
		.overview {
			grid-template-columns: minmax(0, 36rem) minmax(280px, 34%);
			grid-template-rows: auto 1fr;
			justify-content: space-between;
			grid-template-areas: 'main facts' 'main people';
			column-gap: var(--sp-9);
		}

		.people :global(.staff) {
			grid-template-columns: minmax(0, 1fr);
		}
	}

	.section {
		display: grid;
		gap: 14px;
		min-inline-size: 0;
	}

	.heading {
		padding-block-end: 10px;
		border-block-end: 1px solid var(--hairline);
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.facts-skeleton,
	.prose-skeleton,
	.block {
		display: grid;
		gap: 14px;
	}

	.facts-skeleton {
		gap: 20px;
		padding: 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.pair {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 12px;
	}

	.prose-skeleton {
		gap: var(--sp-7);
	}
</style>
