<script lang="ts">
	import { resolve } from '$app/paths';
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import IconCheck from '~icons/ph/check-bold';
	import IconVideoCamera from '~icons/ph/video-camera';
	import type { ExamGrade } from '#lib/data/esse3/grades.ts';
	import BookingCard from '#lib/ui/BookingCard.svelte';
	import BookingCardSkeleton from '#lib/ui/BookingCardSkeleton.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Grade from '#lib/ui/Grade.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import Specimen from './Specimen.svelte';

	const today = new Date();
	const day = (offset: number, hour: number) =>
		new Date(today.getFullYear(), today.getMonth(), today.getDate() + offset, hour, 30);

	const grades: ExamGrade[] = [
		{ kind: 'numeric', value: 30, cumLaude: true },
		{ kind: 'numeric', value: 27, cumLaude: false },
		{ kind: 'passed' },
		{ kind: 'not_passed' },
		{ kind: 'absent' },
		{ kind: 'withdrew' },
		{ kind: 'unknown' }
	];

	let outcome = $state<'booked' | 'cancelled'>('booked');
	/* An outcome takes the focus as it arrives: it shows on request, anew each time. */
	let shown = $state(0);
</script>

<Specimen
	id="records"
	title="Prenotazioni, esiti e voti"
	description="BookingCard per ogni prenotazione (esami, uffici, biblioteca); Outcome dopo una scrittura confermata, che prende il fuoco; Grade, l'unico modo di disegnare un voto."
>
	<div class="cards">
		<BookingCard
			href={resolve('dev/ui')}
			date={day(0, 9)}
			title="Probabilità e Statistica"
			subtitle="Scritto"
			status={{ label: 'Oggi', tone: 'accent' }}
			when="Oggi · 09:30"
			place={{ label: 'U6-08', name: true }}
		/>
		<BookingCard
			href={resolve('dev/ui')}
			date={day(5, 14)}
			title="Segreteria didattica di Informatica"
			subtitle="Carriera"
			when="Lun 3 nov · 14:30–14:50"
			place={{ label: 'Videochiamata', icon: IconVideoCamera }}
		/>
		<BookingCard
			href={resolve('dev/ui')}
			date={day(-3, 10)}
			past
			title="Biblioteca Centrale"
			subtitle="Posto A12"
			status={{ label: 'Terminato' }}
			when="Ven 24 ott · 10:30–13:30"
			place={{ label: 'Aula da confermare', pending: true }}
		/>
		<BookingCardSkeleton />
	</div>

	<div class="grades">
		{#each grades as grade, i (i)}
			<div class="grade">
				<Grade {grade} variant="tile" size="md" />
				<Grade {grade} variant="tile" />
				<Grade {grade} />
			</div>
		{/each}
		<div class="grade">
			<Grade grade={grades[1]} variant="tile" size="md" struck />
			<Grade grade={grades[1]} variant="tile" struck />
		</div>
	</div>

	<div class="controls">
		<SegmentedControl
			label="Esito"
			bind:value={outcome}
			options={[
				{ value: 'booked', label: 'Prenotato' },
				{ value: 'cancelled', label: 'Annullato' }
			]}
		/>
		<Button onclick={() => shown++}>Mostra l’esito</Button>
	</div>
	{#key shown}
		{#if shown === 0}
			<p class="hint">Un esito arriva al posto di ciò che lo ha chiesto e ne prende il fuoco.</p>
		{:else if outcome === 'booked'}
			<Outcome
				icon={IconCheck}
				title="Prenotazione confermata"
				body="Sei iscritto a Probabilità e Statistica (venerdì 6 novembre 2026 alle 09:30)."
			>
				{#snippet actions()}
					<Button variant="solid">Vedi la prenotazione</Button>
					<Button>Aggiungi al calendario</Button>
				{/snippet}
			</Outcome>
		{:else}
			<Outcome
				icon={IconCalendarSlash}
				tone="neutral"
				title="Prenotazione annullata"
				body="Il posto torna disponibile per gli altri studenti."
			>
				{#snippet actions()}
					<Button variant="solid">Torna alle prenotazioni</Button>
				{/snippet}
			</Outcome>
		{/if}
	{/key}
</Specimen>

<style>
	.cards {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 320px), 1fr));
		gap: 12px;
	}

	.grades {
		display: flex;
		flex-wrap: wrap;
		gap: 16px 24px;
	}

	.grade {
		display: flex;
		align-items: center;
		gap: 12px;
	}

	.controls {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 12px;
	}

	.hint {
		color: var(--text-muted);
	}
</style>
