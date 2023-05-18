from bpe import Encoder
import pickle
import sys

if len(sys.argv) != 2:
    print("Insufficient arguments")
    sys.exit()

file_path = sys.argv[1]

with open(file_path, 'rb') as file:
    encoder = pickle.load(file)

example = "System.out.println(\"Hello World\");"
print(encoder.tokenize(example))
# ['__sow', 'vi', 'z', 'zi', 'ni', '__eow', '__sow', ':', '__eow', 'he', 'didn', "'", 't', 'fall', '__sow', '?', '__eow', '__sow', 'in', 'co', 'n', 'ce', 'iv', 'ab', 'le', '__eow', '__sow', '!', '__eow']
print(next(encoder.transform([example])))
# [24, 108, 82, 83, 71, 25, 24, 154, 25, 14, 10, 11, 12, 13, 24, 85, 25, 24, 140, 59, 39, 157, 87, 165, 114, 25, 24, 148, 25]
print(next(encoder.inverse_transform(encoder.transform([example]))))
# vizzini : he didn ' t fall ? inconceivable !

print()
# example = "if ((exampleCode.startsWith(\"{\") && !exampleCode.endsWith(\"}\"))"
example = "SubscriptionResultsList subscriptionResults = uddiSubscriptionService.getSubscriptionResults(req);"
print(next(encoder.transform([example])))
print(next(encoder.inverse_transform(encoder.transform([example]))))

