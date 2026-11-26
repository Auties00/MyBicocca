const LANG_KEY = 'mybicocca:lang';

type Locale = 'it' | 'en';

const META: Record<Locale, { title: string; description: string }> = {
	it: {
		title: 'MyBicocca: tutta la tua università, in un solo posto',
		description:
			'Orario lezioni, esami Esse3, e-learning Moodle, mappa del campus e posti in biblioteca per gli studenti di Milano-Bicocca. Su Web, Android e presto iOS.'
	},
	en: {
		title: 'MyBicocca: your entire university, in one place',
		description:
			'Timetable, Esse3 exams, Moodle e-learning, campus map and library seat bookings for Milano-Bicocca students. On Web, Android, and soon iOS.'
	}
};

function currentLang(): Locale {
	return document.documentElement.dataset.lang === 'en' ? 'en' : 'it';
}

function setupLanguage(onLangChange?: () => void) {
	const root = document.querySelector<HTMLElement>('[data-lang-dropdown]');
	const trigger = root?.querySelector<HTMLButtonElement>('[data-lang-trigger]');
	const menu = root?.querySelector<HTMLElement>('[data-lang-menu]');

	const setOpen = (open: boolean) => {
		if (!trigger || !menu) return;
		trigger.setAttribute('aria-expanded', String(open));
		menu.hidden = !open;
	};

	const syncLangUi = (lang: Locale) => {
		for (const item of document.querySelectorAll<HTMLElement>('[role="menuitemradio"][data-set-lang]')) {
			item.setAttribute('aria-checked', String(item.dataset.setLang === lang));
		}
		document.title = META[lang].title;
		const desc = document.querySelector('meta[name="description"]');
		if (desc) desc.setAttribute('content', META[lang].description);
	};

	const applyLang = (lang: Locale) => {
		document.documentElement.lang = lang;
		document.documentElement.dataset.lang = lang;
		try {
			localStorage.setItem(LANG_KEY, lang);
		} catch {
			/* ignore storage restrictions */
		}
		syncLangUi(lang);
		setOpen(false);
		onLangChange?.();
	};

	syncLangUi(currentLang());

	for (const button of document.querySelectorAll<HTMLButtonElement>('[data-set-lang]')) {
		button.addEventListener('click', () => {
			const next = button.dataset.setLang;
			if (next === 'it' || next === 'en') applyLang(next);
		});
	}

	if (!root || !trigger || !menu) return;

	trigger.addEventListener('click', (event) => {
		event.stopPropagation();
		setOpen(menu.hidden);
	});

	document.addEventListener('click', (event) => {
		if (!menu.hidden && !root.contains(event.target as Node)) setOpen(false);
	});

	document.addEventListener('keydown', (event) => {
		if (event.key === 'Escape' && !menu.hidden) {
			setOpen(false);
			trigger.focus();
		}
	});
}

function setupNavBehavior(): () => void {
	const actions = document.querySelector<HTMLElement>('[data-nav-actions]');
	const heroCta = document.getElementById('hero-cta');
	let remeasure = () => {};

	if (actions && heroCta && 'IntersectionObserver' in window) {
		const inner = actions.querySelector<HTMLElement>('.nav-actions-inner');
		const measure = () => {
			if (!inner) return;
			const w = Math.ceil(inner.getBoundingClientRect().width);
			if (w > 0) actions.style.setProperty('--actions-w', `${w}px`);
		};
		remeasure = measure;

		measure();
		window.addEventListener('resize', measure, { passive: true });
		if (document.fonts?.ready) document.fonts.ready.then(measure);

		let first = true;
		const ctaObserver = new IntersectionObserver(
			(entries) => {
				const heroVisible = entries[0]?.isIntersecting ?? true;
				actions.classList.toggle('is-hidden', heroVisible);
				if (first) {
					first = false;
					requestAnimationFrame(() => actions.classList.remove('no-anim'));
				}
			},
			{ rootMargin: '-64px 0px 0px 0px', threshold: 0 }
		);
		ctaObserver.observe(heroCta);
	} else if (actions) {
		actions.classList.remove('no-anim', 'is-hidden');
	}

	const links = [...document.querySelectorAll<HTMLAnchorElement>('[data-nav-links] a[href^="#"]')];
	if (links.length === 0) return remeasure;

	const pairs = links
		.map((link) => {
			const id = link.getAttribute('href')?.slice(1);
			const section = id ? document.getElementById(id) : null;
			return section ? { link, section } : null;
		})
		.filter((pair): pair is { link: HTMLAnchorElement; section: HTMLElement } => pair !== null);

	const updateCurrent = () => {
		const probe = window.scrollY + 140;
		let active: HTMLAnchorElement | null = null;
		for (const { link, section } of pairs) {
			if (section.offsetTop <= probe) active = link;
		}
		for (const { link } of pairs) {
			link.classList.toggle('is-current', link === active);
		}
	};

	updateCurrent();
	window.addEventListener('scroll', updateCurrent, { passive: true });
	return remeasure;
}

function setupReveals() {
	const nodes = [...document.querySelectorAll<HTMLElement>('[data-reveal]')];
	if (nodes.length === 0) return;

	if (!('IntersectionObserver' in window) || matchMedia('(prefers-reduced-motion: reduce)').matches) {
		for (const node of nodes) node.dataset.reveal = 'in';
		return;
	}

	const revealAboveFold = () => {
		const cutoff = window.innerHeight + 60;
		for (const node of nodes) {
			if (node.dataset.reveal === 'in') continue;
			if (node.getBoundingClientRect().top <= cutoff) {
				node.dataset.reveal = 'in';
			}
		}
	};

	const observer = new IntersectionObserver(
		(entries) => {
			for (const entry of entries) {
				if (!entry.isIntersecting) continue;
				(entry.target as HTMLElement).dataset.reveal = 'in';
				observer.unobserve(entry.target);
			}
		},
		{ rootMargin: '60px 0px 60px 0px', threshold: 0 }
	);

	for (const node of nodes) observer.observe(node);
	revealAboveFold();
	window.addEventListener('scroll', revealAboveFold, { passive: true });
	window.addEventListener('hashchange', () => setTimeout(revealAboveFold, 60));
}

function setupFeaturePlatform(): () => void {
	const root = document.querySelector<HTMLElement>('[data-features-group]');
	if (!root) return () => {};

	const bar = root.querySelector<HTMLElement>('[data-platform-bar]');
	const barInner = root.querySelector<HTMLElement>('[data-platform-bar-inner]');
	const label = root.querySelector<HTMLElement>('[data-platform-label]');
	const tabsRow = root.querySelector<HTMLElement>('[data-platform-tabs]');
	const dropdown = root.querySelector<HTMLElement>('[data-platform-dropdown]');
	const trigger = root.querySelector<HTMLButtonElement>('[data-platform-trigger]');
	const menu = root.querySelector<HTMLElement>('[data-platform-menu]');

	const platformTabs = [...root.querySelectorAll<HTMLButtonElement>('[role="tab"][data-select-platform]')];
	const menuOptions = [
		...root.querySelectorAll<HTMLButtonElement>('[role="menuitemradio"][data-select-platform]')
	];

	const setOpen = (open: boolean) => {
		if (!trigger || !menu) return;
		trigger.setAttribute('aria-expanded', String(open));
		menu.hidden = !open;
	};

	const syncInk = () => {
		if (!tabsRow) return;
		const active = tabsRow.querySelector<HTMLButtonElement>('[role="tab"][aria-selected="true"]');
		if (!active || active.offsetWidth === 0) return;
		tabsRow.style.setProperty('--ink-x', `${active.offsetLeft}px`);
		tabsRow.style.setProperty('--ink-w', `${active.offsetWidth}px`);
		tabsRow.dataset.inkReady = 'true';
	};

	const sections = [...root.querySelectorAll<HTMLElement>('.feature-section')];

	const setPlatform = (id: string, preserveScroll = false) => {
		let anchor: HTMLElement | null = null;
		let beforeTop = 0;
		if (preserveScroll) {
			anchor = sections.find((sec) => sec.getBoundingClientRect().bottom > 120) ?? null;
			if (anchor) beforeTop = anchor.getBoundingClientRect().top;
		}

		root.dataset.platform = id;
		for (const tab of platformTabs) {
			tab.setAttribute('aria-selected', String(tab.dataset.selectPlatform === id));
		}
		for (const opt of menuOptions) {
			opt.setAttribute('aria-checked', String(opt.dataset.selectPlatform === id));
		}
		syncInk();
		setOpen(false);

		if (anchor) {
			const delta = anchor.getBoundingClientRect().top - beforeTop;
			if (Math.abs(delta) > 1) {
				window.scrollBy({ top: delta, behavior: 'instant' });
			}
		}
	};

	const updateCollapse = () => {
		if (!bar || !barInner || !label || !tabsRow) return;
		const style = getComputedStyle(barInner);
		const pad = parseFloat(style.paddingLeft) + parseFloat(style.paddingRight);
		const available = barInner.clientWidth - pad;
		const needed = label.offsetWidth + tabsRow.scrollWidth + 44;
		const collapsed = needed > available;
		bar.dataset.collapsed = collapsed ? 'true' : 'false';
		if (!collapsed) {
			setOpen(false);
			syncInk();
		}
	};

	const device = document.documentElement.dataset.device;
	if (device === 'android') {
		setPlatform('android');
	} else if (device === 'ios') {
		setPlatform(matchMedia('(width >= 768px)').matches ? 'web-tablet' : 'web-mobile');
	} else if (matchMedia('(width < 640px)').matches) {
		setPlatform('web-mobile');
	}

	for (const button of [...platformTabs, ...menuOptions]) {
		button.addEventListener('click', () => {
			const target = button.dataset.selectPlatform;
			if (target) setPlatform(target, true);
		});
	}

	if (dropdown && trigger && menu) {
		trigger.addEventListener('click', (event) => {
			event.stopPropagation();
			setOpen(menu.hidden);
		});

		document.addEventListener('click', (event) => {
			if (!menu.hidden && !dropdown.contains(event.target as Node)) setOpen(false);
		});

		document.addEventListener('keydown', (event) => {
			if (event.key === 'Escape' && !menu.hidden) {
				setOpen(false);
				trigger.focus();
			}
		});
	}

	updateCollapse();
	if (barInner && 'ResizeObserver' in window) {
		const ro = new ResizeObserver(updateCollapse);
		ro.observe(barInner);
	} else {
		window.addEventListener('resize', updateCollapse, { passive: true });
	}
	if (document.fonts?.ready) document.fonts.ready.then(updateCollapse);

	return updateCollapse;
}

const remeasureNav = setupNavBehavior();
const remeasurePlatforms = setupFeaturePlatform();
setupLanguage(() => {
	remeasureNav();
	remeasurePlatforms();
});
setupReveals();
