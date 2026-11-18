<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconNewspaper from '~icons/ph/newspaper-duotone';
	import IconGithubLogo from '~icons/ph/github-logo';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { DataError } from '#lib/data/errors.ts';
	import type { Query } from '#lib/data/query.svelte.ts';
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import { githubLimit } from '../../releases/query.svelte.ts';
	import { REPOSITORY_URL, type Release } from '../../releases/releases.ts';
	import ReleasesSkeleton from './ReleasesSkeleton.svelte';

	interface Props {
		releases: Query<Release[]>;
		/** The releases, once there are some. */
		children: Snippet<[Release[]]>;
	}

	let { releases, children }: Props = $props();

	const gate = new LoadingGate(() => releases.status === 'pending');
	const loading = $derived(releases.status === 'pending' || gate.visible);

	/** Network trouble in the app's words; GitHub's hourly limit with the time it resets. */
	function describe(error: DataError): string {
		if (error.kind === 'offline' || error.kind === 'network' || error.kind === 'timeout') {
			return errorMessage(error);
		}
		const reset = githubLimit.resetAt;
		return reset === undefined
			? m.settings_releases_error()
			: m.settings_releases_rate_limited({ time: formatTime(reset) });
	}
</script>

{#if loading}
	<ReleasesSkeleton />
{:else if releases.data !== undefined}
	{#if releases.error}
		<ErrorState
			variant="stale"
			message={describe(releases.error)}
			updatedAt={releases.updatedAt === undefined ? undefined : new Date(releases.updatedAt)}
			retrying={releases.refreshing}
			onretry={() => void releases.refresh()}
		/>
	{/if}
	{#if releases.data.length > 0}
		{@render children(releases.data)}
	{:else}
		<EmptyState
			size="page"
			icon={IconNewspaper}
			title={m.settings_releases_empty_title()}
			body={m.settings_releases_empty_body()}
		>
			{#snippet action()}
				<Button variant="tonal" icon={IconGithubLogo} href="{REPOSITORY_URL}/releases">
					{m.settings_releases_empty_action()}
				</Button>
			{/snippet}
		</EmptyState>
	{/if}
{:else if releases.error}
	<ErrorState
		title={m.settings_releases_error_title()}
		message={describe(releases.error)}
		retrying={releases.refreshing}
		onretry={() => void releases.refresh()}
	/>
{/if}
