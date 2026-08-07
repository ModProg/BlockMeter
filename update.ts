#! /usr/bin/env -S deno run --allow-all
type Versions = {
  game: {
    version: `${number}.${number}${`.${number}` | ""}`;
    stable: boolean;
  }[];
  mappings: {
    gameVersion: string;
    seperator: string;
    build: number;
    maven: string;
    version: string;
    stable: boolean;
  }[];
  intermediary: {
    maven: string;
    version: string;
    stable: boolean;
  }[];
  loader: {
    separator: string;
    build: number;
    maven: string;
    version: string;
    stable: boolean;
  }[];
  installer: {
    url: string;
    maven: string;
    version: string;
    stable: boolean;
  };
};

const NO_VERSION = "NO VERSION FOUND";

const overrides: {
  [key: string]: {
    version?: { [game_version: string]: string };
    strip: string[];
  };
} = {
  "cloth-config": {
    "strip": ["+fabric"]
  }
};

const mods = ["fabric-api", "modmenu", "cloth-config"];

async function get_mod_version(
  mod: string,
  game_version: string,
): Promise<string> {
  const response = await fetch(
    `https://api.modrinth.com/v2/project/${mod}/version?game_versions=["${game_version}"]&loaders=["fabric"]`,
  );
  const versions: { version_number: string }[] = await response.json();
  const override = overrides[mod]?.version?.[game_version];
  if (override) return override;
  if (versions.length > 0) {
    let version = versions[0].version_number;
    for (const strip of overrides[mod]?.strip || []) {
      version = version.replace(strip, "");
    }
    return version;
  } else {
    return NO_VERSION;
  }
}

function parseProperties(content: string): {
  lines: string[];
  map: Map<string, { lineIndex: number; value: string }>;
} {
  const lines = content.split(/\r?\n/);
  const map = new Map<string, { lineIndex: number; value: string }>();
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const m = line.match(/^\s*([^#\s][^=]*)=(.*)$/);
    if (m) {
      const key = m[1].trim();
      const value = m[2].trim();
      map.set(key, { lineIndex: i, value });
    }
  }
  return { lines, map };
}

function buildUpdatedContent(
  originalContent: string,
  updates: Record<string, string>,
): string {
  const parsed = parseProperties(originalContent);
  const { lines, map } = parsed;

  for (const [k, v] of Object.entries(updates)) {
    if (map.has(k)) {
      const idx = map.get(k)!.lineIndex;
      lines[idx] = `${k}=${v}`;
    } else {
      lines.push(`${k}=${v}`);
    }
  }

  if (lines.length === 0 || lines[lines.length - 1] !== "") {
    return lines.join("\n") + "\n";
  } else {
    return lines.join("\n");
  }
}

if (Deno.args.length < 1) {
  console.error("Usage: deno run --allow-all script.ts <minecraft_version>");
  Deno.exit(1);
}
const targetVersion = Deno.args[0];

const versions_fetch = await fetch("https://meta.fabricmc.net/v2/versions/");
const versions = await versions_fetch.json() as Versions;

const loader = versions.loader[0];

const gameEntry = versions.game.find((g) => g.version === targetVersion && g.stable);
if (!gameEntry) {
  console.error(`Minecraft version ${targetVersion} not found or not stable.`);
  Deno.exit(1);
}

const mappings = versions.mappings.findLast((mapping) =>
  mapping.gameVersion == targetVersion
);

const mod_versions = await Promise.all(
  mods.map(async (mod) =>
    [mod.replace("-", "_"), await get_mod_version(mod, targetVersion)] as const
  ),
);

const updates: Record<string, string> = {
  minecraft_version: targetVersion,
  // loom_version: mappings?.version || NO_VERSION,
  loader_version: loader.version,
};

for (const [key, ver] of mod_versions) {
  updates[`${key}_version`] = ver;
}

const gradlePath = "gradle.properties";
let gradleContent = "";
try {
  gradleContent = Deno.readTextFileSync(gradlePath);
} catch {
  gradleContent = "";
}

gradleContent = buildUpdatedContent(gradleContent, updates);
Deno.writeTextFileSync(gradlePath, gradleContent);
console.log(`Updated ${gradlePath} for minecraft ${targetVersion}`);
