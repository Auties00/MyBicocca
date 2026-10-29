<script lang="ts">
	import type { MoodleCourseModule } from '@mybicocca/data-api/elearning';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconCheckBold from '~icons/ph/check-bold';
	import IconCopy from '~icons/ph/copy';
	import IconDotsThreeVertical from '~icons/ph/dots-three-vertical';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconLockSimple from '~icons/ph/lock-simple';
	import IconSquaresFour from '~icons/ph/squares-four';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { shapePath } from '#lib/features/elearning/shapes.ts';
	import { htmlToLine, isRenderableHtml } from '#lib/format/html.ts';
	import { formatPercent } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Menu, { type MenuEntry } from '#lib/ui/Menu.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import {
		displayName,
		family,
		isCompleted,
		isLocked,
		moduleTarget,
		type CourseFile
	} from '../../content.ts';
	import { getFileOpener } from '../../file/opener.svelte.ts';
	import { coursePath, moduleFileHref } from '../../links.ts';
	import { progressRatio, type VideoProgress } from '../../video/progress.ts';
	import { moduleIcon } from '../icons.ts';
	import TeacherHtml from '../TeacherHtml.svelte';
	import WavyProgress from '../WavyProgress.svelte';
	import { moduleMeta } from './moduleMeta.ts';

	interface Props {
		module: MoodleCourseModule;
		courseId: number;
		language: string;
		progress: VideoProgress | undefined;
		/** The recording watched last and not finished: its bar waves. */
		lastSeen: boolean;
		now: Date;
	}

	let { module, courseId, language, progress, lastSeen, now }: Props = $props();

	const cookiePath = shapePath('cookie');
	const opener = getFileOpener();
	const name = $derived(displayName(module.name, language));
	const target = $derived(moduleTarget(module, language));
	const locked = $derived(isLocked(module));
	const done = $derived(isCompleted(module) || progress?.completed === true);
	const Glyph = $derived(moduleIcon(module));
	const ratio = $derived(progressRatio(progress));
	const restriction = $derived(
		module.availabilityInfo ? htmlToLine(module.availabilityInfo) : null
	);
	const file = $derived<CourseFile | null>(target.kind === 'file' ? target.file : null);
	const downloading = $derived(file !== null && opener.downloading.has(file.url));
	const failure = $derived(
		file !== null && opener.failed?.url === file.url ? opener.failed.error : null
	);

	const href = $derived.by((): ResolvedPathname | undefined => {
		switch (target.kind) {
			case 'quiz':
				return coursePath(courseId, 'quiz', target.id);
			case 'assign':
				return coursePath(courseId, 'assignment', target.id);
			case 'forum':
				return coursePath(courseId, 'forum', target.id);
			case 'video':
				return coursePath(courseId, 'video', target.cmId);
			case 'file':
				return moduleFileHref(courseId, module.id, target.file);
			case 'folder':
				return `${coursePath(courseId, 'content')}?folder=${module.id}` as ResolvedPathname;
			default:
				return undefined;
		}
	});
	const external = $derived(
		target.kind === 'link' || target.kind === 'external' ? target.url : undefined
	);

	const menu = $derived.by((): MenuEntry[] => {
		if (file !== null) {
			const request = { file, href };
			return [
				{
					label: m.course_open_with(),
					icon: IconSquaresFour,
					onselect: () => opener.open(request, true)
				},
				{
					label: m.course_download(),
					icon: IconDownloadSimple,
					onselect: () => void opener.download(file)
				}
			];
		}
		if (external !== undefined) {
			const url = external;
			return [{ label: m.course_copy_link(), icon: IconCopy, onselect: () => void copy(url) }];
		}
		return [];
	});

	async function copy(url: string) {
		try {
			await navigator.clipboard.writeText(url);
			toast.success(m.course_link_copied());
		} catch {
			toast.show({ message: m.course_link_copy_failed(), tone: 'danger' });
		}
	}

	function click(event: MouseEvent) {
		if (file !== null && href !== undefined) opener.click(event, { file, href });
	}
</script>

<li
	id="cm-{module.id}"
	class={['row', `family-${family(module)}`, { locked, done }]}
	style:--indent={Math.min(module.indent, 4)}
>
	<span class="badge" aria-hidden="true">
		{#if downloading}
			<Spinner size={20} />
		{:else if locked}
			<IconLockSimple />
		{:else if done}
			<svg class="cookie" viewBox="0 0 100 100"><path d={cookiePath} /></svg>
			<IconCheckBold />
		{:else}
			<Glyph />
		{/if}
	</span>
	<div class="text">
		{#if external !== undefined}
			<a class="name" href={external} target="_blank" rel="noopener noreferrer external">
				{name}<IconArrowSquareOut class="out" aria-label={m.course_opens_new_tab()} />
			</a>
		{:else if href !== undefined}
			<a
				class="name"
				{href}
				data-sveltekit-reset={target.kind === 'folder' ? 'false' : undefined}
				onclick={click}
			>
				{name}
			</a>
		{:else}
			<span class="name">{name}</span>
		{/if}
		<p class="meta">
			{#if done}<span class="sr-only">{m.course_completed()}. </span>{/if}
			{#if restriction}
				<span class="restriction">{restriction}</span>
			{:else if failure}
				<span class="failure" role="status"
					>{m.course_download_failed()} · {errorMessage(failure)}</span
				>
			{:else}
				{moduleMeta(module, progress, language, now)}
			{/if}
		</p>
		{#if !locked && isRenderableHtml(module.description) && module.modName !== 'label'}
			<div class="description">
				<TeacherHtml html={module.description ?? ''} size="sm" lines={3} />
			</div>
		{/if}
		{#if progress && !progress.completed && ratio > 0.01}
			<div class="watch">
				<WavyProgress
					value={ratio}
					wavy={lastSeen}
					label={m.course_module_watched_percent({ percent: formatPercent(ratio) })}
				/>
			</div>
		{/if}
	</div>
	{#if menu.length > 0}
		<span class="more">
			<Menu items={menu} label={m.course_more_actions({ name })} placement="bottom-end">
				{#snippet trigger(props)}
					<IconButton
						{...props}
						icon={IconDotsThreeVertical}
						label={m.course_more_actions({ name })}
						size="sm"
					/>
				{/snippet}
			</Menu>
		</span>
	{/if}
</li>

<style>
	.row {
		position: relative;
		display: grid;
		grid-template-columns: 40px minmax(0, 1fr) auto;
		align-items: start;
		gap: 4px 14px;
		padding: 12px 8px 12px calc(14px + var(--indent) * 14px);
		border-radius: var(--r-2xs);
		background: var(--row-bg);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.row:first-child {
		border-start-start-radius: var(--r-md);
		border-start-end-radius: var(--r-md);
	}

	.row:last-child {
		border-end-start-radius: var(--r-md);
		border-end-end-radius: var(--r-md);
	}

	@media (hover: hover) {
		.row:has(.name:is(a):hover) {
			background: var(--row-hover);
			transition-duration: 0s;
		}
	}

	.row:has(.name:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.row:target {
		animation: flash 1.6s var(--ease-out) 0.2s;
	}

	@keyframes flash {
		0%,
		40% {
			background: var(--course-container);
		}
	}

	.badge {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		border-radius: 12px;
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.badge > :global(svg:not(.cookie)) {
		position: relative;
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.family-media .badge {
		background: var(--course-container);
		color: var(--course-ink);
	}

	.family-interactive .badge {
		background: var(--gold-container);
		color: var(--on-gold-container);
	}

	.family-document .badge {
		background: var(--info-container);
		color: var(--on-info-container);
	}

	.done .badge {
		background: none;
		color: var(--surface);
	}

	.cookie {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		fill: var(--course-bar);
	}

	.locked .badge {
		background: var(--surface-sunken);
		color: var(--text-subtle);
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		padding-block: 1px;
	}

	.name {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		color: var(--text);
		font: var(--type-row-title);
		letter-spacing: var(--track-row-title);
		text-decoration: none;
		overflow-wrap: anywhere;
		outline: none;
	}

	a.name::after {
		content: '';
		position: absolute;
		inset: 0;
		border-radius: inherit;
	}

	.name :global(.out) {
		display: inline-block;
		inline-size: 14px;
		block-size: 14px;
		margin-inline-start: 4px;
		vertical-align: -1px;
		color: var(--text-subtle);
	}

	.locked .name {
		color: var(--text-muted);
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.restriction {
		font-style: italic;
	}

	.failure {
		color: var(--danger);
	}

	.description {
		position: relative;
		z-index: var(--z-raised);
		margin-block-start: 4px;
		color: var(--text-muted);
	}

	.description :global(.prose) {
		color: var(--text-muted);
	}

	.watch {
		max-inline-size: 280px;
		margin-block-start: 8px;
	}

	.more {
		position: relative;
		z-index: var(--z-raised);
		margin-block: -2px;
	}
</style>
