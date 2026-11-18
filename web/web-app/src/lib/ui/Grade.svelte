<script lang="ts" module>
	import IconCheck from '~icons/ph/check-bold';
	import IconDoor from '~icons/ph/door-open';
	import IconUserMinus from '~icons/ph/user-minus';
	import IconX from '~icons/ph/x-bold';
	import type { ExamGrade } from '#lib/data/esse3/grades.ts';
	import type { Icon } from './types.ts';

	/** The outcomes that are a word rather than a number, each drawn with a glyph. */
	export type WordGrade = Extract<
		ExamGrade['kind'],
		'passed' | 'not_passed' | 'withdrew' | 'absent'
	>;

	const GLYPHS: Record<WordGrade, Icon> = {
		passed: IconCheck,
		not_passed: IconX,
		withdrew: IconDoor,
		absent: IconUserMinus
	};

	export function gradeGlyph(kind: WordGrade): Icon {
		return GLYPHS[kind];
	}

	export type GradeTone = 'plain' | 'gold' | 'success' | 'danger' | 'quiet';

	/**
	 * Gold is for honours alone; a failure is flagged; an absence or a withdrawal is no judgement,
	 * so it stays quiet.
	 */
	export function gradeTone(grade: ExamGrade): GradeTone {
		switch (grade.kind) {
			case 'numeric':
				return grade.cumLaude ? 'gold' : 'plain';
			case 'passed':
				return 'success';
			case 'not_passed':
				return 'danger';
			case 'absent':
			case 'withdrew':
				return 'quiet';
			default:
				return 'plain';
		}
	}
</script>

<script lang="ts">
	import { formatGrade } from '#lib/format/grades.ts';

	interface Props {
		grade: ExamGrade;
		/** tile: a square that leads a row or a card; inline: the figure in a line, as tables set it. */
		variant?: 'tile' | 'inline';
		/** The tile's size: sm in rows, md on cards. */
		size?: 'sm' | 'md';
		/**
		 * What screen readers hear in place of the figure: by default the grade spelled out ("30 e
		 * lode", not "30L"); a sentence that frames it; false where the row says it already.
		 */
		spoken?: string | false;
		/** A grade the student refused: shown, crossed out. */
		struck?: boolean;
	}

	let { grade, variant = 'inline', size = 'sm', spoken, struck = false }: Props = $props();

	const tone = $derived(gradeTone(grade));
	const said = $derived(spoken ?? formatGrade(grade, { full: true }));
</script>

<span class={['grade', variant, variant === 'tile' && size, tone, { struck }]}>
	{#if grade.kind === 'numeric'}
		<span class="numeral" aria-hidden="true">
			{grade.value}{#if grade.cumLaude}<span class="laude">L</span>{/if}
		</span>
	{:else if grade.kind === 'unknown'}
		<span class="numeral" aria-hidden="true">—</span>
	{:else if variant === 'tile'}
		{@const Glyph = gradeGlyph(grade.kind)}
		<Glyph class="glyph" aria-hidden="true" />
		{#if size === 'md'}<span class="short" aria-hidden="true">{formatGrade(grade)}</span>{/if}
	{:else}
		<span class="word" aria-hidden="true">{formatGrade(grade)}</span>
	{/if}
	{#if said}<span class="sr-only">{said}</span>{/if}
</span>

<style>
	/* A list-level figure: the UI face with tabular digits, so the page's title stays its serif. */
	.numeral {
		font-family: var(--font-ui);
		font-weight: 650;
		line-height: 1;
		letter-spacing: -0.02em;
		font-variant-numeric: lining-nums tabular-nums;
	}

	.laude {
		margin-inline-start: 1px;
		font-size: 0.62em;
		font-weight: 600;
	}

	.inline {
		display: inline-flex;
		align-items: baseline;
		font: 600 1.0625rem / 1.2 var(--font-ui);
		white-space: nowrap;
	}

	.inline .numeral {
		font-size: inherit;
	}

	.inline .laude {
		font-size: 0.75em;
	}

	.inline.gold {
		color: var(--gold);
	}

	.inline.success {
		color: var(--success);
	}

	.inline.danger {
		color: var(--danger);
	}

	.inline.quiet {
		color: var(--text-muted);
		font-weight: 500;
	}

	.word {
		font-size: 0.9375rem;
	}

	.tile {
		--size: 44px;
		display: inline-grid;
		flex: none;
		place-items: center;
		place-content: center;
		gap: 1px;
		inline-size: var(--size);
		block-size: var(--size);
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		color: var(--text);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.tile .numeral {
		font-size: 1.3125rem;
	}

	.tile.md {
		--size: 56px;
		border-radius: var(--r-md);
	}

	.tile.md .numeral {
		font-size: 1.625rem;
	}

	.tile.gold {
		background: var(--gold-container);
		color: var(--on-gold-container);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, var(--gold) 28%, transparent);
	}

	.tile.success {
		background: var(--success-container);
		color: var(--on-success-container);
		box-shadow: none;
	}

	.tile.danger {
		background: var(--danger-container);
		color: var(--on-danger-container);
		box-shadow: none;
	}

	.tile.quiet {
		color: var(--text-muted);
	}

	.tile.struck {
		background: transparent;
		color: var(--text-subtle);
	}

	.struck .numeral {
		text-decoration: line-through 2px color-mix(in oklch, var(--text-muted) 70%, transparent);
	}

	.tile :global(.glyph) {
		inline-size: 20px;
		block-size: 20px;
	}

	.tile.md :global(.glyph) {
		inline-size: 18px;
		block-size: 18px;
	}

	.short {
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: 0.02em;
	}

	@supports (corner-shape: squircle) {
		.tile {
			corner-shape: squircle;
			border-radius: calc(var(--r-sm) * 1.6);
		}

		.tile.md {
			border-radius: calc(var(--r-md) * 1.5);
		}
	}
</style>
