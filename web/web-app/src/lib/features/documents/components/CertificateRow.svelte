<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconShareNetwork from '~icons/ph/share-network';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import type { FileState } from '../certificateFiles.svelte.ts';
	import type { Certificate } from '../certificates.ts';
	import { certificateHref } from '../certificates.svelte.ts';
	import {
		certificateLine,
		certificateName,
		kindIcons,
		type OpenMode
	} from '../certificateView.ts';
	import { joinParts } from '../text.ts';

	interface Props {
		certificate: Certificate;
		state: FileState | undefined;
		/** A link to the preview, or a button that opens or saves the PDF from the row. */
		mode: OpenMode;
		selected?: boolean;
		/** The browser kept the PDF from opening in a new tab. */
		blocked?: boolean;
		/** Saved as a file in this visit. */
		saved?: boolean;
		share: boolean;
		onopen: () => void;
		ondownload: () => void;
		onshare: () => void;
	}

	let {
		certificate,
		state: fileState,
		mode,
		selected = false,
		blocked = false,
		saved = false,
		share,
		onopen,
		ondownload,
		onshare
	}: Props = $props();

	const name = $derived(certificateName(certificate));
	const preview = $derived(mode === 'preview');
	const pending = $derived(fileState?.status === 'pending');
	const file = $derived(fileState?.status === 'ready' ? fileState.file : undefined);
	const failure = $derived(fileState?.status === 'failed' ? fileState.error : undefined);

	let inner = $state<HTMLElement>();

	/* Retry goes away while the PDF generates: the row keeps the focus it had. */
	function retry() {
		inner?.focus();
		onopen();
	}

	const line = $derived.by(() => {
		// A preview explains a failure; without one, the row is all there is.
		if (failure && !preview) {
			return `${m.documents_certificates_failed()}. ${errorMessage(failure)}`;
		}
		if (blocked) return m.documents_certificates_blocked();
		if (pending) return m.documents_certificates_generating();
		const base = certificateLine(certificate);
		if (!file || preview) return base;
		const done = saved ? m.documents_certificates_saved() : m.documents_certificates_ready();
		return joinParts([base, done, formatBytes(file.blob.size)]);
	});
</script>

{#snippet description()}
	<span class={{ failed: !!failure && !preview, notice: blocked }}>{line}</span>
{/snippet}

{#snippet status()}
	<span class="status">
		{#if pending}
			<Spinner size={20} />
		{:else if failure && preview}
			<span class="failed-glyph"><IconWarningCircle aria-hidden="true" /></span>
			<span class="sr-only">{m.documents_certificates_failed()}</span>
		{:else if file}
			<span class="ready-glyph"><IconCheckCircle aria-hidden="true" /></span>
			{#if preview}<span class="sr-only">{m.documents_certificates_ready()}</span>{/if}
		{:else if mode === 'save' && !failure}
			<IconDownloadSimple aria-hidden="true" />
			<span class="sr-only">{m.documents_certificates_download()}</span>
		{/if}
	</span>
{/snippet}

{#snippet buttons()}
	{#if failure}
		<IconButton
			icon={IconArrowClockwise}
			label={m.documents_certificates_retry_named({ name })}
			onclick={retry}
		/>
	{:else}
		{#if share && file}
			<IconButton
				icon={IconShareNetwork}
				label={m.documents_certificates_share()}
				onclick={onshare}
			/>
		{/if}
		{#if mode === 'tab'}
			<IconButton
				icon={IconDownloadSimple}
				label={m.documents_certificates_download_named({ name })}
				disabled={pending}
				onclick={ondownload}
			/>
		{/if}
	{/if}
{/snippet}

<!-- A link to the preview, or a button that opens or saves the PDF; the preview has the actions. -->
<ListRow
	title={name}
	titleLang="it"
	spoken={mode === 'tab' ? m.documents_certificates_new_tab() : undefined}
	description={line ? description : undefined}
	icon={kindIcons[certificate.kind]}
	iconTone="accent"
	href={preview ? certificateHref(certificate.key) : undefined}
	onclick={preview ? undefined : onopen}
	{selected}
	trailing={status}
	chevron={false}
	actions={!preview && (failure || (share && file) || mode === 'tab') ? buttons : undefined}
	bind:element={inner}
/>

<style>
	.failed {
		color: var(--danger);
	}

	.notice {
		color: var(--text);
	}

	/* Always there, so a status appearing never rewraps the title. */
	.status {
		display: grid;
		place-items: center;
		inline-size: 24px;
		color: var(--text-subtle);
	}

	.status :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.ready-glyph {
		display: flex;
		color: var(--success);
	}

	.failed-glyph {
		display: flex;
		color: var(--danger);
	}
</style>
