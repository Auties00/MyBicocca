<script lang="ts">
	import { MediaQuery } from 'svelte/reactivity';
	import { m } from '#lib/paraglide/messages.js';
	import {
		palettes,
		theme,
		themeModes,
		type Palette,
		type ThemeMode
	} from '#lib/ui/theme.svelte.ts';
	import { haptics } from '#lib/ui/haptics.svelte.ts';
	import { modeLabels, paletteNames } from '../../labels.ts';
	import ChoiceCard from '../ChoiceCard.svelte';
	import ChoiceGroup from '../ChoiceGroup.svelte';
	import SectionPage from '../SectionPage.svelte';
	import ThemePreview from './ThemePreview.svelte';

	const paletteDescriptions: Record<Palette, () => string> = {
		bicocca: m.settings_palette_bicocca_description,
		oceano: m.settings_palette_oceano_description,
		bosco: m.settings_palette_bosco_description
	};

	const deviceDark = new MediaQuery('(prefers-color-scheme: dark)', false);

	const themeDescription = $derived(
		deviceDark.current ? m.settings_theme_description_dark() : m.settings_theme_description_light()
	);

	/** Applies the choice at once, revealing the new theme from the card that was picked. */
	function pick(next: { mode?: ThemeMode; palette?: Palette }, card: HTMLElement) {
		haptics.tap();
		theme.set(next, card);
	}
</script>

<SectionPage title={m.settings_appearance_title()} subtitle={m.settings_appearance_page_subtitle()}>
	<ChoiceGroup title={m.settings_theme_title()} description={themeDescription}>
		{#each themeModes as mode (mode)}
			<ChoiceCard
				name="theme-mode"
				value={mode}
				checked={theme.mode === mode}
				label={modeLabels[mode]()}
				onselect={(value, card) => pick({ mode: value }, card)}
			>
				{#snippet preview()}
					{#if mode === 'system'}
						<span class="split">
							<ThemePreview palette={theme.palette} scheme="light" />
							<span class="half"><ThemePreview palette={theme.palette} scheme="dark" /></span>
						</span>
					{:else}
						<ThemePreview palette={theme.palette} scheme={mode} />
					{/if}
				{/snippet}
			</ChoiceCard>
		{/each}
	</ChoiceGroup>

	<ChoiceGroup title={m.settings_palette_title()} description={m.settings_palette_description()}>
		{#each palettes as palette (palette)}
			<ChoiceCard
				name="theme-palette"
				value={palette}
				checked={theme.palette === palette}
				label={paletteNames[palette]}
				translateLabel={false}
				description={paletteDescriptions[palette]()}
				onselect={(value, card) => pick({ palette: value }, card)}
			>
				{#snippet preview()}
					<ThemePreview {palette} scheme={theme.resolved} />
				{/snippet}
			</ChoiceCard>
		{/each}
	</ChoiceGroup>
</SectionPage>

<style>
	/* "System": the light preview with the dark one over its lower-right half. */
	.split {
		display: grid;
	}

	.split > :global(*) {
		grid-area: 1 / 1;
	}

	.half {
		clip-path: polygon(100% 0, 100% 100%, 0 100%);
	}
</style>
