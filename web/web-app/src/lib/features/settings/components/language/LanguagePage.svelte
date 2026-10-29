<script lang="ts">
	import IconGithubLogo from '~icons/ph/github-logo';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { Locale } from '#lib/paraglide/runtime.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import { haptics } from '#lib/ui/haptics.svelte.ts';
	import {
		language,
		languageName,
		systemLocale,
		untranslatedSystemLanguage,
		type LanguageChoice
	} from '../../language.svelte.ts';
	import { REPOSITORY_URL } from '../../releases/releases.ts';
	import Knob from '../Knob.svelte';
	import SectionPage from '../SectionPage.svelte';
	import Flag from './Flag.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Option {
		value: LanguageChoice;
		label: string;
		description: string;
		lang?: string;
	}

	const untranslated = untranslatedSystemLanguage();

	const today = new Date();

	/** What the language changes besides the words: dates and decimals, as the app formats them. */
	function sample(locale: Locale): string {
		return `${formatDate(today, 'medium', locale)} · ${formatNumber(27.45, {}, locale)}`;
	}

	const options: readonly Option[] = [
		{
			value: 'system',
			label: m.settings_language_system(),
			description: m.settings_language_system_description({
				language: languageName(systemLocale())
			})
		},
		{ value: 'it', label: 'Italiano', description: sample('it'), lang: 'it' },
		{ value: 'en', label: 'English', description: sample('en'), lang: 'en' }
	];

	/** The choice being applied: the app reloads in its language once the flag has waved. */
	let pending = $state<LanguageChoice>();
	let timer: ReturnType<typeof setTimeout> | undefined;

	const current = $derived(pending ?? language.choice);

	function select(choice: LanguageChoice) {
		haptics.tap();
		pending = choice;
		clearTimeout(timer);
		timer = setTimeout(() => language.set(choice), reducedMotion.current ? 0 : 600);
	}
</script>

<SectionPage title={m.settings_language_title()} subtitle={m.settings_language_page_subtitle()}>
	<fieldset class="list">
		<legend class="sr-only">{m.settings_language_legend()}</legend>
		{#each options as option (option.value)}
			{@const checked = current === option.value}
			<label class={['row', { checked }]}>
				<input
					class="sr-only"
					type="radio"
					name="language"
					value={option.value}
					{checked}
					onchange={() => select(option.value)}
				/>
				<Flag kind={option.value} waving={pending === option.value} />
				<span class="text">
					<span class="label" lang={option.lang} translate={option.lang ? 'no' : undefined}>
						{option.label}
					</span>
					<span class="description" lang={option.lang}>{option.description}</span>
				</span>
				<Knob {checked} />
			</label>
		{/each}
	</fieldset>

	<p class="note" role="status">
		{pending === undefined || pending === language.choice
			? m.settings_language_note()
			: m.settings_language_switching()}
	</p>

	{#if untranslated}
		<Banner
			tone="neutral"
			icon={IconGithubLogo}
			title={m.settings_language_help_translate({ language: languageName(untranslated) })}
			message={m.settings_language_help_translate_body()}
		>
			{#snippet actions()}
				<Button variant="ghost" href={REPOSITORY_URL}>{m.settings_language_contribute()}</Button>
			{/snippet}
		</Banner>
	{/if}
</SectionPage>

<style>
	.list {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		margin: 0;
		padding: 0;
		border: 0;
	}

	.row {
		display: flex;
		align-items: center;
		gap: 16px;
		min-block-size: 68px;
		padding: 12px 16px;
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
		cursor: pointer;
		-webkit-tap-highlight-color: transparent;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.row:first-of-type {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.row:last-of-type {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	@media (hover: hover) {
		.row:not(.checked):hover {
			background: color-mix(in oklch, var(--surface), var(--text) 4%);
			transition-duration: 0s;
		}
	}

	.row:has(input:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.text {
		display: grid;
		flex: 1;
		gap: 2px;
		min-inline-size: 0;
	}

	.label {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.note {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		padding-inline: 4px;
		margin-block-start: calc(-1 * var(--sp-3));
	}
</style>
