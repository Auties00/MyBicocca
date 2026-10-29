<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { appVersion, COPYRIGHT_HOLDER, copyrightYears } from '../about.ts';
</script>

<!-- The quiet signature under the list of settings: name, version and copyright. -->
<p class="colophon">
	<span class="part" translate="no">{m.app_name()} {appVersion}</span> ·
	<span class="part">© {copyrightYears()} <span translate="no">{COPYRIGHT_HOLDER}</span></span>
</p>

<style>
	.colophon {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-align: center;
		text-wrap: balance;
	}

	.part {
		white-space: nowrap;
	}
</style>
