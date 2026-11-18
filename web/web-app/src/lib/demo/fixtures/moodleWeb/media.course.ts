import { html, route } from '../../router.ts';
import { demoEntryId } from '../kaltura/playback.course.ts';

const SITE = 'https://elearning.unimib.it';

export const routes = [
	route(`GET ${SITE}/mod/kalvidres/view.php`, ({ url }) => {
		const cmId = Number(url.searchParams.get('id'));
		const source = encodeURIComponent(
			`https://kaf.unimib.it/browseandembed/index/media/entryid/${demoEntryId(cmId)}/showDescription/false`
		);
		return html(`<!DOCTYPE html><html lang="it"><head><title>Registrazione</title></head><body>
			<div role="main"><div class="kaltura-player-container">
				<iframe class="kaltura-player-iframe" allowfullscreen src="${SITE}/filter/kaltura/lti_launch.php?courseid=0&amp;height=285&amp;width=400&amp;withblocks=0&amp;source=${source}"></iframe>
			</div></div>
		</body></html>`);
	})
];
