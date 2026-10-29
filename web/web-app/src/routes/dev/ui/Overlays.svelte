<script lang="ts">
	import IconDots from '~icons/ph/dots-three-vertical';
	import IconDownload from '~icons/ph/download-simple';
	import IconShare from '~icons/ph/share-network';
	import IconCalendarPlus from '~icons/ph/calendar-plus';
	import IconTrash from '~icons/ph/trash';
	import IconInfo from '~icons/ph/info';
	import IconMapPin from '~icons/ph/map-pin';
	import IconClock from '~icons/ph/clock';
	import IconChalkboard from '~icons/ph/chalkboard-teacher';
	import IconSortAscending from '~icons/ph/sort-ascending';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import Facts from '#lib/ui/Facts.svelte';
	import FilterChip from '#lib/ui/FilterChip.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Menu from '#lib/ui/Menu.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import Popover from '#lib/ui/Popover.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import Specimen from './Specimen.svelte';

	let dialog = $state(false);
	let confirm = $state(false);
	let event = $state(false);
	let filters = $state(false);
	let attempts = 0;
	let note = $state('');
	let types = $state({ lessons: true, exams: true, labs: false });
	let reason = $state('');
	let sort = $state<'date' | 'name' | 'grade'>('date');
	const sorts = { date: 'Data', name: 'Nome', grade: 'Voto' } as const;

	let posts = $state([
		'Orario del ricevimento',
		'Slide della lezione 4',
		'Esercizi sul capitolo 3'
	]);
	let removing = $state('');
	let removeOpen = $state(false);
	let postList = $state<HTMLElement>();

	async function cancelBooking() {
		await new Promise((resolve) => setTimeout(resolve, 900));
		attempts++;
		if (attempts === 1) throw new Error('rejected');
		toast.show({
			message: 'Prenotazione annullata',
			undo: () => toast.info('Prenotazione ripristinata')
		});
	}
</script>

<Specimen
	id="overlays"
	title="Sovrapposizioni"
	description="Dialoghi nativi, foglio adattivo (in basso sui telefoni), popover e menu ancorati, suggerimenti."
>
	<Card>
		<div class="wrap">
			<Button onclick={() => (dialog = true)}>Dialogo</Button>
			<Button variant="danger" icon={IconTrash} onclick={() => (confirm = true)}
				>Annulla prenotazione…</Button
			>
			<Button onclick={() => (event = true)}>Dettaglio evento</Button>
			<Button variant="outline" onclick={() => (filters = true)}>Filtri (pannello)</Button>

			<Popover placement="bottom-start" label="Informazioni sul voto">
				{#snippet trigger(props)}
					<Button variant="ghost" icon={IconInfo} {...props}>Popover</Button>
				{/snippet}
				<div class="popover">
					<p class="popover-title">Come si calcola la media</p>
					<p class="muted">
						La media ponderata pesa ogni voto per i suoi CFU. La lode vale 30; gli esami idonei non
						contano.
					</p>
				</div>
			</Popover>

			<Menu
				label="Azioni sulla prenotazione"
				placement="bottom-end"
				items={[
					{
						label: 'Scarica statino',
						icon: IconDownload,
						onselect: () => toast.success('Statino scaricato')
					},
					{ label: 'Aggiungi al calendario', icon: IconCalendarPlus, shortcut: ['mod', 'd'] },
					{ label: 'Condividi…', icon: IconShare },
					{ separator: true },
					{ heading: 'Promemoria' },
					{ label: 'Il giorno prima', checked: true },
					{ label: 'Un’ora prima', checked: false },
					{ separator: true },
					{
						label: 'Annulla prenotazione…',
						icon: IconTrash,
						tone: 'danger',
						onselect: () => (confirm = true)
					}
				]}
			>
				{#snippet trigger(props)}
					<IconButton icon={IconDots} label="Altre azioni" variant="outline" {...props} />
				{/snippet}
			</Menu>

			<Menu
				label="Ordina per"
				items={(Object.keys(sorts) as (keyof typeof sorts)[]).map((key) => ({
					label: sorts[key],
					radio: true,
					checked: sort === key,
					onselect: () => (sort = key)
				}))}
			>
				{#snippet trigger(props)}
					<Button variant="outline" icon={IconSortAscending} {...props}>{sorts[sort]}</Button>
				{/snippet}
			</Menu>

			<button
				type="button"
				class="hint"
				{@attach tooltip('Aula al primo piano, ingresso lato nord', { placement: 'bottom' })}
			>
				U6‑04
			</button>
		</div>
	</Card>

	<Card title="Il pulsante che apre il dialogo sparisce">
		<ul class="posts" bind:this={postList} tabindex="-1" aria-label="Messaggi">
			{#each posts as post (post)}
				<li class="post">
					<span>{post}</span>
					<IconButton
						icon={IconTrash}
						label="Elimina «{post}»"
						onclick={() => {
							removing = post;
							removeOpen = true;
						}}
					/>
				</li>
			{:else}
				<li class="muted">Nessun messaggio.</li>
			{/each}
		</ul>
	</Card>
</Specimen>

<Dialog
	bind:open={dialog}
	title="Aggiungi una nota"
	description="Visibile solo a te, sul dettaglio dell’evento."
>
	<TextField label="Nota" bind:value={note} placeholder="Portare la calcolatrice" autofocus />
	{#snippet footer()}
		<Button variant="ghost" onclick={() => (dialog = false)}>Annulla</Button>
		<Button variant="solid" onclick={() => (dialog = false)}>Salva</Button>
	{/snippet}
</Dialog>

<ConfirmDialog
	bind:open={confirm}
	title="Annullare la prenotazione?"
	message="Perderai il posto all’appello del 26 giugno. Potrai prenotarti di nuovo finché le iscrizioni sono aperte."
	confirmLabel="Annulla prenotazione"
	cancelLabel="Mantieni"
	tone="danger"
	onconfirm={cancelBooking}
	errorMessage={() => 'Esse3 ha rifiutato l’annullamento. Riprova tra un momento.'}
>
	<TextField label="Motivo" optional bind:value={reason} placeholder="Ho un altro esame" />
</ConfirmDialog>

<ConfirmDialog
	bind:open={removeOpen}
	title="Eliminare il messaggio?"
	message="«{removing}» sparirà dal forum per tutti."
	confirmLabel="Elimina"
	tone="danger"
	onconfirm={() => {
		posts = posts.filter((post) => post !== removing);
	}}
	returnFocus={() => postList?.querySelector('button') ?? postList}
/>

<Overlay bind:open={event} title="Algoritmi e strutture dati" subtitle="Lezione · lun 27 ott">
	<Facts
		items={[
			{ label: 'Orario', value: '12:30–14:30', icon: IconClock },
			{ label: 'Aula', value: 'U6 · Aula 04', icon: IconMapPin },
			{ label: 'Docente', value: 'Prof.ssa Laura Bianchi', icon: IconChalkboard }
		]}
	/>
	{#snippet footer()}
		<Button variant="outline" icon={IconMapPin}>Mostra sulla mappa</Button>
		<Button variant="solid" icon={IconCalendarPlus}>Aggiungi al calendario</Button>
	{/snippet}
</Overlay>

<Overlay bind:open={filters} title="Filtri" wide="side">
	<div class="filters" role="group" aria-label="Tipi di evento">
		<FilterChip label="Lezioni" bind:selected={types.lessons} />
		<FilterChip label="Esami" bind:selected={types.exams} />
		<FilterChip label="Laboratori" bind:selected={types.labs} />
	</div>
	{#snippet footer()}
		<Button variant="solid" onclick={() => (filters = false)}>Applica</Button>
	{/snippet}
</Overlay>

<style>
	.wrap {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px;
	}

	.popover {
		display: grid;
		gap: 4px;
		padding: 8px 10px 10px;
	}

	.popover-title {
		font: var(--type-ui-strong);
	}

	.muted {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.hint {
		padding: 4px 8px;
		border-radius: var(--r-xs);
		color: var(--accent-text);
		font: var(--type-ui);
		text-decoration: underline dotted;
		text-underline-offset: 3px;
	}

	.posts {
		display: grid;
		gap: 4px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.post {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
		padding-inline-start: 4px;
	}

	.filters {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
	}
</style>
