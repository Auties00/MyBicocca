import IconBookOpenText from '~icons/ph/book-open-text';
import IconBriefcase from '~icons/ph/briefcase';
import IconCertificate from '~icons/ph/certificate';
import IconCompass from '~icons/ph/compass';
import IconFirstAidKit from '~icons/ph/first-aid-kit';
import IconGlobe from '~icons/ph/globe-hemisphere-west';
import IconHeadset from '~icons/ph/headset';
import IconIdentificationBadge from '~icons/ph/identification-badge';
import IconPackage from '~icons/ph/package';
import IconPenNib from '~icons/ph/pen-nib';
import IconPiggyBank from '~icons/ph/piggy-bank';
import IconStudent from '~icons/ph/student';
import type { Icon, Tone } from '#lib/ui/types.ts';
import type { SectionId, ServiceKind } from './directory.ts';

/** Picked by section id, so every language gets the same glyph. */
export const sectionIcons: Record<SectionId, Icon> = {
	career: IconStudent,
	teaching: IconBookOpenText,
	deliveries: IconPackage,
	other: IconHeadset
};

export const sectionTones: Record<SectionId, Tone> = {
	career: 'accent',
	teaching: 'info',
	deliveries: 'gold',
	other: 'success'
};

export const kindIcons: Record<ServiceKind, Icon> = {
	badge: IconIdentificationBadge,
	diploma: IconCertificate,
	doctorate: IconPenNib,
	specialization: IconFirstAidKit,
	grants: IconPiggyBank,
	international: IconGlobe,
	internship: IconBriefcase,
	orientation: IconCompass,
	career: IconStudent,
	teaching: IconBookOpenText,
	desk: IconHeadset
};
