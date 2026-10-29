import {
	crossesEdge,
	dragRotation,
	faceAt,
	foilGradient,
	nearestFace,
	releaseTarget,
	tapTarget,
	tiltAt
} from './card.ts';

/** A damped spring toward `target` (Compose's `spring(dampingRatio, stiffness)`, unit mass). */
class Spring {
	value: number;
	target: number;
	velocity = 0;

	constructor(
		initial: number,
		private readonly stiffness: number,
		private readonly damping: number
	) {
		this.value = initial;
		this.target = initial;
	}

	/** Advances `dt` seconds; true while still moving. */
	step(dt: number): boolean {
		const friction = 2 * this.damping * Math.sqrt(this.stiffness);
		const force = this.stiffness * (this.target - this.value) - friction * this.velocity;
		this.velocity += force * dt;
		this.value += this.velocity * dt;
		if (Math.abs(this.velocity) < 0.001 && Math.abs(this.target - this.value) < 0.0005) {
			this.value = this.target;
			this.velocity = 0;
			return false;
		}
		return true;
	}

	snap(value: number): void {
		this.value = this.target = value;
		this.velocity = 0;
	}
}

/** Compose's FastOutSlowIn, cubic-bezier(0.4, 0, 0.2, 1), solved for x by bisection. */
function fastOutSlowIn(t: number): number {
	const curve = (a: number, b: number, s: number) =>
		3 * a * s * (1 - s) ** 2 + 3 * b * s ** 2 * (1 - s) + s ** 3;
	let low = 0;
	let high = 1;
	for (let i = 0; i < 20; i++) {
		const mid = (low + high) / 2;
		if (curve(0.4, 0.2, mid) < t) low = mid;
		else high = mid;
	}
	return curve(0, 1, (low + high) / 2);
}

const TURN_MS = 300;
/** Pixels a pointer moves before a press becomes a drag. */
const SLOP = 6;

export interface CardMotionOptions {
	/** The element pointer coordinates are measured against. */
	stage: HTMLElement;
	/** The element that turns. */
	card: HTMLElement;
	/** Receives `--tx`, `--ty` (pointer, 0–1, 0.5 at rest) and `--foil`. */
	surface: HTMLElement;
	onface: (face: 'front' | 'back') => void;
	/** Called with a vibration pattern at the moments Android plays a haptic. */
	onhaptic?: (pattern: number | number[]) => void;
	/** Reduced motion: faces swap without turning, the art stays still. */
	still: () => boolean;
}

/**
 * The student card's physics: horizontal drags turn it (the card's width is half a turn), a
 * flick or a drag past 90° commits, a tap turns it toward the tapped half, and the pointer (or
 * the device's tilt) drives the parallax and the foil. Only transforms and the foil's gradient
 * change, written straight to the elements, one frame at a time.
 */
export class CardMotion {
	readonly #options: CardMotionOptions;
	#rotation = 0;
	#turn: { from: number; to: number; start: number } | undefined;
	readonly #tilt = new Spring(0, 200, 0.5);
	readonly #x = new Spring(0.5, 400, 0.75);
	readonly #y = new Spring(0.5, 400, 0.75);
	#device = { x: 0, y: 0 };
	#frame = 0;
	#last = 0;
	#face: 'front' | 'back' = 'front';

	#pointer: number | undefined;
	#dragging = false;
	#downX = 0;
	#lastX = 0;
	#dragStart = 0;
	#totalDrag = 0;

	constructor(options: CardMotionOptions) {
		this.#options = options;
		this.#render();
	}

	get face(): 'front' | 'back' {
		return this.#face;
	}

	/** Turns one face forwards (or backwards), as a tap on that half does. */
	flip(backwards = false): void {
		this.#turnTo(tapTarget(this.#current(performance.now()), backwards));
		this.#options.onhaptic?.([12, 180, 6]);
	}

	pointerdown(event: PointerEvent): void {
		if (this.#pointer !== undefined || (event.pointerType === 'mouse' && event.button !== 0))
			return;
		this.#pointer = event.pointerId;
		this.#dragging = false;
		this.#downX = this.#lastX = event.clientX;
	}

	pointermove(event: PointerEvent): void {
		const { stage } = this.#options;
		const box = stage.getBoundingClientRect();
		const x = event.clientX - box.left;
		const y = event.clientY - box.top;
		if (event.pointerId !== this.#pointer) {
			// A mouse or pen hovering: the art follows it, the card stays put.
			if (event.pointerType !== 'touch') this.#follow(x / box.width, y / box.height);
			return;
		}
		if (!this.#dragging) {
			if (Math.abs(event.clientX - this.#downX) < SLOP) return;
			this.#dragging = true;
			this.#dragStart = event.timeStamp;
			this.#totalDrag = 0;
			this.#lastX = event.clientX;
			this.#turn = undefined;
			this.#rotation = this.#current(performance.now());
			stage.setPointerCapture(event.pointerId);
			this.#options.onhaptic?.(15);
		}
		const dx = event.clientX - this.#lastX;
		this.#lastX = event.clientX;
		this.#totalDrag += dx;
		const previous = this.#rotation;
		this.#rotation += dragRotation(dx, box.width);
		if (crossesEdge(previous, this.#rotation)) this.#options.onhaptic?.(6);
		this.#tilt.target = tiltAt(y, box.height);
		this.#follow(x / box.width, y / box.height);
	}

	pointerup(event: PointerEvent): void {
		if (event.pointerId !== this.#pointer) return;
		this.#pointer = undefined;
		const box = this.#options.stage.getBoundingClientRect();
		if (this.#dragging) {
			this.#dragging = false;
			const duration = event.timeStamp - this.#dragStart;
			const target = releaseTarget(this.#rotation, this.#totalDrag, box.width, duration);
			this.#turnTo(target);
			if (duration >= 100) this.#options.onhaptic?.(10);
		} else {
			this.flip(event.clientX - box.left < box.width / 2);
		}
		this.#settle(event.pointerType !== 'touch');
	}

	pointercancel(event: PointerEvent): void {
		if (event.pointerId !== this.#pointer) return;
		this.#pointer = undefined;
		if (this.#dragging) this.#turnTo(nearestFace(this.#rotation));
		this.#dragging = false;
		this.#settle(false);
	}

	pointerleave(event: PointerEvent): void {
		if (event.pointerType !== 'touch' && this.#pointer === undefined) this.#follow(0.5, 0.5);
	}

	/** Device tilt in -1…1 on each axis, added to the pointer for the parallax and the foil. */
	tilt(x: number, y: number): void {
		this.#device = { x, y };
		this.#schedule();
	}

	/** Redraws, after the card resized or the motion preference changed. */
	refresh(): void {
		this.#schedule();
	}

	destroy(): void {
		cancelAnimationFrame(this.#frame);
	}

	#follow(x: number, y: number): void {
		this.#x.target = Math.min(1, Math.max(0, x));
		this.#y.target = Math.min(1, Math.max(0, y));
		this.#schedule();
	}

	/** After a gesture the tilt and the pointer spring back, unless a hovering pointer stays. */
	#settle(keepPointer: boolean): void {
		this.#tilt.target = 0;
		if (!keepPointer) this.#follow(0.5, 0.5);
		this.#schedule();
	}

	#turnTo(target: number): void {
		const now = performance.now();
		this.#turn = { from: this.#current(now), to: target, start: now };
		this.#schedule();
	}

	#current(now: number): number {
		const turn = this.#turn;
		if (turn === undefined) return this.#rotation;
		const t = Math.min(1, (now - turn.start) / TURN_MS);
		return turn.from + (turn.to - turn.from) * fastOutSlowIn(t);
	}

	#schedule(): void {
		if (this.#frame !== 0) return;
		this.#last = performance.now();
		this.#frame = requestAnimationFrame(this.#tick);
	}

	#tick = (now: number): void => {
		this.#frame = 0;
		const dt = Math.min(0.032, Math.max(0, (now - this.#last) / 1000));
		this.#last = now;
		let moving = false;
		if (this.#turn !== undefined) {
			this.#rotation = this.#current(now);
			if (now - this.#turn.start >= TURN_MS) {
				this.#rotation = this.#turn.to;
				this.#turn = undefined;
			} else moving = true;
		}
		for (const spring of [this.#tilt, this.#x, this.#y]) {
			// Several small steps keep the stiff springs stable at low frame rates.
			for (let i = 0; i < 4; i++) moving = spring.step(dt / 4) || moving;
		}
		this.#render();
		if (moving || this.#dragging) this.#frame = requestAnimationFrame(this.#tick);
	};

	#render(): void {
		const { card, surface, stage } = this.#options;
		const still = this.#options.still();
		const face = faceAt(this.#rotation);
		if (face !== this.#face) {
			this.#face = face;
			this.#options.onface(face);
		}
		card.style.transform = still
			? ''
			: `rotateX(${this.#tilt.value.toFixed(3)}deg) rotateY(${this.#rotation.toFixed(3)}deg)`;
		const x = still ? 0.5 : this.#x.value + this.#device.x;
		const y = still ? 0.5 : this.#y.value + this.#device.y;
		surface.style.setProperty('--tx', x.toFixed(4));
		surface.style.setProperty('--ty', y.toFixed(4));
		surface.style.setProperty(
			'--foil',
			foilGradient(x, y, stage.clientWidth || 1, stage.clientHeight || 1)
		);
	}
}
