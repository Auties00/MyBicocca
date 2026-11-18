import { describe, expect, it } from 'vitest';
import { decodeQr } from './decode.ts';
import { project, quadToQuad } from './grid.ts';
import { BitMatrix, type Point } from './matrix.ts';
import { readSymbol } from './read.ts';

/*
 * Symbols drawn by node-qrcode 1.5.4, an independent encoder: modules row by row, eight to a
 * byte, the first in the high bit.
 */
const SYMBOLS = {
	numeric: {
		text: '01234567',
		size: 21,
		modules: '/jv8FxBumLt0tdutrsEJB/qv4AAAqiiWhaiG7d2auyJ3cIBRC/giMERLuurt0quutcsEO4/pcoA='
	},
	alphanumeric: {
		text: 'HELLO WORLD',
		size: 21,
		modules: '/hP8FlBulrt19dutLsElB/qv4BsAXs7V6HuKxMFosY3+74BEo/mZ8FSXutI91cUukIcFzm/lAQA='
	},
	session: {
		text: 'https://elearning.unimib.it/mod/attendance/attendance.php?qrpass=x7Kp2q&sessid=184467',
		size: 37,
		modules:
			'/i8ZC/wVPVDQboNNKrt11uXF26QY0i7BbDRZB/qqqq/gDlfDAPvQSvVVhXipa525ya+6s9oOH6ngt55ujLDchBhp4Sj24wJ1hymuEdrm6210ukm2wds+LgPaKB8dMqymb7qQHICJfWAy1GmCYoPRMjDKfv/FePhJo7HbBjdRGglvGyi8Dv4AUI2sQ/rniapwTnDhErrwy//11XP89m65yRJHBRssdR/otK8DgA=='
	},
	lesson: {
		text: 'https://easystaff.divsi.unimib.it/PortaleStudenti/presenze.php?codice_lezione=AB12CD',
		size: 37,
		modules:
			'/uz9A/wWKV5QboKKELt19ibl26Ynj67BIlsRB/qqqq/gH1xUALcq1SpYRkjJaLuSkdAhErwVusCl/axv/BZIH+9qmOMi1j/w0zMfjBpJYe4+bJP455Mc5tqA1XKuNv5TWI4hTKYqSDE+pA87QSuchwRy9ruPP6TX3NJUOAUbq285A2TD/P6ASZPsX/tmmirwU4cXEboGvA/F1BnLNG65OGqhBLwzw8/sZDZPgA=='
	},
	accented: {
		text: 'Presenza già registrata',
		size: 29,
		modules:
			'/pH7/BXpEG6u8rt0xgXbodOuwVOVB/qqr+AYEwA6pK8/Bc/cZ/OS4DKejKN3AAd4xz3NmMaW0oIcekJfIDWChtwsmdnJCnQvq/es+4B5VG/5qOtwSVEZuqiP9dUEgi6+yfUEvbKv4DtKAA=='
	},
	version8: {
		text: 'MYBICOCCA 2026 https://elearning.unimib.it/course/view.php?id=4321&section=7#module-998877',
		size: 49,
		modules:
			'/ic5Cqi/wVTKnFHQboT43Dlrt1fOrPul26q3PqlC7BIlMeSRB/qqqqqq/gGULEUwAF7Xq+UpbUyrFPvd+in6d5w/Fa2EWXe6v3zo2eP78J7kvsNc33EIuQl2and4UCAoqUtTZvKd+D1avA7oq30G7GVzv6n/Pe8n6kshPfZlp/fu4rRAcx0H/oB/p3v7kcI0c6MVaqCKvMqs9HudG6HE0/QI/jS/N7Z9Xc29f3piOrhS/++eghHhy6pWXYdm27NMdM+XCotQaGLVeTZMJBMxlpqT06tJHCi4aqlyqmuzvHOavAKsGWrUV8EadtC2W87hSzhEZBfjmffjn/gAbHEf0Mc/nQ+s7uuwV+RFztEbrYh/p9/t1O57udYm6OjphbrvBWClCeBW/m6t64xFgA=='
	},
	kanji: {
		text: '点茗',
		size: 21,
		modules: '/nv8ERBulrt09duuLsEpB/qv4BAAM4aGTUra8/x6iGH4KYB3s/sykElBumRN16Suo6kEnZ/lsYA='
	}
};

type SymbolName = keyof typeof SYMBOLS;

function modulesOf(name: SymbolName): BitMatrix {
	const { size, modules } = SYMBOLS[name];
	const bytes = Uint8Array.from(atob(modules), (char) => char.charCodeAt(0));
	const matrix = new BitMatrix(size);
	for (let i = 0; i < size * size; i++) {
		if (bytes[i >> 3] & (0x80 >> (i & 7))) matrix.set(i % size, Math.floor(i / size));
	}
	return matrix;
}

interface Shot {
	width: number;
	height: number;
	/** Where the symbol's corners land: top-left, top-right, bottom-right, bottom-left. */
	corners: Point[];
	ink?: number;
	paper?: number;
	/** How much the light dims from the left edge to the right one, 0–1. */
	falloff?: number;
	/** Peak-to-peak sensor noise, in gray levels. */
	noise?: number;
}

/** A grayscale photograph of the symbol, antialiased, with a quiet zone of paper all around. */
function photograph(name: SymbolName, shot: Shot): Uint8ClampedArray {
	const { width, height, corners, ink = 30, paper = 235, falloff = 0, noise = 0 } = shot;
	const modules = modulesOf(name);
	const size = modules.width;
	const toModules = quadToQuad(corners, [
		{ x: 0, y: 0 },
		{ x: size, y: 0 },
		{ x: size, y: size },
		{ x: 0, y: size }
	]);
	let seed = 7;
	const random = () => (seed = (seed * 16807) % 2147483647) / 2147483647;
	const rgba = new Uint8ClampedArray(width * height * 4);
	for (let y = 0; y < height; y++) {
		for (let x = 0; x < width; x++) {
			let dark = 0;
			for (const [dx, dy] of [
				[0.25, 0.25],
				[0.75, 0.25],
				[0.25, 0.75],
				[0.75, 0.75]
			]) {
				const at = project(toModules, x + dx, y + dy);
				if (modules.get(Math.floor(at.x), Math.floor(at.y))) dark++;
			}
			const light = paper * (1 - (falloff * x) / width);
			const value = light + ((ink - light) * dark) / 4 + (random() - 0.5) * noise;
			const p = (y * width + x) * 4;
			rgba[p] = rgba[p + 1] = rgba[p + 2] = value;
			rgba[p + 3] = 255;
		}
	}
	return rgba;
}

/** A square's corners turned by `degrees` about its centre, the far corner pulled in by `pinch`. */
function tilted(cx: number, cy: number, side: number, degrees: number, pinch = 0): Point[] {
	const angle = (degrees * Math.PI) / 180;
	return [
		[-1, -1],
		[1, -1],
		[1, 1],
		[-1, 1]
	].map(([u, v], i) => {
		const scale = (side / 2) * (i === 2 ? 1 - pinch : 1);
		return {
			x: cx + scale * (u * Math.cos(angle) - v * Math.sin(angle)),
			y: cy + scale * (u * Math.sin(angle) + v * Math.cos(angle))
		};
	});
}

describe('readSymbol', () => {
	it.each(Object.keys(SYMBOLS) as SymbolName[])('reads the %s symbol', (name) => {
		expect(readSymbol(modulesOf(name))).toBe(SYMBOLS[name].text);
	});

	it('repairs scattered damage through error correction', () => {
		const modules = modulesOf('session');
		for (const [x, y] of [
			[12, 12],
			[20, 15],
			[14, 25],
			[30, 30],
			[25, 18],
			[33, 22],
			[10, 20],
			[18, 33]
		]) {
			modules.set(x, y, !modules.get(x, y));
		}
		expect(readSymbol(modules)).toBe(SYMBOLS.session.text);
	});

	it('reads the format from its second copy when the first is smudged', () => {
		const modules = modulesOf('lesson');
		for (const x of [0, 2, 4, 7]) modules.set(x, 8, !modules.get(x, 8));
		expect(readSymbol(modules)).toBe(SYMBOLS.lesson.text);
	});

	it('refuses a symbol damaged past repair instead of misreading it', () => {
		const modules = modulesOf('session');
		for (let y = 10; y < 30; y++)
			for (let x = 10; x < 30; x++) modules.set(x, y, (x * y) % 3 === 0);
		expect(readSymbol(modules)).toBeUndefined();
	});
});

describe('decodeQr', () => {
	it('reads a code straight on', () => {
		const shot = { width: 420, height: 420, corners: tilted(210, 210, 296, 0) };
		expect(decodeQr(photograph('session', shot), 420, 420)).toBe(SYMBOLS.session.text);
	});

	it('reads a small code off-centre, three pixels a module', () => {
		const shot = { width: 320, height: 240, corners: tilted(200, 110, 111, 0) };
		expect(decodeQr(photograph('session', shot), 320, 240)).toBe(SYMBOLS.session.text);
	});

	it('reads a code turned and seen at an angle', () => {
		const shot = { width: 480, height: 400, corners: tilted(240, 200, 260, 33, 0.12) };
		expect(decodeQr(photograph('lesson', shot), 480, 400)).toBe(SYMBOLS.lesson.text);
	});

	it('reads a code upside down, finding its alignment pattern and version word', () => {
		const shot = { width: 480, height: 480, corners: tilted(240, 240, 330, 190, 0.06) };
		expect(decodeQr(photograph('version8', shot), 480, 480)).toBe(SYMBOLS.version8.text);
	});

	it('reads a code in uneven light, through sensor noise', () => {
		const shot = {
			width: 400,
			height: 360,
			corners: tilted(200, 180, 240, -8),
			ink: 60,
			paper: 220,
			falloff: 0.55,
			noise: 40
		};
		expect(decodeQr(photograph('accented', shot), 400, 360)).toBe(SYMBOLS.accented.text);
	});

	it('finds nothing in a frame without a code', () => {
		const shot = { width: 200, height: 200, corners: tilted(-500, -500, 10, 0), noise: 60 };
		expect(decodeQr(photograph('numeric', shot), 200, 200)).toBeUndefined();
	});
});
