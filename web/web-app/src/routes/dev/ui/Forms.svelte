<script lang="ts">
	import IconUser from '~icons/ph/user';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import Checkbox from '#lib/ui/Checkbox.svelte';
	import PasswordField from '#lib/ui/PasswordField.svelte';
	import RadioGroup from '#lib/ui/RadioGroup.svelte';
	import SearchField from '#lib/ui/SearchField.svelte';
	import Select from '#lib/ui/Select.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import Textarea from '#lib/ui/Textarea.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import Specimen from './Specimen.svelte';

	let username = $state('m.rossi99');
	let password = $state('');
	let search = $state('');
	let year = $state<'2026' | '2025' | '2024'>('2026');
	let remember = $state(true);
	let terms = $state(false);
	let mixed = $state(true);
	let notifications = $state(true);
	let slot = $state<'morning' | 'afternoon' | undefined>('morning');
	let mode = $state<'written' | 'oral'>('written');
	let message = $state('');
	let submitted = $state(false);
	let consent = $state(false);
	let consentTried = $state(false);
	let grades = $state({ first: '28', second: '30', third: '' });
</script>

<Specimen
	id="forms"
	title="Moduli"
	description="Campi con etichetta, aiuto ed errore; controlli nativi stilizzati."
>
	<div class="row">
		<Card title="Accesso">
			<form
				class="form"
				onsubmit={(e) => {
					e.preventDefault();
					submitted = true;
				}}
			>
				<TextField
					label="Username o Email"
					bind:value={username}
					icon={IconUser}
					autocomplete="username"
					description="Per esempio m.rossi1: il dominio @campus.unimib.it è aggiunto da solo."
				/>
				<PasswordField
					label="Password"
					bind:value={password}
					error={submitted && !password ? 'Inserisci la password.' : undefined}
				/>
				<Checkbox
					label="Ricorda su questo dispositivo"
					description="La password resta cifrata in questo browser."
					bind:checked={remember}
				/>
				<Button type="submit" variant="solid" full size="lg">Accedi</Button>
			</form>
		</Card>

		<Card title="Ricerca e scelta">
			<div class="form">
				<SearchField label="Cerca un corso" bind:value={search} shortcut={['/']} />
				<Select
					label="Anno accademico"
					bind:value={year}
					options={[
						{ value: '2026', label: '2026/2027' },
						{ value: '2025', label: '2025/2026' },
						{ value: '2024', label: '2024/2025' }
					]}
				/>
				<RadioGroup
					label="Fascia oraria"
					bind:value={slot}
					options={[
						{ value: 'morning', label: 'Mattina', description: '9:00–13:00 · 12 posti liberi' },
						{
							value: 'afternoon',
							label: 'Pomeriggio',
							description: '14:00–18:00 · completo',
							disabled: true
						}
					]}
				/>
				<Switch
					label="Notifiche"
					description="Avvisami quando escono i voti"
					bind:checked={notifications}
				/>
			</div>
		</Card>

		<Card title="Questionario">
			<div class="form">
				<RadioGroup
					label="Modalità d’esame preferita"
					variant="cards"
					bind:value={mode}
					options={[
						{ value: 'written', label: 'Scritto', description: 'Un’unica prova di 2 ore' },
						{ value: 'oral', label: 'Orale', description: 'Colloquio con il docente' }
					]}
				/>
				<Checkbox
					label="Seleziona tutti i moduli"
					bind:checked={terms}
					bind:indeterminate={mixed}
				/>
				<Textarea
					label="Suggerimenti"
					optional
					bind:value={message}
					maxlength={500}
					placeholder="Cosa miglioreresti di questo corso?"
				/>
				<TextField label="Codice lezione" value="ABC" error="Il codice non è valido o è scaduto." />
				<TextField label="Matricola" value="900123" disabled />
			</div>
		</Card>

		<Card title="Colonne strette">
			<div class="form">
				<div class="grades">
					<TextField label="Primo" bind:value={grades.first} inputmode="numeric" />
					<TextField label="Secondo" bind:value={grades.second} inputmode="numeric" />
					<TextField label="Terzo" bind:value={grades.third} inputmode="numeric" />
				</div>
				<Switch
					label="Accetto il regolamento della biblioteca"
					description="Il posto resta tuo per 15 minuti dall’inizio."
					bind:checked={consent}
					aria-invalid={consentTried && !consent}
					aria-describedby={consentTried && !consent ? 'consent-error' : undefined}
				/>
				{#if consentTried && !consent}
					<p class="error" id="consent-error">Per prenotare devi accettare il regolamento.</p>
				{/if}
				<Button onclick={() => (consentTried = true)}>Prenota</Button>
			</div>
		</Card>
	</div>
</Specimen>

<style>
	.row {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(min(100%, 320px), 1fr));
		align-items: start;
		gap: 16px;
	}

	.form {
		display: grid;
		gap: 18px;
	}

	/* Three fields in a column a third of a phone wide. */
	.grades {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 8px;
	}

	.error {
		color: var(--danger);
		font: var(--type-meta);
	}
</style>
