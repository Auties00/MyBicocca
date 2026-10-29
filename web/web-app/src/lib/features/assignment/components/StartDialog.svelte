<script lang="ts">
	import { DataFailure, toDataError } from '#lib/data/errors.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import { siteTiming } from '../assignment.svelte.ts';
	import { failureText, formatSpan } from '../labels.ts';
	import { afterSubmissionChange } from '../queries.ts';
	import { startSubmission } from '../writes.ts';

	interface Props {
		open: boolean;
		courseId: number;
		assignId: number;
		/** The time the student will have (ms). */
		limit: number;
		/** The clock runs: the work can begin. */
		onstarted: () => void | Promise<void>;
	}

	let { open = $bindable(), courseId, assignId, limit, onstarted }: Props = $props();

	async function start() {
		const scope = session.current;
		if (scope === undefined) {
			throw new DataFailure({ kind: 'auth', service: 'moodle', reason: 'reauth_required' });
		}
		try {
			if ((await startSubmission(scope, assignId)) === 'untimed') siteTiming.off = true;
		} finally {
			afterSubmissionChange(scope, courseId, assignId);
		}
		await onstarted();
	}
</script>

<ConfirmDialog
	bind:open
	title={m.assignment_start_title()}
	message={m.assignment_start_body({ span: formatSpan(limit) })}
	confirmLabel={m.assignment_start_confirm()}
	onconfirm={start}
	errorMessage={(error) => failureText(toDataError(error))}
/>
