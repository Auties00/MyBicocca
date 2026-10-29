import { describe, expect, it } from 'vitest';
import { pinShape, spreadHeads, type Head, type Offset } from './pinShape.ts';

describe('pinShape', () => {
	it('stands the head over the tip, or leans it aside with the tip in place', () => {
		const upright = pinShape(36, 9);
		expect(upright).toMatchObject({ width: 42, height: 51, headX: 21, headY: 21, tipX: 21 });

		const leaning = pinShape(36, 9, 30);
		expect(leaning.headX - leaning.tipX).toBe(30);
		expect(leaning.tipX).toBe(3);
		expect(leaning.width).toBe(leaning.headX + 18 + 3);
	});
});

describe('spreadHeads', () => {
	const gap = (a: Head, b: Head, [da, db]: Offset[]) =>
		Math.hypot(b.x + db.dx - (a.x + da.dx), b.y + db.dy - (a.y + da.dy)) - a.r - b.r;

	it('leaves heads that already stand apart', () => {
		const still = { dx: 0, dy: 0 };
		expect(
			spreadHeads([
				{ x: 0, y: 0, r: 19 },
				{ x: 60, y: 0, r: 19 }
			])
		).toEqual([still, still]);
		expect(
			spreadHeads([
				{ x: 0, y: 0, r: 19 },
				{ x: 0, y: 60, r: 19 }
			])
		).toEqual([still, still]);
	});

	it('pushes heads side by side apart sideways, half each', () => {
		const heads = [
			{ x: 100, y: 100, r: 19 },
			{ x: 110, y: 100, r: 19 }
		];
		const moved = spreadHeads(heads);
		expect(moved[0].dx).toBeLessThan(0);
		expect(moved[1].dx).toBeGreaterThan(0);
		expect(Math.abs(moved[0].dx + moved[1].dx)).toBeLessThanOrEqual(1);
		expect(gap(heads[0], heads[1], moved)).toBeGreaterThanOrEqual(1);
	});

	it('lifts the upper of two stacked heads, never sinking the lower', () => {
		const heads = [
			{ x: 100, y: 100, r: 19 },
			{ x: 100, y: 110, r: 19 }
		];
		const moved = spreadHeads(heads);
		expect(moved[0].dy).toBeLessThan(0);
		expect(moved[1].dy).toBe(0);
		expect(gap(heads[0], heads[1], moved)).toBeGreaterThanOrEqual(1);
	});

	it('clears a cluster, each head from every other', () => {
		const heads = [
			{ x: 200, y: 200, r: 19 },
			{ x: 185, y: 195, r: 19 },
			{ x: 205, y: 188, r: 19 },
			{ x: 190, y: 182, r: 19 }
		];
		const moved = spreadHeads(heads);
		for (let i = 0; i < heads.length; i++) {
			for (let j = i + 1; j < heads.length; j++) {
				expect(gap(heads[i], heads[j], [moved[i], moved[j]])).toBeGreaterThanOrEqual(0.5);
			}
		}
		for (const { dy } of moved) expect(dy).toBeLessThanOrEqual(0);
	});

	it('moves no head further than the limit', () => {
		const heads = Array.from({ length: 6 }, () => ({ x: 0, y: 0, r: 19 }));
		for (const { dx, dy } of spreadHeads(heads, 2, 30)) {
			expect(Math.abs(dx)).toBeLessThanOrEqual(30);
			expect(dy).toBeGreaterThanOrEqual(-30);
		}
	});
});
