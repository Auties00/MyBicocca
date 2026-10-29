import { demoService } from '#lib/demo/fixtures/planning/catalog.ts';
import { qrDataUrl } from '#lib/demo/fixtures/planning/qr.ts';
import { personaBooking } from '#lib/demo/fixtures/planning/reservations.ts';
import type { Reservation } from './reservation.ts';

/** The demo account's appointments on first use: the persona's booking, as on Android. */
export function demoReservations(): Reservation[] {
	const booking = personaBooking();
	return [
		{
			code: booking.code,
			email: booking.email,
			entryId: booking.entryId,
			serviceId: booking.serviceId,
			serviceName: booking.serviceName,
			serviceGroup: demoService(booking.serviceId)?.group ?? null,
			areaName: booking.area.name,
			areaAddress: booking.area.address,
			start: booking.start * 1000,
			end: booking.end * 1000,
			qrCode: qrDataUrl(booking.code),
			webConferenceUrl: booking.webConferenceUrl,
			createdAt: Date.now()
		}
	];
}
