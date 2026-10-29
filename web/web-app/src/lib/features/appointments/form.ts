import type { EasyStaffPlanningFormField } from '@mybicocca/data-api/easystaff';
import type { JsonValue } from '@mybicocca/data-api/runtime';

export type FieldKind = 'email' | 'text' | 'phone' | 'textarea' | 'select' | 'consent';

export interface FieldOption {
	value: string;
	label: string;
}

/** A field of an office's booking form, as the page shows it. */
export interface FormField {
	code: string;
	label: string;
	kind: FieldKind;
	required: boolean;
	placeholder: string | null;
	/** The email whose value, with the reservation code, identifies the booking. */
	primary: boolean;
	options: FieldOption[];
	/** The privacy notice a consent links to. */
	policyUrl: string | null;
}

/** The untranslated key the portal sends as the label of its GDPR consent. */
export const GDPR_LABEL = 'gdpr_label';

/** Relative policy paths are files in the portal's storage. */
const STORAGE = 'https://gestioneorari.didattica.unimib.it/portaleplanningAPI/storage/';

function options(values: JsonValue | null): FieldOption[] {
	if (values === null) return [];
	if (Array.isArray(values)) {
		return values.flatMap((item) =>
			item === null || typeof item === 'object'
				? []
				: [{ value: String(item), label: String(item) }]
		);
	}
	if (typeof values === 'object') {
		return Object.entries(values).flatMap(([value, label]) =>
			label === null || typeof label === 'object' ? [] : [{ value, label: String(label) }]
		);
	}
	return [{ value: String(values), label: String(values) }];
}

function policyUrl(values: JsonValue | null): string | null {
	const first = options(values)[0]?.value.trim();
	if (!first) return null;
	return /^https?:\/\//i.test(first) ? first : STORAGE + first.replace(/^\/+/, '');
}

/** The GDPR section (position 100) and checkboxes are consents, which are never sent. */
const isConsent = (field: EasyStaffPlanningFormField) =>
	field.position === '100' || field.type === 'checkbox';

function kindOf(field: EasyStaffPlanningFormField): FieldKind {
	if (isConsent(field)) return 'consent';
	switch (field.type) {
		case 'email':
			return 'email';
		case 'textarea':
			return 'textarea';
		case 'tel':
			return 'phone';
		case 'select':
			return 'select';
		default:
			return 'text';
	}
}

/**
 * Where a field goes on the page: who is booking (section 1), what the desk asks (2), the
 * backoffice's fields, then the consents above the Book button. The portal lists its sections in
 * any order (the GDPR consent comes before a desk's own fields).
 */
function rank(field: EasyStaffPlanningFormField): number {
	if (isConsent(field)) return 4;
	switch (field.position) {
		case '1':
			return 0;
		case '2':
			return 1;
		case 'backoffice':
			return 2;
		default:
			return 3;
	}
}

const normalized = (text: string) => text.trim().replace(/\s+/g, ' ').toLocaleLowerCase('it-IT');

/** A placeholder that only repeats the label says nothing. */
function placeholderOf(field: EasyStaffPlanningFormField): string | null {
	const placeholder = field.placeholder?.trim();
	if (!placeholder || normalized(placeholder) === normalized(field.label)) return null;
	return placeholder;
}

/**
 * The fields a student fills, section by section, each section in the portal's order. Blocked
 * fields are left out: they hold read-only values of a logged-in portal profile, which an
 * anonymous booking never has.
 */
export function formFields(fields: readonly EasyStaffPlanningFormField[]): FormField[] {
	return fields
		.filter((field) => !field.blocked)
		.sort((a, b) => rank(a) - rank(b))
		.map((field) => {
			const kind = kindOf(field);
			return {
				code: field.code,
				label: field.label.trim(),
				kind,
				required: field.required,
				placeholder: placeholderOf(field),
				primary: field.primary,
				options: kind === 'select' ? options(field.values) : [],
				policyUrl: kind === 'consent' ? policyUrl(field.values) : null
			};
		});
}

export type FieldValues = Readonly<Record<string, string>>;

/** What a consent switch stores when on; off is blank. */
export const CONSENT_ON = 'true';

const TEXT_LIKE: readonly FieldKind[] = ['email', 'text', 'phone', 'textarea'];

/** Who is booking, from the signed-in account. */
export interface Identity {
	email: string | null;
	fullName: string;
	fiscalCode: string | null;
	studentNumber: string | null;
}

export type NamePart = 'full' | 'family' | 'given';

/**
 * Which part of a name a label asks for. "Cognome" holds "nome", so a label asks for the given
 * name, or the whole one, only when "nome" appears outside "cognome".
 */
export function namePart(label: string): NamePart | null {
	const text = label.toLocaleLowerCase('it-IT');
	const given = text.replaceAll('cognome', '').includes('nome');
	const family = text.includes('cognome');
	if (text.includes('nominativ') || text.includes('nome completo') || (given && family))
		return 'full';
	if (family) return 'family';
	return given ? 'given' : null;
}

function valueFor(field: FormField, identity: Identity): string | null {
	const label = field.label.toLocaleLowerCase('it-IT');
	const name = identity.fullName.trim().replace(/\s+/g, ' ');
	const space = name.lastIndexOf(' ');
	if (field.kind === 'email') return identity.email;
	switch (namePart(label)) {
		case 'full':
			// "Cognome e nome" wants the surname first, as Italian registers write it.
			return label.trim().startsWith('cognome') && space >= 0
				? `${name.slice(space + 1)} ${name.slice(0, space)}`
				: name;
		case 'family':
			return space < 0 ? name : name.slice(space + 1);
		case 'given':
			return space < 0 ? name : name.slice(0, space);
	}
	if (label.includes('matricola')) return identity.studentNumber;
	if (label.includes('codice fiscale') || label.includes('cod. fiscale') || label.trim() === 'cf')
		return identity.fiscalCode?.trim().toUpperCase() ?? null;
	if (label.includes('mail')) return identity.email;
	return null;
}

/** Values for the text fields the account already answers, matched on the field's label. */
export function prefill(fields: readonly FormField[], identity: Identity): Record<string, string> {
	const values: Record<string, string> = {};
	for (const field of fields) {
		if (!TEXT_LIKE.includes(field.kind)) continue;
		const value = valueFor(field, identity)?.trim();
		if (value) values[field.code] = value;
	}
	return values;
}

export function isEmail(value: string): boolean {
	return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
}

export type FieldProblem = 'required' | 'email' | 'consent';

/** Why `value` can't be sent for `field`, if it can't. */
export function fieldProblem(field: FormField, value: string): FieldProblem | undefined {
	const blank = value.trim() === '';
	switch (field.kind) {
		case 'consent':
			return field.required && value !== CONSENT_ON ? 'consent' : undefined;
		case 'email':
			if (blank) return field.required || field.primary ? 'required' : undefined;
			return isEmail(value) ? undefined : 'email';
		default:
			return field.required && blank ? 'required' : undefined;
	}
}

/** The fields that can't be sent as they are, in form order. */
export function formProblems(
	fields: readonly FormField[],
	values: FieldValues
): Array<{ field: FormField; problem: FieldProblem }> {
	return fields.flatMap((field) => {
		const problem = fieldProblem(field, values[field.code] ?? '');
		return problem ? [{ field, problem }] : [];
	});
}

/** The email the reservation will be keyed by. */
export function primaryEmail(fields: readonly FormField[], values: FieldValues): string | null {
	const field = fields.find((item) => item.primary) ?? fields.find((item) => item.kind === 'email');
	const value = field ? values[field.code]?.trim() : undefined;
	return value || null;
}

export interface SectionValues {
	user: Record<string, string>;
	service: Record<string, string>;
	backoffice: Record<string, string>;
}

/**
 * The values to send, by the section of the form each field belongs to. Blank values, consents
 * and fields of other sections are never sent, as on the portal's own page.
 */
export function sectionValues(
	fields: readonly EasyStaffPlanningFormField[],
	values: FieldValues
): SectionValues {
	const sections: SectionValues = { user: {}, service: {}, backoffice: {} };
	for (const field of fields) {
		if (isConsent(field)) continue;
		const value = values[field.code]?.trim();
		if (!value) continue;
		if (field.position === '1') sections.user[field.code] = value;
		else if (field.position === '2') sections.service[field.code] = value;
		else if (field.position === 'backoffice') sections.backoffice[field.code] = value;
	}
	return sections;
}
