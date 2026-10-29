<script lang="ts">
	import IconAndroidLogo from '~icons/ph/android-logo';
	import { resolve } from '$app/paths';
	import { query } from '#lib/data/query.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import { releasesQuery } from '../../releases/query.svelte.ts';
	import SectionPage from '../SectionPage.svelte';
	import ReleaseCard from './ReleaseCard.svelte';
	import ReleasesView from './ReleasesView.svelte';

	const releases = query(releasesQuery);

	const subtitle = $derived(
		releases.data?.length
			? m.settings_releases_count({ count: releases.data.length })
			: m.settings_releases_subtitle()
	);
</script>

<!--
	The project publishes versions of its Android app only: the site has no installed version to
	compare them with, so this is the plain list, introduced for what it is.
-->
<SectionPage
	title={m.settings_releases_title()}
	{subtitle}
	parent={{ href: resolve('settings/about'), label: m.settings_about_title() }}
>
	<Banner tone="neutral" icon={IconAndroidLogo} message={m.settings_releases_android()} />
	<ReleasesView {releases}>
		{#snippet children(list)}
			<div class="list">
				{#each list as release, index (release.version)}
					<ReleaseCard {release} latest={index === 0} collapsible open={index === 0} />
				{/each}
			</div>
		{/snippet}
	</ReleasesView>
</SectionPage>

<style>
	.list {
		display: grid;
		gap: 12px;
	}
</style>
