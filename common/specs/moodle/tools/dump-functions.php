<?php
// Dumps the parameters and return values of Moodle web service functions as JSON, from a
// Moodle source tree, for the codegen's moodle reader. No database or Moodle install is
// needed: functions are found in the db/services.php files and described by Moodle itself.
//
// Usage: php dump-functions.php <moodle-source-dir> <functions.txt> <output.json>
//
// functions.txt lists one function name per line; blank lines and lines starting with # are
// ignored. The output describes each function in that order (see dump_describe()).

if ($argc !== 4) {
    fwrite(STDERR, "Usage: php dump-functions.php <moodle-source-dir> <functions.txt> <output.json>\n");
    exit(2);
}
[, $moodle, $listfile, $outfile] = $argv;
$moodle = realpath($moodle);

define('CLI_SCRIPT', true);
define('ABORT_AFTER_CONFIG', true);
define('CACHE_DISABLE_ALL', true);
define('MOODLE_INTERNAL', true);

// The settings lib/setup.php reads from config.php. Nothing connects to them.
global $CFG;
$CFG = new stdClass();
$CFG->dirroot = $moodle;
$CFG->wwwroot = 'https://moodle.invalid';
$CFG->dataroot = sys_get_temp_dir() . '/moodle-dump-data';
$CFG->admin = 'admin';
$CFG->dbtype = 'pgsql';
$CFG->prefix = 'mdl_';
$CFG->directorypermissions = 0777;
// Report problems: setup.php sets error reporting from these.
$CFG->debug = E_ALL;
$CFG->debugdisplay = 1;
// Site settings some descriptions read.
$CFG->lang = 'en';
$CFG->country = '';
$CFG->defaultcity = '';
$CFG->calendartype = 'gregorian';
// Moodle's install defaults of the user preferences the user descriptions default to.
$CFG->defaultpreference_maildisplay = 2;
$CFG->defaultpreference_mailformat = 1;
$CFG->defaultpreference_maildigest = 0;
$CFG->defaultpreference_autosubscribe = 1;
$CFG->defaultpreference_trackforums = 0;
@mkdir($CFG->dataroot, 0777, true);
require($moodle . '/lib/setup.php');

// What lib/setup.php would load after the configuration, minus what needs a database.
foreach (['setuplib', 'dmllib', 'weblib', 'moodlelib', 'externallib', 'accesslib', 'datalib', 'filelib', 'grouplib', 'enrollib', 'modinfolib', 'blocklib'] as $lib) {
    require_once($CFG->libdir . "/$lib.php");
}
// The front page course, which setup.php reads from the database; it is always course 1.
define('SITEID', 1);

// A few descriptions look settings up in the database, such as the custom profile fields;
// with no database there are none, so every query finds nothing.
global $DB;
$DB = new class {
    public function __call(string $name, array $arguments) {
        return str_starts_with($name, 'get_records') || str_starts_with($name, 'get_fieldset') ? [] : false;
    }
};

use core_external\external_api;
use core_external\external_description;
use core_external\external_multiple_structure;
use core_external\external_single_structure;
use core_external\external_value;

$wanted = array_values(array_filter(array_map('trim', file($listfile)), fn($line) => $line !== '' && $line[0] !== '#'));

// Every function definition, by name, with the component that declares it.
$definitions = [];
$servicefiles = array_merge(['core' => $CFG->libdir . '/db/services.php'], dump_service_files());
foreach ($servicefiles as $component => $file) {
    $functions = (static function (string $file) {
        $functions = [];
        include($file);
        return $functions;
    })($file);
    foreach ($functions as $name => $definition) {
        $definitions[$name] = (object) array_merge(
            ['methodname' => 'execute', 'classpath' => null],
            $definition,
            ['name' => $name, 'component' => $definition['component'] ?? $component],
        );
    }
}

$output = [
    'moodle' => trim(explode('(', dump_release())[0]),
    'functions' => [],
];
$failures = [];
foreach ($wanted as $name) {
    if (!isset($definitions[$name])) {
        $failures[] = "$name: no such function";
        continue;
    }
    try {
        $info = external_api::external_function_info($definitions[$name]);
        $output['functions'][] = [
            'name' => $name,
            'component' => $info->component === 'core' ? dump_component_of($name) : $info->component,
            'type' => $info->type ?? 'read',
            'loginrequired' => (bool) ($definitions[$name]->loginrequired ?? true),
            'description' => dump_text($info->description ?? ''),
            'parameters' => dump_describe($info->parameters_desc),
            'returns' => $info->returns_desc === null ? null : dump_describe($info->returns_desc),
        ];
    } catch (Throwable $e) {
        $failures[] = "$name: " . get_class($e) . ': ' . $e->getMessage();
    }
}

file_put_contents($outfile, json_encode($output, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE) . "\n");
fwrite(STDERR, count($output['functions']) . " functions written to $outfile\n");
if ($failures) {
    fwrite(STDERR, "Failed:\n  " . implode("\n  ", $failures) . "\n");
    exit(1);
}

/**
 * A description as JSON:
 * - a value: {"kind": "value", "type": "PARAM_INT", ...}
 * - an object: {"kind": "object", "keys": {"name": <description>, ...}, ...}
 * - a list: {"kind": "list", "content": <description>, ...}
 * Every kind also has "presence" (required, optional or default), "nullable" and
 * "description"; a value or list with presence "default" has "default".
 */
function dump_describe(external_description $description): array {
    $common = [
        'presence' => match ($description->required) {
            VALUE_REQUIRED => 'required',
            VALUE_OPTIONAL => 'optional',
            VALUE_DEFAULT => 'default',
        },
        'nullable' => (bool) $description->allownull,
        'description' => dump_text($description->desc ?? ''),
    ];
    if ($description instanceof external_value) {
        $result = ['kind' => 'value', 'type' => $description->type] + $common;
    } else if ($description instanceof external_single_structure) {
        $keys = [];
        foreach ($description->keys as $key => $value) {
            $keys[$key] = dump_describe($value);
        }
        $result = ['kind' => 'object', 'keys' => (object) $keys] + $common;
    } else if ($description instanceof external_multiple_structure) {
        $result = ['kind' => 'list', 'content' => dump_describe($description->content)] + $common;
    } else {
        throw new coding_exception('Unknown description ' . get_class($description));
    }
    if ($description->required === VALUE_DEFAULT) {
        $result['default'] = $description->default;
    }
    return $result;
}

/** The db/services.php file of every plugin that has one, by component. */
function dump_service_files(): array {
    $files = [];
    foreach (core_component::get_plugin_types() as $type => $unused) {
        foreach (core_component::get_plugin_list($type) as $plugin => $dir) {
            if (is_readable("$dir/db/services.php")) {
                $files["{$type}_{$plugin}"] = "$dir/db/services.php";
            }
        }
    }
    return $files;
}

/** The component of a core function, from its name: core_course_get_contents is core_course. */
function dump_component_of(string $function): string {
    $parts = explode('_', $function);
    return $parts[0] . '_' . $parts[1];
}

/** Descriptions on one line, without the markup some of them carry. */
function dump_text(string $text): string {
    return trim(preg_replace('/\s+/', ' ', strip_tags($text)));
}

function dump_release(): string {
    global $CFG;
    $release = null;
    $version = null;
    include($CFG->dirroot . '/version.php');
    return $release;
}
