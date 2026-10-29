<script lang="ts" module>
	export type PageTheme = 'system' | 'light' | 'dark';
</script>

<script lang="ts">
	import { decodeText } from '../../file/text.ts';

	interface Props {
		bytes: Uint8Array;
		name: string;
		theme: PageTheme;
	}

	let { bytes, name, theme }: Props = $props();

	const LIGHT = 'color-scheme: light; background: #fff; color: #1d1a1b;';
	const DARK = 'color-scheme: dark; background: #1a1617; color: #ece6e7;';
	const DARK_LINKS = 'a { color: #f0a4b4; } pre, code { background: #2b2526; }';

	/* No script runs (sandbox without allow-scripts, and a CSP that allows none); links open
	   outside, relative addresses resolve on Moodle; only images and styles may load. */
	const srcdoc = $derived.by(() => {
		const scheme =
			theme === 'light'
				? `html { ${LIGHT} }`
				: theme === 'dark'
					? `html { ${DARK} } ${DARK_LINKS}`
					: `html { ${LIGHT} } @media (prefers-color-scheme: dark) { html { ${DARK} } ${DARK_LINKS} }`;
		const head = [
			'<meta charset="utf-8">',
			`<meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src https: data:; style-src 'unsafe-inline' https:; font-src https: data:">`,
			'<base href="https://elearning.unimib.it/" target="_blank">',
			'<meta name="viewport" content="width=device-width, initial-scale=1">',
			`<style>${scheme} body { margin: 0 auto; padding: 24px 20px 96px; max-width: 72ch; font: 16px/1.6 system-ui, sans-serif; } img { max-width: 100%; height: auto; } pre { overflow: auto; padding: 12px; border-radius: 8px; } table { border-collapse: collapse; } td, th { border: 1px solid #8884; padding: 6px 10px; }</style>`
		].join('');
		return `<!doctype html><html><head>${head}</head><body>${decodeText(bytes).text}</body></html>`;
	});
</script>

<iframe
	class="page"
	title={name}
	{srcdoc}
	sandbox="allow-popups allow-popups-to-escape-sandbox"
	referrerpolicy="no-referrer"
></iframe>

<style>
	.page {
		display: block;
		inline-size: 100%;
		block-size: 100%;
		border: 0;
		background: var(--surface);
	}
</style>
