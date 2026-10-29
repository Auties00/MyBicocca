<script lang="ts">
	import Card from '#lib/ui/Card.svelte';
	import Specimen from './Specimen.svelte';

	const steps = Array.from({ length: 12 }, (_, i) => i + 1);
	const semantic = ['danger', 'warning', 'success', 'info', 'gold', 'violet'] as const;
	const courses = [1, 2, 3, 4, 5, 6, 7, 8] as const;
	const courseNames = [
		'Analisi I',
		'Algoritmi',
		'Basi di dati',
		'Fisica',
		'Reti',
		'Statistica',
		'Sistemi operativi',
		'Inglese B2'
	];
	const type = [
		{ token: 'display-l', sample: 'Buongiorno, Giulia.' },
		{ token: 'display-m', sample: 'Servizi' },
		{ token: 'numeral-xl', sample: '27,45' },
		{ token: 'title-l', sample: 'Esami sostenuti' },
		{ token: 'title-m', sample: 'Prossime lezioni' },
		{ token: 'title-s', sample: 'Algoritmi e strutture dati' },
		{ token: 'row-title', sample: 'Probabilità e Statistica' },
		{ token: 'body', sample: 'La prossima lezione inizia tra 25 min in U6‑04.' },
		{ token: 'ui', sample: 'Prenota appello' },
		{ token: 'meta', sample: 'Aggiornato 3 h fa · lun 27 ott' },
		{ token: 'overline', sample: 'Lunedì 27 ottobre' }
	];
</script>

<Specimen
	id="foundations"
	title="Fondamenta"
	description="Rampe OKLCH a 12 passi, tinte semantiche, colori dei corsi, tipografia, raggi ed elevazione."
>
	<Card title="Accento e neutri">
		<div class="ramps">
			{#each steps as step (step)}
				<span class="swatch" style:background="var(--c{step})" title="c{step}">
					<b>{step}</b>
				</span>
			{/each}
			{#each steps as step (step)}
				<span class="swatch" style:background="var(--g{step})" title="g{step}">
					<b>{step}</b>
				</span>
			{/each}
		</div>
	</Card>

	<div class="row">
		<Card title="Semantici">
			<div class="semantic">
				{#each semantic as tone (tone)}
					<div class="tone" style:--fg="var(--{tone})" style:--bg="var(--{tone}-container)">
						<span class="dot"></span>{tone}
					</div>
				{/each}
				<div class="graphic">
					<span class="bar" style:--fill="var(--warning-graphic)"></span>
					<span><code>warning-graphic</code> · archi e barre, 3:1 sulla superficie</span>
				</div>
			</div>
		</Card>
		<Card title="Colori dei corsi">
			<div class="courses">
				{#each courses as course, i (course)}
					<div class="course" data-course={course}>{courseNames[i]}</div>
				{/each}
			</div>
		</Card>
	</div>

	<Card title="Scala tipografica">
		<div class="type">
			{#each type as row (row.token)}
				<div class="type-row">
					<code>{row.token}</code>
					<span
						style:font="var(--type-{row.token})"
						style:letter-spacing="var(--track-{row.token}, normal)"
						class:upper={row.token === 'overline'}
					>
						{row.sample}
					</span>
				</div>
			{/each}
		</div>
	</Card>

	<div class="row">
		<Card title="Raggi">
			<div class="radii">
				{#each ['2xs', 'xs', 'sm', 'md', 'lg', 'xl', 'pill'] as r (r)}
					<span class="radius" style:border-radius="var(--r-{r})">{r}</span>
				{/each}
			</div>
		</Card>
		<Card title="Elevazione">
			<div class="elevations">
				{#each [1, 2, 3] as e (e)}
					<span class="elev" style:box-shadow="var(--elev-{e}), var(--inner-light)">elev-{e}</span>
				{/each}
			</div>
		</Card>
	</div>
</Specimen>

<style>
	.ramps {
		display: grid;
		grid-template-columns: repeat(12, minmax(0, 1fr));
		gap: 4px;
	}

	.swatch {
		display: grid;
		place-items: end start;
		aspect-ratio: 1;
		padding: 4px;
		border-radius: var(--r-xs);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.swatch b {
		padding: 2px 4px;
		border-radius: 4px;
		background: var(--surface);
		color: var(--text);
		font: 600 0.625rem / 1 var(--font-ui);
	}

	.row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(min(100%, 300px), 1fr));
		gap: 16px;
	}

	.semantic,
	.courses {
		display: grid;
		gap: 6px;
	}

	.courses {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}

	.tone {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 8px 10px;
		border-radius: var(--r-sm);
		background: var(--bg);
		color: var(--fg);
		font: var(--type-ui-strong);
		text-transform: capitalize;
	}

	.dot {
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: currentColor;
	}

	.graphic {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 4px 2px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.bar {
		inline-size: 48px;
		block-size: 6px;
		border-radius: var(--r-pill);
		background: linear-gradient(90deg, var(--fill) 70%, var(--track) 70%);
	}

	.course {
		padding: 8px 10px 8px 13px;
		border-radius: var(--r-sm);
		background: var(--course-container);
		box-shadow: inset 3px 0 0 var(--course-bar);
		font: var(--type-ui);
	}

	.type {
		display: grid;
		gap: 14px;
	}

	.type-row {
		display: grid;
		grid-template-columns: 96px minmax(0, 1fr);
		align-items: baseline;
		gap: 12px;
	}

	code {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.upper {
		text-transform: uppercase;
		color: var(--accent-text);
	}

	.radii,
	.elevations {
		display: flex;
		flex-wrap: wrap;
		gap: 12px;
	}

	.radius,
	.elev {
		display: grid;
		place-items: center;
		inline-size: 64px;
		block-size: 64px;
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.elev {
		inline-size: 96px;
		border-radius: var(--r-md);
		background: var(--surface);
	}
</style>
