<script lang="ts">
	import { SvelteSet } from 'svelte/reactivity';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import { certificateFiles } from '../certificateFiles.svelte.ts';
	import type { Certificate } from '../certificates.ts';
	import { certificateHref, type Certificates } from '../certificates.svelte.ts';
	import {
		canShareFiles,
		certificateName,
		saveFile,
		shareFile,
		viewerUrl,
		type OpenMode
	} from '../certificateView.ts';
	import CertificateRow from './CertificateRow.svelte';

	interface Props {
		certificates: Certificates;
		mode: OpenMode;
		/** The certificate previewed beside the list. */
		selected?: string;
	}

	let { certificates, mode, selected }: Props = $props();

	const share = canShareFiles();
	/** The certificate whose new tab the browser blocked: its row offers the PDF instead. */
	let blocked = $state<string>();
	const saved = new SvelteSet<string>();
	/** The certificate last acted on, whose outcome the status line announces. */
	let last = $state<string>();

	/**
	 * Opens the PDF in a new tab, in the browser's own viewer. The tab opens at once, while the
	 * tap still counts as one (later, popup blockers refuse it), and gets the PDF once ready.
	 */
	async function openInTab(certificate: Certificate) {
		const scope = session.current;
		if (!scope) return;
		blocked = undefined;
		const ready = certificateFiles.state(certificate.key);
		if (ready?.status === 'ready') {
			if (!window.open(viewerUrl(ready.file.url, false), '_blank')) blocked = certificate.key;
			return;
		}
		const tab = window.open('', '_blank');
		if (tab) {
			tab.opener = null;
			tab.document.title = certificateName(certificate);
			tab.document.body.textContent = m.documents_certificates_generating();
			tab.document.body.style.cssText =
				'color-scheme: light dark; margin: 0; padding: 32px; font: 16px system-ui, sans-serif;';
		}
		try {
			const file = await certificateFiles.generate(scope, certificate);
			if (tab && !tab.closed) tab.location.replace(viewerUrl(file.url, false));
			else blocked = certificate.key;
		} catch {
			tab?.close();
		}
	}

	/** Generates the PDF and saves it, as Android hands it to a viewer app. */
	async function save(certificate: Certificate) {
		const scope = session.current;
		if (!scope) return;
		try {
			const file = await certificateFiles.generate(scope, certificate);
			saveFile(file.url, file.name);
			saved.add(certificate.key);
		} catch {
			// The row shows the failure.
		}
	}

	function open(certificate: Certificate) {
		last = certificate.key;
		void (mode === 'tab' ? openInTab(certificate) : save(certificate));
	}

	function download(certificate: Certificate) {
		last = certificate.key;
		void save(certificate);
	}

	function shareCertificate(certificate: Certificate) {
		const state = certificateFiles.state(certificate.key);
		if (state?.status === 'ready') {
			void shareFile(state.file.blob, state.file.name, certificateName(certificate));
		}
	}

	const announcement = $derived.by(() => {
		const state = last === undefined ? undefined : certificateFiles.state(last);
		switch (state?.status) {
			case undefined:
				return '';
			case 'pending':
				return m.documents_certificates_generating();
			case 'failed':
				return `${m.documents_certificates_failed()}. ${errorMessage(state.error)}`;
			case 'ready': {
				const done =
					last !== undefined && saved.has(last)
						? m.documents_certificates_saved()
						: m.documents_certificates_ready();
				return `${done}, ${m.documents_certificates_size({ size: formatBytes(state.file.blob.size) })}`;
			}
		}
	});
</script>

<!-- The pane around the list is named already. -->
<ListGroup>
	{#each certificates.list as certificate (certificate.key)}
		<CertificateRow
			{certificate}
			state={certificateFiles.state(certificate.key)}
			{mode}
			selected={mode === 'preview' && certificateHref(certificate.key) === selected}
			blocked={blocked === certificate.key}
			saved={saved.has(certificate.key)}
			{share}
			onopen={() => open(certificate)}
			ondownload={() => download(certificate)}
			onshare={() => shareCertificate(certificate)}
		/>
	{/each}
</ListGroup>
<p class="sr-only" role="status">{announcement}</p>
