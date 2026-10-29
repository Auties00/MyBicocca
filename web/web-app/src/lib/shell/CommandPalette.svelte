<script lang="ts">
	import { untrack } from 'svelte';
	import { goto } from '$app/navigation';
	import type { ResolvedPathname } from '$app/types';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconCompass from '~icons/ph/compass';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import IconMagnifyingGlassDuotone from '~icons/ph/magnifying-glass-duotone';
	import IconMicrophone from '~icons/ph/microphone';
	import IconStop from '~icons/ph/stop-fill';
	import IconWarningCircle from '~icons/ph/warning-circle';
	import IconX from '~icons/ph/x';
	import { Dictation } from '#lib/features/search/dictation.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Kbd from '#lib/ui/Kbd.svelte';
	import { compact } from '#lib/ui/media.ts';
	import { modal } from '#lib/ui/modal.svelte.ts';
	import { externalHref, internalHref } from '#lib/ui/types.ts';
	import { catalogItems } from './catalog.ts';
	import {
		paletteRegistry,
		searchPalette,
		topHit,
		type PaletteItem,
		type TitleRange
	} from './palette.svelte.ts';
	import { shell } from './shell.svelte.ts';

	const id = $props.id();
	const debounce = 80;

	let query = $state('');
	let results = $state.raw<PaletteItem[]>([]);
	/** The query `results` answer; the field may be ahead of it while a search runs. */
	let answered = $state('');
	let activeId = $state<string>();
	/** On phones a row is highlighted, and opened by Enter, only once the arrow keys are used. */
	let arrowed = $state(false);
	/** Enter pressed before the list answered the field: the query it opens the best row of. */
	let submitted = $state<string>();
	let input = $state<HTMLInputElement>();
	let list = $state<HTMLElement>();
	let scroller = $state<HTMLElement>();
	const dictation = new Dictation();

	const text = $derived(query.trim());

	/* The rows are built when the palette first opens, not as every page loads. */
	let opened = $state(false);
	$effect(() => {
		if (shell.paletteOpen) opened = true;
	});

	/* Before typing: what the providers suggest (history, things to do), then the destinations. */
	const idle = $derived.by((): PaletteItem[] => {
		if (!opened) return [];
		const rows = [
			...paletteRegistry.suggestions(),
			...catalogItems().filter((item) => item.shortcut)
		];
		return rows.filter((item, at) => rows.findIndex((other) => other.id === item.id) === at);
	});

	const top = $derived(text ? topHit(results) : undefined);
	/** What the query found, without the rows offered whatever it finds. */
	const hits = $derived(results.filter((item) => !item.fallback));
	const notices = $derived(paletteRegistry.notices);

	/* Groups in order of their best row, so the first row is the best and arrows follow the eye. */
	const sections = $derived.by(() => {
		const out: { name: string; top: boolean; items: PaletteItem[] }[] = [];
		if (top) out.push({ name: m.search_top_hit(), top: true, items: [top] });
		for (const item of text ? results : idle) {
			if (item === top) continue;
			const group = out.find((section) => !section.top && section.name === item.group);
			if (group) group.items.push(item);
			else out.push({ name: item.group, top: false, items: [item] });
		}
		return out;
	});
	const ordered = $derived(sections.flatMap((section) => section.items));
	const loading = $derived(paletteRegistry.loading);
	/* The fallback (search the catalog) is no answer yet while results are still coming. */
	const active = $derived(
		ordered.find((item) => item.id === activeId) ??
			(loading && ordered[0]?.fallback ? undefined : ordered[0])
	);
	const marked = $derived(!compact.current || arrowed);

	const settled = $derived(text !== '' && answered === text);
	/** Enter waits: the list answers an earlier text, or data is still coming and nothing matched. */
	const unanswered = $derived(text !== '' && (!settled || (loading && hits.length === 0)));
	const emptyTitle = $derived(
		notices.length > 0
			? m.search_empty_partial({ query: text })
			: m.shell_palette_empty({ query: text })
	);
	const status = $derived.by(() => {
		if (!settled) return '';
		if (loading) return hits.length === 0 ? m.search_loading() : '';
		const count = hits.length > 0 ? m.search_results({ count: hits.length }) : emptyTitle;
		return [count, ...notices.map((notice) => notice.message)].join('. ');
	});

	/*
	 * Phones: one button beside the field, so focus stays on it as it turns from the microphone to
	 * stop, then to clear once something is heard.
	 */
	const fieldAction = $derived(
		dictation.listening
			? { icon: IconStop, label: m.search_voice_stop(), run: () => dictation.stop() }
			: query
				? { icon: IconX, label: m.search_clear(), run: clear }
				: dictation.supported
					? { icon: IconMicrophone, label: m.search_voice(), run: dictate }
					: undefined
	);

	$effect(() => {
		const current = text;
		if (!current) {
			results = [];
			answered = '';
			return;
		}
		// A provider whose index changed (data arrived) is asked again.
		void paletteRegistry.version;
		const controller = new AbortController();
		const timer = setTimeout(async () => {
			const found = await searchPalette(current, controller.signal);
			if (controller.signal.aborted) return;
			results = found;
			answered = current;
		}, debounce);
		return () => {
			clearTimeout(timer);
			controller.abort();
		};
	});

	$effect(() => {
		if (shell.paletteOpen) return;
		query = '';
		activeId = undefined;
		arrowed = false;
		submitted = undefined;
		dictation.cancel();
	});

	$effect(() => {
		if (submitted !== text || unanswered) return;
		submitted = undefined;
		untrack(() => active && open(active));
	});

	function optionId(item: PaletteItem) {
		return `${id}-option-${ordered.indexOf(item)}`;
	}

	/** The title with the parts the query matched set apart. */
	function segments(title: string, ranges: readonly TitleRange[] = []) {
		const parts: { text: string; hit: boolean }[] = [];
		let at = 0;
		for (const [start, end] of ranges) {
			if (start > at) parts.push({ text: title.slice(at, start), hit: false });
			parts.push({ text: title.slice(start, end), hit: true });
			at = end;
		}
		if (at < title.length) parts.push({ text: title.slice(at), hit: false });
		return parts;
	}

	function move(step: number) {
		const count = ordered.length;
		if (count === 0) return;
		const from = active ? ordered.indexOf(active) : -1;
		const next = ordered[(from + step + count) % count];
		activeId = next.id;
		arrowed = true;
		list?.querySelector(`#${CSS.escape(optionId(next))}`)?.scrollIntoView({ block: 'nearest' });
	}

	function navigate(to: ResolvedPathname) {
		void goto(to);
	}

	function open(item: PaletteItem) {
		paletteRegistry.opened(item, text ? answered : '');
		if (item.stay) {
			item.run?.();
			return;
		}
		shell.paletteOpen = false;
		const internal = internalHref(item.href);
		const external = externalHref(item.href);
		if (item.run) item.run();
		else if (internal) navigate(internal);
		else if (external) window.open(external, '_blank', 'noopener');
	}

	function remove(item: PaletteItem) {
		const at = ordered.indexOf(item);
		activeId = (ordered[at + 1] ?? ordered[at - 1])?.id;
		item.remove?.();
	}

	function clear() {
		query = '';
		activeId = undefined;
		input?.focus();
	}

	function dictate() {
		dictation.start((heard) => {
			query = heard;
			activeId = undefined;
		});
	}

	function retry(again: () => void) {
		again();
		input?.focus();
	}

	/* Phones: the on-screen keyboard goes away, focus staying in the dialog, on the results. */
	function hideKeyboard() {
		if (document.activeElement === input) scroller?.focus({ preventScroll: true });
	}

	/* As on Android, dragging the results puts the keyboard away. */
	function hideKeyboardOnScroll(pane: HTMLElement) {
		pane.addEventListener('touchmove', hideKeyboard, { passive: true });
		return () => pane.removeEventListener('touchmove', hideKeyboard);
	}

	function keydown(event: KeyboardEvent) {
		if (event.isComposing) return;
		if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
			event.preventDefault();
			move(event.key === 'ArrowDown' ? 1 : -1);
		} else if (event.key === 'Enter') {
			event.preventDefault();
			if (compact.current && !arrowed) hideKeyboard();
			else if (unanswered) submitted = text;
			else if (active) open(active);
		} else if (event.key === 'Escape' && query) {
			// The first Escape empties the field; the next one closes.
			event.preventDefault();
			clear();
		} else if (
			(event.key === 'Backspace' || event.key === 'Delete') &&
			event.shiftKey &&
			!query &&
			active?.remove
		) {
			event.preventDefault();
			remove(active);
		}
	}
</script>

{#snippet option(item: PaletteItem)}
	{@const Glyph = item.icon ?? IconCompass}
	{@const current = marked && item === active}
	<div
		id={optionId(item)}
		class={['option', { active: current }]}
		role="option"
		aria-selected={current}
		aria-keyshortcuts={item.remove ? 'Shift+Backspace' : undefined}
		tabindex="-1"
		onpointermove={() => (activeId = item.id)}
		onclick={() => open(item)}
		onkeydown={(e) => e.key === 'Enter' && open(item)}
	>
		<span class="icon" data-tone={item.tone}><Glyph aria-hidden="true" /></span>
		<span class="text">
			<span class="title">
				{#each segments(item.title, item.highlight) as part, i (i)}
					{#if part.hit}<b>{part.text}</b>{:else}{part.text}{/if}
				{/each}
			</span>
			{#if item.subtitle}<span class="subtitle">{item.subtitle}</span>{/if}
		</span>
		{#if item.shortcut}<span class="shortcut" aria-hidden="true"><Kbd keys={item.shortcut} /></span
			>{/if}
	</div>
{/snippet}

<dialog
	class={['palette', { typed: text !== '' }]}
	aria-label={m.shell_palette_label()}
	{@attach modal({
		open: () => shell.paletteOpen,
		dismissible: () => true,
		close: () => (shell.paletteOpen = false)
	})}
>
	<div class="field">
		{#if compact.current}
			<button class="back" type="button" onclick={() => (shell.paletteOpen = false)}>
				<IconArrowLeft aria-hidden="true" />
				<span class="sr-only">{m.shell_palette_close()}</span>
			</button>
		{:else}
			<IconMagnifyingGlass class="glass" aria-hidden="true" />
		{/if}
		<input
			bind:this={input}
			bind:value={query}
			type="text"
			role="combobox"
			aria-label={m.shell_palette_label()}
			aria-expanded="true"
			aria-controls="{id}-list"
			aria-autocomplete="list"
			aria-activedescendant={active && marked ? optionId(active) : undefined}
			placeholder={dictation.listening
				? m.search_voice_listening()
				: compact.current
					? m.search_placeholder_short()
					: m.search_placeholder()}
			autocomplete="off"
			spellcheck={false}
			enterkeyhint={compact.current ? 'search' : 'go'}
			data-autofocus
			oninput={() => {
				activeId = undefined;
				submitted = undefined;
			}}
			onkeydown={keydown}
		/>
		{#if compact.current}
			{#if fieldAction}
				{@const Glyph = fieldAction.icon}
				<button
					class={['clear', { listening: dictation.listening }]}
					type="button"
					onclick={fieldAction.run}
				>
					<Glyph aria-hidden="true" />
					<span class="sr-only">{fieldAction.label}</span>
				</button>
			{/if}
		{:else}
			<!-- As the key it shows: the first press empties the field, the next one closes. -->
			<button
				class="esc"
				type="button"
				onclick={() => (query ? clear() : (shell.paletteOpen = false))}
			>
				<span class="esc-key" aria-hidden="true"><Kbd keys={['esc']} /></span>
				<IconX class="esc-icon" aria-hidden="true" />
				<span class="sr-only">{query ? m.search_clear() : m.shell_palette_close()}</span>
			</button>
		{/if}
		<span class={['progress', { on: loading }]} aria-hidden="true"></span>
	</div>

	{#if text}
		{#each notices as notice (notice.message)}
			<div class="notice">
				<IconWarningCircle aria-hidden="true" />
				<p>{notice.message}</p>
				{#if notice.retry}
					{@const again = notice.retry}
					<Button variant="ghost" size="sm" onclick={() => retry(again)}>{m.common_retry()}</Button>
				{/if}
			</div>
		{/each}
	{/if}

	<div class="results" tabindex="-1" bind:this={scroller} {@attach hideKeyboardOnScroll}>
		{#if text && hits.length === 0}
			<div class={['empty', { pending: !settled }]}>
				{#if loading}
					<p class="empty-hint">{m.search_loading()}</p>
				{:else}
					<IconMagnifyingGlassDuotone aria-hidden="true" />
					<p class="empty-title">{emptyTitle}</p>
					<p class="empty-hint">
						{#if notices.length === 0}
							{m.shell_palette_empty_hint()}
						{:else if notices.every((notice) => notice.offline)}
							{m.search_empty_offline_hint()}
						{:else}
							{m.search_empty_partial_hint()}
						{/if}
					</p>
				{/if}
			</div>
		{/if}
		<div bind:this={list} id="{id}-list" role="listbox" aria-label={m.shell_palette_label()}>
			{#each sections as section (section.top ? '' : section.name)}
				<div class={['group', { top: section.top }]} role="group" aria-label={section.name}>
					<p class="group-name" aria-hidden="true">{section.name}</p>
					{#each section.items as item (item.id)}
						{#if item.remove}
							<!-- Beside the option, not in it: an option's content is read as plain text. -->
							<div class="removable">
								{@render option(item)}
								<button
									class="remove"
									type="button"
									tabindex="-1"
									aria-label={m.search_history_remove_item({ title: item.title })}
									title={m.search_history_remove()}
									onpointerdown={(e) => e.preventDefault()}
									onclick={() => remove(item)}
								>
									<IconX aria-hidden="true" />
								</button>
							</div>
						{:else}
							{@render option(item)}
						{/if}
					{/each}
				</div>
			{/each}
		</div>
	</div>

	<p class="sr-only" role="status">{status}</p>

	<footer class="hints" aria-hidden="true">
		<span><Kbd keys={['up', 'down']} /> {m.shell_palette_navigate()}</span>
		<span><Kbd keys={['enter']} /> {m.shell_palette_open()}</span>
		{#if active?.remove && !query}
			<span><Kbd keys={['shift', 'backspace']} /> {m.search_history_remove_short()}</span>
		{/if}
		<span><Kbd keys={['esc']} /> {query ? m.search_clear() : m.shell_palette_close()}</span>
	</footer>
</dialog>

<style>
	.palette {
		position: fixed;
		inset: 12vh 0 auto;
		margin-inline: auto;
		inline-size: min(100% - 32px, 640px);
		max-block-size: min(76vh, 640px);
		border-radius: var(--r-xl);
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-overlay), var(--inner-light);
		overflow: hidden;
		opacity: 0;
		transform: scale(0.96);
		transition:
			opacity 120ms var(--ease-out),
			transform 120ms var(--ease-out),
			overlay 120ms allow-discrete,
			display 120ms allow-discrete;
	}

	.palette[open] {
		display: flex;
		flex-direction: column;
		opacity: 1;
		transform: none;
		transition-duration: 160ms;
		transition-timing-function: var(--ease-out-expo);
	}

	/* Once something is typed the dialog keeps its size: its edge doesn't jump on each key. */
	@media (width >= 600px) {
		.palette.typed {
			block-size: min(76vh, 640px);
		}
	}

	@media (width >= 600px) and (height >= 1100px) {
		.palette {
			max-block-size: min(70vh, 800px);
		}

		.palette.typed {
			block-size: min(70vh, 800px);
		}
	}

	@starting-style {
		.palette[open] {
			opacity: 0;
			transform: scale(0.96);
		}
	}

	.palette::backdrop {
		background: var(--scrim);
		opacity: 0;
		transition:
			opacity 160ms var(--ease-out),
			overlay 160ms allow-discrete,
			display 160ms allow-discrete;
	}

	.palette[open]::backdrop {
		opacity: 1;
	}

	@starting-style {
		.palette[open]::backdrop {
			opacity: 0;
		}
	}

	/* Phones: a full-screen search page. */
	@media (width < 600px) {
		.palette {
			inset: 0;
			inline-size: 100%;
			max-inline-size: 100%;
			block-size: 100dvh;
			max-block-size: 100dvh;
			border-radius: 0;
			padding-block-start: var(--safe-top);
			background: var(--surface);
			transform: translateY(12px);
		}

		@starting-style {
			.palette[open] {
				transform: translateY(12px);
			}
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.palette,
		.palette[open] {
			transform: none;
		}

		@starting-style {
			.palette[open] {
				transform: none;
			}
		}
	}

	.field {
		position: relative;
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 0 12px 0 20px;
		box-shadow: inset 0 -1px 0 var(--hairline);
	}

	.field :global(.glass) {
		flex-shrink: 0;
		inline-size: 20px;
		block-size: 20px;
		color: var(--text-muted);
	}

	input {
		flex: 1;
		min-inline-size: 0;
		block-size: 60px;
		border: 0;
		background: none;
		outline: none;
		font: 400 1.0625rem / 1.4 var(--font-ui);
	}

	.esc,
	.back,
	.clear {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		min-inline-size: 44px;
		min-block-size: 44px;
		border-radius: var(--r-sm);
		color: var(--text-muted);
	}

	.back,
	.clear {
		border-radius: var(--r-pill);
		color: var(--text);
	}

	.back :global(svg),
	.clear :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	/* Dictating: the stop button in the accent, a ring breathing out of it. */
	.listening {
		position: relative;
		isolation: isolate;
		background: var(--container);
		color: var(--accent-text);
	}

	.listening::after {
		content: '';
		position: absolute;
		inset: 0;
		z-index: -1;
		border-radius: inherit;
		background: var(--accent);
		opacity: 0;
		animation: listening 1.6s var(--ease-out) infinite;
	}

	@keyframes listening {
		from {
			opacity: 0.3;
			transform: scale(1);
		}
		to {
			opacity: 0;
			transform: scale(1.45);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.listening::after {
			animation: none;
		}
	}

	.esc :global(.esc-icon) {
		display: none;
		inline-size: 22px;
		block-size: 22px;
	}

	@media (hover: hover) {
		.esc:hover,
		.back:hover,
		.clear:hover {
			background: var(--state-hover);
		}
	}

	/* An index still loading: a thin line along the field's edge, after a beat. */
	.progress {
		position: absolute;
		inset: auto 0 0;
		block-size: 2px;
		overflow: hidden;
		opacity: 0;
		pointer-events: none;
	}

	.progress.on {
		opacity: 1;
		transition: opacity 120ms 150ms;
	}

	.progress::after {
		content: '';
		position: absolute;
		inset: 0 auto 0 0;
		inline-size: 30%;
		border-radius: var(--r-pill);
		background: var(--accent);
		animation: sweep 1.1s var(--ease-in-out) infinite;
	}

	@keyframes sweep {
		from {
			transform: translateX(-100%);
		}
		to {
			transform: translateX(340%);
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.progress::after {
			inset-inline-end: 0;
			inline-size: auto;
			opacity: 0.5;
			animation: none;
		}
	}

	@media (width < 600px) {
		.field {
			gap: 4px;
			padding: 4px 8px;
		}

		input {
			block-size: 56px;
			padding-inline: 4px;
		}
	}

	/* What a provider couldn't load: one quiet line over the results. */
	.notice {
		display: flex;
		align-items: center;
		gap: 10px;
		min-block-size: 40px;
		padding: 4px 12px 4px 20px;
		background: color-mix(in oklch, var(--warning-container) 55%, transparent);
		box-shadow: inset 0 -1px 0 var(--hairline);
		color: var(--text);
		font: var(--type-meta);
	}

	.notice > :global(svg) {
		flex-shrink: 0;
		inline-size: 18px;
		block-size: 18px;
		color: var(--warning);
	}

	.notice p {
		flex: 1;
		min-inline-size: 0;
		padding-block: 6px;
	}

	@media (width < 600px) {
		.notice {
			padding-inline: 16px 8px;
		}
	}

	.results {
		flex: 1;
		overflow-y: auto;
		overscroll-behavior: contain;
		padding: 6px 8px 8px;
		scrollbar-width: thin;
	}

	/* Focused only to put the on-screen keyboard away: the rows show where the student is. */
	.results:focus-visible {
		outline: none;
	}

	/* Nothing found: the empty state, and what to search elsewhere, in the middle. */
	.results:has(> .empty) {
		display: flex;
		flex-direction: column;
		justify-content: safe center;
	}

	@media (width < 600px) {
		.results {
			padding: 8px 8px calc(var(--safe-bottom) + 16px);
		}
	}

	.group + .group {
		margin-block-start: 6px;
	}

	.group-name {
		padding: 10px 12px 6px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.top .group-name {
		color: var(--accent-text);
	}

	.option {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 48px;
		padding: 6px 12px;
		border-radius: var(--r-md);
		cursor: pointer;
		scroll-margin-block: 40px 8px;
	}

	@media (width < 600px) {
		.option {
			min-block-size: 56px;
		}
	}

	@media (hover: none) {
		.option:active {
			background: var(--state-press);
		}
	}

	.option.active {
		background: var(--container);
		color: var(--on-container);
	}

	.icon {
		--bed: var(--surface-sunken);
		--ink: var(--text-muted);
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 32px;
		block-size: 32px;
		border-radius: 10px;
		background: var(--bed);
		color: var(--ink);
	}

	.icon[data-tone='accent'] {
		--bed: var(--container);
		--ink: var(--accent-text);
	}

	.icon[data-tone='success'] {
		--bed: var(--success-container);
		--ink: var(--success);
	}

	.icon[data-tone='warning'] {
		--bed: var(--warning-container);
		--ink: var(--warning);
	}

	.icon[data-tone='info'] {
		--bed: var(--info-container);
		--ink: var(--info);
	}

	.icon[data-tone='danger'] {
		--bed: var(--danger-container);
		--ink: var(--danger);
	}

	.icon[data-tone='gold'] {
		--bed: var(--gold-container);
		--ink: var(--gold);
	}

	.icon[data-tone='violet'] {
		--bed: var(--violet-container);
		--ink: var(--violet);
	}

	.active .icon {
		--bed: color-mix(in oklch, var(--on-container) 10%, transparent);
		--ink: currentColor;
	}

	.icon :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.top .option {
		min-block-size: 64px;
		padding-block: 10px;
		background: var(--surface-sunken);
	}

	.top .option.active {
		background: var(--container);
	}

	.top .icon {
		inline-size: 40px;
		block-size: 40px;
		border-radius: 12px;
	}

	.top .option:not(.active) .icon:not([data-tone]) {
		--bed: var(--surface-raised);
	}

	.top .icon :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.text {
		flex: 1;
		display: grid;
		min-inline-size: 0;
	}

	.title {
		overflow: hidden;
		font: var(--type-ui);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.top .title {
		font: var(--type-ui-strong);
		font-size: 1.0625rem;
	}

	.title b {
		color: var(--accent-text);
		font-weight: 650;
	}

	.active .title b {
		color: inherit;
	}

	.subtitle {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.active .subtitle {
		color: color-mix(in oklch, currentColor 80%, transparent);
	}

	.removable {
		position: relative;
	}

	.removable .option {
		padding-inline-end: 48px;
	}

	.remove {
		position: absolute;
		inset-block: 0;
		inset-inline-end: 6px;
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		margin-block: auto;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		opacity: 0;
	}

	.remove :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.removable:hover .remove,
	.removable:has(.active) .remove {
		opacity: 0.7;
	}

	.removable:has(.active) .remove {
		color: var(--on-container);
	}

	.remove:hover {
		background: var(--state-hover);
		opacity: 1;
	}

	@media (hover: none) {
		.removable .option {
			padding-inline-end: 52px;
		}

		.remove {
			inset-inline-end: 2px;
			inline-size: 44px;
			block-size: 44px;
			opacity: 0.7;
		}
	}

	.empty {
		display: grid;
		justify-items: center;
		gap: 4px;
		padding: 36px 24px 28px;
		text-align: center;
	}

	.empty :global(svg) {
		inline-size: 36px;
		block-size: 36px;
		margin-block-end: 8px;
		color: var(--accent-text);
	}

	.pending {
		visibility: hidden;
	}

	.empty-title {
		font: italic 400 1.25rem / 1.3 var(--font-display);
	}

	.empty-hint {
		max-inline-size: 40ch;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.hints {
		display: flex;
		gap: 16px;
		padding: 10px 20px;
		box-shadow: inset 0 1px 0 var(--hairline);
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.hints span {
		display: inline-flex;
		align-items: center;
		gap: 6px;
	}

	/* Touch: no keyboard to hint at, and the close control is an icon. */
	@media (width < 600px), (pointer: coarse) {
		.hints,
		.shortcut,
		.esc-key {
			display: none;
		}

		.esc :global(.esc-icon) {
			display: block;
		}
	}
</style>
