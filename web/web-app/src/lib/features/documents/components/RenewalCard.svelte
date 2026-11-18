<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconClockClockwise from '~icons/ph/clock-clockwise';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { RENEWAL_URL, type Renewal } from '../enrollments.ts';

	interface Props {
		renewal: Exclude<Renewal, { kind: 'none' }>;
	}

	let { renewal }: Props = $props();

	const year = $derived(academicYearLabel(renewal.academicYear));
	const enrolled = $derived(renewal.kind === 'enrolled');
	const titleId = $props.id();
</script>

<!-- The answer to "am I enrolled this year", and the way to the portal when not. -->
<section class={['card', renewal.kind]} aria-labelledby={titleId}>
	<span class="glyph" aria-hidden="true">
		{#if enrolled}<IconCheckCircle />{:else}<IconClockClockwise />{/if}
	</span>
	<div class="body">
		<h2 class="title" id={titleId}>
			{enrolled
				? m.documents_renewal_enrolled_title({ year })
				: m.documents_renewal_due_title({ year })}
		</h2>
		<p class="text">
			{enrolled ? m.documents_renewal_enrolled_body() : m.documents_renewal_due_body()}
		</p>
		{#if !enrolled}
			<div class="action">
				<Button variant="solid" href={RENEWAL_URL} trailingIcon={IconArrowSquareOut}>
					{m.documents_renewal_action()}
				</Button>
			</div>
		{/if}
	</div>
</section>

<style>
	.card {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: var(--sp-3);
		padding: var(--sp-4) var(--sp-5) var(--sp-5) var(--sp-4);
		border-radius: var(--r-lg);
	}

	.enrolled {
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.renewable {
		background: var(--container);
		color: var(--on-container);
	}

	.glyph {
		display: flex;
		padding-block-start: 1px;
	}

	.glyph :global(svg) {
		inline-size: 24px;
		block-size: 24px;
	}

	.enrolled .glyph {
		color: var(--success);
	}

	.renewable .glyph {
		color: var(--accent-text);
	}

	.body {
		display: grid;
		gap: 4px;
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: balance;
	}

	.text {
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	.action {
		margin-block-start: var(--sp-3);
	}

	@supports (corner-shape: squircle) {
		.card {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
