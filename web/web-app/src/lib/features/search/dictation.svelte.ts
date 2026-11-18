import { intlLocale } from '#lib/format/locale.ts';

/** The part of the Web Speech API dictation needs; Chrome and Safari still prefix it. */
interface Recognition {
	lang: string;
	interimResults: boolean;
	onresult: ((event: SpeechRecognitionEvent) => void) | null;
	onend: (() => void) | null;
	start(): void;
	stop(): void;
	abort(): void;
}

type RecognitionClass = new () => Recognition;

function recognitionClass(): RecognitionClass | undefined {
	const scope = window as unknown as Partial<
		Record<'SpeechRecognition' | 'webkitSpeechRecognition', RecognitionClass>
	>;
	return scope.SpeechRecognition ?? scope.webkitSpeechRecognition;
}

/** Android's voice search: speech typed into the search field, where the browser can do it. */
export class Dictation {
	readonly supported = recognitionClass() !== undefined;
	listening = $state(false);
	#recognition: Recognition | undefined;

	/** Streams what is heard into `write`, the guesses included, until a pause ends it. */
	start(write: (text: string) => void): void {
		const Recognition = recognitionClass();
		if (Recognition === undefined || this.#recognition) return;
		const recognition = new Recognition();
		recognition.lang = intlLocale();
		recognition.interimResults = true;
		recognition.onresult = (event) =>
			write(Array.from(event.results, (result) => result[0].transcript).join(''));
		const ended = () => {
			this.#recognition = undefined;
			this.listening = false;
		};
		recognition.onend = ended;
		this.#recognition = recognition;
		this.listening = true;
		try {
			recognition.start();
		} catch {
			ended();
		}
	}

	/** Keeps what was heard so far. */
	stop(): void {
		this.#recognition?.stop();
	}

	/** Drops what wasn't final yet. */
	cancel(): void {
		this.#recognition?.abort();
	}
}
