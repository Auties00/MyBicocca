import type { Dictionary } from './it.ts';

export const en = {
	lang: 'en',
	meta: {
		title: 'MyBicocca: your entire university, in one place',
		description:
			'Timetable, Esse3 exams, Moodle e-learning, campus map and library seat bookings for Milano-Bicocca students. On Web, Android, and soon iOS.'
	},
	nav: {
		skip: 'Skip to content',
		features: 'Features',
		privacy: 'Privacy',
		download: 'Download',
		openWeb: 'Open the Web App',
		getAndroid: 'Download for Android',
		languageLabel: 'Select language'
	},
	hero: {
		titleStart: 'Your entire university,',
		titleAccent: 'in one place.',
		lead: 'One fast, student-built app for Milano-Bicocca. Timetable, exams, Moodle courses, campus map and library seats, unified across desktop and mobile.',
		cta: {
			web: 'Open the Web App',
			android: 'Download for Android'
		},
		altDesktop: 'MyBicocca Web App on desktop showing the lecture calendar',
		altAndroid: 'Native MyBicocca Android app showing the campus map',
		altAndroidHero: 'Native MyBicocca Android app showing the lecture calendar',
		altMobileWeb: 'MyBicocca Web App on phone showing the lecture calendar'
	},
	features: {
		platformLabel: 'Preview on',
		platforms: {
			'web-desktop': 'Web (Desktop)',
			'web-tablet': 'Web (Tablet)',
			'web-mobile': 'Web (Phone)',
			android: 'Android (Phone)',
			'android-tablet': 'Android (Tablet)',
			iphone: 'iPhone',
			ipad: 'iPad'
		},
		sections: [
			{
				id: 'calendar' as const,
				title1: 'Unified',
				title2: 'Calendar',
				lead: 'Lecture shifts, booked exams, Moodle assignment deadlines and library reservations brought together on a single clean timeline.',
				alt: 'MyBicocca unified lecture calendar',
				details: [
					{ title: 'Day, Week & Month', body: 'Switch views effortlessly with a live current-time indicator across your week.' },
					{ title: 'Shift filtering', body: 'Show only the courses and groups in your study plan without visual noise.' },
					{ title: 'Room details', body: 'Tap any lecture for building, floor, lecturer and instant map location.' }
				]
			},
			{
				id: 'elearning' as const,
				title1: 'Native',
				title2: 'E-learning',
				lead: 'All your Moodle courses without clunky webviews: slides, assignments, quizzes, forums with attachments and Kaltura lecture streams.',
				alt: 'Native Moodle e-learning in MyBicocca',
				details: [
					{ title: 'Built-in PDF & slides', body: 'Read lecture slides, announcements and attachments right inside the app.' },
					{ title: 'Assignments & quizzes', body: 'Track submission deadlines, upload status and quiz grades at a glance.' },
					{ title: 'Kaltura playback', body: 'Watch recorded lectures directly with native playback controls.' }
				]
			},
			{
				id: 'map' as const,
				title1: 'Campus',
				title2: 'Map',
				lead: 'Explore every Milano-Bicocca building on a smooth vector map and see where every classroom is and whether it is free right now.',
				alt: 'Interactive Bicocca campus map in MyBicocca',
				details: [
					{ title: 'Every building', body: 'Locate U1, U6 Agorà, U7 Civitas, U14 and every campus wing instantly.' },
					{ title: 'Live free rooms', body: 'Check real-time room occupancy via EasyBadge before walking over.' },
					{ title: 'Instant lookup', body: 'Search any building or room code and jump straight to its floor.' }
				]
			},
			{
				id: 'exams' as const,
				title1: 'Exams',
				title2: '& Services',
				lead: 'The entire Esse3 student registry cleanly organised: exam bookings, grade outcomes, tuition fees, ISEE and signed PDF certificates.',
				alt: 'Esse3 exams and student services in MyBicocca',
				details: [
					{ title: 'Calls & grades', body: 'Book exam dates, track waitlist position and accept or decline grades.' },
					{ title: 'Tuition & ISEE', body: 'Monitor paid instalments, upcoming deadlines and ISEE status.' },
					{ title: 'PDF certificates', body: 'Download signed enrolment and transcript self-certifications in one tap.' }
				]
			},
			{
				id: 'library' as const,
				title1: 'Library',
				title2: 'Seats',
				lead: 'Check live occupancy across university libraries and reserve your study seat in two taps, automatically synced with your calendar.',
				alt: 'Library seat occupancy and bookings in MyBicocca',
				details: [
					{ title: 'Central, Science & Medicine', body: 'See available study seats across every library and reading room live.' },
					{ title: 'Two-tap booking', body: 'Pick your time slot and a desk with a power outlet right in the app.' },
					{ title: 'Calendar sync', body: 'Confirmed reservations appear automatically alongside your classes.' }
				]
			},
			{
				id: 'profile' as const,
				title1: 'Career',
				title2: '& Simulator',
				lead: 'Track your degree progress with a digital student card, grade trend chart, yearly credit breakdown and live GPA simulator.',
				alt: 'Student career transcript and GPA simulator in MyBicocca',
				details: [
					{ title: 'Digital student card', body: 'Student ID, degree programme and career summary always at hand.' },
					{ title: 'Credits & trend', body: 'Follow weighted average, arithmetic average and credits toward graduation.' },
					{ title: 'GPA simulator', body: 'Preview how your next exam grade impacts your average and graduation base.' }
				]
			}
		]
	},
	privacy: {
		title1: 'Your data stays',
		title2: 'on your device.',
		lead: 'MyBicocca communicates exclusively with official university services. No central database, no data brokering, and every line of code open under AGPL-3.0.',
		diagram: {
			clientCaption: 'Your device',
			serversCaption: 'Official Milano-Bicocca servers',
			services: [
				{ name: 'Esse3' },
				{ name: 'Moodle' },
				{ name: 'EasyStaff' },
				{ name: 'Affluences' },
				{ name: 'Kaltura' }
			]
		},
		items: [
			{
				icon: 'lock' as const,
				title: 'No third party servers',
				body: 'Your keys stay encrypted on your device and are sent exclusively to the official university services they belong to. Your data never flows through any third party servers.'
			},
			{
				icon: 'shield' as const,
				title: 'Zero tracking',
				body: 'No personal telemetry, analytics cookies or hidden trackers anywhere in the web app. On Android, anonymous crash reports can be turned off in Settings.'
			},
			{
				icon: 'code' as const,
				title: '100% Open Source',
				body: 'Android app, Web App, generated SDKs and OpenAPI specs are all public on GitHub under AGPL-3.0. Anyone can inspect requests, audit the code or contribute.'
			},
			{
				icon: 'user' as const,
				title: 'Independent & student-built',
				body: 'Built independently by Milano-Bicocca students and not affiliated with the university. Zero ads, zero paid tiers and zero data brokering, made purely for student life.'
			}
		]
	},
	download: {
		titleLine1: 'Available today.',
		titleLine2: 'On desktop and mobile.',
		web: {
			name: 'Web App',
			spec: 'Desktop & mobile browsers · Installable as a desktop app',
			cta: 'Open web app'
		},
		android: {
			name: 'Android',
			spec: 'Native app for Android 7.1+',
			apk: 'GitHub APK'
		},
		ios: {
			name: 'iOS & iPadOS',
			spec: 'Native app for iOS 27+',
			soon: 'Coming soon'
		}
	},
	footer: {
		columns: {
			product: 'Product',
			platforms: 'Platforms',
			project: 'Project',
			community: 'Community'
		},
		links: {
			features: 'Features',
			privacy: 'Privacy',
			download: 'Download',
			webApp: 'Web App',
			android: 'Android',
			ios: 'iOS & iPadOS',
			source: 'Source code',
			license: 'AGPL-3.0 License',
			reportBug: 'Report a bug',
			contribute: 'Contribute'
		}
	}
} satisfies Record<keyof Dictionary, unknown>;
