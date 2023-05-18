import sentencepiece as spm
import sys
import csv
import os 
import json

os.chdir(sys.argv[1])

sp = spm.SentencePieceProcessor()
vocab_file = "imdb.model"
sp.load(vocab_file)

def process_data(data):
    # CentralLine, ContextLine
    tokenized = {}
    tokenized["CentralLine"] = data[2]
    tokenized["ContextLine"] = data[0] + data[1] + data[3] + data[4]

    return tokenized

# read the json file.
with open("../lineblocks.json", "r") as input_file, open('../output/train.csv', 'w', newline="") as output_file:
    reader = json.loads(input_file.read())
    fieldnames = ["CentralLine", "ContextLine"]
    writer = csv.DictWriter(output_file, fieldnames=fieldnames)
    writer.writeheader()

    for linblock in reader:
        list = []
        for line in linblock:
            list.append(sp.encode_as_ids(line))
        processed_data = process_data(list)
        writer.writerow(processed_data)

