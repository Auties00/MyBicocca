<script lang="ts">
	import type { Snippet } from 'svelte';
	import { goto } from '$app/navigation';
	import IconCircleHalf from '~icons/ph/circle-half';
	import IconPalette from '~icons/ph/palette';
	import IconSidebar from '~icons/ph/sidebar-simple';
	import IconKeyboard from '~icons/ph/keyboard';
	import IconUserPlus from '~icons/ph/user-plus';
	import { m } from '#lib/paraglide/messages.js';
	import { PageChrome, setPageChrome } from '#lib/ui/pageChrome.svelte.ts';
	import { internalHref } from '#lib/ui/types.ts';
	import type { AccountModel } from './account.ts';
	import { catalogProvider, keywordList, type CatalogItem } from './catalog.ts';
	import CommandPalette from './CommandPalette.svelte';
	import NavSidebar from './NavSidebar.svelte';
	import { shellHref } from './nav.ts';
	import OfflineBand, { type BandStatus } from './OfflineBand.svelte';
	import { setupPageTransitions } from './pageTransitions.ts';
	import { registerPaletteProvider } from './palette.svelte.ts';
	import { shell } from './shell.svelte.ts';
	import ShortcutsDialog from './ShortcutsDialog.svelte';
	import { installShortcuts } from './shortcuts.ts';
	import TabBar from './TabBar.svelte';
	import { palettes, theme, themeModes, type Palette } from '#lib/ui/theme.svelte.ts';
	import TopBar from './TopBar.svelte';
	import { compact, expanded } from '#lib/ui/media.ts';

	interface Props {
		children: Snippet;
		account: AccountModel;
	}

	let { children, account }: Props = $props();

	const chrome = setPageChrome(new PageChrome());
	const mode = $derived(
		compact.current ? 'tabs' : expanded.current && !shell.sidebarCollapsed ? 'sidebar' : 'rail'
	);
	const tabBar = $derived(compact.current && !chrome.immersive && !chrome.tabBarHidden);

	let band = $state<BandStatus>(navigator.onLine ? 'online' : 'offline');
	let bandHeight = $state(0);

	setupPageTransitions();

	$effect(() =>
		installShortcuts({
			openPalette: () => (shell.paletteOpen = true),
			toggleSidebar: () => expanded.current && shell.toggleSidebar(),
			openShortcuts: () => (shell.shortcutsOpen = true),
			go: (path) => goto(shellHref(path))
		})
	);

	const themeLabels = {
		system: m.shell_theme_system,
		light: m.shell_theme_light,
		dark: m.shell_theme_dark
	};

	const themeKeywords = {
		system: m.shell_kw_theme_system,
		light: m.shell_kw_theme_light,
		dark: m.shell_kw_theme_dark
	};

	const paletteLabels: Record<Palette, () => string> = {
		bicocca: m.shell_palette_bicocca,
		oceano: m.shell_palette_oceano,
		bosco: m.shell_palette_bosco
	};

	function actions(): CatalogItem[] {
		const group = m.shell_palette_actions();
		const paletteKeywords = keywordList(m.shell_kw_palette);
		const items: CatalogItem[] = [
			...themeModes
				.filter((option) => option !== theme.mode)
				.map((option) => ({
					id: `action:theme-${option}`,
					title: `${m.shell_theme_label()}: ${themeLabels[option]()}`,
					subtitle: m.shell_action_theme(),
					icon: IconCircleHalf,
					group,
					run: () => theme.set({ mode: option }),
					keywords: keywordList(themeKeywords[option])
				})),
			...palettes
				.filter((option) => option !== theme.palette)
				.map((option) => ({
					id: `action:palette-${option}`,
					title: `${m.shell_palette_colors()}: ${paletteLabels[option]()}`,
					subtitle: m.shell_action_theme(),
					icon: IconPalette,
					group,
					run: () => theme.set({ palette: option }),
					keywords: paletteKeywords
				}))
		];
		items.push({
			id: 'action:shortcuts',
			title: m.shell_action_shortcuts(),
			icon: IconKeyboard,
			group,
			shortcut: ['?'],
			run: () => (shell.shortcutsOpen = true)
		});
		if (expanded.current) {
			items.push({
				id: 'action:sidebar',
				title: m.shell_action_sidebar(),
				icon: IconSidebar,
				group,
				shortcut: ['mod', 'b'],
				run: () => shell.toggleSidebar()
			});
		}
		const add = internalHref(account.addAccountHref);
		if (add) {
			items.push({
				id: 'action:add-account',
				title: m.shell_action_add_account(),
				icon: IconUserPlus,
				group,
				href: add
			});
		}
		return items;
	}

	$effect(() => registerPaletteProvider(catalogProvider(actions)));
</script>

<a class="skip" href="#main">{m.shell_skip_to_content()}</a>

<div
	class={[
		'shell',
		mode,
		{ immersive: chrome.immersive, tabbar: tabBar, offline: band !== 'online' }
	]}
	style:--band-h="{bandHeight}px"
>
	{#if !compact.current && !chrome.immersive}
		<NavSidebar
			mode={mode === 'sidebar' ? 'sidebar' : 'rail'}
			collapsible={expanded.current}
			{account}
		/>
	{/if}
	<div class="column">
		{#if !chrome.immersive}
			<TopBar {chrome} compact={compact.current} {account} />
		{/if}
		<main id="main" class="main" tabindex="-1">
			{@render children()}
		</main>
	</div>
	{#if tabBar}
		<TabBar />
	{/if}
	<OfflineBand bind:status={band} onheight={(height) => (bandHeight = height)} />
</div>

<CommandPalette />
<ShortcutsDialog />

<style>
	.skip {
		position: fixed;
		inset: 8px auto auto 8px;
		z-index: calc(var(--z-band) + 1);
		padding: 10px 16px;
		border-radius: var(--r-sm);
		background: var(--accent);
		color: var(--on-accent);
		font: var(--type-ui-strong);
		text-decoration: none;
		transform: translateY(calc(-100% - 16px));
	}

	.skip:focus-visible {
		transform: none;
	}

	/*
	 * What the shell's floating bars cover at the viewport's edges, for pages that float UI over
	 * them or run under them: the top bar (always there on phones, appearing over the content once
	 * the title scrolls away from 600 px), the phone tab bar with its gap to the screen's edge, and,
	 * while the offline band shows, the distance from the top to just below it.
	 */
	.shell {
		--shell-inset-top: calc(var(--topbar-h) + var(--safe-top));
		--shell-inset-bottom: var(--safe-bottom);
		--shell-band: 0px;
		--band-top: calc(var(--safe-top) + 10px);
		--nav-w: 0px;
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		min-block-size: 100dvh;
	}

	.tabbar {
		--shell-inset-bottom: calc(var(--tabbar-h) + max(10px, var(--safe-bottom)));
	}

	.tabs:not(.immersive) {
		--band-top: calc(var(--shell-inset-top) + 6px);
	}

	.rail {
		--nav-w: var(--rail-w);
	}

	.sidebar {
		--nav-w: var(--sidebar-w);
	}

	.immersive,
	.shell:not(.tabs):has(:global([data-page-fill='viewport'])) {
		--shell-inset-top: var(--safe-top);
	}

	.immersive {
		--nav-w: 0px;
	}

	/* A page's own offline notice (`data-offline-notice`) replaces the band (see OfflineBand). */
	.offline:not(:has(:global([data-offline-notice]))) {
		--shell-band: calc(var(--band-top) + var(--band-h) + 6px);
	}

	.tabs,
	.immersive {
		grid-template-columns: minmax(0, 1fr);
	}

	.column {
		display: flex;
		flex-direction: column;
		min-inline-size: 0;
	}

	.main {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-inline-size: 0;
		outline: none;
		padding-block-start: var(--sp-2);
	}

	.tabs .main {
		padding-block: 0 var(--shell-inset-bottom);
	}

	.tabbar .main {
		padding-block-end: calc(var(--shell-inset-bottom) + 18px);
	}

	.immersive .main {
		padding: 0;
	}

	/*
	 * <Page fill>: the content area is exactly the viewport's height and nothing pads for the tab
	 * bar; 'viewport' also drops the top padding and, on phones, runs under the top bar.
	 */
	@media screen {
		.shell:has(:global([data-page-fill])) .column {
			block-size: 100dvh;
		}

		.shell:has(:global([data-page-fill])) .main {
			min-block-size: 0;
			padding-block-end: 0;
		}

		.shell:has(:global([data-page-fill='viewport'])) .main {
			padding-block-start: 0;
		}
	}

	/*
	 * Page transitions animate the content pane, or only the innermost detail pane of a list-detail
	 * page: nested list-details (Services around a service's own) can't share the name.
	 */
	.main:not(:has(:global([data-vt-page]))),
	.main :global([data-vt-page]:not(:has([data-vt-page]))) {
		view-transition-name: page;
	}

	:global(:root:has(.shell.tabbar)) {
		--toast-offset: calc(var(--tabbar-h) + 22px);
	}

	@media print {
		.skip {
			display: none;
		}

		.shell {
			display: block;
			min-block-size: 0;
		}

		.main {
			padding: 0;
		}
	}
</style>
