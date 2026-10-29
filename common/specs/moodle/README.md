# Moodle web services

Every function of Moodle's official mobile service (the service the apps' tokens belong to), read
by the codegen's `moodle` reader:

| File             | What it holds                                                                 |
|------------------|-------------------------------------------------------------------------------|
| `functions.txt`  | Adjustments to that set: a name adds a function, `-name` leaves one out.      |
| `functions.json` | Their parameters and results, dumped from Moodle's source (don't edit).       |
| `effects.json`   | What each write does, and the reads that change state anyway.                 |
| `modules.json`   | Module names by Moodle component (`core_course` → `Course`).                  |
| `types.json`     | Type names by the path of a structure (`core_course_get_contents/returns[]`). |
| `names.json`     | Member names by key, since Moodle runs words together (`timemodified`).       |

The overlays are synced by every codegen run: new keys get an empty value, meaning the reader
derives a name, and keys the dump no longer has are dropped.

## Updating the dump

The dump must come from the Moodle release the site runs, since releases add and remove
fields. The site reports it in `core_webservice_get_site_info` (`release`, e.g. `4.5.12+`);
use the matching tag of [moodle/moodle](https://github.com/moodle/moodle):

```sh
curl -L -o moodle.zip https://codeload.github.com/moodle/moodle/zip/refs/tags/v4.5.12
unzip -q moodle.zip
php tools/dump-functions.php moodle-4.5.12 functions.txt functions.json
```

The script needs PHP 8.1 or later with the extensions Moodle requires (`intl`, `mbstring`,
`curl`, …), but no database or Moodle install: it reads the functions from the
`db/services.php` files and has Moodle describe them. A function the new release adds needs an
effect in `effects.json` unless Moodle marks it as a read: generation fails until it has one. Then
regenerate from `common/codegen`.

Functions that work without a token, such as `tool_mobile_get_public_config`, are served by
Moodle's AJAX endpoint rather than the REST server, so they stay hand-written.
