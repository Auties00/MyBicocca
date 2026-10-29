<script lang="ts">
	import IconHardDrives from '~icons/ph/hard-drives';
	import IconArrowsLeftRight from '~icons/ph/arrows-left-right';
	import IconGlobe from '~icons/ph/globe-hemisphere-west';
	import IconEyeSlash from '~icons/ph/eye-slash';
	import IconSignOut from '~icons/ph/sign-out';
	import IconChatCircle from '~icons/ph/chat-circle-text';
	import { m } from '#lib/paraglide/messages.js';
	import IconChip from '#lib/ui/IconChip.svelte';
	import type { Icon } from '#lib/ui/types.ts';
	import { REPOSITORY_URL } from '../../releases/releases.ts';
	import SectionPage from '../SectionPage.svelte';

	interface Topic {
		id: string;
		icon: Icon;
		title: () => string;
		/** Read when shown: a paragraph that names a feature appears only while it is offered. */
		paragraphs: () => readonly string[];
	}

	/** What the code does with data, topic by topic; each claim can be checked in the source. */
	const topics: readonly Topic[] = [
		{
			id: 'device',
			icon: IconHardDrives,
			title: m.settings_privacy_device_title,
			paragraphs: () => [m.settings_privacy_device_body(), m.settings_privacy_device_password()]
		},
		{
			id: 'requests',
			icon: IconArrowsLeftRight,
			title: m.settings_privacy_requests_title,
			paragraphs: () => [m.settings_privacy_requests_body(), m.settings_privacy_requests_relay()]
		},
		{
			id: 'others',
			icon: IconGlobe,
			title: m.settings_privacy_others_title,
			paragraphs: () => [m.settings_privacy_others_body(), m.settings_privacy_others_local()]
		},
		{
			id: 'tracking',
			icon: IconEyeSlash,
			title: m.settings_privacy_tracking_title,
			paragraphs: () => [m.settings_privacy_tracking_body()]
		},
		{
			id: 'control',
			icon: IconSignOut,
			title: m.settings_privacy_control_title,
			paragraphs: () => [m.settings_privacy_control_body(), m.settings_privacy_control_lock()]
		}
	];
</script>

<SectionPage title={m.settings_privacy_title()} subtitle={m.settings_privacy_page_subtitle()}>
	<p class="lead">{m.settings_privacy_lead()}</p>

	<div class="topics">
		{#each topics as topic (topic.id)}
			<section class="topic" aria-labelledby="privacy-{topic.id}">
				<IconChip icon={topic.icon} tone="accent" />
				<div class="text">
					<h2 class="title" id="privacy-{topic.id}">{topic.title()}</h2>
					{#each topic.paragraphs() as paragraph, index (index)}<p>{paragraph}</p>{/each}
				</div>
			</section>
		{/each}
	</div>

	<aside class="questions">
		<IconChatCircle aria-hidden="true" />
		<p>
			{m.settings_privacy_questions()}
			<a href="{REPOSITORY_URL}/issues" rel="external noopener noreferrer" target="_blank">
				{m.settings_privacy_questions_link()}
			</a>
		</p>
	</aside>
</SectionPage>

<style>
	.lead {
		max-inline-size: 48ch;
		font: var(--type-body);
		font-size: 1.125rem;
		text-wrap: pretty;
	}

	.topics {
		display: grid;
		gap: 2px;
	}

	.topic {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 16px;
		padding: 18px 20px;
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.topic:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.topic:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.text {
		display: grid;
		gap: 6px;
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.title {
		color: var(--text);
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.questions {
		display: flex;
		gap: 12px;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.questions :global(svg) {
		flex-shrink: 0;
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.questions a {
		color: var(--accent-text);
		font-weight: 600;
	}
</style>
