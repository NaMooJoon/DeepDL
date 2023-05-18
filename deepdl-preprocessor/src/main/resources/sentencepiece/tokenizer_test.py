import csv
import sentencepiece as spm
import sys
import os 

resources = sys.argv[1] # resources/sentencepiece
input_csv_file = sys.argv[2] + sys.argv[3] + sys.argv[4]
output_csv_file = "../output/" + sys.argv[3] + sys.argv[4]

os.chdir(sys.argv[1])
if not os.path.exists(os.path.dirname("../output/" + sys.argv[3])):
    os.makedirs(os.path.dirname("../output/" + sys.argv[3]))

sp = spm.SentencePieceProcessor()
vocab_file = "imdb.model"
sp.load(vocab_file)

def process_data(data):
    # Filename,Line1,Line2,Line3,Line4,Line5,Buggy
    data["Line1"] = "[SOS]"+ data["Line1"].strip() + "[EOL]"
    data["Line2"] = data["Line2"].strip() + "[EOL]"
    data["Line3"] = data["Line3"].strip() + "[EOL]"
    data["Line4"] = data["Line4"].strip() + "[EOL]"
    data["Line5"] = data["Line5"].strip() + "[EOS]"

    data["Line1"] = sp.encode_as_ids(data["Line1"].strip('\"'))
    data["Line2"] = sp.encode_as_ids(data["Line2"].strip('\"'))
    data["Line3"] = sp.encode_as_ids(data["Line3"].strip('\"'))
    data["Line4"] = sp.encode_as_ids(data["Line4"].strip('\"'))
    data["Line5"] = sp.encode_as_ids(data["Line5"].strip('\"'))

    tokenized = {}
    tokenized["Filename"] = data["Filename"]
    tokenized["CentralLine"] = data["Line3"]
    tokenized["ContextLine"] = data["Line1"] + data["Line2"] + data["Line4"] + data["Line5"]
    tokenized["Buggy"] = data["Buggy"]

    return tokenized

# read the json file.
with open(input_csv_file, "r") as input_file, open(output_csv_file, "w", newline="") as output_file:
    reader = csv.DictReader(input_file)
    fieldnames = ["Filename", "CentralLine", "ContextLine", "Buggy"]
    writer = csv.DictWriter(output_file, fieldnames=fieldnames)
    writer.writeheader()

    for row in reader:
        processed_data = process_data(row)
        writer.writerow(processed_data)


