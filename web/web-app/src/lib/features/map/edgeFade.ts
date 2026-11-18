import type { Attachment } from 'svelte/attachments';

/**
 * Marks a sideways-scrolling row with `data-more-start` / `data-more-end` while some of it is
 * scrolled out of sight that way, for its style to fade that edge.
 */
export const edgeFade: Attachment<HTMLElement> = (node) => {
	const update = () => {
		const hidden = node.scrollWidth - node.clientWidth;
		node.toggleAttribute('data-more-start', node.scrollLeft > 1);
		node.toggleAttribute('data-more-end', node.scrollLeft < hidden - 1);
	};
	const observer = new ResizeObserver(update);
	observer.observe(node);
	for (const child of node.children) observer.observe(child);
	node.addEventListener('scroll', update, { passive: true });
	return () => {
		observer.disconnect();
		node.removeEventListener('scroll', update);
	};
};
