<script lang="ts">
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconTray from '~icons/ph/tray-duotone';
	import IconCheckCircle from '~icons/ph/check-circle-duotone';
	import IconStairs from '~icons/ph/stairs-duotone';
	import IconChatsCircle from '~icons/ph/chats-circle-duotone';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ProgressBar from '#lib/ui/ProgressBar.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import Specimen from './Specimen.svelte';

	let retrying = $state(false);
	let showBanner = $state(true);
	let loadingKey = $state(0);

	function retry() {
		retrying = true;
		setTimeout(() => (retrying = false), 1400);
	}
</script>

<Specimen
	id="feedback"
	title="Stati e messaggi"
	description="Avvisi, vuoti tipografici, errori con dati non aggiornati, caricamento onesto, toast."
>
	<div class="stack">
		{#if showBanner}
			<Banner
				tone="info"
				title="Iscrizioni aperte"
				message="Il rinnovo per l’A.A. 2026/27 è aperto fino al 31 ottobre."
				ondismiss={() => (showBanner = false)}
			>
				{#snippet actions()}
					<Button size="sm" variant="solid" href={shellHref('services/enrollments')}
						>Rinnova…</Button
					>
				{/snippet}
			</Banner>
		{/if}
		<Banner tone="warning" message="Seconda rata in scadenza il 15 dicembre: € 612,00." />
		<Banner tone="success" message="Prenotazione confermata. Trovi lo statino in Appelli." />
		<Banner
			tone="danger"
			title="Pagamento non riuscito"
			message="PagoPA non ha confermato il pagamento. Non ti è stato addebitato nulla."
		/>
		<ErrorState
			variant="stale"
			message="Non è stato possibile aggiornare gli esiti."
			updatedAt={new Date(Date.now() - 3 * 3600_000)}
			onretry={retry}
			{retrying}
		/>
	</div>

	<div class="row">
		<Card>
			<EmptyState
				icon={IconCalendarX}
				title="Nessun esame in programma."
				body="Le prossime sessioni si aprono il 12 gennaio."
			>
				{#snippet action()}<Button href={shellHref('services/exams')}>Vedi gli appelli</Button
					>{/snippet}
			</EmptyState>
		</Card>
		<Card>
			<ErrorState
				title="Impossibile caricare il calendario"
				message="Il servizio orari non risponde. Riprova tra un momento."
				onretry={retry}
				{retrying}
			/>
		</Card>
		<Card>
			<EmptyState
				icon={IconTray}
				title="Tutto in ordine."
				body="Non ci sono questionari da compilare."
			/>
		</Card>
	</div>

	<div class="row">
		<Card title="Piano 2 · Edificio U6">
			<EmptyState
				size="panel"
				icon={IconStairs}
				title="Nessuna aula prenotabile a questo piano."
				body="Le aule studio sono al piano terra."
			>
				{#snippet action()}<Button size="sm">Vai al piano terra</Button>{/snippet}
			</EmptyState>
		</Card>
		<Card>
			<EmptyState
				icon={IconCheckCircle}
				tone="success"
				title="Piano di studi presentato."
				body="La segreteria lo approverà entro novembre."
			/>
		</Card>
		<Card>
			<EmptyState
				size="cover"
				icon={IconChatsCircle}
				title="Nessuna discussione nel forum."
				body="Sotto la copertina di un corso: senza l’altezza di una pagina."
			/>
		</Card>
	</div>

	<div class="row">
		<Card title="Caricamento">
			{#snippet actions()}
				<Button size="sm" variant="ghost" onclick={() => loadingKey++}>Ricarica</Button>
			{/snippet}
			{#key loadingKey}
				<div class="skeleton" aria-busy="true">
					<p class="sr-only" role="status">Caricamento…</p>
					{#each [0, 1, 2] as i (i)}
						<div class="sk-row">
							<Skeleton variant="block" width="40px" height="40px" radius="12px" />
							<div class="sk-text">
								<Skeleton variant="text" width="70%" />
								<Skeleton variant="text" width="45%" />
							</div>
						</div>
					{/each}
				</div>
			{/key}
		</Card>
		<Card title="Avanzamento">
			<div class="stack">
				<ProgressBar label="Caricamento del compito" value={3} max={8} valueText="3 di 8 file" />
				<ProgressBar label="Sincronizzazione" />
				<ProgressBar
					label="Questionario"
					value={5}
					max={6}
					valueText="5 di 6"
					size="sm"
					tone="success"
				/>
			</div>
		</Card>
		<Card title="Toast">
			<div class="wrap">
				<Button onclick={() => toast.success('Prenotazione confermata')}>Successo</Button>
				<Button
					onclick={() =>
						toast.show({
							message: 'Prenotazione annullata',
							description: 'Algoritmi · 26 giu',
							undo: () => toast.info('Prenotazione ripristinata')
						})}
				>
					Con annulla
				</Button>
				<Button
					onclick={() =>
						toast.error('Sincronizzazione non riuscita', {
							action: { label: 'Riprova', onclick: () => {} }
						})}
				>
					Errore
				</Button>
				<Button variant="ghost" onclick={() => toast.show({ message: 'Copiato negli appunti' })}
					>Neutro</Button
				>
			</div>
		</Card>
	</div>

	<Card>
		<EmptyState
			size="page"
			title="Nessun corso per questo filtro."
			body="Prova a mostrare anche i corsi conclusi, oppure aggiungine uno dal catalogo."
		>
			{#snippet action()}
				<Button variant="solid" href={shellHref('elearning')}>Sfoglia il catalogo</Button>
			{/snippet}
		</EmptyState>
	</Card>
</Specimen>

<style>
	.stack {
		display: grid;
		gap: 12px;
	}

	.row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(min(100%, 300px), 1fr));
		align-items: start;
		gap: 16px;
	}

	.wrap {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
	}

	.skeleton {
		display: grid;
		gap: 14px;
	}

	.sk-row {
		display: flex;
		align-items: center;
		gap: 12px;
	}

	.sk-text {
		flex: 1;
		display: grid;
	}
</style>
