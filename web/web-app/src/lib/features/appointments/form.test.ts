import { decodeEasyStaffPlanningFormField } from '@mybicocca/data-api/easystaff';
import { describe, expect, it } from 'vitest';
import {
	CONSENT_ON,
	fieldProblem,
	formFields,
	formProblems,
	namePart,
	prefill,
	primaryEmail,
	sectionValues,
	type FormField
} from './form.ts';

let next = 1;
const wire = (fields: Record<string, unknown>) => {
	const decoded = decodeEasyStaffPlanningFormField({ id: next++, cliente: 2, ...fields });
	if (decoded === undefined) throw new Error('Not a field');
	return decoded;
};

/* As the live portal sends a desk's form: its own fields after the GDPR consent, flags as 0/1. */
const raw = [
	wire({ label: 'Email', codice: 'email', tipo: 'email', primaria: true, obbligatorio: true }),
	wire({ label: 'Nome', codice: 'nome', tipo: 'text', obbligatorio: true }),
	wire({ label: 'Cognome', codice: 'cognome', tipo: 'text', obbligatorio: true }),
	wire({ label: 'Codice fiscale', codice: 'cf', tipo: 'text' }),
	wire({ label: 'Matricola', codice: 'matricola', tipo: 'text', obbligatorio: 0 }),
	wire({ label: 'Telefono', codice: 'tel', tipo: 'tel' }),
	wire({
		label: 'gdpr_label',
		codice: 'gdpr',
		tipo: 'checkbox',
		obbligatorio: 1,
		valori: 'privacy/informativa.pdf',
		posizione: 100
	}),
	wire({
		label: 'Motivo',
		codice: 'motivo',
		tipo: 'select',
		obbligatorio: 1,
		valori: { iscrizione: 'Iscrizione', altro: 'Altro' },
		posizione: 2
	}),
	wire({
		label: 'Note',
		codice: 'note',
		tipo: 'textarea',
		placeholder: ' note ',
		posizione: '2'
	}),
	wire({ label: 'Esito', codice: 'esito', tipo: 'text', blocked: true, posizione: 'backoffice' }),
	wire({ label: 'Sportello', codice: 'desk', tipo: 'text', posizione: 'backoffice' })
];

describe('formFields', () => {
	const fields = formFields(raw);
	const byCode = (code: string) => fields.find((field) => field.code === code);

	it('orders the sections as the page shows them, consents last, and leaves out blocked fields', () => {
		expect(fields.map((field) => field.code)).toEqual([
			'email',
			'nome',
			'cognome',
			'cf',
			'matricola',
			'tel',
			'motivo',
			'note',
			'desk',
			'gdpr'
		]);
	});

	it('maps the portal types and flags', () => {
		expect(byCode('email')).toMatchObject({ kind: 'email', primary: true, required: true });
		expect(byCode('matricola')?.required).toBe(false);
		expect(byCode('tel')?.kind).toBe('phone');
		expect(byCode('note')?.kind).toBe('textarea');
		expect(byCode('motivo')).toMatchObject({ kind: 'select', required: true });
		expect(byCode('motivo')?.options).toEqual([
			{ value: 'iscrizione', label: 'Iscrizione' },
			{ value: 'altro', label: 'Altro' }
		]);
	});

	it('drops a placeholder that repeats the label', () => {
		expect(byCode('note')?.placeholder).toBeNull();
		const [field] = formFields([
			wire({ label: 'Motivo', codice: 'm', tipo: 'text', placeholder: 'Es. cambio corso' })
		]);
		expect(field.placeholder).toBe('Es. cambio corso');
	});

	it('makes the GDPR checkbox a required consent linking to the policy in the portal storage', () => {
		expect(byCode('gdpr')).toMatchObject({
			kind: 'consent',
			required: true,
			policyUrl:
				'https://gestioneorari.didattica.unimib.it/portaleplanningAPI/storage/privacy/informativa.pdf'
		});
	});

	it('reads select options from lists and single values too', () => {
		const [list, single] = formFields([
			wire({ label: 'A', codice: 'a', tipo: 'select', valori: ['Sì', 'No'] }),
			wire({ label: 'B', codice: 'b', tipo: 'checkbox', valori: 'https://example.org/p.pdf' })
		]);
		expect(list.options).toEqual([
			{ value: 'Sì', label: 'Sì' },
			{ value: 'No', label: 'No' }
		]);
		expect(single.policyUrl).toBe('https://example.org/p.pdf');
	});
});

describe('namePart', () => {
	it('tells a surname from a given name, though "cognome" holds "nome"', () => {
		expect(namePart('Cognome')).toBe('family');
		expect(namePart('Nome')).toBe('given');
		expect(namePart('Cognome e nome')).toBe('full');
		expect(namePart('Nominativo')).toBe('full');
		expect(namePart('Matricola')).toBeNull();
	});
});

describe('prefill', () => {
	const identity = {
		email: 'm.rossi99@campus.unimib.it',
		fullName: 'Marco  Rossi Bianchi',
		fiscalCode: ' rssmrc03a01f205x ',
		studentNumber: '900123'
	};

	it('answers the text fields the account knows, matched on their label', () => {
		expect(prefill(formFields(raw), identity)).toEqual({
			email: 'm.rossi99@campus.unimib.it',
			nome: 'Marco Rossi',
			cognome: 'Bianchi',
			cf: 'RSSMRC03A01F205X',
			matricola: '900123'
		});
	});

	it('gives a full-name field the whole name and leaves selects alone', () => {
		const fields = formFields([
			wire({ label: 'Nome e cognome', codice: 'name', tipo: 'text' }),
			wire({ label: 'Cognome e Nome', codice: 'surname_first', tipo: 'text' }),
			wire({ label: 'Nominativo del delegato', codice: 'delegate', tipo: 'text' }),
			wire({ label: 'Nome corso', codice: 'course', tipo: 'select', valori: ['A'] })
		]);
		expect(prefill(fields, identity)).toEqual({
			name: 'Marco Rossi Bianchi',
			surname_first: 'Bianchi Marco Rossi',
			delegate: 'Marco Rossi Bianchi'
		});
	});

	it('skips what the account doesn’t know', () => {
		const values = prefill(formFields(raw), {
			email: null,
			fullName: '',
			fiscalCode: null,
			studentNumber: null
		});
		expect(values).toEqual({});
	});
});

describe('fieldProblem', () => {
	const field = (kind: FormField['kind'], required: boolean, primary = false): FormField => ({
		code: 'x',
		label: 'X',
		kind,
		required,
		primary,
		placeholder: null,
		options: [],
		policyUrl: null
	});

	it('requires the primary email even when the portal marks it optional', () => {
		expect(fieldProblem(field('email', false, true), '')).toBe('required');
		expect(fieldProblem(field('email', false), '')).toBeUndefined();
		expect(fieldProblem(field('email', false), 'nope')).toBe('email');
		expect(fieldProblem(field('email', true), 'a@b')).toBe('email');
		expect(fieldProblem(field('email', true), ' a@b.it ')).toBeUndefined();
	});

	it('wants a required consent switched on', () => {
		expect(fieldProblem(field('consent', true), '')).toBe('consent');
		expect(fieldProblem(field('consent', true), CONSENT_ON)).toBeUndefined();
		expect(fieldProblem(field('consent', false), '')).toBeUndefined();
	});

	it('counts blanks as missing', () => {
		expect(fieldProblem(field('select', true), '')).toBe('required');
		expect(fieldProblem(field('text', true), '   ')).toBe('required');
		expect(fieldProblem(field('textarea', false), '')).toBeUndefined();
	});
});

describe('formProblems and primaryEmail', () => {
	const fields = formFields(raw);

	it('lists what stops the booking, in form order', () => {
		const problems = formProblems(fields, { email: 'a@b.it', nome: 'Marco', cognome: 'Rossi' });
		expect(problems.map((item) => [item.field.code, item.problem])).toEqual([
			['motivo', 'required'],
			['gdpr', 'consent']
		]);
	});

	it('reads the email the booking is keyed by', () => {
		expect(primaryEmail(fields, { email: ' a@b.it ' })).toBe('a@b.it');
		expect(primaryEmail(fields, {})).toBeNull();
	});
});

describe('sectionValues', () => {
	it('sends each value under its section, never consents or blanks', () => {
		expect(
			sectionValues(raw, {
				email: 'a@b.it',
				nome: ' Marco ',
				cognome: '',
				motivo: 'altro',
				note: 'Piano di studi',
				gdpr: CONSENT_ON,
				desk: '3'
			})
		).toEqual({
			user: { email: 'a@b.it', nome: 'Marco' },
			service: { motivo: 'altro', note: 'Piano di studi' },
			backoffice: { desk: '3' }
		});
	});
});
