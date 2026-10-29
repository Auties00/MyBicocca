<script lang="ts">
	import type { Snippet } from 'svelte';
	import Wordmark from '#lib/ui/Wordmark.svelte';
	import LanguageSwitch from './LanguageSwitch.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
</script>

<div class="frame">
	<header class="bar">
		<span class="brand"><Wordmark size="lg" /></span>
	</header>
	<main id="main" class="content" tabindex="-1">
		{@render children()}
	</main>
	<footer class="foot">
		<LanguageSwitch />
	</footer>
</div>

<style>
	/*
	 * The wordmark and the footer stand on the page's own column, which the page names with
	 * data-auth-column: narrow (the form alone), wide (the career picker) or spread (the form beside
	 * the cover, from 1024 px).
	 */
	.frame {
		--column: 440px;
		display: grid;
		grid-template-rows: auto 1fr auto;
		min-block-size: 100dvh;
		padding-inline: max(var(--gutter), env(safe-area-inset-left))
			max(var(--gutter), env(safe-area-inset-right));
		/* Crimson ink soaking into the paper from the top corner: a static wash, nothing animates. */
		background:
			radial-gradient(
				ellipse 70% 55% at 0% 0%,
				color-mix(in oklch, var(--c3) 75%, transparent),
				transparent 72%
			),
			var(--bg);
	}

	.frame:has(:global([data-auth-column='wide'])) {
		--column: 560px;
	}

	.bar,
	.foot {
		inline-size: 100%;
		max-inline-size: var(--column);
		margin-inline: auto;
	}

	.bar {
		display: flex;
		align-items: center;
		padding-block: calc(var(--safe-top) + 28px) 8px;
	}

	.brand {
		display: inline-flex;
	}

	/* The wordmark settles in once, when the sign-in pages first open. */
	@media (prefers-reduced-motion: no-preference) {
		.brand {
			animation: brand-in 700ms var(--spring-pop) both;
		}
	}

	@keyframes brand-in {
		from {
			opacity: 0;
			transform: translateY(6px) scale(0.96);
		}
	}

	.content {
		display: grid;
		align-items: center;
		inline-size: 100%;
		max-inline-size: 1180px;
		margin-inline: auto;
		padding-block: 24px 32px;
		outline: none;
	}

	.foot {
		display: flex;
		justify-content: center;
		padding-block: 8px calc(var(--safe-bottom) + 16px);
	}

	@media (width >= 1024px) {
		.bar {
			padding-block-start: calc(var(--safe-top) + 36px);
		}

		.frame:has(:global([data-auth-column='spread'])) {
			--column: 1180px;
		}

		.frame:has(:global([data-auth-column='spread'])) .foot {
			justify-content: flex-end;
		}
	}

	/* Very wide screens give the spread more room, never the text lines. */
	@media (width >= 1800px) {
		.frame:has(:global([data-auth-column='spread'])) {
			--column: 1360px;
		}

		.content {
			max-inline-size: 1360px;
		}
	}

	/*
	 * Into the app: the page cross-fades (Android swaps its root phases with a 500 ms fade) and the
	 * wordmark flies to the shell's, which the shell names during this transition.
	 */
	:global(:active-view-transition-type(auth-enter)::view-transition-old(root)) {
		animation: vt-fade-out 300ms var(--ease-in) both;
	}

	:global(:active-view-transition-type(auth-enter)::view-transition-new(root)),
	:global(:active-view-transition-type(auth-enter)::view-transition-new(page)) {
		animation: vt-fade-in 500ms var(--ease-out) both;
	}

	:global(::view-transition-group(wordmark)) {
		animation-duration: 500ms;
		animation-timing-function: var(--ease-in-out);
	}

	@media (prefers-reduced-motion: no-preference) {
		:global(:root:active-view-transition-type(auth-enter)) .brand {
			view-transition-name: wordmark;
		}
	}

	/*
	 * Between the sign-in pages the content cross-fades, and the wordmark and footer glide to the
	 * new page's column.
	 */
	:global(:root:active-view-transition-type(auth-step)) .content {
		view-transition-name: auth-page;
	}

	:global(:root:active-view-transition-type(auth-step)) .bar {
		view-transition-name: auth-bar;
	}

	:global(:root:active-view-transition-type(auth-step)) .foot {
		view-transition-name: auth-foot;
	}

	:global(::view-transition-old(auth-page)) {
		animation: vt-fade-out 120ms var(--ease-in) both;
	}

	:global(::view-transition-new(auth-page)) {
		animation: vt-fade-in var(--dur-base) var(--ease-out) 60ms both;
	}

	@media (prefers-reduced-motion: no-preference) {
		:global(::view-transition-new(auth-page)) {
			animation:
				vt-fade-in var(--dur-base) var(--ease-out) 60ms both,
				vt-rise var(--dur-slow) var(--spring-standard) both;
		}
	}
</style>
