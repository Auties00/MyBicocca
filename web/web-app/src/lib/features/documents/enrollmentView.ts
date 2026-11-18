import IconBookOpenText from '~icons/ph/book-open-text';
import IconCalendarDots from '~icons/ph/calendar-dots';
import IconClockCountdown from '~icons/ph/clock-countdown';
import IconGraduationCap from '~icons/ph/graduation-cap';
import IconIdentificationBadge from '~icons/ph/identification-badge';
import IconMapPin from '~icons/ph/map-pin';
import IconNotepad from '~icons/ph/notepad';
import IconPauseCircle from '~icons/ph/pause-circle';
import IconPiggyBank from '~icons/ph/piggy-bank';
import IconWheelchair from '~icons/ph/wheelchair';
import { academicYearLabel } from '#lib/data/academicYear.ts';
import { formatDate } from '#lib/format/dates.ts';
import { formatCredits, formatNumber, formatPercent } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Fact } from '#lib/ui/Facts.svelte';
import type { Icon, Tone } from '#lib/ui/types.ts';
import type { AnnualEnrollment, EnrollmentStatus, EnrollmentType } from './enrollments.ts';
import { FactList, type FactSection } from './factSections.ts';
import { sentenceCase } from './text.ts';

export function statusLabel(status: EnrollmentStatus): string {
	switch (status) {
		case 'active':
			return m.documents_enrollment_status_active();
		case 'canceled':
			return m.documents_enrollment_status_canceled();
		case 'suspended':
			return m.documents_enrollment_status_suspended();
		case 'unknown':
			return m.documents_enrollment_status_unknown();
	}
}

/** The type in the reader's language; one Esse3 adds is named as Esse3 names it. */
export function typeLabel(enrollment: Pick<AnnualEnrollment, 'type' | 'typeDescription'>): string {
	const labels: Record<Exclude<EnrollmentType, 'unknown'>, () => string> = {
		in_progress: m.documents_enrollment_type_in_progress,
		out_of_course: m.documents_enrollment_type_out_of_course,
		repeating: m.documents_enrollment_type_repeating
	};
	return enrollment.type === 'unknown'
		? (enrollment.typeDescription ?? m.documents_enrollment_type_unknown())
		: labels[enrollment.type]();
}

export function courseYearLabel(courseYear: number): string {
	return courseYear > 0
		? m.documents_enrollment_year({ year: courseYear })
		: m.documents_enrollment_year_unknown();
}

/** "Enrolment 2026/27". */
export function enrollmentTitle(academicYear: number): string {
	return m.documents_enrollment_title({ year: academicYearLabel(academicYear) });
}

export interface EnrollmentBadge {
	label: string;
	tone: Tone;
}

/** What sets the year apart; a regular year in progress has none. */
export function enrollmentBadges(enrollment: AnnualEnrollment): EnrollmentBadge[] {
	const badges: EnrollmentBadge[] = [];
	if (enrollment.type === 'out_of_course') {
		badges.push({
			label:
				enrollment.outOfCourseYears > 0
					? m.documents_enrollment_badge_out_of_course_years({
							count: enrollment.outOfCourseYears
						})
					: m.documents_enrollment_type_out_of_course(),
			tone: 'warning'
		});
	}
	if (enrollment.type === 'repeating') {
		badges.push({ label: m.documents_enrollment_type_repeating(), tone: 'warning' });
	}
	if (enrollment.status === 'suspended') {
		badges.push({ label: m.documents_enrollment_status_suspended(), tone: 'warning' });
	}
	if (enrollment.status === 'canceled') {
		badges.push({ label: m.documents_enrollment_status_canceled(), tone: 'danger' });
	}
	if (enrollment.partTime !== null) {
		badges.push({
			label:
				enrollment.partTime.credits === null
					? m.documents_enrollment_group_part_time()
					: m.documents_enrollment_badge_part_time_credits({
							credits: formatCredits(enrollment.partTime.credits)
						}),
			tone: 'info'
		});
	}
	if (enrollment.conditional) {
		badges.push({ label: m.documents_enrollment_conditional(), tone: 'info' });
	}
	if (enrollment.awaitingDegree) {
		badges.push({ label: m.documents_enrollment_badge_awaiting_degree(), tone: 'info' });
	}
	if (enrollment.exemptionDescription !== null) {
		badges.push({ label: m.documents_enrollment_field_exemption(), tone: 'info' });
	}
	return badges;
}

const date = (value: Parameters<typeof formatDate>[0] | null) =>
	value === null ? null : formatDate(value, 'long');

const id = (value: number | null) => (value === null ? null : formatNumber(value));

/** A programme's name as the summary shows it, out of Esse3's capitals. */
const name = (value: string | null) => (value === null ? null : sentenceCase(value));

/**
 * The year's topics, each only when Esse3 filled one of its fields: a regular year shows the
 * essentials, while anomalies (part-time, a suspension, graduation) bring their own sections.
 */
export function enrollmentSections(enrollment: AnnualEnrollment): FactSection[] {
	const e = enrollment;
	const sections: FactSection[] = [];
	const push = (id: string, title: string, icon: Icon, facts: Fact[]) => {
		if (facts.length > 0) sections.push({ id, title, icon, facts });
	};

	push(
		'enrollment',
		m.documents_enrollment_group_enrollment(),
		IconIdentificationBadge,
		new FactList()
			.add(m.documents_enrollment_field_type(), typeLabel(e))
			.add(m.documents_enrollment_field_student_type(), e.studentTypeDescription)
			.add(
				m.documents_enrollment_field_out_of_course_years(),
				e.outOfCourseYears > 0 ? formatNumber(e.outOfCourseYears) : null
			)
			.add(
				m.documents_enrollment_field_condition(),
				e.conditional ? m.documents_enrollment_conditional() : null
			)
			.add(
				m.documents_enrollment_field_origin(),
				e.reconstructed ? m.documents_enrollment_reconstructed() : null
			)
			.add(m.documents_enrollment_field_status_reason(), e.statusReasonCode, true).items
	);

	if (e.partTime !== null) {
		const { partTime } = e;
		push(
			'part-time',
			m.documents_enrollment_group_part_time(),
			IconClockCountdown,
			new FactList()
				.add(
					m.documents_enrollment_field_planned_credits(),
					partTime.credits === null ? null : formatCredits(partTime.credits)
				)
				.add(
					m.documents_enrollment_field_extra_credits(),
					partTime.extraCredits === null ? null : formatCredits(partTime.extraCredits)
				)
				.add(
					m.documents_enrollment_field_changes(),
					partTime.locked ? m.documents_enrollment_locked() : m.documents_enrollment_allowed()
				).items
		);
	}

	if (e.suspension !== null) {
		const facts = new FactList();
		if (e.suspension.reasonCode !== null) {
			facts.add(m.documents_enrollment_field_reason(), e.suspension.reasonCode, true);
		} else {
			facts.add(m.documents_enrollment_field_status(), m.documents_enrollment_status_suspended());
		}
		push('suspension', m.documents_enrollment_group_suspension(), IconPauseCircle, facts.items);
	}

	if (e.awaitingDegree || e.degreeAwardDate !== null) {
		push(
			'degree',
			m.documents_enrollment_group_degree(),
			IconGraduationCap,
			new FactList()
				.add(
					m.documents_enrollment_field_status(),
					e.awaitingDegree ? m.documents_enrollment_badge_awaiting_degree() : null
				)
				.add(m.documents_enrollment_field_expected_degree_date(), date(e.degreeAwardDate)).items
		);
	}

	push(
		'benefits',
		m.documents_enrollment_group_benefits(),
		IconPiggyBank,
		new FactList()
			.add(m.documents_enrollment_field_exemption(), e.exemptionDescription)
			.add(m.documents_enrollment_field_income_band(), id(e.incomeBandId), true)
			.add(m.documents_enrollment_field_canteen_band(), id(e.canteenBandId), true)
			.add(m.documents_enrollment_field_merit_band(), id(e.meritBandId), true)
			.add(m.documents_enrollment_field_merit_note(), e.meritNote).items
	);

	push(
		'disability',
		m.documents_enrollment_group_disability(),
		IconWheelchair,
		new FactList()
			.add(m.documents_enrollment_field_disability_type(), e.disabilityTypeDescription)
			.add(
				m.documents_enrollment_field_disability_percentage(),
				e.disabilityPercentage === null ? null : formatPercent(e.disabilityPercentage / 100)
			).items
	);

	push(
		'programme',
		m.documents_enrollment_group_programme(),
		IconBookOpenText,
		new FactList()
			.add(m.documents_enrollment_field_course(), name(e.courseDescription))
			.add(m.documents_enrollment_field_course_type(), name(e.courseTypeDescription))
			.add(m.documents_enrollment_field_class(), name(e.degreeClassDescription))
			.add(m.documents_enrollment_field_class_code(), e.degreeClassCode, true)
			.add(m.documents_enrollment_field_path(), name(e.path))
			.add(m.documents_enrollment_field_orientation(), name(e.orientation))
			.add(m.documents_enrollment_field_study_order(), e.studyOrder)
			.add(
				m.documents_enrollment_field_credits_for_degree(),
				e.minimumCredits === null ? null : formatCredits(e.minimumCredits)
			)
			.add(
				m.documents_enrollment_field_duration(),
				e.courseDuration === null ? null : m.documents_years({ count: e.courseDuration })
			)
			.add(m.documents_enrollment_field_language(), e.teachingLanguage)
			.add(m.documents_enrollment_field_regulation(), e.regulationCode, true).items
	);

	push(
		'location',
		m.documents_enrollment_group_location(),
		IconMapPin,
		new FactList()
			.add(m.documents_enrollment_field_university(), e.universityDescription)
			.add(m.documents_enrollment_field_site(), e.siteDescription).items
	);

	push(
		'dates',
		m.documents_enrollment_group_dates(),
		IconCalendarDots,
		new FactList()
			.add(m.documents_enrollment_field_enrollment_date(), date(e.enrollmentDate))
			.add(m.documents_enrollment_field_inserted(), date(e.insertionDate))
			.add(m.documents_enrollment_field_updated(), date(e.modificationDate)).items
	);

	if (e.enrollmentNote !== null) {
		sections.push({
			id: 'notes',
			title: m.documents_enrollment_group_notes(),
			icon: IconNotepad,
			facts: [],
			note: e.enrollmentNote
		});
	}

	return sections;
}
