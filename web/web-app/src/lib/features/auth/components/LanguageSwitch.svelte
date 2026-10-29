<script lang="ts">
	import IconTranslate from '~icons/ph/translate';
	import { language } from '#lib/features/settings/language.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale, locales, type Locale } from '#lib/paraglide/runtime.js';

	const current = getLocale();

	/** Each language in itself, so a student who can't read the current one still finds theirs. */
	function endonym(locale: Locale): string {
		const name = new Intl.DisplayNames([locale], { type: 'language' }).of(locale) ?? locale;
		return name.charAt(0).toLocaleUpperCase(locale) + name.slice(1);
	}
</script>

<div class="switch" role="group" aria-label={m.auth_language()}>
	<IconTranslate aria-hidden="true" />
	{#each locales as locale (locale)}
		<button
			type="button"
			class="option"
			lang={locale}
			aria-pressed={locale === current}
			onclick={() => locale !== current && language.set(locale)}
		>
			{endonym(locale)}
		</button>
	{/each}
</div>

<style>
	.switch {
		display: inline-flex;
		align-items: center;
		gap: 2px;
		color: var(--text-muted);
	}

	.switch > :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		margin-inline-end: 4px;
	}

	.option {
		position: relative;
		padding: 6px 10px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-meta);
		font-weight: 500;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	/* Keeps the touch target at 44 px high around the 30 px pill. */
	.option::after {
		content: '';
		position: absolute;
		inset: -7px -2px;
	}

	.option[aria-pressed='true'] {
		background: var(--container);
		color: var(--on-container);
		font-weight: 600;
		cursor: default;
	}

	@media (hover: hover) {
		.option[aria-pressed='false']:hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.option:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}
</style>
