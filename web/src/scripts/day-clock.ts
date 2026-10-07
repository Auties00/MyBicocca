import { type ClockTime, type TimeAnchor, dayProgress, formatClock, parseClock, timeAt } from '~/lib/clock';

const CLOCK_PATTERN = /^\d{2}:\d{2}$/;

function isClockTime(value: string | undefined): value is ClockTime {
  return value !== undefined && CLOCK_PATTERN.test(value);
}

interface Mark extends TimeAnchor {
  readonly element: HTMLElement;
}

/**
 * `<day-clock>`: the red "now" line fixed across the viewport, like the one in the
 * app's day view.
 *
 * Every element with `data-time="HH:MM"` is a waypoint. As the page scrolls, the
 * clock reads the time at the line's position by interpolating between waypoints,
 * marks the moment the line is in with `data-now`, and publishes the day's progress
 * as `--day` on the root element, which scrubs the page from morning to night.
 */
export class DayClock extends HTMLElement {
  #marks: Mark[] = [];
  #lineTop = 0;
  #output: HTMLTimeElement | null = null;
  #current: HTMLElement | undefined;
  #label = '';
  #progress = -1;
  #frame = 0;
  #abort: AbortController | undefined;
  #observer: ResizeObserver | undefined;

  connectedCallback(): void {
    this.#output = this.querySelector('time');
    this.#abort = new AbortController();
    const { signal } = this.#abort;

    window.addEventListener('scroll', this.#schedule, { passive: true, signal });
    // Layout shifts (fonts, images, viewport changes) move the waypoints.
    this.#observer = new ResizeObserver(this.#remeasure);
    this.#observer.observe(document.body);
    this.#remeasure();
  }

  disconnectedCallback(): void {
    this.#abort?.abort();
    this.#observer?.disconnect();
    cancelAnimationFrame(this.#frame);
  }

  #remeasure = (): void => {
    const scroll = window.scrollY;
    this.#lineTop = this.getBoundingClientRect().top;
    this.#marks = [...document.querySelectorAll<HTMLElement>('[data-time]')]
      .flatMap((element) => {
        const { time } = element.dataset;
        if (!isClockTime(time)) return [];
        return [{ element, time: parseClock(time), offset: element.getBoundingClientRect().top + scroll }];
      })
      .sort((a, b) => a.offset - b.offset);
    this.#update();
  };

  #schedule = (): void => {
    if (this.#frame) return;
    this.#frame = requestAnimationFrame(() => {
      this.#frame = 0;
      this.#update();
    });
  };

  #update(): void {
    const position = window.scrollY + this.#lineTop;
    const now = timeAt(this.#marks, position);

    const label = formatClock(now);
    if (label !== this.#label && this.#output) {
      this.#label = label;
      this.#output.textContent = label;
      this.#output.dateTime = label;
    }

    // Quantised so scrolling doesn't restyle the page more often than it can show.
    const progress = Math.round(dayProgress(now) * 1000) / 1000;
    if (progress !== this.#progress) {
      this.#progress = progress;
      document.documentElement.style.setProperty('--day', String(progress));
    }

    // Once midnight has scrolled well past the line, the day is over: the clock bows out.
    const last = this.#marks.at(-1);
    this.toggleAttribute('data-ended', last !== undefined && position > last.offset + window.innerHeight * 0.5);

    const current = this.#marks.findLast((mark) => mark.offset <= position)?.element;
    if (current !== this.#current) {
      this.#current?.removeAttribute('data-now');
      current?.setAttribute('data-now', '');
      this.#current = current;
    }
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'day-clock': DayClock;
  }
}

if (!customElements.get('day-clock')) customElements.define('day-clock', DayClock);
