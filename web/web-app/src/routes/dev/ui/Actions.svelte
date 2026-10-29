<script lang="ts">
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconDownload from '~icons/ph/download-simple';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconTrash from '~icons/ph/trash';
	import IconStar from '~icons/ph/star';
	import IconShare from '~icons/ph/share-network';
	import IconDots from '~icons/ph/dots-three-vertical';
	import IconListBullets from '~icons/ph/list-bullets';
	import IconSquaresFour from '~icons/ph/squares-four';
	import IconMapPin from '~icons/ph/map-pin';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import Chip from '#lib/ui/Chip.svelte';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Tabs from '#lib/ui/Tabs.svelte';
	import { shellHref } from '#lib/shell/nav.ts';
	import Specimen from './Specimen.svelte';

	let saving = $state(false);
	let starred = $state(false);
	let view = $state<'day' | 'week' | 'month'>('week');
	let layout = $state<'list' | 'grid'>('list');
	let tab = $state<'overview' | 'content' | 'quizzes' | 'forums'>('overview');
	let courseTab = $state<'overview' | 'content' | 'quizzes' | 'assignments' | 'forums'>('forums');
	const courseTabs = [
		{ value: 'overview', label: 'Panoramica' },
		{ value: 'content', label: 'Contenuti' },
		{ value: 'quizzes', label: 'Quiz' },
		{ value: 'assignments', label: 'Compiti' },
		{ value: 'forums', label: 'Forum', count: 4 }
	] as const;
	let filters = $state({ lessons: true, exams: false, deadlines: true });

	function save() {
		saving = true;
		setTimeout(() => (saving = false), 1600);
	}
</script>

<Specimen id="actions" title="Azioni" description="Pulsanti, chip, controlli segmentati e schede.">
	<Card title="Pulsanti">
		<div class="stack">
			<div class="wrap">
				<Button variant="solid" icon={IconCalendarPlus}>Prenota appello</Button>
				<Button>Apri calendario</Button>
				<Button variant="outline" icon={IconDownload}>Statino PDF</Button>
				<Button variant="ghost">Annulla</Button>
				<Button variant="danger" icon={IconTrash}>Annulla prenotazione</Button>
			</div>
			<div class="wrap">
				<Button variant="solid" size="lg" loading={saving} onclick={save}
					>Conferma prenotazione</Button
				>
				<Button variant="outline" icon={IconDownload} loading={saving} onclick={save}>
					Statino PDF
				</Button>
				<Button
					size="sm"
					variant="outline"
					href="https://elearning.unimib.it"
					trailingIcon={IconArrowSquareOut}
				>
					Apri in Moodle
				</Button>
				<Button size="sm" disabled>Non disponibile</Button>
				<Button variant="solid" disabled>Chiuso</Button>
			</div>
			<div class="wrap">
				<IconButton
					icon={IconStar}
					label="Aggiungi ai preferiti"
					pressed={starred}
					onclick={() => (starred = !starred)}
				/>
				<IconButton icon={IconShare} label="Condividi" variant="tonal" />
				<IconButton
					icon={IconDots}
					label="Altre azioni"
					variant="outline"
					shortcut={['mod', '.']}
				/>
				<IconButton icon={IconDownload} label="Scarica" variant="solid" size="lg" />
				<IconButton
					icon={IconMapPin}
					label="Mostra sulla mappa"
					size="sm"
					href={shellHref('map')}
				/>
			</div>
		</div>
	</Card>

	<div class="row">
		<Card title="Chip e filtri">
			<div class="stack">
				<div class="wrap">
					<Chip icon={IconMapPin} href={shellHref('map')}>U6 · Aula 04</Chip>
					<Chip onclick={() => {}}>Algoritmi</Chip>
					<Chip size="sm">6 CFU</Chip>
				</div>
				<div class="wrap" role="group" aria-label="Filtra eventi">
					<FilterChip label="Lezioni" bind:selected={filters.lessons} count={12} />
					<FilterChip label="Esami" bind:selected={filters.exams} count={2} />
					<FilterChip label="Scadenze" bind:selected={filters.deadlines} />
					<FilterChip label="Annullate" disabled />
				</div>
			</div>
		</Card>

		<Card title="Controlli segmentati">
			<div class="stack">
				<SegmentedControl
					label="Vista del calendario"
					bind:value={view}
					full
					options={[
						{ value: 'day', label: 'Gio 18', caption: 'Giorno' },
						{ value: 'week', label: '15–21', caption: 'Settimana' },
						{ value: 'month', label: 'Giugno', caption: 'Mese' }
					]}
				/>
				<SegmentedControl
					label="Disposizione"
					bind:value={layout}
					options={[
						{ value: 'list', label: 'Elenco', icon: IconListBullets },
						{ value: 'grid', label: 'Griglia', icon: IconSquaresFour }
					]}
				/>
			</div>
		</Card>
	</div>

	<Card title="Schede">
		<div class="stack">
			<Tabs
				label="Sezioni del corso"
				bind:value={tab}
				items={[
					{ value: 'overview', label: 'Panoramica' },
					{ value: 'content', label: 'Contenuti' },
					{ value: 'quizzes', label: 'Quiz', count: 3 },
					{ value: 'forums', label: 'Forum', count: 12 }
				]}
			>
				{#snippet panel(value)}
					<p class="muted">
						Pannello attivo: <strong>{value}</strong>. Le frecce ← → cambiano scheda.
					</p>
				{/snippet}
			</Tabs>
			<div class="narrow">
				<p class="caption">fill e dense: cinque schede in 360 px</p>
				<Tabs label="Sezioni del corso" bind:value={courseTab} items={courseTabs} fill dense />
				<p class="caption">Senza fill: la scheda attiva scorre in vista</p>
				<Tabs label="Sezioni del corso" bind:value={courseTab} items={courseTabs} />
			</div>
			<Tabs
				label="Servizi"
				value="fees"
				items={[
					{ value: 'fees', label: 'Tasse', href: shellHref('services/fees') },
					{ value: 'isee', label: 'ISEE', href: shellHref('services/isee') },
					{ value: 'refunds', label: 'Rimborsi', href: shellHref('services/refunds') }
				]}
			/>
		</div>
	</Card>
</Specimen>

<style>
	.stack {
		display: grid;
		gap: 16px;
	}

	.wrap {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px;
	}

	.row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(min(100%, 320px), 1fr));
		gap: 16px;
	}

	.muted {
		color: var(--text-muted);
	}

	.narrow {
		display: grid;
		gap: 8px;
		max-inline-size: 360px;
		padding: 12px;
		border-radius: var(--r-md);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.caption {
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
