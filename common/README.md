# Common

Everything here is shared by the Android, iOS and web apps.

- [`specs/`](specs) describes the university services the apps talk to, one folder per API.
- [`codegen/`](codegen) turns those descriptions into the Kotlin, Swift and TypeScript code the
  apps use.

## The APIs

| Folder               | Service                                                           |
|----------------------|-------------------------------------------------------------------|
| `esse3`              | Esse3: careers, exams, fees and self-declarations                  |
| `moodle`             | Moodle's web services and file downloads/uploads                  |
| `moodle-web`         | Moodle's pages: course catalog, video players, attendance         |
| `kaltura`            | Kaltura, which streams the lecture recordings                     |
| `easystaff-agenda`   | Agenda Web and EasyBadge: timetables, exams, rooms, attendance    |
| `easystaff-planning` | Portale Planning: appointments at the student desks               |
| `affluences`         | Affluences: library study room bookings                           |

Each folder has a `codegen.json`, which says how to read the spec and where to write the code,
plus the spec files themselves, and a `tests.yaml`, which says how to test the API against the
real service. Some APIs need a few extra files. Esse3, for example, keeps a glossary that gives
its Italian names readable English ones, and an `effects.json` saying what each of its writes
does.

## Changing an API

1. Edit the spec in `specs/<api>`. An operation that isn't a GET also needs its effect: whether
   it only reads, or changes something and how much.
2. Run the generator from `codegen/`:

   ```sh
   ../../android/gradlew :cli:run --args="specs/<api>"
   ```

3. Commit the spec and the generated code together. CI fails if they don't match.
4. Run the API's live tests, which the generator updated too, to see the change work against the
   real service.

See the [codegen README](codegen) for everything a spec can describe, and how the live tests
work.
