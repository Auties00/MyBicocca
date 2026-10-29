<script lang="ts">
	import { goto } from '$app/navigation';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import type { EasyStaffPlanningService } from '@mybicocca/data-api/easystaff';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError } from '#lib/data/errors.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { cache } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Dialog from '#lib/ui/Dialog.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { isEmail } from '../form.ts';
	import { reservationHref } from '../links.ts';
	import { servicesQuery } from '../queries.ts';
	import { recoverReservation } from '../reservations.ts';

	interface Props {
		open: boolean;
	}

	let { open = $bindable() }: Props = $props();

	let code = $state('');
	let email = $state(session.current?.account.username ?? '');
	let tried = $state(false);
	let busy = $state(false);
	let failure = $state<string>();

	const codeError = $derived(
		tried && code.trim() === '' ? m.appointments_add_code_required() : undefined
	);
	const emailError = $derived(tried && !isEmail(email) ? m.appointments_field_email() : undefined);
	const formId = $props.id();

	/* The directory, if this visit has it, names the booked service's group. */
	function serviceOf(id: number) {
		const scope = session.current;
		const services = scope
			? cache.peek<EasyStaffPlanningService[]>(servicesQuery(scope).key).data
			: undefined;
		return services?.find((item) => item.id === id);
	}

	async function submit(event: SubmitEvent) {
		event.preventDefault();
		tried = true;
		failure = undefined;
		const scope = session.current;
		if (!scope || codeError || emailError || busy) return;
		busy = true;
		try {
			const found = await recoverReservation(scope, code.trim(), email.trim(), serviceOf);
			if (found === undefined) {
				failure = m.appointments_add_not_found();
				return;
			}
			open = false;
			reset();
			toast.show({ message: m.appointments_add_done(), tone: 'success' });
			await goto(reservationHref(found.code));
		} catch (error) {
			failure = errorMessage(toDataError(error));
		} finally {
			busy = false;
		}
	}

	function reset() {
		tried = false;
		failure = undefined;
		code = '';
	}
</script>

<Dialog
	bind:open
	title={m.appointments_add_title()}
	description={m.appointments_add_description()}
	size="sm"
	dismissible={!busy}
	onclose={reset}
>
	<form id={formId} class="form" novalidate onsubmit={submit}>
		<TextField
			label={m.appointments_add_code()}
			bind:value={code}
			error={codeError}
			autocomplete="off"
			autocapitalize="characters"
			spellcheck={false}
			data-autofocus
		/>
		<TextField
			label={m.appointments_add_email()}
			type="email"
			bind:value={email}
			error={emailError}
			autocomplete="email"
			inputmode="email"
		/>
		{#if failure}
			<Banner tone="danger" message={failure} role="alert" />
		{:else if !network.online}
			<Banner tone="warning" message={m.appointments_add_offline()} role="status" />
		{/if}
	</form>
	{#snippet footer()}
		<Button variant="ghost" disabled={busy} onclick={() => (open = false)}>{m.ui_cancel()}</Button>
		<Button
			variant="solid"
			type="submit"
			form={formId}
			icon={IconMagnifyingGlass}
			loading={busy}
			disabled={!network.online}
		>
			{m.appointments_add_submit()}
		</Button>
	{/snippet}
</Dialog>

<style>
	.form {
		display: grid;
		gap: var(--sp-4);
	}
</style>
