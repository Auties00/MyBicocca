import { decodeInteger, decodeNumber } from "./coding.js";

/** A node of a parsed HTML page: an element, or text. */
export type ScrapeNode = Node;

function present(text: string | null | undefined, missing: readonly string[] = []): string | undefined {
  const value = text?.replace(/ /g, " ").trim();
  if (value === undefined || value === "") return undefined;
  return missing.some((absent) => sameText(absent, value)) ? undefined : value;
}

function sameText(a: string, b: string): boolean {
  return a.localeCompare(b, undefined, { sensitivity: "accent" }) === 0;
}

/** The node as something selectors can search: an element or a document. */
function queryRoot(node: ScrapeNode | undefined): ParentNode | undefined {
  const type = node?.nodeType;
  return type === Node.ELEMENT_NODE || type === Node.DOCUMENT_NODE ? (node as unknown as ParentNode) : undefined;
}

function asElement(node: ScrapeNode | undefined): Element | undefined {
  return node !== undefined && node.nodeType === Node.ELEMENT_NODE ? (node as Element) : undefined;
}

/** Text with whitespace collapsed, as a browser renders it. */
function collapsed(text: string | null): string {
  return (text ?? "").replace(/\s+/g, " ");
}

function siblings(node: ScrapeNode): ScrapeNode[] {
  return Array.from(node.parentNode?.childNodes ?? []).filter((sibling) => sibling !== node);
}

/**
 * The steps generated scrapers compile to. Every helper accepts and returns `undefined` for a
 * missing value, so it travels to the end of a pipeline; reads return `undefined` for blank text.
 */
export const scrape = {
  /** Parses `html` as a document whose relative links resolve against `url`. */
  document(html: string, url: string): ScrapeNode {
    const document = new DOMParser().parseFromString(html, "text/html");
    const base = document.createElement("base");
    base.href = url;
    document.head.prepend(base);
    return document;
  },

  /** The JSON a script assigns to `variable` (`var name = [...];`), or `undefined` when it assigns none. */
  scriptVariable(script: string, variable: string): unknown {
    const prefix = `var ${variable} =`;
    const line = script.split(/\r?\n/).find((candidate) => candidate.startsWith(prefix));
    if (line === undefined) return undefined;
    return JSON.parse(line.slice(prefix.length).trim().replace(/;$/, ""));
  },

  select(node: ScrapeNode | undefined, selector: string): ScrapeNode | undefined {
    return queryRoot(node)?.querySelector(selector) ?? undefined;
  },

  selectAll(node: ScrapeNode | undefined, selector: string): ScrapeNode[] {
    return Array.from(queryRoot(node)?.querySelectorAll(selector) ?? []);
  },

  root(node: ScrapeNode | undefined): ScrapeNode | undefined {
    return node?.ownerDocument ?? undefined;
  },

  next(node: ScrapeNode | undefined): ScrapeNode | undefined {
    return asElement(node)?.nextElementSibling ?? undefined;
  },

  lastSibling(node: ScrapeNode | undefined): ScrapeNode | undefined {
    return node === undefined ? undefined : siblings(node).at(-1);
  },

  /** The element of the whole document matched by the selector in the node's `attribute`. */
  follow(node: ScrapeNode | undefined, attribute: string): ScrapeNode | undefined {
    const selector = scrape.attribute(node, attribute, []);
    if (selector === undefined) return undefined;
    try {
      return node?.ownerDocument?.querySelector(selector) ?? undefined;
    } catch {
      return undefined;
    }
  },

  /** The node's children grouped into sections, each starting with a child matching `header`. */
  sections(node: ScrapeNode | undefined, header: string): ScrapeNode[] {
    const element = asElement(node);
    if (element === undefined) return [];
    const sections: Element[] = [];
    for (const child of Array.from(element.children)) {
      if (child.matches(header)) sections.push(element.ownerDocument.createElement("section"));
      sections.at(-1)?.append(child.cloneNode(true));
    }
    return sections;
  },

  text(node: ScrapeNode | undefined, exclude: string | undefined, missing: readonly string[]): string | undefined {
    if (node === undefined) return undefined;
    const element = asElement(node);
    if (element === undefined || exclude === undefined) return present(collapsed(node.textContent), missing);
    const copy = element.cloneNode(true) as Element;
    copy.querySelectorAll(exclude).forEach((excluded) => excluded.remove());
    return present(collapsed(copy.textContent), missing);
  },

  ownText(node: ScrapeNode | undefined, missing: readonly string[]): string | undefined {
    if (node === undefined) return undefined;
    const own = asElement(node) === undefined
      ? node.textContent
      : Array.from(node.childNodes).filter((child) => child.nodeType === Node.TEXT_NODE).map((child) => child.textContent).join("");
    return present(collapsed(own), missing);
  },

  html(node: ScrapeNode | undefined): string | undefined {
    return present(asElement(node)?.innerHTML);
  },

  attribute(node: ScrapeNode | undefined, name: string, missing: readonly string[]): string | undefined {
    return present(asElement(node)?.getAttribute(name), missing);
  },

  /** The node's `attribute` as an absolute URL, resolved against the page's. */
  url(node: ScrapeNode | undefined, attribute: string): string | undefined {
    const value = scrape.attribute(node, attribute, []);
    if (value === undefined) return undefined;
    try {
      return new URL(value, node?.ownerDocument?.baseURI).href;
    } catch {
      return undefined;
    }
  },

  /** The node's siblings as text, keeping line breaks and writing list items as `• ` lines. */
  lines(node: ScrapeNode | undefined, missing: readonly string[]): string | undefined {
    if (node === undefined) return undefined;
    let rendered = "";
    for (const sibling of siblings(node)) {
      const element = asElement(sibling);
      if (element === undefined) rendered += sibling.textContent ?? "";
      else if (element.tagName === "BR") rendered += "\n";
      else if (element.tagName === "UL") element.querySelectorAll("li").forEach((item) => (rendered += `\n• ${collapsed(item.textContent).trim()}`));
      else rendered += collapsed(element.textContent).trim();
    }
    const text = rendered.split("\n").map((line) => line.trim()).join("\n").replace(/\n{3,}/g, "\n\n");
    return present(text, missing);
  },

  /** The text `pattern` matches: its first group, or the whole match without groups. */
  regex(text: string | undefined, pattern: string): string | undefined {
    if (text === undefined) return undefined;
    const match = new RegExp(pattern).exec(text);
    if (match === null) return undefined;
    return present(match.length > 1 ? match[1] : match[0]);
  },

  removePrefix(text: string | undefined, prefix: string): string | undefined {
    return text === undefined ? undefined : present(text.startsWith(prefix) ? text.slice(prefix.length) : text);
  },

  removeSuffix(text: string | undefined, suffix: string): string | undefined {
    return text === undefined ? undefined : present(text.endsWith(suffix) ? text.slice(0, text.length - suffix.length) : text);
  },

  /** The text without `prefix` and `suffix`, when it has both. */
  unwrap(text: string | undefined, prefix: string, suffix: string): string | undefined {
    if (text === undefined) return undefined;
    const wrapped = text.length >= prefix.length + suffix.length && text.startsWith(prefix) && text.endsWith(suffix);
    return present(wrapped ? text.slice(prefix.length, text.length - suffix.length) : text);
  },

  nullIf(text: string | undefined, values: readonly string[]): string | undefined {
    return text === undefined || values.some((value) => sameText(value, text)) ? undefined : text;
  },

  /** The value of the query parameter `name` of the URL `text` holds. */
  queryParameter(text: string | undefined, name: string): string | undefined {
    if (text === undefined) return undefined;
    try {
      return present(new URL(text, "https://localhost").searchParams.get(name));
    } catch {
      return undefined;
    }
  },

  replace(text: string | undefined, replacements: Readonly<Record<string, string>>): string | undefined {
    if (text === undefined) return undefined;
    return Object.entries(replacements).find(([key]) => sameText(key, text))?.[1] ?? text;
  },

  equals(text: string | undefined, value: string): boolean | undefined {
    return text === undefined ? undefined : sameText(text, value);
  },

  contains(text: string | undefined, value: string): boolean | undefined {
    return text === undefined ? undefined : text.toLowerCase().includes(value.toLowerCase());
  },

  not(flag: boolean | undefined): boolean | undefined {
    return flag === undefined ? undefined : !flag;
  },

  /** `nodes`, or `undefined` when empty, so alternatives yielding elements can be chained with `??`. */
  nonEmpty(nodes: ScrapeNode[]): ScrapeNode[] | undefined {
    return nodes.length === 0 ? undefined : nodes;
  },

  /** `node` read with `read`, or `undefined` when there is no node. */
  map<T>(node: ScrapeNode | undefined, read: (node: ScrapeNode) => T | undefined): T | undefined {
    return node === undefined ? undefined : read(node);
  },

  /** The values that aren't missing. */
  compact<T>(values: ReadonlyArray<T | undefined>): T[] {
    return values.filter((value): value is T => value !== undefined);
  },

  integer(text: string | undefined): number | undefined {
    return decodeInteger(text);
  },

  number(text: string | undefined): number | undefined {
    return decodeNumber(text);
  },
};

/** A page that didn't hold what a scraper expected, usually because the site changed. */
export class ScrapeError extends Error {
  override readonly name = "ScrapeError";
}

/** Reads a value out of an HTML response with `read`, failing when it returns `undefined`. */
export async function scrapeResponse<T>(response: Response, read: (document: ScrapeNode) => T | undefined): Promise<T> {
  const value = read(scrape.document(await response.text(), response.url));
  if (value === undefined) throw new ScrapeError(`Unreadable page at ${response.url}`);
  return value;
}

/** Decodes the JSON a script response assigns to `variable` with `decode`. */
export async function scriptResponse<T>(response: Response, variable: string, decode: (json: unknown) => T | undefined): Promise<T> {
  const value = decode(scrape.scriptVariable(await response.text(), variable));
  if (value === undefined) throw new ScrapeError(`Missing script variable ${variable} at ${response.url}`);
  return value;
}
