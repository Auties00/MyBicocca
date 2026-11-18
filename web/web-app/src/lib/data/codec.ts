import { LocalDate, LocalDateTime, LocalTime } from '@mybicocca/data-api/runtime';

/**
 * Turns values into JSON text and back without losing what JSON can't hold: the SDK's date and
 * time classes, `Date`s and bytes come back as instances, not as the strings their `toJSON` gives.
 * Everything persisted (query cache, vault) goes through here.
 */
export function serialize(value: unknown): string {
	return JSON.stringify(tag(value));
}

export function deserialize<T>(text: string): T {
	return untag(JSON.parse(text)) as T;
}

const TAG = '#t';
const VALUE = '#v';

function tag(value: unknown): unknown {
	if (value === null || typeof value !== 'object') return value;
	if (value instanceof LocalDateTime) return { [TAG]: 'LocalDateTime', [VALUE]: value.toString() };
	if (value instanceof LocalDate) return { [TAG]: 'LocalDate', [VALUE]: value.toString() };
	if (value instanceof LocalTime) return { [TAG]: 'LocalTime', [VALUE]: value.toString() };
	if (value instanceof Date) return { [TAG]: 'Date', [VALUE]: value.getTime() };
	if (value instanceof Uint8Array) return { [TAG]: 'Bytes', [VALUE]: toBase64(value) };
	if (Array.isArray(value)) return value.map(tag);
	const object: Record<string, unknown> = {};
	for (const [key, item] of Object.entries(value)) {
		if (item !== undefined) object[key] = tag(item);
	}
	// A plain object that happens to use the tag key is wrapped, so it can't pass for a tagged one.
	return TAG in object ? { [TAG]: 'Object', [VALUE]: object } : object;
}

function untag(value: unknown): unknown {
	if (value === null || typeof value !== 'object') return value;
	if (Array.isArray(value)) return value.map(untag);
	const object = value as Record<string, unknown>;
	const kind = object[TAG];
	const inner = object[VALUE];
	switch (kind) {
		case 'LocalDateTime':
			return LocalDateTime.parse(String(inner));
		case 'LocalDate':
			return LocalDate.parse(String(inner));
		case 'LocalTime':
			return LocalTime.parse(String(inner));
		case 'Date':
			return new Date(Number(inner));
		case 'Bytes':
			return fromBase64(String(inner));
		case 'Object':
			return untagEntries(inner as Record<string, unknown>);
		default:
			return untagEntries(object);
	}
}

function untagEntries(object: Record<string, unknown>): Record<string, unknown> {
	const result: Record<string, unknown> = {};
	for (const [key, item] of Object.entries(object)) result[key] = untag(item);
	return result;
}

function toBase64(bytes: Uint8Array): string {
	let binary = '';
	for (let start = 0; start < bytes.length; start += 0x8000) {
		binary += String.fromCharCode(...bytes.subarray(start, start + 0x8000));
	}
	return btoa(binary);
}

function fromBase64(text: string): Uint8Array<ArrayBuffer> {
	const binary = atob(text);
	const bytes = new Uint8Array(binary.length);
	for (let index = 0; index < binary.length; index++) bytes[index] = binary.charCodeAt(index);
	return bytes;
}
