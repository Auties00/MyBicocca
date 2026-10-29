<script lang="ts">
	import IconArmchair from '~icons/ph/armchair';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconAt from '~icons/ph/at';
	import IconBooks from '~icons/ph/books';
	import IconClock from '~icons/ph/clock';
	import IconEnvelope from '~icons/ph/envelope-simple';
	import IconSquares from '~icons/ph/squares-four';
	import type { AffluencesAgreement } from '@mybicocca/data-api/affluences';
	import { tick } from 'svelte';
	import { beforeNavigate } from '$app/navigation';
	import { page } from '$app/state';
	import type { ResolvedPathname } from '$app/types';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatTime } from '#lib/format/dates.ts';
	import type { IsoDate } from '#lib/format/days.ts';
	import { personName } from '#lib/format/names.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import type { LibraryLink } from '../account.svelte.ts';
	import { durationLabel, siteDate, whenLong } from '../format.ts';
	import { loginHref } from '../links.ts';
	import type { Seat, Zone } from '../model.ts';
	import { bookSeat, writeFailure } from '../writes.ts';

	interface Props {
		siteId: string;
		library: { name: string; place: string | null };
		zone: Zone;
		seat: Seat;
		/** Whether the seat is still bookable at `start`, as the last load of the free seats says. */
		free: boolean;
		day: IsoDate;
		start: number;
		duration: number;
		link: LibraryLink;
		agreements: readonly AffluencesAgreement[];
		/** The seat step, to pick another seat for the same time. */
		seatsHref: ResolvedPathname;
		/** The booking went through; Affluences may give its id. */
		onbooked: (id: number | null) => void;
		/** The booking failed: the free seats may have changed. */
		onfailed: () => void;
	}

	let {
		siteId,
		library,
		zone,
		seat,
		free,
		day,
		start,
		duration,
		link,
		agreements,
		seatsHref,
		onbooked,
		onfailed
	}: Props = $props();

	const id = $props.id();
	const startDate = $derived(siteDate(day, start));
	const endDate = $derived(siteDate(day, start + duration));
	const when = $derived(`${whenLong(startDate, endDate)} · ${durationLabel(duration)}`);
	const agreement = $derived(agreements.find((item) => item.mandatoryConsent));
	const terms = $derived(
		agreement?.url && /^https?:\/\//.test(agreement.url) ? agreement.url : undefined
	);
	const asksNote = $derived(seat.noteRequired || seat.noteLabel !== null);

	let note = $state(personName(session.current?.account.displayName ?? ''));
	let accepted = $state(false);
	let tried = $state(false);
	let confirming = $state(false);
	let booking = $state(false);
	let failure = $state<string>();
	let noteInput = $state<HTMLInputElement>();

	const noteError = $derived(
		tried && asksNote && seat.noteRequired && note.trim() === ''
			? m.library_note_required()
			: undefined
	);
	const consentError = $derived(
		tried && agreement && !accepted ? m.library_consent_required() : undefined
	);

	const facts = $derived.by((): Fact[] => [
		{
			label: m.library_fact_library(),
			value: library.place ? `${library.name} · ${library.place}` : library.name,
			icon: IconBooks
		},
		{
			label: m.library_fact_zone(),
			value: zone.name ?? m.library_zone_fallback(),
			icon: IconSquares
		},
		{
			label: m.library_fact_seat(),
			value: [seat.short, seat.outlet ? m.library_power_outlet() : seat.description]
				.filter(Boolean)
				.join(' · '),
			icon: IconArmchair
		},
		{ label: m.library_fact_when(), value: when, icon: IconClock },
		{ label: m.library_fact_book_as(), value: link.email ?? '—', icon: IconAt }
	]);

	async function submit(event: SubmitEvent) {
		event.preventDefault();
		tried = true;
		if (noteError) {
			noteInput?.focus();
			return;
		}
		if (consentError) {
			await tick();
			document.getElementById(`${id}-consent`)?.focus();
			return;
		}
		if (!free || !link.auth || !network.online) return;
		confirming = true;
	}

	async function book() {
		const scope = session.current;
		if (!scope || booking) return;
		booking = true;
		failure = undefined;
		let reservation: number | null;
		try {
			reservation = await bookSeat(scope, link, {
				siteId,
				seatId: seat.id,
				day,
				start,
				duration,
				note: asksNote ? note.trim() || null : null
			});
		} catch (error) {
			// Told in place, above the recap, once the dialog has closed; the free seats reload.
			failure = writeFailure(m.library_retry_soon)(error);
			onfailed();
			return;
		} finally {
			booking = false;
		}
		onbooked(reservation);
	}

	beforeNavigate((navigation) => {
		if (booking) navigation.cancel();
	});

	function guard(event: BeforeUnloadEvent) {
		if (booking) event.preventDefault();
	}
</script>

<svelte:window onbeforeunload={guard} />

<form class="confirm" novalidate onsubmit={submit}>
	{#if !free}
		<Banner
			tone="warning"
			title={m.library_seat_taken({
				seat: seat.short,
				time: formatTime(startDate)
			})}
			message={m.library_seat_taken_body()}
			role="alert"
		>
			{#snippet actions()}
				<Button size="sm" variant="solid" href={seatsHref}>{m.library_pick_another_seat()}</Button>
			{/snippet}
		</Banner>
	{:else if failure}
		<Banner tone="danger" title={m.library_booking_failed()} message={failure} role="alert" />
	{/if}

	<Facts items={facts} label={m.library_step_confirm()} />

	{#if asksNote || agreement}
		<div class="fields">
			{#if asksNote}
				<TextField
					label={seat.noteLabel ?? m.library_note_fallback()}
					bind:value={note}
					bind:input={noteInput}
					optional={!seat.noteRequired}
					error={noteError}
					autocomplete="name"
					disabled={booking}
				/>
			{/if}
			{#if agreement}
				<div class="consent">
					<label class="consent-text" for="{id}-consent">
						{m.library_consent()}
						{#if terms}
							<a class="terms" href={terms} target="_blank" rel="external noopener noreferrer">
								{m.library_consent_name({ name: agreement.name ?? m.library_terms() })}
								<IconArrowSquareOut aria-hidden="true" />
							</a>
						{:else}
							{m.library_consent_name({ name: agreement.name ?? m.library_terms() })}
						{/if}
					</label>
					<Switch
						id="{id}-consent"
						bind:checked={accepted}
						disabled={booking}
						aria-invalid={consentError ? true : undefined}
						aria-describedby={consentError ? `${id}-consent-error` : undefined}
					/>
				</div>
				{#if consentError}
					<p class="error" id="{id}-consent-error" role="alert">{consentError}</p>
				{/if}
			{/if}
		</div>
	{/if}

	{#if !network.online}
		<Banner tone="warning" message={m.library_offline_write()} />
	{/if}

	<div class="actions">
		{#if link.auth}
			<Button
				type="submit"
				variant="solid"
				size="lg"
				icon={IconArmchair}
				disabled={!network.online || !free}
			>
				{m.library_book_ellipsis()}
			</Button>
		{:else}
			<Button
				variant="solid"
				size="lg"
				icon={IconEnvelope}
				href={loginHref(`${page.url.pathname}${page.url.search}` as ResolvedPathname)}
			>
				{m.library_verify_to_book()}
			</Button>
		{/if}
	</div>
</form>

<ConfirmDialog
	bind:open={confirming}
	title={m.library_book_confirm_title({ seat: seat.short })}
	message={m.library_book_confirm_body({
		library: library.name,
		when: whenLong(startDate, endDate, true)
	})}
	confirmLabel={m.library_book()}
	cancelLabel={m.ui_back()}
	onconfirm={book}
/>

<style>
	.confirm {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: var(--w-reading);
	}

	.fields {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-4) var(--sp-5) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.consent {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-4);
	}

	.consent-text {
		font: var(--type-ui);
		font-size: 0.9375rem;
		cursor: pointer;
		text-wrap: pretty;
	}

	.terms {
		display: inline-flex;
		align-items: center;
		gap: 4px;
		color: var(--accent-text);
		font-weight: 600;
		text-decoration: none;
	}

	.terms :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	@media (hover: hover) {
		.terms:hover {
			text-decoration: underline;
		}
	}

	.error {
		margin-block-start: calc(-1 * var(--sp-2));
		color: var(--danger);
		font: var(--type-meta);
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
	}
</style>
