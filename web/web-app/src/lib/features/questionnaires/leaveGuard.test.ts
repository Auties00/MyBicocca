import { describe, expect, it, vi } from 'vitest';
import { decideLeave, type CompileState, type LeaveAttempt } from './leaveGuard.svelte.ts';

vi.mock('$app/navigation', () => ({ beforeNavigate: () => undefined, goto: () => undefined }));

const ORIGIN = 'https://app.example';
const running: CompileState = { open: true, busy: false, canGoBack: true };
const firstPage: CompileState = { ...running, canGoBack: false };

const link = (href: string): LeaveAttempt => ({ type: 'link', to: new URL(href, ORIGIN) });
const back = (delta: number): LeaveAttempt => ({
	type: 'popstate',
	delta,
	to: new URL('/services', ORIGIN)
});

describe('decideLeave', () => {
	it('lets every navigation through once nothing would be lost', () => {
		const sent = { ...running, open: false };
		expect(decideLeave(link('/services'), sent, ORIGIN)).toEqual({ action: 'go' });
		expect(decideLeave(back(-1), sent, ORIGIN)).toEqual({ action: 'go' });
		expect(decideLeave({ type: 'leave', to: null }, sent, ORIGIN)).toEqual({ action: 'go' });
	});

	it('stays without asking while a send or a dialog is under way, and on a tab close', () => {
		const busy = { ...running, busy: true };
		expect(decideLeave(link('/services'), busy, ORIGIN)).toEqual({ action: 'stay' });
		expect(decideLeave(back(-1), busy, ORIGIN)).toEqual({ action: 'stay' });
		expect(decideLeave({ type: 'leave', to: null }, running, ORIGIN)).toEqual({ action: 'stay' });
	});

	it('turns Back into the previous page while there is one', () => {
		expect(decideLeave(back(-1), running, ORIGIN)).toEqual({ action: 'previous' });
	});

	it('asks before history leaves the questionnaire, keeping how far it was going', () => {
		expect(decideLeave(back(-1), firstPage, ORIGIN)).toEqual({
			action: 'ask',
			destination: { kind: 'history', delta: -1 }
		});
		expect(decideLeave(back(2), running, ORIGIN)).toEqual({
			action: 'ask',
			destination: { kind: 'history', delta: 2 }
		});
	});

	it('asks before a link, inside the app with its query and hash, or away from it', () => {
		expect(decideLeave(link('/calendar?view=week#today'), running, ORIGIN)).toEqual({
			action: 'ask',
			destination: { kind: 'app', href: '/calendar?view=week#today' }
		});
		expect(decideLeave(link('https://elearning.unimib.it/'), running, ORIGIN)).toEqual({
			action: 'ask',
			destination: { kind: 'away', href: 'https://elearning.unimib.it/' }
		});
	});

	it('stays when a navigation names nowhere to go', () => {
		expect(decideLeave({ type: 'goto', to: null }, running, ORIGIN)).toEqual({ action: 'stay' });
	});
});
