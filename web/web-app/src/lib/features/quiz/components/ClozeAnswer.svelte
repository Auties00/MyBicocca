<script lang="ts">
	import { untrack } from 'svelte';
	import type { Attachment } from 'svelte/attachments';
	import IconCheck from '~icons/ph/check-bold';
	import IconMinus from '~icons/ph/minus-bold';
	import IconX from '~icons/ph/x-bold';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import { m } from '#lib/paraglide/messages.js';
	import { GAP, splitGaps, type Gap, type Response } from '../questions.ts';

	interface Props {
		/** The question's HTML with a {@link GAP} marker for each gap. */
		html: string;
		gaps: readonly Gap[];
		response: Response;
		review: boolean;
		disabled?: boolean;
		onanswer?: (name: string, value: string) => void;
	}

	let { html, gaps, response, review, disabled = false, onanswer }: Props = $props();

	const MARK_LABELS = {
		correct: m.quiz_verdict_correct,
		partial: m.quiz_verdict_partial,
		incorrect: m.quiz_verdict_wrong
	};

	function control(gap: Gap, index: number, glyphs: Element): HTMLElement {
		const label = gap.label ?? m.quiz_gap_label({ number: index + 1 });
		const value = response[gap.name] ?? gap.value;
		let element: HTMLInputElement | HTMLSelectElement;
		if (gap.kind === 'menu') {
			const select = document.createElement('select');
			for (const option of gap.options) {
				const item = document.createElement('option');
				item.value = option.value;
				item.textContent = option.value === gap.blank && !option.label ? '—' : option.label || '—';
				select.append(item);
			}
			select.value = value;
			select.disabled = review || disabled;
			select.addEventListener('change', () => onanswer?.(gap.name, select.value));
			element = select;
		} else {
			const input = document.createElement('input');
			input.type = 'text';
			input.value = value;
			input.autocomplete = 'off';
			input.spellcheck = false;
			if (gap.numeric) input.inputMode = 'decimal';
			input.style.setProperty('--chars', String(Math.min(24, Math.max(6, gap.size ?? 10))));
			input.readOnly = review;
			input.disabled = disabled && !review;
			input.addEventListener('input', () => onanswer?.(gap.name, input.value));
			element = input;
		}
		element.className = `gap ${gap.kind}`;
		element.setAttribute('aria-label', label);
		if (!review || gap.mark === null) return element;
		const wrap = document.createElement('span');
		wrap.className = `marked ${gap.mark}`;
		const mark = document.createElement('span');
		mark.className = 'mark';
		const glyph = glyphs.querySelector(`[data-mark="${gap.mark}"] svg`);
		if (glyph) mark.append(glyph.cloneNode(true));
		mark.setAttribute('role', 'img');
		mark.setAttribute('aria-label', MARK_LABELS[gap.mark]());
		wrap.append(element, mark);
		return wrap;
	}

	/*
	 * The gaps go where the markers are, inside the sanitized text, so sentences and tables read
	 * on. They are built once: afterwards they keep their own value, as the student types.
	 */
	const place: Attachment<HTMLElement> = (root) =>
		untrack(() => {
			const glyphs = root.querySelector('.glyphs') ?? root;
			const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
			const nodes: Text[] = [];
			while (walker.nextNode()) {
				const node = walker.currentNode as Text;
				if (node.data.includes(GAP)) nodes.push(node);
			}
			for (const node of nodes) {
				const parts = splitGaps(node.data).map((part) => {
					if (typeof part === 'string') return document.createTextNode(part);
					const gap = gaps[part];
					return gap ? control(gap, part, glyphs) : document.createTextNode('');
				});
				node.replaceWith(...parts);
			}
		});

	const lock: Attachment<HTMLElement> = (root) => {
		for (const element of root.querySelectorAll<HTMLInputElement | HTMLSelectElement>('.gap')) {
			element.disabled = review ? element instanceof HTMLSelectElement : disabled;
		}
	};
</script>

<div class={['cloze', { review }]} {@attach place} {@attach lock}>
	<!-- The marks a review gives each gap, copied next to it from here. -->
	<span class="glyphs" hidden>
		<span data-mark="correct"><IconCheck aria-hidden="true" /></span>
		<span data-mark="partial"><IconMinus aria-hidden="true" /></span>
		<span data-mark="incorrect"><IconX aria-hidden="true" /></span>
	</span>
	<TeacherHtml {html} />
</div>

<style>
	.cloze {
		min-inline-size: 0;
	}

	/* Lines with a gap grow by its margin; the others keep the text's own spacing. */
	.cloze :global(.gap) {
		box-sizing: border-box;
		min-block-size: 36px;
		margin: 3px 2px;
		padding: 4px 10px;
		border: 0;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-body);
		line-height: 1.4;
		vertical-align: middle;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	.cloze :global(.gap.text) {
		inline-size: calc(var(--chars, 10) * 1ch + 24px);
		max-inline-size: 100%;
	}

	.cloze :global(.gap.menu) {
		max-inline-size: 100%;
		padding-inline-end: 6px;
		cursor: pointer;
	}

	@media (hover: hover) {
		.cloze :global(.gap:not(:disabled, [readonly]):hover) {
			box-shadow: inset 0 0 0 1px var(--border-strong);
		}
	}

	.cloze :global(.gap:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	.cloze :global(.gap:disabled),
	.cloze :global(.gap[readonly]) {
		opacity: 1;
		cursor: default;
	}

	.cloze :global(.marked) {
		display: inline-flex;
		align-items: center;
		gap: 4px;
		white-space: nowrap;
	}

	.cloze :global(.marked.correct .gap) {
		background: var(--success-container);
		box-shadow: inset 0 0 0 1.5px var(--success);
		color: var(--on-success-container);
	}

	.cloze :global(.marked.incorrect .gap) {
		background: var(--danger-container);
		box-shadow: inset 0 0 0 1.5px var(--danger);
		color: var(--on-danger-container);
	}

	.cloze :global(.marked.partial .gap) {
		background: var(--warning-container);
		box-shadow: inset 0 0 0 1.5px var(--warning);
		color: var(--on-warning-container);
	}

	.cloze :global(.mark) {
		display: inline-grid;
		place-items: center;
		inline-size: 20px;
		block-size: 20px;
		border-radius: 50%;
		color: oklch(100% 0 0);
	}

	.cloze :global(.mark svg) {
		inline-size: 12px;
		block-size: 12px;
	}

	.cloze :global(.correct .mark) {
		background: var(--success-solid);
	}

	.cloze :global(.incorrect .mark) {
		background: var(--danger-solid);
	}

	.cloze :global(.partial .mark) {
		background: var(--warning-solid);
		color: var(--on-warning-solid);
	}
</style>
