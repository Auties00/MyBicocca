export type ToastTone = 'neutral' | 'success' | 'info' | 'warning' | 'danger';

export interface ToastOptions {
	message: string;
	description?: string;
	tone?: ToastTone;
	/** A button in the toast; it closes the toast after running. */
	action?: { label: string; onclick: () => void };
	/**
	 * Restores what a destructive action did ("Prenotazione annullata · Annulla"). Shows an Undo
	 * button and keeps the toast longer.
	 */
	undo?: () => void;
	/** Milliseconds before it leaves; 0 keeps it until dismissed. */
	duration?: number;
}

export interface Toast extends ToastOptions {
	id: number;
	tone: ToastTone;
	duration: number;
	createdAt: number;
}

const defaultDuration = 5000;
const undoDuration = 8000;
const maxQueued = 5;

class Toaster {
	items = $state<Toast[]>([]);
	/** Text for the polite live region: the latest toast. */
	announcement = $state('');
	#next = 1;

	show(options: ToastOptions): number {
		const toast: Toast = {
			...options,
			id: this.#next++,
			tone: options.tone ?? 'neutral',
			duration: options.duration ?? (options.undo ? undoDuration : defaultDuration),
			createdAt: Date.now()
		};
		this.items = [toast, ...this.items].slice(0, maxQueued);
		this.announcement = [toast.message, toast.description].filter(Boolean).join('. ');
		return toast.id;
	}

	dismiss(id: number) {
		this.items = this.items.filter((t) => t.id !== id);
	}

	success(message: string, options: Omit<ToastOptions, 'message' | 'tone'> = {}) {
		return this.show({ ...options, message, tone: 'success' });
	}

	info(message: string, options: Omit<ToastOptions, 'message' | 'tone'> = {}) {
		return this.show({ ...options, message, tone: 'info' });
	}

	/** For background failures only; failures of an action the user just took are shown in place. */
	error(message: string, options: Omit<ToastOptions, 'message' | 'tone'> = {}) {
		return this.show({ ...options, message, tone: 'danger' });
	}
}

/**
 * The global toast stack, rendered by `ToastHost`.
 *
 * ```ts
 * toast.show({ message: m.exams_cancelled(), undo: () => rebook(id) });
 * ```
 */
export const toast = new Toaster();
