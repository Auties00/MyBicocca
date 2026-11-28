"""Checks that Compass's sign-up page still looks the way the app's in-app sign-up expects.

The Mensa sign-up drives these pages over HTTP from its own form (CompassSignUpClient in data-api)
and falls back to showing them in a web view (MensaRegisterPage.kt), which reads the email and
password fields, hides the site's header and footer and restyles the form. None of that has an API
contract, so this check fetches the page the same way and fails when something the app relies on
is gone.

Standard library only. Exit code 0 when everything is in place, 1 otherwise (with the reasons).
"""
import re
import sys
import urllib.request
from http.cookiejar import CookieJar
from html.parser import HTMLParser

ORIGIN = "https://unibicocca.compasscloud.it"
APP_ENTRY = ORIGIN + "/api/apps/v1/mylunch/auth/getToken"
REGISTER = ORIGIN + "/area-riservata/register"
UA = "Mozilla/5.0 (Linux; Android 14; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/133.0 Mobile Safari/537.36"


class Page(HTMLParser):
    def __init__(self):
        super().__init__()
        self.classes = set()
        self.tags_with_class = set()
        self.form_action = None
        self.inputs = set()
        self._in_form = False

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        for c in (a.get("class") or "").split():
            self.classes.add(c)
            self.tags_with_class.add(f"{tag}.{c}")
        if tag == "form" and "registration_form" in (a.get("class") or ""):
            self.form_action = a.get("action")
            self._in_form = True
        if self._in_form and tag in ("input", "select", "textarea") and a.get("name"):
            self.inputs.add(a["name"])

    def handle_endtag(self, tag):
        if tag == "form":
            self._in_form = False


def main() -> int:
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(CookieJar()))
    opener.addheaders = [("User-Agent", UA), ("Accept-Language", "it-IT,it;q=0.9")]
    opener.open(APP_ENTRY, timeout=30).read()
    response = opener.open(REGISTER, timeout=30)
    html = response.read().decode("utf-8", "replace")
    problems = []

    if "/area-riservata/regist" not in response.geturl():
        problems.append(f"the app entry no longer unlocks the form: {REGISTER} redirected to {response.geturl()}")

    page = Page()
    page.feed(html)
    if page.form_action is None:
        problems.append("no <form class=registration_form> on the page")
    elif "/area-riservata/registration" not in page.form_action:
        problems.append(f"the form now posts to {page.form_action}")

    # The auto sign-in after activation reads these two.
    for field in ("email", "password"):
        if field not in page.inputs:
            problems.append(f"the form has no field named {field!r} (found {sorted(page.inputs)})")

    # Hidden around the form, and restyled inside it.
    for hook in ("header.header", "footer.footer", "div.frontend_header", "input.form-control",
                 "span.input-group-addon", "div.bs-callout"):
        if hook not in page.tags_with_class:
            problems.append(f"no element matches {hook}")

    # The native form also reads the email check's key and shows the captcha image.
    if not re.search(r"check_email'\s*,\s*\{\s*email\s*:\s*email\s*,\s*v\s*:\s*'[^']+'", html):
        problems.append("the email check's key is no longer in the page")
    captcha = opener.open(ORIGIN + "/api/utilities/captcha", timeout=30)
    if not captcha.headers.get("Content-Type", "").startswith("image/"):
        problems.append(f"the captcha is no longer an image ({captcha.headers.get('Content-Type')})")

    if problems:
        print("Compass's sign-up page changed; the in-app sign-up needs a look:")
        for p in problems:
            print(" -", p)
        return 1
    print(f"Sign-up page as expected: form posts to {page.form_action}, fields {sorted(page.inputs)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
