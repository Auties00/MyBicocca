/** Whether this browser takes `types` in `startViewTransition` (the CSS keys off them). */
const typedTransitions =
	typeof ViewTransition !== 'undefined' && 'types' in ViewTransition.prototype;

/** `document.startViewTransition` with a type for the CSS, where the browser supports types. */
export function startTypedTransition(
	update: () => void | Promise<void>,
	type: string
): ViewTransition {
	return typedTransitions
		? document.startViewTransition({ update, types: [type] })
		: document.startViewTransition(update);
}
