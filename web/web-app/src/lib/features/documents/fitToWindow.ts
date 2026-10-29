import type { Attachment } from 'svelte/attachments';

/**
 * Sets `--fit-block` on the element to the height that ends it at the window's bottom edge, above
 * the paddings that close the page, so a frame scrolling on its own never scrolls the page as well.
 * It follows the window's height and whatever above moves the element down.
 */
export const fitToWindow: Attachment<HTMLElement> = (node) => {
	const fit = () => {
		let below = 0;
		let parent = node.parentElement;
		while (parent && parent !== document.body) {
			const style = getComputedStyle(parent);
			below += parseFloat(style.paddingBlockEnd) + parseFloat(style.borderBlockEndWidth);
			parent = parent.parentElement;
		}
		const top = node.getBoundingClientRect().top + window.scrollY;
		node.style.setProperty('--fit-block', `${Math.floor(window.innerHeight - top - below)}px`);
	};
	fit();
	const observer = new ResizeObserver(fit);
	observer.observe(document.body);
	window.addEventListener('resize', fit);
	return () => {
		observer.disconnect();
		window.removeEventListener('resize', fit);
	};
};
