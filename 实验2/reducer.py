#!/usr/bin/env python3
import sys
from collections import defaultdict

current_word = None
doc_counts = defaultdict(int)

def emit_result(word, counts):
    total = sum(counts.values())
    doc_str = ";".join([f"<{doc},{cnt}>" for doc, cnt in sorted(counts.items())])
    print(f"{word} {doc_str};<total,{total}>.")

for line in sys.stdin:
    line = line.rstrip('\n')  # 只去换行符，保留制表符
    if not line: continue
    
    parts = line.split('\t')
    if len(parts) < 2: continue
    
    word = parts[0].strip()
    doc = parts[1].strip()

    if current_word == word:
        doc_counts[doc] += 1
    else:
        if current_word:
            emit_result(current_word, doc_counts)
        current_word = word
        doc_counts = defaultdict(int)
        doc_counts[doc] = 1

if current_word:
    emit_result(current_word, doc_counts)
