import type { Esse3Certificate } from '@mybicocca/data-api/esse3';

export type CertificateKind = 'enrolment' | 'degree' | 'fees' | 'other';

/** The portal's own list of self-certifications, behind the student's SSO. */
export const CERTIFICATES_URL =
	'https://s3w.si.unimib.it/auth/studente/Certificati/ListaCertificati.do';

/** A self-certification the portal offers, with what prints it. */
export interface Certificate {
	/** Stable across loads and safe in a URL: `{configuration}-{document}[-{year}]`. */
	key: string;
	kind: CertificateKind;
	/** The portal's own name for it, in Italian. */
	description: string | null;
	solarYear: number | null;
	signed: boolean;
	source: Esse3Certificate;
}

function kind(type: string | null): CertificateKind {
	switch (type?.trim().toUpperCase()) {
		case 'I':
			return 'enrolment';
		case 'L':
			return 'degree';
		case 'TAX':
			return 'fees';
		default:
			return 'other';
	}
}

export function certificateKey(certificate: Esse3Certificate): string {
	const { configurationId, documentId, solarYear } = certificate;
	return solarYear === null
		? `${configurationId}-${documentId}`
		: `${configurationId}-${documentId}-${solarYear}`;
}

/**
 * The certificates once each: the portal links every declaration twice (its Italian and English
 * print), with the same configuration and year. The first link wins.
 */
export function toCertificates(list: readonly Esse3Certificate[]): Certificate[] {
	const seen = new Set<string>();
	const certificates: Certificate[] = [];
	for (const source of list) {
		const identity = `${source.configurationId}:${source.solarYear ?? ''}`;
		if (seen.has(identity)) continue;
		seen.add(identity);
		const description = source.description.replace(/\s+/g, ' ').trim();
		certificates.push({
			key: certificateKey(source),
			kind: kind(source.type),
			description: description === '' ? null : description,
			solarYear: source.solarYear,
			signed: (source.signatureFlag?.trim() ?? '') !== '',
			source
		});
	}
	return certificates;
}

const PDF_MAGIC = [0x25, 0x50, 0x44, 0x46, 0x2d];

/** Whether `bytes` are a PDF (`%PDF-`): an expired portal session answers the IdP's login page. */
export function isPdf(bytes: Uint8Array): boolean {
	return (
		bytes.length >= PDF_MAGIC.length && PDF_MAGIC.every((byte, index) => bytes[index] === byte)
	);
}

/**
 * A file name for the download: the description in plain ASCII letters, digits and underscores,
 * then the year, such as `Dichiarazione_delle_tasse_pagate_2025.pdf`.
 */
export function pdfFileName(description: string | null, solarYear: number | null): string {
	const base =
		(description ?? '')
			.normalize('NFD')
			.replace(/\p{M}/gu, '')
			.replace(/[^A-Za-z0-9 _-]/g, '')
			.trim()
			.replace(/\s+/g, '_')
			.slice(0, 80) || 'certificato';
	return solarYear === null ? `${base}.pdf` : `${base}_${solarYear}.pdf`;
}
