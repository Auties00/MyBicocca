<script lang="ts">
	interface Props {
		/** Full name: initials are drawn from it when there is no photo. */
		name: string;
		/** Photo URL (object URL or remote). Falls back to initials if it fails to load. */
		src?: string;
		/** Diameter in px. */
		size?: number;
		/** accent = the signed-in user; neutral = other people. */
		tone?: 'accent' | 'neutral';
		/** Decorative when the name is printed next to it (the default). */
		decorative?: boolean;
	}

	let { name, src, size = 40, tone = 'accent', decorative = true }: Props = $props();

	let failedSrc = $state<string>();

	const initials = $derived(
		name
			.trim()
			.split(/\s+/)
			.filter(Boolean)
			.filter((_, i, all) => i === 0 || i === all.length - 1)
			.map((part) => part[0]?.toLocaleUpperCase())
			.join('')
	);
</script>

<span
	class={['avatar', tone]}
	style:--size="{size}px"
	role={decorative ? undefined : 'img'}
	aria-label={decorative ? undefined : name}
	aria-hidden={decorative ? 'true' : undefined}
>
	{#if src && src !== failedSrc}
		<img
			{src}
			alt=""
			width={size}
			height={size}
			decoding="async"
			onerror={() => (failedSrc = src)}
		/>
	{:else}
		<span class="initials" data-initials={initials}></span>
	{/if}
</span>

<style>
	.avatar {
		display: inline-grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: var(--size);
		block-size: var(--size);
		border-radius: 50%;
		overflow: hidden;
		background: var(--accent);
		color: var(--on-accent);
		box-shadow: inset 0 0 0 1px var(--hairline);
		user-select: none;
	}

	.neutral {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	img {
		inline-size: 100%;
		block-size: 100%;
		object-fit: cover;
	}

	/* Drawn as generated content with empty alt text: the name around the avatar already says it. */
	.initials::before {
		content: attr(data-initials) / '';
		font: 600 calc(var(--size) * 0.38) / 1 var(--font-ui);
		letter-spacing: 0.02em;
	}
</style>
