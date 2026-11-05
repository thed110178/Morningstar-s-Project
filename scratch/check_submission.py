import sys, re, glob
sys.stdout.reconfigure(encoding='utf-8')

files = glob.glob('f2026-final-project/f2026-final-project/*.md')

keywords = ['leader', 'representative', 'đại diện', 'one student', 'each student', 'all student', 'per group', 'each member', 'all member', 'individual', 'group submission', 'who submit', 'submit by']

for fpath in files:
    with open(fpath, 'r', encoding='utf-8') as f:
        text = f.read()
    print(f'=== {fpath} ===')
    for kw in keywords:
        matches = re.findall(rf'.{{0,40}}{kw}.{{0,40}}', text, re.IGNORECASE)
        if matches:
            print(f'Keyword "{kw}":')
            for m in matches[:5]:
                print('   -> ' + m.strip().replace('\n', ' '))

