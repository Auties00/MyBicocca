<script lang="ts">
	import { afterNavigate, goto } from '$app/navigation';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconSkipBack from '~icons/ph/skip-back';
	import IconSkipForward from '~icons/ph/skip-forward';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Immersive from '#lib/ui/Immersive.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { getCourse } from '../../course.svelte.ts';
	import { coursePath } from '../../links.ts';
	import { continueWatching, progressRatio } from '../../video/progress.ts';
	import { formatPercent } from '#lib/format/numbers.ts';
	import Player from './Player.svelte';
	import Playlist from './Playlist.svelte';

	interface Props {
		cmId: number;
	}

	let { cmId }: Props = $props();

	const course = getCourse();
	const playlist = $derived(course.playlist);
	const index = $derived(playlist.findIndex((item) => item.cmId === cmId));
	const item = $derived(index >= 0 ? playlist[index] : undefined);
	const previous = $derived(index > 0 ? playlist[index - 1] : undefined);
	const next = $derived(
		index >= 0 && index < playlist.length - 1 ? playlist[index + 1] : undefined
	);
	const progress = $derived(course.progress.data ?? {});
	const saved = $derived(progress[cmId]);
	const lastSeen = $derived(continueWatching(progress, course.id)?.cmId);
	const title = $derived(item?.title ?? saved?.title ?? m.course_video_fallback_title());
	const section = $derived(
		item
			? (item.sectionTitle ?? m.course_section_fallback({ number: item.sectionOrdinal }))
			: saved?.sectionTitle
	);
	const watched = $derived(item?.completed === true || saved?.completed === true);

	const hrefOf = (target: number) => coursePath(course.id, 'video', target);
	const play = (target: { cmId: number } | undefined) =>
		target && goto(hrefOf(target.cmId), { replace: true });

	/* Moving between recordings replaces the entry, so one Back leaves the player: to where it was
	   opened from, or to the course when it was opened on its own. */
	let fromApp = false;
	afterNavigate(({ from, type }) => {
		fromApp ||= type !== 'enter' && from !== null && !from.url.pathname.includes('/video/');
	});

	function back() {
		if (fromApp) history.back();
		else void goto(coursePath(course.id, 'content'), { replace: true });
	}
</script>

<svelte:head>
	<title>{title} · {course.title ?? m.app_name()}</title>
</svelte:head>

<Immersive />

<div class="watch" data-course={course.slot}>
	<header class="bar">
		<IconButton icon={IconArrowLeft} label={m.course_video_back_to_course()} onclick={back} />
		<div class="titles">
			<p class="course-title">{course.title ?? ''}</p>
			<p class="now">{title}</p>
		</div>
	</header>

	<div class="layout">
		<div class="primary">
			<div class="player">
				<Player
					courseId={course.id}
					{cmId}
					{title}
					sectionTitle={section ?? null}
					courseTitle={course.title ?? ''}
					manual={item?.manual === true}
					completed={item?.completed === true}
					{saved}
					ready={course.progress.data !== undefined}
					onprevious={previous ? () => play(previous) : undefined}
					onnext={next ? () => play(next) : undefined}
					onended={() => play(next)}
				/>
			</div>
			<div class="details">
				{#if section}<p class="section">{section}</p>{/if}
				<h1 class="title">{title}</h1>
				<p class="status">
					{#if watched}
						{m.course_module_watched()}
					{:else if saved && progressRatio(saved) > 0.01}
						{m.course_module_watched_percent({ percent: formatPercent(progressRatio(saved)) })}
					{/if}
				</p>
				<div class="steps">
					<Button
						variant="tonal"
						icon={IconSkipBack}
						disabled={previous === undefined}
						href={previous ? hrefOf(previous.cmId) : undefined}
						data-sveltekit-replacestate
						label={previous ? m.course_video_previous_named({ title: previous.title }) : undefined}
					>
						{m.course_video_previous()}
					</Button>
					<Button
						variant="tonal"
						trailingIcon={IconSkipForward}
						disabled={next === undefined}
						href={next ? hrefOf(next.cmId) : undefined}
						data-sveltekit-replacestate
						label={next ? m.course_video_next_named({ title: next.title }) : undefined}
					>
						{m.course_video_next()}
					</Button>
				</div>
			</div>
		</div>
		<aside class="list">
			{#if playlist.length > 0}
				<Playlist items={playlist} courseId={course.id} current={cmId} {progress} {lastSeen} />
			{:else if course.contents.data === undefined}
				<div class="list-skeleton" aria-hidden="true">
					{#each { length: 5 }, row (row)}
						<Skeleton height="52px" radius="var(--r-md)" />
					{/each}
				</div>
			{/if}
		</aside>
	</div>
</div>

<style>
	.watch {
		display: grid;
		grid-template-rows: auto minmax(0, 1fr);
		min-block-size: 100dvh;
		background: var(--bg);
		container-type: inline-size;
	}

	.bar {
		position: sticky;
		inset-block-start: 0;
		z-index: var(--z-sticky);
		display: flex;
		align-items: center;
		gap: 8px;
		min-block-size: calc(var(--topbar-h) + var(--safe-top));
		padding: var(--safe-top) 16px 0 8px;
		background: var(--surface);
		box-shadow: 0 1px 0 var(--hairline);
	}

	.titles {
		display: grid;
		min-inline-size: 0;
	}

	.course-title {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.now {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.layout {
		display: grid;
		align-content: start;
		gap: var(--sp-6);
		inline-size: 100%;
		max-inline-size: 1680px;
		margin-inline: auto;
		padding-block-end: var(--sp-8);
	}

	.primary {
		display: grid;
		align-content: start;
		gap: var(--sp-5);
		min-inline-size: 0;
	}

	.player {
		position: sticky;
		inset-block-start: calc(var(--topbar-h) + var(--safe-top));
		z-index: var(--z-raised);
		background: oklch(0% 0 0);
	}

	.details {
		display: grid;
		gap: 6px;
		padding-inline: var(--gutter);
	}

	.section {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.title {
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
		text-wrap: balance;
	}

	.status {
		min-block-size: 1lh;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.steps {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
		margin-block-start: 8px;
	}

	/* Touch players carry previous and next on the video itself. */
	@media (pointer: coarse) {
		.steps {
			display: none;
		}
	}

	.list {
		min-inline-size: 0;
		padding-inline: calc(var(--gutter) - 8px);
	}

	.list-skeleton {
		display: grid;
		gap: 6px;
		padding: 40px 8px 0;
	}

	@media (width >= 600px) {
		.layout {
			padding: var(--sp-6) var(--sp-6) var(--sp-8);
		}

		.player {
			position: static;
			overflow: hidden;
			border-radius: var(--r-lg);
			box-shadow: var(--elev-2);
		}

		.details {
			padding-inline: 4px;
		}

		.list {
			padding-inline: 0;
		}
	}

	@container (width >= 1000px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) clamp(320px, 28cqi, 400px);
			align-items: start;
		}

		.list {
			position: sticky;
			inset-block-start: calc(var(--topbar-h) + var(--sp-6));
			display: grid;
			max-block-size: calc(100dvh - var(--topbar-h) - var(--sp-6) * 2);
			padding: 16px 8px 8px;
			border-radius: var(--r-lg);
			background: var(--surface);
			box-shadow: var(--elev-1);
		}
	}
</style>
