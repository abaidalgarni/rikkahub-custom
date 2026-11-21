// AUTHOR: Abdullah M. Algarni
// Apply the maintained customization to a clean upstream release; fail on drift.
import { readFile, writeFile } from 'node:fs/promises';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const source = resolve(process.argv[2] ?? '.');
const patch = resolve(dirname(fileURLToPath(import.meta.url)), 'cx_image-sharing.patch');
execFileSync('git', ['apply', '--check', patch], { cwd: source, stdio: 'inherit' });
const file = resolve(source, 'app/build.gradle.kts');
let text = await readFile(file, 'utf8');
function replaceOnce(before, after) {
  if (text.split(before).length !== 2) throw new Error(`Upstream build script changed near: ${before}`);
  text = text.replace(before, after);
}
replaceOnce('        applicationId = "me.rerere.rikkahub"', '        applicationId = "dev.abaidalgarni.rikkahub"');
const versions = [...text.matchAll(/^        versionName = "([^"]+)"$/gm)];
if (versions.length !== 1 || !/^\d+\.\d+\.\d+$/.test(versions[0][1])) throw new Error('Unsupported upstream version');
replaceOnce(versions[0][0], `${versions[0][0]}

        val customRevision = providers.gradleProperty("customRevision").orElse("1").get().toInt()
        require(customRevision in 1..9999)
        versionCode = versionCode!! * 10000 + customRevision
        versionName = "$versionName-custom.$customRevision"`);
replaceOnce('    alias(libs.plugins.google.services)\n', '');
replaceOnce('    alias(libs.plugins.firebase.crashlytics)\n', '');
replaceOnce('    // Firebase\n    implementation(platform(libs.firebase.bom))\n    implementation(libs.firebase.analytics)\n    implementation(libs.firebase.crashlytics)\n\n', '');
execFileSync('git', ['apply', patch], { cwd: source, stdio: 'inherit' });
await writeFile(file, text);
console.log(`CUSTOMIZATION_COMPLETE upstream=${versions[0][1]}`);
