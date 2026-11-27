import { m } from '#lib/paraglide/messages.js';
import { serviceCatalog, type ServiceEntry, type ServiceGroup } from '#lib/shell/catalog.ts';

export const serviceGroups: readonly ServiceGroup[] = ['teaching', 'bookings', 'documents', 'fees'];

export const groupCaptions: Record<ServiceGroup, () => string> = {
	teaching: m.services_group_teaching_caption,
	bookings: m.services_group_bookings_caption,
	documents: m.services_group_documents_caption,
	fees: m.services_group_fees_caption
};

export function servicesOf(group: ServiceGroup): ServiceEntry[] {
	return serviceCatalog.filter((entry) => entry.group === group);
}

export function serviceEntry(id: string): ServiceEntry {
	const entry = serviceCatalog.find((item) => item.id === id);
	if (entry === undefined) throw new Error(`The service catalog lists no ${id} page`);
	return entry;
}
