import bbetween from '../assets/bbetween.svg';
import bicoccaAcademy from '../assets/bicocca-academy.svg';
import formazioneInsegnanti from '../assets/formazione-insegnanti.webp';
import formazioneTutor from '../assets/formazione-tutor.webp';
import idoneitaLinguistica from '../assets/idoneita-linguistica.webp';
import scuolaDottorato from '../assets/scuola-dottorato.svg';
import scuoleSpecializzazione from '../assets/scuole-specializzazione.webp';

/**
 * How a catalog area looks: the teaching areas wear the university's official area colours with
 * its logo on a white disc; the schools and other spaces a deep plate of their own hue with their
 * artwork on the disc. Every level below an area keeps its accent, so the browse always says
 * where it is.
 */
export type AreaVisual =
	| { kind: 'brand'; color: string; ink: 'light' | 'dark' }
	| { kind: 'artwork'; src: string; fit: 'cover' | 'contain' }
	| { kind: 'plain' };

/**
 * Lowercased name fragments, the more specific first ("di scienze" also ends "della formazione"),
 * with each colour's OKLCH hue and chroma, which tint the levels below.
 */
const BRANDS: readonly (readonly [string, string, number, number])[] = [
	['economico-statistica', '#f3c513', 91, 0.17],
	['giuridica', '#0b4e91', 254, 0.13],
	['medica', '#e53728', 30, 0.2],
	['psicologica', '#982069', 349, 0.17],
	['scienze della formazione', '#ce2992', 347, 0.2],
	['di scienze', '#0d733c', 153, 0.12],
	['sociologica', '#f29420', 64, 0.16],
	['altre attività formative', '#b9bbbd', 248, 0.01]
];

/** Artwork by name fragment, each with the hue and chroma its plate and levels take from it. */
const ARTWORK: readonly (readonly [string, Extract<AreaVisual, { kind: 'artwork' }>, AreaTint])[] =
	[
		['academy', { kind: 'artwork', src: bicoccaAcademy, fit: 'contain' }, { hue: 8, chroma: 0.13 }],
		[
			'dottorato',
			{ kind: 'artwork', src: scuolaDottorato, fit: 'contain' },
			{ hue: 238, chroma: 0.1 }
		],
		[
			'formazione insegnanti',
			{ kind: 'artwork', src: formazioneInsegnanti, fit: 'cover' },
			{ hue: 292, chroma: 0.12 }
		],
		[
			'specializzazione',
			{ kind: 'artwork', src: scuoleSpecializzazione, fit: 'cover' },
			{ hue: 205, chroma: 0.08 }
		],
		['bbetween', { kind: 'artwork', src: bbetween, fit: 'contain' }, { hue: 352, chroma: 0.11 }],
		[
			'formazione tutor',
			{ kind: 'artwork', src: formazioneTutor, fit: 'cover' },
			{ hue: 58, chroma: 0.09 }
		],
		[
			'idoneit',
			{ kind: 'artwork', src: idoneitaLinguistica, fit: 'cover' },
			{ hue: 250, chroma: 0.04 }
		]
	];

export function areaVisual(name: string): AreaVisual {
	const lower = name.toLowerCase();
	const brand = BRANDS.find(([fragment]) => lower.includes(fragment));
	if (brand) return { kind: 'brand', color: brand[1], ink: inkOn(brand[1]) };
	return artworkOf(lower)?.[1] ?? { kind: 'plain' };
}

/**
 * White where it reads as large text (3:1), dark ink otherwise: white on the yellow, orange and
 * grey areas would fade.
 */
function inkOn(hex: string): 'light' | 'dark' {
	const channel = (offset: number) => {
		const value = parseInt(hex.slice(offset, offset + 2), 16) / 255;
		return value <= 0.04045 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
	};
	const luminance = 0.2126 * channel(1) + 0.7152 * channel(3) + 0.0722 * channel(5);
	return 1.05 / (luminance + 0.05) >= 3 ? 'light' : 'dark';
}

/** An area's tint for the levels below it: hue and chroma in OKLCH. */
export interface AreaTint {
	hue: number;
	chroma: number;
}

/** Android's muted area palette (violet, teal, wine, green, ochre, navy, plum), for the rest. */
const MUTED: readonly AreaTint[] = [
	{ hue: 301, chroma: 0.15 },
	{ hue: 224, chroma: 0.07 },
	{ hue: 18, chroma: 0.11 },
	{ hue: 160, chroma: 0.08 },
	{ hue: 64, chroma: 0.09 },
	{ hue: 262, chroma: 0.1 },
	{ hue: 348, chroma: 0.13 }
];

export function areaTint(name: string): AreaTint {
	const lower = name.toLowerCase();
	const brand = BRANDS.find(([fragment]) => lower.includes(fragment));
	if (brand) return { hue: brand[2], chroma: brand[3] };
	return artworkOf(lower)?.[2] ?? MUTED[javaHash(name) % MUTED.length];
}

function artworkOf(lower: string) {
	return ARTWORK.find(([fragment]) => lower.includes(fragment));
}

/** Java's `String.hashCode`, made non-negative, as Android picks the muted accent. */
function javaHash(text: string): number {
	let value = 0;
	for (let i = 0; i < text.length; i++) value = (Math.imul(31, value) + text.charCodeAt(i)) | 0;
	return Math.abs(value);
}
