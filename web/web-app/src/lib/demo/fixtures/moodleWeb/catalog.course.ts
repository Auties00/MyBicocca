import { DemoElearning, type DemoCourse } from '../../persona.ts';
import { html, route } from '../../router.ts';
import {
	demoCourseById,
	demoCourseTitle,
	demoEmail,
	demoIdNumber
} from '../moodle/course.course.ts';

const SITE = 'https://elearning.unimib.it';

const escape = (value: string) =>
	value.replace(
		/[&<>"]/g,
		(char) => `&${{ '&': 'amp', '<': 'lt', '>': 'gt', '"': 'quot' }[char]};`
	);

function field(id: string, title: string, content: string): string {
	return `<div class="field field-${escape(title)} pb-1">
		<a data-toggle="collapse" href="#${id}" class="field-header w-100 collapsed"><h3 class="field-title">${escape(title)}</h3></a>
		<div id="${id}" class="collapse"><div class="field-content ">${content}</div></div>
	</div>`;
}

function italian(course: DemoCourse, topics: readonly string[]): string {
	const list = (items: readonly string[]) => items.map((item) => `${escape(item)}<br />`).join('');
	return [
		field(
			'obiettiviit',
			'Obiettivi',
			`<p>Il corso fornisce le conoscenze di base e gli strumenti metodologici di ${escape(demoCourseTitle(course))}.</p>
			<ul><li>Conoscenza e capacità di comprensione dei concetti fondamentali</li><li>Capacità di applicarli alla soluzione di problemi</li><li>Autonomia di giudizio nella scelta delle tecniche</li></ul>`
		),
		field('contenuti_sinteticiit', 'Contenuti sintetici', `<p>${escape(topics.join(', '))}.</p>`),
		field(
			'programma_estesoit',
			'Programma esteso',
			`<p><strong>Parte generale</strong><br />${list(topics.slice(0, 3))}</p>
			<p><strong>Parte avanzata</strong><br />${list(topics.slice(3))}</p>
			<p><strong>Laboratorio</strong><br />Esercitazioni guidate<br />Progetto finale</p>`
		),
		field(
			'prerequisitiit',
			'Prerequisiti',
			"<p>Programmazione 1, Fondamenti dell'informatica.</p>"
		),
		field(
			'modalita_didatticait',
			'Modalità didattica',
			'<p>Lezioni frontali ed esercitazioni in aula e in laboratorio, in italiano. Le lezioni sono registrate e pubblicate sulla pagina del corso.</p>'
		),
		field(
			'materiale_didatticoit',
			'Materiale didattico',
			'<p>Slide e dispense pubblicate sulla pagina del corso. Per approfondire: <em>Introduzione agli algoritmi e strutture dati</em>, Cormen, Leiserson, Rivest, Stein, McGraw-Hill.</p>'
		),
		field(
			'periodo_erogazioneit',
			"Periodo di erogazione dell'insegnamento",
			'<p>Secondo semestre</p>'
		),
		field(
			'modalita_profittoit',
			'Modalità di verifica del profitto e valutazione',
			'<p><strong>Prova scritta</strong> con esercizi e domande aperte, valutata in trentesimi, e <strong>discussione orale</strong> facoltativa.</p><p>Il progetto finale vale fino a 4 punti aggiuntivi.</p>'
		),
		field('orario_ricevimentoit', 'Orario di ricevimento', '<p>Su appuntamento, via e-mail.</p>')
	].join('\n');
}

function english(course: DemoCourse, topics: readonly string[]): string {
	return [
		field(
			'obiettivien',
			'Aims',
			`<p>The course provides the basic knowledge and the methods of ${escape(demoCourseTitle(course))}.</p>`
		),
		field('contentsen', 'Contents', `<p>${escape(topics.join(', '))}.</p>`),
		field(
			'programma_estesoen',
			'Detailed program',
			`<ul>${topics.map((topic) => `<li>${escape(topic)}</li>`).join('')}</ul>`
		),
		field(
			'prerequisitesen',
			'Prerequisites',
			'<p>Programming 1, Foundations of computer science.</p>'
		),
		field('teaching_formen', 'Teaching form', '<p>Lectures and lab sessions, in Italian.</p>'),
		field(
			'textbooken',
			'Textbook and teaching resource',
			'<p>Slides and notes on the course page.</p>'
		),
		field('semesteren', 'Semester', '<p>Second semester</p>'),
		field(
			'assessmenten',
			'Assessment method',
			'<p>Written exam and an optional oral discussion.</p>'
		),
		field('office_hoursen', 'Office hours', '<p>By appointment, by e-mail.</p>')
	].join('\n');
}

function contact(name: string, userId: number): string {
	const initials = name
		.split(' ')
		.map((word) => word.charAt(0))
		.join('');
	return `<li id="contact-${demoEmail(name)}" class="contact clearfix">
		<div class="contact-picture"><a href="${SITE}/user/profile.php?id=${userId}" class="d-inline-block aabtn"><span class="userinitials size-35" title="${escape(name)}" role="img">${initials}</span></a></div>
		<div class="contact-details"><div class="contact-name">${escape(name)}</div></div>
	</li>`;
}

function page(course: DemoCourse): string {
	const topics = DemoElearning.topics.get(course) ?? [];
	const id = course.moodleCourseId;
	return `<!DOCTYPE html><html lang="it"><head><title>Informazioni sul corso</title></head><body>
	<div id="course-header"><div class="coursename"><div class="card-title course-fullname">${escape(demoCourseTitle(course))}</div><div class="card-subtitle course-shortname">${demoIdNumber(course)}</div></div></div>
	<div role="main"><div class="box py-3 generalbox info"><div class="card mb-1 coursebox clearfix has-shortname has-info has-metadata " data-courseid=${id} data-type=1>
	<div class="card-body content"><div class="row no-gutters">
	<div class="coursebox-content col-md-8"><section class="syllabus">
		<div class="tab-content syllabus-content p-3">
			<div class="tab-pane active" id="it" role="tabpanel">
				<div class="syllabus-actions text-right"><a class="btn" href="${SITE}/local/syllabus/export.php?courseid=${id}&amp;language=it">Esporta</a></div>
				<div class="accordion">${italian(course, topics)}</div>
			</div>
			<div class="tab-pane " id="en" role="tabpanel">
				<div class="syllabus-actions text-right"><a class="btn" href="${SITE}/local/syllabus/export.php?courseid=${id}&amp;language=en">Export</a></div>
				<div class="accordion">${english(course, topics)}</div>
			</div>
		</div>
	</section></div>
	<div class="coursebox-sidebar col-md-4">
		<div class="course-access px-3"><a class="btn btn-lg btn-block btn-success" href="${SITE}/course/view.php?id=${id}">Entra</a></div>
		<div class="summary-block course-contacts"><ul class="summary-content teachers">
			<h4 class="contact-role">Docente</h4>
			${contact(course.teacher, 30000 + id)}
			<h4 class="contact-role">Tutor</h4>
			${contact(course.tutor, 31000 + id)}
		</ul></div>
		<div class="summary-block course-opinion"><div class="summary-content opinion-data container-fluid"><a href="https://opinionistudenti.unimib.it/valdid/opinioniAD.vm?idAD=${course.code}" target="_blank">Vedi valutazione del precedente anno accademico</a></div></div>
		<div class="summary-block course-biblio"><div class="summary-content biblio-data container-fluid"><a href="https://unimib.on.worldcat.org/search?queryString=${demoIdNumber(course)}" target="_blank">Trova i libri per questo corso nella Biblioteca di Ateneo</a></div></div>
		<div class="summary-block course-enrolments"><div class="summary-content enrols-data container-fluid">
			<div class="row no-gutters w-100"><div class="col-6 font-weight-bold">Iscrizione manuale</div></div>
			<div class="row no-gutters w-100"><div class="col-6 font-weight-bold">Iscrizione spontanea (Studente)</div></div>
		</div></div>
	</div>
	</div></div></div></div></div>
	</body></html>`;
}

const EMPTY =
	'<!DOCTYPE html><html><body><div role="main"><p>Corso non trovato</p></div></body></html>';

export const routes = [
	route(
		`GET ${SITE}/course/info.php`,
		({ url }) => {
			const course = demoCourseById(url.searchParams.get('id'));
			return html(course === undefined ? EMPTY : page(course));
		},
		{
			empty: ({ url }) =>
				html(
					`<!DOCTYPE html><html><body><div class="card mb-1 coursebox clearfix has-info has-metadata" data-courseid=${Number(url.searchParams.get('id'))}><div class="tab-content"></div></div></body></html>`
				)
		}
	)
];
