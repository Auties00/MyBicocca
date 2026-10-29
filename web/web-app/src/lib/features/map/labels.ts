import { EasyStaffRoomEquipment } from '@mybicocca/data-api/easystaff';
import IconBarbell from '~icons/ph/barbell';
import IconBooks from '~icons/ph/books';
import IconBriefcase from '~icons/ph/briefcase';
import IconForkKnife from '~icons/ph/fork-knife';
import IconGraduationCap from '~icons/ph/graduation-cap';
import IconMapPin from '~icons/ph/map-pin';
import { romeTime } from '#lib/format/rome.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Icon } from '#lib/ui/types.ts';
import type { BuildingCategory } from './catalog.ts';
import type { RoomStatus } from './status.ts';

export const categoryLabels: Record<BuildingCategory, () => string> = {
	teaching: m.map_category_teaching,
	library: m.map_category_library,
	canteen: m.map_category_canteen,
	admin: m.map_category_admin,
	sport: m.map_category_sport,
	other: m.map_category_other
};

export const categoryIcons: Record<BuildingCategory, Icon> = {
	teaching: IconGraduationCap,
	library: IconBooks,
	canteen: IconForkKnife,
	admin: IconBriefcase,
	sport: IconBarbell,
	other: IconMapPin
};

export function floorLabel(floor: number | null): string {
	if (floor === null) return m.map_floor_other();
	return floor === 0 ? m.map_floor_ground() : m.map_floor_n({ floor });
}

/**
 * The status as a word ("Occupata") and, apart, until when ("fino alle 14:30"): a narrow room cell
 * puts them on two lines.
 */
export function statusParts(status: RoomStatus | undefined): { word: string; detail?: string } {
	if (status === undefined) return { word: m.map_room_checking() };
	switch (status.kind) {
		case 'free':
			return {
				word: m.map_room_free(),
				detail:
					status.until === null
						? m.map_room_rest_of_day()
						: m.map_room_until({ time: romeTime(status.until) })
			};
		case 'busy':
			return {
				word: m.map_room_busy(),
				detail: m.map_room_until({ time: romeTime(status.until) })
			};
		case 'closed':
			return {
				word: m.map_room_closed(),
				detail:
					status.until === null
						? m.map_room_closed_today()
						: m.map_room_until({ time: romeTime(status.until) })
			};
		case 'unknown':
			return { word: m.map_room_unknown() };
	}
}

const KINDS: Record<string, () => string> = {
	lezione: m.map_kind_lesson,
	esame: m.map_kind_exam,
	tutorato: m.map_kind_tutoring,
	riunione: m.map_kind_meeting,
	seminario: m.map_kind_seminar,
	altro: m.map_kind_other,
	'concorsi pubblici': m.map_kind_public_exam,
	'attività istituzionali': m.map_kind_institutional,
	laurea: m.map_kind_graduation,
	'sessione di laurea': m.map_kind_graduation,
	evento: m.map_kind_event
};

/** EasyStaff's kind of booking, in the app's language when it is a known one. */
export function kindLabel(kind: string): string {
	return KINDS[kind.toLowerCase()]?.() ?? kind;
}

const EQUIPMENT: Record<string, () => string> = {
	[EasyStaffRoomEquipment.FixedEquipment]: m.map_equipment_fixed,
	[EasyStaffRoomEquipment.MobileEquipment]: m.map_equipment_mobile,
	[EasyStaffRoomEquipment.TeacherDesk]: m.map_equipment_desk,
	[EasyStaffRoomEquipment.BlackoutWindows]: m.map_equipment_blackout,
	[EasyStaffRoomEquipment.TieredSeating]: m.map_equipment_tiered,
	[EasyStaffRoomEquipment.AudioSystem]: m.map_equipment_audio,
	[EasyStaffRoomEquipment.StandardVideoLessonKit]: m.map_equipment_video_kit,
	[EasyStaffRoomEquipment.FlipChart]: m.map_equipment_flip_chart,
	[EasyStaffRoomEquipment.Whiteboard]: m.map_equipment_whiteboard,
	[EasyStaffRoomEquipment.SlateBlackboard]: m.map_equipment_blackboard,
	[EasyStaffRoomEquipment.OverheadProjector]: m.map_equipment_overhead,
	[EasyStaffRoomEquipment.SlidingBlackboard]: m.map_equipment_sliding_board,
	[EasyStaffRoomEquipment.ExaminationBeds]: m.map_equipment_beds,
	[EasyStaffRoomEquipment.LcdPlasmaMonitor]: m.map_equipment_monitor,
	[EasyStaffRoomEquipment.TeacherPc]: m.map_equipment_teacher_pc,
	[EasyStaffRoomEquipment.Podium]: m.map_equipment_podium,
	[EasyStaffRoomEquipment.StudentPowerSocket]: m.map_equipment_power,
	[EasyStaffRoomEquipment.RecordingSystem]: m.map_equipment_recording,
	[EasyStaffRoomEquipment.WiredNetwork]: m.map_equipment_wired,
	[EasyStaffRoomEquipment.WifiNetwork]: m.map_equipment_wifi,
	[EasyStaffRoomEquipment.FixedChairsWithDesks]: m.map_equipment_chairs_desks,
	[EasyStaffRoomEquipment.FixedChairsWithTabletArms]: m.map_equipment_chairs_fixed_tablet,
	[EasyStaffRoomEquipment.MobileChairsWithTabletArms]: m.map_equipment_chairs_mobile_tablet,
	[EasyStaffRoomEquipment.MobileChairsWithoutTabletArms]: m.map_equipment_chairs_mobile,
	[EasyStaffRoomEquipment.AccessibleTable]: m.map_equipment_accessible_desk,
	[EasyStaffRoomEquipment.Camera]: m.map_equipment_camera,
	[EasyStaffRoomEquipment.VideoConference]: m.map_equipment_video_call,
	[EasyStaffRoomEquipment.CeilingProjectorAndScreen]: m.map_equipment_projector,
	[EasyStaffRoomEquipment.WebConference]: m.map_equipment_web_conference
};

/** A piece of equipment in the app's language; one EasyStaff adds later keeps its own name. */
export function equipmentLabel(name: string): string {
	return EQUIPMENT[name]?.() ?? name;
}
