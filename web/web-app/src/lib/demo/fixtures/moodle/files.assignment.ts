import { DemoStudent } from '../../persona.ts';
import { json, route } from '../../router.ts';
import { demoState } from '../../state.ts';
import { DEMO_WS_TOKEN } from '../../tokens.ts';

/**
 * Moodle's upload endpoint (`webservice/upload.php`), which puts files in the student's draft
 * areas; a submission's save then names the area. Forum attachments go through it too, so the
 * areas are exported for every feature's save.
 */

export interface DraftFile {
	name: string;
	size: number;
	mime: string;
	uploadedAt: Date;
}

/** The student's draft areas, by item id. */
export const draftAreas = () =>
	demoState('moodle.files.drafts', () => new Map<number, DraftFile[]>());

/** The student's user context, which draft files belong to. */
export const DRAFT_CONTEXT = 990_001;

/** An item id none of the student's draft areas uses, picked at random as Moodle does. */
export function unusedDraftId(): number {
	const areas = draftAreas();
	let itemId = 0;
	while (itemId === 0 || areas.has(itemId)) {
		itemId = 100_000_000 + Math.floor(Math.random() * 899_999_999);
	}
	return itemId;
}

export const routes = [
	route('POST https://elearning.unimib.it/webservice/upload.php', async (request) => {
		if (request.url.searchParams.get('token') !== DEMO_WS_TOKEN) {
			return json({ error: 'Token non valido', errorcode: 'invalidtoken' });
		}
		const form = await request.formData();
		const file = form.get('file');
		if (!(file instanceof Blob)) {
			return json({ error: 'Nessun file caricato', errorcode: 'nofile' });
		}
		const requested = Number(form.get('itemid') ?? 0);
		const areas = draftAreas();
		const itemId = requested > 0 ? requested : unusedDraftId();
		const name = file instanceof File ? file.name : String(form.get('filename') ?? 'file');
		const area = (areas.get(itemId) ?? []).filter((existing) => existing.name !== name);
		area.push({ name, size: file.size, mime: file.type, uploadedAt: new Date() });
		areas.set(itemId, area);
		return json([
			{
				component: 'user',
				contextid: DRAFT_CONTEXT,
				userid: String(DemoStudent.moodle.userId),
				filearea: 'draft',
				filename: name,
				filepath: String(form.get('filepath') ?? '/'),
				itemid: itemId,
				filesize: file.size,
				license: 'allrightsreserved',
				author: DemoStudent.fullName,
				source: ''
			}
		]);
	})
];
