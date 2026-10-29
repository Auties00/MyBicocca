<script lang="ts">
	import IconTree from '~icons/ph/tree-structure';
	import IconUserFocus from '~icons/ph/user-focus';
	import IconListChecks from '~icons/ph/list-checks';
	import IconClipboard from '~icons/ph/clipboard-text';
	import IconBell from '~icons/ph/bell-simple';
	import IconFingerprint from '~icons/ph/fingerprint';
	import IconVibrate from '~icons/ph/vibrate';
	import IconClock from '~icons/ph/clock';
	import IconMapPin from '~icons/ph/map-pin';
	import IconIdentification from '~icons/ph/identification-card';
	import IconBarcode from '~icons/ph/barcode';
	import IconCalendar from '~icons/ph/calendar-blank';
	import IconCurrency from '~icons/ph/currency-eur';
	import IconCertificate from '~icons/ph/certificate';
	import IconFolder from '~icons/ph/folder-simple';
	import IconShare from '~icons/ph/share-network';
	import IconDownload from '~icons/ph/download-simple';
	import IconCopy from '~icons/ph/copy';
	import IconBuilding from '~icons/ph/buildings';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import Avatar from '#lib/ui/Avatar.svelte';
	import Badge from '#lib/ui/Badge.svelte';
	import Card from '#lib/ui/Card.svelte';
	import DateBadge from '#lib/ui/DateBadge.svelte';
	import Facts from '#lib/ui/Facts.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import Meter from '#lib/ui/Meter.svelte';
	import NowLine from '#lib/ui/NowLine.svelte';
	import Stat from '#lib/ui/Stat.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import Timeline from '#lib/ui/Timeline.svelte';
	import TimelineRow from '#lib/ui/TimelineRow.svelte';
	import { shellHref } from '#lib/shell/nav.ts';
	import Specimen from './Specimen.svelte';

	let lock = $state(true);
	let haptics = $state(false);

	let openRow = $state('docs');

	const payment = [
		{ label: 'Importo', value: '€ 612,00', icon: IconCurrency },
		{
			label: 'Codice avviso',
			value: '301000000123456789',
			icon: IconBarcode,
			code: true,
			copy: true
		},
		{ label: 'Scadenza', value: '15 dic 2026', icon: IconCalendar }
	];

	async function copy(label: string, value: string) {
		try {
			await navigator.clipboard.writeText(value);
			toast.success(`${label} copiato`);
		} catch {
			toast.error('Copia non riuscita');
		}
	}

	const at = (h: number, min = 0) => {
		const d = new Date(2026, 9, 27, h, min);
		return d;
	};
</script>

<Specimen
	id="data"
	title="Dati"
	description="Liste a segmenti, righe, statistiche, linea del tempo, fatti e stati."
>
	<div class="row">
		<ListGroup title="Didattica" description="Percorso, presenze ed esiti">
			<ListRow
				title="Piano di studi"
				description="Percorso e piano di studi"
				icon={IconTree}
				iconTone="info"
				href={shellHref('services/study-plan')}
			/>
			<ListRow
				title="Presenze"
				description="Frequenze e rilevazioni"
				icon={IconUserFocus}
				iconTone="info"
				href={shellHref('services/attendance')}
				selected
			/>
			<ListRow
				title="Esiti"
				description="Accetta o rifiuta i voti"
				icon={IconListChecks}
				iconTone="info"
				href={shellHref('services/results')}
			>
				{#snippet trailing()}<Badge
						count={2}
						tone="accent"
						variant="solid"
						label="2 da accettare"
					/>{/snippet}
			</ListRow>
			<ListRow
				title="Questionari"
				description="Valutazione della didattica"
				icon={IconClipboard}
				iconTone="info"
				href={shellHref('services/questionnaires')}
			/>
		</ListGroup>

		<div class="stack">
			<ListGroup label="Esami sostenuti">
				<ListRow
					title="Basi di dati"
					description="18 giu 2026 · 9 CFU"
					course={3}
					icon={IconListChecks}
				>
					{#snippet trailing()}<StatusChip status="gold" label="30 e lode" />{/snippet}
				</ListRow>
				<ListRow title="Probabilità e Statistica" description="2 feb 2026 · 6 CFU" meta="27">
					{#snippet trailing()}<StatusChip
							status="success"
							label="Verbalizzato"
							size="sm"
						/>{/snippet}
				</ListRow>
				<ListRow title="Fisica" description="In attesa di verbalizzazione" meta="24">
					{#snippet trailing()}<StatusChip
							status="pending"
							label="Da accettare"
							size="sm"
						/>{/snippet}
				</ListRow>
				<ListRow title="Inglese B2" description="Prova non superata" disabled>
					{#snippet trailing()}<StatusChip status="danger" label="Respinto" size="sm" />{/snippet}
				</ListRow>
			</ListGroup>
			<ListGroup label="Frequenze">
				<ListRow
					title="Algoritmi e strutture dati"
					description="18 di 24 lezioni"
					meta="75%"
					progress={0.75}
					course={2}
					icon={IconUserFocus}
				/>
				<ListRow
					title="Reti di calcolatori"
					description="6 di 20 lezioni"
					meta="30%"
					progress={0.3}
					course={5}
					icon={IconUserFocus}
				/>
			</ListGroup>
		</div>
	</div>

	<div class="row">
		<ListGroup title="Preferenze" description="Personalizza l’esperienza">
			<ListRow
				title="Blocco app"
				description="Chiedi la password all’apertura"
				icon={IconFingerprint}
				iconTone="accent"
			>
				{#snippet control()}<Switch bind:checked={lock} />{/snippet}
			</ListRow>
			<ListRow
				title="Vibrazione"
				description="Feedback al tocco"
				icon={IconVibrate}
				iconTone="accent"
			>
				{#snippet control()}<Switch bind:checked={haptics} />{/snippet}
			</ListRow>
			<ListRow
				title="Promemoria scadenze"
				description="3 giorni prima"
				icon={IconBell}
				iconTone="accent"
				onclick={() => {}}
			/>
		</ListGroup>

		<Card title="Media ponderata">
			{#snippet actions()}<Badge tone="gold" variant="outline">2 lodi</Badge>{/snippet}
			<Stat
				label="Su 21 esami"
				value={27.45}
				decimals={2}
				unit="/ 30"
				size="xl"
				countUp="gallery-average"
			>
				{#snippet footer()}
					<Meter
						value={129}
						max={180}
						label="Crediti"
						valueText="129 di 180 CFU"
						marks={[60, 120]}
					/>
				{/snippet}
			</Stat>
		</Card>
	</div>

	<div class="row">
		<Card title="Oggi">
			{#snippet actions()}<Badge>lun 27 ott</Badge>{/snippet}
			<Timeline label="Lezioni di oggi">
				<TimelineRow
					start={at(9, 30)}
					end={at(11, 30)}
					title="Analisi matematica II"
					subtitle="U5 · Aula 3 · Lezione"
					course={1}
					state="past"
				/>
				<NowLine now={at(12, 5)} />
				<TimelineRow
					start={at(12, 30)}
					end={at(14, 30)}
					title="Algoritmi e strutture dati"
					subtitle="U6 · Aula 04 · Lezione"
					course={2}
					href={shellHref('calendar')}
				/>
				<TimelineRow
					start={at(15)}
					end={at(17)}
					title="Basi di dati"
					subtitle="U24 · C2 · Esame scritto"
					course={3}
					state="upcoming"
				>
					{#snippet badge()}<StatusChip status="accent" label="Esame" size="sm" />{/snippet}
				</TimelineRow>
			</Timeline>
		</Card>

		<div class="stack">
			<Card padding="none" href={shellHref('services/exams')}>
				<div class="booking">
					<DateBadge date={new Date(2026, 5, 26)} />
					<div>
						<p class="booking-title">Probabilità e Statistica</p>
						<p class="muted">Scritto · iscrizioni fino al 22 giu</p>
					</div>
				</div>
				{#snippet footer()}
					<span class="foot"><IconClock aria-hidden="true" /> ven · 09:30</span>
					<span class="foot"><IconMapPin aria-hidden="true" /> U4‑08</span>
				{/snippet}
			</Card>
			<Card title="Statistiche" tone="container">
				<div class="stats">
					<Stat label="Media aritmetica" value={27.13} decimals={2} size="md" />
					<Stat label="Esami" value="9/21" size="md" />
				</div>
			</Card>
		</div>
	</div>

	<div class="row">
		<ListGroup
			title="Opzioni delle righe"
			description="badge, dense, titleLines, actions…"
			size="sm"
		>
			{#snippet actions()}<Badge tone="accent">size sm</Badge>{/snippet}
			<ListRow
				title="Esiti"
				description="Badge accanto al titolo, su una riga"
				icon={IconListChecks}
				iconTone="info"
				titleLines={1}
				href={shellHref('services/results')}
			>
				{#snippet badge()}<Badge tone="warning">2 da accettare</Badge>{/snippet}
			</ListRow>
			<ListRow
				title="Documenti"
				description="Tinta di categoria viola; chip a contrasto da selezionata"
				icon={IconFolder}
				iconTone="violet"
				selected={openRow === 'docs'}
				onclick={() => (openRow = 'docs')}
			/>
			<ListRow
				title="Algoritmi e strutture dati per la bioinformatica"
				icon={IconUserFocus}
				course={2}
				dense
				selected={openRow === 'algo'}
				onclick={() => (openRow = 'algo')}
			>
				{#snippet description()}<span class="warning">A rischio</span> · 14 di 24 lezioni, riga densa{/snippet}
			</ListRow>
			<ListRow
				title="Analisi dei Processi sociali / Turismo, territorio e sviluppo locale: segreteria didattica"
				description="titleLines=all: il nome intero"
				icon={IconBuilding}
				titleLines="all"
				href={shellHref('services/appointments')}
			/>
			<ListRow
				title="Basi di dati"
				description="Chevron accanto a trailing"
				href={shellHref('services/exams')}
				chevron
			>
				{#snippet trailing()}<StatusChip status="accent" label="Esame" size="sm" />{/snippet}
			</ListRow>
			<ListRow
				title="Certificato di iscrizione"
				titleLang="it"
				description="Azioni accanto al link, fuori da esso"
				icon={IconCertificate}
				iconTone="accent"
				href={shellHref('services/certificates')}
			>
				{#snippet actions()}
					<IconButton icon={IconShare} label="Condividi" />
					<IconButton icon={IconDownload} label="Scarica" />
				{/snippet}
			</ListRow>
			<ListRow
				title="Programmazione 1"
				description="meta a destra anche sotto 380 px"
				meta="30L"
				metaEnd
				spoken="30 e lode"
				href={shellHref('profile')}
			/>
		</ListGroup>

		<div class="stack">
			<div class="tiles">
				<Card padding="sm">
					<Stat
						label="Media"
						value={27.45}
						decimals={2}
						layout="tile"
						countUp="gallery-tile"
						caption="Ponderata · 21 esami"
					/>
				</Card>
				<Card padding="sm">
					<Stat label="Crediti" value={129} unit="/ 180" layout="tile">
						{#snippet footer()}
							<Meter value={129} max={180} label="Crediti" valueText="129 di 180" hideLabel />
						{/snippet}
					</Stat>
				</Card>
				<Card padding="sm">
					<Stat label="Esami da sostenere con un’etichetta lunga" value={8} layout="tile" />
				</Card>
			</div>
			<Facts label="Codici del pagamento" items={payment}>
				{#snippet value(fact)}
					{#if fact.copy}
						<span class="copy">
							<span translate="no">{fact.value}</span>
							<IconButton
								icon={IconCopy}
								label="Copia {fact.label.toLowerCase()}"
								size="sm"
								onclick={() => copy(fact.label, fact.value)}
							/>
						</span>
					{:else}
						{fact.value}
					{/if}
				{/snippet}
			</Facts>
		</div>
	</div>

	<Facts
		label="Dettagli del pagamento"
		items={[
			{ label: 'Importo', value: '€ 612,00', icon: IconCurrency },
			{ label: 'Scadenza', value: '15 dic 2026', icon: IconCalendar },
			{ label: 'Codice IUV', value: '0123 4567 8901 2345', icon: IconBarcode, code: true },
			{ label: 'Matricola', value: '900123', icon: IconIdentification, code: true }
		]}
	/>

	<Card title="Badge, stati e avatar">
		<div class="stack">
			<div class="wrap">
				<Badge>Bozza</Badge>
				<Badge tone="accent">Nuovo</Badge>
				<Badge tone="info">Online</Badge>
				<Badge tone="warning">In scadenza</Badge>
				<Badge tone="danger">Scaduto</Badge>
				<Badge tone="gold" variant="outline">Lode</Badge>
				<Badge count={3} tone="accent" variant="solid" label="3 non letti" />
				<Badge count={128} variant="solid" tone="danger" label="128 notifiche" />
			</div>
			<div class="wrap">
				<StatusChip status="success" label="Prenotato" />
				<StatusChip status="warning" label="Da pagare" />
				<StatusChip status="danger" label="Scaduto" />
				<StatusChip status="info" label="In revisione" />
				<StatusChip status="pending" label="In attesa" />
				<StatusChip status="neutral" label="Bozza" />
				<StatusChip status="gold" label="30 e lode" />
				<StatusChip status="accent" label="Esame" />
			</div>
			<div class="wrap">
				<Avatar name="Marco Rossi" size={56} />
				<Avatar name="Giulia Bianchi" size={40} />
				<Avatar name="Prof. Luca Neri" size={32} tone="neutral" />
				<Avatar name="Anna" size={24} />
			</div>
		</div>
	</Card>
</Specimen>

<style>
	.row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(min(100%, 340px), 1fr));
		align-items: start;
		gap: 16px;
	}

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

	.booking {
		display: flex;
		align-items: center;
		gap: 14px;
		padding: 16px;
	}

	.booking-title {
		font: var(--type-title-s);
	}

	.muted {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.foot {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		font-variant-numeric: tabular-nums;
	}

	.foot :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.tiles {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
		gap: 12px;
	}

	.warning {
		color: var(--warning);
		font-weight: 600;
	}

	.copy {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
	}

	.copy :global(.btn) {
		margin-block: -6px;
		margin-inline-end: -8px;
	}

	.stats {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 16px;
	}
</style>
