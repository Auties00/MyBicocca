<script lang="ts">
	import { tick, type ComponentProps } from 'svelte';
	import { afterNavigate, goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import IconDotsNine from '~icons/ph/dots-nine';
	import IconQrCode from '~icons/ph/qr-code';
	import IconScan from '~icons/ph/scan';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { canScanQr } from '#lib/qr/camera.ts';
	import type QrScanner from '#lib/qr/QrScanner.svelte';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import TextField from '#lib/ui/TextField.svelte';
	import type { Icon } from '#lib/ui/types.ts';
	import type { CheckInFlow, CheckInSource } from '../checkInFlow.svelte.ts';
	import { checkInSubtitle } from '../labels.ts';
	import { looksLikeLink } from '../scan.ts';
	import { getAttendanceShell } from '../shell.ts';
	import CheckInResult from './CheckInResult.svelte';

	const shell = getAttendanceShell();
	const headingId = $props.id();

	let codeInput = $state<HTMLInputElement>();
	let linkInput = $state<HTMLInputElement>();
	let linkError = $state<string>();
	let canScan = $state(false);
	let Scanner = $state<typeof QrScanner>();
	let scanning = $state(false);

	void canScanQr().then((supported) => (canScan = supported));

	const offline = $derived(!network.online);
	/* Beside the list the pane titles the check-in (h2); alone, the page's title does (h1). */
	const level = $derived(shell.wide ? 3 : 2);

	/*
	 * Arriving to check in, the student has the lesson code in front of them: its field takes the
	 * focus the shell gives the page's title. Beside the list the form is the page's default pane,
	 * and leaves focus be.
	 */
	afterNavigate(() => {
		if (
			page.route.id === '/(app)/services/attendance/check-in' &&
			shell.flow?.phase.name === 'form'
		) {
			codeInput?.focus({ preventScroll: true });
		}
	});

	function submit(flow: CheckInFlow, source: CheckInSource) {
		if (session.current === undefined || offline) return;
		void flow.submit(session.current, source);
	}

	/* A code typed in the link's field would go to EasyBadge as a lesson code: point to its form. */
	function submitLink(flow: CheckInFlow) {
		if (flow.link.trim() !== '' && !looksLikeLink(flow.link)) {
			linkError = m.attendance_link_not_a_link();
			linkInput?.focus();
			return;
		}
		submit(flow, 'link');
	}

	async function edit(flow: CheckInFlow, source: CheckInSource) {
		flow.edit();
		await tick();
		const input = source === 'code' ? codeInput : linkInput;
		input?.focus();
		input?.select();
	}

	/*
	 * Alone on its page, the check-in hands back to the list; beside it, the form stays for the
	 * next one, its first field ready.
	 */
	async function done(flow: CheckInFlow) {
		flow.reset();
		if (shell.wide) {
			await tick();
			codeInput?.focus();
		} else {
			await goto(resolve('services/attendance'));
		}
	}

	function retry(flow: CheckInFlow) {
		if (flow.phase.name !== 'result') return;
		const { outcome, source } = flow.phase;
		if (outcome.kind === 'failed' && outcome.error !== undefined) submit(flow, source);
		else void edit(flow, source);
	}

	async function openScanner() {
		Scanner ??= (await import('#lib/qr/QrScanner.svelte')).default;
		scanning = true;
	}

	function scannerProps(flow: CheckInFlow): ComponentProps<typeof QrScanner> {
		return {
			title: m.attendance_scanner_title(),
			hint: m.attendance_qr_hint(),
			manualLabel: m.attendance_manual_code_entry(),
			onscan(raw) {
				scanning = false;
				flow.link = raw;
				linkError = undefined;
				submit(flow, 'scan');
			},
			onclose() {
				scanning = false;
			},
			onmanual() {
				scanning = false;
				requestAnimationFrame(() => linkInput?.focus());
			}
		};
	}

	function focusOnShow(node: HTMLElement) {
		node.focus({ preventScroll: true });
	}
</script>

{#snippet method(icon: Icon, tone: 'accent' | 'info', title: string, caption: string, id: string)}
	<header class="method-head">
		<IconChip {icon} {tone} size="lg" />
		<div>
			<svelte:element this={`h${level}`} {id} class="method-title">{title}</svelte:element>
			<p>{caption}</p>
		</div>
	</header>
{/snippet}

{#snippet forms(flow: CheckInFlow)}
	{#if offline}
		<Banner tone="warning" icon={IconWifiSlash} message={m.attendance_offline()} role="status" />
	{/if}
	<div class="methods">
		<section class="method" aria-labelledby="{headingId}-lesson">
			{@render method(
				IconDotsNine,
				'accent',
				m.attendance_lesson(),
				m.attendance_classroom_presence(),
				`${headingId}-lesson`
			)}
			<form
				class="entry"
				onsubmit={(event) => {
					event.preventDefault();
					submit(flow, 'code');
				}}
			>
				<TextField
					label={m.attendance_lesson_code()}
					description={m.attendance_lesson_code_hint()}
					bind:value={flow.code}
					bind:input={codeInput}
					autocomplete="off"
					autocapitalize="characters"
					spellcheck={false}
					enterkeyhint="send"
				/>
				<Button
					type="submit"
					variant="solid"
					size="lg"
					full
					disabled={flow.code.trim() === '' || offline}
				>
					{m.attendance_check_in()}
				</Button>
			</form>
		</section>

		<section class="method" aria-labelledby="{headingId}-elearning">
			{@render method(
				IconQrCode,
				'info',
				m.attendance_other_activities(),
				m.attendance_elearning_presence(),
				`${headingId}-elearning`
			)}
			{#if canScan}
				<Button
					variant="tonal"
					size="lg"
					full
					icon={IconScan}
					onclick={openScanner}
					disabled={offline}
				>
					{m.attendance_scan_qr()}
				</Button>
			{/if}
			<form
				class="entry"
				onsubmit={(event) => {
					event.preventDefault();
					submitLink(flow);
				}}
			>
				<TextField
					label={m.attendance_link_label()}
					description={canScan ? m.attendance_link_hint_scan() : m.attendance_link_hint()}
					error={linkError}
					bind:value={flow.link}
					bind:input={linkInput}
					oninput={() => (linkError = undefined)}
					autocomplete="off"
					autocapitalize="off"
					spellcheck={false}
					inputmode="url"
					enterkeyhint="send"
				/>
				<Button
					type="submit"
					variant="tonal"
					size="lg"
					full
					disabled={flow.link.trim() === '' || offline}
				>
					{m.attendance_record()}
				</Button>
			</form>
		</section>
	</div>
{/snippet}

{#if shell.flow}
	{@const flow = shell.flow}
	<section class="check-in" aria-labelledby={headingId} {@attach flow.view}>
		<!-- Beside the list the check-in is the pane shown by default, with nothing to close. -->
		<DetailHeader
			id={headingId}
			title={m.attendance_check_in()}
			subtitle={checkInSubtitle(flow.phase)}
			closable={false}
		/>
		{#if flow.phase.name === 'form'}
			{@render forms(flow)}
		{:else if flow.phase.name === 'sending'}
			<!-- Focus waits here, where the outcome will appear. -->
			<div class="sending" role="status" tabindex="-1" {@attach focusOnShow}>
				<Spinner size={48} />
				<p class="sending-title">{m.attendance_recording()}</p>
			</div>
		{:else}
			<CheckInResult
				outcome={flow.phase.outcome}
				source={flow.phase.source}
				{level}
				ondone={() => done(flow)}
				onretry={() => retry(flow)}
			/>
		{/if}
	</section>

	{#if scanning && Scanner}
		<Scanner {...scannerProps(flow)} />
	{/if}
{/if}

<style>
	.check-in {
		display: grid;
		gap: var(--sp-5);
		container: check-in / inline-size;
	}

	.methods {
		display: grid;
		gap: var(--sp-4);
	}

	@container check-in (width >= 640px) {
		.methods {
			grid-template-columns: repeat(2, minmax(0, 1fr));
			align-items: start;
		}
	}

	.method {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.method-head {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
	}

	.method-title {
		margin: 0;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.method-head p {
		margin: 2px 0 0;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.entry {
		display: grid;
		gap: var(--sp-3);
	}

	.sending {
		display: grid;
		justify-items: center;
		gap: var(--sp-2);
		padding: var(--sp-9) var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		text-align: center;
		outline: none;
	}

	.sending-title {
		margin: var(--sp-3) 0 0;
		font: var(--type-title-s);
	}
</style>
