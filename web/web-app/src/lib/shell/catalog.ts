import { untrack } from 'svelte';
import IconBell from '~icons/ph/bell-simple';
import IconTree from '~icons/ph/tree-structure';
import IconUserFocus from '~icons/ph/user-focus';
import IconListChecks from '~icons/ph/list-checks';
import IconClipboard from '~icons/ph/clipboard-text';
import IconCalendarCheck from '~icons/ph/calendar-check';
import IconHeadset from '~icons/ph/headset';
import IconBooks from '~icons/ph/books';
import IconIdCard from '~icons/ph/identification-card';
import IconScroll from '~icons/ph/scroll';
import IconSealCheck from '~icons/ph/seal-check';
import IconReceipt from '~icons/ph/receipt';
import IconScales from '~icons/ph/scales';
import IconHandCoins from '~icons/ph/hand-coins';
import IconExam from '~icons/ph/exam';
import IconCalculator from '~icons/ph/calculator';
import IconPalette from '~icons/ph/palette';
import IconTranslate from '~icons/ph/translate';
import IconLock from '~icons/ph/lock-simple';
import IconFolder from '~icons/ph/folder-simple';
import IconInfo from '~icons/ph/info';
import IconShieldCheck from '~icons/ph/shield-check';
import IconCertificate from '~icons/ph/certificate';
import IconSparkle from '~icons/ph/sparkle';
import IconQrCode from '~icons/ph/qr-code';
import { m } from '#lib/paraglide/messages.js';
import type { ChipTone, Icon } from '#lib/ui/types.ts';
import { allDestinations } from '#lib/ui/destinations.ts';
import { shellHref, type ShellPath } from './nav.ts';
import { matchScore } from './match.ts';
import { paletteRegistry, type PaletteItem, type PaletteProvider } from './palette.svelte.ts';

export type ServiceGroup = 'teaching' | 'bookings' | 'documents' | 'fees';

export interface ServiceEntry {
	id: string;
	path: ShellPath;
	group: ServiceGroup | 'deadlines';
	icon: Icon;
	/** Icon chip tint, by group (as the Android hub; violet for Documents, a hue with no status). */
	tone: ChipTone;
	label: () => string;
	description: () => string;
	keywords: () => string;
}

export const serviceGroupLabels: Record<ServiceGroup, () => string> = {
	teaching: m.shell_group_teaching,
	bookings: m.shell_group_bookings,
	documents: m.shell_group_documents,
	fees: m.shell_group_fees
};

/** Every service page of ia.md, in hub order: labels, icons and groups shared by hub and palette. */
export const serviceCatalog: readonly ServiceEntry[] = [
	{
		id: 'deadlines',
		path: 'services/deadlines',
		group: 'deadlines',
		icon: IconBell,
		tone: 'accent',
		label: m.shell_service_deadlines,
		description: m.shell_service_deadlines_desc,
		keywords: m.shell_kw_deadlines
	},
	{
		id: 'study-plan',
		path: 'services/study-plan',
		group: 'teaching',
		icon: IconTree,
		tone: 'info',
		label: m.shell_service_study_plan,
		description: m.shell_service_study_plan_desc,
		keywords: m.shell_kw_study_plan
	},
	{
		id: 'attendance',
		path: 'services/attendance',
		group: 'teaching',
		icon: IconUserFocus,
		tone: 'info',
		label: m.shell_service_attendance,
		description: m.shell_service_attendance_desc,
		keywords: m.shell_kw_attendance
	},
	{
		id: 'results',
		path: 'services/results',
		group: 'teaching',
		icon: IconListChecks,
		tone: 'info',
		label: m.shell_service_results,
		description: m.shell_service_results_desc,
		keywords: m.shell_kw_results
	},
	{
		id: 'questionnaires',
		path: 'services/questionnaires',
		group: 'teaching',
		icon: IconClipboard,
		tone: 'info',
		label: m.shell_service_questionnaires,
		description: m.shell_service_questionnaires_desc,
		keywords: m.shell_kw_questionnaires
	},
	{
		id: 'exams',
		path: 'services/exams',
		group: 'bookings',
		icon: IconCalendarCheck,
		tone: 'success',
		label: m.shell_service_exams,
		description: m.shell_service_exams_desc,
		keywords: m.shell_kw_exams
	},
	{
		id: 'appointments',
		path: 'services/appointments',
		group: 'bookings',
		icon: IconHeadset,
		tone: 'success',
		label: m.shell_service_appointments,
		description: m.shell_service_appointments_desc,
		keywords: m.shell_kw_appointments
	},
	{
		id: 'library',
		path: 'services/library',
		group: 'bookings',
		icon: IconBooks,
		tone: 'success',
		label: m.shell_service_library,
		description: m.shell_service_library_desc,
		keywords: m.shell_kw_library
	},
	{
		id: 'enrollments',
		path: 'services/enrollments',
		group: 'documents',
		icon: IconIdCard,
		tone: 'violet',
		label: m.shell_service_enrollments,
		description: m.shell_service_enrollments_desc,
		keywords: m.shell_kw_enrollments
	},
	{
		id: 'degrees',
		path: 'services/degrees',
		group: 'documents',
		icon: IconScroll,
		tone: 'violet',
		label: m.shell_service_degrees,
		description: m.shell_service_degrees_desc,
		keywords: m.shell_kw_degrees
	},
	{
		id: 'certificates',
		path: 'services/certificates',
		group: 'documents',
		icon: IconSealCheck,
		tone: 'violet',
		label: m.shell_service_certificates,
		description: m.shell_service_certificates_desc,
		keywords: m.shell_kw_certificates
	},
	{
		id: 'fees',
		path: 'services/fees',
		group: 'fees',
		icon: IconReceipt,
		tone: 'gold',
		label: m.shell_service_fees,
		description: m.shell_service_fees_desc,
		keywords: m.shell_kw_fees
	},
	{
		id: 'isee',
		path: 'services/isee',
		group: 'fees',
		icon: IconScales,
		tone: 'gold',
		label: m.shell_service_isee,
		description: m.shell_service_isee_desc,
		keywords: m.shell_kw_isee
	},
	{
		id: 'refunds',
		path: 'services/refunds',
		group: 'fees',
		icon: IconHandCoins,
		tone: 'gold',
		label: m.shell_service_refunds,
		description: m.shell_service_refunds_desc,
		keywords: m.shell_kw_refunds
	}
];

interface PageEntry {
	id: string;
	path: ShellPath;
	icon: Icon;
	label: () => string;
	parent: () => string;
	keywords?: () => string;
}

const subPages: readonly PageEntry[] = [
	{
		id: 'profile-exams',
		path: 'profile/exams',
		icon: IconExam,
		label: m.shell_profile_exams,
		parent: m.shell_profile,
		keywords: m.shell_kw_profile_exams
	},
	{
		id: 'profile-simulator',
		path: 'profile/simulator',
		icon: IconCalculator,
		label: m.shell_profile_simulator,
		parent: m.shell_profile
	},
	{
		id: 'settings-appearance',
		path: 'settings/appearance',
		icon: IconPalette,
		label: m.shell_settings_appearance,
		parent: m.shell_settings,
		keywords: m.shell_kw_appearance
	},
	{
		id: 'settings-language',
		path: 'settings/language',
		icon: IconTranslate,
		label: m.shell_settings_language,
		parent: m.shell_settings,
		keywords: m.shell_kw_language
	},
	{
		id: 'settings-security',
		path: 'settings/security',
		icon: IconLock,
		label: m.shell_settings_security,
		parent: m.shell_settings,
		keywords: m.shell_kw_security
	},
	{
		id: 'settings-files',
		path: 'settings/files',
		icon: IconFolder,
		label: m.shell_settings_files,
		parent: m.shell_settings,
		keywords: m.shell_kw_files
	},
	{
		id: 'settings-about',
		path: 'settings/about',
		icon: IconInfo,
		label: m.shell_settings_about,
		parent: m.shell_settings,
		keywords: m.shell_kw_about
	},
	{
		id: 'settings-releases',
		path: 'settings/about/releases',
		icon: IconSparkle,
		label: m.shell_settings_releases,
		parent: m.shell_settings,
		keywords: m.shell_kw_releases
	},
	{
		id: 'settings-privacy',
		path: 'settings/privacy',
		icon: IconShieldCheck,
		label: m.shell_settings_privacy,
		parent: m.shell_settings,
		keywords: m.shell_kw_privacy
	},
	{
		id: 'settings-licences',
		path: 'settings/licences',
		icon: IconCertificate,
		label: m.shell_settings_licences,
		parent: m.shell_settings,
		keywords: m.shell_kw_licences
	}
];

const destinationKeywords: Record<string, () => string> = {
	calendar: m.shell_kw_calendar,
	elearning: m.shell_kw_elearning,
	map: m.shell_kw_map,
	services: m.shell_kw_services,
	profile: m.shell_kw_profile,
	settings: m.shell_kw_settings
};

/** A comma-separated keyword message as its words. */
export const keywordList = (list: () => string): string[] =>
	list()
		.split(',')
		.map((word) => word.trim())
		.filter(Boolean);

/** A palette row with the extra words it should also be found by. */
export type CatalogItem = PaletteItem & { keywords?: string[] };

/** Every page of the catalogue as palette rows. */
export function catalogItems(): CatalogItem[] {
	return [
		...allDestinations.map((d) => ({
			id: `page:${d.id}`,
			title: d.label(),
			icon: d.icon,
			group: m.shell_palette_pages(),
			href: shellHref(d.path),
			shortcut: ['g', d.key],
			keywords: keywordList(destinationKeywords[d.id])
		})),
		...serviceCatalog.map((s) => ({
			id: `service:${s.id}`,
			title: s.label(),
			subtitle: s.description(),
			icon: s.icon,
			group: m.shell_palette_services(),
			href: shellHref(s.path),
			keywords: keywordList(s.keywords)
		})),
		...subPages.map((p) => ({
			id: `page:${p.id}`,
			title: p.label(),
			subtitle: p.parent(),
			icon: p.icon,
			group: p.path.startsWith('settings') ? m.shell_palette_settings() : m.shell_palette_pages(),
			href: shellHref(p.path),
			keywords: p.keywords ? keywordList(p.keywords) : undefined
		})),
		{
			id: 'action:check-in',
			title: m.shell_action_check_in(),
			subtitle: m.shell_service_attendance(),
			icon: IconQrCode,
			group: m.shell_palette_actions(),
			href: shellHref('services/attendance/check-in'),
			keywords: keywordList(m.shell_kw_check_in)
		}
	];
}

/**
 * The static catalogue (plus the shell's actions) as a palette provider. With a provider that
 * rescores the others' rows it returns every row, for that matcher to judge; `matchScore` scores
 * and filters them otherwise.
 */
export function catalogProvider(actions: () => CatalogItem[]): PaletteProvider {
	return {
		id: 'catalog',
		search(query) {
			const rows = [...catalogItems(), ...actions()].map((item) => ({
				...item,
				score: matchScore(query, item)
			}));
			const rescored = untrack(() => paletteRegistry.providers).some(
				(provider) => provider.id !== 'catalog' && provider.rescore
			);
			return rescored ? rows : rows.filter((item) => item.score > 0);
		}
	};
}
