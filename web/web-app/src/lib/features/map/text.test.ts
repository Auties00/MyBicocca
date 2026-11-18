import { describe, expect, it } from 'vitest';
import { parseRoomFloor, plainText, roomType } from './text.ts';

describe('parseRoomFloor', () => {
	it('reads numbers, with or without "Piano"', () => {
		expect(parseRoomFloor('1')).toBe(1);
		expect(parseRoomFloor('-1')).toBe(-1);
		expect(parseRoomFloor('Piano -1')).toBe(-1);
		expect(parseRoomFloor(' piano 2 ')).toBe(2);
	});

	it('reads every spelling of the ground floor', () => {
		for (const text of ['T', 'PT', 'Terra', 'piano terra', 'Rialzato']) {
			expect(parseRoomFloor(text)).toBe(0);
		}
	});

	it('gives no floor for anything else', () => {
		expect(parseRoomFloor(null)).toBeNull();
		expect(parseRoomFloor('')).toBeNull();
		expect(parseRoomFloor('Seminterrato')).toBeNull();
		expect(parseRoomFloor('1-2')).toBeNull();
	});
});

describe('plainText', () => {
	it('keeps the line structure of text and markup', () => {
		expect(plainText('Posti pre-covid: 130\r\nCoeff. utilizzo: 50%')).toBe(
			'Posti pre-covid: 130\nCoeff. utilizzo: 50%'
		);
		expect(plainText('Ingresso da via Cozzi<br>Ascensore &egrave; al piano')).toBe(
			'Ingresso da via Cozzi\nAscensore è al piano'
		);
		expect(plainText('Dotazioni:<ul><li>Rampa</li><li> Bagno&nbsp;accessibile </li></ul>')).toBe(
			'Dotazioni:\n• Rampa\n• Bagno accessibile'
		);
	});

	it('gives no text for blanks and the registry placeholder', () => {
		expect(plainText(null)).toBeNull();
		expect(plainText('  <p> </p> ')).toBeNull();
		expect(plainText('Dato mancante')).toBeNull();
	});
});

describe('roomType', () => {
	it('keeps real categories and drops placeholders and seat brackets', () => {
		expect(roomType('Laboratorio informatico')).toBe('Laboratorio informatico');
		expect(roomType('non definito')).toBeNull();
		expect(roomType('126-142')).toBeNull();
		expect(roomType('')).toBeNull();
		expect(roomType(null)).toBeNull();
	});
});
