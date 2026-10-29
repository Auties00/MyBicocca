<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { primaryDestinations, type DestinationId } from '#lib/ui/destinations.ts';

	const descriptions: Partial<Record<DestinationId, () => string>> = {
		calendar: m.auth_index_calendar,
		elearning: m.auth_index_elearning,
		map: m.auth_index_map,
		services: m.auth_index_services
	};

	const id = $props.id();
</script>

<div class="cover">
	<h1 class="headline" tabindex="-1">
		{m.auth_headline()}
		<em>{m.auth_headline_accent()}</em>
	</h1>

	<section class="index" aria-labelledby="{id}-index">
		<h2 id="{id}-index" class="overline">{m.auth_index_title()}</h2>
		<ul role="list">
			{#each primaryDestinations as destination (destination.id)}
				<li class="entry">
					<span class="glyph"><destination.icon aria-hidden="true" /></span>
					<span class="name">{destination.label()}</span>
					<span class="what">{descriptions[destination.id]?.()}</span>
				</li>
			{/each}
		</ul>
		<p class="sources" translate="no">{m.auth_index_sources()}</p>
	</section>
</div>

<style>
	.cover {
		display: grid;
		gap: 48px;
		align-content: center;
	}

	.headline {
		outline: none;
		font: var(--type-display-m);
		letter-spacing: var(--track-display-m);
		text-wrap: balance;
	}

	.headline em {
		display: block;
		color: var(--text-muted);
		font-style: italic;
	}

	.index {
		display: none;
	}

	@media (width >= 1024px) {
		.headline {
			font-size: clamp(2.75rem, 1.4rem + 2.2vw, 4rem);
			line-height: 1.04;
			letter-spacing: var(--track-display-l);
		}

		.index {
			display: grid;
			gap: 12px;
		}
	}

	.overline {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	ul {
		display: grid;
		margin: 0;
		box-shadow: 0 -1px 0 var(--hairline);
	}

	.entry {
		display: grid;
		grid-template-columns: auto 8.5rem minmax(0, 1fr);
		align-items: center;
		gap: 14px;
		padding-block: 12px;
		box-shadow: 0 1px 0 var(--hairline);
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		border-radius: var(--r-sm);
		background: var(--container);
		color: var(--accent-text);
	}

	.glyph :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.name {
		font: var(--type-ui-strong);
	}

	.what {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.sources {
		margin-block-start: 4px;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}
</style>
