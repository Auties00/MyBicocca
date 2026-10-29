import { epochSeconds, isoDate } from '../../dates.ts';
import { html, json, route, text } from '../../router.ts';
import { demoSubjects, type DemoSubject } from './schedule.calendar.ts';

/**
 * EasyStaff's rooms, as the campus map reads them: the room list of every site (`combo.php`,
 * script variables), each site's showcase (`index.php`, `window.data_to_show`) and the day's
 * bookings of a site's rooms (`rooms_call.php`). Rooms and seats follow the real sites; the
 * persona's lectures (`schedule.calendar.ts`) sit in their rooms, among bookings that differ by
 * room and day but stay the same all day long.
 */

const AGENDA = 'https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib';

/** Code, name, seats, floor as the staff type it. */
type Room = readonly [code: string, name: string, seats: number, floor: string];

interface Site {
	name: string;
	address: string;
	rooms: readonly Room[];
}

const SITES: Record<string, Site> = {
	U01: {
		name: 'ATLAS ex U1',
		address: 'Piazza della Scienza 1 - 20126 Milano',
		rooms: [
			['U1-01', 'U1- 01 con Podio', 163, '-1'],
			['U1-02', 'U1-02 con Podio', 97, '-1'],
			['U1-03', 'U1-03 con Podio', 130, '-1'],
			['U1-04', 'U1-04 con Podio', 97, '-1'],
			['U1-05', 'U1-05 con Podio', 97, '-1'],
			['U1-06', 'U1-06 con Podio', 116, '-1'],
			['U1-08', 'U1-08 con Podio', 147, '-1'],
			['U1-09', 'U1-09 con Podio', 174, '-1'],
			['U1-10', 'U1-10 con podio', 107, 'T'],
			['U1-11', 'U1-11', 46, 'T'],
			['U1-12', 'U1-12', 46, 'T'],
			['U1-13', 'U1-13', 46, 'T']
		]
	},
	U02: {
		name: 'QUANTUM ex U2',
		address: 'Piazza della Scienza 2 - 20126 Milano',
		rooms: [
			['U2-01', 'U2-01 con Podio', 168, '-1'],
			['U2-02', 'U2-02 con Podio', 115, '-1'],
			['U2-03', 'U2-03', 46, 'T'],
			['U2-04', 'U2-04', 46, 'T'],
			['U2-05', 'U2-05', 46, 'T'],
			['U2-06', 'U2-06 con Podio', 160, '-1'],
			['U2-07', 'U2-07 con Podio', 174, '-1'],
			['U2-08a', 'U2-08a con Podio', 126, '1'],
			['U2-08b', 'U2-08b', 180, '1']
		]
	},
	U03: {
		name: 'BIOS ex U3',
		address: 'Piazza della Scienza 3 - 20126 Milano',
		rooms: [
			['U3-01', 'U3-01 con Podio', 185, '-1'],
			['U3-02', 'U3-02 con Podio', 250, '-1'],
			['U3-03', 'U3-03 con Podio', 250, '-1'],
			['U3-04', 'U3-04 con Podio', 166, 'T'],
			['U3-05', 'U3-05 con Podio', 194, 'T'],
			['U3-06', 'U3-06', 50, 'T'],
			['U3-07', 'U3-07 con Podio', 118, '1'],
			['U3-08', 'U3-08 con Podio', 110, '1']
		]
	},
	U04: {
		name: 'TELLUS ex U4',
		address: 'Piazza della Scienza 4 - 20126 Milano',
		rooms: [
			['U4-01', 'U4-01 con Podio', 160, '-1'],
			['U4-02', 'U4-02 con Podio', 174, '-1'],
			['U4-03', 'U4-03 con Podio', 174, '-1'],
			['U4-04', 'U4-04 con Podio', 115, 'T'],
			['U4-05', 'U4-05', 46, 'T'],
			['U4-06', 'U4-06', 46, 'T'],
			['U4-07', 'U4-07', 46, 'T'],
			['U4-08', 'U4-08 con Podio', 120, '1'],
			['Aula3019_videoconf', 'Sala videoconferenza 3019 U4 3°piano', 18, '3']
		]
	},
	'U05-MATERIALI': {
		name: 'AULE DIP. SC.MATERIALI (RATIO - ex U5)',
		address: 'Via Roberto Cozzi 55 - 20125 Milano',
		rooms: [
			['U5-2107', 'U5-2107', 20, '2'],
			['U5-2109', 'U5-2109', 30, '2']
		]
	},
	U06: {
		name: "AGORA' ex U6",
		address: "Piazza dell'Ateneo Nuovo 1 - 20126 Milano",
		rooms: [
			['U6-01a', 'U6-01a con Podio', 178, 'T'],
			['U6-01b', 'U6-01b con Podio', 178, 'T'],
			['U6-01c', 'U6-01c con Podio', 78, 'T'],
			['U6-02', 'U6-02 con Podio', 247, 'T'],
			['U6-03', 'U6-03 con Podio', 208, 'T'],
			['U6-05', 'U6-05 con Podio', 244, '1'],
			['U6-06', 'U6-06 con Podio', 286, '1'],
			['U6-07', 'U6-07 con Podio', 240, '1'],
			['U6-08', 'U6-08 con Podio', 286, '1'],
			['U6-10', 'U6-10 con Podio', 100, '2'],
			['U6-11', 'U6-11 con Podio', 100, '2'],
			['U6-12', 'U6-12', 43, '2'],
			['U6-16', 'U6-16', 43, '2'],
			['U6-20', 'U6-20 con Podio', 100, '3'],
			['U6-21', 'U6-21 con Podio', 100, '3'],
			['U6-31', 'U6-31', 34, '3']
		]
	},
	'U06-GIURISPRUDENZA': {
		name: "AULE DIP. GIURISPRUDENZA(AGORA' - ex U6)",
		address: "Piazza dell'Ateneo Nuovo 1 - 20126 Milano",
		rooms: [
			['GIURI-2117', 'Aula  2117', 30, '2'],
			['GIURI-2088', 'Aula 2088 (Aula Tribunale)', 30, '2'],
			['GIURI-2151', 'Aula 2151 (Sala Lauree)', 80, '2']
		]
	},
	U07: {
		name: 'CIVITAS ex U7',
		address: 'Via Bicocca degli Arcimboldi 8 - 20126 Milano',
		rooms: [
			['U7-01', 'U7-01 con Podio', 454, 'T'],
			['U7-02', 'U7-02 con Podio', 448, 'T'],
			['U7-03', 'U7-03 con Podio', 477, 'T'],
			['U7-04', 'U7-04 con Podio', 240, '1'],
			['U7-05', 'U7-05 con Podio', 240, '1'],
			['U7-06', 'U7-06', 42, '1'],
			['U7-07', 'U7-07', 37, '1'],
			['U7-12', 'U7-12', 97, '2'],
			['U7-13', 'U7-13', 93, '2'],
			['LAB 713', 'Laboratorio 713', 60, '7']
		]
	},
	U08: {
		name: 'ASCLEPIO ex U8',
		address: 'Via Cadore 48 - 20900 Monza',
		rooms: [
			['U8-01', 'U8-01 con Podio', 68, 'T'],
			['U8-03', 'U8-03 con Podio', 120, 'T'],
			['U8-04', 'U8-04 con Podio', 120, 'T'],
			['U8-AULAMAGNA', 'U8-AULAMAGNA', 200, 'T'],
			['U8-MICROSCOPI', 'Aula Microscopi', 50, '1'],
			['U8-LAB-BIO', 'Laboratorio di Biologia', 20, '1']
		]
	},
	U09: {
		name: "KOINE' ex U9",
		address: "Viale dell'Innovazione 10 - 20126 Milano",
		rooms: [
			['U9-01', 'U9-01 con Podio', 302, 'T'],
			['U9-02', 'U9-02', 83, 'T'],
			['U9-03', 'U9-03', 65, 'T'],
			['U9-04', 'U9-04 con Podio', 119, '1'],
			['U9-05', 'U9-05 con Podio', 106, '1'],
			['U9-06', 'U9-06 con Podio', 270, '1'],
			['U9-12', 'U9-12 con Podio', 61, '2'],
			['U9-13', 'U9-13', 52, '2']
		]
	},
	U14: {
		name: 'ABACUS ex U14',
		address: 'Viale Sarca 336 - 20126 Milano',
		rooms: [
			['U14-T014', 'Aula T014', 20, 'T'],
			['U14-T023', 'Aula T023', 70, 'T'],
			['U14-T024', 'Aula T024', 70, 'T']
		]
	},
	'U14-DISCO': {
		name: 'SALE INTERNE DIP. INFORMATICA e SISTEMISTICA (ABACUS - ex U14)',
		address: 'Viale Sarca 336 - 20126 Milano',
		rooms: [
			['1036', 'Sala Seminari', 49, '1'],
			['1016', 'Sala Lettura (1016)', 8, '1'],
			['3003', 'Sala Riunioni Terzo Piano (3003)', 8, '3']
		]
	},
	U16: {
		name: 'MAIEUTICA ex U16',
		address: "Piazza dell'Ateneo Nuovo 1 - 20126 Milano",
		rooms: [
			['U16-01a', 'U16-01a con Podio', 142, 'T'],
			['U16-01b', 'U16-01b con Podio', 147, 'T'],
			['U16-07', 'U16-07 con Podio', 112, '1'],
			['U16-08', 'U16-08 con Podio', 96, '1'],
			['U16-14', 'U16-14', 40, '2']
		]
	},
	'U16-FORMAZIONE': {
		name: 'AULE DIP. SC.UMANE PER LA FORMAZIONE (MAIEUTICA - ex U16)',
		address: "Piazza dell'Ateneo Nuovo 1 - 20126 Milano",
		rooms: [
			['FORMAZIONE - U16-03', 'U16-03', 35, '1'],
			['FORMAZIONE - U16-09', 'U16-09', 43, '2']
		]
	},
	U18: {
		name: 'YGEIA ex U18',
		address: 'Via Cadore 48 - 20900 Monza',
		rooms: [
			['U18-Auditorium', 'U18 - Auditorium', 158, 'T'],
			['U18-02', 'U18-02 con Podio', 134, 'T'],
			['U18-05', 'U18-05 con Podio', 92, '1'],
			['U18-09-lettini1', 'U18-09 (Lettini1)', 30, '1']
		]
	},
	U24: {
		name: 'ZIFERA ex U24',
		address: 'Viale Sarca 202 - 20126 Milano',
		rooms: [
			['U24-C1', 'Aula C1', 141, 'T'],
			['U24-C2', 'Aula C2', 141, 'T']
		]
	},
	U26: {
		name: 'AGON ex U26',
		address: 'Via Raffaello Giolli 7A - 20126 Milano',
		rooms: [['U26-PALESTRA', 'PALESTRA BICOCCA', 50, 'T']]
	},
	U28: {
		name: 'KYTOS ex U28',
		address: 'Via Cadore 48 - 20900 Monza',
		rooms: [['U28-01', 'U28-01', 150, 'T']]
	},
	U38: {
		name: 'VILLA SERENA ex U38',
		address: 'Via Pergolesi 33 - 20900 Monza',
		rooms: [
			['U38-01', 'U38-01 con Podio', 92, 'T'],
			['U38-02', 'U38-02', 44, 'T'],
			['U38-03', 'U38-03', 44, '1'],
			['U38-04', 'U38-04', 25, '1']
		]
	},
	'U-BG': {
		name: 'Sede Nini da Fano (Bergamo)',
		address: 'Via Alessandro Ninì da Fano 11 - 24125 Bergamo',
		rooms: [
			['U-BERGAMO-0.AU.10', 'A-05', 101, 'T'],
			['U-BERGAMO-0.AU.11', 'A-06', 131, 'T'],
			['U-BERGAMO-0.AU.24', 'A-07', 31, 'T'],
			['U-BERGAMO-1.AU.07', 'A-14', 20, '1'],
			['U-BERGAMO-1.AU.08', 'A-15', 39, '1']
		]
	},
	'U-MB': {
		name: 'Istituti Clinici Zucchi - Edificio Villa Maria (Carate Brianza)',
		address: 'Piazza Madonnina 1 - 20841 Carate Brianza',
		rooms: [
			['U-CARATE BRIANZA-01-A', 'Aula A', 30, 'T'],
			['U-CARATE BRIANZA-02-B', 'Aula B', 30, 'T']
		]
	},
	LIB: {
		name: "LIB - Laboratori Informatici d'Ateneo",
		address: '',
		rooms: [
			['LIB-U7-713', 'LAB 713 (U7)', 60, ''],
			['LIB-U14-T014', 'LAB T014 (U14)', 20, '']
		]
	}
};

/** The persona's weekly lectures (as `schedule.calendar.ts` lays them out): Monday is 0. */
const LECTURES: ReadonlyArray<
	readonly [weekday: number, subject: DemoSubject, from: string, to: string, room: string]
> = [
	[0, demoSubjects.algorithms, '08:30', '10:30', 'U6-06'],
	[0, demoSubjects.statistics, '10:30', '12:30', 'U4-08'],
	[0, demoSubjects.languagesFirst, '14:30', '16:30', 'U1-09'],
	[1, demoSubjects.operatingSystems, '09:30', '12:30', 'U14-T024'],
	[1, demoSubjects.databases, '14:30', '16:30', 'U24-C2'],
	[2, demoSubjects.algorithms, '10:30', '12:30', 'U6-06'],
	[2, demoSubjects.languagesSecond, '13:30', '15:30', 'U1-09'],
	[3, demoSubjects.databases, '09:30', '11:30', 'U24-C2'],
	[3, demoSubjects.operatingSystemsLab, '13:30', '15:30', 'LAB 713'],
	[4, demoSubjects.statistics, '08:30', '10:30', 'U4-08'],
	[4, demoSubjects.algorithmsExercises, '10:30', '12:30', 'U6-06']
];

/** Other courses taught on campus, to fill the rooms around the persona's. */
const COURSES: ReadonlyArray<readonly [title: string, kind: string, teachers: string]> = [
	['FISICA I - I MODULO (T1 FRONTALE)', 'Lezione', 'MARCO|PAGANONI'],
	['CHIMICA GENERALE ED INORGANICA', 'Lezione', "MASSIMILIANO|D'ARIENZO"],
	['ALGEBRA LINEARE E GEOMETRIA (TU FRONTALE TURNO UNICO)', 'Lezione', 'SAMUELE|MONGODI'],
	['ISTITUZIONI DI DIRITTO PUBBLICO', 'Lezione', 'NICOLA|CANZIAN'],
	['PSICOLOGIA DELLO SVILUPPO', 'Lezione', 'CLAUDIA|CAPRIN'],
	['ECONOMIA POLITICA (T2 FRONTALE)', 'Lezione', 'LUCA|POLONIO'],
	['STATISTICAL MODELLING', 'Lezione', "Laura|D'angelo"],
	['MARINE ECOLOGY - Lecture', 'Lezione', 'DAVIDE|SEVESO'],
	['TERMODINAMICA', 'Lezione', 'DARIO|NARDUCCI'],
	['FILOSOFIA POLITICA', 'Lezione', 'GIORGIA|SERUGHETTI'],
	['Tutoraggio Fisica II', 'Tutorato', 'GIUSEPPE|GORINI'],
	['Appello di Analisi Matematica I', 'Esame', 'NICOLA|TURCHI'],
	['Riunione Consiglio di Coordinamento Didattico', 'Riunione', ''],
	['Seminario di dipartimento', 'Seminario', '']
];

/** A whole number from `seed`, the same on every visit. */
function hash(seed: string): number {
	let value = 2166136261;
	for (const char of seed) value = Math.imul(value ^ char.charCodeAt(0), 16777619);
	return value >>> 0;
}

const pad = (value: number) => String(value).padStart(2, '0');

function parseWireDate(text: string | null): Date | undefined {
	const parts = /^(\d{2})-(\d{2})-(\d{4})$/.exec(text ?? '');
	return parts ? new Date(Number(parts[3]), Number(parts[2]) - 1, Number(parts[1])) : undefined;
}

function wireRoom([code, name, seats]: Room, site: string, siteName: string, order: number) {
	return {
		label: name,
		valore: code,
		sede_name: siteName,
		sede_code: site,
		capacity: seats,
		order: String(order)
	};
}

function roomsScript(sites: Record<string, Site>): string {
	const list = Object.entries(sites);
	const rooms = Object.fromEntries(
		list.map(([code, site]) => [
			code,
			site.rooms.map((room, index) => wireRoom(room, code, site.name, index))
		])
	);
	return [
		`var elenco_sedi = ${JSON.stringify(list.map(([code, site]) => ({ label: site.name, valore: code })))};`,
		`var elenco_aule = ${JSON.stringify(rooms)};`,
		`var elenco_scuole = ${JSON.stringify([{ label: 'Scienze', valore: 'AreaScientifica' }])};`
	].join('\n');
}

type Equipment = readonly [name: string, status?: string];

const HALL: readonly Equipment[] = [
	['Videoproiettore soffitto+Telo'],
	['Rete wifi'],
	['Impianto audio'],
	['Lavagna in ardesia'],
	['Podio'],
	['Gradoni'],
	['Registrazione'],
	['Kit aule standard per videolezioni'],
	['Tavolo Accessibile (disabilità)']
];
const CLASSROOM: readonly Equipment[] = [
	['Videoproiettore soffitto+Telo'],
	['Rete wifi'],
	['Lavagna a pennarelli'],
	['Sedie mobili con ribaltina'],
	['Presa elettrica su postazione studente', '1']
];
const LAB: readonly Equipment[] = [
	['PC cattedra'],
	['Rete fissa'],
	['Rete wifi'],
	['Videoproiettore soffitto+Telo'],
	['Presa elettrica su postazione studente'],
	['Finestre oscurabili']
];

function showcaseRoom([code, name, seats, floor]: Room) {
	const lab = /^lab/i.test(name) || /laboratorio/i.test(name);
	const hall = seats >= 100;
	const equipment = lab ? LAB : hall ? HALL : CLASSROOM;
	const accessible = hash(`${code}:access`) % 5 !== 0;
	const validated = accessible && hash(`${code}:valid`) % 3 === 0;
	return {
		room_code: code,
		room_name: name,
		room_description: lab
			? 'Laboratorio informatico con postazioni Linux e Windows.<br>Accesso con badge universitario.'
			: hall
				? `Aula ad anfiteatro con podio e registrazione delle lezioni.\r\nPosti a sedere: ${seats}`
				: '',
		room_capacity: String(seats),
		room_type_name: lab ? 'Laboratorio informatico' : hall ? `${seats - 10}-${seats + 10}` : 'Aula',
		room_google_map_link: '',
		room_floor_name: floor,
		room_senza_barriere_architettoniche: accessible ? '1' : '0',
		note_accessibilita: validated
			? 'Ingresso accessibile dal piano terra.<ul><li>Ascensore vicino alle scale</li><li>Postazione per carrozzina in prima fila</li></ul>'
			: '',
		validato: validated ? '1' : null,
		file_img_360: '',
		room_equipment_fixed: Object.fromEntries(
			equipment.map(([item, status = '0'], index) => [
				String(index + 2),
				{ Nome: item, Descrizione: '', Immagine: '', Stato: status, NotaStato: '' }
			])
		),
		room_equipment_mobile: []
	};
}

function showcasePage(codes: readonly string[], rooms: readonly string[]): string {
	const data = Object.fromEntries(
		codes
			.filter((code) => code in SITES)
			.map((code) => {
				const site = SITES[code];
				const wanted = site.rooms.filter(([room]) => rooms.length === 0 || rooms.includes(room));
				return [
					code,
					{
						area_code: code,
						area_name: site.name,
						address: site.address,
						area_google_map_link: '',
						elenco_aule: Object.fromEntries(wanted.map((room) => [room[0], showcaseRoom(room)]))
					}
				];
			})
	);
	return `<!doctype html><html><head><title>Vetrina aule</title></head><body><div id="vetrina"></div><script>\nwindow.data_to_show = ${JSON.stringify(data)};\n</script></body></html>`;
}

interface Booking {
	room: Room;
	site: string;
	title: string;
	kind: string;
	teachers: string;
	from: string;
	to: string;
	cancelled?: boolean;
}

/** The day's bookings of a room, besides the persona's lectures. */
function bookingsOf(room: Room, site: string, day: Date): Booking[] {
	const [code] = room;
	const weekday = (day.getDay() + 6) % 7;
	const key = `${code}:${isoDate(day)}`;
	if (code === 'U4-07' && weekday < 5) {
		return [
			{
				room,
				site,
				title: 'Chiusura',
				kind: 'chiusura_type',
				teachers: '',
				from: '00:00',
				to: '23:59'
			}
		];
	}
	/* Works close U4-06 for the working day only: the map tells until when. */
	if (code === 'U4-06' && weekday < 5) {
		return [
			{
				room,
				site,
				title: 'Chiusura',
				kind: 'chiusura_type',
				teachers: '',
				from: '08:00',
				to: '18:00'
			}
		];
	}
	const own = LECTURES.filter(
		([lectureDay, , , , where]) => lectureDay === weekday && where === code
	);
	const slots = weekday >= 5 ? [] : ['08:30', '10:30', '12:30', '14:30', '16:30'];
	const bookings: Booking[] = own.map(([, subject, from, to]) => ({
		room,
		site,
		title: subject.name,
		kind: 'Lezione',
		teachers: subject.course.teacher.toLocaleUpperCase('it-IT').replace(' ', '|'),
		from,
		to
	}));
	slots.forEach((from, index) => {
		const roll = hash(`${key}:${index}`);
		const [hour] = from.split(':').map(Number);
		const long = roll % 7 === 0 && index < slots.length - 1;
		const to = `${pad(hour + (long ? 3 : 2))}:30`;
		const taken = bookings.some((booking) => booking.from < to && booking.to > from);
		if (taken || roll % 100 > 55) return;
		const [title, kind, teachers] = COURSES[roll % COURSES.length];
		bookings.push({
			room,
			site,
			title,
			kind,
			teachers,
			from,
			to,
			cancelled: roll % 23 === 0
		});
	});
	return bookings;
}

function occupationEvent(booking: Booking, day: Date, id: number) {
	const [code, name, seats] = booking.room;
	const at = (time: string) => {
		const [hour, minute] = time.split(':').map(Number);
		return epochSeconds(new Date(day.getFullYear(), day.getMonth(), day.getDate(), hour, minute));
	};
	const teachers = booking.teachers
		? [booking.teachers.split('|')].map(([first, last]) => ({
				Nome: first,
				Cognome: last,
				Mail: '',
				Codice: ''
			}))
		: [];
	const closure = booking.kind === 'chiusura_type';
	return {
		id: String(id),
		from: `${booking.from}:00`,
		to: `${booking.to}:00`,
		NomeAula: name,
		CodiceAula: code,
		NomeSede: SITES[booking.site].name,
		CodiceSede: booking.site,
		capacity: seats,
		Giorno: isoDate(day),
		faculty: '20',
		...(closure ? {} : { Annullato: booking.cancelled ? '1' : '0' }),
		EventoAteneo: 0,
		name: booking.title,
		name_original: booking.title,
		nome: booking.title,
		timestamp_day: at('00:00'),
		timestamp_from: at(booking.from),
		timestamp_to: at(booking.to),
		Utenti: teachers,
		type: booking.kind,
		tipo: booking.kind,
		orario: `${booking.from} - ${booking.to}`
	};
}

function occupation(sites: readonly string[], day: Date) {
	let id = 1_475_000 + (hash(isoDate(day)) % 10_000);
	const events = sites
		.filter((site) => site in SITES)
		.flatMap((site) => SITES[site].rooms.flatMap((room) => bookingsOf(room, site, day)))
		.sort((a, b) => a.from.localeCompare(b.from))
		.map((booking) => occupationEvent(booking, day, id++));
	return { events, contains_events: events.length > 0, n_events: events.length };
}

export const routes = [
	route(
		`GET ${AGENDA}/combo.php?sw=rooms_`,
		() => text(roomsScript(SITES), 'text/javascript; charset=utf-8'),
		{ empty: () => text(roomsScript({}), 'text/javascript; charset=utf-8') }
	),
	route(
		`GET ${AGENDA}/index.php?view=vetrina_aule`,
		async (request) => {
			const form = await request.form();
			return html(showcasePage(form.getAll('sede[]'), form.getAll('aula[]')));
		},
		{ empty: () => html(showcasePage([], [])) }
	),
	route(
		`POST ${AGENDA}/rooms_call.php`,
		async (request) => {
			const form = await request.form();
			const day = parseWireDate(form.get('date')) ?? new Date();
			return json(occupation(form.getAll('sede[]'), day));
		},
		{ empty: () => json({ events: [], contains_events: false, n_events: 0 }) }
	)
];
