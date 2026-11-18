/** A page of a login chain, parsed so its relative links resolve against `url`. */
export async function readPage(response: Response): Promise<Document> {
	const document = new DOMParser().parseFromString(await response.text(), 'text/html');
	const base = document.createElement('base');
	base.href = response.url;
	document.head.prepend(base);
	return document;
}

/** Where `form` posts to: its absolute action, else the page it is on. */
export function formAction(form: HTMLFormElement, document: Document): string {
	const action = form.getAttribute('action');
	return action === null ? document.baseURI : new URL(action, document.baseURI).href;
}

/**
 * The fields a browser would submit for `form`, as jsoup's `FormElement.formData()` reads them:
 * named, enabled controls, checkboxes and radios only when checked.
 */
export function formFields(form: HTMLFormElement): Array<[string, string]> {
	const fields: Array<[string, string]> = [];
	for (const control of form.querySelectorAll<HTMLElement>('input, select, textarea')) {
		const name = control.getAttribute('name');
		if (name === null || name === '' || control.hasAttribute('disabled')) continue;
		if (control instanceof HTMLSelectElement) {
			const selected = [...control.options].filter((option) => option.selected);
			const values =
				selected.length > 0 || control.multiple ? selected : [...control.options].slice(0, 1);
			for (const option of values) fields.push([name, option.value]);
		} else if (control instanceof HTMLTextAreaElement) {
			fields.push([name, control.value]);
		} else {
			const type = (control.getAttribute('type') ?? 'text').toLowerCase();
			if (type === 'button' || type === 'image') continue;
			if ((type === 'checkbox' || type === 'radio') && !control.hasAttribute('checked')) continue;
			fields.push([name, control.getAttribute('value') ?? (type === 'checkbox' ? 'on' : '')]);
		}
	}
	return fields;
}

/** Posts `fields` as a urlencoded form, returning redirects instead of following them. */
export function postForm(
	send: typeof fetch,
	url: string,
	fields: ReadonlyArray<readonly [string, string]>
): Promise<Response> {
	return send(url, {
		method: 'POST',
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
		body: new URLSearchParams(fields.map(([name, value]) => [name, value])),
		redirect: 'manual'
	});
}
