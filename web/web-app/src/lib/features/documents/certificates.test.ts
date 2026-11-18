import type { Esse3Certificate } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { isPdf, pdfFileName, toCertificates } from './certificates.ts';

function link(fields: Partial<Esse3Certificate> = {}): Esse3Certificate {
	return {
		configurationId: 103,
		documentId: 3,
		description: 'Dichiarazione delle tasse pagate',
		type: 'TAX',
		solarYear: 2025,
		signatureFlag: '1',
		...fields
	};
}

describe('toCertificates', () => {
	it('keeps the first of the links a declaration has in each language', () => {
		const list = toCertificates([
			link(),
			link({ description: 'Statement of fees paid' }),
			link({ solarYear: 2024 }),
			link({ solarYear: 2024, description: 'Statement of fees paid' })
		]);
		expect(list.map((item) => [item.description, item.solarYear])).toEqual([
			['Dichiarazione delle tasse pagate', 2025],
			['Dichiarazione delle tasse pagate', 2024]
		]);
	});

	it('reads the kind, the signature and a key safe in a URL', () => {
		const [fees, enrolment, other] = toCertificates([
			link(),
			link({ configurationId: 101, type: 'I', solarYear: null, signatureFlag: ' ' }),
			link({ configurationId: 104, type: 'X', solarYear: null })
		]);
		expect(fees).toMatchObject({ key: '103-3-2025', kind: 'fees', signed: true });
		expect(enrolment).toMatchObject({ key: '101-3', kind: 'enrolment', signed: false });
		expect(other.kind).toBe('other');
	});

	it('tidies the link text, which spans lines on the page', () => {
		const [item] = toCertificates([link({ description: '\n  Certificato di\n  iscrizione  ' })]);
		expect(item.description).toBe('Certificato di iscrizione');
		expect(toCertificates([link({ description: '  ' })])[0].description).toBeNull();
	});
});

describe('isPdf', () => {
	const bytes = (text: string) => new TextEncoder().encode(text);

	it('tells a PDF from the login page an expired session serves', () => {
		expect(isPdf(bytes('%PDF-1.4\n…'))).toBe(true);
		expect(isPdf(bytes('<!doctype html><form action="/idp/profile/SAML2">'))).toBe(false);
		expect(isPdf(bytes('%PDF'))).toBe(false);
	});
});

describe('pdfFileName', () => {
	it('keeps plain letters, digits and dashes, and adds the year', () => {
		expect(pdfFileName('Dichiarazione delle tasse pagate', 2025)).toBe(
			'Dichiarazione_delle_tasse_pagate_2025.pdf'
		);
		expect(pdfFileName('Certificato d’iscrizione (con esami) – a.a. 2026/27', null)).toBe(
			'Certificato_discrizione_con_esami_aa_202627.pdf'
		);
		expect(pdfFileName('Università', null)).toBe('Universita.pdf');
		expect(pdfFileName(null, null)).toBe('certificato.pdf');
	});
});
