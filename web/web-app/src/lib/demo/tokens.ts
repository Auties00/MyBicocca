import { basic } from '#lib/api/auth/esse3Token.ts';
import { DemoStudent } from './persona.ts';

/** The demo's credentials: any password signs in, but the servers still check them. */
export const DEMO_USERNAME = DemoStudent.username;
export const DEMO_PASSWORD = 'demo';

export const DEMO_WS_TOKEN = 'd3e40000d3e40000d3e40000d3e40000';
export const DEMO_PRIVATE_TOKEN = 'demoPrivateToken';
export const DEMO_MOODLE_SESSION = 'demo-moodle-session';

/** Esse3's token lifetime, kept so the demo rolls its tokens as live sessions do. */
const LIFETIME_S = 300;

const base64url = (value: string) =>
	btoa(value).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

/** A token shaped like Esse3's: the client reads its lifetime from `iat` and `exp`. */
export function mintDemoJwt(): string {
	const iat = Math.floor(Date.now() / 1000);
	const header = base64url(JSON.stringify({ alg: 'none', typ: 'JWT' }));
	const claims = base64url(
		JSON.stringify({ sub: DemoStudent.recordUserId, iat, exp: iat + LIFETIME_S })
	);
	return `${header}.${claims}.demo`;
}

/** Whether `authorization` is what Esse3 would accept from the demo student. */
export function esse3Accepts(authorization: string | null, allowBasic: boolean): boolean {
	if (authorization === null) return false;
	if (authorization.startsWith('Basic ')) {
		return (
			allowBasic && authorization === basic({ username: DEMO_USERNAME, password: DEMO_PASSWORD })
		);
	}
	const token = authorization.startsWith('Bearer ') ? authorization.slice('Bearer '.length) : '';
	try {
		const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
		const claims = JSON.parse(atob(part)) as { exp?: unknown; sub?: unknown };
		return (
			claims.sub === DemoStudent.recordUserId &&
			typeof claims.exp === 'number' &&
			claims.exp * 1000 > Date.now()
		);
	} catch {
		return false;
	}
}
