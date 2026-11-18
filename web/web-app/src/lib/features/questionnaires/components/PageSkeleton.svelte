<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';

	interface Props {
		/** Announced while the page loads. */
		label: string;
	}

	let { label }: Props = $props();
</script>

<!--
	Shaped like the first page every questionnaire opens on: one section, and the attendance
	question with its two answers.
-->
<div class="skeleton" role="status" aria-label={label}>
	<span class="section-title"><Skeleton variant="text" width="7rem" /></span>
	<div class="card" aria-hidden="true">
		<div class="head">
			<Skeleton variant="circle" width="26px" />
			<div class="words">
				<span class="text">
					<Skeleton variant="text" width="92%" />
					<span class="narrow"><Skeleton variant="text" width="48%" /></span>
				</span>
				<span class="note">
					<Skeleton variant="text" width="78%" />
					<span class="narrow"><Skeleton variant="text" width="30%" /></span>
				</span>
			</div>
		</div>
		<div class="options">
			{#each { length: 2 }, option (option)}
				<div class="option"><Skeleton height="100%" radius="var(--r-md)" /></div>
			{/each}
		</div>
	</div>
</div>

<style>
	/* Every box and line as tall as what it stands for, so nothing moves when the page comes. */
	.skeleton {
		display: grid;
		gap: var(--sp-3);
	}

	.section-title {
		display: grid;
		margin-block-end: var(--sp-1);
		font: var(--type-title-m);
	}

	.card {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		container-type: inline-size;
	}

	.head {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: start;
		gap: var(--sp-3);
	}

	.words {
		display: grid;
		gap: 4px;
		padding-block-start: 2px;
	}

	.text,
	.note {
		display: grid;
	}

	.text {
		font: var(--type-body-strong);
	}

	.note {
		font: var(--type-meta);
	}

	.options {
		display: grid;
		gap: 6px;
	}

	.option {
		display: grid;
		block-size: max(var(--hit-min), calc(20px + 0.875rem * 1.43));
	}

	/* The question and its note fit one line each from here. */
	@container (width >= 480px) {
		.narrow {
			display: none;
		}
	}
</style>
