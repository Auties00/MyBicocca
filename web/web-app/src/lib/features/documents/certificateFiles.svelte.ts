import { untrack } from 'svelte';
import { SvelteMap } from 'svelte/reactivity';
import { DataFailure, toDataError, type DataError } from '#lib/data/errors.ts';
import { session, type SessionScope } from '#lib/data/session.svelte.ts';
import { isPdf, pdfFileName, type Certificate } from './certificates.ts';

/** A generated certificate, held as an object URL the browser's PDF viewer can open. */
export interface CertificateFile {
	url: string;
	blob: Blob;
	/** The name a download saves it under. */
	name: string;
}

export type FileState =
	| { status: 'pending' }
	| { status: 'ready'; file: CertificateFile }
	| { status: 'failed'; error: DataError };

const ACCOUNT_CHANGED: DataError = { kind: 'unknown', detail: 'The account changed meanwhile' };

/** The identity provider's login page, which the portal serves instead once its session expired. */
const LOGIN_PAGE = /j_username|SAML2|idp-idm\.unimib\.it/i;

/**
 * The certificates generated in this visit, by certificate. Esse3 prints each one on request, so
 * a file is fetched once and reopened from memory. They hold personal data and belong to one
 * account: switching account or signing out drops them and revokes their URLs.
 */
class CertificateFiles {
	#account: string | undefined;
	readonly #states = new SvelteMap<string, FileState>();
	readonly #running = new Map<string, Promise<CertificateFile>>();

	constructor() {
		$effect.root(() => {
			$effect(() => {
				const active = session.current?.account.id;
				untrack(() => {
					if (this.#account !== undefined && active !== this.#account) this.#own(active);
				});
			});
		});
	}

	state(key: string): FileState | undefined {
		return this.#states.get(key);
	}

	/** The file, generated now unless this visit already has it; one request per certificate. */
	generate(scope: SessionScope, certificate: Certificate): Promise<CertificateFile> {
		this.#own(scope.account.id);
		const current = this.#states.get(certificate.key);
		if (current?.status === 'ready') return Promise.resolve(current.file);
		const { key } = certificate;
		const running = this.#running.get(key);
		if (running !== undefined) return running;
		const task: Promise<CertificateFile> = this.#fetch(scope, certificate).finally(() => {
			// After a switch of account another account's task may hold the key.
			if (this.#running.get(key) === task) this.#running.delete(key);
		});
		this.#running.set(key, task);
		return task;
	}

	/** Generates the certificate again, as when the data it prints has changed. */
	regenerate(scope: SessionScope, certificate: Certificate): Promise<CertificateFile> {
		this.#own(scope.account.id);
		const current = this.#states.get(certificate.key);
		if (current?.status === 'ready') URL.revokeObjectURL(current.file.url);
		if (!this.#running.has(certificate.key)) this.#states.delete(certificate.key);
		return this.generate(scope, certificate);
	}

	async #fetch(scope: SessionScope, certificate: Certificate): Promise<CertificateFile> {
		const { key, source } = certificate;
		this.#states.set(key, { status: 'pending' });
		const account = scope.account.id;
		try {
			const bytes = await scope.api.esse3Web(async (portal) => {
				const data = await portal.certificates.getCertificatePdf(
					source.configurationId,
					source.documentId,
					{
						typeCode: source.type,
						solarYear: source.solarYear,
						signatureFlag: source.signatureFlag
					}
				);
				if (isPdf(data)) return data;
				// A login page means the session expired: a new one and one more try.
				if (LOGIN_PAGE.test(new TextDecoder().decode(data))) return null;
				throw new DataFailure({ kind: 'no_file', service: 'esse3Web' });
			});
			// Another account's papers are neither kept nor handed to whoever asked.
			if (this.#account !== account) throw new DataFailure(ACCOUNT_CHANGED);
			const blob = new Blob([bytes as Uint8Array<ArrayBuffer>], { type: 'application/pdf' });
			const file = {
				url: URL.createObjectURL(blob),
				blob,
				name: pdfFileName(certificate.description, certificate.solarYear)
			};
			this.#states.set(key, { status: 'ready', file });
			return file;
		} catch (error) {
			if (this.#account === account) {
				this.#states.set(key, { status: 'failed', error: toDataError(error) });
			}
			throw error;
		}
	}

	/** Forgets another account's files, revoking their URLs. */
	#own(account: string | undefined): void {
		if (this.#account === account) return;
		for (const state of this.#states.values()) {
			if (state.status === 'ready') URL.revokeObjectURL(state.file.url);
		}
		this.#states.clear();
		this.#running.clear();
		this.#account = account;
	}
}

export const certificateFiles = new CertificateFiles();
