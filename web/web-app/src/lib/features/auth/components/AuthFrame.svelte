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
		<span class="brand view-enter"><Wordmark /></span>
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
	 * The app's own frame: from 1024 px a sidebar-wide pane holds the wordmark and the language, as
	 * the shell holds its wordmark and account, and the page sits in the pane beside it. Below that
	 * the wordmark is a bar and the page fills the width. The page names its column with
	 * data-auth-column (narrow, or wide for the career picker).
	 */
	.frame {
		--column: 440px;
		display: grid;
		grid-template-rows: auto 1fr auto;
		min-block-size: 100dvh;
		padding-inline: max(var(--gutter), env(safe-area-inset-left))
			max(var(--gutter), env(safe-area-inset-right));
		background: var(--bg);
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
		min-block-size: 44px;
		padding-block: calc(var(--safe-top) + 20px) 8px;
	}

	.brand {
		display: inline-flex;
	}

	.content {
		display: grid;
		align-items: start;
		inline-size: 100%;
		max-inline-size: var(--column);
		margin-inline: auto;
		padding-block: 16px 32px;
		outline: none;
	}

	.foot {
		display: flex;
		justify-content: center;
		padding-block: 8px calc(var(--safe-bottom) + 16px);
	}

	@media (width >= 1024px) {
		.frame {
			grid-template-columns: var(--sidebar-w) minmax(0, 1fr);
			padding-inline: 0;
		}

		/* The sidebar's edge, running the page's full height as the shell's does. */
		.frame::before {
			content: '';
			grid-column: 1;
			grid-row: 1 / -1;
			box-shadow: inset -1px 0 0 var(--hairline);
		}

		.bar,
		.foot {
			grid-column: 1;
			max-inline-size: none;
			margin-inline: 0;
			padding-inline: 20px;
		}

		.bar {
			grid-row: 1;
			padding-block: calc(var(--safe-top) + 16px) 8px;
		}

		.foot {
			grid-row: 3;
			justify-content: flex-start;
		}

		.content {
			grid-column: 2;
			grid-row: 1 / -1;
			padding: clamp(40px, 14vh, 144px) var(--gutter) 48px;
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

	/* Between sign-in pages, the new content fades in place. */
	:global(:root:active-view-transition-type(auth-step)) .content {
		view-transition-name: auth-page;
	}

	:global(::view-transition-group(auth-page)) {
		animation: none;
	}

	:global(::view-transition-old(auth-page)) {
		opacity: 0;
	}

	:global(::view-transition-new(auth-page)) {
		animation: view-enter var(--view-enter-duration) var(--ease-out);
	}
</style>
