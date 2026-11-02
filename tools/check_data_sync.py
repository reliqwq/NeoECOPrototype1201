"""Check the ported data (advancements + item tags) against what this line actually registers.

Why this exists: the tree came from the 1.21.1 line, and every way it can drift is silent at runtime -
a lang key that no file has renders as the raw key, an item id that is not registered simply never
awards, and a tag whose members are all "required": false can be empty without any complaint at all.

Run:  python tools/check_data_sync.py
Exit 0 = every reference resolves. Exit 1 = the printed lines name what does not.
"""
import json
import os
import re
import sys

NS = "neoecoprototype1201"
RES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources")
ADV_DIR = os.path.join(RES, "data", NS, "advancements")
TAG_DIR = os.path.join(RES, "data", NS, "tags", "items")
LANG_DIR = os.path.join(RES, "assets", NS, "lang")
ASSET_DIR = os.path.join(RES, "assets", NS)

problems = []


def registered_ids():
    """Ids this line ships, read from its own lang file: a block or item without a name is already a bug."""
    ids = set()
    for loc in ("zh_cn", "en_us"):
        for key in json.load(open(os.path.join(LANG_DIR, loc + ".json"), encoding="utf-8")):
            m = re.match(r"^(?:block|item)\." + NS + r"\.([a-z0-9_]+)$", key)
            if m:
                ids.add(NS + ":" + m.group(1))
    return ids


OURS = registered_ids()


def integration_registers():
    """Registration calls living under integration/**, which only run when an optional mod is loaded.

    Two shapes: a whole id in one literal, and a literal prefix the code concatenates a size onto. The
    subfolder names the mod that gates it, which is what the recipe conditions have to agree with.
    """
    root = os.path.join(os.path.dirname(RES), "java")
    exact, prefixes = {}, {}
    for folder, _, files in os.walk(root):
        parts = folder.split(os.sep)
        if "integration" not in parts:
            continue
        index = parts.index("integration")
        if index + 1 >= len(parts):
            # Files sitting directly under integration/ name no mod, so nothing registered there could be
            # gated by one. Skip rather than invent an id: an invented one would pass the condition rule.
            continue
        modid = parts[index + 1]
        for name in files:
            if not name.endswith(".java"):
                continue
            text = open(os.path.join(folder, name), encoding="utf-8").read()
            for found in re.finditer(r'\.register\(\s*"([a-z0-9_]+)"(\s*\+)?', text):
                (prefixes if found.group(2) else exact)[found.group(1)] = modid
    return exact, prefixes


EXACT, PREFIXES = integration_registers()


def gated_by(member):
    """The mod id a member needs, or None when this line registers it unconditionally."""
    path = member.split(":", 1)[1]
    if path in EXACT:
        return EXACT[path]
    for prefix, modid in PREFIXES.items():
        if path.startswith(prefix):
            return modid
    return None


def is_gated(member):
    return gated_by(member) is not None

# ids proven by the recipe files this line ships (recipes/ plus recipes/disassembly/), which the game
# loads clean: every ingredient and output in them resolves.
PROVEN = set()
RECIPE_FILES = 0
for here, _, files in os.walk(os.path.join(RES, "data", NS, "recipes")):
    for name in files:
        if not name.endswith(".json"):
            continue
        RECIPE_FILES += 1
        text = open(os.path.join(here, name), encoding="utf-8").read()
        PROVEN |= set(re.findall(r'"([a-z0-9_.-]+:[a-z0-9_./-]+)"', text))

LANG_KEYS = {}
for loc in ("zh_cn", "en_us"):
    LANG_KEYS[loc] = set(json.load(open(os.path.join(LANG_DIR, loc + ".json"), encoding="utf-8")))


def check_id(where, raw):
    if not re.match(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$", raw):
        problems.append(f"{where}: not an id: {raw!r}")
    elif raw.startswith(NS + ":") and raw not in OURS and raw not in PROVEN:
        problems.append(f"{where}: {raw} is not registered on this line")


def check_texture(where, raw):
    if not raw.startswith(NS + ":"):
        return
    path = os.path.join(ASSET_DIR, raw.split(":", 1)[1])
    if not os.path.isfile(path):
        problems.append(f"{where}: texture file missing: {raw}")


def check_node(where, node):
    # Forge 1.20.1 does not read an inline condition key on an advancement: it reads the wrapper shape
    # {"advancements":[{"conditions":[...],"advancement":{...}}]} (ConditionalAdvancement.processConditional,
    # the only caller of the "advancements"/"conditions"/"advancement" strings). A file that arrives from
    # 1.21.1 with "neoforge:conditions" therefore loads unconditionally here, silently: the node appears in
    # the tree and its criterion names an item that is not registered when the optional mod is absent.
    for key in list(node):
        if key.startswith("neoforge:"):
            problems.append(f"{where}: {key} is 1.21's inline condition key - Forge 1.20.1 ignores it, so "
                            f"the node loads unconditionally; wrap it in "
                            f"\"advancements\": [{{\"conditions\": [...], \"advancement\": ...}}]")
    if "advancements" in node:
        for i, entry in enumerate(node["advancements"]):
            for cond in entry.get("conditions", []):
                cond_type = cond.get("type", "")
                if cond_type and not cond_type.startswith("forge:"):
                    problems.append(f"{where}[{i}]: not a Forge 1.20.1 condition type: {cond_type}")
            check_node(where + "[" + str(i) + "]", entry.get("advancement", {}))
        return
    for key, value in walk_strings(node):
        if key == "translate":
            for loc in LANG_KEYS:
                if value not in LANG_KEYS[loc]:
                    problems.append(f"{where}: lang key absent from {loc}.json: {value}")
        elif key in ("icon", "item") and ":" in value:
            check_id(where + "." + key, value)
        elif key == "background":
            check_texture(where, value)
    icon = node.get("display", {}).get("icon", {})
    if "id" in icon:
        problems.append(f"{where}: display.icon still uses 1.21's 'id'; Forge 1.20.1 reads 'item'")
    parent = node.get("parent")
    if parent and parent.startswith(NS + ":"):
        target = parent.split(":", 1)[1]
        if not os.path.isfile(os.path.join(ADV_DIR, target + ".json")):
            problems.append(f"{where}: parent {parent} is not among the ported files")
    for crit_name, crit in node.get("criteria", {}).items():
        for entry in crit.get("conditions", {}).get("items", []):
            if "items" in entry:
                if not entry["items"]:
                    problems.append(f"{where}/{crit_name}: criterion matches no item at all")
                for value in entry["items"]:
                    check_id(f"{where}/{crit_name}", value)
            elif "tag" in entry:
                tag_file = os.path.join(TAG_DIR, entry["tag"].split(":", 1)[1] + ".json")
                if not os.path.isfile(tag_file):
                    problems.append(f"{where}/{crit_name}: tag file missing: {entry['tag']}")


def walk_strings(value, key=""):
    if isinstance(value, dict):
        for k, v in value.items():
            if isinstance(v, str):
                yield k, v
            else:
                yield from walk_strings(v, k)
    elif isinstance(value, list):
        for v in value:
            yield from walk_strings(v, key)
    elif isinstance(value, str):
        yield key, value


GUIDE_DIR = os.path.join(RES, "assets", NS, "ae2guide")
FOREIGN_NS = re.compile(r"(?<!1201)\bneoecoprototype[:.]")


def check_pages():
    """Every id, page link and scene a guide page names has to exist here, in both languages."""
    langs = {}
    for sub, folder in (("", os.path.join(GUIDE_DIR, NS + "_intro")),
                        ("_zh_cn", os.path.join(GUIDE_DIR, "_zh_cn", NS + "_intro"))):
        if not os.path.isdir(folder):
            problems.append(f"guide: missing folder {folder}")
            continue
        langs[sub] = {f for f in os.listdir(folder) if f.endswith(".md")}
        for page in sorted(langs[sub]):
            text = open(os.path.join(folder, page), encoding="utf-8").read()
            where = f"{sub or 'en'}/{page}"
            for raw in re.findall(r'id="([a-z0-9_.-]+:[a-z0-9_.-]+)"', text):
                check_id(where, raw)
            for raw in re.findall(r"^\s*-\s+([a-z0-9_.-]+:[a-z0-9_.-]+)\s*$", text, re.M):
                check_id(where + " item_ids", raw)
            icon = re.search(r"^\s*icon:\s*(\S+)\s*$", text, re.M)
            if icon:
                check_id(where + " icon", icon.group(1))
            for target in re.findall(r"\]\(([a-z0-9_]+\.md)\)", text):
                if target not in langs[sub]:
                    problems.append(f"{where}: links to a page this line does not ship: {target}")
            for src in re.findall(r'<ImportStructure src="([^"]+)"', text):
                # Resolved against the guide root, not the page: eco 20.5.3 ships one ae2guide/scenes/ and
                # its _zh_cn pages carry the same "../scenes/x.nbt" string, so the folder is shared.
                path = os.path.join(GUIDE_DIR, "scenes", os.path.basename(src))
                if not os.path.isfile(path):
                    problems.append(f"{where}: scene missing: {src}")
            parent = re.search(r"^\s*parent:\s*(\S+)\s*$", text, re.M)
            if parent and NS + "_intro" in parent.group(1):
                target = parent.group(1).rsplit("/", 1)[-1]
                if target != "index.md" and target not in langs[sub]:
                    problems.append(f"{where}: navigation.parent page not shipped: {target}")
            for leak in FOREIGN_NS.findall(text):
                problems.append(f"{where}: still names the other line's namespace")
    if len(langs) == 2 and langs[""] != langs["_zh_cn"]:
        problems.append("guide: the two languages ship different page sets: "
                        f"{sorted(langs[''] ^ langs['_zh_cn'])}")


RECIPE_DIR = os.path.join(RES, "data", NS, "recipes")
DISASSEMBLY_DIR = os.path.join(RECIPE_DIR, "disassembly")
DISASSEMBLY_TYPE = "neoecoae:storage_cell_disassembly"
# The infinite matrix is the punchline of a joke, not an oversight: 1.21.1 ships no disassembly for it
# either, and the cell itself hands the player ten seconds of blindness for trying.
UNDISASSEMBLABLY = {NS + ":simplify_concrete_storage_cell"}
CELL_ID = re.compile(r"(prototype_item_cell_[a-z0-9]+|.*_storage_cell(_[a-z0-9]+)?)$")


def member_ids(node):
    """Our own ids named inside a recipe, skipping the recipe type.

    The "type" field is a RecipeType/serializer id, not an item: our assembler's seven recipes name
    neoecoprototype1201:processor_assembler there, and that entry is registered by a DeferredRegister, so
    it has no lang key and would never pass a membership test against items.
    """
    found = set()
    if isinstance(node, dict):
        for key, item in node.items():
            if key == "type":
                continue
            found |= member_ids(item)
    elif isinstance(node, list):
        for entry in node:
            found |= member_ids(entry)
    elif isinstance(node, str) and node.startswith(NS + ":"):
        found.add(node)
    return found


def check_recipes():
    """Two rules the game never complains about out loud: 联动配方 and 矩阵要能拆.

    A recipe that names an item this line only registers with an optional mod must carry the matching
    forge:mod_loaded condition, because Forge 1.20.1 throws on the unknown id instead of skipping the file.
    And every storage cell has to have a disassembly file, because a matrix the player cannot take apart
    is a trap, not a feature.
    """
    for folder, label in ((RECIPE_DIR, "recipes"), (DISASSEMBLY_DIR, "recipes/disassembly")):
        if not os.path.isdir(folder):
            problems.append(f"{label}: folder missing")
            continue
        for name in sorted(f for f in os.listdir(folder) if f.endswith(".json")):
            path = os.path.join(folder, name)
            data = json.load(open(path, encoding="utf-8"))
            text = json.dumps(data, ensure_ascii=False)
            # Every id in our own namespace inside a recipe has to be an item this line registers with a
            # name. PROVEN is built out of these very files, so it cannot vouch for them: a result that
            # names the other line's id is a parse error in-game and passed this check unnoticed once.
            for member in sorted(member_ids(data)):
                if member not in OURS:
                    problems.append(f"{label}/{name}: {member} is not registered on this line"
                                    " - the game throws Unknown item and drops the recipe")
            mods = {gated_by(member) for member in member_ids(data)}
            mods.discard(None)
            declared = {c.get("modid") for c in data.get("conditions", [])
                        if c.get("type") == "forge:mod_loaded"}
            if data.get("conditions") and not declared:
                problems.append(f"{label}/{name}: conditions present but no forge:mod_loaded among them")
            if mods - declared:
                problems.append(f"{label}/{name}: names gated ids {sorted(mods - declared)} without"
                                f" a mod_loaded condition - Forge 1.20.1 throws on the unknown item")
            if declared - mods:
                problems.append(f"{label}/{name}: gated on {sorted(declared - mods)} but every id it"
                                " names registers unconditionally")
            if label == "recipes" and DISASSEMBLY_TYPE in text:
                problems.append(f"{label}/{name}: disassembly belongs under recipes/disassembly")

    cells = {member for member in OURS if CELL_ID.search(member)}
    shipped = {name[:-5] for name in os.listdir(DISASSEMBLY_DIR)
               if name.endswith(".json")} if os.path.isdir(DISASSEMBLY_DIR) else set()
    for cell in sorted(cells - UNDISASSEMBLABLY):
        if cell.split(":", 1)[1] not in shipped:
            problems.append(f"recipes/disassembly: {cell} has no disassembly file - a matrix the player "
                            "cannot take apart loses the housing and the component both")
    for name in sorted(shipped):
        cell = NS + ":" + name
        if cell not in cells:
            problems.append(f"recipes/disassembly/{name}.json: names no storage cell this line registers")
        elif cell in UNDISASSEMBLABLY:
            problems.append(f"recipes/disassembly/{name}.json: the infinite matrix is meant to be"
                            " undisassemblable")
    for name in sorted(shipped):
        data = json.load(open(os.path.join(DISASSEMBLY_DIR, name + ".json"), encoding="utf-8"))
        if data.get("type") != DISASSEMBLY_TYPE:
            problems.append(f"recipes/disassembly/{name}.json: type {data.get('type')} is not this"
                            f" line's disassembly recipe ({DISASSEMBLY_TYPE} - AE2's own id arrived with"
                            " the 1.21 generation)")
        for entry in data.get("cell_disassembly_items", []):
            if "id" in entry:
                problems.append(f"recipes/disassembly/{name}.json: output uses 1.21's 'id';"
                                " eco 20.5.3 reads 'item'")
                continue
            check_id(f"recipes/disassembly/{name}", entry["item"])
    print(f"recipes: {len(cells)} storage cells, {len(shipped)} disassembly tables")


def main():
    if not os.path.isdir(ADV_DIR):
        print(f"no advancements at {ADV_DIR}")
        return 1
    check_pages()
    check_recipes()
    nodes = sorted(f for f in os.listdir(ADV_DIR) if f.endswith(".json"))
    for name in nodes:
        check_node(name[:-5], json.load(open(os.path.join(ADV_DIR, name), encoding="utf-8")))
    for name in sorted(os.listdir(TAG_DIR)):
        tag = json.load(open(os.path.join(TAG_DIR, name), encoding="utf-8"))
        for entry in tag.get("values", []):
            member, required = entry["id"], entry.get("required", True)
            if not member.startswith(NS + ":"):
                continue
            if member not in OURS:
                problems.append(f"tags/{name}: member {member} has no name on this line")
            elif required and is_gated(member):
                problems.append(f"tags/{name}: {member} is registered only with an optional mod,"
                                " so a required entry is a load error on a pack without it")
            elif not required and not is_gated(member):
                problems.append(f"tags/{name}: optional member {member} is unconditional here"
                                " - an optional miss is silent, make it required or drop it")
    print(f"advancements: {len(nodes)} nodes, {len(OURS)} registered ids, "
          f"{len(PROVEN)} recipe-proven ids")
    for problem in problems:
        print("  FAIL " + problem)
    if not problems:
        print("  all references resolve")
    return 1 if problems else 0


sys.exit(main())
