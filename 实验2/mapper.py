#!/usr/bin/env python3
import sys
import os
import re

stopwords = set(['a','an','the','and','or','but','is','are','was','were','of','to','in','on','at','for','with','by','this','that','it','as','from','be'])

filename = os.environ.get('mapreduce_map_input_file')
if not filename:
    filename = 'local_test.txt'
filename = os.path.basename(filename)

for line in sys.stdin:
    line = line.lower()
    words = re.findall(r'[a-z]+', line)
    for word in words:
        if word not in stopwords and len(word) > 1: 
            print(f"{word}\t{filename}")
