import { describe, expect, it } from 'vitest';
import { toDataError } from '#lib/data/errors.ts';
import { isWithdrawn, LoadQueue } from './queries.ts';

/** A load that ends when told to, recording when it started. */
function gated(log: string[], id: string) {
	let finish: () => void = () => {};
	const done = new Promise<string>((resolve) => (finish = () => resolve(id)));
	return {
		load: () => {
			log.push(id);
			return done;
		},
		finish: () => finish()
	};
}

const tick = () => new Promise((resolve) => setTimeout(resolve, 0));

describe('LoadQueue', () => {
	it('runs at most its limit at once, handing slots over in order', async () => {
		const queue = new LoadQueue(2);
		const log: string[] = [];
		const loads = ['a', 'b', 'c', 'd'].map((id) => ({ id, ...gated(log, id) }));
		for (const { id } of loads) queue.want(id);
		const results = loads.map(({ id, load }) => queue.run(id, load));
		expect(log).toEqual(['a', 'b']);
		loads[1].finish();
		await tick();
		expect(log).toEqual(['a', 'b', 'c']);
		loads[0].finish();
		await tick();
		expect(log).toEqual(['a', 'b', 'c', 'd']);
		loads[2].finish();
		loads[3].finish();
		expect(await Promise.all(results)).toEqual(['a', 'b', 'c', 'd']);
	});

	it('starts a promoted load at once, ahead of the line', async () => {
		const queue = new LoadQueue(1);
		const log: string[] = [];
		const first = gated(log, 'a');
		const last = gated(log, 'z');
		queue.want('a');
		queue.want('m');
		queue.want('z');
		void queue.run('a', first.load);
		void queue.run('m', gated(log, 'm').load);
		void queue.run('z', last.load);
		queue.promote('z');
		expect(log).toEqual(['a', 'z']);
		first.finish();
		last.finish();
		await tick();
		expect(log).toEqual(['a', 'z', 'm']);
	});

	it('drops waiting loads nobody wants any more, as withdrawn', async () => {
		const queue = new LoadQueue(1);
		const log: string[] = [];
		const first = gated(log, 'a');
		const release = queue.want('a');
		const releaseB = queue.want('b');
		void queue.run('a', first.load);
		const second = queue.run('b', gated(log, 'b').load);
		releaseB();
		release();
		first.finish();
		const error = await second.catch((reason: unknown) => toDataError(reason));
		expect(log).toEqual(['a']);
		expect(isWithdrawn(typeof error === 'string' ? undefined : error)).toBe(true);
	});
});
