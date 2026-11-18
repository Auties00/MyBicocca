import { decodeMoodleWebCatalog, type MoodleWebCatalog } from '@mybicocca/data-api/elearning';
import { DecodeError } from '@mybicocca/data-api/runtime';
import catalogUrl from '@mybicocca/data-api/elearning/resources/moodle-web/catalog.json?url';

/**
 * The SDK's bundled catalog (`MoodleWebResources.catalog()`'s resource), fetched as the plain
 * JSON file it is and decoded with the SDK's own decoder. As a file it ships once under a hashed
 * name, compressed and cached for good, and parses natively; and the SDK's dynamic import with
 * `with { type: 'json' }` fails under the dev server, which serves JSON as a JavaScript module.
 */
export async function loadCatalog(): Promise<MoodleWebCatalog> {
	const response = await fetch(catalogUrl);
	if (!response.ok) throw new Error(`The catalog answered ${response.status}`);
	const catalog = decodeMoodleWebCatalog(await response.json());
	if (catalog === undefined) throw new DecodeError("The SDK's resource catalog doesn't decode");
	return catalog;
}
