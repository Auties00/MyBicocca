import { describe, expect, it } from 'vitest';
import type { PlaylistItem } from '../content.ts';
import {
	continueWatching,
	mergeProgress,
	pickRecording,
	type ProgressMap,
	type VideoProgress
} from './progress.ts';

const recording = {
	cmId: 1,
	courseId: 10,
	title: 'Lezione 1',
	sectionTitle: null,
	courseTitle: 'Algoritmi',
	entryId: null
};

function progress(cmId: number, overrides: Partial<VideoProgress> = {}): VideoProgress {
	return {
		...recording,
		cmId,
		position: 100,
		duration: 1000,
		completed: false,
		updatedAt: cmId,
		...overrides
	};
}

function item(cmId: number, completed = false): PlaylistItem {
	return {
		cmId,
		title: `Lezione ${cmId}`,
		sectionId: 1,
		sectionTitle: null,
		sectionOrdinal: 1,
		completed,
		manual: false
	};
}

describe('mergeProgress', () => {
	it('marks a recording watched at 90 % of its length', () => {
		expect(
			mergeProgress(undefined, { ...recording, position: 899, duration: 1000 }, 1).completed
		).toBe(false);
		expect(
			mergeProgress(undefined, { ...recording, position: 900, duration: 1000 }, 1).completed
		).toBe(true);
	});

	it('keeps it watched whatever is watched after, and keeps a known length', () => {
		const previous = progress(1, { completed: true, duration: 1000, entryId: '1_abc' });
		const next = mergeProgress(previous, { ...recording, position: 5, duration: 0 }, 2);
		expect(next).toMatchObject({ completed: true, duration: 1000, entryId: '1_abc', updatedAt: 2 });
	});

	it('never completes a recording of unknown length', () => {
		expect(mergeProgress(undefined, { ...recording, position: 50, duration: 0 }, 1).completed).toBe(
			false
		);
	});
});

describe('pickRecording', () => {
	const playlist = [item(1, true), item(2), item(3), item(4)];

	it('resumes the recording started last', () => {
		const map: ProgressMap = {
			2: progress(2, { updatedAt: 5 }),
			3: progress(3, { updatedAt: 9 })
		};
		expect(pickRecording(playlist, map)?.cmId).toBe(3);
	});

	it('else suggests the first neither Moodle nor this device counts as watched', () => {
		const map: ProgressMap = { 2: progress(2, { completed: true, position: 1000 }) };
		expect(pickRecording(playlist, map)?.cmId).toBe(3);
	});

	it('else the first, once all are watched', () => {
		expect(pickRecording([item(1, true), item(2, true)], {})?.cmId).toBe(1);
	});
});

describe('continueWatching', () => {
	it('picks the latest unfinished recording past 1 %, of one course or any', () => {
		const map: ProgressMap = {
			1: progress(1, { updatedAt: 3, courseId: 10 }),
			2: progress(2, { updatedAt: 8, courseId: 20 }),
			3: progress(3, { updatedAt: 9, position: 5 }),
			4: progress(4, { updatedAt: 10, completed: true })
		};
		expect(continueWatching(map)?.cmId).toBe(2);
		expect(continueWatching(map, 10)?.cmId).toBe(1);
	});
});
