<script lang="ts" module>
	import type { MoodleForumPost } from '@mybicocca/data-api/elearning';

	export type ComposerMode =
		| { kind: 'new' }
		| { kind: 'reply'; parent: MoodleForumPost }
		| { kind: 'edit'; post: MoodleForumPost };

	export type ComposerResult =
		| { kind: 'new'; discussionId: number }
		| { kind: 'reply'; postId: number; post: MoodleForumPost }
		| { kind: 'edit'; postId: number };
</script>

<script lang="ts">
	import { onMount, tick, untrack } from 'svelte';
	import { beforeNavigate, goto } from '$app/navigation';
	import type { ResolvedPathname } from '$app/types';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError, type DataError } from '#lib/data/errors.ts';
	import { accountKey } from '#lib/data/keys.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { mutate, query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import RichEditor from '#lib/features/course/components/editor/RichEditor.svelte';
	import { getCourse } from '#lib/features/course/course.svelte.ts';
	import { editorHtml } from '#lib/features/course/editor/editorHtml.ts';
	import { sanitizeHtml } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Checkbox from '#lib/ui/Checkbox.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import Kbd from '#lib/ui/Kbd.svelte';
	import Select from '#lib/ui/Select.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import { attachmentLimits, replySubject } from '../forum.ts';
	import { getForum, rememberFollowing } from '../forumState.svelte.ts';
	import { courseGroupsQuery, discussionsKey, postsKey } from '../queries.ts';
	import { createDiscussion, editPost, reply, type Draft, type UploadState } from '../writes.ts';
	import AttachmentField from './AttachmentField.svelte';
	import AuthorAvatar from './AuthorAvatar.svelte';

	interface Props {
		mode: ComposerMode;
		onsubmitted: (result: ComposerResult) => void;
		/** The student closed the composer, after confirming if there was a draft. */
		oncancel: () => void;
	}

	let { mode, onsubmitted, oncancel }: Props = $props();

	const forum = getForum();
	const course = getCourse();
	const id = $props.id();

	const start = untrack(() => mode);
	const initialSubject = start.kind === 'edit' ? start.post.subject : '';
	const initialHtml = start.kind === 'edit' ? sanitizeHtml(start.post.message) : '';
	const existing = start.kind === 'edit' ? start.post.attachments : [];
	const draftKey =
		start.kind === 'new'
			? 'new'
			: `${start.kind}:${(start.kind === 'reply' ? start.parent : start.post).id}`;
	const saved = forum.drafts.get(draftKey);

	let subject = $state(saved?.subject ?? initialSubject);
	let html = $state(saved?.html ?? initialHtml);
	let blank = $state(saved === undefined);
	let changed = $state(false);
	let files = $state<File[]>(saved?.files ?? []);
	let kept = $state(saved?.kept ?? existing.map((file) => file.fileName));
	let states = $state<UploadState[]>([]);
	let subscribe = $state(saved?.subscribe ?? true);
	let submitting = $state(false);
	let attempted = $state(false);
	let error = $state.raw<DataError>();
	let confirming = $state(false);
	let editor = $state<HTMLElement>();
	let subjectInput = $state<HTMLInputElement>();
	/** Set once the post is sent or the draft discarded: leaving no longer asks. */
	let settled = false;

	function settle() {
		settled = true;
		forum.drafts.delete(draftKey);
	}
	let leaving: { href: ResolvedPathname } | { delta: number } | null = null;

	const record = $derived(forum.forum);
	const limits = $derived(
		record === undefined
			? { maxFiles: 0, maxBytes: 0 }
			: attachmentLimits(record, session.current?.account.learning?.maxUploadBytes)
	);
	const asksSubject = $derived(mode.kind !== 'reply');
	const subjectError = $derived(
		attempted && asksSubject && subject.trim() === ''
			? m.forum_composer_subject_required()
			: undefined
	);
	const messageError = $derived(
		attempted && blank ? m.forum_composer_message_required() : undefined
	);
	const dirty = $derived(
		subject !== initialSubject ||
			(mode.kind === 'edit' ? changed : !blank) ||
			files.length > 0 ||
			kept.length !== existing.length
	);
	const offline = $derived(!network.online);

	/* Group forums take the group a discussion belongs to; the module says whether this is one. */
	const groupMode = $derived(
		(course.contents.data ?? [])
			.flatMap((section) => section.modules)
			.find((module) => module.modName === 'forum' && module.instance === forum.id)?.groupMode ?? 0
	);
	const groups = query(() => {
		const scope = session.current;
		return (
			scope && mode.kind === 'new' && groupMode > 0 && courseGroupsQuery(scope, forum.courseId)
		);
	});
	const groupOptions = $derived(
		(groups.data?.groups ?? []).map((group) => ({
			value: String(group.id),
			label: group.name.trim() || m.forum_group_fallback({ id: group.id })
		}))
	);
	let group = $state(saved?.group ?? '');
	const groupId = $derived(group || groupOptions[0]?.value);

	/* The draft outlives this composer until it is sent or discarded (see `ForumState.drafts`). */
	$effect(() => {
		const draft = { subject, html, files: [...files], kept: [...kept], subscribe, group };
		if (dirty && !settled) forum.drafts.set(draftKey, draft);
		else forum.drafts.delete(draftKey);
	});

	const submitLabel = $derived(
		{ new: m.forum_composer_post, reply: m.forum_composer_reply, edit: m.forum_composer_save }[
			mode.kind
		]()
	);

	/* A new discussion starts from its subject; a reply or an edit from the message's end. */
	function place() {
		if (start.kind === 'new' || editor === undefined) {
			subjectInput?.focus();
			return;
		}
		editor.focus();
		const end = document.createRange();
		end.selectNodeContents(editor);
		end.collapse(false);
		getSelection()?.removeAllRanges();
		getSelection()?.addRange(end);
	}

	onMount(place);

	beforeNavigate((navigation) => {
		if (settled || (!dirty && !submitting)) return;
		navigation.cancel();
		if (navigation.willUnload || submitting || navigation.to === null) return;
		const { pathname, search, hash } = navigation.to.url;
		leaving =
			navigation.type === 'popstate'
				? { delta: navigation.delta }
				: { href: `${pathname}${search}${hash}` as ResolvedPathname };
		confirming = true;
	});

	function cancel() {
		if (submitting) return;
		if (dirty) {
			leaving = null;
			confirming = true;
		} else {
			settle();
			oncancel();
		}
	}

	function discard() {
		settle();
		const target = leaving;
		leaving = null;
		if (target === null) oncancel();
		else if ('delta' in target) history.go(target.delta);
		else void goto(target.href);
	}

	async function submit() {
		if (submitting) return;
		attempted = true;
		await tick();
		if (subjectError) {
			subjectInput?.focus();
			return;
		}
		if (messageError) {
			editor?.focus();
			return;
		}
		const scope = session.current;
		if (scope === undefined || offline) return;
		const current = mode;
		const draft: Draft = {
			subject: current.kind === 'reply' ? replySubject(current.parent) : subject,
			message: editorHtml(html, {
				files:
					current.kind === 'edit'
						? { component: 'mod_forum', area: 'post', itemId: current.post.id }
						: undefined
			}),
			files,
			subscribe: subscribe || !forum.canFollow
		};
		const progress = (index: number, state: UploadState) => (states[index] = state);
		const discussion =
			current.kind === 'reply'
				? current.parent.discussionId
				: current.kind === 'edit'
					? current.post.discussionId
					: undefined;
		const stale = [
			discussionsKey(scope, forum.id),
			accountKey(scope.account, 'moodle.forum.getForumsByCourses', forum.courseId),
			...(discussion === undefined ? [] : [postsKey(scope, discussion)])
		];
		submitting = true;
		error = undefined;
		states = files.map(() => 'waiting');
		try {
			const result = await mutate<ComposerResult>(
				async () => {
					if (current.kind === 'new') {
						const created = await createDiscussion(
							scope,
							forum.id,
							draft,
							groupId === undefined ? undefined : Number(groupId),
							progress
						);
						if (forum.canFollow) rememberFollowing(scope, created, draft.subscribe);
						return { kind: 'new', discussionId: created };
					}
					if (current.kind === 'reply') {
						const answer = await reply(scope, current.parent.id, draft, progress);
						if (forum.canFollow && draft.subscribe) {
							rememberFollowing(scope, current.parent.discussionId, true);
						}
						return { kind: 'reply', postId: answer.postId, post: answer.post };
					}
					await editPost(scope, current.post, draft, kept, progress);
					return { kind: 'edit', postId: current.post.id };
				},
				{ invalidate: stale }
			);
			settle();
			onsubmitted(result);
		} catch (failure) {
			error = toDataError(failure);
		} finally {
			submitting = false;
		}
	}

	function keydown(event: KeyboardEvent) {
		if (event.key === 'Escape' && !event.defaultPrevented) {
			event.preventDefault();
			cancel();
		}
	}

	function dragover(event: DragEvent) {
		if (event.dataTransfer?.types.includes('Files')) event.preventDefault();
	}
</script>

<!-- Escape and files dropped anywhere in the composer are the composer's. -->
<!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
<form
	class={['composer', mode.kind]}
	aria-labelledby={mode.kind === 'new' ? undefined : `${id}-title`}
	aria-label={mode.kind === 'new' ? m.forum_new_discussion() : undefined}
	aria-busy={submitting}
	novalidate
	onsubmit={(event) => {
		event.preventDefault();
		void submit();
	}}
	onkeydown={keydown}
	ondragover={dragover}
	ondrop={(event) => event.preventDefault()}
>
	{#if mode.kind === 'reply'}
		<p class="context" id="{id}-title">
			<AuthorAvatar name={mode.parent.author.fullName ?? ''} size={24} />
			<span>{m.forum_composer_replying({ name: mode.parent.author.fullName ?? '' })}</span>
		</p>
	{:else if mode.kind === 'edit'}
		<p class="context" id="{id}-title">{m.forum_composer_editing()}</p>
	{/if}

	{#if mode.kind === 'new' && groupOptions.length > 0}
		<Select
			label={m.forum_composer_group()}
			options={groupOptions}
			bind:value={() => groupId ?? '', (value) => (group = value)}
		/>
	{/if}

	{#if asksSubject}
		<TextField
			label={m.forum_composer_subject()}
			bind:value={subject}
			bind:input={subjectInput}
			maxlength={255}
			required
			autocomplete="off"
			error={subjectError}
			disabled={submitting}
		/>
	{/if}

	<RichEditor
		label={m.forum_composer_message()}
		hideLabel={mode.kind === 'reply'}
		initial={html}
		baseline={saved === undefined ? undefined : initialHtml}
		bind:html
		bind:blank
		bind:element={editor}
		error={messageError}
		placeholder={mode.kind === 'reply'
			? m.forum_composer_reply_placeholder()
			: m.forum_composer_placeholder()}
		disabled={submitting}
		onsubmit={submit}
		onedit={(value) => (changed = value)}
	/>

	{#if limits.maxFiles > 0}
		<AttachmentField
			{limits}
			existing={existing.map((file) => ({ name: file.fileName, size: file.fileSize }))}
			bind:kept
			bind:files
			{states}
			disabled={submitting}
		/>
	{/if}

	{#if offline}
		<div data-offline-notice>
			<Banner tone="warning" message={m.forum_composer_offline()} />
		</div>
	{/if}
	{#if error}
		<Banner
			tone="danger"
			role="alert"
			title={mode.kind === 'edit' ? m.forum_composer_save_failed() : m.forum_composer_post_failed()}
			message={errorMessage(error)}
		/>
	{/if}

	<div class="footer">
		{#if mode.kind !== 'edit' && forum.canFollow}
			<Checkbox
				label={m.forum_composer_subscribe()}
				bind:checked={subscribe}
				disabled={submitting}
			/>
		{/if}
		<div class="actions">
			<span class="shortcut" aria-hidden="true"><Kbd keys={['mod', 'enter']} /></span>
			<Button variant="ghost" disabled={submitting} onclick={cancel}>{m.ui_cancel()}</Button>
			<Button
				type="submit"
				variant="solid"
				loading={submitting}
				disabled={offline}
				aria-keyshortcuts="Control+Enter Meta+Enter"
			>
				{submitLabel}
			</Button>
		</div>
	</div>
</form>

<ConfirmDialog
	bind:open={confirming}
	title={m.forum_discard_title()}
	message={m.forum_discard_body()}
	confirmLabel={m.forum_discard_confirm()}
	cancelLabel={m.forum_discard_keep()}
	tone="danger"
	onconfirm={discard}
	oncancel={() => {
		leaving = null;
		editor?.focus();
	}}
/>

<style>
	.composer {
		display: grid;
		gap: var(--sp-4);
		min-inline-size: 0;
	}

	.reply,
	.edit {
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow:
			inset 0 0 0 1px var(--hairline-strong),
			var(--elev-2);
	}

	.context {
		display: flex;
		align-items: center;
		gap: 8px;
		color: var(--text-muted);
		font: var(--type-ui-strong);
	}

	.footer {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3) var(--sp-4);
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: flex-end;
		gap: 8px;
		margin-inline-start: auto;
	}

	.shortcut {
		margin-inline-end: 4px;
		opacity: 0.8;
	}

	@media (pointer: coarse) {
		.shortcut {
			display: none;
		}
	}
</style>
