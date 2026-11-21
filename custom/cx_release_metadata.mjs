// AUTHOR: Abdullah M. Algarni
// Generate the custom update feed from the actual signed APK.
import { readFile, writeFile, stat } from 'node:fs/promises';
import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { basename, resolve } from 'node:path';

const apk = resolve(process.argv[2]);
const aapt = process.argv[3];
const repository = process.env.GITHUB_REPOSITORY ?? 'abaidalgarni/rikkahub-custom';
if (repository !== 'abaidalgarni/rikkahub-custom') throw new Error('Unexpected update repository');
const badging = execFileSync(aapt, ['dump', 'badging', apk], { encoding: 'utf8' });
const packageName = badging.match(/^package: name='([^']+)'/m)?.[1];
const version = badging.match(/^package: .*versionName='([^']+)'/m)?.[1];
const versionCode = badging.match(/^package: .*versionCode='([^']+)'/m)?.[1];
if (packageName !== 'dev.abaidalgarni.rikkahub' || !/^\d+\.\d+\.\d+-custom\.\d+$/.test(version ?? '')) {
  throw new Error('APK identity/version verification failed');
}
const filename = basename(apk);
const sha256 = createHash('sha256').update(await readFile(apk)).digest('hex');
const size = (await stat(apk)).size;
const changelog = 'RikkaHub Custom: Android image sharing, multiple image attachments, separate app identity, and custom updates. Based on upstream RikkaHub ' + version.split('-custom.')[0] + '.';
await writeFile(resolve(apk, '..', 'update.json'), JSON.stringify({
  version,
  publishedAt: new Date().toISOString(),
  changelog,
  downloads: [{ name: filename, url: `https://github.com/${repository}/releases/download/${version}/${filename}`, size: `${(size / 1048576).toFixed(1)} MB` }]
}, null, 2) + '\n');
await writeFile(resolve(apk, '..', 'SHA256SUMS'), `${sha256}  ${filename}\n`);
await writeFile(resolve(apk, '..', 'release-notes.md'), `${changelog}\n\nPackage: \`${packageName}\`\nVersion code: \`${versionCode}\`\n\nInstall over the previous **RikkaHub Custom** build. The official RikkaHub app has a separate package.\n`);
console.log(`RELEASE_METADATA_COMPLETE version=${version} versionCode=${versionCode} sha256=${sha256}`);
