from bpe import Encoder
import pickle
import sys

if len(sys.argv) != 3:
    print("Insufficient arguments")
    print(len(sys.argv));
    sys.exit()

file_path = sys.argv[1] + "python/dictionary.p"

with open(file_path, 'rb') as file:
    encoder = pickle.load(file)

codeLines = sys.argv[2]
print('\n>\n')
print(encoder.tokenize(codeLines))
print('\n>\n')
# ['__sow', 'vi', 'z', 'zi', 'ni', '__eow', '__sow', ':', '__eow', 'he', 'didn', "'", 't', 'fall', '__sow', '?', '__eow', '__sow', 'in', 'co', 'n', 'ce', 'iv', 'ab', 'le', '__eow', '__sow', '!', '__eow']
print(next(encoder.transform([codeLines])))
print('\n>\n')
# [24, 108, 82, 83, 71, 25, 24, 154, 25, 14, 10, 11, 12, 13, 24, 85, 25, 24, 140, 59, 39, 157, 87, 165, 114, 25, 24, 148, 25]
print(next(encoder.inverse_transform(encoder.transform([codeLines]))))
# vizzini : he didn ' t fall ? inconceivable !
