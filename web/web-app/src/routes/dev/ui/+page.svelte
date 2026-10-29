<script lang="ts">
	import { resolve } from '$app/paths';
	import IconMonitor from '~icons/ph/monitor';
	import IconSun from '~icons/ph/sun';
	import IconMoon from '~icons/ph/moon';
	import type { AccountModel } from '#lib/shell/account.ts';
	import AppShell from '#lib/shell/AppShell.svelte';
	import { shellHref } from '#lib/shell/nav.ts';
	import { palettes, theme, type Palette, type ThemeMode } from '#lib/ui/theme.svelte.ts';
	import Chip from '#lib/ui/Chip.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Actions from './Actions.svelte';
	import DataDisplay from './DataDisplay.svelte';
	import Feedback from './Feedback.svelte';
	import Forms from './Forms.svelte';
	import Foundations from './Foundations.svelte';
	import Layouts from './Layouts.svelte';
	import Overlays from './Overlays.svelte';
	import Records from './Records.svelte';

	const wait = () => new Promise<void>((resolve) => setTimeout(resolve, 700));

	let activeId = $state('marco');
	let careers = $state<Record<string, number>>({ marco: 1, giulia: 3 });

	const people = [
		{
			id: 'marco',
			name: 'Marco Rossi',
			email: 'm.rossi99@campus.unimib.it',
			careers: [
				{
					id: 1,
					title: 'Informatica',
					detail: 'Matricola 900123 · A.A. 2025',
					level: { short: 'L', full: 'Triennale' }
				},
				{
					id: 2,
					title: 'Data Science',
					detail: 'Matricola 912345 · A.A. 2028',
					level: { short: 'LM', full: 'Magistrale' },
					status: { label: 'Ipotesi', tone: 'info' as const }
				}
			]
		},
		{
			id: 'giulia',
			name: 'Giulia Bianchi',
			email: 'g.bianchi12@campus.unimib.it',
			careers: [
				{
					id: 3,
					title: 'Psicologia',
					detail: 'Matricola 845210 · A.A. 2022',
					level: { short: 'L', full: 'Triennale' },
					status: { label: 'Conclusa', tone: 'gold' as const }
				}
			]
		}
	];

	const account: AccountModel = $derived({
		accounts: [...people]
			.sort((a, b) => Number(b.id === activeId) - Number(a.id === activeId))
			.map((p) => ({ ...p, selectedCareerId: careers[p.id] })),
		activeId,
		switchAccount: async (id) => {
			await wait();
			activeId = id;
		},
		selectCareer: async (accountId, careerId) => {
			await wait();
			careers[accountId] = careerId;
		},
		signOut: async () => {
			await wait();
		},
		addAccountHref: shellHref('login')
	});

	const sections = [
		['foundations', 'Fondamenta'],
		['actions', 'Azioni'],
		['data', 'Dati'],
		['feedback', 'Stati'],
		['overlays', 'Sovrapposizioni'],
		['forms', 'Moduli'],
		['layouts', 'Elenco e dettaglio'],
		['records', 'Prenotazioni ed esiti']
	] as const;

	/** The reveal grows from the chosen segment. */
	function segment(event: Event & { currentTarget: HTMLInputElement }) {
		return event.currentTarget.closest('label') ?? undefined;
	}

	const paletteLabels: Record<Palette, string> = {
		bicocca: 'Bicocca',
		oceano: 'Oceano',
		bosco: 'Bosco'
	};
</script>

<svelte:head>
	<title>Componenti · MyBicocca</title>
</svelte:head>

<AppShell {account}>
	<Page width="dashboard">
		<PageHeader
			eyebrow="Carta · sistema di design"
			title="Componenti"
			subtitle="Ogni componente e stato del kit, con testi realistici. Prova tema, colori e tastiera (⌘K, ?, g poi una lettera)."
		>
			{#snippet actions()}
				<SegmentedControl
					label="Tema"
					value={theme.mode}
					onchange={(mode: ThemeMode, event) => theme.set({ mode }, segment(event))}
					options={[
						{ value: 'system', label: 'Sistema', icon: IconMonitor },
						{ value: 'light', label: 'Chiaro', icon: IconSun },
						{ value: 'dark', label: 'Scuro', icon: IconMoon }
					]}
				/>
				<SegmentedControl
					label="Colori"
					value={theme.palette}
					onchange={(palette: Palette, event) => theme.set({ palette }, segment(event))}
					options={palettes.map((p) => ({ value: p, label: paletteLabels[p] }))}
				/>
			{/snippet}
			{#snippet meta()}
				<nav class="toc" aria-label="Sezioni">
					{#each sections as [id, label] (id)}
						<Chip size="sm" href={resolve(`dev/ui#${id}`)}>{label}</Chip>
					{/each}
				</nav>
			{/snippet}
		</PageHeader>

		<Foundations />
		<Actions />
		<DataDisplay />
		<Feedback />
		<Overlays />
		<Forms />
		<Layouts />
		<Records />
	</Page>
</AppShell>

<style>
	.toc {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
	}
</style>
