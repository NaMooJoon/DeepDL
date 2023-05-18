import sentencepiece as spm
import os 

os.chdir("./src/main/resources/sentencepiece/")

sp = spm.SentencePieceProcessor()
vocab_file = "imdb.model"
sp.load(vocab_file)

print("======>")
print(sp.decode([0,0,0,0,0,0,6,7,8,9]))
print("======>")
