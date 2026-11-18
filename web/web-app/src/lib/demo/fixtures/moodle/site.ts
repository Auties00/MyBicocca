import { DemoStudent } from '../../persona.ts';
import { json, route } from '../../router.ts';

const SERVER = 'https://elearning.unimib.it/webservice/rest/server.php';

export const routes = [
	route(`POST ${SERVER}?wsfunction=core_webservice_get_site_info`, ({ url }) =>
		json({
			sitename: 'e-Learning - UNIMIB',
			username: DemoStudent.username,
			firstname: DemoStudent.firstName,
			lastname: DemoStudent.lastName,
			fullname: DemoStudent.fullName,
			lang: url.searchParams.get('moodlewssettinglang') ?? 'it',
			userid: DemoStudent.moodle.userId,
			siteurl: 'https://elearning.unimib.it',
			userpictureurl: 'https://elearning.unimib.it/theme/image.php/boost/core/1/u/f1',
			functions: [],
			downloadfiles: 1,
			uploadfiles: 1,
			release: '4.5.4 (Build: 20250414)',
			version: '2024100704',
			usercanmanageownfiles: true,
			userquota: DemoStudent.moodle.quotaBytes,
			usermaxuploadfilesize: DemoStudent.moodle.maxUploadBytes,
			userhomepage: 1,
			siteid: 1,
			sitecalendartype: 'gregorian',
			usercalendartype: 'gregorian',
			userissiteadmin: false,
			theme: 'boost',
			limitconcurrentlogins: 0,
			policyagreed: 1
		})
	)
];
