import re

pluginYml = "src/main/resources/plugin.yml"

pattern = re.compile(r"build (\d+)")

with open(pluginYml, 'r+') as f:
    text = f.read()
    m = pattern.search(text)
    buildid = int(m.group(1))
    text = re.sub(m.group(0), 'build '+str(buildid+1), text)
    f.seek(0)
    f.write(text)
    f.truncate()