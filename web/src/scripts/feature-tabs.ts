/**
 * `<feature-tabs>`: an accessible tab set (WAI-ARIA "tabs with automatic activation").
 *
 * The markup is fully server-rendered; this element only wires up selection and
 * keyboard navigation, so it stays tiny and works with any number of tabs.
 */
export class FeatureTabs extends HTMLElement {
  #tabs: HTMLElement[] = [];
  #panels: HTMLElement[] = [];
  #abort: AbortController | undefined;

  connectedCallback(): void {
    this.#tabs = [...this.querySelectorAll<HTMLElement>('[role="tab"]')];
    this.#panels = this.#tabs
      .map((tab) => tab.getAttribute('aria-controls'))
      .map((id) => (id ? this.querySelector<HTMLElement>(`#${CSS.escape(id)}`) : null))
      .filter((panel): panel is HTMLElement => panel !== null);

    if (this.#tabs.length === 0 || this.#tabs.length !== this.#panels.length) return;

    this.#abort = new AbortController();
    const { signal } = this.#abort;
    for (const [index, tab] of this.#tabs.entries()) {
      tab.addEventListener('click', () => this.select(index), { signal });
      tab.addEventListener('keydown', (event) => this.#onKeyDown(event, index), { signal });
    }
  }

  disconnectedCallback(): void {
    this.#abort?.abort();
  }

  /** Activates the tab at `index` and shows its panel. */
  select(index: number, { focus = false } = {}): void {
    for (const [i, tab] of this.#tabs.entries()) {
      const selected = i === index;
      tab.setAttribute('aria-selected', String(selected));
      tab.tabIndex = selected ? 0 : -1;
      this.#panels[i]?.toggleAttribute('hidden', !selected);
    }
    if (focus) this.#tabs[index]?.focus();
  }

  #onKeyDown(event: KeyboardEvent, index: number): void {
    const last = this.#tabs.length - 1;
    const target = ((): number | undefined => {
      switch (event.key) {
        case 'ArrowDown':
        case 'ArrowRight':
          return index === last ? 0 : index + 1;
        case 'ArrowUp':
        case 'ArrowLeft':
          return index === 0 ? last : index - 1;
        case 'Home':
          return 0;
        case 'End':
          return last;
        default:
          return undefined;
      }
    })();

    if (target === undefined) return;
    event.preventDefault();
    this.select(target, { focus: true });
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'feature-tabs': FeatureTabs;
  }
}

if (!customElements.get('feature-tabs')) customElements.define('feature-tabs', FeatureTabs);
