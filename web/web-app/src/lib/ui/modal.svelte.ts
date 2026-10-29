import type { Attachment } from 'svelte/attachments';

interface ModalOptions {
	open: () => boolean;
	dismissible: () => boolean;
	/** Called when the dialog closes on its own (Escape, outside click, a form with method=dialog). */
	close: () => void;
	/** Called once the dialog has closed, whatever closed it. */
	closed?: () => void;
}

/**
 * Drives a native `<dialog>` from an `open` flag: modal, top layer, focus returns to the opener on
 * close. Escape and a click on the backdrop close it unless it is not dismissible. Initial focus
 * goes to `[autofocus]` if present, else to the dialog itself so its title is announced.
 */
export function modal(options: ModalOptions): Attachment<HTMLDialogElement> {
	return (dialog) => {
		dialog.tabIndex = -1;

		$effect(() => {
			const open = options.open();
			if (open && !dialog.open) {
				dialog.showModal();
				const target = dialog.querySelector<HTMLElement>('[autofocus], [data-autofocus]') ?? dialog;
				target.focus();
			} else if (!open && dialog.open) {
				dialog.close();
			}
		});

		let pressedOutside = false;
		const outside = (e: PointerEvent | MouseEvent) => {
			if (e.target !== dialog) return false;
			const r = dialog.getBoundingClientRect();
			return e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom;
		};
		const pointerdown = (e: PointerEvent) => {
			pressedOutside = outside(e);
		};
		const click = (e: MouseEvent) => {
			if (pressedOutside && outside(e) && options.dismissible()) dialog.close();
			pressedOutside = false;
		};
		const cancel = (e: Event) => {
			if (!options.dismissible()) e.preventDefault();
		};
		const close = () => {
			if (options.open()) options.close();
			options.closed?.();
		};

		dialog.addEventListener('pointerdown', pointerdown);
		dialog.addEventListener('click', click);
		dialog.addEventListener('cancel', cancel);
		dialog.addEventListener('close', close);
		return () => {
			dialog.removeEventListener('pointerdown', pointerdown);
			dialog.removeEventListener('click', click);
			dialog.removeEventListener('cancel', cancel);
			dialog.removeEventListener('close', close);
			if (dialog.open) dialog.close();
		};
	};
}
