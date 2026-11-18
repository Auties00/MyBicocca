import { decodeEasyStaffPlanningManagedReservation } from '@mybicocca/data-api/easystaff';
import { describe, expect, it } from 'vitest';
import {
	applySync,
	fromManaged,
	isExpired,
	isSyncDue,
	reconcile,
	split,
	upsert,
	type PortalCheck,
	type Reservation
} from './reservation.ts';

const HOUR = 60 * 60_000;

const reservation = (code: string, start: number, end = start + HOUR / 3): Reservation => ({
	code,
	email: 'a@b.it',
	entryId: null,
	serviceId: 1,
	serviceName: 'Segreteria studenti',
	serviceGroup: null,
	areaName: null,
	areaAddress: null,
	start,
	end,
	qrCode: null,
	webConferenceUrl: null,
	createdAt: 0
});

describe('kept reservations', () => {
	const now = Date.UTC(2026, 9, 27, 10);

	it('forget an appointment a day after it ended, not before', () => {
		expect(isExpired(reservation('A', now - 26 * HOUR, now - 25 * HOUR), now)).toBe(true);
		expect(isExpired(reservation('B', now - 23 * HOUR, now - 22 * HOUR), now)).toBe(false);
	});

	it('stay in start order, a booking replacing the one with its code', () => {
		const list = upsert([reservation('B', now + 2 * HOUR), reservation('A', now + HOUR)], {
			...reservation('B', now - HOUR),
			areaName: 'Sportello 3'
		});
		expect(list.map((item) => [item.code, item.areaName])).toEqual([
			['B', 'Sportello 3'],
			['A', null]
		]);
	});

	it('split into what is coming and what just ended, latest first', () => {
		const { upcoming, past } = split(
			[
				reservation('old', now - 5 * HOUR),
				reservation('recent', now - 2 * HOUR),
				reservation('now', now - HOUR / 6),
				reservation('next', now + HOUR)
			],
			now
		);
		expect(upcoming.map((item) => item.code)).toEqual(['now', 'next']);
		expect(past.map((item) => item.code)).toEqual(['recent', 'old']);
	});
});

describe('fromManaged', () => {
	const managed = (entry: Record<string, unknown> | null) => {
		const value = decodeEasyStaffPlanningManagedReservation({
			success: true,
			codice: 'BK-9',
			area: { name: 'Edificio U6', address: ' ' },
			entry,
			servizio: { type: 'Didattica - Scienze' },
			qr_code: 'data:image/png;base64,AAAA'
		});
		if (value === undefined) throw new Error('Undecodable fixture');
		return value;
	};

	it('rebuilds a booking made elsewhere from the portal’s page', () => {
		const found = fromManaged(
			'bk-9',
			'a@b.it',
			managed({ id: 4, start_time: 1_800_000_000, end_time: 1_800_001_200 }),
			{ id: 201, name: null, group: 'Didattica' },
			5
		);
		expect(found).toEqual({
			code: 'BK-9',
			email: 'a@b.it',
			entryId: 4,
			serviceId: 201,
			serviceName: 'Didattica - Scienze',
			serviceGroup: 'Didattica',
			areaName: 'Edificio U6',
			areaAddress: null,
			start: 1_800_000_000_000,
			end: 1_800_001_200_000,
			qrCode: 'data:image/png;base64,AAAA',
			webConferenceUrl: null,
			createdAt: 5
		});
	});

	it('gives up without a time', () => {
		expect(
			fromManaged('BK-9', 'a@b.it', managed(null), { id: null, name: null, group: null }, 0)
		).toBeUndefined();
	});
});

describe('syncing with the portal', () => {
	const now = Date.UTC(2026, 9, 27, 10);
	const ended = reservation('OLD', now - 30 * HOUR, now - 29 * HOUR);
	const kept = reservation('A', now + HOUR);
	const cancelled = reservation('B', now + 2 * HOUR);
	const later = reservation('C', now + 3 * HOUR);

	const portal = (answers: Record<string, PortalCheck>) => {
		const asked: string[] = [];
		const check = async (item: Reservation): Promise<PortalCheck> => {
			asked.push(item.code);
			return answers[item.code] ?? { kind: 'found', qrCode: null };
		};
		return { asked, check };
	};

	it('forgets ended bookings without asking, and those the portal no longer knows', async () => {
		const { asked, check } = portal({ B: { kind: 'missing' } });
		const changes = await reconcile([ended, kept, cancelled, later], now, check);
		expect(asked).toEqual(['A', 'B', 'C']);
		expect([...changes.gone]).toEqual(['OLD', 'B']);
		expect(applySync([ended, kept, cancelled, later], changes).map((item) => item.code)).toEqual([
			'A',
			'C'
		]);
	});

	it('stops at a check that fails otherwise, keeping every booking it didn’t reach', async () => {
		const { asked, check } = portal({ A: { kind: 'failed' }, B: { kind: 'missing' } });
		const changes = await reconcile([ended, kept, cancelled, later], now, check);
		expect(asked).toEqual(['A']);
		expect([...changes.gone]).toEqual(['OLD']);
		expect(applySync([kept, cancelled, later], changes)).toEqual([kept, cancelled, later]);
	});

	it('fills in a missing QR and never replaces one', async () => {
		const withQr = { ...later, qrCode: 'data:image/png;base64,KEPT' };
		const { check } = portal({
			A: { kind: 'found', qrCode: 'data:image/png;base64,NEW' },
			C: { kind: 'found', qrCode: 'data:image/png;base64,OTHER' }
		});
		const changes = await reconcile([kept, withQr], now, check);
		const synced = applySync([kept, withQr], changes);
		expect(synced.map((item) => item.qrCode)).toEqual([
			'data:image/png;base64,NEW',
			'data:image/png;base64,KEPT'
		]);
	});

	it('asks at most every ten minutes', () => {
		expect(isSyncDue(undefined, now)).toBe(true);
		expect(isSyncDue(now - 9 * 60_000, now)).toBe(false);
		expect(isSyncDue(now - 10 * 60_000, now)).toBe(true);
	});
});
