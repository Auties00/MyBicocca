import { m } from '#lib/paraglide/messages.js';

/** What a submission holds, in words for the confirmations: "2 file e il testo". */
export function recapOf(parts: { files: readonly unknown[]; text: string | null }): string {
	const count = parts.files.length;
	const text = parts.text !== null && parts.text.trim() !== '';
	if (count > 0 && text) {
		return m.assignment_recap_files_text({ files: m.assignment_recap_files({ count }) });
	}
	if (count > 0) return m.assignment_recap_files({ count });
	return text ? m.assignment_recap_text() : m.assignment_recap_fallback();
}
