import { describe, expect, it } from 'vitest';
import {
	chipIndex,
	crossesEdge,
	faceAt,
	javaHashCode,
	nearestFace,
	releaseTarget,
	tapTarget
} from './card.ts';

describe('javaHashCode', () => {
	it('matches Java String.hashCode, overflow included', () => {
		expect(javaHashCode('')).toBe(0);
		expect(javaHashCode('hello')).toBe(99162322);
		expect(javaHashCode('user123')).toBe(-147178873);
		expect(javaHashCode('550e8400-e29b-41d4-a716-446655440000')).toBe(1716781005);
	});
});

describe('chipIndex', () => {
	it('keeps the remainder of negative hashes in range, as Kotlin rem does', () => {
		// -147178873 rem 10 = -3, plus 10.
		expect(chipIndex('user123')).toBe(7);
		expect(chipIndex('hello')).toBe(2);
	});
});

describe('faceAt', () => {
	it('shows the back strictly between 90° and 270°, at any turn', () => {
		expect(faceAt(0)).toBe('front');
		expect(faceAt(90)).toBe('front');
		expect(faceAt(91)).toBe('back');
		expect(faceAt(-180)).toBe('back');
		expect(faceAt(540)).toBe('back');
		expect(faceAt(-300)).toBe('front');
	});
});

describe('releaseTarget', () => {
	const width = 400;

	it('snaps a slow drag to the nearest face', () => {
		expect(releaseTarget(80, 177, width, 400)).toBe(0);
		expect(releaseTarget(100, 222, width, 400)).toBe(180);
		expect(releaseTarget(-100, -222, width, 400)).toBe(-180);
	});

	it('turns a flick one face in its direction', () => {
		expect(releaseTarget(9, 21, width, 60)).toBe(180);
		expect(releaseTarget(-9, -21, width, 60)).toBe(-180);
	});

	it('ignores a flick shorter than 5% of the width', () => {
		expect(releaseTarget(4, 10, width, 60)).toBe(0);
	});

	it('rounds a drag that stops edge-on to the even face', () => {
		expect(nearestFace(90)).toBe(0);
		expect(nearestFace(270)).toBe(360);
	});
});

describe('tapTarget', () => {
	it('turns toward the tapped half from the nearest face', () => {
		expect(tapTarget(0, false)).toBe(180);
		expect(tapTarget(0, true)).toBe(-180);
		expect(tapTarget(170, false)).toBe(360);
	});
});

describe('crossesEdge', () => {
	it('detects crossing 90° or 270° either way', () => {
		expect(crossesEdge(80, 95)).toBe(true);
		expect(crossesEdge(95, 80)).toBe(true);
		expect(crossesEdge(260, 280)).toBe(true);
		expect(crossesEdge(100, 260)).toBe(false);
		expect(crossesEdge(-95, -80)).toBe(true);
		expect(crossesEdge(10, 10)).toBe(false);
	});
});
