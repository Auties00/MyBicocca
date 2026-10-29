import { goto } from '$app/navigation';
import { resolve } from '$app/paths';
import { isOpen, type Career } from './account.ts';
import { session } from './session.svelte.ts';

/** The career picker's state: the active account's careers, open ones first. */
export class CareerPick {
	readonly careers: Career[] = $derived(
		[...(session.active?.careers ?? [])].sort(
			(a, b) =>
				Number(isOpen(b.status)) - Number(isOpen(a.status)) || b.academicYear - a.academicYear
		)
	);
	selected = $state<number | undefined>(session.active?.selectedCareerId);
	pending = $state(false);

	confirm = async (): Promise<void> => {
		const account = session.active;
		if (account === undefined || this.selected === undefined || this.pending) return;
		this.pending = true;
		try {
			await session.selectCareer(account.id, this.selected);
			await goto(resolve('calendar'), { replace: true });
		} finally {
			this.pending = false;
		}
	};
}
