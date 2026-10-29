<script lang="ts">
	import type { MoodleForum, MoodleForumDiscussion } from '@mybicocca/data-api/elearning';
	import IconBell from '~icons/ph/bell';
	import IconClockCounterClockwise from '~icons/ph/clock-counter-clockwise';
	import IconPaperclip from '~icons/ph/paperclip';
	import IconSquaresFour from '~icons/ph/squares-four';
	import { now } from '#lib/data/clock.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { stripMlang } from '#lib/features/elearning/mlang.ts';
	import { formatRecent } from '#lib/format/dates.ts';
	import { isRenderableHtml } from '#lib/format/html.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import KeyHints from '#lib/ui/KeyHints.svelte';
	import { attachmentLimits, forumKind, lastActivity, subscriptionMode } from '../forum.ts';
	import { kindLabel } from './kinds.ts';

	interface Props {
		forum: MoodleForum;
		/** The discussion with the most recent post, once the list has loaded. */
		latest: MoodleForumDiscussion | undefined;
	}

	let { forum, latest }: Props = $props();

	const intro = $derived(stripMlang(forum.intro, getLocale()));
	const limits = $derived(
		attachmentLimits(forum, session.current?.account.learning?.maxUploadBytes)
	);

	const subscription = {
		forced: m.forum_subscription_forced,
		optional: m.forum_subscription_optional,
		disabled: m.forum_subscription_disabled
	} as const;

	const facts = $derived.by<Fact[]>(() => {
		const items: Fact[] = [
			{ label: m.forum_fact_kind(), value: kindLabel[forumKind(forum)](), icon: IconSquaresFour },
			{
				label: m.forum_fact_subscription(),
				value: subscription[subscriptionMode(forum)](),
				icon: IconBell
			}
		];
		if (latest !== undefined) {
			items.splice(1, 0, {
				label: m.forum_fact_latest(),
				value: m.forum_latest_activity({
					time: formatRecent(lastActivity(latest) * 1000, now.current),
					name: latest.userModifiedFullName || latest.userFullName
				}),
				icon: IconClockCounterClockwise
			});
		}
		if (forum.canCreateDiscussions === true && limits.maxFiles > 0) {
			items.push({
				label: m.forum_fact_attachments(),
				value:
					limits.maxBytes > 0
						? m.forum_attachments_limits({
								count: limits.maxFiles,
								size: formatBytes(limits.maxBytes)
							})
						: m.forum_attachments_count_limit({ count: limits.maxFiles }),
				icon: IconPaperclip
			});
		}
		return items;
	});
</script>

<div class="overview">
	{#if isRenderableHtml(intro)}
		<div class="intro">
			<TeacherHtml html={intro} />
		</div>
	{/if}
	<Facts items={facts} label={m.forum_about()} />
	{#if latest !== undefined}
		<div class="prompt">
			<p class="hint">{m.forum_pick_discussion()}</p>
			<KeyHints
				more={forum.canCreateDiscussions ? [{ keys: ['N'], label: m.forum_keys_new() }] : []}
			/>
		</div>
	{/if}
</div>

<style>
	.overview {
		display: grid;
		gap: var(--sp-6);
		max-inline-size: var(--w-reading);
		padding: var(--sp-6);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.intro {
		padding-block-end: var(--sp-5);
		border-block-end: 1px solid var(--hairline);
	}

	.prompt {
		display: grid;
		justify-items: start;
		gap: var(--sp-2);
		padding-block-start: var(--sp-5);
		border-block-start: 1px solid var(--hairline);
	}

	.hint {
		font: var(--type-title-s);
	}
</style>
