import { json, moodleError, route } from '../../router.ts';
import { demoRegister, type DemoRegister, type DemoSession } from '../moodleWeb/attendance.ts';

/**
 * `tool_mobile_get_content`, the site-plugin bridge of the Moodle app, for mod_attendance: the
 * take form of a session (`mobile_user_form`, which only reads) and the activity view rendered
 * after marking (`mobile_view_activity`, which records the presence). The templates carry the
 * Ionic markup the SDK scrapes.
 */

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';

/** `args[0][name]=cmid&args[0][value]=…` as the named values they encode. */
function namedArgs(form: URLSearchParams): Map<string, string> {
	const names = new Map<string, string>();
	const values = new Map<string, string>();
	for (const [key, value] of form) {
		const match = /^args\[(\d+)\]\[(name|value)\]$/.exec(key);
		if (match) (match[2] === 'name' ? names : values).set(match[1], value);
	}
	return new Map([...names].map(([index, name]) => [name, values.get(index) ?? '']));
}

function content(html: string) {
	return json({
		templates: [{ id: 'main', html }],
		javascript: '',
		otherdata: [],
		files: [],
		restrict: { users: [], courses: [] },
		disabled: false
	});
}

const message = (key: string) =>
	`<ion-item><ion-label><span class="messages">{{ 'plugin.mod_attendance.${key}' | translate }}</span></ion-label></ion-item>`;

function userForm(register: DemoRegister, session: DemoSession | undefined): string {
	if (session === undefined || session.marked)
		return `<ion-list>${message('sessionclosed')}</ion-list>`;
	const password = session.password
		? `<ion-item><ion-input type="text" name="studentpass" [(ngModel)]="CONTENT_OTHERDATA.studentpass" label="{{ 'plugin.mod_attendance.password' | translate }}"></ion-input></ion-item>`
		: '';
	return `<ion-list><ion-item><ion-label><h2>${register.name}</h2></ion-label></ion-item>
		<ion-radio-group [(ngModel)]="CONTENT_OTHERDATA.status">
			<ion-item><ion-radio value="P">Presente</ion-radio></ion-item>
		</ion-radio-group>${password}</ion-list>`;
}

function markButton(register: DemoRegister, session: DemoSession): string {
	return `<ion-button expand="block" core-site-plugins-new-content component="mod_attendance" method="mobile_user_form" [args]="{cmid: ${register.cmId}, courseid: ${register.course.moodleCourseId}, sessid: ${session.id}}">{{ 'plugin.mod_attendance.submitattendance' | translate }}</ion-button>`;
}

/** Marks the session when it is open and the password matches; the view tells how it went. */
function viewActivity(register: DemoRegister, session: DemoSession | undefined, password: string) {
	const open = register.sessions.filter((item) => !item.marked && item !== session);
	const others = open.map((item) => `<ion-item>${markButton(register, item)}</ion-item>`).join('');
	if (session === undefined || session.marked) return `<ion-list>${others}</ion-list>`;
	if (session.password !== null && session.password !== password) {
		return `<ion-list>${message('incorrectpasswordshort')}<ion-item>${markButton(register, session)}</ion-item>${others}</ion-list>`;
	}
	session.marked = true;
	register.attended += 1;
	register.taken += 1;
	return `<ion-list>${others}<ion-item><ion-label><h2>Oggi</h2><h3>Tutti gli studenti</h3><h3>Presente</h3></ion-label></ion-item></ion-list>`;
}

export const routes = [
	route(`POST ${SERVER}?wsfunction=tool_mobile_get_content`, async (request) => {
		const form = await request.form();
		if (form.get('component') !== 'mod_attendance') {
			return moodleError('invalidparameter', 'Valore di parametro non valido');
		}
		const args = namedArgs(form);
		const register = demoRegister(args.get('cmid'));
		if (register === undefined) {
			return moodleError('invalidcoursemodule', 'Modulo del corso non valido');
		}
		const session = register.sessions.find((item) => item.id === args.get('sessid'));
		switch (form.get('method')) {
			case 'mobile_user_form':
				return content(userForm(register, session));
			case 'mobile_view_activity':
				return content(viewActivity(register, session, args.get('studentpass') ?? ''));
			default:
				return moodleError('invalidparameter', 'Valore di parametro non valido');
		}
	})
];
