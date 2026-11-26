export const site = {
	name: 'MyBicocca',
	url: 'https://mybicocca.it',
	webApp: 'https://web.mybicocca.it',
	playStore: 'https://play.google.com/store/apps/details?id=it.attendance100.mybicocca',
	appStore: null as string | null,
	releases: 'https://github.com/Auties00/MyBicocca/releases/latest',
	repository: 'https://github.com/Auties00/MyBicocca',
	license: 'https://github.com/Auties00/MyBicocca/blob/main/LICENSE',
	authors: [
		'Alessandro Autiero',
		'Lorenzo Angelo Lupi',
		'Federico Giarrusso',
		'Alessandro Ferrari Pagini'
	],
	since: 2025
} as const;

export const webAppHost = new URL(site.webApp).host;
