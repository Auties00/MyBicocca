<script lang="ts">
	import { italianTitleCase, personName } from '#lib/format/names.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { planState, yearTitle } from '../labels.ts';
	import type { PlanYear, StudyPlan } from '../plan.ts';

	interface Props {
		plan: StudyPlan;
		years: readonly PlanYear[];
		student: { name: string; number: string; degree: string };
		/** "Percorso comune · piano standard". */
		path: string;
	}

	let { plan, years, student, path }: Props = $props();

	const credits = (value: number) => formatNumber(value, { maximumFractionDigits: 1 });
	const total = $derived(years.reduce((sum, year) => sum + year.credits, 0));
	const status = $derived(planState(plan.state));
	const today = () => formatDate(new Date(), 'long');
	/* Dated when printed: the page may stay open across midnight. */
	let printedOn = $state(today());

	/*
	 * The sheet lives at the end of <body>, outside the app, so the print stylesheet can show it
	 * alone, whatever the shell around the page.
	 */
	function toBody(node: HTMLElement) {
		document.body.append(node);
		return () => node.remove();
	}
</script>

<svelte:window onbeforeprint={() => (printedOn = today())} />

<article class="sheet studyplan-print" {@attach toBody}>
	<header class="masthead">
		<p class="university">{m.studyplan_print_university()}</p>
		<h1 class="title">{m.studyplan_print_title()}</h1>
	</header>

	<dl class="facts">
		<div>
			<dt>{m.studyplan_print_student()}</dt>
			<dd>{personName(student.name)}</dd>
		</div>
		<div>
			<dt>{m.studyplan_print_number()}</dt>
			<dd class="code">{student.number}</dd>
		</div>
		<div>
			<dt>{m.studyplan_print_degree()}</dt>
			<dd>{italianTitleCase(student.degree)}</dd>
		</div>
		<div>
			<dt>{m.studyplan_print_path()}</dt>
			<dd>{path}</dd>
		</div>
		{#if status}
			<div>
				<dt>{m.studyplan_print_status()}</dt>
				<dd>
					{status.label}{plan.lastChange
						? `, ${m.studyplan_state_since({ date: formatDate(plan.lastChange, 'long') })}`
						: ''}
				</dd>
			</div>
		{/if}
	</dl>

	{#each years as group (group.year)}
		<table class="year">
			<caption>{yearTitle(group.year)}</caption>
			<thead>
				<tr>
					<th scope="col" class="code-col">{m.studyplan_print_code()}</th>
					<th scope="col">{m.studyplan_print_activity()}</th>
					<th scope="col" class="num">{m.studyplan_print_credits()}</th>
				</tr>
			</thead>
			<tbody>
				{#each group.courses as course (course.id)}
					<tr>
						<td class="code">{course.code ?? ''}</td>
						<td>{italianTitleCase(course.name)}</td>
						<td class="num">{credits(course.credits)}</td>
					</tr>
				{/each}
			</tbody>
			<tfoot>
				<tr>
					<td></td>
					<th scope="row">{m.studyplan_activities_count({ count: group.courses.length })}</th>
					<td class="num">{credits(group.credits)}</td>
				</tr>
			</tfoot>
		</table>
	{/each}

	<p class="total">
		<span>{m.studyplan_print_total()}</span>
		<span class="num">{credits(total)} {m.studyplan_print_credits()}</span>
	</p>
	<p class="note">{m.studyplan_print_note({ date: printedOn })}</p>
</article>

<style>
	.sheet {
		display: none;
	}

	@media print {
		@page {
			margin: 16mm 14mm;
		}

		/* Only while this page is open: the sheet is the document. */
		:global(body:has(> .studyplan-print) > :not(.studyplan-print)) {
			display: none !important;
		}

		:global(html:has(> body > .studyplan-print)),
		:global(body:has(> .studyplan-print)) {
			background: #fff;
		}

		/* Paper: dark ink on white whatever the theme, so plain colours rather than theme tokens. */
		.sheet {
			display: block;
			color-scheme: light;
			color: #1a1414;
			font: 10.5pt / 1.45 var(--font-ui);
		}

		.masthead {
			padding-block-end: 10pt;
			border-block-end: 2pt solid var(--brand);
		}

		.university {
			color: #6b5f60;
			font-size: 9pt;
			letter-spacing: 0.06em;
			text-transform: uppercase;
		}

		.title {
			margin-block-start: 2pt;
			font: 500 24pt / 1.1 var(--font-display);
			letter-spacing: -0.015em;
		}

		.facts {
			display: grid;
			grid-template-columns: repeat(2, minmax(0, 1fr));
			gap: 6pt 18pt;
			margin-block: 14pt 18pt;
		}

		.facts dt {
			color: #6b5f60;
			font-size: 8.5pt;
		}

		.facts dd {
			font-weight: 600;
		}

		/* A year is a few rows: whole on one page, its caption and header never left behind. */
		.year {
			inline-size: 100%;
			margin-block-end: 14pt;
			border-collapse: collapse;
			break-inside: avoid;
		}

		.year caption,
		.year thead {
			break-after: avoid;
		}

		/* Printed once, after the rows, rather than repeated on every page the table spans. */
		.year tfoot {
			display: table-row-group;
		}

		.year caption {
			padding-block-end: 4pt;
			text-align: start;
			font: 500 13pt / 1.2 var(--font-display);
		}

		.year th,
		.year td {
			padding: 3.5pt 6pt 3.5pt 0;
			border-block-end: 0.5pt solid #d9d1d1;
			text-align: start;
			vertical-align: top;
		}

		.year thead th {
			color: #6b5f60;
			font-size: 8.5pt;
			font-weight: 600;
			border-block-end-color: #1a1414;
		}

		.year tr {
			break-inside: avoid;
		}

		.year tfoot th,
		.year tfoot td {
			border-block-end: none;
			font-weight: 600;
		}

		.code-col {
			inline-size: 22%;
		}

		.code {
			font-variant-numeric: tabular-nums slashed-zero;
		}

		.num {
			inline-size: 10%;
			text-align: end !important;
			font-variant-numeric: tabular-nums;
		}

		.total {
			display: flex;
			justify-content: space-between;
			padding-block: 6pt;
			border-block: 1pt solid #1a1414;
			font-weight: 700;
		}

		.note {
			margin-block-start: 12pt;
			color: #6b5f60;
			font-size: 8.5pt;
		}
	}
</style>
