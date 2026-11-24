"""Check the ported data (advancements + item tags) against what this line actually registers.

Why this exists: the tree came from the 1.21.1 line, and every way it can drift is silent at runtime -
a lang key that no file has renders as the raw key, an item id that is not registered simply never
awards, and a tag whose members are all "required": false can be empty without any complaint at all.

Run:  python tools/check_data_sync.py
Exit 0 = every reference resolves. Exit 1 = the printed lines name what does not.
"""
import json
import gzip
import os
import re
import struct
import sys

NS = "neoecoprototype1201"
RES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources")
ADV_DIR = os.path.join(RES, "data", NS, "advancements")
TAG_DIR = os.path.join(RES, "data", NS, "tags", "items")
LANG_DIR = os.path.join(RES, "assets", NS, "lang")
ASSET_DIR = os.path.join(RES, "assets", NS)

problems = []
JAVA_DIR = os.path.join(os.path.dirname(RES), "java", "cn", "dancingsnow", NS)
COLD_JAVA = os.path.join(JAVA_DIR, "registration", "ColdContent.java")
COLD_TEXT = open(COLD_JAVA, encoding="utf-8").read() if os.path.isfile(COLD_JAVA) else ""
# Keep the registry spaces distinct: a LiquidBlock has no BlockItem, and source/flowing fluid ids
# are not items. Translations alone must never manufacture a container or a fluid registration.
COLD_REGISTRATIONS = {
    kind: re.findall(r'\b' + kind + r'\.register\(\s*"([a-z0-9_]+)"', COLD_TEXT)
    for kind in ("BLOCKS", "ITEMS", "FLUIDS", "FLUID_TYPES")
}
COLD_BLOCKS = {NS + ":" + name for name in COLD_REGISTRATIONS["BLOCKS"]}
COLD_ITEMS = {NS + ":" + name for name in COLD_REGISTRATIONS["ITEMS"]}
FLUID_IDS = {NS + ":" + name for name in COLD_REGISTRATIONS["FLUIDS"]}


def registered_ids():
    """Ids this line ships, read from its own lang file: a block or item without a name is already a bug."""
    ids = set()
    for loc in ("zh_cn", "en_us"):
        for key in json.load(open(os.path.join(LANG_DIR, loc + ".json"), encoding="utf-8")):
            m = re.match(r"^(?:block|item)\." + NS + r"\.([a-z0-9_]+)$", key)
            if m:
                ids.add(NS + ":" + m.group(1))
    return (ids - COLD_BLOCKS) | COLD_ITEMS


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
    """The mod id a member needs, or None when this line registers it unconditionally.

    Two answers, in order: an id this line registers under integration/<modid>/ is gated by that mod,
    and any id whose NAMESPACE is another mod's needs that mod - Forge 1.20.1 throws on the unknown id
    whatever namespace it wears, so ars_nouveau:source_gem gates on ars_nouveau exactly the way a
    mekansim barrel does. The namespaces this line ships unconditionally are the hard trio: ours,
    minecraft and ae2, plus neoecoae (a hard dependency whose gated items we never name in recipes)."""
    ns, _, path = member.partition(":")
    if path in EXACT:
        return EXACT[path]
    for prefix, modid in PREFIXES.items():
        if path.startswith(prefix):
            return modid
    if ns in (NS, "minecraft", "ae2", "neoecoae"):
        return None
    return ns


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
    elif raw.startswith(NS + ":") and raw not in OURS | COLD_BLOCKS | FLUID_IDS and raw not in PROVEN:
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
# Only the palette holds block states, and every string in NBT is preceded by its own uint16 length, so
# a palette name is readable without a full parser: TAG_String (0x08) + the four bytes of the key "Name"
# + length + payload. Other namespaces may legitimately appear there - the capture is a world snapshot.
NAME_TAG = re.compile(rb"\x08\x00\x04Name")


def scene_palette(raw):
    out = []
    for found in NAME_TAG.finditer(raw):
        start = found.end()
        if start + 2 > len(raw):
            break
        length = struct.unpack(">H", raw[start:start + 2])[0]
        value = raw[start + 2:start + 2 + length]
        if len(value) == length and b":" in value:
            out.append(value.decode("utf-8", "replace"))
    return out


def check_scene(where, raw):
    """Every block a shipped scene names has to exist on this line, or the picture loses it in silence.

    guideme's structure reader turns an unregistered id into air: the page still opens, the frame is just
    missing its shell. That is how both the crafting and the computation pictures came to show nothing but
    the host block on 2026-11-02 - those two captures were taken in the 1.21.1 line's world, so all of
    their blocks named `neoecoprototype:`, a namespace this line never registers. A structure file keeps
    its ids as binary strings, so nothing in the page text could have shown this.
    """
    palette = scene_palette(raw)
    ours = 0
    for member in palette:
        if not member.startswith(NS + ":"):
            continue
        ours += 1
        if member not in OURS and member not in PROVEN:
            problems.append(f"{where}: {member} is in the palette but this line does not register it, "
                            "so the reader draws air where that block stood")
    foreign = [member for member in palette if FOREIGN_NS.search(member)]
    if foreign:
        problems.append(f"{where}: {len(foreign)} palette blocks name the other line's namespace "
                        f"({sorted(set(foreign))[:3]}...) - the whole picture loses them")
    if not ours:
        problems.append(f"{where}: the palette names no block of ours at all - an empty capture")
    return ours


def check_pages():
    """Every id, page link and scene a guide page names has to exist here, in both languages.

    Also the item -> page index: guideme builds it from each page's item_ids block, so an id that no page
    claims is an item the reader cannot look up, and an id two pages claim lands on whichever page guideme
    happened to read last. Both are silent in-game, so they are asserted here.
    """
    langs = {}
    claims = {}
    referenced_scenes = set()
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
                if raw.startswith(NS + ":"):
                    claims.setdefault(sub, {}).setdefault(raw, []).append(page)
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
                referenced_scenes.add(os.path.basename(src))
                if not os.path.isfile(path):
                    problems.append(f"{where}: scene missing: {src}")
            parent = re.search(r"^\s*parent:\s*(\S+)\s*$", text, re.M)
            if parent and NS + "_intro" in parent.group(1):
                target = parent.group(1).rsplit("/", 1)[-1]
                if target != "index.md" and target not in langs[sub]:
                    problems.append(f"{where}: navigation.parent page not shipped: {target}")
            # The three KubeJS builder types are deliberately named neoecoprototype:* on both lines
            # (the 1.21.1 family allocation, per reliqwq 2026-11-22): a script written against the
            # 1.21.1 docs runs unchanged here. Strip them before the foreign-namespace scan.
            scan = re.sub(r"neoecoprototype:(?:storage_matrix|infinite_storage_matrix|eco_supply_cell)",
                          "", text)
            for leak in FOREIGN_NS.findall(scan):
                problems.append(f"{where}: still names the other line's namespace")
    for sub, per_page in sorted(claims.items()):
        label = "guide/" + (sub or "en")
        for raw, pages in sorted(per_page.items()):
            if len(pages) > 1:
                problems.append(f"{label}: {raw} is claimed by {len(pages)} pages "
                                f"({sorted(set(pages))}) - the item index can only point at one")
        for raw in sorted(OURS - set(per_page)):
            problems.append(f"{label}: {raw} is registered but claimed by no page - the book's item"
                            " index has nothing to open for it")
    if len(claims) == 2 and set(claims[""]) != set(claims["_zh_cn"]):
        problems.append("guide: the two languages claim different item sets: "
                        + str(sorted(set(claims[""]) ^ set(claims["_zh_cn"]))[:6]))
    scenes_dir = os.path.join(GUIDE_DIR, "scenes")
    scene_ids = 0
    if os.path.isdir(scenes_dir):
        for name in sorted(f for f in os.listdir(scenes_dir)
                           if f.endswith(".nbt") and f not in referenced_scenes):
            problems.append(f"guide/scenes/{name}: shipped but no page imports it")
        for name in sorted(f for f in os.listdir(scenes_dir) if f.endswith(".nbt")):
            scene_ids += check_scene("guide/scenes/" + name, gzip.decompress(
                open(os.path.join(scenes_dir, name), "rb").read()))
    print(f"guide: {len(claims.get('', {}))} of {len(OURS)} ids claimed, "
          f"{len(referenced_scenes)} scenes imported, {scene_ids} block ids of ours inside them")
    if len(langs) == 2 and langs[""] != langs["_zh_cn"]:
        problems.append("guide: the two languages ship different page sets: "
                        f"{sorted(langs[''] ^ langs['_zh_cn'])}")


RECIPE_DIR = os.path.join(RES, "data", NS, "recipes")
DATA_DIR = os.path.join(RES, "data", NS)
# Keys whose string value is an item / tag / block id, in the data formats this line ships.
ID_KEYS = ("item", "tag", "name", "id", "items", "fluid")
DISASSEMBLY_DIR = os.path.join(RECIPE_DIR, "disassembly")
DISASSEMBLY_TYPE = "neoecoae:storage_cell_disassembly"
# The infinite matrix is the punchline of a joke, not an oversight: 1.21.1 ships no disassembly for it
# either, and the cell itself hands the player ten seconds of blindness for trying.
# The two small-bulk expansions are the other kind of deliberate: the card is spent turning a base cell
# into them, and a disassembly back to housing + 1M component would quietly un-spend it. 1.21.1 leaves
# them un-takable for the same reason, and its guide page warns the player before they craft one.
# The three growing cells (singularity / sky stone dust / source) are OUT OF CELL_ID on purpose, per
# reliqwq's call on 2026-11-21: they ship no disassembly here, so the guide pages claim none. 1.21.1
# DOES ship those three tables - do not re-import them on a sync, and re-extend CELL_ID together with
# new tables if the design ever changes.
UNDISASSEMBLABLY = {NS + ":simplify_concrete_storage_cell",
                    NS + ":simplify_small_bulk_storage_cell_expanded",
                    NS + ":simplify_small_bulk_fluid_storage_cell_expanded"}
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


def all_ids(node):
    """Every namespaced id a recipe names, third-party ones included.

    The gating walk uses this: Forge 1.20.1 throws on an unknown id whatever namespace it wears, so
    ars_nouveau:source_gem inside a pedestal list needs ars_nouveau exactly the way our own gated ids
    need theirs. The membership test above keeps walking our namespace only - that test is about ids
    this line registers, not about the whole world's.
    """
    found = set()
    if isinstance(node, dict):
        for key, item in node.items():
            if key in ("type", "tag"):
                # "type" is a serializer id, not an item; "tag" is a tag reference, and forge:* tags
                # are not mod-gated anything.
                continue
            found |= all_ids(item)
    elif isinstance(node, list):
        for entry in node:
            found |= all_ids(entry)
    elif isinstance(node, str) and re.match(r"^[a-z0-9_]+:[a-z0-9_/.\-]+$", node):
        found.add(node)
    return found


def check_recipes():
    """Check recipe ids, Forge conditional wrappers and storage-cell disassembly coverage.

    Forge 1.20.1 only evaluates optional-mod conditions through the forge:conditional serializer. A
    namespaced condition field attached to an ordinary recipe is ignored, and the recipe then fails on
    any item registered only while an optional mod is present.
    """
    for folder, label in ((root, "recipes/" + os.path.relpath(root, RECIPE_DIR).replace(os.sep, "/"))
                          for root, _, _ in os.walk(RECIPE_DIR)):
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
            for key, member in walk_strings(data):
                if key in ("type", "tag") or not member.startswith(NS + ":"):
                    continue
                allowed = FLUID_IDS if key == "fluid" else OURS
                if member not in allowed:
                    problems.append(f"{label}/{name}: {member} is not a registered "
                                    f"{'fluid' if key == 'fluid' else 'item'} on this line"
                                    " - the game drops the recipe")
            mods = {gated_by(member) for member in all_ids(data)}
            mods.discard(None)
            entries = data.get("recipes", []) if data.get("type") == "forge:conditional" else []
            declared = {condition.get("modid")
                        for entry in entries
                        for condition in entry.get("conditions", [])
                        if condition.get("type") == "forge:mod_loaded"}
            if "forge:conditions" in data:
                problems.append(f"{label}/{name}: has ignored top-level \"forge:conditions\"; wrap it"
                                " with type \"forge:conditional\"")
            if data.get("type") == "forge:conditional":
                if not entries or any(not isinstance(entry, dict)
                                      or not isinstance(entry.get("conditions"), list)
                                      or not isinstance(entry.get("recipe"), dict)
                                      for entry in entries):
                    problems.append(f"{label}/{name}: malformed forge:conditional wrapper")
            elif mods:
                problems.append(f"{label}/{name}: names gated ids {sorted(mods)} outside a"
                                " forge:conditional wrapper - Forge 1.20.1 will parse them unconditionally")
            if mods - declared:
                problems.append(f"{label}/{name}: names gated ids {sorted(mods - declared)} without"
                                f" a mod_loaded condition - Forge 1.20.1 throws on the unknown item")
            if declared - mods:
                problems.append(f"{label}/{name}: gated on {sorted(declared - mods)} but every id it"
                                " names registers unconditionally")
            if folder != DISASSEMBLY_DIR and DISASSEMBLY_TYPE in text:
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
        recipe = (data["recipes"][0]["recipe"] if data.get("type") == "forge:conditional"
                  else data)
        if recipe.get("type") != DISASSEMBLY_TYPE:
            problems.append(f"recipes/disassembly/{name}.json: type {recipe.get('type')} is not this"
                            f" line's disassembly recipe ({DISASSEMBLY_TYPE} - AE2's own id arrived with"
                            " the 1.21 generation)")
        for entry in recipe.get("cell_disassembly_items", []):
            if "id" in entry:
                problems.append(f"recipes/disassembly/{name}.json: output uses 1.21's 'id';"
                                " eco 20.5.3 reads 'item'")
                continue
            check_id(f"recipes/disassembly/{name}", entry["item"])
    print(f"recipes: {len(cells)} storage cells, {len(shipped)} disassembly tables")


MODEL_DIR = os.path.join(ASSET_DIR, "models")
TEXTURE_DIR = os.path.join(ASSET_DIR, "textures")
BLOCKSTATE_DIR = os.path.join(ASSET_DIR, "blockstates")
# The two folders whose models are named by a string literal in java rather than by a file the game
# discovers on its own: nothing bakes or draws a model the client code never names, and nothing draws a
# name whose file this line does not ship. Both directions are silent - the missing file is a purple
# block, the missing name is a model that never appears no matter how many faces it has.
NAMED_FOLDERS = {
    "block/computation_cable": ("ComputationCableModels.java", r'\bid\(\s*"([a-z0-9_]+)"'),
    "block/computation_cell": ("Prototype1201Client.java", r'computationModel\(\s*"([a-z0-9_]+)"'),
    # A part model nobody names is a part that draws nothing at all, and the other direction is a path
    # handed to PartModels that never bakes. Same silent pair as the two folders above. The lookbehind skips
    # AE2's own status overlays, which are its models to ship, not ours.
    # The interface cable faces name their base models from their own part classes, so this folder's
    # check reads three sources and unions them.
    "part": (["SimplifyPatternProviderPart.java", "PoweredInterfacePart.java",
              "SuperconductiveInterfacePart.java"], r'(?<!"ae2", )"part/([a-z0-9_]+)"'),
}


def java_cell_names():
    """Drive-chip model names the client and MatrixMaterials point at, for the block/cell folder.

    Three name sources, because the client names chips in three shapes: string literals
    (the rung arrays and the three single-family constants), the rungs() template whose
    medium argument is expanded here - item / fluid / chemical, the same three calls
    everyCellModel() makes; a family added there without adding it to this list turns the
    new files red, which is the accounting this check exists for - and MatrixMaterials'
    kind table, whose drive paths name the custom and small-bulk chips nothing else names.
    """
    root = os.path.join(os.path.dirname(RES), "java", "cn", "dancingsnow", "neoecoprototype1201")
    client = open(os.path.join(root, "Prototype1201Client.java"), encoding="utf-8").read()
    # The rungs() template carries "storage_cell_l0_" as a bare stem; a name that ends in the
    # underscore is that stem, not a model, so it is dropped here.
    named = {n for n in re.findall(r'"(storage_cell_[a-z0-9_]+)"', client)
             if not n.endswith("_")}
    for medium in ("item", "fluid", "chemical"):
        named |= {"storage_cell_l0_" + medium + "_1k",
                  "storage_cell_l0_" + medium + "_16k",
                  "storage_cell_l0_" + medium + "_64k",
                  "storage_cell_l1_" + medium,
                  "storage_cell_l1r_" + medium}
    materials = open(os.path.join(root, "api", "MatrixMaterials.java"), encoding="utf-8").read()
    named |= set(re.findall(r'"block/cell/(storage_cell_[a-z0-9_]+)"', materials))
    return named


def java_names(sources, pattern):
    """Every string literal the named helpers are called with, across the files that call them.

    sources is one file name or a list of them; a multi-file folder unions every source's literals.
    """
    if isinstance(sources, str):
        sources = [sources]
    root = os.path.join(os.path.dirname(RES), "java")
    named = set()
    seen_any = False
    for here, _, files in os.walk(root):
        for name in files:
            if name not in sources:
                continue
            seen_any = True
            text = open(os.path.join(here, name), encoding="utf-8").read()
            named |= set(re.findall(pattern, text))
    if not seen_any:
        problems.append(f"models: none of {sources} found, so no model name in them can be checked")
    return named


def check_models():
    lit = 0
    ao = {"false": 0, "true": 0, "absent": 0}
    for here, _, files in os.walk(MODEL_DIR):
        for name in sorted(files):
            if not name.endswith(".json"):
                continue
            path = os.path.join(here, name)
            where = "models/" + os.path.relpath(path, MODEL_DIR).replace(os.sep, "/")
            data = json.load(open(path, encoding="utf-8"))
            for key, value in data.get("textures", {}).items():
                if value.startswith(NS + ":") and not os.path.isfile(
                        os.path.join(TEXTURE_DIR, value.split(":", 1)[1] + ".png")):
                    problems.append(f"{where}: texture key {key} points at no png: {value}")
            parent = data.get("parent", "")
            if parent.startswith(NS + ":") and not os.path.isfile(
                    os.path.join(MODEL_DIR, parent.split(":", 1)[1] + ".json")):
                problems.append(f"{where}: parent model missing: {parent}")
            for element in data.get("elements", []):
                faces = list(element.get("faces", {}).values())
                lit_faces = [f for f in faces if f.get("forge_data")]
                if not lit_faces:
                    continue
                lit += len(lit_faces)
                # eco 20.5.3 puts "shade": false on every element that carries lit faces, and this line's
                # pack did too except in the fourteen controller elements found on 2026-11-02. The key is
                # not cosmetic: a shaded face is multiplied by its own direction (0.6 for the sides, 0.5
                # for the bottom) however bright forge_data makes it, so a missing flag makes our glow
                # read as a different texture beside eco's own art in the same host - which is what the
                # flash crystal array looked like inside an L4+ frame.
                if lit_faces != faces:
                    problems.append(f"{where}: element {element.get('name')!r} mixes lit and unlit faces"
                                    " - an element-level shade flag cannot be right for both")
                elif element.get("shade") is not False:
                    problems.append(f"{where}: element {element.get('name')!r} has {len(lit_faces)} lit"
                                    ' faces but no "shade": false, so the light is dimmed by direction')
                for face in lit_faces:
                    value = face["forge_data"].get("ambient_occlusion")
                    ao["absent" if value is None else ("false" if value is False else "true")] += 1
    for folder, (source, pattern) in NAMED_FOLDERS.items():
        shipped = {name[:-5] for name in os.listdir(os.path.join(MODEL_DIR, folder))
                   if name.endswith(".json")}
        named = java_names(source, pattern)
        for extra in sorted(shipped - named):
            problems.append(f"models/{folder}/{extra}.json: no java names it, so nothing bakes or draws"
                            f" it ({source})")
        for missing in sorted(named - shipped):
            problems.append(f"{source} names {folder}/{missing} but this line does not ship that model")
    cell_shipped = {name[:-5] for name in os.listdir(os.path.join(MODEL_DIR, "block/cell"))
                    if name.endswith(".json")}
    cell_named = java_cell_names()
    for extra in sorted(cell_shipped - cell_named):
        problems.append(f"models/block/cell/{extra}.json: no java names it, so nothing bakes or draws it"
                        " (Prototype1201Client / MatrixMaterials)")
    for missing in sorted(cell_named - cell_shipped):
        problems.append(f"the client names block/cell/{missing} but this line does not ship that model")
    print(f"models: {lit} lit faces on flat-shaded elements, every texture and parent resolving, "
          f"{len(os.listdir(os.path.join(MODEL_DIR, 'block/computation_cable')))} cable models named by "
          f"java, {len(cell_shipped)} drive-chip models all named; ambient_occlusion census {dict(ao)} - eco 20.5.3 spells false on all 830 of its lit "
          "faces and no model anywhere sets a top-level ambientocclusion, so true and absent are the "
          "same thing here: the number is the open question, not a failure")


def check_jade_keys():
    """Every Jade provider this line registers needs its own config translation, in both languages.

    Jade 11.13.2 (Forge 1.20.1) asserts config.jade.plugin_<modid>.<uid path> as the provider registers,
    and it does that only on a physical client. A missing key is not a missing label: the AssertionError
    lands inside the model-load path, so the game dies on the loading screen with the provider's uid in the
    message. Nothing short of starting a client sees this - which is how the machine-mind provider crashed
    the 2026-11-02 launch after the headless server had already called that boot clean.
    """
    langs = {loc: json.load(open(os.path.join(LANG_DIR, loc + ".json"), encoding="utf-8"))
             for loc in ("zh_cn", "en_us")}
    uids = sorted(java_names("SimplifyCraftingHostProvider.java", r'\.id\(\s*"([a-z0-9_]+)"'))
    for uid in uids:
        label = "config.jade.plugin_" + NS + "." + uid
        for loc, lang in langs.items():
            if label not in lang:
                problems.append(f"lang/{loc}: {label} is missing - Jade asserts it as the provider registers,"
                                " and that AssertionError kills the client on the loading screen")
    print(f"jade: {len(uids)} provider uid(s), each needing a config key in both languages")


def check_slot_count():
    """The provider's slot count lives in three places at once: the java constant, both languages'
    advancement text, and the box face, which draws three rows of nine. A drift is invisible until someone
    counts slots in a GUI, so the number is read back out of the source and compared with the prose.
    """
    root = os.path.join(os.path.dirname(RES), "java")
    text = None
    for here, _, files in os.walk(root):
        if "SimplifyPatternProviderBlockEntity.java" in files:
            text = open(os.path.join(here, "SimplifyPatternProviderBlockEntity.java"), encoding="utf-8").read()
            break
    if text is None:
        problems.append("slots: SimplifyPatternProviderBlockEntity.java not found")
        return
    found = re.search(r"PATTERN_SLOTS\s*=\s*(\d+)", text)
    if not found:
        problems.append("slots: no PATTERN_SLOTS constant in the provider's block entity")
        return
    slots = found.group(1)
    if slots != "27":
        problems.append(f"slots: the java constant says {slots}, the art draws 3 rows of 9 (27)")
    for loc in ("zh_cn", "en_us"):
        lang = json.load(open(os.path.join(LANG_DIR, loc + ".json"), encoding="utf-8"))
        for key in [k for k in lang if k.startswith("advancement." + NS + ".pattern_provider.")]:
            if "slot" in lang[key].lower() or "槽" in lang[key]:
                if slots not in re.findall(r"\d+", lang[key]):
                    problems.append(f"lang/{loc}: {key} advertises a different slot count than the code "
                                    f"({lang[key]!r} vs {slots})")
    print(f"provider: PATTERN_SLOTS = {slots}, both languages' text agrees")


def check_data_ids():
    """Every item, tag and block id in our data files has to be written with its namespace.

    This bites because it was missed once: a doll recipe carried "item": "simplify_storage_controller",
    and every other rule here only looks at strings that already look like ours, so the file passed while
    the game read it as minecraft:simplify_storage_controller and dropped the recipe with one ERROR line
    ("Unknown item ...") in the server log. A namespaced id is the only shape vanilla accepts.
    """
    total = 0
    scanned = 0
    for folder, _, files in os.walk(DATA_DIR):
        for name in sorted(f for f in files if f.endswith(".json")):
            path = os.path.join(folder, name)
            label = path.replace(RES + "/", "").replace("\\", "/")
            data = json.load(open(path, encoding="utf-8"))
            scanned += len(id_strings(data))
            for key, value in naked_ids(data):
                total += 1
                problems.append(f"{label}: {key}={value!r} has no namespace - the game reads it as "
                                f"minecraft:{value} and drops the file")
    # The denominator is printed on purpose: "0 problems" from a rule that matched nothing at all is the
    # same number as a blind scanner, and only this line can tell the two apart.
    print(f"data ids: {scanned} id-shaped strings scanned, {total} of them without a namespace")


def id_strings(node, key=None):
    """Every id-shaped string in a data file, namespace or not - the denominator for the rule above."""
    if isinstance(node, dict):
        return [found for child_key, child in node.items() if child_key != "type"
                for found in id_strings(child, child_key)]
    if isinstance(node, list):
        return [found for entry in node for found in id_strings(entry, key)]
    if isinstance(node, str) and key in ID_KEYS:
        return [node]
    return []


def naked_ids(node, key=None):
    """(key, value) for every id-shaped string that is missing its namespace."""
    if isinstance(node, dict):
        out = []
        for child_key, child in node.items():
            if child_key == "type":
                continue  # a RecipeType id is a serializer name, and the ones we ship are ours anyway
            out += naked_ids(child, child_key)
        return out
    if isinstance(node, list):
        out = []
        for entry in node:
            out += naked_ids(entry, key)
        return out
    if isinstance(node, str) and key in ID_KEYS and ":" not in node:
        return [(key, node)]
    return []


HAND_MINED_BLOCKS = {NS + ":fumo_doll_kouooki", NS + ":fumo_doll_reliqwq", NS + ":fumo_doll_tedxenon",
                     NS + ":fumo_doll_yang120", NS + ":fumo_doll_yang120_cpu", NS + ":fumo_doll_yang120_giant",
                     NS + ":cryotheum_ice_cream_block"}
# LiquidBlocks are placed fluid states, not mineable ore or solid dessert. Their noLootTable properties
# and lack of BlockItems are checked by the bounded family gate below.
LIQUID_BLOCKS = {NS + ":cryotheum_ice_cream", NS + ":dissolution_solution"}
# Our blocks whose Properties come from an AE2 constructor: the tool flag is not ours to set, so they
# carry the pickaxe entry (a tool-type declaration) but never the stone rung (a demand the flag would
# have to back). 1.21.1's guard keeps the same shape with its own three; the energized batteries are
# on this list HERE because this line's AE2 15.4.10 EnergyCellBlock ctor builds glassProps() itself.
AE2_OWNED_PROPS_BLOCKS = {NS + ":energized_energy_cell_16g", NS + ":energized_energy_cell_512g",
                          NS + ":simplify_pattern_provider", NS + ":simplify_powered_me_interface",
                          NS + ":superconductive_interface"}


def check_block_tags():
    """The 1.21.1 guard everyPlacedBlockIsStonePickaxeGated, as a data check.

    Every blockstate-registered block is stone-pickaxe gated unless it is hand-mined (the plushies)
    or its properties are AE2's: needs_stone_tool is the block list minus both exemptions, and
    mineable/pickaxe is the stone rung plus the AE2-owned set - a stone entry without the pickaxe
    entry is not a pickaxe block at all, and a pickaxe entry for a hand-mined plushie would make the
    wool demand a tool. The requiresCorrectToolForDrops flag that makes the stone rung bite lives in
    metal()'s copy(Blocks.IRON_BLOCK); blocks built from AE2 constructors cannot carry it, which is
    exactly why they are exempted instead of tagged.
    """
    blocks = {NS + ":" + name[:-5] for name in os.listdir(BLOCKSTATE_DIR) if name.endswith(".json")}
    # Vanilla tool tags live in the MINECRAFT namespace - extending #minecraft:needs_stone_tool means
    # a file under data/minecraft/, not under the mod's own namespace.
    mc_tags = os.path.join(RES, "data", "minecraft", "tags", "blocks")
    pick = set(json.load(open(os.path.join(mc_tags, "mineable", "pickaxe.json"),
                               encoding="utf-8")).get("values", []))
    stone = set(json.load(open(os.path.join(mc_tags, "needs_stone_tool.json"),
                               encoding="utf-8")).get("values", []))
    expected_stone = blocks - HAND_MINED_BLOCKS - AE2_OWNED_PROPS_BLOCKS - LIQUID_BLOCKS
    if expected_stone != stone:
        problems.append("tags/blocks/needs_stone_tool is not our gated block list: missing="
                        + str(sorted(expected_stone - stone)) + " extra=" + str(sorted(stone - expected_stone)))
    if not stone <= pick:
        problems.append("tags/blocks/needs_stone_tool entries missing from mineable/pickaxe: "
                        + str(sorted(stone - pick)) + " - eco keeps stone as a subset of pickaxe")
    expected_pick = stone | AE2_OWNED_PROPS_BLOCKS
    if expected_pick != pick:
        problems.append("tags/blocks/mineable/pickaxe should be the stone rung plus the AE2-owned set:"
                        " missing=" + str(sorted(expected_pick - pick)) + " extra=" + str(sorted(pick - expected_pick))
                        + " - a hand-mined plushie in the pickaxe tag would demand a tool for wool")


def check_blockstates():
    """Two silent failures a blockstate can hold: a model nothing bakes, and a face nothing selects.

    A blockstate is the only place that names most of this pack's models, and a wrong path there costs
    nothing at load - the cell simply draws nothing, which reads as a missing texture or a ghost machine.
    The second rule is the 2026-11-02 mind port: the crafting controller publishes has_mind and mind_tier,
    so every shaped row has to carry all four appearance rows (no face, then the three bands). A forgotten
    one is invisible in every log and in every panel - a minded host would just keep drawing the plain face.
    """
    total_rows = 0
    named = 0
    for here, _, files in os.walk(BLOCKSTATE_DIR):
        for name in sorted(files):
            if not name.endswith(".json"):
                continue
            path = os.path.join(here, name)
            label = "blockstates/" + os.path.relpath(path, BLOCKSTATE_DIR).replace(os.sep, "/")
            data = json.load(open(path, encoding="utf-8"))
            rows = data.get("variants") or data.get("multipart") or []
            # "variants" maps a state string to either one model object or a list of them, and "multipart"
            # is already a list. Iterating a dict yields its keys - strings - so every row of every
            # variants file used to be skipped in silence: 69 of 563 model paths were ever looked at.
            groups = rows.values() if isinstance(rows, dict) else [rows]
            for group in groups:
                for entry in (group if isinstance(group, list) else [group]):
                    total_rows += 1
                    applies = entry.get("apply", entry) if isinstance(entry, dict) else entry
                    for apply in (applies if isinstance(applies, list) else [applies]):
                        model = apply.get("model") if isinstance(apply, dict) else None
                        if not model or not model.startswith(NS + ":"):
                            continue
                        named += 1
                        if not os.path.isfile(os.path.join(MODEL_DIR, model.split(":", 1)[1] + ".json")):
                            problems.append(f"{label}: names {model} and this line ships no such model")

    crafted = os.path.join(BLOCKSTATE_DIR, "simplify_crafting_system.json")
    if not os.path.isfile(crafted):
        problems.append("blockstates/simplify_crafting_system.json is missing - the mind rule has nothing to read")
        print(f"blockstates: {total_rows} rows, {named} of them naming one of our models")
        return
    rows = json.load(open(crafted, encoding="utf-8"))["multipart"]
    faces = [e for e in rows if e["when"].get("has_mind") == "true"]
    shaped = {}
    for entry in rows:
        when = entry["when"]
        if when.get("formed") != "true":
            continue
        key = (when.get("facing"), when.get("mirrored"), when.get("communication_interface"))
        shaped.setdefault(key, []).append(when.get("mind_tier", "absent"))
    for key, tiers in sorted(shaped.items()):
        if sorted(tiers) != ["0", "1", "2", "absent"]:
            problems.append(f"blockstates/simplify_crafting_system.json: the {key} shape offers {sorted(tiers)}"
                            " - a formed host needs the no-face row plus mind_tier 0, 1 and 2")
    for entry in faces:
        if "face" not in entry["apply"]["model"]:
            problems.append(f"blockstates/simplify_crafting_system.json: has_mind=true with"
                            f" {entry['apply']['model']} names no face model")
    print(f"blockstates: {total_rows} rows, {named} naming one of our models; the formed crafting "
          f"controller offers {len(shaped)} shapes x {len(faces)} face rows")


def check_texture_frames():
    """Every multi-frame-shaped png carries a .mcmeta, because a sprite sheet without one renders
    as all its frames squeezed into one slot. The shape test is h > w with h a multiple of w - the
    drive's level/type strips are square and pass; three deliveries already lost their mcmeta on
    the way over (singularity item, singularity drive housing, quantum omni layer) and each bug
    was a player-visible squeezed carousel. 1.21.1 is the mcmeta authority: a png this flag names
    gets its sidecar copied from there, byte for byte.
    """
    count = 0
    for here, _, files in os.walk(TEXTURE_DIR):
        for name in sorted(files):
            if not name.endswith(".png"):
                continue
            path = os.path.join(here, name)
            try:
                from PIL import Image
                width, height = Image.open(path).size
            except Exception:
                continue
            if height <= width or height % width or height // width < 2:
                continue
            count += 1
            if not os.path.isfile(path + ".mcmeta"):
                where = "textures/" + os.path.relpath(path, TEXTURE_DIR).replace(os.sep, "/")
                problems.append(f"{where}: {height // width} frames in a {width}-wide sheet with no"
                                " .mcmeta - the game renders every frame squeezed into one slot")
    print(f"textures: {count} multi-frame pngs, every one with a sidecar mcmeta" if not problems
          else f"textures: {count} multi-frame pngs")


def check_grette():
    """Check this bounded family against actual registrations, not just translated names.

    The general guide gate uses lang as its inventory. Also check the Java declarations here so a
    copied translation cannot hide a missing block/item. Loot predicates need a separate format check:
    1.21 component predicates are ignored by 1.20.1 and would make Silk Touch drops unconditional.
    """
    blocks = {"small_grette_bud", "medium_grette_bud", "large_grette_bud", "grette_cluster",
              "grette_crystal_block", "flawless_budding_grette", "flawed_budding_grette",
              "chipped_budding_grette", "damaged_budding_grette"}
    materials = {"grette_crystal", "grette_crystal_dust"}
    java = os.path.join(os.path.dirname(RES), "java", "cn", "dancingsnow", NS)
    growth_path = os.path.join(java, "block", "grette", "GretteBuddingBlock.java")
    if not os.path.isfile(growth_path):
        problems.append("grette: growth class missing from the target package directory")
    block_java = open(os.path.join(java, "registration", "ModRegistration.java"), encoding="utf-8").read()
    item_java = open(os.path.join(java, "Registration.java"), encoding="utf-8").read()
    for names, text, pattern in ((blocks, block_java, r'registerBlock\(\s*"([a-z0-9_]+)"'),
                                 (materials, item_java, r'ITEMS\.register\(\s*"([a-z0-9_]+)"')):
        declared = re.findall(pattern, text)
        for name in sorted(names):
            if declared.count(name) != 1:
                problems.append(f"grette: {name} needs exactly one Java registration, got {declared.count(name)}")
    for loc in ("en_us", "zh_cn"):
        for name in sorted(blocks | materials):
            key = ("block." if name in blocks else "item.") + NS + "." + name
            if key not in LANG_KEYS[loc]:
                problems.append(f"grette: {loc} is missing {key}")
    for folder, names in (("blockstates", blocks), ("models/block", blocks),
                          ("models/item", blocks | materials), ("textures/block", blocks),
                          ("textures/item", materials)):
        for name in sorted(names):
            suffix = ".png" if folder.startswith("textures/") else ".json"
            path = os.path.join(ASSET_DIR, folder, name + suffix)
            if not os.path.isfile(path):
                problems.append(f"grette: missing {folder}/{name}{suffix}")
    for name in ("small_grette_bud", "medium_grette_bud", "large_grette_bud", "grette_cluster"):
        path = os.path.join(MODEL_DIR, "block", name + ".json")
        if os.path.isfile(path) and json.load(open(path, encoding="utf-8")).get("render_type") != "cutout_mipped":
            problems.append(f"grette: {name} model needs this line's cutout_mipped render type")
    for name in sorted(blocks):
        path = os.path.join(DATA_DIR, "loot_tables", "blocks", name + ".json")
        if not os.path.isfile(path):
            problems.append(f"grette: missing loot table for {name}")
            continue
        loot = json.load(open(path, encoding="utf-8"))
        if "random_sequence" in loot or any(key == "predicates" for key, _ in walk_strings(loot)):
            problems.append(f"grette: {name} loot still uses the 1.21 format")
        predicates = []

        def check_loot(node):
            if isinstance(node, dict):
                if node.get("condition") == "minecraft:match_tool":
                    predicates.append(node.get("predicate", {}).get("enchantments", []))
                if node.get("type") == "minecraft:item":
                    check_id("grette loot " + name, node.get("name", ""))
                for child in node.values():
                    check_loot(child)
            elif isinstance(node, list):
                for child in node:
                    check_loot(child)

        check_loot(loot)
        expected = [] if name == "grette_crystal_block" else [[{
            "enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]]
        if predicates != expected:
            problems.append(f"grette: {name} needs the 1.20.1 Silk Touch predicate, got {predicates}")
    for kind in ("blocks", "items"):
        groups = {"buds": {"small_grette_bud", "medium_grette_bud", "large_grette_bud"},
                  "clusters": {"grette_cluster"},
                  "budding_blocks": {name for name in blocks if "budding" in name}}
        if kind == "items":
            groups.update({"gems/grette": {"grette_crystal"}, "dusts/grette": {"grette_crystal_dust"},
                           "gems": {"#forge:gems/grette"}, "dusts": {"#forge:dusts/grette"}})
        for tag, names in sorted(groups.items()):
            path = os.path.join(RES, "data", "forge", "tags", kind, tag + ".json")
            if not os.path.isfile(path):
                problems.append(f"grette: missing Forge {kind} tag {tag}")
                continue
            values = json.load(open(path, encoding="utf-8")).get("values", [])
            expected = {name if name.startswith("#") else NS + ":" + name for name in names}
            if set(values) != expected:
                problems.append(f"grette: Forge {kind}/{tag} members {values}, expected {sorted(expected)}")
    recipe = os.path.join(RECIPE_DIR, "inscriber", "grette_crystal_dust.json")
    if not os.path.isfile(recipe):
        problems.append("grette: missing inscriber dust recipe")
    else:
        data = json.load(open(recipe, encoding="utf-8"))
        if (data.get("type") != "ae2:inscriber" or data.get("mode") != "inscribe"
                or data.get("ingredients") != {"middle": {"tag": "forge:gems/grette"}}
                or data.get("result") != {"item": NS + ":grette_crystal_dust", "count": 1}):
            problems.append("grette: dust recipe must inscribe forge:gems/grette into one dust (1.20.1 item key)")
    print("grette: 9 block/item pairs, 2 materials, models, loot predicates and Forge tags checked")


def check_cold_content():
    """Validate the bounded registry spaces, old serializers, loot and fluid-only model roots."""
    ores = {"cryotheum_ore", "nether_cryotheum_ore", "end_cryotheum_ore"}
    expected = {
        "BLOCKS": ores | {"cryotheum_ice_cream", "cryotheum_ice_cream_block", "dissolution_solution"},
        "ITEMS": ores | {"cryotheum_ice_cream", "cryotheum_ice_cream_block",
                         "cryotheum_ice_cream_bucket", "dissolution_bucket"},
        "FLUIDS": {"cryotheum_ice_cream", "flowing_cryotheum_ice_cream",
                   "dissolution_solution", "flowing_dissolution_solution",
                   "powder_snow", "flowing_powder_snow"},
        "FLUID_TYPES": {"cryotheum_ice_cream", "dissolution_solution", "powder_snow"},
    }
    for kind, names in expected.items():
        actual = COLD_REGISTRATIONS[kind]
        if set(actual) != names or len(actual) != len(names):
            problems.append(f"cold: {kind} registrations {actual}, expected once each: {sorted(names)}")
    for loc in ("en_us", "zh_cn"):
        for kind, names in (("block", expected["BLOCKS"]),
                            ("item", expected["ITEMS"] - ores - {"cryotheum_ice_cream_block"}),
                            ("fluid", {"powder_snow"}),
                            ("fluid_type", expected["FLUID_TYPES"])):
            for name in names:
                if f"{kind}.{NS}.{name}" not in LANG_KEYS[loc]:
                    problems.append(f"cold: {loc} missing {kind}.{NS}.{name}")
    for name in expected["ITEMS"]:
        if not os.path.isfile(os.path.join(MODEL_DIR, "item", name + ".json")):
            problems.append(f"cold: item model missing for {name}")
    for name in expected["BLOCKS"]:
        if not os.path.isfile(os.path.join(BLOCKSTATE_DIR, name + ".json")):
            problems.append(f"cold: blockstate missing for {name}")
    for name in ores | {"cryotheum_ice_cream_block"}:
        path = os.path.join(DATA_DIR, "loot_tables", "blocks", name + ".json")
        if not os.path.isfile(path):
            problems.append(f"cold: loot table missing for {name}")
            continue
        data = json.load(open(path, encoding="utf-8"))
        if "random_sequence" in data:
            problems.append(f"cold: {name} still has a 1.21 random_sequence")
        if name in ores:
            children = data.get("pools", [{}])[0].get("entries", [{}])[0].get("children", [])
            predicate = {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}
            if (len(children) != 2 or children[0].get("conditions") != [
                    {"condition": "minecraft:match_tool", "predicate": predicate}]
                    or children[0].get("name") != NS + ":" + name
                    or children[1].get("name") != "neoecoae:cryotheum_crystal"
                    or children[1].get("functions") != [
                        {"enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops",
                         "function": "minecraft:apply_bonus"}, {"function": "minecraft:explosion_decay"}]):
                problems.append(f"cold: {name} must preserve Silk Touch, Fortune and eco crystal drops")
        elif data.get("pools", [{}])[0].get("entries") != [
                {"type": "minecraft:item", "name": NS + ":cryotheum_ice_cream_block"}]:
            problems.append("cold: solid dessert must drop itself")
    # The liquid forms intentionally have no loot and no BlockItem. Their source ids overlap a food
    # or fluid name, so a lang-derived inventory cannot establish this distinction.
    for name in ("cryotheum_ice_cream", "dissolution_solution"):
        if os.path.exists(os.path.join(DATA_DIR, "loot_tables", "blocks", name + ".json")):
            problems.append(f"cold: liquid {name} must not acquire a solid-block loot table")
    for rel, values in (("ores/cryotheum", {NS + ":" + name for name in ores}),
                        ("ores", {"#forge:ores/cryotheum"})):
        path = os.path.join(RES, "data", "forge", "tags", "blocks", rel + ".json")
        if not os.path.isfile(path) or set(json.load(open(path, encoding="utf-8")).get("values", [])) != values:
            problems.append(f"cold: Forge block tag {rel} has incorrect members")
    # Still/flowing sprites are Java-only roots, outside ordinary model closure.
    sprites = {"block/dissolution_solution_still", "block/dissolution_solution_flow",
               "block/cryotheum_ice_cream_block"}
    atlas_path = os.path.join(RES, "assets", "minecraft", "atlases", "blocks.json")
    atlas = json.load(open(atlas_path, encoding="utf-8")) if os.path.isfile(atlas_path) else {}
    stitched = {entry.get("resource") for entry in atlas.get("sources", []) if entry.get("type") in ("single", "minecraft:single")}
    for sprite in sprites:
        if (NS + ":" + sprite not in stitched or f'id("{sprite}")' not in COLD_TEXT
                or not os.path.isfile(os.path.join(TEXTURE_DIR, sprite + ".png"))):
            problems.append(f"cold: fluid sprite {sprite} is missing a Java/atlas/PNG root")
    if ("minecraft:block/powder_snow" not in stitched
            or '"minecraft", "block/powder_snow"' not in COLD_TEXT):
        problems.append("cold: powder snow must reference vanilla's stitched sprite for still and flowing fluid")
    powder_tag = os.path.join(RES, "data", "c", "tags", "fluids", "powder_snow.json")
    if (not os.path.isfile(powder_tag)
            or set(json.load(open(powder_tag, encoding="utf-8")).get("values", [])) != {NS + ":powder_snow"}):
        problems.append("cold: c:powder_snow must include only our source fluid")
    bucket_cap = os.path.join(JAVA_DIR, "event", "PowderSnowBucketCapabilities.java")
    bucket_handler = os.path.join(JAVA_DIR, "items", "PowderSnowBucketFluidHandler.java")
    if (not os.path.isfile(bucket_cap) or not os.path.isfile(bucket_handler)
            or "Items.POWDER_SNOW_BUCKET" not in open(bucket_cap, encoding="utf-8").read()
            or "ForgeCapabilities.FLUID_HANDLER_ITEM" not in open(bucket_cap, encoding="utf-8").read()
            or "Items.BUCKET" not in open(bucket_handler, encoding="utf-8").read()):
        problems.append("cold: vanilla powder snow bucket capability/read-drain conversion is missing")
    bucket = json.load(open(os.path.join(MODEL_DIR, "item", "dissolution_bucket.json"), encoding="utf-8"))
    if bucket != {"parent": "forge:item/bucket_drip", "fluid": NS + ":dissolution_solution",
                  "loader": "forge:fluid_container"}:
        problems.append("cold: dissolution bucket must use Forge 47's fluid_container model")
    for name in ("cryotheum_ice_cream", "cryotheum_ice_cream_bucket"):
        model = json.load(open(os.path.join(MODEL_DIR, "item", name + ".json"), encoding="utf-8"))
        if model != {"parent": "minecraft:item/generated", "textures": {"layer0": NS + ":item/" + name}}:
            problems.append(f"cold: {name} must keep the delivered flat icon")
    # Exact source artwork, no local source-repository dependency needed by the gate.
    import hashlib
    delivered = {
        "block/cryotheum_ice_cream_block": "d9ca8e3983d4adb51ea69a92d362c5e5f011c798980d2a8ddd99604086da4615",
        "item/cryotheum_ice_cream": "e691bd623b47673890cc12ed555da0b377e11d89c40acf934786da6e2f71899a",
        "item/cryotheum_ice_cream_bucket": "3bf29a2b5a68ba1b9c12a6e378d3a1d51eb6c03bec96789ba9b1795b85213275",
    }
    for sprite, digest in delivered.items():
        path = os.path.join(TEXTURE_DIR, sprite + ".png")
        if not os.path.isfile(path) or hashlib.sha256(open(path, "rb").read()).hexdigest() != digest:
            problems.append(f"cold: {sprite} differs from the integrated source artwork")
    expected_recipes = {
        "cryotheum_ice_cream": {"type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [{"item": item} for item in ("minecraft:glass_bottle", "minecraft:sugar",
                                                        "neoecoae:cryotheum", "minecraft:snowball")],
            "result": {"count": 1, "item": NS + ":cryotheum_ice_cream"}},
        "cryotheum_ice_cream_bucket": {"type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [{"item": NS + ":cryotheum_ice_cream"}] * 4 + [{"item": "minecraft:bucket"}],
            "result": {"count": 1, "item": NS + ":cryotheum_ice_cream_bucket"}},
        "cryotheum_ice_cream_from_bucket": {"type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [{"item": NS + ":cryotheum_ice_cream_bucket"}] + [{"item": "minecraft:glass_bottle"}] * 4,
            "result": {"count": 4, "item": NS + ":cryotheum_ice_cream"}},
        "integrated_working_station/cryotheum_ice_cream": {"type": "neoecoae:integrated_working_station",
            "energy": 1000, "inputFluid": {"amount": 1000, "tag": "minecraft:water"},
            "inputItems": [{"count": 4, "item": "neoecoae:cryotheum"},
                           {"count": 2, "item": "minecraft:sugar"}, {"count": 2, "item": "minecraft:snowball"}],
            "fluidOutput": {"amount": 1000, "fluid": NS + ":cryotheum_ice_cream"}},
        "cooling/cryotheum_ice_cream": {"type": "neoecoae:cooling", "coolant": 10000,
            "input": {"amount": 100, "fluid": NS + ":cryotheum_ice_cream"}, "max_overclock": 8, "output": {}},
        "cooling/powder_snow": {"type": "neoecoae:cooling", "coolant": 6000,
            "input": {"amount": 100, "tag": "c:powder_snow"}, "max_overclock": 4,
            "output": {"amount": 100, "fluid": "minecraft:water"}},
    }
    for rel, expected_recipe in expected_recipes.items():
        path = os.path.join(RECIPE_DIR, rel + ".json")
        if not os.path.isfile(path) or json.load(open(path, encoding="utf-8")) != expected_recipe:
            problems.append(f"cold: {rel} must preserve quantities and the eco 20.5.10/1.20.1 schema")
    print("cold: 6 blocks, 7 items, 6 fluids, 3 fluid types; source art, loot, tags, bucket handler and 6 recipes checked")


def main():
    if not os.path.isdir(ADV_DIR):
        print(f"no advancements at {ADV_DIR}")
        return 1
    check_pages()
    check_recipes()
    check_data_ids()
    check_models()
    check_texture_frames()
    check_block_tags()
    check_blockstates()
    check_grette()
    check_cold_content()
    check_jade_keys()
    check_slot_count()
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
