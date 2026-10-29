<script lang="ts">
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import type { ResolvedPathname } from '$app/types';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconCloudSlash from '~icons/ph/cloud-slash';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import IconSealCheck from '~icons/ph/seal-check-duotone';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import DataTable, { type DataColumn } from '#lib/ui/DataTable.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import Facts from '#lib/ui/Facts.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Kbd from '#lib/ui/Kbd.svelte';
	import ListDetail from '#lib/ui/ListDetail.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import HeaderSpecimen from './HeaderSpecimen.svelte';
	import Specimen from './Specimen.svelte';

	interface Fee {
		id: string;
		title: string;
		installment: string;
		invoice: string;
		amount: string;
		due: string;
		paidOn?: string;
		year: string;
	}

	const fees: Fee[] = [
		{
			id: 'r1',
			title: 'Prima rata',
			installment: 'Rata 1 di 3',
			invoice: '6001842',
			amount: '€ 156,00',
			due: '31 ott 2026',
			paidOn: '12 ott 2026',
			year: '2026/27'
		},
		{
			id: 'r2',
			title: 'Seconda rata',
			installment: 'Rata 2 di 3',
			invoice: '6001843',
			amount: '€ 612,00',
			due: '15 dic 2026',
			year: '2026/27'
		},
		{
			id: 'r3',
			title: 'Terza rata',
			installment: 'Rata 3 di 3',
			invoice: '6001844',
			amount: '€ 612,00',
			due: '15 mar 2027',
			year: '2026/27'
		},
		{
			id: 'p1',
			title: 'Contributo onnicomprensivo',
			installment: 'Rata unica',
			invoice: '5004410',
			amount: '€ 1.380,00',
			due: '30 nov 2025',
			paidOn: '28 nov 2025',
			year: '2025/26'
		},
		{
			id: 'p2',
			title: 'Tassa regionale per il diritto allo studio',
			installment: 'Rata unica',
			invoice: '5004411',
			amount: '€ 140,00',
			due: '31 ott 2025',
			paidOn: '2 ott 2025',
			year: '2025/26'
		}
	];

	/* The open fee lives in the address, as list-detail pages keep it: Back, J/K and Esc work. */
	const feeHref = (id: string): ResolvedPathname => resolve(`dev/ui?fee=${id}#layouts`);
	const closeHref = resolve('dev/ui#layouts');
	const selected = $derived(page.url.searchParams.get('fee') ?? undefined);
	const fee = $derived(fees.find((f) => f.id === selected));

	let twoPanes = $state<boolean>();
	let centered = $state(false);
	let detailMin = $state<'360' | '480' | '640'>('480');
	let note = $state('');

	let loading = $state(true);
	let headerBack = $state(true);

	const columns: DataColumn<Fee>[] = [
		{
			id: 'what',
			label: 'Descrizione',
			value: (f) => f.title,
			sub: (f) => f.installment,
			wrap: true
		},
		{ id: 'invoice', label: 'Fattura', value: (f) => f.invoice, code: true, narrow: 'hide' },
		{ id: 'due', label: 'Scadenza', value: (f) => f.due },
		{
			id: 'paid',
			label: 'Pagata',
			value: (f) => (f.paidOn ? `Pagata il ${f.paidOn}` : 'Da pagare'),
			cell: paidCell
		},
		{ id: 'amount', label: 'Importo', value: (f) => f.amount, align: 'end', narrow: 'meta' }
	];
	const groups = [
		{
			id: '2026/27',
			title: 'A.A. 2026/27',
			aside: '€ 156,00 pagati',
			rows: fees.filter((f) => f.year === '2026/27')
		},
		{
			id: '2025/26',
			title: 'A.A. 2025/26',
			aside: '€ 1.520,00 pagati',
			rows: fees.filter((f) => f.year === '2025/26')
		}
	];
</script>

{#snippet paidCell(f: Fee)}
	{#if f.paidOn}
		<StatusChip status="success" size="sm" label={f.paidOn} />
	{:else}
		<StatusChip status="pending" size="sm" label="Da pagare" />
	{/if}
{/snippet}

{#snippet feeDetail()}
	{#if fee}
		<div class="detail">
			<DetailHeader
				title={fee.title}
				subtitle="{fee.installment} · A.A. {fee.year}"
				eyebrow={`Fattura ${fee.invoice}`}
				eyebrowCode
			/>
			<StatusChip
				status={fee.paidOn ? 'success' : 'warning'}
				label={fee.paidOn ? `Pagata il ${fee.paidOn}` : 'Da pagare'}
			/>
			<Facts
				items={[
					{ label: 'Importo', value: fee.amount },
					{ label: 'Scadenza', value: fee.due },
					{ label: 'Fattura', value: fee.invoice, code: true }
				]}
			/>
			<TextField
				label="Nota"
				optional
				bind:value={note}
				description="Scrivi qualcosa e ridimensiona la finestra: resta qui."
			/>
		</div>
	{/if}
{/snippet}

<Specimen
	id="layouts"
	title="Elenco e dettaglio"
	description="Due riquadri quando lo spazio del componente ospita elenco, spazio e dettaglio minimo (da 1024 px di finestra); un solo albero, quindi nulla si rimonta."
>
	<div class="controls">
		<SegmentedControl
			label="Dettaglio minimo"
			bind:value={detailMin}
			options={[
				{ value: '360', label: '360 px' },
				{ value: '480', label: '480 px' },
				{ value: '640', label: '640 px' }
			]}
		/>
		<Switch label="Centrato" bind:checked={centered} />
		<p class="muted" role="status">
			{twoPanes ? 'Due riquadri' : 'Un riquadro'} ·
			<Kbd keys={['j']} />/<Kbd keys={['k']} /> scorre, <Kbd keys={['esc']} /> chiude
		</p>
	</div>

	<Card padding="lg" tone="sunken">
		<ListDetail
			listLabel="Rate"
			detailLabel="Dettaglio della rata"
			listWidth={320}
			detailMin={Number(detailMin)}
			{centered}
			bind:twoPanes
			detail={fee ? feeDetail : undefined}
			current={fee ? feeHref(fee.id) : undefined}
			items={fees.map((f) => feeHref(f.id))}
			{closeHref}
		>
			{#snippet list()}
				<ListGroup title="Tasse" description="Rate e contributi" size="sm">
					{#each fees as item (item.id)}
						<ListRow
							title={item.title}
							description="Scadenza {item.due}"
							meta={item.amount}
							href={feeHref(item.id)}
							selected={item.id === selected}
							dense={twoPanes}
						/>
					{/each}
				</ListGroup>
			{/snippet}
			{#snippet placeholder()}
				<EmptyState icon={IconSealCheck} title="Scegli una rata per vederne i dettagli." />
			{/snippet}
		</ListDetail>
	</Card>

	<DataTable
		label="Storico dei pagamenti"
		{columns}
		{groups}
		key={(f) => f.id}
		href={(f) => feeHref(f.id)}
		selected={(f) => f.id === selected}
		icon={(f) =>
			f.paidOn
				? { icon: IconCheckCircle, tone: 'success' }
				: { icon: IconHourglass, tone: 'neutral' }}
	/>

	<div class="controls">
		<Switch label="Intestazione in caricamento" bind:checked={loading} />
		<Switch label="Con pagina superiore" bind:checked={headerBack} />
	</div>
	<div class="headers">
		<HeaderSpecimen>
			<PageHeader
				title={loading ? '' : 'Algoritmi e strutture dati'}
				eyebrow={loading ? undefined : 'F1801Q127'}
				eyebrowCode
				subtitle={loading ? undefined : '18 di 24 lezioni · aggiornato alle 9:40'}
				back={headerBack ? { href: resolve('dev/ui'), label: 'Presenze' } : undefined}
				{loading}
			/>
		</HeaderSpecimen>
		<HeaderSpecimen>
			<PageHeader
				title="Esiti"
				subtitle="Non aggiornati · dati di 3 ore fa"
				subtitleTone="warning"
				subtitleIcon={IconCloudSlash}
			>
				{#snippet actions()}
					<IconButton icon={IconArrowClockwise} label="Aggiorna" variant="tonal" />
				{/snippet}
			</PageHeader>
		</HeaderSpecimen>
		<HeaderSpecimen>
			<PageHeader
				title="Certificato di iscrizione con esami sostenuti"
				titleLang="it"
				sans
				subtitle="Titolo senza grazie: il momento serif è altrove"
			>
				{#snippet actions()}
					<Button variant="solid">Scarica PDF</Button>
				{/snippet}
			</PageHeader>
		</HeaderSpecimen>
	</div>
</Specimen>

<style>
	.controls {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 12px 24px;
	}

	.detail {
		display: grid;
		gap: 16px;
		justify-items: start;
	}

	.detail > :global(*) {
		justify-self: stretch;
	}

	.detail > :global(.status) {
		justify-self: start;
	}

	.muted {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.headers {
		display: grid;
		gap: 16px;
	}
</style>
