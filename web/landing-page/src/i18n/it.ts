export const it = {
	lang: 'it',
	meta: {
		title: 'MyBicocca: tutta la tua università, in un solo posto',
		description:
			'Orario lezioni, esami Esse3, e-learning Moodle, mappa del campus e posti in biblioteca per gli studenti di Milano-Bicocca. Su Web, Android e presto iOS.'
	},
	nav: {
		skip: 'Vai al contenuto',
		features: 'Funzionalità',
		privacy: 'Privacy',
		download: 'Scarica',
		openWeb: 'Apri la Web App',
		getAndroid: 'Scarica per Android',
		languageLabel: 'Seleziona lingua'
	},
	hero: {
		titleStart: 'Tutta la tua università,',
		titleAccent: 'in un solo posto.',
		lead: 'Un’unica app veloce per Milano-Bicocca, creata da studenti. Orario, esami, corsi Moodle, mappa del campus e biblioteche, su computer e telefono.',
		cta: {
			web: 'Apri la Web App',
			android: 'Scarica per Android'
		},
		altDesktop: 'MyBicocca Web App su computer con il calendario delle lezioni',
		altAndroid: 'App nativa MyBicocca per Android con la mappa del campus',
		altAndroidHero: 'App nativa MyBicocca per Android con il calendario delle lezioni',
		altMobileWeb: 'MyBicocca Web App su smartphone con il calendario delle lezioni'
	},
	features: {
		platformLabel: 'Visualizza su',
		platforms: {
			'web-desktop': 'Web (Desktop)',
			'web-tablet': 'Web (Tablet)',
			'web-mobile': 'Web (Telefono)',
			android: 'Android (Telefono)',
			'android-tablet': 'Android (Tablet)',
			iphone: 'iPhone',
			ipad: 'iPad'
		},
		sections: [
			{
				id: 'calendar' as const,
				title1: 'Calendario',
				title2: 'unificato',
				lead: 'Orario delle lezioni per turno, appelli prenotati, scadenze Moodle e posti in biblioteca raccolti su una sola timeline chiara e immediata.',
				alt: 'Calendario unificato di MyBicocca',
				details: [
					{ title: 'Giorno, Settimana e Mese', body: 'Passa da una vista all’altra in un clic con indicatore in tempo reale dell’ora corrente.' },
					{ title: 'Filtri per turno', body: 'Mostra solo i corsi e i turni del tuo piano di studi, senza sovrapposizioni inutili.' },
					{ title: 'Dettaglio aula', body: 'Tocca qualsiasi lezione per vedere edificio, piano, docente e posizione sulla mappa.' }
				]
			},
			{
				id: 'elearning' as const,
				title1: 'E-learning',
				title2: 'nativo',
				lead: 'Tutti i tuoi corsi Moodle senza webview lente: slide, consegne, quiz, forum con allegati e registrazioni Kaltura direttamente dentro l’app.',
				alt: 'Sezione E-learning Moodle in MyBicocca',
				details: [
					{ title: 'Slide e PDF integrati', body: 'Apri materiali didattici, avvisi dei docenti e allegati senza scaricare decine di file.' },
					{ title: 'Consegne e quiz', body: 'Controlla scadenze, stato delle consegne e punteggi dei quiz a colpo d’occhio.' },
					{ title: 'Video Kaltura', body: 'Riproduci le registrazioni delle lezioni con player nativo e controlli rapidi.' }
				]
			},
			{
				id: 'map' as const,
				title1: 'Mappa',
				title2: 'del campus',
				lead: 'Esplora tutti gli edifici di Milano-Bicocca su una mappa vettoriale fluida e scopri in un attimo dove si trova ogni aula e quando è libera.',
				alt: 'Mappa interattiva del campus Bicocca in MyBicocca',
				details: [
					{ title: 'Tutti gli edifici', body: 'Trova rapidamente U1, U6 Agorà, U7 Civitas, U14 e tutte le sedi del campus.' },
					{ title: 'Aule libere live', body: 'Verifica l’occupazione in tempo reale tramite EasyBadge prima di spostarti.' },
					{ title: 'Ricerca istantanea', body: 'Cerca un edificio o una sigla d’aula e centra subito la mappa sul punto esatto.' }
				]
			},
			{
				id: 'exams' as const,
				title1: 'Esami',
				title2: 'e segreteria',
				lead: 'L’intera segreteria Esse3 organizzata in modo pulito: prenotazione appelli, esiti, tasse, ISEE e certificati firmati senza menu nascosti.',
				alt: 'Sezione Esami e Segreteria Esse3 in MyBicocca',
				details: [
					{ title: 'Appelli e voti', body: 'Prenota gli esami, controlla la posizione in lista e accetta o rifiuta i voti.' },
					{ title: 'Tasse e scadenze', body: 'Visualizza rate pagate, importi in scadenza e stato della dichiarazione ISEE.' },
					{ title: 'Certificati PDF', body: 'Scarica autocertificazioni di iscrizione ed esami sostenuti pronti all’uso.' }
				]
			},
			{
				id: 'library' as const,
				title1: 'Posti',
				title2: 'in biblioteca',
				lead: 'Controlla l’affluenza in tempo reale nelle biblioteche di ateneo e prenota il tuo posto studio in due tocchi, sincronizzato nel calendario.',
				alt: 'Prenotazione posti in biblioteca in MyBicocca',
				details: [
					{ title: 'Centrale, Scienze e Medicina', body: 'Consulta subito i posti disponibili in ogni sede e sala studio.' },
					{ title: 'Prenotazione rapida', body: 'Scegli fascia oraria e postazione con presa elettrica direttamente dall’app.' },
					{ title: 'Nel tuo calendario', body: 'Ogni prenotazione confermata compare automaticamente accanto alle tue lezioni.' }
				]
			},
			{
				id: 'profile' as const,
				title1: 'Libretto',
				title2: 'e simulatore',
				lead: 'Tieni sotto controllo il percorso universitario con tesserino digitale, grafico dei voti, crediti maturati per anno e simulatore di media.',
				alt: 'Libretto universitario e simulatore di media in MyBicocca',
				details: [
					{ title: 'Tesserino digitale', body: 'Matricola, corso di laurea e riepilogo carriera sempre a portata di mano.' },
					{ title: 'Andamento e CFU', body: 'Monitora media ponderata, media aritmetica e progressi verso la laurea.' },
					{ title: 'Simulatore di media', body: 'Calcola subito come cambia la tua media e la base di laurea con il prossimo voto.' }
				]
			}
		]
	},
	privacy: {
		title1: 'I tuoi dati restano',
		title2: 'sul tuo dispositivo.',
		lead: 'MyBicocca dialoga esclusivamente con i servizi ufficiali dell’ateneo. Nessun database centrale, nessuna rivendita di dati e codice sorgente pubblico sotto licenza AGPL-3.0.',
		diagram: {
			clientCaption: 'Il tuo dispositivo',
			serversCaption: 'Server ufficiali Milano-Bicocca',
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
				title: 'Nessun server di terze parti',
				body: 'Le tue chiavi restano cifrate sul dispositivo e vengono inviate solo ai servizi ufficiali a cui appartengono. I tuoi dati non passano mai da server di terze parti.'
			},
			{
				icon: 'shield' as const,
				title: 'Zero tracking',
				body: 'Nessuna telemetria personale, nessun cookie di profilazione e nessun tracker nella web app. Su Android i report di crash anonimi sono disattivabili dalle impostazioni.'
			},
			{
				icon: 'code' as const,
				title: '100% Open Source',
				body: 'App Android, Web App, SDK generati e specifiche OpenAPI sono pubblici su GitHub sotto licenza AGPL-3.0. Chiunque può verificare il codice, leggerlo o contribuire.'
			},
			{
				icon: 'user' as const,
				title: 'Indipendente e studentesca',
				body: 'Creata in modo indipendente da studenti della Bicocca e non affiliata all’ateneo. Nessuna pubblicità, nessun piano a pagamento e nessuna rivendita dei tuoi dati.'
			}
		]
	},
	download: {
		titleLine1: 'Pronta oggi.',
		titleLine2: 'Sul computer e sul telefono.',
		web: {
			name: 'Web App',
			spec: 'Desktop e browser mobile · Installabile come app su computer',
			cta: 'Apri la web app'
		},
		android: {
			name: 'Android',
			spec: 'App nativa per Android 7.1+',
			apk: 'APK GitHub'
		},
		ios: {
			name: 'iOS e iPadOS',
			spec: 'App nativa per iOS 27+',
			soon: 'In arrivo'
		}
	},
	footer: {
		columns: {
			product: 'Prodotto',
			platforms: 'Piattaforme',
			project: 'Progetto',
			community: 'Community'
		},
		links: {
			features: 'Funzionalità',
			privacy: 'Privacy',
			download: 'Scarica',
			webApp: 'Web App',
			android: 'Android',
			ios: 'iOS e iPadOS',
			source: 'Codice sorgente',
			license: 'Licenza AGPL-3.0',
			reportBug: 'Segnala un bug',
			contribute: 'Contribuisci'
		}
	}
} as const;

export type Dictionary = typeof it;
