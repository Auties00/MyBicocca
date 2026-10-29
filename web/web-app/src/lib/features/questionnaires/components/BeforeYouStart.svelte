<script lang="ts">
	import IconArrowCounterClockwise from '~icons/ph/arrow-counter-clockwise';
	import IconEyeSlash from '~icons/ph/eye-slash';
	import IconPaperPlane from '~icons/ph/paper-plane-tilt';
	import IconUser from '~icons/ph/user';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { Icon } from '#lib/ui/types.ts';

	interface Props {
		/** Undefined while the questionnaire loads: its note is drawn as a placeholder. */
		anonymous: boolean | undefined;
		/** Heading level, under the page's or the pane's title. */
		level?: 2 | 3;
		/** Without its heading and box, inside a card that already frames it. */
		bare?: boolean;
	}

	let { anonymous, level = 2, bare = false }: Props = $props();

	const id = $props.id();

	/* `null` stands for the note on anonymity until the questionnaire says which it is. */
	const items: ({ icon: Icon; title: string; text: string } | null)[] = $derived([
		anonymous === undefined
			? null
			: anonymous
				? {
						icon: IconEyeSlash,
						title: m.questionnaires_fact_anonymous_title(),
						text: m.questionnaires_fact_anonymous()
					}
				: {
						icon: IconUser,
						title: m.questionnaires_fact_named_title(),
						text: m.questionnaires_fact_named()
					},
		{
			icon: IconPaperPlane,
			title: m.questionnaires_fact_send_title(),
			text: m.questionnaires_fact_send()
		},
		{
			icon: IconArrowCounterClockwise,
			title: m.questionnaires_fact_draft_title(),
			text: m.questionnaires_fact_draft()
		}
	]);
</script>

<section class={['facts', { bare }]} aria-labelledby={bare ? undefined : id}>
	{#if !bare}
		<svelte:element this={`h${level}`} class="heading" {id}>
			{m.questionnaires_facts_heading()}
		</svelte:element>
	{/if}
	<ul class="items" role="list">
		{#each items as item, index (index)}
			<li>
				{#if item}
					<span class="icon"><item.icon aria-hidden="true" /></span>
					<span class="words">
						<strong class="title">{item.title}</strong>
						<span class="text">{item.text}</span>
					</span>
				{:else}
					<span class="icon placeholder" aria-hidden="true"></span>
					<span class="words" aria-hidden="true">
						<span class="title"><Skeleton variant="text" width="5rem" /></span>
						<span class="text"><Skeleton variant="text" width="80%" /></span>
					</span>
				{/if}
			</li>
		{/each}
	</ul>
</section>

<style>
	.facts {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.bare {
		padding: 0;
		background: none;
		box-shadow: none;
	}

	.heading {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.items {
		display: grid;
		gap: var(--sp-4);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	li {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-3);
	}

	.icon {
		display: grid;
		flex: none;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		border-radius: var(--r-pill);
		background: var(--container);
		color: var(--on-container);
	}

	.placeholder {
		background: var(--track);
	}

	.icon :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	.words {
		display: grid;
		gap: 1px;
		padding-block-start: 1px;
	}

	.title {
		font: var(--type-ui-strong);
	}

	.text {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	@container (width >= 560px) {
		.facts:not(.bare) .items {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}

		.facts:not(.bare) li {
			flex-direction: column;
			gap: var(--sp-2);
		}
	}

	@supports (corner-shape: squircle) {
		.facts:not(.bare) {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
