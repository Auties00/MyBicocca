<script lang="ts">
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import IconStudent from '~icons/ph/student';
	import { isOpen } from '#lib/data/account.ts';
	import { CareerPick } from '#lib/data/careerPick.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import CareerOption from './CareerOption.svelte';

	const id = $props.id();
	const pick = new CareerPick();

	const open = $derived(pick.careers.filter((career) => isOpen(career.status)));
	const ended = $derived(pick.careers.filter((career) => !isOpen(career.status)));

	let failed = $state(false);
	let leaving = $state(false);

	async function confirm(event: SubmitEvent) {
		event.preventDefault();
		failed = false;
		try {
			await pick.confirm();
		} catch {
			failed = true;
		}
	}

	/**
	 * The way out for whoever signed into the wrong account, since the picker can't be skipped:
	 * another signed-in account takes over, else the login page.
	 */
	async function signOut() {
		const account = session.active;
		if (account === undefined || leaving) return;
		leaving = true;
		try {
			await session.signOut(account.id);
			await goto(resolve(session.active ? '' : 'login'), { replace: true });
		} finally {
			leaving = false;
		}
	}
</script>

{#snippet options(careers: typeof pick.careers)}
	<div class="options">
		{#each careers as career (career.id)}
			<CareerOption
				{career}
				name="{id}-career"
				checked={pick.selected === career.id}
				onselect={() => (pick.selected = career.id)}
			/>
		{/each}
	</div>
{/snippet}

<section class="picker" aria-labelledby="{id}-title" data-auth-column="wide">
	<header class="head">
		<span class="emblem" aria-hidden="true"><IconStudent /></span>
		<h1 id="{id}-title" class="title" tabindex="-1">{m.auth_career_title()}</h1>
		<p class="lead">{m.auth_career_instruction()}</p>
	</header>

	<form class="form" onsubmit={confirm}>
		<fieldset class="careers" disabled={pick.pending || leaving}>
			<legend class="sr-only">{m.auth_career_title()}</legend>
			{@render options(open)}
			{#if ended.length > 0}
				<p class="group">{m.auth_career_ended_group()}</p>
				{@render options(ended)}
			{/if}
		</fieldset>

		{#if failed}
			<Banner tone="danger" role="alert" message={m.auth_career_error()} />
		{/if}

		<div class="actions">
			<Button
				type="submit"
				variant="solid"
				size="lg"
				full
				loading={pick.pending}
				disabled={pick.selected === undefined || leaving}
			>
				{m.common_continue()}
			</Button>
		</div>
	</form>

	{#if session.active}
		<footer class="who">
			<span>{m.auth_career_signed_in_as({ name: session.active.username })}</span>
			<Button variant="ghost" size="sm" loading={leaving} onclick={() => void signOut()}>
				{m.auth_career_sign_out()}
			</Button>
		</footer>
	{/if}
</section>

<style>
	.picker {
		display: grid;
		gap: 24px;
		inline-size: 100%;
		max-inline-size: 560px;
		margin-inline: auto;
	}

	@media (width >= 600px) {
		.picker {
			padding: 36px 32px 24px;
			border-radius: var(--r-xl);
			background: var(--surface);
			box-shadow: var(--elev-3), var(--inner-light);
		}
	}

	.head {
		display: grid;
		justify-items: start;
		gap: 8px;
	}

	.emblem {
		display: grid;
		place-items: center;
		inline-size: 52px;
		block-size: 52px;
		margin-block-end: 8px;
		border-radius: var(--r-md);
		background: var(--container);
		color: var(--accent-text);
		box-shadow: var(--inner-light);
	}

	.emblem :global(svg) {
		inline-size: 28px;
		block-size: 28px;
	}

	.title {
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
		outline: none;
	}

	.lead {
		color: var(--text-muted);
		font: var(--type-body);
		text-wrap: pretty;
	}

	.form {
		display: grid;
		gap: 20px;
	}

	.careers {
		display: grid;
		gap: 10px;
	}

	.options {
		display: grid;
		gap: 10px;
	}

	.group {
		margin-block-start: 10px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	/*
	 * Many careers outgrow the screen: Continue stays in reach at the bottom, and the cards scrolling
	 * under it fade into its edge instead of being cut flat.
	 */
	.actions {
		--bar: var(--bg);
		position: sticky;
		inset-block-end: 0;
		z-index: var(--z-sticky);
		margin-block: -8px;
		padding-block: 8px calc(8px + var(--safe-bottom));
		background: var(--bar);
	}

	.actions::before {
		content: '';
		position: absolute;
		inset-inline: 0;
		inset-block-end: 100%;
		block-size: 12px;
		background: linear-gradient(to bottom, transparent, var(--bar));
		pointer-events: none;
	}

	@media (width >= 600px) {
		.actions {
			--bar: var(--surface);
			margin-inline: -8px;
			padding-inline: 8px;
		}
	}

	.who {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: center;
		gap: 4px 8px;
		padding-block-start: 16px;
		box-shadow: 0 -1px 0 var(--hairline);
		color: var(--text-muted);
		font: var(--type-meta);
		text-align: center;
	}
</style>
