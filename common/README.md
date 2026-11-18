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
plus the spec files themselves. Some APIs need a few extra files. Esse3, for example, keeps a
glossary that gives its Italian names readable English ones.

## Changing an API

1. Edit the spec in `specs/<api>`.
2. Run the generator from `codegen/`:

   ```sh
   ../../android/gradlew :cli:run --args="specs/<api>"
   ```

3. Commit the spec and the generated code together. CI fails if they don't match.

See the [codegen README](codegen) for everything a spec can describe.
