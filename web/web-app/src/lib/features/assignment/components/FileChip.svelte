<script lang="ts">
	interface Props {
		name: string;
	}

	let { name }: Props = $props();

	/** The extension as Android's tiles print it: upper case, at most four letters. */
	const label = $derived.by(() => {
		const extension = /\.([a-z0-9]+)$/i.exec(name)?.[1];
		return extension && extension.length <= 4 ? extension.toUpperCase() : 'FILE';
	});
</script>

<span class="chip" aria-hidden="true">{label}</span>

<style>
	.chip {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 44px;
		block-size: 44px;
		border-radius: 14px;
		background: var(--course-container);
		color: var(--course-ink);
		font: 700 0.6875rem / 1 var(--font-ui);
		letter-spacing: 0.04em;
		box-shadow: var(--inner-light);
	}
</style>
