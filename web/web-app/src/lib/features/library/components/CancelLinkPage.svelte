<script lang="ts">
	import IconArmchair from '~icons/ph/armchair';
	import IconCalendarSlash from '~icons/ph/calendar-slash';
	import IconCalendarX from '~icons/ph/calendar-x-duotone';
	import IconClock from '~icons/ph/clock';
	import IconLink from '~icons/ph/link';
	import IconLinkBreak from '~icons/ph/link-break-duotone';
	import IconMapPin from '~icons/ph/map-pin';
	import { goto } from '$app/navigation';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Outcome from '#lib/ui/Outcome.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import { siteDate, whenLong } from '../format.ts';
	import { cancelHref, homeHref } from '../links.ts';
	import { cancelTokenOf, toCancelPreview } from '../model.ts';
	import { reservationToCancelQuery } from '../queries.ts';
	import { cancelBooking, writeFailure } from '../writes.ts';

	interface Props {
		/** The `reservationToken` of the emailed link. */
		token: string | null;
	}

	let { token }: Props = $props();

	const preview = query(() =>
		session.current && token ? reservationToCancelQuery(session.current, token) : undefined
	);
	const booking = $derived(preview.data ? toCancelPreview(preview.data) : undefined);
	/* Affluences answers an unknown or spent token with a bare 400. */
	const invalid = $derived(
		preview.error?.kind === 'not_found' || preview.error?.kind === 'rejected'
	);
	const gate = new LoadingGate(() => !preview.data && !preview.error);

	const range = $derived(
		booking?.day && booking.start !== undefined && booking.end !== undefined
			? { start: siteDate(booking.day, booking.start), end: siteDate(booking.day, booking.end) }
			: undefined
	);
	const when = $derived(range ? whenLong(range.start, range.end) : undefined);
	const seat = $derived(
		booking?.seat &&
			(booking.outlet ? `${booking.seat} · ${m.library_power_outlet()}` : booking.seat)
	);
	const facts = $derived.by((): Fact[] => [
		...(booking?.library
			? [{ label: m.library_fact_library(), value: booking.library, icon: IconMapPin }]
			: []),
		...(seat
			? [{ label: m.library_fact_seat(), value: seat, icon: IconArmchair, code: true }]
			: []),
		...(when ? [{ label: m.library_fact_when(), value: when, icon: IconClock }] : [])
	]);

	let confirming = $state(false);
	let cancelled = $state(false);

	async function cancel() {
		const scope = session.current;
		if (!scope || !token) return;
		await cancelBooking(scope, token);
		cancelled = true;
	}

	let pasted = $state('');
	let pasteError = $state<string>();
	let pasteInput = $state<HTMLInputElement>();

	function open(event: SubmitEvent) {
		event.preventDefault();
		const found = cancelTokenOf(pasted);
		if (found === undefined) {
			pasteError = m.library_cancel_link_unreadable();
			pasteInput?.focus();
			return;
		}
		void goto(cancelHref(found));
	}
</script>

<svelte:head>
	<title>{m.library_cancel_link_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="reading">
	<PageHeader
		title={m.library_cancel_link_title()}
		subtitle={m.library_cancel_link_subtitle()}
		back={{ href: homeHref(), label: m.library_title() }}
	/>

	{#if !token}
		<form class="paste" novalidate onsubmit={open}>
			<div class="paste-text">
				<h2 class="paste-title">{m.library_cancel_link_paste_title()}</h2>
				<p class="paste-body">{m.library_cancel_link_paste_body()}</p>
			</div>
			<TextField
				label={m.library_cancel_link_field()}
				type="url"
				inputmode="url"
				icon={IconLink}
				placeholder="https://affluences.com/…"
				spellcheck={false}
				bind:value={pasted}
				bind:input={pasteInput}
				error={pasteError}
				oninput={() => (pasteError = undefined)}
			/>
			<div class="actions">
				<Button type="submit" variant="solid">{m.library_cancel_link_open()}</Button>
			</div>
		</form>
	{:else if cancelled}
		<Outcome
			icon={IconCalendarX}
			tone="neutral"
			title={m.library_cancelled_title()}
			body={m.library_cancelled_body()}
		>
			{#snippet actions()}
				<Button variant="solid" href={homeHref()}>{m.library_go_to_bookings()}</Button>
			{/snippet}
		</Outcome>
	{:else if invalid}
		<EmptyState
			size="page"
			icon={IconLinkBreak}
			title={m.library_cancel_link_invalid()}
			body={m.library_cancel_link_invalid_body()}
		>
			{#snippet action()}
				<Button href={homeHref()}>{m.library_go_to_bookings()}</Button>
			{/snippet}
		</EmptyState>
	{:else if preview.error}
		<ErrorState
			title={m.library_cancel_link_failed()}
			message={errorMessage(preview.error)}
			onretry={preview.refresh}
			retrying={preview.refreshing}
		/>
	{:else if !booking}
		<div class="loading" aria-busy="true">
			{#if gate.visible}<Skeleton variant="block" height="180px" radius="var(--r-lg)" />{/if}
		</div>
	{:else}
		<Facts items={facts} label={m.library_reservation()} />
		{#if !booking.cancellable}
			<Banner tone="neutral" message={m.library_cancel_link_not_cancellable()} />
		{:else}
			{#if !network.online}
				<Banner tone="warning" message={m.library_offline_write()} />
			{/if}
			<div class="actions">
				<Button
					variant="danger"
					icon={IconCalendarSlash}
					disabled={!network.online}
					onclick={() => (confirming = true)}
				>
					{m.library_cancel()}
				</Button>
			</div>
		{/if}
	{/if}
</Page>

<ConfirmDialog
	bind:open={confirming}
	title={m.library_cancel_confirmation()}
	message={booking
		? m.library_confirm_cancel({
				seat: booking.seat ?? '',
				library: booking.library ?? '',
				when: range ? whenLong(range.start, range.end, true) : ''
			})
		: ''}
	confirmLabel={m.library_cancel()}
	cancelLabel={m.library_keep()}
	tone="danger"
	onconfirm={cancel}
	errorMessage={writeFailure(m.library_cancellation_failed)}
/>

<style>
	.loading {
		min-block-size: 180px;
	}

	.paste {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.paste-text {
		display: grid;
		gap: 4px;
	}

	.paste-title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.paste-body {
		max-inline-size: 60ch;
		color: var(--text-muted);
		font: var(--type-ui);
		text-wrap: pretty;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
	}
</style>
