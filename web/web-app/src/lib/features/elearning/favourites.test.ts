import { beforeEach, describe, expect, it, vi } from 'vitest';

const store = vi.hoisted(() => ({
	read: vi.fn<(key: string, sealed: boolean) => Promise<{ data: unknown } | undefined>>(),
	write: vi.fn<(key: string, value: { data: unknown }, sealed: boolean) => Promise<void>>()
}));

vi.mock('#lib/data/queryStore.ts', () => ({ idbPersistence: store }));
vi.mock('#lib/data/session.svelte.ts', () => ({ session: { accounts: [] } }));

const { AccountFavourites } = await import('./favourites.svelte.ts');

describe('AccountFavourites', () => {
	beforeEach(() => {
		store.read.mockReset();
		store.write.mockReset().mockResolvedValue(undefined);
	});

	it('prefers the choice made here over Moodle’s flag', async () => {
		store.read.mockResolvedValue({ data: { 1: false } });
		const favourites = new AccountFavourites('a');
		await favourites.load();
		expect(favourites.has({ id: 1, moodleFavourite: true })).toBe(false);
		expect(favourites.has({ id: 2, moodleFavourite: true })).toBe(true);
		expect(favourites.has({ id: 3, moodleFavourite: false })).toBe(false);
	});

	it('keeps a star chosen before the stored ones arrive, and stores both', async () => {
		let arrive: (value: { data: unknown }) => void = () => undefined;
		store.read.mockReturnValue(new Promise((resolve) => (arrive = resolve)));
		const favourites = new AccountFavourites('a');
		const loading = favourites.load();
		const starring = favourites.set([2, 3], true);
		expect(favourites.choices).toEqual({ 2: true, 3: true });
		// Writing before the stored choices are read would lose them.
		expect(store.write).not.toHaveBeenCalled();
		arrive({ data: { 1: true, 2: false } });
		await Promise.all([loading, starring]);
		expect(favourites.choices).toEqual({ 1: true, 2: true, 3: true });
		expect(store.write).toHaveBeenCalledOnce();
		expect(store.write.mock.calls[0][1].data).toEqual({ 1: true, 2: true, 3: true });
	});

	it('reads the stored choices once', async () => {
		store.read.mockResolvedValue(undefined);
		const favourites = new AccountFavourites('a');
		await favourites.load();
		await favourites.set([1], true);
		await favourites.set([1], false);
		expect(store.read).toHaveBeenCalledOnce();
		expect(store.write.mock.calls.at(-1)?.[1].data).toEqual({ 1: false });
	});
});
