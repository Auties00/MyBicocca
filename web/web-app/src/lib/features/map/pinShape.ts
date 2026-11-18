/** A pin's outline: a round head whose sides run tangentially into the tip at the bottom. */
export interface PinShape {
	width: number;
	height: number;
	/** Centre of the head, where the label sits. */
	headX: number;
	headY: number;
	/** Where the tip is across the box: the point of the box that stands on the building. */
	tipX: number;
	path: string;
}

/**
 * The pin outline for a head of `head` px and a tail of `tail` px, inside `pad` px of margin for
 * the border. The tip, at the bottom, is the building's point; `lean` moves the head that many px
 * sideways, the tail slanting back to the tip, so crowded pins can stand apart.
 */
export function pinShape(head: number, tail: number, lean = 0, pad = 3): PinShape {
	const r = head / 2;
	const left = Math.min(-r, -lean) - pad;
	const right = Math.max(r, -lean) + pad;
	const headX = -left;
	const headY = pad + r;
	const tipX = headX - lean;
	const tipY = pad + head + tail;
	const toTip = Math.atan2(tipY - headY, tipX - headX);
	const theta = Math.acos(r / Math.hypot(tipX - headX, tipY - headY));
	const point = (angle: number) =>
		`${(headX + r * Math.cos(angle)).toFixed(2)} ${(headY + r * Math.sin(angle)).toFixed(2)}`;
	return {
		width: right - left,
		height: tipY + pad,
		headX,
		headY,
		tipX,
		path: `M${tipX} ${tipY} L${point(toTip + theta)} A${r} ${r} 0 1 1 ${point(toTip - theta)} Z`
	};
}

export interface Head {
	/** Centre of the head on screen, before any lean. */
	x: number;
	y: number;
	r: number;
}

/** How far a head moves from where it stands: sideways, and up (negative `dy`) on a longer tail. */
export interface Offset {
	dx: number;
	dy: number;
}

/**
 * Moves overlapping heads apart, `gap` px at least, each by half the overlap along the line
 * between them. Heads only rise, never sink below their pins' tips, and move at most `max` px
 * either way. Heads clear of all others stay put; the order of the input breaks ties.
 */
export function spreadHeads(heads: readonly Head[], gap = 2, max = 48): Offset[] {
	const moved = heads.map(() => ({ dx: 0, dy: 0 }));
	for (let round = 0; round < 16; round++) {
		let changed = false;
		for (let i = 0; i < heads.length; i++) {
			for (let j = i + 1; j < heads.length; j++) {
				const a = moved[i];
				const b = moved[j];
				const reach = heads[i].r + heads[j].r + gap;
				let x = heads[j].x + b.dx - (heads[i].x + a.dx);
				let y = heads[j].y + b.dy - (heads[i].y + a.dy);
				const distance = Math.hypot(x, y);
				if (distance >= reach - 0.5) continue;
				if (distance === 0) [x, y] = [heads[i].x <= heads[j].x ? 1 : -1, 0];
				const length = Math.hypot(x, y);
				const push = (reach - distance) / 2;
				const ux = x / length;
				const uy = y / length;
				a.dx = clamp(a.dx - ux * push, -max, max);
				a.dy = clamp(a.dy - uy * push, -max, 0);
				b.dx = clamp(b.dx + ux * push, -max, max);
				b.dy = clamp(b.dy + uy * push, -max, 0);
				changed = true;
			}
		}
		if (!changed) break;
	}
	return moved.map(({ dx, dy }) => ({ dx: Math.round(dx), dy: Math.round(dy) }));
}

function clamp(value: number, min: number, max: number): number {
	return Math.max(min, Math.min(max, value));
}
