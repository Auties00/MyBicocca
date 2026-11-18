<script lang="ts">
	import IconEnvelopeSimple from '~icons/ph/envelope-simple';
	import { courseShapes, shapePath } from '#lib/features/elearning/shapes.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { StaffMember } from '../../course.svelte.ts';

	interface Props {
		staff: readonly StaffMember[];
		courseTitle: string | undefined;
		courseId: number;
	}

	let { staff, courseTitle, courseId }: Props = $props();

	const roles: Record<StaffMember['role'], () => string> = {
		teacher: m.course_role_teacher,
		tutor: m.course_role_tutor,
		assistant: m.course_role_assistant,
		staff: m.course_role_staff
	};

	const shapes = $derived(courseShapes(courseId + 1));

	function initials(member: StaffMember): string {
		if (member.initials) return member.initials;
		const letters = member.name
			.split(/\s+/)
			.filter((word) => word !== '')
			.slice(0, 2)
			.map((word) => word.charAt(0).toLocaleUpperCase());
		return letters.join('') || '?';
	}

	/** A mail about this course: its subject names it, as the Android app writes it. */
	function mailto(email: string): `mailto:${string}` {
		const subject = encodeURIComponent(`${courseTitle ?? ''} | `);
		return `mailto:${email}?subject=${subject}`;
	}
</script>

<ul class="staff">
	{#each staff as member, index (`${member.name}-${index}`)}
		{@const shape = shapes[index % shapes.length]}
		<li>
			<svelte:element
				this={member.email ? 'a' : 'div'}
				class={['member', { link: member.email }]}
				href={member.email ? mailto(member.email) : undefined}
			>
				<span class={['avatar', `tone-${index % 2}`]} aria-hidden="true">
					<svg viewBox="0 0 100 100" focusable="false"><path d={shapePath(shape)} /></svg>
					<span class="letters">{initials(member)}</span>
				</span>
				<span class="who">
					<span class="name">{member.name}</span>
					<span class="role">{roles[member.role]()}</span>
				</span>
				{#if member.email}
					<span class="sr-only">, {m.course_staff_write()}</span>
					<IconEnvelopeSimple class="mail" aria-hidden="true" />
				{/if}
			</svelte:element>
		</li>
	{/each}
</ul>

<style>
	.staff {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 220px), 1fr));
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.member {
		display: grid;
		grid-template-columns: 44px minmax(0, 1fr) auto;
		align-items: center;
		gap: 12px;
		block-size: 100%;
		padding: 12px 14px 12px 12px;
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: var(--elev-1);
		color: inherit;
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.link:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.link:active {
		transform: scale(0.98);
		transition: transform var(--dur-instant) var(--ease-out);
	}

	.avatar {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
	}

	.avatar svg {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
	}

	.tone-0 svg {
		fill: var(--course-container);
	}

	.tone-1 svg {
		fill: var(--gold-container);
	}

	.letters {
		position: relative;
		font: var(--type-ui-strong);
		color: var(--course-ink);
	}

	.tone-1 .letters {
		color: var(--on-gold-container);
	}

	.who {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.name {
		font: var(--type-ui-strong);
		font-size: 0.9375rem;
		overflow-wrap: anywhere;
	}

	.role {
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.member :global(.mail) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
		color: var(--text-subtle);
	}
</style>
