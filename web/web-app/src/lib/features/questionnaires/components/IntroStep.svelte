<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import IconPlay from '~icons/ph/play-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import BeforeYouStart from './BeforeYouStart.svelte';

	interface Props {
		/** Undefined while the questionnaire loads: the screen is drawn, the start held back. */
		anonymous: boolean | undefined;
		online: boolean;
		backHref: ResolvedPathname;
		onstart: () => void;
	}

	let { anonymous, online, backHref, onstart }: Props = $props();
</script>

<!-- Reached without "Fill in" (a reload, a link): nothing starts until the student says so. -->
<section
	class="intro"
	aria-labelledby="questionnaire-intro"
	aria-busy={anonymous === undefined || undefined}
>
	<div class="lead">
		<h2 class="title" id="questionnaire-intro" tabindex="-1" data-step-heading>
			{m.questionnaires_intro_title()}
		</h2>
		<p class="body">{m.questionnaires_intro_body()}</p>
	</div>
	<BeforeYouStart {anonymous} bare />
	{#if !online}<p class="offline">{m.questionnaires_offline_hint()}</p>{/if}
	<div class="actions">
		<Button href={backHref}>{m.questionnaires_back_to_units()}</Button>
		<Button
			variant="solid"
			icon={IconPlay}
			disabled={!online || anonymous === undefined}
			onclick={onstart}
		>
			{m.questionnaires_intro_start()}
		</Button>
	</div>
</section>

<style>
	.intro {
		display: grid;
		gap: var(--sp-6);
		padding: var(--sp-6);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.lead {
		display: grid;
		gap: var(--sp-2);
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		outline: none;
	}

	.body {
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.offline {
		color: var(--warning);
		font: var(--type-ui);
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		justify-content: flex-end;
		gap: 8px;
	}

	/* On a phone the two actions stack full width, the start on top, under the thumb. */
	@container compile (width < 440px) {
		.intro {
			padding: var(--sp-5);
		}

		.actions {
			display: grid;
		}

		.actions > :global(:last-child) {
			order: -1;
		}
	}

	@supports (corner-shape: squircle) {
		.intro {
			corner-shape: squircle;
			border-radius: calc(var(--r-xl) * 1.5);
		}
	}
</style>
