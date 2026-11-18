<script lang="ts">
	import IconFilePdf from '~icons/ph/file-pdf-duotone';
	import { m } from '#lib/paraglide/messages.js';
</script>

<!-- Beside the certificates with none open: what opening one does. -->
<div class="overview">
	<span class="glyph" aria-hidden="true"><IconFilePdf /></span>
	<div class="lead">
		<p class="statement">{m.documents_certificates_overview_title()}</p>
		<p class="hint">{m.documents_certificates_overview_body()}</p>
	</div>
</div>

<style>
	.overview {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: 560px;
		padding-block: var(--sp-2) var(--sp-4);
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 64px;
		block-size: 64px;
		border-radius: 20px;
		background: var(--container);
		color: var(--accent-text);
	}

	.glyph :global(svg) {
		inline-size: 34px;
		block-size: 34px;
	}

	.lead {
		display: grid;
		gap: var(--sp-2);
	}

	.statement {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
	}

	.hint {
		color: var(--text-muted);
		text-wrap: pretty;
	}
</style>
