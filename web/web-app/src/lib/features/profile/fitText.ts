import type { Attachment } from 'svelte/attachments';

/**
 * Shrinks a single line of text to fit its box, as Compose's auto-size does: sets `--fit` (at
 * most 1, at least `min`) for the element's font size to multiply. Pass the text, so the fit is
 * measured again when it changes; it is measured again when the box's width or the fonts change.
 */
export function fitText(text: string, min = 0): Attachment<HTMLElement> {
	return (node) => {
		let width = -1;
		const fit = () => {
			node.style.setProperty('--fit', '1');
			const ratio = node.scrollWidth > 0 ? node.clientWidth / node.scrollWidth : 1;
			node.style.setProperty('--fit', String(Math.max(min, Math.min(1, ratio))));
		};
		const resized = new ResizeObserver(() => {
			if (node.clientWidth === width) return;
			width = node.clientWidth;
			fit();
		});
		resized.observe(node);
		const fonts = () => fit();
		document.fonts.addEventListener('loadingdone', fonts);
		return () => {
			resized.disconnect();
			document.fonts.removeEventListener('loadingdone', fonts);
		};
	};
}
