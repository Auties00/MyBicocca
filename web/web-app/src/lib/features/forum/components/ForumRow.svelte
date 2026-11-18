<script lang="ts">
	import type { MoodleForum } from '@mybicocca/data-api/elearning';
	import { coursePath } from '#lib/features/course/links.ts';
	import { stripMlang } from '#lib/features/elearning/mlang.ts';
	import { htmlToLine } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import Badge from '#lib/ui/Badge.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import type { CourseSlot } from '#lib/ui/types.ts';
	import { forumKind } from '../forum.ts';
	import { kindIcon, kindLabel } from './kinds.ts';

	interface Props {
		courseId: number;
		forum: MoodleForum;
		slot: CourseSlot;
	}

	let { courseId, forum, slot }: Props = $props();

	const language = getLocale();
	const kind = $derived(forumKind(forum));
	const count = $derived(forum.numDiscussions ?? 0);
	const unread = $derived(forum.unreadPostsCount ?? 0);
	const intro = $derived(htmlToLine(stripMlang(forum.intro, language)));
	const description = $derived(
		count === 0 ? m.forum_row_empty({ kind: kindLabel[kind]() }) : intro || kindLabel[kind]()
	);
</script>

{#snippet badge()}
	<Badge
		count={unread}
		variant="solid"
		tone="accent"
		label={m.forum_unread_posts({ count: unread })}
	/>
{/snippet}

<ListRow
	title={stripMlang(forum.name, language).trim()}
	{description}
	meta={count === 0 ? undefined : m.forum_discussion_count({ count })}
	icon={kindIcon[kind]}
	course={count === 0 ? undefined : slot}
	href={coursePath(courseId, 'forum', forum.id)}
	lines={2}
	trailing={unread > 0 ? badge : undefined}
/>
