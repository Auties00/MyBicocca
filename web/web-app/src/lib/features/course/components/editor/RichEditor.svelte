<script lang="ts">
	import { untrack } from 'svelte';
	import type { Attachment } from 'svelte/attachments';
	import IconLink from '~icons/ph/link';
	import IconListBullets from '~icons/ph/list-bullets';
	import IconListNumbers from '~icons/ph/list-numbers';
	import IconQuotes from '~icons/ph/quotes';
	import IconTextB from '~icons/ph/text-b';
	import IconTextItalic from '~icons/ph/text-italic';
	import IconTextStrikethrough from '~icons/ph/text-strikethrough';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { hasMod, isApple } from '#lib/ui/platform.ts';
	import type { Icon } from '#lib/ui/types.ts';
	import { editorHtml } from '../../editor/editorHtml.ts';
	import { fetchFile } from '../../file/opener.svelte.ts';
	import { blobUrl } from '../../file/save.ts';
	import { classifyFile, mimeFor } from '../../files.ts';

	interface Props {
		label: string;
		/** Keeps the label for screen readers only, where the context already names the field. */
		hideLabel?: boolean;
		/** Sanitized HTML to start from. */
		initial?: string;
		/** What changes are measured against, when it isn't `initial` (a draft picked up again). */
		baseline?: string;
		/** The content as typed; clean it with `editorHtml` before sending. */
		html?: string;
		/** Whether the editor holds no text and no image. */
		blank?: boolean;
		error?: string;
		placeholder?: string;
		disabled?: boolean;
		/** Ctrl/⌘+Enter. */
		onsubmit?: () => void;
		/** After every change: whether the content now differs from `initial`. */
		onedit?: (changed: boolean) => void;
		/** The editable element, for focusing it. */
		element?: HTMLElement;
	}

	let {
		label,
		hideLabel = false,
		initial = '',
		baseline,
		html = $bindable(''),
		blank = $bindable(true),
		error,
		placeholder,
		disabled = false,
		onsubmit,
		onedit,
		element = $bindable()
	}: Props = $props();

	const id = $props.id();

	type Command = 'bold' | 'italic' | 'strikeThrough' | 'insertUnorderedList' | 'insertOrderedList';

	interface Tool {
		name: string;
		label: string;
		icon: Icon;
		command?: Command;
		shortcut?: string;
		run: () => void;
	}

	let active = $state<Record<string, boolean>>({});
	let linking = $state(false);
	let linkUrl = $state('');
	let linkInput = $state<HTMLInputElement>();
	let saved: Range | null = null;
	let current = $state(0);
	/** The content as the browser first laid out `initial`. */
	let pristine = '';

	/* Moodle's images get their address only once fetched; that isn't an edit. */
	const withoutBlobs = (markup: string) => markup.replace(/ src="blob:[^"]*"/g, '');

	/** `markup` as the browser serializes it once parsed, which is how `innerHTML` reads back. */
	function laidOut(markup: string): string {
		const template = document.createElement('template');
		template.innerHTML = markup;
		return template.innerHTML;
	}

	const mod = isApple ? '⌘' : 'Ctrl+';

	const tools: Tool[] = [
		{
			name: 'bold',
			label: m.course_editor_bold(),
			shortcut: `${mod}B`,
			icon: IconTextB,
			command: 'bold',
			run: () => exec('bold')
		},
		{
			name: 'italic',
			label: m.course_editor_italic(),
			icon: IconTextItalic,
			command: 'italic',
			shortcut: `${mod}I`,
			run: () => exec('italic')
		},
		{
			name: 'strike',
			label: m.course_editor_strike(),
			icon: IconTextStrikethrough,
			command: 'strikeThrough',
			run: () => exec('strikeThrough')
		},
		{ name: 'link', label: m.course_editor_link(), icon: IconLink, run: () => openLink() },
		{
			name: 'bullets',
			label: m.course_editor_bullets(),
			icon: IconListBullets,
			command: 'insertUnorderedList',
			run: () => exec('insertUnorderedList')
		},
		{
			name: 'numbers',
			label: m.course_editor_numbers(),
			icon: IconListNumbers,
			command: 'insertOrderedList',
			run: () => exec('insertOrderedList')
		},
		{ name: 'quote', label: m.course_editor_quote(), icon: IconQuotes, run: () => toggleQuote() }
	];

	/*
	 * The browser's editing commands are deprecated but still the only way to edit rich text
	 * without shipping an editor library; what they produce is cleaned by `editorHtml` anyway.
	 */
	function exec(command: string, value?: string) {
		if (element === undefined || disabled) return;
		element.focus();
		document.execCommand(command, false, value);
		sync();
	}

	function inQuote(): boolean {
		const node = getSelection()?.anchorNode ?? null;
		const start = node instanceof Element ? node : (node?.parentElement ?? null);
		const quote = start?.closest('blockquote');
		return quote !== null && quote !== undefined && element?.contains(quote) === true;
	}

	function toggleQuote() {
		exec('formatBlock', inQuote() ? 'p' : 'blockquote');
	}

	function sync() {
		if (element === undefined) return;
		html = element.innerHTML;
		blank =
			(element.textContent ?? '').trim() === '' && element.querySelector('img, table, hr') === null;
		onedit?.(withoutBlobs(html) !== pristine);
		refreshActive();
	}

	function refreshActive() {
		const selection = getSelection();
		if (element === undefined || selection === null || !element.contains(selection.anchorNode))
			return;
		const next: Record<string, boolean> = { quote: inQuote() };
		for (const tool of tools) {
			if (tool.command) next[tool.name] = document.queryCommandState(tool.command);
		}
		active = next;
	}

	function openLink() {
		const selection = getSelection();
		saved =
			selection && selection.rangeCount > 0 && element?.contains(selection.anchorNode)
				? selection.getRangeAt(0).cloneRange()
				: null;
		const anchor = saved?.commonAncestorContainer;
		const existing =
			anchor instanceof Element
				? anchor.closest('a')
				: (anchor?.parentElement?.closest('a') ?? null);
		linkUrl = existing?.getAttribute('href') ?? 'https://';
		linking = true;
		queueMicrotask(() => linkInput?.select());
	}

	function restore() {
		element?.focus();
		const selection = getSelection();
		if (saved !== null && selection !== null) {
			selection.removeAllRanges();
			selection.addRange(saved);
		}
	}

	function insertLink() {
		const url = linkUrl.trim();
		const valid = /^(https?:\/\/|mailto:)\S+$/i.test(url) && url !== 'https://';
		closeLink(false);
		if (!valid) return;
		restore();
		if (saved === null || saved.collapsed) {
			const text = document.createTextNode(url.replace(/^mailto:/i, ''));
			const anchor = document.createElement('a');
			anchor.href = url;
			anchor.append(text);
			exec('insertHTML', anchor.outerHTML);
		} else {
			exec('createLink', url);
		}
	}

	function closeLink(refocus = true) {
		linking = false;
		if (refocus) restore();
	}

	/* Bold and italic go through the same commands as the toolbar, whoever else heard the keys. */
	const keyCommands: Readonly<Record<string, Command>> = { b: 'bold', i: 'italic' };

	function keydown(event: KeyboardEvent) {
		if (!hasMod(event) || event.altKey) return;
		const key = event.key.toLowerCase();
		if (key === 'enter') {
			event.preventDefault();
			onsubmit?.();
		} else if (!event.shiftKey && keyCommands[key] !== undefined) {
			event.preventDefault();
			exec(keyCommands[key]);
		} else if (key === 'u') {
			queueMicrotask(sync);
		}
	}

	/* Pasted pages keep their structure, not their styles; plain text stays plain. */
	function paste(event: ClipboardEvent) {
		const data = event.clipboardData;
		if (data === null) return;
		event.preventDefault();
		const rich = data.getData('text/html');
		const cleaned = rich ? editorHtml(rich) : '';
		if (cleaned !== '') exec('insertHTML', cleaned);
		else exec('insertText', data.getData('text/plain'));
	}

	function toolbarKeydown(event: KeyboardEvent) {
		const count = tools.length;
		let next: number | undefined;
		if (event.key === 'ArrowRight') next = (current + 1) % count;
		else if (event.key === 'ArrowLeft') next = (current - 1 + count) % count;
		else if (event.key === 'Home') next = 0;
		else if (event.key === 'End') next = count - 1;
		if (next === undefined) return;
		event.preventDefault();
		current = next;
		(event.currentTarget as HTMLElement)
			.querySelectorAll<HTMLButtonElement>('button')
			[next]?.focus();
	}

	/* Runs once: the content is the browser's to edit from here, never rewritten by state. */
	const setup: Attachment<HTMLElement> = (node) => {
		untrack(() => {
			node.innerHTML = initial;
			pristine = withoutBlobs(baseline === undefined ? node.innerHTML : laidOut(baseline));
			document.execCommand('defaultParagraphSeparator', false, 'p');
			sync();
		});
		const selectionchange = () => refreshActive();
		document.addEventListener('selectionchange', selectionchange);
		return () => document.removeEventListener('selectionchange', selectionchange);
	};

	/* Images Moodle serves need the token: fetched with it and shown from memory, as posts do. */
	const moodleImages: Attachment<HTMLElement> = (root) => {
		const scope = untrack(() => session.current);
		const images = root.querySelectorAll<HTMLImageElement>('img[data-moodle-src]');
		if (scope === undefined || images.length === 0) return;
		const urls: string[] = [];
		let live = true;
		for (const image of images) {
			const url = image.dataset.moodleSrc ?? '';
			const name = new URL(url).pathname.split('/').at(-1) ?? '';
			fetchFile(scope, { url, modified: null })
				.then((bytes) => {
					if (!live) return;
					const src = blobUrl(bytes, mimeFor(classifyFile(name, null), name));
					urls.push(src);
					image.src = src;
				})
				.catch(() => image.setAttribute('alt', name));
		}
		return () => {
			live = false;
			urls.forEach((url) => URL.revokeObjectURL(url));
		};
	};
</script>

<div class="field">
	<span class={['label', { 'sr-only': hideLabel }]} id="{id}-label">{label}</span>
	<div class={['box', { invalid: !!error, disabled }]}>
		<div
			class="toolbar"
			role="toolbar"
			tabindex="-1"
			aria-label={m.course_editor_toolbar()}
			aria-controls={id}
			onkeydown={toolbarKeydown}
		>
			{#each tools as tool, index (tool.name)}
				<button
					type="button"
					class={['tool', { on: active[tool.name] }]}
					title={tool.shortcut ? `${tool.label} (${tool.shortcut})` : tool.label}
					aria-label={tool.label}
					aria-pressed={tool.name === 'link' ? undefined : (active[tool.name] ?? false)}
					tabindex={index === current ? 0 : -1}
					{disabled}
					onmousedown={(event) => event.preventDefault()}
					onclick={() => {
						current = index;
						tool.run();
					}}
				>
					<tool.icon aria-hidden="true" />
				</button>
				{#if index === 2 || index === 3}<span class="divider" aria-hidden="true"></span>{/if}
			{/each}
		</div>
		{#if linking}
			<div class="linkbar">
				<label class="sr-only" for="{id}-url">{m.course_editor_link_url()}</label>
				<input
					id="{id}-url"
					type="url"
					inputmode="url"
					bind:this={linkInput}
					bind:value={linkUrl}
					placeholder="https://"
					onkeydown={(event) => {
						if (event.key === 'Enter') {
							event.preventDefault();
							insertLink();
						} else if (event.key === 'Escape') {
							event.preventDefault();
							event.stopPropagation();
							closeLink();
						}
					}}
				/>
				<Button size="sm" variant="ghost" onclick={() => closeLink()}>{m.ui_cancel()}</Button>
				<Button size="sm" variant="tonal" onclick={insertLink}>{m.course_editor_link_add()}</Button>
			</div>
		{/if}
		<div
			{id}
			class={['editor', { blank }]}
			contenteditable={!disabled}
			role="textbox"
			tabindex="0"
			aria-multiline="true"
			aria-labelledby="{id}-label"
			aria-describedby={error ? `${id}-error` : undefined}
			aria-invalid={error ? true : undefined}
			aria-disabled={disabled || undefined}
			data-placeholder={placeholder}
			aria-placeholder={placeholder}
			bind:this={element}
			{@attach setup}
			{@attach moodleImages}
			oninput={sync}
			onkeydown={keydown}
			onpaste={paste}
		></div>
	</div>
	{#if error}
		<p class="error" id="{id}-error">
			<IconWarningCircle aria-hidden="true" />
			{error}
		</p>
	{/if}
</div>

<style>
	.field {
		display: grid;
		gap: 6px;
		min-inline-size: 0;
	}

	.label {
		color: var(--text);
		font: var(--type-ui-strong);
	}

	.box {
		display: grid;
		container: editor / inline-size;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.box:hover:not(.disabled) {
			box-shadow: inset 0 0 0 1px var(--border-strong);
			transition-duration: 0s;
		}
	}

	.box:focus-within {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	.invalid,
	.invalid:hover {
		box-shadow: inset 0 0 0 1px var(--danger);
	}

	.disabled {
		opacity: 0.6;
	}

	/*
	 * One row at every width: a box too narrow even for the compact tools scrolls them, and the
	 * edge with more tools behind it fades out.
	 */
	.toolbar {
		display: flex;
		align-items: center;
		gap: 2px;
		padding: 4px 6px;
		overflow-x: auto;
		scrollbar-width: none;
		overscroll-behavior-inline: contain;
		border-block-end: 1px solid var(--hairline);
	}

	@supports (animation-timeline: scroll()) {
		.toolbar {
			mask-image: linear-gradient(
				to right,
				transparent,
				#000 var(--forum-tools-fade-start),
				#000 calc(100% - var(--forum-tools-fade-end)),
				transparent
			);
			animation: toolbar-edges linear both;
			animation-timeline: scroll(self inline);
		}
	}

	@property --forum-tools-fade-start {
		syntax: '<length>';
		inherits: false;
		initial-value: 0px;
	}

	@property --forum-tools-fade-end {
		syntax: '<length>';
		inherits: false;
		initial-value: 0px;
	}

	@keyframes toolbar-edges {
		0% {
			--forum-tools-fade-start: 0px;
			--forum-tools-fade-end: 28px;
		}

		15%,
		85% {
			--forum-tools-fade-start: 28px;
			--forum-tools-fade-end: 28px;
		}

		100% {
			--forum-tools-fade-start: 28px;
			--forum-tools-fade-end: 0px;
		}
	}

	.toolbar::-webkit-scrollbar {
		display: none;
	}

	.tool {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 34px;
		block-size: 34px;
		border-radius: var(--r-xs);
		color: var(--text-muted);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.tool :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (hover: hover) {
		.tool:hover:not(:disabled) {
			background: var(--state-hover);
			color: var(--text);
		}
	}

	.tool.on {
		background: var(--container);
		color: var(--on-container);
	}

	.tool:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	@media (pointer: coarse) {
		.tool {
			inline-size: 40px;
			block-size: 40px;
		}
	}

	.divider {
		flex-shrink: 0;
		inline-size: 1px;
		block-size: 20px;
		margin-inline: 4px;
		background: var(--hairline-strong);
	}

	/* A phone-width reply still fits every tool. */
	@container editor (width < 330px) {
		.tool {
			inline-size: 34px;
		}

		.divider {
			margin-inline: 2px;
		}
	}

	.linkbar {
		display: flex;
		align-items: center;
		gap: 6px;
		padding: 6px 8px;
		border-block-end: 1px solid var(--hairline);
	}

	.linkbar input {
		flex: 1;
		min-inline-size: 0;
		block-size: var(--control-h-sm);
		padding-inline: 10px;
		border: 0;
		border-radius: var(--r-xs);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		font: var(--type-ui);
	}

	.linkbar input:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	.editor {
		position: relative;
		min-block-size: 140px;
		max-block-size: min(60vh, 520px);
		overflow-y: auto;
		padding: 12px 14px;
		color: var(--text);
		font: var(--type-body);
		overflow-wrap: break-word;
		outline: none;
		cursor: text;
	}

	@media (pointer: fine) {
		.editor {
			font-size: 0.9375rem;
		}
	}

	.editor.blank::before {
		content: attr(data-placeholder);
		position: absolute;
		inset-block-start: 12px;
		inset-inline-start: 14px;
		color: var(--text-subtle);
		pointer-events: none;
	}

	.editor :global(:where(p, ul, ol, blockquote, pre) + *) {
		margin-block-start: 0.7em;
	}

	.editor :global(:where(ul, ol)) {
		padding-inline-start: 1.4em;
	}

	.editor :global(blockquote) {
		padding-inline-start: 1em;
		border-inline-start: 3px solid var(--border);
		color: var(--text-muted);
	}

	.editor :global(a) {
		color: var(--accent-text);
		font-weight: 550;
	}

	.editor :global(img) {
		max-inline-size: 100%;
		block-size: auto;
		border-radius: var(--r-sm);
		background: var(--surface);
	}

	/* Moodle's images appear once fetched with the token. Until then they hold the box their width
	   and height give them, as the loaded picture will; without those, a fixed one. */
	.editor :global(img[data-moodle-src]:not([src])) {
		display: inline-block;
		background: var(--track);
	}

	.editor :global(img[data-moodle-src]:not([src]):not([width][height])) {
		inline-size: 100%;
		block-size: 120px;
	}

	.editor :global(code) {
		padding: 0.1em 0.35em;
		border-radius: var(--r-xs, 4px);
		background: var(--surface-sunken);
		font-family: var(--font-mono, ui-monospace, monospace);
		font-size: 0.9em;
	}

	.editor :global(pre) {
		overflow-x: auto;
		padding: 10px 12px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
	}

	.editor :global(pre code) {
		padding: 0;
		background: none;
	}

	.error {
		display: flex;
		align-items: center;
		gap: 6px;
		color: var(--danger);
		font: var(--type-meta);
	}

	.error :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		flex-shrink: 0;
	}
</style>
