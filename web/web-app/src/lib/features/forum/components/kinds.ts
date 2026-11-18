import IconChats from '~icons/ph/chats';
import IconChatsCircle from '~icons/ph/chats-circle';
import IconMegaphone from '~icons/ph/megaphone-simple';
import IconNotebook from '~icons/ph/notebook';
import IconQuestion from '~icons/ph/question';
import IconUser from '~icons/ph/user';
import { m } from '#lib/paraglide/messages.js';
import type { Icon } from '#lib/ui/types.ts';
import type { ForumKind } from '../forum.ts';

export const kindLabel: Readonly<Record<ForumKind, () => string>> = {
	news: m.forum_kind_news,
	general: m.forum_kind_general,
	qanda: m.forum_kind_qanda,
	eachuser: m.forum_kind_eachuser,
	single: m.forum_kind_single,
	blog: m.forum_kind_blog,
	other: m.forum_kind_other
};

export const kindIcon: Readonly<Record<ForumKind, Icon>> = {
	news: IconMegaphone,
	general: IconChats,
	qanda: IconQuestion,
	eachuser: IconUser,
	single: IconChatsCircle,
	blog: IconNotebook,
	other: IconChats
};
