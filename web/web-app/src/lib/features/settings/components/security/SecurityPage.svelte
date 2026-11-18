<script lang="ts">
	import IconTimer from '~icons/ph/timer';
	import IconPassword from '~icons/ph/password';
	import IconLockSimple from '~icons/ph/lock-simple';
	import IconShieldCheck from '~icons/ph/shield-check';
	import { m } from '#lib/paraglide/messages.js';
	import { theme } from '#lib/ui/theme.svelte.ts';
	import { haptics } from '#lib/ui/haptics.svelte.ts';
	import IconChip from '#lib/ui/IconChip.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import Select from '#lib/ui/Select.svelte';
	import { appLock, LOCK_TIMEOUTS, type LockTimeout } from '../../lock/appLock.svelte.ts';
	import { timeoutLabel } from '../../lock/labels.ts';
	import ChoiceCard from '../ChoiceCard.svelte';
	import ChoiceGroup from '../ChoiceGroup.svelte';
	import SectionPage from '../SectionPage.svelte';
	import ThemePreview from '../appearance/ThemePreview.svelte';
	import LockedPreview from './LockedPreview.svelte';
	import PinDialog, { type PinDialogMode } from './PinDialog.svelte';

	type LockChoice = 'off' | 'on';

	let dialogOpen = $state(false);
	let dialogMode = $state<PinDialogMode>('create');
	/** Re-creates the dialog for every opening, so each one starts blank. */
	let dialogRun = $state(0);
	/** The card picked while its dialog asks for the PIN; once it closes, the cards show the lock. */
	let picked = $state<LockChoice>();

	const actual = $derived<LockChoice>(appLock.enabled ? 'on' : 'off');
	const shown = $derived(dialogOpen && picked ? picked : actual);

	const timeoutOptions = LOCK_TIMEOUTS.map((value) => ({
		value: String(value),
		label: timeoutLabel(value)
	}));

	function ask(mode: PinDialogMode, choice?: LockChoice) {
		picked = choice;
		dialogMode = mode;
		dialogRun++;
		dialogOpen = true;
	}

	function choose(value: LockChoice) {
		haptics.tap();
		if (value === actual) return;
		ask(value === 'on' ? 'create' : 'disable', value);
	}
</script>

<SectionPage title={m.settings_security_title()} subtitle={m.settings_security_page_subtitle()}>
	<ChoiceGroup
		title={m.settings_lock_group_title()}
		description={m.settings_lock_group_description()}
		columns={2}
	>
		<ChoiceCard
			name="app-lock"
			value="off"
			checked={shown === 'off'}
			label={m.settings_lock_off()}
			description={m.settings_lock_off_description()}
			onselect={choose}
		>
			{#snippet preview()}
				<ThemePreview palette={theme.palette} scheme={theme.resolved} />
			{/snippet}
		</ChoiceCard>
		<ChoiceCard
			name="app-lock"
			value="on"
			checked={shown === 'on'}
			label={m.settings_lock_on()}
			description={m.settings_lock_on_description()}
			onselect={choose}
		>
			{#snippet preview()}<LockedPreview />{/snippet}
		</ChoiceCard>
	</ChoiceGroup>

	{#if appLock.config}
		<section class="options" aria-labelledby="lock-options">
			<SectionHeader id="lock-options" title={m.settings_lock_options_title()} />
			<ListGroup label={m.settings_lock_options_title()}>
				<li class="timeout">
					<span class="icon"><IconChip icon={IconTimer} tone="accent" /></span>
					<span class="text">
						<span class="label">{m.settings_lock_after()}</span>
						<span class="description">{m.settings_lock_after_description()}</span>
					</span>
					<span class="select">
						<Select
							label={m.settings_lock_after()}
							hideLabel
							options={timeoutOptions}
							value={String(appLock.config.timeout)}
							onchange={(value) => appLock.setTimeout(Number(value) as LockTimeout)}
						/>
					</span>
				</li>
				<!-- Actions rather than places: no chevron. -->
				<ListRow
					title={m.settings_pin_change_title()}
					description={m.settings_lock_change_description()}
					icon={IconPassword}
					iconTone="accent"
					onclick={() => ask('change')}
				>
					{#snippet trailing()}{/snippet}
				</ListRow>
				<ListRow
					title={m.settings_lock_now()}
					description={m.settings_lock_now_description()}
					icon={IconLockSimple}
					iconTone="accent"
					onclick={() => appLock.lock()}
				>
					{#snippet trailing()}{/snippet}
				</ListRow>
			</ListGroup>
		</section>
	{/if}

	<aside class="note">
		<IconShieldCheck aria-hidden="true" />
		<p>{m.settings_lock_note()}</p>
	</aside>
</SectionPage>

{#key dialogRun}
	<PinDialog bind:open={dialogOpen} mode={dialogMode} />
{/key}

<style>
	.options {
		display: grid;
		gap: 12px;
	}

	/* A row of the group with its choice beside it, or under it when the row is narrow. */
	.timeout {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		grid-template-areas:
			'icon text'
			'select select';
		align-items: center;
		gap: 12px;
		padding: 12px 16px 14px;
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.icon {
		grid-area: icon;
		display: flex;
	}

	.text {
		grid-area: text;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.label {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		line-height: 1.35;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.select {
		grid-area: select;
	}

	@container (width >= 440px) {
		.timeout {
			grid-template-columns: auto minmax(0, 1fr) 11rem;
			grid-template-areas: 'icon text select';
			padding-block: 10px;
		}
	}

	.note {
		display: flex;
		gap: 12px;
		padding: 14px 16px;
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	.note :global(svg) {
		flex-shrink: 0;
		inline-size: var(--icon);
		block-size: var(--icon);
		color: var(--success);
	}
</style>
