import { describe, expect, it } from 'vitest';
import { smartInvert } from './smartInvert.ts';

function run(...colours: [number, number, number][]): number[][] {
	const pixels = new Uint8ClampedArray(colours.flatMap((colour) => [...colour, 255]));
	smartInvert(pixels);
	return colours.map((_, index) => [...pixels.subarray(index * 4, index * 4 + 4)]);
}

describe('smartInvert', () => {
	it('turns white paper dark and black ink light, warm', () => {
		expect(run([255, 255, 255], [0, 0, 0])).toEqual([
			[24, 26, 30, 255],
			[230, 220, 200, 255]
		]);
	});

	it('inverts greys and near-greys fully', () => {
		const [[r, g, b]] = run([128, 126, 130]);
		expect([r, g, b]).toEqual([127, 124, 113]);
	});

	it('keeps saturated colours as they are', () => {
		expect(run([220, 30, 60], [20, 90, 200])).toEqual([
			[220, 30, 60, 255],
			[20, 90, 200, 255]
		]);
	});

	it('blends the saturations in between', () => {
		const [[r]] = run([200, 170, 170]);
		const inverted = 230.01 - 0.808 * 200;
		expect(r).toBeGreaterThan(inverted);
		expect(r).toBeLessThan(200);
	});
});
