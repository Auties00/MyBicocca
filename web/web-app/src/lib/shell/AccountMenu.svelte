<script lang="ts" module>
	export interface AccountTriggerProps {
		onclick: (event: MouseEvent) => void;
		'aria-expanded': boolean;
		'aria-haspopup': 'dialog' | 'menu';
		'aria-controls'?: string;
		style?: string;
	}
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconCheck from '~icons/ph/check-bold';
	import IconArrowsLeftRight from '~icons/ph/arrows-left-right';
	import IconUserPlus from '~icons/ph/user-plus';
	import IconSignOut from '~icons/ph/sign-out';
	import IconMonitor from '~icons/ph/monitor';
	import IconSun from '~icons/ph/sun';
	import IconMoon from '~icons/ph/moon';
	import { m } from '#lib/paraglide/messages.js';
	import Avatar from '#lib/ui/Avatar.svelte';
	import Banner from '#lib/ui/Banner.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import Popover, { type PopoverPlacement } from '#lib/ui/Popover.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { internalHref } from '#lib/ui/types.ts';
	import type { AccountModel } from './account.ts';
	import { secondaryDestinations } from '#lib/ui/destinations.ts';
	import { shellHref } from './nav.ts';
	import { shell } from './shell.svelte.ts';
	import { theme, type ThemeMode } from '#lib/ui/theme.svelte.ts';
	import { compact } from '#lib/ui/media.ts';

	interface Props {
		model: AccountModel;
		/** The opener; spread the given props on a button. */
		trigger: Snippet<[AccountTriggerProps]>;
		/** Anchor side on wider screens (a sheet on phones). */
		placement?: PopoverPlacement;
	}

	let { model, trigger, placement = 'bottom-end' }: Props = $props();

	let pending = $state<string>();
	let error = $state<string>();
	let signOutOpen = $state(false);

	const active = $derived(model.accounts.find((a) => a.id === model.activeId));
	const addHref = $derived(internalHref(model.addAccountHref));

	const themeOptions: { value: ThemeMode; label: () => string; icon: typeof IconSun }[] = [
		{ value: 'system', label: m.shell_theme_system, icon: IconMonitor },
		{ value: 'light', label: m.shell_theme_light, icon: IconSun },
		{ value: 'dark', label: m.shell_theme_dark, icon: IconMoon }
	];

	async function run(key: string, action: () => Promise<void>) {
		pending = key;
		error = undefined;
		try {
			await action();
		} catch {
			error = m.ui_error_generic();
		} finally {
			pending = undefined;
		}
	}

	function close() {
		shell.accountOpen = false;
	}
</script>

{#snippet content()}
	<div class="menu">
		{#if error}<Banner tone="danger" message={error} role="alert" />{/if}

		<div class="accounts">
			{#each model.accounts as account (account.id)}
				{#if account.id === model.activeId}
					<section class="card active" aria-label={account.name}>
						<div class="who">
							<Avatar name={account.name} src={account.photo} size={44} />
							<div class="names">
								<p class="name">{account.name}</p>
								<p class="email">{account.email}</p>
							</div>
						</div>
						{#if account.careers.length > 0}
							<div class="careers" role="radiogroup" aria-label={m.shell_career_active()}>
								{#each account.careers as career (career.id)}
									{@const selected = career.id === account.selectedCareerId}
									{@const key = `${account.id}:${career.id}`}
									<button
										type="button"
										class={['career', { selected }]}
										role="radio"
										aria-checked={selected}
										onclick={() =>
											!selected && run(key, () => model.selectCareer(account.id, career.id))}
									>
										{#if career.level}
											<span class="pip" title={career.level.full}>
												<span aria-hidden="true">{career.level.short}</span>
												<span class="sr-only">{career.level.full}</span>
											</span>
										{/if}
										<span class="career-text">
											<span class="career-title">{career.title}</span>
											<span class="career-detail">{career.detail}</span>
											{#if career.status}
												<StatusChip
													status={career.status.tone}
													label={career.status.label}
													size="sm"
												/>
											{/if}
										</span>
										<span class="mark" aria-hidden="true">
											{#if pending === key}<Spinner size={16} />{:else if selected}<IconCheck
												/>{/if}
										</span>
									</button>
								{/each}
							</div>
						{/if}
					</section>
				{:else}
					<button
						type="button"
						class="card other"
						onclick={() => run(account.id, () => model.switchAccount(account.id))}
					>
						<Avatar name={account.name} src={account.photo} size={36} tone="neutral" />
						<span class="names">
							<span class="name">{account.name}</span>
							<span class="email">{account.email}</span>
						</span>
						<span class="mark" aria-hidden="true">
							{#if pending === account.id}<Spinner size={16} />{:else}<IconArrowsLeftRight />{/if}
						</span>
						<span class="sr-only">{m.shell_switch_account({ name: account.name })}</span>
					</button>
				{/if}
			{/each}
		</div>

		<div class="theme" role="radiogroup" aria-label={m.shell_theme_label()}>
			<span class="theme-label" aria-hidden="true">{m.shell_theme_label()}</span>
			<span class="theme-options">
				{#each themeOptions as option (option.value)}
					<button
						type="button"
						role="radio"
						class={['theme-option', { selected: theme.mode === option.value }]}
						aria-checked={theme.mode === option.value}
						aria-label={option.label()}
						title={option.label()}
						onclick={(e) => theme.set({ mode: option.value }, e)}
					>
						<option.icon aria-hidden="true" />
					</button>
				{/each}
			</span>
		</div>

		<ul class="links" role="list">
			{#each secondaryDestinations as destination (destination.id)}
				<li>
					<a class="link" href={shellHref(destination.path)} onclick={close}>
						<destination.icon aria-hidden="true" />
						{destination.label()}
					</a>
				</li>
			{/each}
			{#if addHref}
				<li>
					<a class="link" href={addHref} onclick={close}>
						<IconUserPlus aria-hidden="true" />
						{m.shell_add_account()}
					</a>
				</li>
			{/if}
			<li>
				<button type="button" class="link danger" onclick={() => (signOutOpen = true)}>
					<IconSignOut aria-hidden="true" />
					{m.shell_sign_out()}
				</button>
			</li>
		</ul>
	</div>
{/snippet}

{#if compact.current}
	{@render trigger({
		onclick: () => (shell.accountOpen = true),
		'aria-expanded': shell.accountOpen,
		'aria-haspopup': 'dialog'
	})}
	<Overlay
		bind:open={shell.accountOpen}
		title={m.shell_accounts_title()}
		subtitle={m.shell_accounts_count({ count: model.accounts.length })}
	>
		{@render content()}
	</Overlay>
{:else}
	<Popover
		{trigger}
		bind:open={shell.accountOpen}
		{placement}
		label={m.shell_accounts_title()}
		width="340px"
		onopen={(surface) => surface.querySelector<HTMLElement>('button, a')?.focus()}
	>
		{@render content()}
	</Popover>
{/if}

{#if active}
	<ConfirmDialog
		bind:open={signOutOpen}
		title={m.shell_sign_out_title({ name: active.name })}
		message={m.shell_sign_out_message()}
		confirmLabel={m.shell_sign_out()}
		tone="danger"
		onconfirm={async () => {
			await model.signOut(active.id);
			close();
		}}
	/>
{/if}

<style>
	.menu {
		display: grid;
		gap: 10px;
	}

	.accounts {
		display: grid;
		gap: 6px;
	}

	.card {
		border-radius: var(--r-lg);
	}

	.active {
		display: grid;
		gap: 10px;
		padding: 12px;
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.who {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 2px;
	}

	.names {
		display: grid;
		min-inline-size: 0;
		text-align: start;
	}

	.name {
		font: var(--type-title-s);
		font-size: 1rem;
		letter-spacing: var(--track-title-s);
	}

	.email {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.careers {
		display: grid;
		gap: 4px;
	}

	.career {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 10px 12px;
		border-radius: var(--r-md);
		background: var(--surface);
		color: var(--text);
		text-align: start;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.career.selected {
		background: var(--container);
		color: var(--on-container);
	}

	@media (hover: hover) {
		.career:not(.selected):hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.pip {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		min-inline-size: 32px;
		block-size: 32px;
		padding-inline: 6px;
		border-radius: 10px;
		background: color-mix(in oklch, currentColor 10%, transparent);
		font: 700 0.75rem / 1 var(--font-ui);
		letter-spacing: 0.02em;
	}

	.career-text {
		flex: 1;
		display: grid;
		justify-items: start;
		gap: 2px;
		min-inline-size: 0;
	}

	.career-title {
		font: var(--type-ui-strong);
		text-transform: uppercase;
		letter-spacing: 0.02em;
	}

	.career-detail {
		font: var(--type-meta);
		opacity: 0.8;
		font-variant-numeric: tabular-nums;
	}

	.career-text :global(.status) {
		margin-block-start: 4px;
	}

	.mark {
		display: grid;
		place-items: center;
		inline-size: 20px;
		flex-shrink: 0;
	}

	.mark :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.other {
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 10px 12px;
		color: var(--text);
		box-shadow: inset 0 0 0 1px var(--hairline);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.other .name {
		font: var(--type-ui-strong);
	}

	.other .names {
		flex: 1;
	}

	.other .mark {
		color: var(--text-muted);
	}

	@media (hover: hover) {
		.other:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.theme {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12px;
		padding: 4px 4px 4px 12px;
	}

	.theme-label {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.theme-options {
		display: flex;
		gap: 2px;
		padding: 3px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.theme-option {
		display: grid;
		place-items: center;
		inline-size: 36px;
		block-size: 30px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.theme-option :global(svg) {
		inline-size: 17px;
		block-size: 17px;
	}

	.theme-option.selected {
		background: var(--container);
		color: var(--on-container);
	}

	@media (hover: hover) {
		.theme-option:not(.selected):hover {
			color: var(--text);
		}
	}

	.links {
		display: grid;
		gap: 1px;
		margin: 0;
		padding: 6px 0 0;
		border-block-start: 1px solid var(--hairline);
		list-style: none;
	}

	.link {
		display: flex;
		align-items: center;
		gap: 12px;
		inline-size: 100%;
		min-block-size: var(--control-h);
		padding: 0 12px;
		border-radius: var(--r-sm);
		color: var(--text);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.link :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
		color: var(--text-muted);
	}

	@media (hover: hover) {
		.link:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.danger,
	.danger :global(svg) {
		color: var(--danger);
	}
</style>
