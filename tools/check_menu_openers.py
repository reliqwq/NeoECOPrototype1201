"""Every menu this line teaches AE2 to open must hand the server-side menu its locator.

Why this exists: AE2's priority tab does not draw anything itself - the client sends
SwitchGuisPacket.openSubMenu(PriorityMenu.TYPE), and the server handler is

    if (player.containerMenu instanceof AEBaseMenu menu) {
        var locator = menu.getLocator();
        if (locator == null) return;          // <- no log, no message, no error
        MenuOpener.open(PriorityMenu.TYPE, player, locator);
    }

(both readings taken from appliedenergistics2-forge-15.4.10_mapped_official_1.20.1.jar with javap:
SwitchGuisPacket.doOpenSubMenu, and MenuTypeBuilder, whose single setLocator call site is its
server-side menu factory - AE2 never sets it on the client instance).

On 1.21.1 AE2 builds our menu types through MenuTypeBuilder, so that line runs for us. On 1.20.1 the
builder can only name entries inside ae2:, so the type is built by Forge and re-taught to MenuOpener by
hand in Prototype1201 - and an opener that forgets setLocator ships a machine whose GUI opens fine,
whose slots all work, and whose wrench button does nothing at all. That is silent in every log.

Run:  python tools/check_menu_openers.py
Exit 0 = every addOpener covers its locator. Exit 1 = the printed lines name the menu type missing it.
"""
import os
import re
import sys

SRC = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "java")
problems = []


def lambda_body(text, start):
    """The text of the balanced {...} that follows `start`, or None if the braces never close."""
    opened = text.find("{", start)
    if opened < 0:
        return None
    depth = 0
    for i in range(opened, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return text[opened:i + 1]
    return None


def code_only(text):
    """Drop comments, because the rule has to read the code that runs, not the prose about it.

    This is not a Java parser: a "//" inside a string literal would be eaten too. Nothing in this line
    puts one there, and the only thing read afterwards is brace structure and one method name.
    """
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    return re.sub(r"//[^\n]*", "", text)


def check_file(path):
    with open(path, encoding="utf-8") as handle:
        text = code_only(handle.read())
    for match in re.finditer(r"MenuOpener\.addOpener\(", text):
        body = lambda_body(text, match.end())
        if body is None:
            problems.append(f"{path}: an addOpener whose lambda braces never balance - the rule cannot read it")
            continue
        # Name the menu so a failure points at a machine, not at a line number that drifts. The type is
        # usually held in a local assigned just above the call, so look both inside and behind.
        named = (re.search(r"\b([A-Z0-9_]+_MENU)\b", body)
                 or re.search(r"\b([A-Z0-9_]+_MENU)\b", text[max(0, match.start() - 400):match.end()]))
        label = named.group(1) if named else "an unnamed menu type"
        if "setLocator(" not in body:
            problems.append(f"{path}: {label} is opened without setLocator(locator) - its AE2 sub-menus "
                            f"(the priority tab above all) will be dead buttons with no log line")
        elif "setLocator(locator)" not in body:
            problems.append(f"{path}: {label} calls setLocator but not with the locator it was handed - "
                            f"the sub-menu packet will not find the machine")


def main():
    for root, _, files in os.walk(SRC):
        for name in files:
            if name.endswith(".java"):
                check_file(os.path.join(root, name).replace(os.sep, "/"))
    for line in problems:
        print("FAIL " + line)
    if problems:
        print(f"{len(problems)} opener(s) would ship a silent sub-menu")
        return 1
    print("every MenuOpener.addOpener in this line sets its server-side locator")
    return 0


if __name__ == "__main__":
    sys.exit(main())
