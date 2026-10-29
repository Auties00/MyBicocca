<script lang="ts">
	import IconArrowsClockwise from '~icons/ph/arrows-clockwise';
	import IconNewspaper from '~icons/ph/newspaper';
	import IconGithubLogo from '~icons/ph/github-logo';
	import IconBug from '~icons/ph/bug';
	import { resolve } from '$app/paths';
	import { formatDate, formatRelative } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Wordmark from '#lib/ui/Wordmark.svelte';
	import Avatar from '#lib/ui/Avatar.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { appVersion, buildDate, COPYRIGHT_HOLDER, copyrightYears, credits } from '../../about.ts';
	import { REPOSITORY_URL } from '../../releases/releases.ts';
	import { updateCheck } from '../../update.svelte.ts';
	import SectionPage from '../SectionPage.svelte';

	/** What the last check found, under its button; empty until one ran. */
	const updateText = $derived.by(() => {
		if (updateCheck.available) return m.settings_update_available_title();
		const status = updateCheck.status;
		switch (status.state) {
			case 'checking':
				return m.settings_update_checking();
			case 'current':
				return m.settings_update_current({ time: formatRelative(status.at) });
			case 'offline':
				return m.settings_update_offline();
			default:
				return '';
		}
	});
</script>

<SectionPage title={m.settings_about_title()} subtitle={m.settings_about_page_subtitle()}>
	<section class="hero" aria-labelledby="about-name">
		<span class="logo"><Wordmark variant="mark" size={72} /></span>
		<p class="wordmark" id="about-name" translate="no">My<span class="accent">Bicocca</span></p>
		<p class="tagline">{m.settings_about_tagline()}</p>
		<p class="facts">
			<span>{m.settings_about_version({ version: appVersion })}</span>
			{#if buildDate}
				<span aria-hidden="true">·</span>
				<span>{m.settings_about_build({ date: formatDate(buildDate) })}</span>
			{/if}
		</p>
		<div class="update">
			{#if updateCheck.available}
				<Button
					variant="solid"
					size="sm"
					icon={IconArrowsClockwise}
					onclick={() => location.reload()}
				>
					{m.settings_update_reload()}
				</Button>
			{:else}
				<Button
					variant="tonal"
					size="sm"
					icon={IconArrowsClockwise}
					loading={updateCheck.status.state === 'checking'}
					onclick={() => void updateCheck.run()}
				>
					{m.settings_update_check_title()}
				</Button>
			{/if}
			<p class="status" role="status">{updateText}</p>
		</div>
		<p class="copyright">
			© {copyrightYears()} <span translate="no">{COPYRIGHT_HOLDER}</span> · {m.settings_about_licence()}
		</p>
	</section>

	<ListGroup title={m.settings_about_project_title()}>
		<ListRow
			title={m.settings_about_github()}
			description={m.settings_about_github_subtitle()}
			icon={IconGithubLogo}
			href={REPOSITORY_URL}
		/>
		<ListRow
			title={m.settings_releases_title()}
			description={m.settings_releases_row()}
			icon={IconNewspaper}
			href={resolve('settings/about/releases')}
		/>
		<ListRow
			title={m.settings_about_report()}
			description={m.settings_about_report_subtitle()}
			icon={IconBug}
			href="{REPOSITORY_URL}/issues"
		/>
	</ListGroup>

	<ListGroup
		title={m.settings_about_credits_title()}
		description={m.settings_about_credits_subtitle()}
	>
		{#each credits as person (person.github)}
			<ListRow
				title={person.name}
				description="@{person.github}"
				href="https://github.com/{person.github}"
			>
				{#snippet leading()}
					<Avatar
						name={person.name}
						src="https://github.com/{person.github}.png?size=80"
						tone="neutral"
					/>
				{/snippet}
			</ListRow>
		{/each}
	</ListGroup>
</SectionPage>

<style>
	.hero {
		display: grid;
		justify-items: center;
		gap: 6px;
		padding: var(--sp-7) var(--sp-5) var(--sp-6);
		border-radius: var(--r-xl);
		background:
			radial-gradient(120% 90% at 50% 0%, var(--container), transparent 70%), var(--surface);
		box-shadow: var(--inner-light);
		text-align: center;
	}

	.logo {
		display: flex;
		margin-block-end: 10px;
		border-radius: 10px;
		box-shadow: var(--elev-2);
	}

	.wordmark {
		font: 650 2rem / 1.1 var(--font-ui);
		letter-spacing: -0.03em;
	}

	.accent {
		color: var(--brand-text);
	}

	.tagline {
		max-inline-size: 40ch;
		color: var(--text-muted);
		font: var(--type-body);
		text-wrap: balance;
	}

	.facts {
		display: flex;
		flex-wrap: wrap;
		justify-content: center;
		gap: 0 6px;
		margin-block-start: 8px;
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
	}

	.update {
		display: grid;
		justify-items: center;
		gap: 6px;
		margin-block: 10px 6px;
	}

	/* One line kept for the result, so the copyright doesn't move when a check reports. */
	.status {
		min-block-size: 1lh;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: balance;
	}

	.copyright {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}
</style>
