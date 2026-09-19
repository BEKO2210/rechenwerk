import re

base = 'app/src/main/res'


def namen(p):
    t = open(p, encoding='utf-8').read()
    return set(re.findall(r'<(?:string|plurals) name="([^"]+)"', t))


d = namen(base + '/values/strings.xml')
print('default', len(d))
for l in ['en', 'es', 'fr', 'it', 'pl', 'tr']:
    o = namen(base + '/values-' + l + '/strings.xml')
    print(l, len(o), 'fehlt:', sorted(d - o), 'extra:', sorted(o - d))
