<script lang="ts">
	import { page } from '$app/state';
	import { m } from '#lib/paraglide/messages.js';
	import { serviceGroupLabels } from '#lib/shell/catalog.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import Badge from '#lib/ui/Badge.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import { badgeInput, useAlerts } from '../alerts.svelte.ts';
	import { useDeadlines } from '../deadlines.svelte.ts';
	import { serviceBadge, serviceGroups, servicesOf } from '../hub.ts';
	import DeadlinesCard from './DeadlinesCard.svelte';

	const deadlines = useDeadlines();
	const alerts = useAlerts();
	const badges = $derived(badgeInput(deadlines, alerts));

	const path = $derived(page.url.pathname);
	const within = (href: string) => path === href || path.startsWith(`${href}/`);
</script>

<nav class="nav" aria-label={m.services_nav_label()}>
	<DeadlinesCard {deadlines} variant="nav" current={within(shellHref('services/deadlines'))} />

	{#each serviceGroups as group (group)}
		<!-- A group, not a region: the nav landmark already names the column. -->
		<div class="group" role="group" aria-labelledby="services-nav-{group}">
			<p class="heading" id="services-nav-{group}">{serviceGroupLabels[group]()}</p>
			<ul class="items" role="list">
				{#each servicesOf(group) as service (service.id)}
					{@const href = shellHref(service.path)}
					{@const current = within(href)}
					{@const badge = serviceBadge(service.id, badges)}
					<li>
						<a class={['item', { current }]} {href} aria-current={current ? 'page' : undefined}>
							<IconChip icon={service.icon} tone={service.tone} size="sm" />
							<span class="label">{service.label()}</span>
							{#if badge}<Badge tone={badge.tone}>{badge.label}</Badge>{/if}
						</a>
					</li>
				{/each}
			</ul>
		</div>
	{/each}
</nav>

<style>
	.nav {
		display: grid;
		gap: var(--sp-4);
		align-content: start;
	}

	.group {
		display: grid;
		gap: 4px;
	}

	.heading {
		padding-inline: 10px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.items {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.item {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 44px;
		padding: 6px 10px 6px 6px;
		border-radius: var(--r-md);
		color: var(--text);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (pointer: fine) {
		.item {
			min-block-size: 40px;
			padding-block: 4px;
		}
	}

	@media (hover: hover) {
		.item:not(.current):hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.item.current {
		background: var(--container);
		color: var(--on-container);
		font-weight: 600;
	}

	.item:focus-visible {
		outline-offset: -2px;
	}

	.label {
		flex: 1;
		min-inline-size: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
