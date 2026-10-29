const BLOCK = /\{\s*mlang\s+([a-z0-9_-]+(?:\s*,\s*[a-z0-9_-]+)*)\s*\}(.*?)\{\s*mlang\s*\}/gis;

const normalize = (code: string) => code.trim().toLowerCase().replaceAll('_', '-');

/**
 * Resolves multilang2 `{mlang}` blocks in Moodle names, which arrive unresolved whatever the
 * web service settings (as in the Moodle app's client-side filter): the blocks in the wanted
 * language stay, the others go, text outside blocks is kept. Wanted, in order: `lang`, the
 * `other` block, English, then the first block's language, so a name never comes out empty. A
 * block tagged `en` also serves `en-us`.
 */
export function stripMlang(text: string, lang: string): string {
	if (!/mlang/i.test(text)) return text;
	const first = new RegExp(BLOCK.source, 'is').exec(text);
	if (!first) return text;
	const firstLanguage = normalize(first[1].split(',')[0]);
	for (const wanted of new Set([normalize(lang), 'other', 'en', firstLanguage])) {
		let matched = false;
		const result = text.replace(BLOCK, (_, languages: string, body: string) => {
			const hit = languages
				.split(',')
				.map(normalize)
				.some((code) => code === wanted || wanted.startsWith(`${code}-`));
			if (hit) matched = true;
			return hit ? body : '';
		});
		if (matched) return result.trim();
	}
	return text;
}
