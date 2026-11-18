<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import { m } from '#lib/paraglide/messages.js';

	/** Esse3's "Rigenera password": the university account's own recovery page. */
	const RECOVERY_URL = 'https://s3w.si.unimib.it/Anagrafica/PasswordDimenticata.do';
</script>

<a class="forgot" href={RECOVERY_URL} target="_blank" rel="external noopener noreferrer">
	{m.auth_forgot_password()}
	<IconArrowSquareOut aria-hidden="true" />
</a>

<style>
	.forgot {
		position: relative;
		display: inline-flex;
		align-items: center;
		gap: 2px;
		justify-self: end;
		border-radius: var(--r-2xs);
		color: var(--accent-text);
		font: var(--type-meta);
		font-weight: 500;
		text-decoration: none;
	}

	/* A 44 px touch target around the short line of text. */
	.forgot::after {
		content: '';
		position: absolute;
		inset: -12px -6px;
	}

	.forgot :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	@media (hover: hover) {
		.forgot:hover {
			text-decoration: underline;
			text-underline-offset: 3px;
		}
	}

	.forgot:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}
</style>
