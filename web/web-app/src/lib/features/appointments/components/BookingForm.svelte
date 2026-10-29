<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';
	import { m } from '#lib/paraglide/messages.js';
	import Select from '#lib/ui/Select.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import Textarea from '#lib/ui/Textarea.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import {
		CONSENT_ON,
		GDPR_LABEL,
		fieldProblem,
		namePart,
		type FieldProblem,
		type FieldValues,
		type FormField
	} from '../form.ts';

	interface Props {
		fields: readonly FormField[];
		values: FieldValues;
		/** Show what's missing: set by the first attempt to book. */
		checked: boolean;
		disabled: boolean;
		onchange: (code: string, value: string) => void;
	}

	let { fields, values, checked, disabled, onchange }: Props = $props();

	const id = $props.id();

	const problemText: Record<FieldProblem, () => string> = {
		required: m.appointments_field_required,
		email: m.appointments_field_email,
		consent: m.appointments_field_consent
	};

	function error(field: FormField): string | undefined {
		if (!checked) return undefined;
		const problem = fieldProblem(field, values[field.code] ?? '');
		return problem ? problemText[problem]() : undefined;
	}

	/** What the browser may fill in, guessed from the portal's Italian label. */
	function autocomplete(field: FormField): HTMLInputAttributes['autocomplete'] {
		if (field.kind === 'email') return 'email';
		if (field.kind === 'phone') return 'tel';
		switch (namePart(field.label)) {
			case 'full':
				return 'name';
			case 'family':
				return 'family-name';
			case 'given':
				return 'given-name';
			default:
				return 'off';
		}
	}

	const fiscal = (field: FormField) =>
		/codice fiscale|cod\. fiscale|^cf$/i.test(field.label.trim());
</script>

<div class="form">
	{#each fields as field (field.code)}
		{@const value = values[field.code] ?? ''}
		{#if field.kind === 'consent'}
			{@const problem = error(field)}
			<div class={['consent', { invalid: problem }]}>
				<p class="consent-text" id="{id}-{field.code}">
					{#if field.label === GDPR_LABEL}
						{m.appointments_gdpr_prefix()}{#if field.policyUrl}<a
								href={field.policyUrl}
								target="_blank"
								rel="noopener noreferrer external"
								>{m.appointments_gdpr_link()}<span class="sr-only">
									{m.appointments_new_tab()}</span
								></a
							>{:else}{m.appointments_gdpr_link()}{/if}{m.appointments_gdpr_suffix()}
					{:else if field.policyUrl}
						<a href={field.policyUrl} target="_blank" rel="noopener noreferrer external">
							{field.label}<span class="sr-only"> {m.appointments_new_tab()}</span>
						</a>
					{:else}
						{field.label}
					{/if}
				</p>
				<Switch
					checked={value === CONSENT_ON}
					{disabled}
					data-field={field.code}
					aria-labelledby="{id}-{field.code}"
					aria-invalid={problem ? true : undefined}
					aria-errormessage={problem ? `${id}-${field.code}-error` : undefined}
					onchange={(event) => onchange(field.code, event.currentTarget.checked ? CONSENT_ON : '')}
				/>
				{#if problem}
					<p class="consent-error" id="{id}-{field.code}-error">{problem}</p>
				{/if}
			</div>
		{:else if field.kind === 'select'}
			<Select
				label={field.label}
				options={[
					{ value: '', label: field.placeholder ?? m.appointments_select_placeholder() },
					...field.options
				]}
				{value}
				optional={!field.required}
				error={error(field)}
				{disabled}
				data-field={field.code}
				onchange={(next) => onchange(field.code, next)}
			/>
		{:else if field.kind === 'textarea'}
			<Textarea
				label={field.label}
				{value}
				rows={3}
				maxRows={6}
				placeholder={field.placeholder ?? undefined}
				optional={!field.required}
				error={error(field)}
				{disabled}
				data-field={field.code}
				oninput={(event) => onchange(field.code, event.currentTarget.value)}
			/>
		{:else}
			<TextField
				label={field.label}
				{value}
				type={field.kind === 'email' ? 'email' : field.kind === 'phone' ? 'tel' : 'text'}
				inputmode={field.kind === 'email' ? 'email' : field.kind === 'phone' ? 'tel' : undefined}
				autocomplete={autocomplete(field)}
				autocapitalize={fiscal(field) ? 'characters' : undefined}
				spellcheck={field.kind === 'text' && !fiscal(field) ? undefined : false}
				placeholder={field.placeholder ?? undefined}
				description={field.primary ? m.appointments_email_hint() : undefined}
				optional={!field.required && !field.primary}
				error={error(field)}
				{disabled}
				data-field={field.code}
				oninput={(event) => onchange(field.code, event.currentTarget.value)}
			/>
		{/if}
	{/each}
</div>

<style>
	.form {
		display: grid;
		gap: var(--sp-4);
	}

	.consent {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		align-items: center;
		gap: var(--sp-2) var(--sp-4);
		padding: var(--sp-3) var(--sp-4);
		border-radius: var(--r-md);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.consent.invalid {
		box-shadow: inset 0 0 0 1px var(--danger);
	}

	.consent-text {
		font: var(--type-ui);
		font-size: 0.9375rem;
		text-wrap: pretty;
	}

	.consent-text a {
		color: var(--accent-text);
		font-weight: 600;
		text-decoration: underline;
		text-decoration-thickness: 1px;
		text-underline-offset: 3px;
	}

	.consent-error {
		grid-column: 1 / -1;
		color: var(--danger);
		font: var(--type-meta);
	}
</style>
