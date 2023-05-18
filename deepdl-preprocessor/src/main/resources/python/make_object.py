from bpe import Encoder
import pickle
import sys

if len(sys.argv) != 3:
    print("Insufficient arguments")
    sys.exit()

input_file = sys.argv[1]
output_file = sys.argv[2]

f = open(input_file, 'r')   
test_corpus = f.read()                

encoder = Encoder(80000,1)  # params chosen for demonstration purposes
encoder.fit(test_corpus.split('\n'))

print(encoder.bpe_vocab)

with open(output_file, 'wb') as file:
    pickle.dump(encoder, file);

print("Done!!")
file.close() 
