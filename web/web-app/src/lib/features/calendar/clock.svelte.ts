import { now } from '#lib/data/clock.svelte.ts';
import { romeWallClock, type WallClock } from '#lib/format/rome.ts';

let at: Date | undefined;
let wall: WallClock;

/** Today and the minute of the day in Rome, updated every minute: the now-line, "in progress". */
export const clock = {
	get now(): WallClock {
		const current = now.current;
		if (current !== at) {
			at = current;
			wall = romeWallClock(current.getTime());
		}
		return wall;
	}
};
